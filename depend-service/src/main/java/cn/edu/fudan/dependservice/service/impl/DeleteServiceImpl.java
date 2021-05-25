package cn.edu.fudan.dependservice.service.impl;

import cn.edu.fudan.dependservice.component.RestInterfaceManager;
import cn.edu.fudan.dependservice.dao.DeleteDao;
import cn.edu.fudan.dependservice.service.DeleteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author shaoxi
 * @version 0.1.0
 * @create 2021-05-11 11:08
 **/
@Slf4j
@Service
public class DeleteServiceImpl implements DeleteService {
    @Autowired
    DeleteDao deleteDao;

    @Autowired
    RestInterfaceManager restInterfaceManager;
    @Override
    @Async("taskExecutor")
    public void deleteOneRepo(String repoUuid, String token) throws InterruptedException {
        log.info("to delete");
        boolean res =deleteRecursive(repoUuid);
        log.info("end delete");
        if(res){
            boolean recallres=restInterfaceManager.deleteRecall(repoUuid,token);
            if(recallres){
                log.info(" recall ok");
            }else {
                log.error(" recall fail");
            }
        }

    }
    public boolean deleteRecursive(String repoUuid) throws InterruptedException {
        if(deleteDao.norepoData(repoUuid)){
            return true;
        }
        deleteDao.delete(repoUuid);
        return deleteRecursive(repoUuid);
    }
}
