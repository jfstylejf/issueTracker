package cn.edu.fudan.dependservice.service;

import cn.edu.fudan.dependservice.domain.ScanStatus;

/**
 * @author shaoxi
 * @version 0.1.0
 * @create 2021-04-25 17:25
 **/
public interface StatusService {
    public ScanStatus getScanStatus(String repouuid);
    public boolean canScan(String repouuid);
}
