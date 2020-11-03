package cn.edu.fudan.scanservice.service;


/**
 * @author fancying
 */
public interface ScanInfoService {

    /**
     * 根据repo id 获取该repo的扫描情况
     * @param repoId
     * @return
     */
    Object getAllScanStatus(String repoId);


}
