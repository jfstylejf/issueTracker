package cn.edu.fudan.cloneservice.task;

import cn.edu.fudan.cloneservice.dao.CloneLocationDao;
import cn.edu.fudan.cloneservice.domain.LocationInfo;
import cn.edu.fudan.cloneservice.domain.Result;
import cn.edu.fudan.cloneservice.domain.clone.CloneLocation;
import cn.edu.fudan.cloneservice.domain.clone.CloneScan;
import cn.edu.fudan.cloneservice.domain.clone.CloneScanInitialInfo;
import cn.edu.fudan.cloneservice.domain.clone.CloneScanResult;
import cn.edu.fudan.cloneservice.util.ASTUtil;
import cn.edu.fudan.cloneservice.util.DeleteFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


import static com.fdse.SagaShell.cloneDetect;


/**
 * @author zyh
 * @date 2020/5/25
 */
@Slf4j
@Component("CPUClone")
public class CPUCloneScanOperation extends ScanOperationAdapter {
    private static final Object lock = new Object();
    private static final String SNIPPET = "snippet";
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    @Value("${clone.resultFileHome}")
    private String cloneResultFileHome;
    @Value("${clone.home}")
    private String cloneHome;

    @Value("${min.snippet.num}")
    private int minSnippetNum;

    private CloneLocationDao cloneLocationDao;

    @Autowired
    private void setCloneLocationDao(CloneLocationDao cloneLocationDao) {
        this.cloneLocationDao = cloneLocationDao;
    }

    /**
     * 检测clone location是否存在交叉重复
     *
     * @param tmpCloneLocationList clone location list
     * @return 存在为true
     */
    private boolean isIntersection(List<CloneLocation> tmpCloneLocationList) {

        for (int i = 0; i < tmpCloneLocationList.size(); i++) {
            for (int j = i + 1; j < tmpCloneLocationList.size(); j++) {
                if (tmpCloneLocationList.get(i).getFilePath().equals(tmpCloneLocationList.get(j).getFilePath())) {
                    int start1 = Integer.parseInt(tmpCloneLocationList.get(i).getCloneLines().split(",")[0]);
                    int end1 = Integer.parseInt(tmpCloneLocationList.get(i).getCloneLines().split(",")[1]);
                    int start2 = Integer.parseInt(tmpCloneLocationList.get(j).getCloneLines().split(",")[0]);
                    int end2 = Integer.parseInt(tmpCloneLocationList.get(j).getCloneLines().split(",")[1]);
                    boolean exit = (start1 >= start2 && start1 <= end2) ||
                            (end1 >= start2 && end1 <= end2) ||
                            (start2 >= start1 && start2 <= end1) ||
                            (end2 >= start1 && end2 <= end1);
                    if (exit) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 过滤对测试代码的入库
     *
     * @param tmpCloneLocationList clone组
     * @return 过滤后的clone组
     */
    private List<CloneLocation> wipeOffTest(List<CloneLocation> tmpCloneLocationList) {

        List<CloneLocation> cloneLocationList = new ArrayList<>();
        //!!!
        tmpCloneLocationList.forEach(cloneLocation -> {
            String className = cloneLocation.getFilePath().toLowerCase();
            String fullName = className.substring(className.lastIndexOf("/") + 1);
            if (!className.contains("/test/") && !fullName.startsWith("test") && !fullName.endsWith("test.java") && !fullName.endsWith("tests.java")) {
                cloneLocationList.add(cloneLocation);
            }
        });

        if (cloneLocationList.size() > 1) {
            return cloneLocationList;
        }
        return null;
    }

    //to do:调用外部可执行文件仍然占据大量cpu算力、且有并发问题，可以把可执行文件的源码直接集成吗？
    private boolean invokeCloneTool(String repoPath, String granularity, String languagePara) {
        try {
            DeleteFileUtil.deleteDirectory(cloneHome + "result");
            DeleteFileUtil.deleteDirectory(cloneHome + "tokenData");
            String language = languagePara.toLowerCase();
            if("javascript".equalsIgnoreCase(languagePara)) {
                language = "js";
            }
            String exe = IS_WINDOWS? "executable/executable_cpu_win10_snippet.exe":"executable/executable_cpu_linux_snippet";
            String[] configs = new String[]{
                    "dataset=" + repoPath,
                    "language=" + language,
                    "extensions=" + language,
                    "granularity=" + granularity,
                    "exe=" + exe,
                    "threshold=0.7"
            };
            cloneDetect(configs);
            log.info("{} -> method scan complete -> {}", Thread.currentThread().getName());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    protected boolean analyzeResultFile(String repoId, String repoPath, String commitId, String type, String resultFilePath) throws IOException {
        try {
            BufferedReader resultReader = new BufferedReader(new FileReader(resultFilePath));
            Map<String, List<Result>> groups = getResultFromFile(resultReader);
            if (groups.isEmpty()) return false;
            for (Map.Entry<String, List<Result>> entry : groups.entrySet()) {
                String groupId = entry.getKey();
                List<Result> results = entry.getValue();
                List<CloneLocation> tmpCloneLocationList = new ArrayList<>();
                //记录同一个clone组的snippet最小行数
                int min = Integer.MAX_VALUE;
                for (Result result : results) {
                    String filePath = result.getRepoPath();
                    String cloneLocationId = UUID.randomUUID().toString();
                    CloneLocation cloneLocation = new CloneLocation();
                    cloneLocation.setUuid(cloneLocationId);
                    cloneLocation.setRepoId(repoId);
                    cloneLocation.setCommitId(commitId);
                    cloneLocation.setCategory(groupId);
                    //截取filePath
                    cloneLocation.setFilePath(filePath.substring(repoPath.length() + 1));
                    //clone的方法起始行，结束行
                    String methodLines = result.getMethodLoc();
                    cloneLocation.setMethodLines(methodLines);
                    //具体clone代码的起始行结束行
                    String cloneLines = result.getSnippetLoc();
                    cloneLocation.setCloneLines(cloneLines);
                    //method or snippet
                    cloneLocation.setType(type);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String commitTime = repoCommitMapper.getCommitTimeByCommitId(commitId);
                    if(StringUtils.isEmpty(commitTime)){
                        commitTime = "2000-00-00";
                    }
                    log.info(commitTime);
                    cloneLocation.setCommitTime(simpleDateFormat.parse(commitTime));
//                    //类名 方法名
//                    List<LocationInfo> classLocationInfos = getClassList(filePath, language);
//                    List<LocationInfo> methodLocationInfos = getMethodList(filePath, language);

//                    if(classLocationInfos.isEmpty()) {
//                        cloneLocation.setClassName(result.getRepoPath().substring(result.getRepoPath().lastIndexOf("/")+1, result.getRepoPath().lastIndexOf(".")));
//                    }
//                    else{
//                        List<String> classAndMethods = getClassAndMethod(cloneLines, classLocationInfos, methodLocationInfos);
//                        if(classAndMethods.size()!=0){
//                            cloneLocation.setClassName(classAndMethods.get(0));
//                            if(classAndMethods.size()>1) {
//                                cloneLocation.setMethodName(classAndMethods.get(1));
//                            }
//                        }
//                    }

                    String[] methodLoc = result.getMethodLoc().split(",");
                    String[] snippetLoc = result.getSnippetLoc().split(",");
                    ASTUtil.CodeLocation codeLocation = new ASTUtil().getCode(Integer.parseInt(methodLoc[0]),
                            Integer.parseInt(methodLoc[1]),
                            Integer.parseInt(snippetLoc[0]),
                            Integer.parseInt(snippetLoc[1]),
                            filePath);
                    String code = codeLocation.getCode();
                    List<String> num = codeLocation.getNum();
                    //记录clone组内最小的片段行数
                    if (num.size() < min) {
                        min = num.size();
                    }
                    //具体代码
                    cloneLocation.setCode(code);
                    //去除空行和注释的行数
                    //修改录入的方式
                    cloneLocation.setNum(String.join(",", num));
                    //插入列表
                    tmpCloneLocationList.add(cloneLocation);
                }
                //大于所规定的最小片段行数才入库
                if (min >= minSnippetNum && !isIntersection(tmpCloneLocationList)) {
                    //过滤测试代码的clone检测
                    List<CloneLocation> cloneLocationList1 = wipeOffTest(tmpCloneLocationList);
                    if (cloneLocationList1 != null) {
                        cloneLocationDao.insertCloneLocations(tmpCloneLocationList);
                    }
                }
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private Map<String, List<Result>> getResultFromFile(BufferedReader resultReader) throws IOException {
        String line;
        List<Result> results = new ArrayList<>();
        log.info("open file successfully");
        while ((line = resultReader.readLine()) != null) {
            String[] lineResult = line.split(",");
            if (lineResult.length == 9) {
                Result result = new Result(lineResult[0], lineResult[2], lineResult[3].concat(",".concat(lineResult[4])), lineResult[7].concat(",".concat(lineResult[8])));
                results.add(result);
            }
        }
        if(!results.isEmpty()) log.info("result not empty");
        Map<String, List<Result>> resultMap = new HashMap<>();

        for (Result result : results) {
            resultMap.computeIfAbsent(result.getGroupId(), k -> new ArrayList<>()).add(result);
        }

        return resultMap;
    }

    @Override
    public CloneScanResult doScan(CloneScanInitialInfo cloneScanInitialInfo) throws IOException {
        try {

            CloneScan cloneScan = cloneScanInitialInfo.getCloneScan();
            String repoId = cloneScan.getRepoId();
            String commitId = cloneScan.getCommitId();
            String type = cloneScan.getType();
            String repoPath = cloneScanInitialInfo.getRepoPath();
            String language = cloneScanInitialInfo.getLanguage();
            synchronized (lock) {
                log.info("{} -> start to invoke tool to scan......", Thread.currentThread().getName());
                if (!invokeCloneTool(repoPath, type, language)) {
                    log.error("{} -> Invoke Analyze Tool Failed!", Thread.currentThread().getName());
                    return new CloneScanResult(repoId, commitId, type, "failed", "tool invoke failed");
                }
                log.info("{} -> tool invoke complete!", Thread.currentThread().getName());
                log.info("{} -> scan complete", Thread.currentThread().getName());
                log.info("{} -> start to analyze resultFile......", Thread.currentThread().getName());
                //只有片段级的入库
                if (SNIPPET.equals(type)) {
                    String resultFilePath = cloneResultFileHome + "type12_" + type + "_result.csv";
                    if (!analyzeResultFile(repoId, repoPath, commitId, type, resultFilePath)) {
                        log.error("{} -> Result File Analyze Failed!", Thread.currentThread().getName());
                        return new CloneScanResult(repoId, commitId, type, "failed", "analyze failed");
                    }
                    log.info("{} -> resultFile analyze complete", Thread.currentThread().getName());
                }
            }
            return new CloneScanResult(repoId, commitId, type, "success", "Scan Success");
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


//
//    public List<String> getClassAndMethod(String cloneLines, List<LocationInfo> classLocationInfos, List<LocationInfo> methodLocationInfos){
//        List<String> result = new ArrayList<>();
//        List<String> cloneInfo = new ArrayList<>(Arrays.asList(cloneLines.split(",")));
//        int beginLine = Integer.parseInt(cloneInfo.get(0));
//        int endLine = Integer.parseInt(cloneInfo.get(1));
//        if(methodLocationInfos.isEmpty()) return result;
//        for(LocationInfo classLocationInfo: classLocationInfos){
//            if(beginLine >= classLocationInfo.getBeginLine() && endLine <= classLocationInfo.getEndLine()){
//                result.add(classLocationInfo.getClassName());
//            }
//        }
//        for(LocationInfo methodLocationInfo: methodLocationInfos){
//            if(beginLine >= methodLocationInfo.getBeginLine() && endLine <= methodLocationInfo.getEndLine()){
//                result.add(methodLocationInfo.getMethodName());
//            }
//        }
//        return result;
//    }
//
//    public List<LocationInfo> getClassList(String filePath, String language){
//        List<LocationInfo> result = new ArrayList<>();
//        if("java".equals(language)){
//            List<String> filePathList = new ArrayList<>();
//            filePathList.add(filePath);
//            JavaTree javaTree = new JavaTree(filePathList, "t", filePath.substring(0,filePath.lastIndexOf("/")));
//            List<ClassNode> classInfos = javaTree.getClassInfos();
//            if(classInfos.isEmpty()) return result;
//            for(ClassNode classInfo: classInfos){
//                LocationInfo loc = new LocationInfo(classInfo.getBeginLine(), classInfo.getEndLine(), classInfo.getClassName(), null);
//                result.add(loc);
//            }
//            return result;
//        }else if("js".equals(language.toLowerCase())||"javascript".equals(language.toLowerCase())){
//            JsFileParser.setBabelPath(cloneHome + "babelEsLint.js");
//            List<String> filePathList = new ArrayList<>();
//            filePathList.add(filePath);
//            JsTree jsTree = new JsTree(filePathList, "t", filePath.substring(0,filePath.lastIndexOf("/")));
//            List<ClassNode> classInfos = jsTree.getClassInfos();
//            if(classInfos.isEmpty()) return result;
//            for(ClassNode classInfo: classInfos){
//                LocationInfo loc = new LocationInfo(classInfo.getBeginLine(), classInfo.getEndLine(), classInfo.getClassName(), null);
//                result.add(loc);
//            }
//            return result;
//        }else{
//            log.error("don't support language"+language);
//            return result;
//        }
//    }
//
//    public List<LocationInfo> getMethodList(String filePath, String language){
//        List<LocationInfo> result = new ArrayList<>();
//        if("java".equals(language)){
//            List<String> filePathList = new ArrayList<>();
//            filePathList.add(filePath);
//            JavaTree javaTree = new JavaTree(filePathList, "t", filePath.substring(0,filePath.lastIndexOf("/")));
//            List<MethodNode> methodInfos = javaTree.getMethodInfos();
//            if(methodInfos.isEmpty()) return result;
//            for(MethodNode methodInfo: methodInfos){
//                LocationInfo loc = new LocationInfo(methodInfo.getBeginLine(), methodInfo.getEndLine(), null, methodInfo.getSignature());
//                result.add(loc);
//            }
//            return result;
//        }else if("js".equals(language.toLowerCase())||"javascript".equals(language.toLowerCase())){
//            JsFileParser.setBabelPath(cloneHome + "babelEsLint.js");
//            List<String> filePathList = new ArrayList<>();
//            filePathList.add(filePath);
//            JsTree jsTree = new JsTree(filePathList, "t", filePath.substring(0,filePath.lastIndexOf("/")));
//            List<MethodNode> methodInfos = jsTree.getMethodInfos();
//            if(methodInfos.isEmpty()) return result;
//            for(MethodNode methodInfo: methodInfos){
//                LocationInfo loc = new LocationInfo(methodInfo.getBeginLine(), methodInfo.getEndLine(), null, methodInfo.getSignature());
//                result.add(loc);
//            }
//            return result;
//        }else{
//            log.error("don't support language"+language);
//            return result;
//        }
//    }
}
