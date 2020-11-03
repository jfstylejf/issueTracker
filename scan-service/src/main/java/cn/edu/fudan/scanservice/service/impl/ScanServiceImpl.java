package cn.edu.fudan.scanservice.service.impl;

import cn.edu.fudan.scanservice.service.InvokeToolService;
import cn.edu.fudan.scanservice.service.ScanService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * description:
 *
 * @author fancying
 * create: 2020-04-21 16:27
 **/
@Slf4j
@Service
public class ScanServiceImpl implements ScanService {

    @Value("${defaultScanInterval}")
    private int defaultScanInterval;

    private InvokeToolService invokeToolService;

    @Override
    public void scan(String repoId, String branch) {
        scan(repoId, branch, new DateTime().minusYears(defaultScanInterval).toDate());
    }

    @Override
    public void scan(String repoId, String branch, Date startDate) {
        // TODO
        String commitId = getFirstCommitAfterDate(repoId, startDate);
        invokeToolService.invokeTools(repoId, branch, commitId);
    }

    @Override
    public void scan(String repoId, String branch, String commitId) {
        invokeToolService.invokeTools(repoId, branch, commitId);
    }

    private String getFirstCommitAfterDate(String repoId, Date startDate) {
        // TODO 得到指定时间之后的第一个commit
        return "";
    }

    @Autowired
    public ScanServiceImpl(InvokeToolService invokeToolService) {
        this.invokeToolService = invokeToolService;
    }

}