package cn.edu.fudan.dependservice.service.impl;

import cn.edu.fudan.common.scan.ToolScan;

/**
 * description:
 *
 * @author fancying
 * create: 2021-03-02 21:06
 **/
public class ToolScanImpl implements ToolScan {
    @Override
    public boolean scanOneCommit(String commit) {
        return false;
    }

    @Override
    public void prepareForScan() {

    }

    @Override
    public void prepareForOneScan(String commit) {

    }

    @Override
    public void cleanUpForOneScan(String commit) {

    }

    @Override
    public void cleanUpForScan() {

    }
}
