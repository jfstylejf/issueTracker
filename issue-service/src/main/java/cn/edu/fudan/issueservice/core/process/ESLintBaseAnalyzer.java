package cn.edu.fudan.issueservice.core.process;

import cn.edu.fudan.issueservice.dao.CommitDao;
import cn.edu.fudan.issueservice.domain.dbo.Location;
import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.domain.enums.ToolEnum;
import cn.edu.fudan.issueservice.util.ASTUtil;
import cn.edu.fudan.issueservice.util.AstParserUtil;
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
public class ESLintBaseAnalyzer extends BaseAnalyzer {

    private String resultFileHome;

    private String esLintConfigFile;

    private CommitDao commitDao;

    @Override
    public boolean invoke(String repoUuid, String repoPath, String commit) {
        try {
            Runtime rt = Runtime.getRuntime();
            //执行sonar命令,一个commit对应一个sonarqube project(repoUuid_commit)
            String command = binHome + "executeESLint.sh " + esLintConfigFile + " " + repoPath + " " + repoUuid + "_" + commit;
            log.info("command -> {}",command);
            Process process = rt.exec(command);
            //最多等待sonar脚本执行300秒,超时则认为该commit解析失败
            boolean timeout = process.waitFor(300L, TimeUnit.SECONDS);
            if (!timeout) {
                process.destroy();
                log.error("invoke tool timeout ! (300s)");
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
        //read result from json file
        try (BufferedReader reader = new BufferedReader(new FileReader(resultFileHome))) {
            int ch;
            char[] buf = new char[1024];
            StringBuilder data = new StringBuilder();
            while ((ch = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, ch);
                data.append(readData);
            }
            return analyzeEsLintResults(repoPath, (JSONArray) JSONArray.parse(data.toString()), repoUuid, commit);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("read file error,projectName is ---> {}", repoUuid + "_" + commit);
        }
        return false;
    }

    private boolean analyzeEsLintResults(String repoPath, JSONArray esLintResults, String repoUuid, String commit) {
        try {
            //add the rawIssues to result
            esLintResults.forEach(esLintResult -> resultRawIssues.addAll(handleEsLintResults(repoPath, (JSONObject) esLintResult,repoUuid , commit)));
            return true;
        }catch (Exception e){
            log.error("analyze rawIssues failed!");
        }
        return false;
    }

    List<RawIssue> handleEsLintResults(String repoPath, JSONObject esLintResult, String repoUuid, String commit){
        List<RawIssue> rawIssues = new ArrayList<>();
        //handle the ESLint result;
        JSONArray rawIssueList = esLintResult.getJSONArray("messages");
        //fixme handle fileName
        String fileName = esLintResult.getString("filePath");
        //get the rawIssues
        rawIssueList.forEach(issue -> rawIssues.add(getRawIssue(repoPath, (JSONObject) issue, repoUuid, commit, fileName)));
        return rawIssues;
    }

    private RawIssue getRawIssue(String repoPath, JSONObject issue, String repoUuid, String commit, String fileName){
        RawIssue rawIssue = new RawIssue();
        rawIssue.setTool("ESLint");
        rawIssue.setUuid(UUID.randomUUID().toString());
        rawIssue.setType(issue.getString("ruleId"));
        //fixme fileName
        rawIssue.setFile_name(fileName);
        //todo severity(int) to severity(String)
        rawIssue.setDetail(issue.getString("message") + "---" + issue.getString("severity"));
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
        rawIssue.setLocations(getLocations(fileName, issue, rawIssue));
        return rawIssue;
    }

    private List<Location> getLocations(String fileName, JSONObject issue, RawIssue rawIssue) {
        Location location = new Location();

        String code = null;
        int line = issue.getIntValue("line");
        int endLine = issue.getIntValue("endLine");
        try{
            //fixme get js code
            code = ASTUtil.getCode(line, endLine, fileName);
        }catch (Exception e){
            log.info("file path --> {} file deleted", fileName);
            log.error("rawIssueId --> {}  get code failed.", rawIssue.getUuid());
        }
        location.setStart_token(issue.getIntValue("column"));
        location.setEnd_token(issue.getIntValue("endColumn"));
        location.setCode(code);
        location.setUuid(UUID.randomUUID().toString());
        //fixme fileName
        location.setFile_path(fileName);
        location.setRawIssue_id(rawIssue.getUuid());
        location.setStart_line(line);
        location.setEnd_line(endLine);

        if(line > endLine){
            log.error("startLine number greater than endLine number");
            return null;
        }else if(line == endLine){
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
        //fixme get js methodName
        String methodName = AstParserUtil.findMethod (fileName, line, endLine);
        location.setMethod_name (methodName);

        return new ArrayList<Location>(){{add(location);}};
    }

    @Override
    public String getToolName() {
        return ToolEnum.ESLINT.getType();
    }

    @Override
    public Integer getPriorityByRawIssue(RawIssue rawIssue) {
        return null;
    }

    public static void main(String[] args) {
        ESLintBaseAnalyzer esLintBaseAnalyzer = new ESLintBaseAnalyzer();
        esLintBaseAnalyzer.setResultFileHome("C:\\Users\\Beethoven\\Desktop\\issue-tracker-web\\eslint-report.json");
        esLintBaseAnalyzer.analyze("C:\\Users\\Beethoven\\Desktop\\issue-tracker-web", "6f1170ac-4102-11eb-b6ff-f9c372bb0fcb", "8f4a106a354126e24d2173b7ceecd7407e7e4005");
    }
}
