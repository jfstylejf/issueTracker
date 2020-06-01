package cn.edu.fudan.cloneservice.scan.task;


import cn.edu.fudan.cloneservice.scan.domain.CloneScanInitialInfo;
import cn.edu.fudan.cloneservice.scan.domain.CloneScanResult;

/**
 * @author zyh
 * @date 2020/5/25
 */
public interface ScanOperation {

    /**
     * 判断是否扫描过
     * @param repoId repo id
     * @param commitId commit id
     * @param type snippet or method
     * @return true 表示已经扫描过
     */
    boolean isScanned(String repoId,String commitId,String type);

    /**
     * 初始化一个扫描，clone扫描的开始
     * @param repoId repo id
     * @param commitId commit id
     * @param type snippet or method
     * @return CloneScanInitialInfo
     */
    CloneScanInitialInfo initialScan(String repoId, String commitId, String type);

    /**
     * clone扫描
     * @param cloneScanInitialInfo CloneScanInitialInfo
     * @return scan result
     */
    CloneScanResult doScan(CloneScanInitialInfo cloneScanInitialInfo);

    /**
     * 更新scan的结果
     * @param cloneScanInitialInfo cloneScanInitialInfo
     * @return true 更新成功
     */
    boolean updateScan(CloneScanInitialInfo cloneScanInitialInfo);
}
