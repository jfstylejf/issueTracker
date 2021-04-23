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

    /**
     * 删除指定repo得所有信息
     * @param repoId
     */
    void deleteOneRepo(String repoId, String token) throws InterruptedException;

}
