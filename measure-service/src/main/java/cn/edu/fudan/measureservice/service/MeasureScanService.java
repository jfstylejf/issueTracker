package cn.edu.fudan.measureservice.service;

import cn.edu.fudan.measureservice.domain.dto.FileInfo;
import cn.edu.fudan.measureservice.domain.dto.RepoResourceDTO;

import java.io.IOException;

public interface MeasureScanService {


    /**
     * 根据repoUuid，获取项目的扫描状态
     * @param repoUuid id
     * @return object Map<String, Object>
     */
    Object getScanStatus(String repoUuid);

    /**
     * 根据repoUuid和起始commit，对项目进行度量方面的扫描，即将数据入库
     * @param repoResource 代码库信息
     * @param branch 分支
     * @param beginCommit 开始扫描的commit
     */
    void scan(RepoResourceDTO repoResource, String branch, String beginCommit);

    /**
     * 删除一个项目的所有度量信息
     * @param repoUuid repo的唯一标识
     */
    void stop(String repoUuid);

    /**
     * 删除一个项目的所有度量信息
     * @param repoUuid repo的唯一标识
     */
    void delete(String repoUuid);

    /**
     * 解析文件信息
     * @param filePath 文件路径
     * @throws IOException
     * @return
     */
    FileInfo parseFileInfo(String filePath) throws IOException;

}
