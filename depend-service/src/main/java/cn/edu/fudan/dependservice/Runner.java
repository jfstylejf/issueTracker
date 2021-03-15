package cn.edu.fudan.dependservice;

import org.springframework.stereotype.Service;

/**
 * description: 调用工具 将数据入库
 *
 * @author fancying
 * create: 2021-02-24 09:39
 **/
@Service
public interface Runner {

    void runTool(String repoPath, String commitID);
}
