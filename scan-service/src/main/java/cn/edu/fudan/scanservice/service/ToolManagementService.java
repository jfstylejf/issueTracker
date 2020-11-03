package cn.edu.fudan.scanservice.service;

import cn.edu.fudan.scanservice.domain.dbo.Tool;

import java.util.List;

/**
 * description: 配置工具管理
 *
 * @author fancying
 * create: 2020-03-02 22:26
 **/
public interface ToolManagementService {

//    /**
//     * 设置看到的数据来源
//     */
//    Integer modifyDisplayData(int id, int enabled);

    /**
     * 设置是否启用工具
     * @param id tool id
     * @param enabled 0 for turn off  1 for turn on
     * @return is success
     */
    Integer modifyToolStatus(int id, int enabled);

    /**
     * 显示所有的工具
     *
     * @return tool list
     */
    List<Tool> getAllTools();

}
