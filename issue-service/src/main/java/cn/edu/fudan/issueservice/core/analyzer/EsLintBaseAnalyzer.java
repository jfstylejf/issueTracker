package cn.edu.fudan.issueservice.core.analyzer;

import cn.edu.fudan.codetracker.scan.core.tree.JsTree;
import cn.edu.fudan.codetracker.scan.core.tree.parser.JsFileParser;
import cn.edu.fudan.codetracker.scan.domain.projectinfo.ClassNode;
import cn.edu.fudan.codetracker.scan.domain.projectinfo.FieldNode;
import cn.edu.fudan.codetracker.scan.domain.projectinfo.MethodNode;
import cn.edu.fudan.codetracker.scan.domain.projectinfo.StatementNode;
import cn.edu.fudan.issueservice.dao.CommitDao;
import cn.edu.fudan.issueservice.domain.dbo.Location;
import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.domain.enums.IssuePriorityEnums.*;
import cn.edu.fudan.issueservice.domain.enums.ToolEnum;
import cn.edu.fudan.issueservice.util.*;
import cn.edu.fudan.issueservice.util.FileFilter;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Beethoven
 */
@Setter
@Slf4j
@Component
public class EsLintBaseAnalyzer extends BaseAnalyzer {

    private CommitDao commitDao;

    @Value("${babelEsLint}")
    private String babelEsLintPath;

    @Value("${ESLintLogHome}")
    private String logHome;

    @Value("${binHome}")
    private String binHome;

    private static final String ESLINT_IGNORE = "/.eslintignore";

    @Override
    public boolean invoke(String repoUuid, String repoPath, String commit) {

        try {
            //eslint ignore
            File file = new File(repoPath + ESLINT_IGNORE);
            boolean newFile = false;
            if (!file.exists()) {
                newFile = file.createNewFile();
                if (!newFile) {
                    log.error("create .eslintignore failed!");
                    return false;
                }
            }
            //find src dir
            String srcDir = FileUtil.findSrcDir(repoPath);
            if (srcDir == null) {
                log.error("can't find this repo src path");
                checkNeedDeleteIgnoreFile(newFile, repoPath);
                return true;
            }

            addIgnoreFile(repoPath + ESLINT_IGNORE);
            boolean executeSuccess = ShUtil.executeCommand(binHome + "executeESLint.sh " + repoPath + " " + repoUuid + "_" + commit + " " + srcDir, 200);
            checkNeedDeleteIgnoreFile(newFile, repoPath);

            return executeSuccess;
        } catch (Exception e) {
            log.error("ESLint can not parse this repo,repoUuid: {},commit: {}", repoUuid, commit);
        }
        return false;
    }

    private void checkNeedDeleteIgnoreFile(boolean newFile, String repoPath) throws IOException {
        if (newFile) {
            Runtime rt = Runtime.getRuntime();
            rt.exec("rm -f " + repoPath + ESLINT_IGNORE);
        }
    }

    public void addIgnoreFile(String file) {
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)))) {
            out.write("src/assets");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean analyze(String repoPath, String repoUuid, String commit) {
        //get esLint report file path
        String esLintReportFile = FileUtil.getEsLintReportAbsolutePath(logHome, repoUuid, commit);
        String reportPath = FileUtil.getEsLintRunningLogAbsolutePath(logHome, repoUuid, commit);
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
            deleteEsLintReportFile(esLintReportFile, reportPath);
            return analyzeEsLintResults(repoPath, (JSONArray) JSONArray.parse(data.toString()), repoUuid, commit);
        } catch (Exception e) {
            log.error("read esLint report file error,projectName is ---> {}", repoUuid + "_" + commit);
        }
        return false;
    }

    private void deleteEsLintReportFile(String esLintReportFile, String reportPath) {
        try {
            Runtime rt = Runtime.getRuntime();
            String command = binHome + "deleteScanResult.sh " + esLintReportFile + " " + reportPath;
            log.info("command -> {}", command);
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
        //set babel eslint path for ast tree
        JsFileParser.setBabelPath(babelEsLintPath);
        try {
            //filter files
            for (Object esLintTempResult : esLintResults) {
                JSONObject esLintResult = (JSONObject) esLintTempResult;
                String filePath = esLintResult.getString("filePath");
                //file ---> esLintResult
                if (!FileFilter.jsFilenameFilter(filePath)) {
                    List<String> fileNames = new ArrayList<>();
                    fileNames.add(filePath);
                    //get jsTree
                    JsTree jsTree = new JsTree(fileNames, "", "");
                    getJsMethodInFile(filePath, jsTree);
                    resultRawIssues.addAll(handleEsLintResults(repoPath, esLintResult, repoUuid, commit, jsTree));
                }
            }
            log.info("eslint analyze complete, get {} issues", resultRawIssues.size());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("analyze rawIssues failed!");
        }
        return false;
    }

    private void getJsMethodInFile(String filePath, JsTree jsTree) {
        List<FieldNode> fieldInfos = jsTree.getFieldInfos();
        for (FieldNode fieldInfo : fieldInfos) {
            Set<String> set = methodsAndFieldsInFile.getOrDefault(filePath, new HashSet<>());
            set.add(fieldInfo.getSimpleType() + " " + fieldInfo.getSimpleName());
            methodsAndFieldsInFile.put(filePath, set);
        }
        List<MethodNode> methodInfos = jsTree.getMethodInfos();
        for (MethodNode methodInfo : methodInfos) {
            Set<String> set = methodsAndFieldsInFile.getOrDefault(filePath, new HashSet<>());
            set.add(methodInfo.getSignature());
            methodsAndFieldsInFile.put(filePath, set);
        }
        List<StatementNode> statementInfos = jsTree.getStatementInfos();
        for (StatementNode statementInfo : statementInfos) {
            Set<String> set = methodsAndFieldsInFile.getOrDefault(filePath, new HashSet<>());
            String statementCode = StringsUtil.removeBr(statementInfo.getBody());
            String statement = statementCode.substring(0, Math.min(statementCode.length(), 21));
            set.add(statement);
            methodsAndFieldsInFile.put(filePath, set);
        }
    }

    List<RawIssue> handleEsLintResults(String repoPath, JSONObject esLintResult, String repoUuid, String commit, JsTree jsTree) {
        List<RawIssue> rawIssues = new ArrayList<>();
        //handle the ESLint result
        JSONArray rawIssueList = esLintResult.getJSONArray("messages");
        if (rawIssueList == null || rawIssueList.isEmpty()) {
            return new ArrayList<>();
        }
        //get file path
        String filePath = esLintResult.getString("filePath");
        //handle file name
        String fileName = FileUtil.handleFileNameToRelativePath(filePath);
        //get the rawIssues
        for (Object issue : rawIssueList) {
            RawIssue rawIssue = getRawIssue(repoPath, (JSONObject) issue, repoUuid, commit, fileName, filePath, jsTree);
            if (rawIssue != null) {
                rawIssues.add(rawIssue);
            }
        }
        return rawIssues;
    }

    private RawIssue getRawIssue(String repoPath, JSONObject issue, String repoUuid, String commit, String fileName, String filePath, JsTree jsTree) {
        if (issue.getString("ruleId") == null) {
            return null;
        }
        RawIssue rawIssue = new RawIssue();
        rawIssue.setTool("ESLint");
        rawIssue.setUuid(UUID.randomUUID().toString());
        rawIssue.setType(issue.getString("ruleId"));
        rawIssue.setFileName(fileName);
        rawIssue.setDetail(issue.getString("message") + "---" + JavaScriptIssuePriorityEnum.getPriorityByRank(issue.getInteger("severity")));
        rawIssue.setScanId(ToolEnum.ESLINT.getType());
        rawIssue.setCommitId(commit);
        rawIssue.setRepoId(repoUuid);
        JGitHelper jGitInvoker = new JGitHelper(repoPath);
        String developerUniqueName = jGitInvoker.getAuthorName(commit);
        Map<String, Object> commitViewInfo = commitDao.getCommitViewInfoByCommitId(repoUuid, commit);
        if (commitViewInfo != null) {
            developerUniqueName = commitViewInfo.get("developer_unique_name") == null ? developerUniqueName : (String) commitViewInfo.get("developer_unique_name");
        }
        rawIssue.setDeveloperName(developerUniqueName);
        //set rawIssue's location
        rawIssue.setLocations(getLocations(fileName, issue, rawIssue, filePath, jsTree));
        rawIssue.setPriority(getPriorityByRawIssue(rawIssue));
        return rawIssue;
    }

    private List<Location> getLocations(String fileName, JSONObject issue, RawIssue rawIssue, String filePath, JsTree jsTree) {
        List<Location> locations = new ArrayList<>();
        Location location = new Location();
        //get start line,end line and bug line
        int line = issue.getIntValue("line");
        int endLine = issue.getIntValue("endLine");
        //handle some condition line > endLine ?
        if (line > endLine) {
            log.error("startLine > endLine,fileName is {},startLine is {},endLine is {}", fileName, line, endLine);
            int temp = line;
            line = endLine;
            endLine = temp;
        }
        location.setStartLine(line);
        location.setEndLine(endLine);
        //get start token and end token
        location.setStartToken(issue.getIntValue("column"));
        location.setEndToken(issue.getIntValue("endColumn"));
        //get js code
        String code = FileUtil.getCode(filePath, line, endLine);
        location.setCode(code);
        location.setUuid(UUID.randomUUID().toString());
        location.setFilePath(fileName);
        location.setRawIssueId(rawIssue.getUuid());
        //set location class name and method name
        //fixme todo import condition and statement
        setLocationClassNameAndMethodName(line, endLine, location, jsTree);
        //set bug lines
        location.setBugLines(endLine + "-" + line);
        locations.add(location);

        return locations;
    }

    private void setLocationClassNameAndMethodName(int line, int endLine, Location location, JsTree jsTree) {
        //set class name
        handleClassName(line, endLine, location, jsTree);
        //handle statement
        handleStatement(line, endLine, location, jsTree);
        //handle method name
        handleMethodName(line, endLine, location, jsTree);
        //handle field name
        handleFieldName(line, endLine, location, jsTree);
    }

    private void handleClassName(int line, int endLine, Location location, JsTree jsTree) {
        List<ClassNode> classInfos = jsTree.getClassInfos();
        for (ClassNode classInfo : classInfos) {
            if (classInfo.getBeginLine() <= line && classInfo.getEndLine() >= endLine) {
                location.setClassName(classInfo.getName());
                location.setOffset(line - classInfo.getBeginLine());
                break;
            }
        }
    }

    private void handleFieldName(int line, int endLine, Location location, JsTree jsTree) {
        List<FieldNode> fieldInfos = jsTree.getFieldInfos();
        for (FieldNode fieldInfo : fieldInfos) {
            String fieldName = fieldInfo.getSimpleType() + " " + fieldInfo.getSimpleName();
            if (fieldInfo.getBeginLine() <= line && fieldInfo.getEndLine() >= endLine) {
                location.setMethodName(fieldName);
                location.setOffset(line - fieldInfo.getBeginLine());
                break;
            }
        }
    }


    private void handleMethodName(int line, int endLine, Location location, JsTree jsTree) {
        List<MethodNode> methodInfos = jsTree.getMethodInfos();
        for (MethodNode methodInfo : methodInfos) {
            if (methodInfo.getBeginLine() <= line && methodInfo.getEndLine() >= endLine) {
                location.setMethodName(methodInfo.getSignature());
                location.setOffset(line - methodInfo.getBeginLine());
                break;
            }
        }
    }

    private void handleStatement(int line, int endLine, Location location, JsTree jsTree) {
        List<StatementNode> statementInfos = jsTree.getStatementInfos();
        for (StatementNode statementInfo : statementInfos) {
            if (statementInfo.getBeginLine() <= line && statementInfo.getEndLine() >= endLine) {
                String statementCode = StringsUtil.removeBr(statementInfo.getBody());
                String statement = statementCode.substring(0, Math.min(statementCode.length(), 21));
                location.setMethodName(statement);
                location.setOffset(line - statementInfo.getBeginLine());
                break;
            }
        }
    }

    @Override
    public String getToolName() {
        return ToolEnum.ESLINT.getType();
    }

    @Override
    public Integer getPriorityByRawIssue(RawIssue rawIssue) {
        int result = 0;
        String[] rawIssueArgs = rawIssue.getDetail().split("---");
        String severity = rawIssueArgs[rawIssueArgs.length - 1];
        switch (severity) {
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

    @Autowired
    public void setCommitDao(CommitDao commitDao) {
        this.commitDao = commitDao;
    }

    public static void main(String[] args) {
        EsLintBaseAnalyzer esLintBaseAnalyzer = new EsLintBaseAnalyzer();
        esLintBaseAnalyzer.setBabelEsLintPath("/Users/beethoven/Desktop/saic/IssueTracker-Master/issue-service/src/main/resources/node/babelEsLint.js");
        esLintBaseAnalyzer.setLogHome("/Users/beethoven/Desktop/saic/issue-tracker-web");
        esLintBaseAnalyzer.analyze("/Users/beethoven/Desktop/saic/issue-tracker-web", "test", "4f42e73bda0a80d044a013ef73da4d8af0f4c981");
        JsFileParser.setBabelPath("/Users/beethoven/Desktop/saic/IssueTracker-Master/issue-service/src/main/resources/node/babelEsLint.js");
        JsTree jsTree = new JsTree(Collections.singletonList("/Users/beethoven/Desktop/saic/issue-tracker-web/src/issue.js"), "", "");
        esLintBaseAnalyzer.handleFieldName(24, 25, new Location(), jsTree);
    }
}
