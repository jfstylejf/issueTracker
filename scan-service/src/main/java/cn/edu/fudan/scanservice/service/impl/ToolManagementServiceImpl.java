package cn.edu.fudan.scanservice.service.impl;

import cn.edu.fudan.scanservice.dao.ToolDao;
import cn.edu.fudan.scanservice.domain.dbo.Tool;
import cn.edu.fudan.scanservice.mapper.ToolMapper;
import cn.edu.fudan.scanservice.service.ToolManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * description:
 *
 * @author fancying
 * create: 2020-04-25 18:05
 **/
@Service
public class ToolManagementServiceImpl implements ToolManagementService {

    private ToolDao toolDao;

    @Override
    public Integer modifyToolStatus(int id, int enabled) {
        return toolDao.modifyToolEnabled(id, enabled);
    }

    @Override
    public List<Tool> getAllTools() {
        return toolDao.getAllTools();
    }

    @Autowired
    public void setToolDao(ToolDao toolDao) {
        this.toolDao = toolDao;
    }
}