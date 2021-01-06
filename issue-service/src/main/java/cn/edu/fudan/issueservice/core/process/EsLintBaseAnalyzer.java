package cn.edu.fudan.issueservice.core.process;

import cn.edu.fudan.issueservice.dao.CommitDao;
import cn.edu.fudan.issueservice.domain.dbo.Location;
import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.domain.enums.JavaScriptIssuePriorityEnum;
import cn.edu.fudan.issueservice.domain.enums.ToolEnum;
import cn.edu.fudan.issueservice.exception.ParseFileException;
import cn.edu.fudan.issueservice.util.AstParserUtil;
import cn.edu.fudan.issueservice.util.FileFilter;
import cn.edu.fudan.issueservice.util.FileUtil;
import cn.edu.fudan.issueservice.util.JGitHelper;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.List;

/**
 * @author Beethoven
 */
@Setter
@Slf4j
public class EsLintBaseAnalyzer extends BaseAnalyzer {

    private String resultFileHome;

    private CommitDao commitDao;

    @Override
    public boolean invoke(String repoUuid, String repoPath, String commit) {
        try {
            Runtime rt = Runtime.getRuntime();
            //ESLint exe command
            String command = binHome + "executeESLint.sh " + repoPath + " " + repoUuid + "_" + commit;
            log.info("command -> {}",command);
            Process process = rt.exec(command);
            //wait command 200s,if time > 200,invoke tool failed
            boolean timeout = process.waitFor(200L, TimeUnit.SECONDS);
            if (!timeout) {
                process.destroy();
                log.error("invoke tool timeout ! (200s)");
                return false;
            }
            return process.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean analyze(String repoPath, String repoUuid, String commit) {
        //get esLint report file path
        String esLintReportFile = FileUtil.getEsLintReportAbsolutePath(resultFileHome, repoUuid, commit);
        //read result from json file
        try (BufferedReader reader = new BufferedReader(new FileReader(esLintReportFile))) {
            int ch;
            char[] buf = new char[1024];
            StringBuilder data = new StringBuilder();
            while ((ch = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, ch);
                data.append(readData);
            }
            log.info("read esLint report file success !");
            //delete esLint report file
            deleteEsLintReportFile(esLintReportFile);
            return analyzeEsLintResults(repoPath, (JSONArray) JSONArray.parse(data.toString()), repoUuid, commit);
        } catch (Exception e) {
            log.error("read esLint report file error,projectName is ---> {}", repoUuid + "_" + commit);
        }
        return false;
    }

    private void deleteEsLintReportFile(String esLintReportFile) {
        try {
            Runtime rt = Runtime.getRuntime();
            String command = binHome + "deleteESLintReport.sh " + esLintReportFile;
            log.info("command -> {}",command);
            Process process = rt.exec(command);
            boolean timeout = process.waitFor(20L, TimeUnit.SECONDS);
            if (!timeout) {
                process.destroy();
                log.error("delete esLint report file {} timeout ! (20s)", esLintReportFile);
                return;
            }
            log.info("delete esLint report file {} success !", esLintReportFile);
        } catch (Exception e) {
            log.error("delete esLint report file {} failed !", esLintReportFile);
        }
    }


    private boolean analyzeEsLintResults(String repoPath, JSONArray esLintResults, String repoUuid, String commit) {
        try {
            for(Object esLintTempResult : esLintResults) {
                JSONObject esLintResult = (JSONObject) esLintTempResult;
                if(!FileFilter.jsFileFilter(esLintResult.getString("filePath"))) {
                    //file ---> esLintResult
                    resultRawIssues.addAll(handleEsLintResults(repoPath, esLintResult, repoUuid, commit));
                }
            }
            return true;
        }catch (Exception e){
            log.error("analyze rawIssues failed!");
        }
        return false;
    }

    List<RawIssue> handleEsLintResults(String repoPath, JSONObject esLintResult, String repoUuid, String commit) throws ParseFileException {
        List<RawIssue> rawIssues = new ArrayList<>();
        //handle the ESLint result;
        JSONArray rawIssueList = esLintResult.getJSONArray("messages");
        if(rawIssueList == null || rawIssueList.size() == 0){
            return new ArrayList<>();
        }
        //get file path
        String filePath = esLintResult.getString("filePath");
        //handle file name
        String fileName = FileUtil.handleFileNameToRelativePath(filePath);
        //parse js code ---> return node json,if null throws exception
        JSONObject nodeJsCode = AstParserUtil.parseJsCode(binHome, filePath, resultFileHome, repoUuid);
        if(nodeJsCode == null){
            //if can't get AST result throws ParseFileException
            log.error("parse repoUuid:{} commit:{} file ---> {} failed !", repoUuid, commit, esLintResult.getString("filePath"));
            throw new ParseFileException();
        }
        //get the rawIssues
        for(Object issue : rawIssueList){
            rawIssues.add(getRawIssue(repoPath, (JSONObject) issue, repoUuid, commit, fileName, filePath, nodeJsCode));
        }
        return rawIssues;
    }

    private RawIssue getRawIssue(String repoPath, JSONObject issue, String repoUuid, String commit, String fileName, String filePath, JSONObject nodeJsCode) throws ParseFileException {
        RawIssue rawIssue = new RawIssue();
        rawIssue.setTool("ESLint");
        rawIssue.setUuid(UUID.randomUUID().toString());
        rawIssue.setType(issue.getString("ruleId"));
        rawIssue.setFile_name(fileName);
        rawIssue.setDetail(issue.getString("message") + "---" + JavaScriptIssuePriorityEnum.getPriorityByRank(issue.getInteger("severity")));
        rawIssue.setScan_id(ToolEnum.ESLINT.getType());
        rawIssue.setCommit_id(commit);
        rawIssue.setRepo_id(repoUuid);
        JGitHelper jGitInvoker = new JGitHelper (repoPath);
        String developerUniqueName = jGitInvoker.getAuthorName(commit);
        Map<String, Object> commitViewInfo = commitDao.getCommitViewInfoByCommitId(repoUuid, commit);
        if (commitViewInfo != null) {
            developerUniqueName = commitViewInfo.get("developer_unique_name") == null ? developerUniqueName : (String) commitViewInfo.get("developer_unique_name");
        }
        rawIssue.setDeveloperName(developerUniqueName);
        //set rawIssue's location
        rawIssue.setLocations(getLocations(fileName, issue, rawIssue, filePath, nodeJsCode));
        return rawIssue;
    }

    private List<Location> getLocations(String fileName, JSONObject issue, RawIssue rawIssue, String filePath, JSONObject nodeJsCode) throws ParseFileException {
        Location location = new Location();
        //get start line,end line and bug line
        int line = issue.getIntValue("line");
        int endLine = issue.getIntValue("endLine");
        //handle some condition line > endLine ?
        if(line > endLine){
            int temp = line;
            line = endLine;
            endLine = temp;
        }
        location.setStart_line(line);
        location.setEnd_line(endLine);
        //get start token and end token
        location.setStart_token(issue.getIntValue("column"));
        location.setEnd_token(issue.getIntValue("endColumn"));
        //get js code
        String code = FileUtil.getCode(filePath, line, endLine);
        location.setCode(code);
        location.setUuid(UUID.randomUUID().toString());
        location.setFile_path(fileName);
        location.setRawIssue_id(rawIssue.getUuid());
        //todo check class name
        location.setClass_name(AstParserUtil.getJsClass(nodeJsCode, line, endLine));
        //get method name
        location.setMethod_name (AstParserUtil.getJsMethod(nodeJsCode, line, endLine, filePath));
        //set bug lines
        if(line == endLine){
            location.setBug_lines(line + "");
        }else{
            StringBuilder lines = new StringBuilder();
            while(line < endLine){
                lines.append(line).append(",");
                line++;
            }
            lines.append(endLine);
            location.setBug_lines(lines.toString());
        }

        return new ArrayList<Location>(){{add(location);}};
    }

    @Override
    public String getToolName() {
        return ToolEnum.ESLINT.getType();
    }

    @Override
    public Integer getPriorityByRawIssue(RawIssue rawIssue) {
        int result = 0;
        String[] rawIssueArgs  = rawIssue.getDetail().split ("---");
        String severity = rawIssueArgs[rawIssueArgs.length - 1];
        switch (severity){
            case "Off":
                result = JavaScriptIssuePriorityEnum.OFF.getRank();
                break;
            case "Warn":
                result = JavaScriptIssuePriorityEnum.WARN.getRank();
                break;
            case "Error":
                result = JavaScriptIssuePriorityEnum.ERROR.getRank();
                break;
            default:
        }
        return result;
    }

    public static void main(String[] args) {
        EsLintBaseAnalyzer esLintBaseAnalyzer = new EsLintBaseAnalyzer();
        esLintBaseAnalyzer.setResultFileHome("C:\\Users\\Beethoven\\Desktop\\issue-tracker-web");
        esLintBaseAnalyzer.analyze("C:\\Users\\Beethoven\\Desktop\\issue-tracker-web", "6f1170ac-4102-11eb-b6ff-f9c372bb0fcb", "1f54d6ba0e5a74c3562db4d4af9d93f7e186d85b");
    }
}
