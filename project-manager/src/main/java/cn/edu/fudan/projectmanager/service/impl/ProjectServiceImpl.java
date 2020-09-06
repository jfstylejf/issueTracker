package cn.edu.fudan.projectmanager.service.impl;


import cn.edu.fudan.projectmanager.component.RestInterfaceManager;
import cn.edu.fudan.projectmanager.dao.ProjectDao;
import cn.edu.fudan.projectmanager.domain.NeedDownload;
import cn.edu.fudan.projectmanager.domain.Project;
import cn.edu.fudan.projectmanager.service.ProjectService;
import cn.edu.fudan.projectmanager.util.DateTimeUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    private static Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    @Value("${github.api.path}")
    private String githubAPIPath;
    @Value("${repo.url.pattern}")
    private String repoUrlPattern;
    @Value("${clone.result.pre.home}")
    private String cloneResPreHome;

    private RestInterfaceManager restInterfaceManager;

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }

    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private KafkaTemplate kafkaTemplate;

    @Autowired
    public void setKafkaTemplate(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    private ProjectDao projectDao;

    @Autowired
    public void setProjectDao(ProjectDao projectDao) {
        this.projectDao = projectDao;
    }

    @SuppressWarnings("unchecked")
    private void send(String projectId, String url,boolean isPrivate,String username,String password, String branch,String repoSource) {
        NeedDownload needDownload = new NeedDownload(projectId, url,isPrivate,username,password ,branch,repoSource);
        kafkaTemplate.send("ProjectManager", JSONObject.toJSONString(needDownload));
        logger.info("send message to topic ProjectManage ---> " + JSONObject.toJSONString(needDownload));
    }


    @Override
    public void addOneProject(String userToken, JSONObject projectInfo) {
        String url = projectInfo.getString("url");
        String repo_source = projectInfo.getString("repo_source");
        repo_source = repo_source.toLowerCase();
        if (url == null) {
            throw new RuntimeException("please input the project url!");
        }
        url = url.trim();
        if(url.endsWith(".git")){
            url = url.substring(0, url.lastIndexOf("."));
        }
        boolean isPrivate=projectInfo.getBooleanValue("isPrivate");
        checkProjectURL(url,isPrivate);
        String accountId = restInterfaceManager.getAccountId(userToken);
        String branch = (projectInfo.getString("branch") == null || projectInfo.getString("branch").equals("")) ? "master" : projectInfo.getString("branch") ;
        String username = projectInfo.getString("username");
        String password = projectInfo.getString("password");
        if(isPrivate){
            if(username==null||password==null|| username.equals("") || password.equals("")){
                throw new RuntimeException("this project is private,please provide your git username and password!");
            }
        }
        Project project=new Project();
        //验证project name是否重复
        String name=projectInfo.getString("name");
        String type=projectInfo.getString("type");
        //如果没有规定type的值，默认给bug（即用findbugs扫描）
        if (type == null || type.equals("")){
            type = "findbugs";
        }
        List<Project> verifyProjectList= projectDao.getProjectsByCondition(accountId,type,name,null);
        if(verifyProjectList.size()>=1){
            throw new RuntimeException("The project name has already been used! ");
        }
        String module=projectInfo.getString("module");
        Date startScanDate = projectInfo.getDate("startScanDate");
        if (projectDao.hasBeenAdded(accountId, url, type, branch)) {
            throw new RuntimeException("The project has been added!");
        }
        List<Project> projects = projectDao.getProjectsByURLAndTypeBranch(url,type, branch);
        if(projects!=null&&!projects.isEmpty()){
            //如果存在其他用户的project和当前添加的URL和type以及branch相同，需要将扫描状态同步
            Project oneProject=projects.get(0);
            project.setScan_status(oneProject.getScan_status());
            project.setTill_commit_time(oneProject.getTill_commit_time());
            project.setLast_scan_time(oneProject.getLast_scan_time());
       }else{
            project.setScan_status("Not Scanned");
        }
        String projectId = UUID.randomUUID().toString();
        project.setUuid(projectId);
        project.setName(name);
        project.setUrl(url);
        project.setType(type);
        project.setRepo_source(repo_source);
        project.setAccount_id(accountId);
        project.setDownload_status("Downloading");
        project.setAdd_time(new Date());
        project.setStart_scan_date(startScanDate);
        project.setBranch(branch);
        project.setModule(module);
        projectDao.addOneProject(project);
        //向RepoManager这个Topic发送消息，请求开始下载
        send(projectId, url,isPrivate,username,password,branch,repo_source);
    }



    @Override
    public JSONObject addProjectList(String userToken, List<JSONObject> projectListInfo){
        JSONObject result = new JSONObject();
        boolean flag = true;
        String logInfo = "";
        for (int i = 0; i < projectListInfo.size(); i++){
            JSONObject projectInfo = projectListInfo.get(i);
            logger.info("开始导入第" + (i+1) + "个项目：" + projectInfo.getString("url"));
            logInfo = logInfo + "开始导入第" + (i+1) + "个项目：" + projectInfo.getString("url");
            try {
                addOneProject(userToken,projectInfo);
                logger.info("导入第" + (i+1) + "个项目成功！");
                logInfo = logInfo + "导入第" + (i+1) + "个项目成功！";
            }catch (Exception e){
                logger.info("导入第" + (i+1) + "个项目失败：");
                logInfo = logInfo + "导入第" + (i+1) + "个项目失败：";
                logger.info(e.getMessage());
                logInfo = logInfo + e.getMessage();
                flag = false;
                continue;
            }
        }
        result.put("isSuccessful", flag);
        result.put("logInfo", logInfo);
        return result;
    }


    @Override
    public List<JSONObject> getProjectListInfoFromExcelFile(MultipartFile file) throws IOException {
        List<JSONObject> result = new ArrayList<>();

        String fileName = file.getOriginalFilename();
        String dir=System.getProperty("user.dir");
        System.out.println(dir);
        String destFileName=dir+ File.separator + "project-manager" + File.separator + "uploadedfiles"+ File.separator + fileName;
        System.out.println(destFileName);
        File destFile = new File(destFileName);
        file.transferTo(destFile);

        System.out.println("文件上传成功");
        logger.info("文件上传成功");
        System.out.println("开始读取EXCEL内容");
        logger.info("开始读取EXCEL内容");

        Sheet sheet;
        InputStream fis = null;

        fis = new FileInputStream(destFileName);

        Workbook workbook = null;
        try {
            workbook= new XSSFWorkbook(destFile);
        } catch (Exception ex) {
            workbook = new HSSFWorkbook(fis);
        }
        sheet = workbook.getSheetAt(0);

        int totalRowNum = sheet.getLastRowNum();

        System.out.println("当前表格共有："+totalRowNum+"行");
        logger.info("当前表格共有："+totalRowNum+"行");


        for(int i = 13;i <= totalRowNum; i++){
            JSONObject projectInfo = new JSONObject();
            Row row = sheet.getRow(i);
            if(row!=null){
                int columnNum = row.getPhysicalNumberOfCells();
                System.out.println("该行共有列数："+columnNum);
                logger.info("该行共有列数："+columnNum);
                for(int j=1;j<columnNum;j++){
                    System.out.println("当前处理第"+i+"行，第"+j+"列");
                    logger.info("当前处理第"+i+"行，第"+j+"列");
                    Cell cell = row.getCell(j);
                    String cellValue="";
                    if(cell!=null){
                        switch (cell.getCellType()) {
                            case STRING:
                                cellValue = cell.getStringCellValue();
                                break;
                            case NUMERIC:
                                cellValue = String.valueOf(cell.getNumericCellValue());
                                break;
                            case BOOLEAN:
                                cellValue = String.valueOf(cell.getBooleanCellValue());
                                break;
                            default:
                                cellValue = cell.getStringCellValue();
                                break;
                        }
                    }
                    System.out.println(cellValue);
                    if (j == 1){projectInfo.put("url",cellValue);}
                    if (j == 2){projectInfo.put("branch",cellValue);}
                    if (j == 3){projectInfo.put("name",cellValue);}
                    if (j == 4){projectInfo.put("isPrivate",cellValue);}
                    if (j == 5){projectInfo.put("username",cellValue);}
                    if (j == 6){projectInfo.put("password",cellValue);}
                    if (j == 7){projectInfo.put("module",cellValue);}
                    if (j == 8){projectInfo.put("repo_source",cellValue);}
                }

            }
            //Jason对象中必须有"type"字段,默认是bug工具来检测
            projectInfo.put("type","bug");
            result.add(projectInfo);
        }
        return result;
    }


    @Override
    public Object getProjectList(String userToken,String type,int isRecycled) {
        if("1".equals(restInterfaceManager.getAccountId(userToken))){
            return projectDao.getAllProjects().stream().filter(project -> project.getRecycled()==isRecycled).collect(Collectors.toList());
        }
        String account_id = restInterfaceManager.getAccountId(userToken);
        return projectDao.getProjectList(account_id,type).stream().filter(project -> project.getRecycled()==isRecycled).collect(Collectors.toList());
    }

    //jeff
    @Override
    public Object getProjectListByModule(String userToken,String type, String module) {
        String account_id = restInterfaceManager.getAccountId(userToken);
        return projectDao.getProjectListByModule(account_id,type,module).stream().filter(project -> project.getRecycled()==0).collect(Collectors.toList());
    }

    @Override
    public Object getProjectByRepoId(String repo_id) {
        return projectDao.getProjectByRepoId(repo_id);
    }

    @Override
    public Object getProjectByAccountId(String account_id,int isRecycled) {

        return projectDao.getProjectByAccountId(account_id).stream().filter(project ->
                project.getRecycled()==isRecycled).collect(Collectors.toList());
    }

    @Override
    public Object getProjectListByKeyWord(String userToken, String module,String keyWord,String type,int isRecycled) {
        String account_id = restInterfaceManager.getAccountId(userToken);
        if("1".equals(account_id)){
            List<Project> projects = projectDao.getAllProjectByKeyWord(keyWord,module,type).stream()
                    .filter(project -> project.getRecycled()==isRecycled).collect(Collectors.toList());
            projects.stream().forEach(project -> project.setAccount_name(restInterfaceManager.getAccountName(project.getAccount_id())));
            //遍历projects，将till_commit_time + 8小时，更改为北京时间
            return resetTillCommitTime(projects);
        }else {
            List<Project> projects = projectDao.getProjectByKeyWordAndAccountId(account_id, keyWord.trim(), module, type).stream()
                    .filter(project -> project.getRecycled()==isRecycled).collect(Collectors.toList());
            //遍历projects，将till_commit_time + 8小时，更改为北京时间
            return resetTillCommitTime(projects);
        }
    }

    private Object resetTillCommitTime(List<Project> projects) {
        for (Project project : projects) {
            Date oldTime = project.getTill_commit_time();
            Date newTime = DateTimeUtil.addHourOfDate(oldTime, 8);
            project.setTill_commit_time(newTime);
        }
        return projects;
    }

    @Override
    public List<String> getRepoIdsByAccountIdAndType(String account_id, String type, int isRecycled) {
        List<String> repoIds;
        if("1".equals(account_id)){
            repoIds = projectDao.getAllProjects().stream()
                    .filter(project -> project.getRecycled()==isRecycled)
                    .map(Project::getRepo_id).filter(repoId -> repoId != null).collect(Collectors.toList());
        }else {
            repoIds = projectDao.getRepoIdsByAccountIdAndType(account_id,type).stream()
                    .filter(repoId -> repoId != null)
                    .filter(repoId -> projectDao.getProjectByRepoIdAndCategory(account_id,repoId,type)
                            .getRecycled()==isRecycled).collect(Collectors.toList());

        }
        return repoIds;

    }

    @Override
    public Project getProjectByID(String projectId) {
        return projectDao.getProjectByID(projectId);
    }

    @Override
    public void updateProjectStatus(Project project) {
        projectDao.updateProjectStatus(project);
    }


    private void updateProjectStatus(String projectId,String status){
        Project project=new Project();
        project.setUuid(projectId);
        project.setScan_status(status);
        projectDao.updateProjectStatus(project);
    }

    /**
     * 若非管理员，先判断是非为唯一repo，若唯一，则将project的accountID改为adminID；若不唯一，则和之前处理相同，只删除project表对应数据
     * 若是管理员，则不需要考虑是否还有其他人拥有此项目，直接删除所有repoId相同的项目
     * @param projectId
     * @param type
     * @param userToken
     */
    @Override
    public void remove(String projectId, String type,String userToken) {
        //updateProjectStatus(projectId,"deleting");
        String repoId = projectDao.getRepoId(projectId);
        if(repoId!=null){
            String accountId = restInterfaceManager.getAccountId(userToken);
            //如果当前repoId和type只有这一个projectId与其对应，那么删除project的同时会删除repo的相关内容
            //否则还有其他project与当前repoId和type对应，该repo的相关内容就不删
            //先删project表，再删redis中的数据，再删其它表
            //if (!projectDao.existOtherProjectWithThisRepoIdAndType(repoId, type) ) {
            if(accountId.equals("1")){
                List<Project> projects = projectDao.getProjectByRepoId(repoId);
                String branch = projects.get(0).getBranch();
                List<String> projectIds = projects.stream().map(Project::getUuid).collect(Collectors.toList());
                for(String id : projectIds){
                    projectDao.remove(id);
                }
                //delete info in redis
                deleteProjectInfo(repoId, accountId, "bug");
                deleteProjectInfo(repoId, accountId, "clone");
                restInterfaceManager.deleteCodeTrackerOfRepo(branch,repoId);
                restInterfaceManager.deleteIgnoreRecord(accountId, repoId);
                restInterfaceManager.deleteRepoMeasure(repoId);
                restInterfaceManager.deleteCloneMeasure(repoId);
                deleteCloneResPreFile(repoId);

            }else if (!projectDao.existOtherProjectWithThisRepoIdAndType(repoId, type) ) {
                Project project = projectDao.getProjectByID(projectId);
                project.setAccount_id("1");
                projectDao.updateProjectStatus(project);
            }else {
                projectDao.remove(projectId);
            }
        }else{
            //repoId为空表示没有下载完成，直接删除
            projectDao.remove(projectId);
        }
        logger.info("project delete success!");
    }

    private void deleteProjectInfo(String repoId, String accountId, String type){
        stringRedisTemplate.setEnableTransactionSupport(true);
        stringRedisTemplate.multi();
        stringRedisTemplate.delete("dashboard:" + type + ":day:" + repoId);
        stringRedisTemplate.delete("dashboard:" + type + ":week:" + repoId);
        stringRedisTemplate.delete("dashboard:" + type + ":month:" + repoId);
        stringRedisTemplate.delete("dashboard:" + type + ":day:new:" + repoId);
        stringRedisTemplate.delete("dashboard:" + type + ":week:new:" + repoId);
        stringRedisTemplate.delete("dashboard:" + type + ":month:new:" + repoId);
        stringRedisTemplate.delete("dashboard:" + type + ":day:eliminated:" + repoId);
        stringRedisTemplate.delete("dashboard:" + type + ":week:eliminated:" + repoId);
        stringRedisTemplate.delete("dashboard:" + type + ":month:eliminated:" + repoId);
        stringRedisTemplate.delete("trend:" + type + ":day:new:" + accountId + ":" + repoId);
        stringRedisTemplate.delete("trend:" + type + ":day:remaining:" + accountId + ":" + repoId);
        stringRedisTemplate.delete("trend:" + type + ":day:eliminated:" + accountId + ":" + repoId);
        stringRedisTemplate.delete("trend:" + type + ":week:new:" + accountId + ":" + repoId);
        stringRedisTemplate.delete("trend:" + type + ":week:remaining:" + accountId + ":" + repoId);
        stringRedisTemplate.delete("trend:" + type + ":week:eliminated:" + accountId + ":" + repoId);
        stringRedisTemplate.exec();

        restInterfaceManager.deleteIssuesOfRepo(repoId, type);
        restInterfaceManager.deleteRawIssueOfRepo(repoId, type);
        restInterfaceManager.deleteScanOfRepo(repoId, type);
        restInterfaceManager.deleteEventOfRepo(repoId, type);
        restInterfaceManager.deleteScanResultOfRepo(repoId, type);


    }

    private void deleteCloneResPreFile(String repoId) {
        JSONObject currentRepo = restInterfaceManager.getRepoById(repoId);
        String localAddress=currentRepo.getJSONObject("data").getString("local_addr");
        String repoName = localAddress.substring(localAddress.lastIndexOf("/")+1);
        String filePath=cloneResPreHome+repoName+"_A.csv";
        File file=new File(filePath);
        if(file.exists()){
            if(file.delete()){
                logger.info("clone pre file delete success!");
            }
        }else {
            logger.info("clone pre file not exist!");
        }
    }

    @Override
    public String getRepoId(String projectId) {
        return projectDao.getRepoId(projectId);
    }

    @Override
    public Object existProjectWithThisRepoIdAndType(String repoId, String type,boolean isFirst) {
        Map<String,Object> result=new HashMap<>();
        if(isFirst)
            //如果项目是第一次添加时判断是否自动扫
        {
            result.put("exist",projectDao.existProjectWithThisRepoIdAndTypeAndScannedSign(repoId, type,0));
        } else
            //后续收到新的commit时判断是否扫
        {
            result.put("exist",projectDao.existProjectWithThisRepoIdAndTypeAndScannedSign(repoId, type,1));
        }
        return result;
    }

    @Override
    public void updateProjectFirstAutoScan(String repoId, String type) {
        projectDao.updateProjectFirstAutoScan(repoId, type);
    }

    private void checkProjectURL( String url,boolean isPrivate) {
        Pattern pattern = Pattern.compile(repoUrlPattern);
        Matcher matcher = pattern.matcher(url);
//        Pattern pattern2 = Pattern.compile(repoUrlPatternGitlab);
//        Matcher matcher2 = pattern2.matcher(url);
        if (!matcher.matches()) {
            throw new RuntimeException("invalid url!");
        }


//        if(!isPrivate){
//            //前面做过一次全匹配，需要把matcher重置一下才能接着从头匹配
//            matcher.reset();
//            boolean isMavenProject = false;
//            while (matcher.find()) {
//                String author_projectName = matcher.group(1);
//                JSONArray fileList = restTemplate.getForEntity(githubAPIPath + author_projectName + "/contents", JSONArray.class).getBody();
//                if (fileList != null) {
//                    for (int i = 0; i < fileList.size(); i++) {
//                        JSONObject file = fileList.getJSONObject(i);
//                        if (file.getString("name").equals("pom.xml")) {
//                            isMavenProject = true;
//                            break;
//                        }
//                    }
//                    if (!isMavenProject)
//                        throw new RuntimeException("failed,this project is not maven project!");
//                } else {
//                    throw new RuntimeException("invalid url!");
//                }
//            }
//        }
    }

    @Override
    public Project getProjectByRepoIdAndCategory(String userToken,String repoId,String category){
        String account_id = restInterfaceManager.getAccountId(userToken);
        return projectDao.getProjectByRepoIdAndCategory(account_id,repoId,category);
    }

    @Override
    public List<Project> getProjectsByCondition(String userToken, String category, String name, String module) {
        String account_id = restInterfaceManager.getAccountId(userToken);
        List<Project> projects = projectDao.getProjectsByCondition(account_id,category,name,module);
        return projects;
    }

    @Override
    public void addRootProject(String projectId) {
        Project project = getProjectByID(projectId);

        if(!projectDao.getProjectList("1","bug").stream().
                map(Project::getRepo_id).collect(Collectors.toList()).contains(project.getRepo_id())){
            String uuid = UUID.randomUUID().toString();
            project.setUuid(uuid);
            project.setAccount_id("1");
            projectDao.addOneProject(project);
        }

    }

    @Override
    public void removeNonAdminProject(String projectId, String type, String userToken) {
        projectDao.remove(projectId);
    }


    /**
     * 功能已合并到getProjectList接口
     * @param
     * @return
     */
    @Override
    public List<Project> getAllProject(int isRecycled) {
        return projectDao.getAllProjects().stream().
                filter(project -> project.getRecycled()==isRecycled).collect(Collectors.toList());

    }

    /**
     *
     * @param projectId
     * @param userToken
     */
    @Override
    public void recycle(String projectId, String userToken, int isRecycled) {
        Project project = getProjectByID(projectId);
        project.setRecycled(isRecycled);
        project.setDelete_time(new Date());
        project.setAccount_id("1");
        projectDao.updateProjectStatus(project);
    }

    @Override
    public void updateProject(JSONObject project) {

        String projectId = project.getString("uuid");
        String name = project.getString("name");
        String module = project.getString("module");
        Project project1 = new Project();
        project1.setUuid(projectId);
        project1.setModule(module);
        project1.setName(name);
        projectDao.updateProjectStatus(project1);
    }

    @Override
    public List<Map<String, Object>> getProjectInfoByAccountName(String accountName) {
        return projectDao.getProjectInfoByAccountName(accountName);
    }

    @Override
    public Object getAllProject() {
        List<Map<String, String>> projects =  projectDao.getAllProject();
        Map<String, List< Map<String, String>>> result = new HashMap<>(8);
        for (Map<String, String> project : projects) {
            String projectName = project.get("module");
            if (StringUtils.isEmpty(projectName)) {
                projectName = "unnamed";
            }
            if (! result.keySet().contains(projectName)) {
                result.put(projectName, new ArrayList<>(4));
            }
            List< Map<String, String>> v = result.get(projectName);
            Map<String, String> p = new HashMap<>(4);
            p.put("repo_id", project.get("repo_id"));
            p.put("name", project.get("name"));
            v.add(p);
        }
        return result;
    }

}