package cn.edu.fudan.issueservice.core.process;

import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.dao.CommitDao;
import cn.edu.fudan.issueservice.domain.dbo.Location;
import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.domain.enums.RawIssueStatus;
import cn.edu.fudan.issueservice.domain.enums.ToolEnum;
import cn.edu.fudan.issueservice.util.ASTUtil;
import cn.edu.fudan.issueservice.util.AstParserUtil;
import cn.edu.fudan.issueservice.util.FileFilter;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

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
public class SonarQubeBaseAnalyzer extends BaseAnalyzer {

    private CommitDao commitDao;

    private RestInterfaceManager restInvoker;

    private ThreadLocal<JSONObject> sonarIssue = new ThreadLocal<>();

    @Override
    public boolean invoke(String repoId, String repoPath, String commit) {
        // 先得到上次扫描的数据
        JSONObject before = restInvoker.getSonarAnalysisTime(repoId);
        setSonarIssue(before);

        try {
            Runtime rt = Runtime.getRuntime();
            String command = binHome + "executeSonar.sh " + repoPath + " " + repoId + " " + commit;
            log.info("command -> {}",command);
            Process process = rt.exec(command);
            //最多等待sonar脚本执行100秒，超时就返回false
            boolean timeout = process.waitFor(300L,TimeUnit.SECONDS);
            if (!timeout) {
                process.destroy();
                log.error("invoke tool timeout ! (300s)");
                return false;
            }
            int exitValue = process.exitValue();
            return exitValue == 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean analyze(String repoPath, String repoId, String commitId) {
        long analyzeStartTime = System.currentTimeMillis();
        boolean isChanged = false;
        try {
            // 最多等待100秒
            for (int i = 1; i <= 50; i++) {
                TimeUnit.SECONDS.sleep(2);
                if (isChange(restInvoker.getSonarAnalysisTime(repoId))){
                    isChanged = true;
                    long analyzeEndTime2= System.currentTimeMillis();
                    log.info("It takes {}s to wait for the latest sonar result ", (analyzeEndTime2-analyzeStartTime)/1000 );
                    //睡4秒再去拿sonar issue search api 的结果
                    TimeUnit.SECONDS.sleep(4);
                    break;
                }
            }
        }catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        if(!isChanged){
            log.error("get latest sonar result failed!");
            long analyzeEndTime1 = System.currentTimeMillis();
            log.info("It takes {}s to wait for the latest sonar result ", (analyzeEndTime1-analyzeStartTime)/1000 );
            return false;
        }
        ///实际上这里的old已经是最新sonar扫描的结果了
        JSONObject old = restInvoker.getSonarIssueResults(repoId,null,1,false,0);
        // 分析之前记录上一个版本的数据 此版本的数据与上个版本的数据不一致说明数据已经入库
        try {
            List<Location> allLocations = new ArrayList<> ();
            int pageSize = 100;
            int issueTotal = old.getIntValue("total");
            log.info("Current commit {}, issueTotal in sonar result is {}",commitId,issueTotal);
            int pages = issueTotal % pageSize > 0 ? issueTotal/pageSize+1 : issueTotal/pageSize;
            for(int i = 1; i <= pages; i++){
                //获取第i页的全部issue结果
                JSONObject sonarResult = restInvoker.getSonarIssueResults(repoId,null, pageSize,false, i);
                JSONArray sonarRawIssues = sonarResult.getJSONArray("issues");
                //遍历存储 location,rawIssue,issue
                for(int j = 0; j<sonarRawIssues.size();j++){
                    JSONObject sonarIssue = sonarRawIssues.getJSONObject(j);
                    //issue所在的文件
                    String component = sonarIssue.getString ("component");
                    if(FileFilter.javaFilenameFilter(component)){
                        continue;
                    }
                    String rawIssueUuid = UUID.randomUUID().toString();

                    //fixme location status 放入getRawIssue方法内
                    //将该issue的所有location直接插入表中
                    List<Location> locations = getLocations(rawIssueUuid, sonarIssue, repoPath, allLocations);
                    //获取rawIssue
                    RawIssue rawIssue = getRawIssue(repoId,commitId,ToolEnum.SONAR.getType (),rawIssueUuid ,sonarIssue);
                    rawIssue.setLocations (locations);
                    rawIssue.setStatus (RawIssueStatus.DEFAULT.getType ());
                    resultRawIssues.add(rawIssue);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isChange(JSONObject newRecord) {
        JSONObject old = sonarIssue.get();
        String errors = "errors", component = "component";
        // 0. old 为 error    new 为 error 此时是没有change
        if (old.containsKey(errors) && newRecord.containsKey(errors)){
            return false;
        }
        // 1. old 为 error    new 有 component 此时是change
        if (old.containsKey(errors) && newRecord.containsKey(component)){
            return true;
        }
        // 2. old 有 component  new 有 component 此时需要判断时间analysisDate
        return !old.getJSONObject(component).getString("analysisDate").equals(newRecord.getJSONObject(component).getString("analysisDate"));
    }

    @Override
    public String getToolName() {
        return ToolEnum.SONAR.getType ();
    }

    @Override
    public Integer getPriorityByRawIssue(RawIssue rawIssue) {
        int result = -1;
        String detail = rawIssue.getDetail ();
        String[] rawIssueArgs  = detail.split ("---");
        String severity = rawIssueArgs[rawIssueArgs.length - 1];
        switch (severity){
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

    public List<Location> getLocations(String rawIssueUuid, JSONObject issue, String repoPath, List<Location> allLocations) throws Exception{
        int startLine =0;
        int endLine = 0;
        String sonarPath;
        String[] sonarComponents;
        String filePath = null;
        List<Location> locations = new ArrayList<> ();
        JSONArray flows = issue.getJSONArray("flows");
        if(flows.size () == 0){
            //第一种针对issue中的textRange存储location
            JSONObject textRange = issue.getJSONObject("textRange");
            if(textRange != null){
                startLine = textRange.getIntValue("startLine");
                endLine = textRange.getIntValue("endLine");
            }else{
                log.error("textRange is null , sonar issue-->{}",issue.toJSONString());
            }

            sonarPath =issue.getString("component");
            if(sonarPath != null) {
                sonarComponents = sonarPath.split(":");
                if (sonarComponents.length >= 2) {
                    filePath = sonarComponents[sonarComponents.length - 1];
                }
            }

            Location mainLocation = getLocation(startLine,endLine,rawIssueUuid,filePath,repoPath);
            locations.add(mainLocation);
        }else{
            //第二种针对issue中的flows中的所有location存储
            for(int i=0;i<flows.size();i++){
                JSONObject flow = flows.getJSONObject(i);
                JSONArray flowLocations = flow.getJSONArray("locations");
                //一个flows里面有多个locations， locations是一个数组，目前看sonar的结果每个locations都是一个location，但是不排除有多个。
                for(int j=0;j<flowLocations.size();j++){
                    JSONObject flowLocation = flowLocations.getJSONObject(j);
                    String flowComponent = flowLocation.getString("component");
                    JSONObject flowTextRange = flowLocation.getJSONObject("textRange");
                    if(flowTextRange==null || flowComponent == null){
                        continue;
                    }
                    int flowStartLine = flowTextRange.getIntValue("startLine");
                    int flowEndLine = flowTextRange.getIntValue("endLine");
                    String flowFilePath = null;

                    String[] flowComponents = flowComponent.split(":");
                    if (flowComponents.length >= 2) {
                        flowFilePath = flowComponents[flowComponents.length - 1];
                    }

                    Location location = getLocation(flowStartLine,flowEndLine,rawIssueUuid,flowFilePath,repoPath);
                    locations.add(location);
                }
            }
        }

        allLocations.addAll (locations);
        return locations;
    }


    private Location getLocation(int startLine,int endLine,String rawIssueId,String filePath,String repoPath) {
        Location location = new Location ();
        String locationUuid = UUID.randomUUID().toString();
        //获取相应的code
        String code= null;
        try{
            code = ASTUtil.getCode(startLine,endLine,repoPath+"/"+filePath);
        }catch (Exception e){
            log.info("file path --> {} file deleted",repoPath+"/"+filePath);
            log.error("rawIssueId --> {}  get code failed.",rawIssueId);

        }

        location.setCode(code);

        location.setUuid(locationUuid);
        location.setStart_line(startLine);
        location.setEnd_line(endLine);
        if(startLine>endLine){
            log.error("startLine number greater than endLine number");
            return null;
        }else if(startLine==endLine){
            location.setBug_lines(startLine+"");
        }else{
            StringBuilder lines = new StringBuilder();
            while(startLine<endLine){
                lines.append(startLine).append(",");
                startLine++;
            }
            lines.append(endLine);
            location.setBug_lines(lines.toString());
        }
        location.setFile_path(filePath);
        location.setRawIssue_id(rawIssueId);
        // todo location 方法名解析
        String methodName = AstParserUtil.findMethod (repoPath+"/"+filePath, startLine, endLine);
        location.setMethod_name (methodName);

        return location;
    }


    private RawIssue getRawIssue(String repoId, String commitId, String category, String rawIssueUuid, JSONObject issue){
        //根据ruleId获取rule的name
        String issueName=null;
        JSONObject rule = restInvoker.getRuleInfo(issue.getString("rule"),null,null);
        if(rule != null){
            issueName = rule.getJSONObject("rule").getString("name");
        }
        //获取文件路径
        String[] sonarComponents;
        String sonarPath =issue.getString("component");
        String filePath= null;
        if(sonarPath != null) {
            sonarComponents = sonarPath.split(":");
            if (sonarComponents.length >= 2) {
                filePath = sonarComponents[sonarComponents.length - 1];
            }
        }

        RawIssue rawIssue = new RawIssue();
        rawIssue.setTool(category);
        rawIssue.setUuid(rawIssueUuid);
        rawIssue.setType(issueName);
        rawIssue.setFile_name(filePath);
        rawIssue.setDetail(issue.getString("message")+ "---" + issue.getString ("severity"));
        // fixme 待改，因为数据库不可为空
        rawIssue.setScan_id(ToolEnum.SONAR.getType ());
        rawIssue.setCommit_id(commitId);
        rawIssue.setRepo_id(repoId);
        Map<String, Object> commitViewInfo = commitDao.getCommitViewInfoByCommitId(repoId, commitId);
        String developerUniqueName = (String) commitViewInfo.get("developer_unique_name");
        if(StringUtils.isEmpty(developerUniqueName)){
            developerUniqueName = (String) commitViewInfo.get("developer");
        }
        rawIssue.setDeveloperName(developerUniqueName);
        return rawIssue;
    }

    private void setSonarIssue(JSONObject sonar) {
        sonarIssue.remove();
        sonarIssue.set(sonar);
    }

    public void setRestInvoker(RestInterfaceManager restInvoker) {
        this.restInvoker = restInvoker;
    }

    public void setCommitDao(CommitDao commitDao) {
        this.commitDao = commitDao;
    }

    public static void main(String[] args){
        SonarQubeBaseAnalyzer sonarQubeBaseAnalyzer = new SonarQubeBaseAnalyzer();
        String repoPath = "/home/fdse/user/issueTracker/bin/executeSonar.sh /home/fdse/user/issueTracker/repo/github/FudanSELab/IssueTracker-Master-zhonghui20191012_duplicate_fdse-13";
        String projectName = "test1";
        String version = "v1";
        sonarQubeBaseAnalyzer.invoke (projectName, repoPath, version);
    }

}
