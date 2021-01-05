package cn.edu.fudan.scanservice.service;

import cn.edu.fudan.scanservice.component.rest.RestInterfaceManager;
import cn.edu.fudan.scanservice.dao.ScanDao;
import cn.edu.fudan.scanservice.dao.ToolDao;
import cn.edu.fudan.scanservice.domain.dbo.Scan;
import cn.edu.fudan.scanservice.domain.dbo.Tool;
import cn.edu.fudan.scanservice.mapper.ToolMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * description: invoke tool
 *
 * @author fancying
 * create: 2020-04-22 16:45
 **/
@Service
@Slf4j
public class InvokeToolService {

    private ToolDao toolDao;
    private RestInterfaceManager restInvoker;
    private ScanDao scanDao;


    public void invokeTools(String repoId, String branch, String startCommit) {
        final int enabled = 1;
        Map<Integer,String> preToolInvokeMap = new HashMap<> ();

        //第一步 先获取当前repo 之前的调用情况
        Scan preScan = scanDao.getScanByRepoId (repoId);
        if(startCommit == null && preScan == null){
            log.error (" repo id --> {} ,invoke tools error, cause startCommit is null and has not been scanned!", repoId);
            return ;
        }

        if(preScan != null){
            String preInvokeResult = preScan.getInvokeResult ();
            String[] toolInvokeResults = preInvokeResult.split (",");
            for(String toolInvokeResult : toolInvokeResults){
                String[] toolInvokeResultKeyAndValue = toolInvokeResult.split (":");
                String tool = toolInvokeResultKeyAndValue[0];
                String result = toolInvokeResultKeyAndValue[1];
                preToolInvokeMap.put (Integer.parseInt (tool),result);
            }
        }

        //第二步根据之前的调用结果，采取相应的调用方式
        Map<String, String> currentToolInvokeMap = new HashMap<> ();
        List<Tool> toolList = toolDao.getAllTools();
        for (Tool tool : toolList) {
            if (tool.getEnabled() == enabled) {
                String toolStartCommit = startCommit;
                String preResult = preToolInvokeMap.get (tool.getId ());
                if("0".equals (preResult) && startCommit == null){
                    toolStartCommit = preScan.getStartCommit ();
                }

                boolean status = restInvoker.invokeTools(tool.getToolType(), tool.getToolName(), repoId, branch, toolStartCommit);
                if (status) {
                    currentToolInvokeMap.put (String.valueOf (tool.getId ()),"1");
                    log.info("tool {} start scan", tool.getToolName());
                } else {
                    currentToolInvokeMap.put (String.valueOf (tool.getId ()),"0");
                    log.error("tool {} invoke failed", tool.getToolName());
                }
            }
        }

        //第三步 存储更新Scan表
        StringBuilder currentResultBuilder = new StringBuilder ();
        for(Map.Entry<String,String> entry : currentToolInvokeMap.entrySet ()){
            currentResultBuilder.append (entry.getKey () + ":" + entry.getValue () + ",");
        }
        String currentInvokeResult = currentResultBuilder.toString ();
        if(currentInvokeResult.length () > 0){
            currentInvokeResult = currentInvokeResult.substring (0,currentInvokeResult.length ()-1);
        }

        Scan currentScan = new Scan ();
        if(preScan != null ){
            currentScan.setUuid (preScan.getUuid ());
        }else{
            currentScan.setUuid (UUID.randomUUID ().toString ());
        }
        currentScan.setRepoId (repoId);
        currentScan.setInvokeResult (currentInvokeResult);
        currentScan.setStartCommit (startCommit);

        if(startCommit != null){
            if(preScan != null){
                scanDao.deleteScanByRepoId (repoId);
            }
            scanDao.insertOneScan (currentScan);
        }else{
            scanDao.updateOneScan (currentScan);
        }
    }

    @Autowired
    public void setToolDao(ToolDao toolDao) {
        this.toolDao = toolDao;
    }

    @Autowired
    public void setRestInvoker(RestInterfaceManager restInvoker) {
        this.restInvoker = restInvoker;
    }

    @Autowired
    public void setScanDao(ScanDao scanDao) {
        this.scanDao = scanDao;
    }
}