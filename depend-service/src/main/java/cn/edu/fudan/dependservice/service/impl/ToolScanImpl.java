package cn.edu.fudan.dependservice.service.impl;

import cn.edu.fudan.common.jgit.JGitHelper;
import cn.edu.fudan.common.scan.ToolScan;
import cn.edu.fudan.dependservice.config.ShHomeConfig;
import cn.edu.fudan.dependservice.domain.RelationShip;
import cn.edu.fudan.dependservice.mapper.RelationshipMapper;
import com.alibaba.druid.stat.TableStat;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.management.relation.Relation;
import java.util.concurrent.TimeUnit;

/**
 * description:
 *
 * @author fancying
 * create: 2021-03-02 21:06
 **/
@Slf4j
@Service
@Data
public class ToolScanImpl implements ToolScan {
    String dependenceHome;
    String shName;
    ApplicationContext applicationContext;
//    @Resource
    @Autowired
    RelationshipMapper relationshipMapper;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    @Override
    public boolean scanOneCommit(String commit) {
        //checkout to this commit id
//        runSh();

        put2DataBase();




        return false;
    }

    @Override
    public void prepareForScan() {

        this.dependenceHome = applicationContext.getBean(ShHomeConfig.class).getDependenceHome();
        this.setShName(applicationContext.getBean(ShHomeConfig.class).getDependenceHome());
        //dont need prepare
        //change to commitid
        //get repoPath

    }

    @Override
    public void prepareForOneScan(String commit) {
        // check out to commit
        String repoPath =this.getScanData().getRepoPath();
        JGitHelper jGitHelper = new JGitHelper(repoPath);
        jGitHelper.checkout(commit);
        // commitid have branch info



    }

    @Override
    public void cleanUpForOneScan(String commit) {
        //what is clean up
        // delete file

    }

    @Override
    public void cleanUpForScan() {

    }
    public void put2DataBase(){
        log.debug("-----------------------\n come to put2dsatabase\n\n");
        RelationShip re=new RelationShip();
        re.setCommit_id("temp");
        re.setDepend_details("tempdetail");
        re.setFile("temp.file");
        re.setGroup_id(11);
        re.setRepo_uuid("temp_repo");

        add(re);


    }
    public int add(RelationShip entity1){

        int rows=0;
        rows= relationshipMapper.add(entity1);
        return rows;
    }
    public boolean runSh() {
        String repoPath =this.getScanData().getRepoPath();
        try {
            Runtime rt = Runtime.getRuntime();
//            String command ="sh "+shHome+shName+" "+repoPath+" "+commitID;
            String command ="sh "+dependenceHome+shName+" "+repoPath;
            log.info("command -> {}",command);

            Process process = rt.exec(command);
            //最多等待sonar脚本执行200秒,超时则认为该commit解析失败
            boolean timeout = process.waitFor(300L, TimeUnit.SECONDS);
            if (!timeout) {
                process.destroy();
                log.error("invoke tool timeout ! (300s)");
                return false;
            }
            return process.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
