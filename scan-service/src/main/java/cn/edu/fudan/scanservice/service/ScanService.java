package cn.edu.fudan.scanservice.service;

import java.util.Date;

/**
 * description:
 *
 * @author fancying
 * create: 2020-04-21 00:05
 **/
public interface ScanService {

    /**
     * description
     *
     * @param repoId id
     * @param branch branch
     */
    void scan(String repoId, String branch);

    /**
     * description
     *
     * @param repoId id
     * @param branch branch
     * @param commitId commit
     */
    void scan(String repoId, String branch, String commitId);

    /**
     * description
     *
     * @param repoId id
     * @param branch branch
     * @param startDate eg 2020-01-01
     */
    void scan(String repoId, String branch, Date startDate);

}
