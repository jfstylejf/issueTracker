package cn.edu.fudan.dependservice.service.impl;

import cn.edu.fudan.dependservice.dao.ScanDao;
import cn.edu.fudan.dependservice.domain.ScanStatus;
import cn.edu.fudan.dependservice.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author shaoxi
 * @version 0.1.0
 * @create 2021-04-25 17:26
 **/
@Service
public class StatusServiceImpl  implements StatusService {
    @Autowired
    ScanDao scanDao;
    @Override
    public ScanStatus getScanStatus(String repouuid) {

        return scanDao.getScanStatus(repouuid);
    }
}
