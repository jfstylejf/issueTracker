package cn.edu.fudan.dependservice.service.impl;

import cn.edu.fudan.common.jgit.JGitHelper;
import cn.edu.fudan.common.scan.ToolScan;
import cn.edu.fudan.dependservice.config.ShHomeConfig;
import cn.edu.fudan.dependservice.domain.Group;
import cn.edu.fudan.dependservice.domain.RelationShip;
import cn.edu.fudan.dependservice.mapper.GroupMapper;
import cn.edu.fudan.dependservice.mapper.RelationshipMapper;
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
        log.info("resultFile: " + resultFile);

        while (continuedetect) {
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                log.error("error in sleep");
            }
            if (resultFileDetect()) {
                break;
            } else {
                log.info("detect dir: " + resultFileDir);
                log.info("detect sh result every 3 seconds ......");
            }
        }
        long endTime = System.currentTimeMillis();
        log.info("The total cost of waiting for the sh results -> {} second",(endTime-startTime)/1000);

        log.info("sh end time:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        Map<String, List> fileRes = null;
        ReadUtill readUtill = ReadUtill.builder().commitId(commit).repo_uuid(getScanData().getRepoUuid()).build();
        try {
            fileRes = readUtill.getFileResult(resultFile);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("deal result file error");
            return false;
        }

        put2DataBase(fileRes);

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
        File file = new File(resultFile);
        if (!file.exists()) {// 判断是否存在目录
            return;
        }
        // fortest to see what res now is
        file.delete();
    }

    @Override
    public void cleanUpForScan() {

    }

    public void put2DataBase(Map<String, List> fileRes) {
        for (Object g : fileRes.get("group")) {
            addGroup((Group) g);

        }
        for (Object g : fileRes.get("relation")) {
            addRelation((RelationShip) g);
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
