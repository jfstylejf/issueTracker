package cn.edu.fudan.measureservice.service;

import cn.edu.fudan.measureservice.domain.dto.RepoResourceDTO;

public interface MeasureScanService {


    /**
     * 根据repoId，获取项目的扫描状态
     * @param repoId id
     * @return object Map<String, Object>
     */
    Object getScanStatus(String repoId);

    /**
     * 根据repoId和起始commit，对项目进行度量方面的扫描，即将数据入库
     * @param repoResource 代码库信息
     * @param branch 分支
     * @param beginCommit 开始扫描的commit
     * @param toolName 工具名称
     */
    void scan(RepoResourceDTO repoResource, String branch, String beginCommit, String toolName);

    /**
     * 删除一个项目的所有度量信息
     * @param repoId repo的唯一标识
     */
    void stop(String repoId);

    /**
     * 删除一个项目的所有度量信息
     * @param repoId repo的唯一标识
     */
    void delete(String repoId);


}
