package cn.edu.fudan.dependservice.service.impl;

import cn.edu.fudan.common.jgit.JGitHelper;
import cn.edu.fudan.common.scan.ToolScan;
import cn.edu.fudan.dependservice.config.ShHomeConfig;
import cn.edu.fudan.dependservice.domain.Group;
import cn.edu.fudan.dependservice.domain.RelationShip;
import cn.edu.fudan.dependservice.mapper.GroupMapper;
import cn.edu.fudan.dependservice.mapper.RelationshipMapper;
import cn.edu.fudan.dependservice.utill.WriteUtill;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    // where sh run
    String dependenceHome;
    String shName;
    String resultFile;
    String resultFileDir;
    ApplicationContext applicationContext;
    //    @Resource
    @Autowired
    RelationshipMapper relationshipMapper;

    @Autowired
    GroupMapper groupMapper;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean scanOneCommit(String commit) {
        //checkout to this commit id
        //
//        String repoPath = scanData.getRepoPath();
        ShThread shRunner = new ShThread();
        shRunner.setShName(shName);
        shRunner.setDependenceHome(dependenceHome);
        shRunner.setRepoPath(scanData.getRepoPath());

        Thread shThread = new Thread(shRunner);
        shThread.start();
        log.info("sh start time:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        long startTime = System.currentTimeMillis();
        boolean continuedetect = true;
        log.info("wait for sh result");

        while (continuedetect) {
            if ((System.currentTimeMillis() - startTime) / 1000 > 600) {
                log.error("wait for sh result time too long");
                return false;
            }
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                log.error("exception ms:" + e.getMessage());
                return false;
            }
            if (resultFileDetect()) {
                break;
            }
        }
        long endTime = System.currentTimeMillis();
        log.info("The total cost of waiting for the sh results -> {} second", (endTime - startTime) / 1000);

        Map<String, List> fileRes = null;
        ReadUtill readUtill = ReadUtill.builder().commitId(commit).repo_uuid(getScanData().getRepoUuid()).build();
        try {
            fileRes = readUtill.getFileResult(resultFile);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("deal result file error");
            return false;
        }
        if (fileRes.size() > 0) {
            put2DataBase(fileRes);

        }

        return true;
    }

    public boolean resultFileDetect() {
        File dir = new File(resultFileDir);
        if (!dir.isDirectory()) {
            log.error("resultfileDir Wrong");
            return false;
        }

        String[] files = dir.list();// 读取目录下的所有目录文件信息
        for (int i = 0; i < files.length; i++) {// 循环，添加文件名或回调自身
            File file = new File(dir, files[i]);
            if (file.isFile() && file.getName().matches(".*\\.xlsx")) {// 如果文件
                this.resultFile = this.resultFileDir + file.getName();
                return true;
            }
        }
        return false;
    }

    @Override
    public void prepareForScan() {

        //  this.dependenceHome = applicationContext.getBean(ShHomeConfig.class).getDependenceHome();
        this.setDependenceHome(applicationContext.getBean(ShHomeConfig.class).getDependenceHome());
        this.setShName(applicationContext.getBean(ShHomeConfig.class).getShName());
        this.setResultFileDir(applicationContext.getBean(ShHomeConfig.class).getResultFileDir());
        //make config file
        String configFile = this.resultFileDir + "source-project-conf.json";
        log.info("configFile :" + configFile);
        WriteUtill.writeProjecConf(configFile, this.getScanData().getRepoPath());


    }

    @Override
    public void prepareForOneScan(String commit) {
        // check out to commit
        String repoPath = this.getScanData().getRepoPath();
        JGitHelper jGitHelper = new JGitHelper(repoPath);
        jGitHelper.checkout(commit);
    }

    @Override
    public void cleanUpForOneScan(String commit) {
        try {
            File file = new File(resultFile);
            if (!file.exists()) {// 判断是否存在目录
                return;
            }
            // fortest to see what res now is
            file.delete();
            // end Thread
        } catch (Exception e) {
            log.info("no  sh result");
            log.error(e.getMessage());
        } finally {
            ShThread2 shRunner = new ShThread2();
            shRunner.setShName("tdepend2.sh");
            shRunner.setDependenceHome(dependenceHome);
            shRunner.setRepoPath(scanData.getRepoPath());
            Thread shThread = new Thread(shRunner);
            shThread.start();
        }


    }


    @Override
    public void cleanUpForScan() {

    }

    public void put2DataBase(Map<String, List> fileRes) {

        if (fileRes.containsKey("group")) {
            for (Object g : fileRes.get("group")) {
                addGroup((Group) g);

            }

        }
        if (fileRes.containsKey("relation")) {
            for (Object g : fileRes.get("relation")) {
                addRelation((RelationShip) g);
            }

        }


    }

    public int addRelation(RelationShip entity1) {

        int rows = 0;
        rows = relationshipMapper.add(entity1);
        return rows;
    }

    public int addGroup(Group entity) {

        int rows = 0;
        rows = groupMapper.add(entity);
        return rows;
    }
}
