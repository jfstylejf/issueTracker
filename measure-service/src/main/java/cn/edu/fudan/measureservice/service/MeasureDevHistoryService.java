package cn.edu.fudan.measureservice.service;

public interface MeasureDevHistoryService {

    /**
     * 根据repoUuid和起止时间段，查询commit info 返回给前端
     */
    Object getDevHistoryCommitInfo(String repoUuid, String since, String until);

    /**
     * 根据commitId，返回file 相关数据 给前端
     */
    Object getDevHistoryFileInfo(String commitId);
}
