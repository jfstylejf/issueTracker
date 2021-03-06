package cn.edu.fudan.issueservice.core.analyzer;

import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.dao.CommitDao;
import cn.edu.fudan.issueservice.domain.dbo.Location;
import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.domain.enums.RawIssueStatus;
import cn.edu.fudan.issueservice.domain.enums.ToolEnum;
import cn.edu.fudan.issueservice.util.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * description:
 *
 * @author fancying
 * create: 2020-05-20 15:55
 **/
@Slf4j
@Component
@Scope("prototype")
public class SonarQubeBaseAnalyzer extends BaseAnalyzer {

    private CommitDao commitDao;

    private RestInterfaceManager restInterfaceManager;

    @Value("${binHome}")
    private String binHome;

    private static final String COMPONENT = "component";

    @Override
    public boolean invoke(String repoUuid, String repoPath, String commit) {
        return ShUtil.executeCommand(binHome + "executeSonar.sh " + repoPath + " " + repoUuid + "_" + commit + " " + commit, 300);
    }

    @Override
    public boolean analyze(String repoPath, String repoUuid, String commit) {

        long analyzeStartTime = System.currentTimeMillis();
        boolean isChanged = false;
        try {
            // 最多等待200秒
            for (int i = 1; i <= 100; i++) {
                TimeUnit.SECONDS.sleep(2);
                JSONObject sonarIssueResults = restInterfaceManager.getSonarIssueResults(repoUuid + "_" + commit, null, 1, false, 0);
                if (sonarIssueResults.getInteger("total") != 0) {
                    isChanged = true;
                    long analyzeEndTime2 = System.currentTimeMillis();
                    log.info("It takes {}s to wait for the latest sonar result ", (analyzeEndTime2 - analyzeStartTime) / 1000);
                    break;
                }
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        //判断是否确实issue为0,还是没获取到这个commit的sonar结果
        if (!isChanged) {
            JSONObject sonarAnalysisTime = restInterfaceManager.getSonarAnalysisTime(repoUuid + "_" + commit);
            if (sonarAnalysisTime.containsKey(COMPONENT)) {
                isChanged = true;
                try {
                    log.info("200s past,the number of issue is 0,but get sonar analysis time,sonar result should be changed");
                    TimeUnit.SECONDS.sleep(4);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        //此时isChanged == false则认为解析失败
        if (!isChanged) {
            log.error("get commit {} latest sonar result failed!", commit);
            // todo return 之前存储所有的issue 结果 存储解析失败

            return false;
        }
        //解析sonar的issues为平台的rawIssue
        boolean getRawIssueSuccess = getSonarResult(repoUuid, commit, repoPath);
        //删除本次sonar库
        deleteSonarProject(repoUuid + "_" + commit);

        // todo return 之前存储所有的issue 结果

        return getRawIssueSuccess;
    }

    private void deleteSonarProject(String projectName) {
        try {

            Runtime rt = Runtime.getRuntime();
            String command = binHome + "deleteSonarProject.sh " + projectName + " " + DatatypeConverter.printBase64Binary((restInterfaceManager.sonarLogin + ":" + restInterfaceManager.sonarPassword).getBytes(StandardCharsets.UTF_8));
            log.info("command -> {}", command);
            if (rt.exec(command).waitFor() == 0) {
                log.info("delete sonar project:{} success! ", projectName);
            }
        } catch (Exception e) {
            log.error("delete sonar project:{},cause:{}", projectName, e.getMessage());
        }
    }


    private boolean getSonarResult(String repoUuid, String commit, String repoPath) {
        //获取issue数量
        JSONObject sonarIssueResult = restInterfaceManager.getSonarIssueResults(repoUuid + "_" + commit, null, 1, false, 0);
        try {
            List<Location> allLocations = new ArrayList<>();
            int pageSize = 100;
            int issueTotal = sonarIssueResult.getIntValue("total");
            log.info("Current commit {}, issueTotal in sonar result is {}", commit, issueTotal);
            //分页取sonar的issue
            int pages = issueTotal % pageSize > 0 ? issueTotal / pageSize + 1 : issueTotal / pageSize;
            for (int i = 1; i <= pages; i++) {
                JSONObject sonarResult = restInterfaceManager.getSonarIssueResults(repoUuid + "_" + commit, null, pageSize, false, i);
                JSONArray sonarRawIssues = sonarResult.getJSONArray("issues");
                //解析sonar的issues为平台的rawIssue
                for (int j = 0; j < sonarRawIssues.size(); j++) {
                    JSONObject sonarIssue = sonarRawIssues.getJSONObject(j);
                    //仅解析java文件且非test文件夹
                    String component = sonarIssue.getString(COMPONENT);
                    if (FileFilter.javaFilenameFilter(component)) {
                        continue;
                    }
                    String rawIssueUuid = UUID.randomUUID().toString();
                    //解析location
                    List<Location> locations = getLocations(rawIssueUuid, sonarIssue, repoPath, allLocations);
                    //解析rawIssue
                    RawIssue rawIssue = getRawIssue(repoUuid, commit, ToolEnum.SONAR.getType(), rawIssueUuid, sonarIssue, repoPath);
                    rawIssue.setLocations(locations);
                    rawIssue.setStatus(RawIssueStatus.DEFAULT.getType());
                    resultRawIssues.add(rawIssue);
                }
            }
            log.info("Current commit {}, rawIssue total is {}", commit, resultRawIssues.size());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getToolName() {
        return ToolEnum.SONAR.getType();
    }

    @Override
    public Integer getPriorityByRawIssue(RawIssue rawIssue) {
        int result = 1;
        String detail = rawIssue.getDetail();
        String[] rawIssueArgs = detail.split("---");
        String severity = rawIssueArgs[rawIssueArgs.length - 1];
        switch (severity) {
            case "BLOCKER":
                result = 0;
                break;
            case "CRITICAL":
                result = 1;
                break;
            case "MAJOR":
                result = 2;
                break;
            case "MINOR":
                result = 3;
                break;
            case "INFO":
                result = 4;
                break;
            default:
        }
        return result;
    }

    public List<Location> getLocations(String rawIssueUuid, JSONObject issue, String repoPath, List<Location> allLocations) throws Exception {
        int startLine = 0;
        int endLine = 0;
        String sonarPath;
        String[] sonarComponents;
        String filePath = null;
        List<Location> locations = new ArrayList<>();
        JSONArray flows = issue.getJSONArray("flows");
        if (flows.size() == 0) {
            //第一种针对issue中的textRange存储location
            JSONObject textRange = issue.getJSONObject("textRange");
            if (textRange != null) {
                startLine = textRange.getIntValue("startLine");
                endLine = textRange.getIntValue("endLine");
            } else {
                log.error("textRange is null , sonar issue-->{}", issue.toJSONString());
            }

            sonarPath = issue.getString(COMPONENT);
            if (sonarPath != null) {
                sonarComponents = sonarPath.split(":");
                if (sonarComponents.length >= 2) {
                    filePath = sonarComponents[sonarComponents.length - 1];
                }
            }

            Location mainLocation = getLocation(startLine, endLine, rawIssueUuid, filePath, repoPath);
            locations.add(mainLocation);
        } else {
            //第二种针对issue中的flows中的所有location存储
            for (int i = 0; i < flows.size(); i++) {
                JSONObject flow = flows.getJSONObject(i);
                JSONArray flowLocations = flow.getJSONArray("locations");
                //一个flows里面有多个locations， locations是一个数组，目前看sonar的结果每个locations都是一个location，但是不排除有多个。
                for (int j = 0; j < flowLocations.size(); j++) {
                    JSONObject flowLocation = flowLocations.getJSONObject(j);
                    String flowComponent = flowLocation.getString(COMPONENT);
                    JSONObject flowTextRange = flowLocation.getJSONObject("textRange");
                    if (flowTextRange == null || flowComponent == null) {
                        continue;
                    }
                    int flowStartLine = flowTextRange.getIntValue("startLine");
                    int flowEndLine = flowTextRange.getIntValue("endLine");
                    String flowFilePath = null;

                    String[] flowComponents = flowComponent.split(":");
                    if (flowComponents.length >= 2) {
                        flowFilePath = flowComponents[flowComponents.length - 1];
                    }

                    Location location = getLocation(flowStartLine, flowEndLine, rawIssueUuid, flowFilePath, repoPath);
                    locations.add(location);
                }
            }
        }

        allLocations.addAll(locations);
        return locations;
    }

    private Location getLocation(int startLine, int endLine, String rawIssueId, String filePath, String repoPath) {
        Location location = new Location();
        String locationUuid = UUID.randomUUID().toString();
        //获取相应的code
        String code = null;
        try {
            code = AstUtil.getCode(startLine, endLine, repoPath + "/" + filePath);
        } catch (Exception e) {
            log.info("file path --> {} file deleted", repoPath + "/" + filePath);
            log.error("rawIssueId --> {}  get code failed.", rawIssueId);

        }

        location.setCode(code);
        location.setUuid(locationUuid);
        location.setStartLine(startLine);
        location.setEndLine(endLine);
        if (startLine > endLine) {
            log.error("startLine > endLine,fileName is {},startLine is {},endLine is {}", filePath, startLine, endLine);
            int temp = startLine;
            startLine = endLine;
            endLine = temp;
        }
        location.setBugLines(startLine + "-" + endLine);
        location.setFilePath(filePath);
        location.setRawIssueId(rawIssueId);
        // todo location 方法名解析
        Object[] methodNameAndOffset = AstParserUtil.findMethodNameAndOffset(repoPath + "/" + filePath, startLine, endLine);
        if (methodNameAndOffset != null) {
            location.setMethodName((String) methodNameAndOffset[0]);
            location.setOffset((int) methodNameAndOffset[1]);
        }

        return location;
    }

    private RawIssue getRawIssue(String repoId, String commitId, String category, String rawIssueUuid, JSONObject issue, String repoPath) {
        //根据ruleId获取rule的name
        String issueName = null;
        JSONObject rule = restInterfaceManager.getRuleInfo(issue.getString("rule"), null, null);
        if (rule != null) {
            issueName = rule.getJSONObject("rule").getString("name");
        }
        //获取文件路径
        String[] sonarComponents;
        String sonarPath = issue.getString(COMPONENT);
        String filePath = null;
        if (sonarPath != null) {
            sonarComponents = sonarPath.split(":");
            if (sonarComponents.length >= 2) {
                filePath = sonarComponents[sonarComponents.length - 1];
            }
        }

        RawIssue rawIssue = new RawIssue();
        rawIssue.setTool(category);
        rawIssue.setUuid(rawIssueUuid);
        rawIssue.setType(issueName);
        rawIssue.setFileName(filePath);
        rawIssue.setDetail(issue.getString("message") + "---" + issue.getString("severity"));
        // fixme 待改，因为数据库不可为空
        rawIssue.setScanId(ToolEnum.SONAR.getType());
        rawIssue.setCommitId(commitId);
        rawIssue.setRepoId(repoId);

        // 1.配置jGit资源
        JGitHelper jGitInvoker = new JGitHelper(repoPath);
        // 2.从JGit获取该commit的author name
        String developerUniqueName = jGitInvoker.getAuthorName(commitId);
        // 3.这里是尝试获取人员聚合后的developer_unique_name
        Map<String, Object> commitViewInfo = commitDao.getCommitViewInfoByCommitId(repoId, commitId);
        if (commitViewInfo != null) {
            // 如果commitView表里有该commit，并且developer_unique_name字段的值不为空，则就用这个developer_unique_name
            developerUniqueName = commitViewInfo.get("developer_unique_name") == null ? developerUniqueName : (String) commitViewInfo.get("developer_unique_name");
        }
        // 否则直接用JGit获取到的该commit的author name
        rawIssue.setDeveloperName(developerUniqueName);
        rawIssue.setPriority(getPriorityByRawIssue(rawIssue));
        return rawIssue;
    }

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }

    @Autowired
    public void setCommitDao(CommitDao commitDao) {
        this.commitDao = commitDao;
    }

}
