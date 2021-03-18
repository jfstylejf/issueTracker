package cn.edu.fudan.cloneservice.task;

import cn.edu.fudan.cloneservice.dao.CloneLocationDao;
import cn.edu.fudan.cloneservice.domain.LocationInfo;
import cn.edu.fudan.cloneservice.domain.Result;
import cn.edu.fudan.cloneservice.domain.clone.CloneLocation;
import cn.edu.fudan.cloneservice.domain.clone.CloneScan;
import cn.edu.fudan.cloneservice.domain.clone.CloneScanInitialInfo;
import cn.edu.fudan.cloneservice.domain.clone.CloneScanResult;
import cn.edu.fudan.cloneservice.util.ASTUtil;
import cn.edu.fudan.codetracker.core.tree.JsTree;
import cn.edu.fudan.codetracker.core.tree.parser.JavaFileParser;
import cn.edu.fudan.codetracker.core.tree.parser.JsFileParser;
import cn.edu.fudan.codetracker.domain.projectinfo.BaseNode;
import cn.edu.fudan.codetracker.domain.projectinfo.ClassNode;
import cn.edu.fudan.codetracker.domain.projectinfo.MethodNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static com.fdse.SagaShell.cloneDetect;
import static cn.edu.fudan.codetracker.core.tree.parser.JsFileParser.*;
import static cn.edu.fudan.codetracker.core.tree.parser.JavaFileParser.*;

/**
 * @author zyh
 * @date 2020/5/25
 */
@Slf4j
@Component("CPUClone")
public class CPUCloneScanOperation extends ScanOperationAdapter {

    private static final String SNIPPET = "snippet";
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    @Value("${clone.workHome}")
    private String cloneWorkHome;
    @Value("${clone.resultFileHome}")
    private String cloneResultFileHome;
    @Value("${clone.home}")
    private String cloneHome;
    @Value("${clone.resultHome}")
    private String cloneResultHome;

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

//    @SuppressWarnings("unchecked")
//    private boolean analyzeResultFile(String repoId, String repoPath, String commitId, String resultFilePath, String type) {
//        SAXReader reader = new SAXReader();
//        try {
//            Document doc = reader.read(new File(resultFilePath));
//            Element root = doc.getRootElement();
//            Iterator<Element> iterator = root.elementIterator("group");
//            List<CloneLocation> cloneLocationList = new ArrayList<>();
//            while (iterator.hasNext()) {
//                Element group = iterator.next();
//                String groupId = group.attributeValue("id");
//                Iterator<Element> cloneInstances = group.elementIterator("cloneInstance");
//                List<CloneLocation> tmpCloneLocationList = new ArrayList<>();
//                //记录同一个clone组的snippet最小行数
//                int min = Integer.MAX_VALUE;
//                while (cloneInstances.hasNext()) {
//                    Element cloneInstance = cloneInstances.next();
//                    String filePath = cloneInstance.attributeValue("path");
//                    String cloneLocationId = UUID.randomUUID().toString();
//                    CloneLocation cloneLocation = new CloneLocation();
//                    cloneLocation.setUuid(cloneLocationId);
//                    cloneLocation.setRepoId(repoId);
//                    cloneLocation.setCommitId(commitId);
//                    cloneLocation.setCategory(groupId);
//                    //截取filePath
//                    cloneLocation.setFilePath(filePath.substring(repoPath.length() + 1));
//                    //clone的方法起始行，结束行
//                    String methodStartLine = cloneInstance.attributeValue("methodStartLine");
//                    String methodEndLine = cloneInstance.attributeValue("methodEndLine");
//                    String methodLines = methodStartLine + "," + methodEndLine;
//                    cloneLocation.setMethodLines(methodLines);
//                    //具体clone代码的其实行结束行
//                    String fragStart = cloneInstance.attributeValue("fragStartLine");
//                    String fragEnd = cloneInstance.attributeValue("fragEndLine");
//                    String cloneLines = fragStart + "," + fragEnd;
//                    cloneLocation.setCloneLines(cloneLines);
//                    //method or snippet
//                    cloneLocation.setType(type);
//                    //类名 方法名
//                    cloneLocation.setClassName(cloneInstance.attributeValue("className"));
//                    cloneLocation.setMethodName(cloneInstance.attributeValue("methodName"));
//                    ASTUtil.CodeLocation codeLocation = new ASTUtil().getCode(Integer.parseInt(methodStartLine),
//                            Integer.parseInt(methodEndLine),
//                            Integer.parseInt(fragStart),
//                            Integer.parseInt(fragEnd),
//                            filePath);
//                    String code = codeLocation.getCode();
//                    List<String> num = codeLocation.getNum();
//                    //记录clone组内最小的片段行数
//                    if (num.size() < min) {
//                        min = num.size();
//                    }
//                    //具体代码
//                    cloneLocation.setCode(code);
//                    //去除空行和注释的行数
//                    //修改录入的方式
//                    cloneLocation.setNum(String.join(",", num));
//                    //插入列表
//                    tmpCloneLocationList.add(cloneLocation);
//                }
//                //大于所规定的最小片段行数才入库
//                if (min >= minSnippetNum && !isIntersection(tmpCloneLocationList)) {
//                    //过滤测试代码的clone检测
//                    List<CloneLocation> cloneLocationList1 = wipeOffTest(tmpCloneLocationList);
//                    if (cloneLocationList1 != null) {
//                        cloneLocationList.addAll(tmpCloneLocationList);
//                    }
//                }
//            }
//            if (!cloneLocationList.isEmpty()) {
//                cloneLocationDao.insertCloneLocations(cloneLocationList);
//            }
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }

    private boolean invokeCloneTool(String repoPath, String granularity) {
        String cmd = cloneWorkHome + "/main.sh " + repoPath + " " + granularity;
        try {
            Process processMethod = Runtime.getRuntime().exec(cmd, null, new File(cloneWorkHome));
            processMethod.waitFor();
            if (processMethod.exitValue() == 0) {
                log.info("{} -> method scan complete -> {}", Thread.currentThread().getName(), cmd);
            }
            return processMethod.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //to do:调用外部可执行文件仍然占据大量cpu算力、且有并发问题，可以把可执行文件的源码直接集成吗？
    private boolean invokeCloneTool(String repoPath, String granularity, String language) {
        try {
            String system = IS_WINDOWS?"win10":"linux";
            String[] configs = new String[]{
                    "dataset=" + repoPath,
                    "language=" + language,
                    "extensions=" + language,
                    "granularity=" + granularity,
                    "exe="+cloneHome+"executable/executable_cpu_"+system+"_snippet.exe",
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

    private boolean analyzeResultFile(String repoId, String repoPath, String commitId, String type, String language) throws IOException {
        try {
            BufferedReader resultReader = new BufferedReader(new FileReader(cloneResultFileHome + "type12_" + type + "_result.csv"));
            Map<String, List<Result>> groups = getResultFromFile(resultReader);
            List<CloneLocation> cloneLocationList = new ArrayList<>();
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
                    //类名 方法名
                    List<LocationInfo> locationInfos;
                    if("java".equals(language)) {
                        locationInfos = getJavaClassAndMethodList(repoPath, filePath);
                    }else if("js".equals(language)){
                        locationInfos = getJsClassAndMethodList(repoPath, filePath);
                    }else{
                        locationInfos = new ArrayList<>();
                    }
                    if(!locationInfos.isEmpty()) {
                        cloneLocation.setClassName(result.getRepoPath().substring(result.getRepoPath().lastIndexOf("/"), result.getRepoPath().lastIndexOf(".")));
                    }
                    else{
                        cloneLocation.setClassName(getClassAndMethod(cloneLines, locationInfos).get(0));
                        cloneLocation.setMethodName(getClassAndMethod(cloneLines, locationInfos).get(1));
                    }

                    String[] methodLoc = result.getMethodLoc().split(",");
                    String[] snippetLoc = result.getMethodLoc().split(",");
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
                        cloneLocationList.addAll(tmpCloneLocationList);
                    }
                }
            }
            if (!cloneLocationList.isEmpty()) {
                cloneLocationDao.insertCloneLocations(cloneLocationList);
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
        while ((line = resultReader.readLine()) != null) {
            String[] lineResult = line.split(",");
            if (lineResult.length == 5) {
                Result result = new Result(lineResult[0], lineResult[1], lineResult[2].concat(lineResult[3]), lineResult[4].concat(lineResult[5]));
                results.add(result);

            }
        }
        Map<String, List<Result>> resultMap = new HashMap<>();
        for (Result result : results) {
            resultMap.computeIfAbsent(result.getGroupId(), k -> new ArrayList<>()).add(result);
        }
        return resultMap;
    }

    @Override
    public CloneScanResult doScan(CloneScanInitialInfo cloneScanInitialInfo) throws IOException {
        CloneScan cloneScan = cloneScanInitialInfo.getCloneScan();
        String repoId = cloneScan.getRepoId();
        String commitId = cloneScan.getCommitId();
        String type = cloneScan.getType();
        String repoPath = cloneScanInitialInfo.getRepoPath();
        String language = cloneScanInitialInfo.getLanguage();
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
            if (!analyzeResultFile(repoId, repoPath, commitId, type, language)) {
                log.error("{} -> Result File Analyze Failed!", Thread.currentThread().getName());
                return new CloneScanResult(repoId, commitId, type, "failed", "analyze failed");
            }
            log.info("{} -> resultFile analyze complete", Thread.currentThread().getName());
        }
        return new CloneScanResult(repoId, commitId, type, "success", "Scan Success");
    }

    public List<String> getClassAndMethod(String cloneLines, List<LocationInfo> locationInfos){
        List<String> result = new ArrayList<>();
        List<String> cloneInfo = new ArrayList<>(Arrays.asList(cloneLines.split(",")));
        int beginLine = Integer.parseInt(cloneInfo.get(0));
        int endLine = Integer.parseInt(cloneInfo.get(1));
        if(locationInfos.isEmpty()) return result;
        for(LocationInfo locationInfo: locationInfos){
            if(beginLine >= locationInfo.getBeginLine() && endLine <= locationInfo.getEndLine()){
                result.add(locationInfo.getClassName());
                result.add(locationInfo.getMethodName());
                return result;
            }
        }
        return result;
    }

    public List<LocationInfo> getJavaClassAndMethodList(String repoPath, String filePath){
        List<LocationInfo> locInfosJava = new ArrayList<>();
        JavaFileParser javaFileParser = new JavaFileParser();
        javaFileParser.parse(filePath.substring(repoPath.length() + 1), "1", filePath.substring(repoPath.lastIndexOf(".")+1));
        javaFileParser.parseClassOrInterface();
        List<? extends BaseNode> children = javaFileParser.getFileNode().getChildren();
        if(!children.isEmpty()) {
            for (BaseNode child : children) {
                if (child instanceof ClassNode) {
                    int begin = child.getBeginLine();
                    int end = child.getEndLine();
                    ClassNode classNode = (ClassNode) child;
                    if (!classNode.getChildren().isEmpty()) {
                        for (BaseNode grandChild : classNode.getChildren()) {
                            if (grandChild instanceof MethodNode) {
                                MethodNode methodNode = (MethodNode) grandChild;
                                if (methodNode.getBeginLine() != begin) {
                                    LocationInfo locationInfo1 = new LocationInfo(begin, methodNode.getBeginLine(), classNode.getClassName(), null);
                                    locInfosJava.add(locationInfo1);
                                }
                                LocationInfo locInfo = new LocationInfo(methodNode.getBeginLine(), methodNode.getEndLine(), classNode.getClassName(), methodNode.getFullName());
                                locInfosJava.add(locInfo);
                                begin = methodNode.getEndLine();
                            }
                        }
                        if (begin != end) {
                            LocationInfo locationInfo2 = new LocationInfo(begin, end, classNode.getClassName(), null);
                            locInfosJava.add(locationInfo2);
                        }
                    }
                }
            }
        }
        return locInfosJava;
    }

    public List<LocationInfo> getJsClassAndMethodList(String repoPath, String filePath){
        List<LocationInfo> locInfos = new ArrayList<>();
        JsFileParser jsFileParser = new JsFileParser();
        jsFileParser.parse(filePath.substring(repoPath.length() + 1), "1", filePath.substring(repoPath.lastIndexOf(".")+1));
        List<? extends BaseNode> children = jsFileParser.getFileNode().getChildren();
        if(!children.isEmpty()){
            for(BaseNode child: children){
                if(child instanceof ClassNode){
                    int begin = child.getBeginLine();
                    int end = child.getEndLine();
                    ClassNode classNode = (ClassNode) child;
                    if(!classNode.getChildren().isEmpty()){
                        for(BaseNode grandChild: classNode.getChildren()){
                            if(grandChild instanceof MethodNode){
                                MethodNode methodNode = (MethodNode) grandChild;
                                if(methodNode.getBeginLine()!=begin){
                                    LocationInfo locationInfo1 = new LocationInfo(begin, methodNode.getBeginLine(), classNode.getClassName(), null);
                                    locInfos.add(locationInfo1);
                                }
                                LocationInfo locInfo = new LocationInfo(methodNode.getBeginLine(), methodNode.getEndLine(), classNode.getClassName(), methodNode.getFullName());
                                locInfos.add(locInfo);
                                begin = methodNode.getEndLine();
                            }
                        }
                        if(begin!=end){
                            LocationInfo locationInfo2 = new LocationInfo(begin, end, classNode.getClassName(), null);
                            locInfos.add(locationInfo2);
                        }
                    }

                }
                else if(child instanceof MethodNode){
                    MethodNode methodNode = (MethodNode) child;
                    LocationInfo locInfo = new LocationInfo(methodNode.getBeginLine(), methodNode.getEndLine(), null, methodNode.getFullName());
                    locInfos.add(locInfo);
                }
            }
        }
        return locInfos;
    }

    public static void main(String[] args) {
        JsFileParser.setBabelPath("E:\\Lab\\gitlab\\IssueTracker-Master\\clone-service\\src\\main\\resources\\node\\babelEsLint.js");
        JsTree jsTree = new JsTree(Collections.singletonList("C:\\Users\\fancy\\Desktop\\testCode\\Measure.js"), "t" , "C:\\Users\\fancy\\Desktop\\testCode");

        System.out.printf(jsTree.toString());
    }
}
