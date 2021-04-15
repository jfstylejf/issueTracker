package cn.edu.fudan.common.scan;

import cn.edu.fudan.common.domain.po.scan.RepoScan;
import org.springframework.stereotype.Service;

@Service
public interface CommonScanService {

    /**
     * 更新扫描信息
     * @param scanInfo repo的扫描信息
     */
    void updateRepoScan(RepoScan scanInfo);

    /**
     * 停止对某个代码库的扫描
     * @param repoUuid repoUuid
     * @return 是否已经停止
     */
    boolean stopScan(String repoUuid);


    /**
     * 停止对某个代码库的扫描
     * @param repoUuid repoUuid
     * @param toolName 工具名
     * @return 是否已经停止
     */
    boolean stopScan(String repoUuid, String toolName);

    /**
     * 删除代码库所有数据
     * @param repoUuid repoUuid
     */
    void deleteRepo(String repoUuid);

    /**
     * 删除代码库所有数据
     * @param repoUuid repoUuid
     * @param toolName 工具名
     */
    void deleteRepo(String repoUuid, String toolName);

    /**
     *  获取某个工具的扫描状态
     * @param repoUuid repoUuid
     * @param toolName 工具名
     **/
    RepoScan getRepoScanStatus(String repoUuid, String toolName);

    /**
     * 获取当前repo 聚合的扫描信息
     * @param repoUuid repoUuid
     * @return RepoScan
     */
    RepoScan getRepoScanStatus(String repoUuid);

}
