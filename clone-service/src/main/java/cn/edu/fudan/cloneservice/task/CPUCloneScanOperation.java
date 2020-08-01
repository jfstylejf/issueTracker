package cn.edu.fudan.cloneservice.task;

import cn.edu.fudan.cloneservice.dao.CloneLocationDao;
import cn.edu.fudan.cloneservice.domain.clone.CloneLocation;
import cn.edu.fudan.cloneservice.domain.clone.CloneScan;
import cn.edu.fudan.cloneservice.domain.clone.CloneScanInitialInfo;
import cn.edu.fudan.cloneservice.domain.clone.CloneScanResult;
import cn.edu.fudan.cloneservice.util.ASTUtil;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * @author zyh
 * @date 2020/5/25
 */
@Slf4j
@Component("CPUClone")
public class CPUCloneScanOperation extends ScanOperationAdapter {

    private static final String SNIPPET = "snippet";

    @Value("${clone.workHome}")
    private String cloneWorkHome;
    @Value("${clone.resultFileHome}")
    private String cloneResultFileHome;

    @Value("${min.snippet.num}")
    private int minSnippetNum;

    private CloneLocationDao cloneLocationDao;

    @Autowired
    private void setCloneLocationDao(CloneLocationDao cloneLocationDao) {
        this.cloneLocationDao = cloneLocationDao;
    }

    /**
     * 检测clone location是否存在交叉重复
     * @param tmpCloneLocationList clone location list
     * @return 存在为true
     */
    private boolean isIntersection(List<CloneLocation> tmpCloneLocationList){

        for(int i = 0; i < tmpCloneLocationList.size(); i++){
            for(int j = i + 1; j < tmpCloneLocationList.size(); j++){
                if(tmpCloneLocationList.get(i).getFilePath().equals(tmpCloneLocationList.get(j).getFilePath())){
                    int start1 = Integer.parseInt(tmpCloneLocationList.get(i).getCloneLines().split(",")[0]);
                    int end1 = Integer.parseInt(tmpCloneLocationList.get(i).getCloneLines().split(",")[1]);
                    int start2 = Integer.parseInt(tmpCloneLocationList.get(j).getCloneLines().split(",")[0]);
                    int end2 = Integer.parseInt(tmpCloneLocationList.get(j).getCloneLines().split(",")[1]);
                    boolean exit = (start1 >= start2 && start1 <= end2) ||
                            (end1 >= start2 && end1 <= end2) ||
                            (start2 >= start1 && start2 <=end1) ||
                            (end2 >=start1 && end2 <= end1);
                    if(exit){
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 过滤对测试代码的入库
     * @param tmpCloneLocationList clone组
     * @return 过滤后的clone组
     */
    private List<CloneLocation> wipeOffTest(List<CloneLocation> tmpCloneLocationList){

        List<CloneLocation> cloneLocationList = new ArrayList<>();
        //!!!
        tmpCloneLocationList.forEach(cloneLocation -> {
            String className = cloneLocation.getFilePath().toLowerCase();
            String fullName = className.substring(className.lastIndexOf("/") + 1);
            if(!className.contains("/test/") && !fullName.startsWith("test") && !fullName.endsWith("test.java") && !fullName.endsWith("tests.java")){
                cloneLocationList.add(cloneLocation);
            }
        });

        if(cloneLocationList.size() > 1){
            return cloneLocationList;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private boolean analyzeResultFile(String repoId,String repoPath,String commitId,String resultFilePath, String type){
        SAXReader reader = new SAXReader();
        try{
            Document doc = reader.read(new File(resultFilePath));
            Element root = doc.getRootElement();
            Iterator<Element> iterator = root.elementIterator("group");
            List<CloneLocation> cloneLocationList=new ArrayList<>();
            while (iterator.hasNext()){
                Element group=iterator.next();
                String groupId=group.attributeValue("id");
                Iterator<Element> cloneInstances=group.elementIterator("cloneInstance");
                List<CloneLocation> tmpCloneLocationList = new ArrayList<>();
                //记录同一个clone组的snippet最小行数
                int min = Integer.MAX_VALUE;
                while(cloneInstances.hasNext()){
                    Element cloneInstance=cloneInstances.next();
                    String filePath=cloneInstance.attributeValue("path");
                    String cloneLocationId= UUID.randomUUID().toString();
                    CloneLocation cloneLocation = new CloneLocation();
                    cloneLocation.setUuid(cloneLocationId);
                    cloneLocation.setRepoId(repoId);
                    cloneLocation.setCommitId(commitId);
                    cloneLocation.setCategory(groupId);
                    //截取filePath
                    cloneLocation.setFilePath(filePath.substring(repoPath.length() + 1));
                    //clone的方法起始行，结束行
                    String methodStartLine = cloneInstance.attributeValue("methodStartLine");
                    String methodEndLine = cloneInstance.attributeValue("methodEndLine");
                    String methodLines = methodStartLine + "," + methodEndLine;
                    cloneLocation.setMethodLines(methodLines);
                    //具体clone代码的其实行结束行
                    String fragStart=cloneInstance.attributeValue("fragStartLine");
                    String fragEnd=cloneInstance.attributeValue("fragEndLine");
                    String cloneLines = fragStart + "," + fragEnd;
                    cloneLocation.setCloneLines(cloneLines);
                    //method or snippet
                    cloneLocation.setType(type);
                    //类名 方法名
                    cloneLocation.setClassName(cloneInstance.attributeValue("className"));
                    cloneLocation.setMethodName(cloneInstance.attributeValue("methodName"));
                    ASTUtil.CodeLocation codeLocation = new ASTUtil().getCode(Integer.parseInt(methodStartLine),
                            Integer.parseInt(methodEndLine),
                            Integer.parseInt(fragStart),
                            Integer.parseInt(fragEnd),
                            filePath);
                    String code = codeLocation.getCode();
                    List<String> num = codeLocation.getNum();
                    //记录clone组内最小的片段行数
                    if(num.size() < min){
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
                if(min >= minSnippetNum && !isIntersection(tmpCloneLocationList)){
                    //过滤测试代码的clone检测
                    List<CloneLocation> cloneLocationList1 = wipeOffTest(tmpCloneLocationList);
                    if(cloneLocationList1!=null){
                        cloneLocationList.addAll(tmpCloneLocationList);
                    }
                }
            }
            if(!cloneLocationList.isEmpty()){
                cloneLocationDao.insertCloneLocations(cloneLocationList);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private boolean invokeCloneTool(String repoPath, String granularity){
        String cmd = cloneWorkHome + "/main.sh " + repoPath + " " + granularity;
        try {
            Process processMethod = Runtime.getRuntime().exec(cmd,null,new File(cloneWorkHome));
            processMethod.waitFor();
            if(processMethod.exitValue()==0){
                log.info("{} -> method scan complete -> {}",Thread.currentThread().getName(), cmd);
            }
            return processMethod.exitValue()==0;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public CloneScanResult doScan(CloneScanInitialInfo cloneScanInitialInfo) {
        CloneScan cloneScan = cloneScanInitialInfo.getCloneScan();
        String repoId = cloneScan.getRepoId();
        String commitId = cloneScan.getCommitId();
        String type = cloneScan.getType();
        String repoPath = cloneScanInitialInfo.getRepoPath();
        log.info("{} -> start to invoke tool to scan......", Thread.currentThread().getName());
        if (!invokeCloneTool(repoPath, type)) {
            log.error("{} -> Invoke Analyze Tool Failed!", Thread.currentThread().getName());
            //return new CloneScanResult("clone","failed", "tool invoke failed");
            return new CloneScanResult(repoId, commitId, type, "failed", "tool invoke failed");
        }
        log.info("{} -> tool invoke complete!", Thread.currentThread().getName());
        log.info("{} -> scan complete", Thread.currentThread().getName());
        log.info("{} -> start to analyze resultFile......", Thread.currentThread().getName());
        String resultFilePath = cloneResultFileHome;
        //只有片段级的入库
        if(SNIPPET.equals(type)){
            if(!analyzeResultFile(repoId, repoPath, commitId, resultFilePath, type)){
                log.error("{} -> Result File Analyze Failed!", Thread.currentThread().getName());
                return new CloneScanResult(repoId, commitId, type, "failed", "analyze failed");
            }
            log.info("{} -> resultFile analyze complete", Thread.currentThread().getName());
        }
        return new CloneScanResult(repoId, commitId, type, "success", "Scan Success");
    }


}
