package cn.edu.fudan.scanservice.service.impl;

import cn.edu.fudan.scanservice.component.rest.RestInterfaceManager;
import cn.edu.fudan.scanservice.dao.ScanDao;
import cn.edu.fudan.scanservice.dao.ToolDao;
import cn.edu.fudan.scanservice.domain.ToolStatus;
import cn.edu.fudan.scanservice.domain.dbo.Scan;
import cn.edu.fudan.scanservice.domain.dbo.Tool;
import cn.edu.fudan.scanservice.domain.dto.ScanStatus;
import cn.edu.fudan.scanservice.domain.enums.ScanStatusEnum;
import cn.edu.fudan.scanservice.service.ScanInfoService;
import cn.edu.fudan.scanservice.util.DateTimeUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.acl.LastOwnerException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author fancying
 */
@Slf4j
@Service
public class ScanInfoServiceImpl implements ScanInfoService {

    private RestInterfaceManager restInterfaceManager;
    private ScanDao scanDao;
    private ToolDao toolDao;
    public static final int INVOKE_SUCCESS = 1;
    public static final int INVOKE_FAILED = 0;


    @Override
    public Object getAllScanStatus(String repoId) {
        log.info("getAllScanStatus : repoId is " + repoId);
        String overAllStatus = ScanStatusEnum.COMPLETE.getType () ;
        List<ScanStatus> toolScanStatuses = new ArrayList<> ();
        Scan scan = scanDao.getScanByRepoId (repoId) ;

        LocalDateTime startScanDateTime = LocalDateTime.now();
        LocalDateTime endScanDateTime = LocalDateTime.MIN;
        int scanTime = 0;
        // 1.验证是否已经分配工具扫描任务
        if(scan == null){
            overAllStatus = ScanStatusEnum.NOT_SCANNED.getType ();
            log.warn("repo [{}] is not scanned", repoId);
        } else {
            List<Tool> toolList = toolDao.getAllTools();
            Map<Integer,Integer> toolInvokeMap = scan.analyzeInvokeResult ();

            for(Tool tool : toolList){
                if (tool.getEnabled() != Tool.ENABLED) {
                    continue;
                }
                // 2. 验证是否有工具调用失败，或者未调用
                Integer toolInvokeResult = toolInvokeMap.get (tool.getId ());
                ScanStatus scanStatus = new ScanStatus ();
                scanStatus.setToolName (tool.getToolName ());
                toolScanStatuses.add (scanStatus);

                // 工具在上次扫描中调用失败或者当时没有调用这个工具
                if(((Integer)Scan.INVOKE_FAILED).equals(toolInvokeResult) || toolInvokeResult == null){
                    scanStatus.setStatus (ScanStatusEnum.INVOKE_FAILED.getType ());
                    overAllStatus = ScanStatusEnum.INVOKE_FAILED.getType ();
                    // 再次调用该工具进行扫描
                    reScan(scan, tool);
                    continue;
                }
                //3. 调用成功 获取各个服务的扫描状态
                log.debug("start get toolStatus");
                JSONObject toolStatusJson = restInterfaceManager.getToolsScanStatus(tool.getToolType (), tool.getToolName (), repoId);
                if (toolStatusJson == null) {
                    log.debug("toolStatus is null! set default status");
                    scanStatus.setStatus (ScanStatusEnum.WAITING_FOR_SCAN.getType ());
                    overAllStatus = judgeOverStatus(overAllStatus, ScanStatusEnum.WAITING_FOR_SCAN);
                    // 再次调用该工具进行扫描
//                    reScan(scan, tool);
                    continue;
                }

                ToolStatus toolStatus = new ToolStatus(toolStatusJson);
                //更新状态
                String toolScanStatus = toolStatus.getStatus();
                overAllStatus = judgeOverStatus(overAllStatus, ScanStatusEnum.getScanStatusEnum(toolScanStatus));
                scanStatus.setStatus (toolScanStatus);

                //更新起始时间
                String toolStartScanTime = toolStatus.getStartScanTime();
                if (! StringUtils.isEmpty(toolStartScanTime)) {
                    LocalDateTime toolStartScanDateTime = DateTimeUtil.stringToLocalDate(toolStartScanTime);
                    if(toolStartScanDateTime.isBefore (startScanDateTime)){
                        startScanDateTime = toolStartScanDateTime;
                    }
                }
                scanStatus.setStartScanTime (toolStartScanTime);

                //更新终止时间
                String toolEndScanTime = toolStatus.getEndScanTime();
                if(! StringUtils.isEmpty(toolEndScanTime)){
                    LocalDateTime toolEndScanDateTime = DateTimeUtil.stringToLocalDate(toolEndScanTime);
                    if(toolEndScanDateTime.isAfter (endScanDateTime)){
                        endScanDateTime = toolEndScanDateTime;
                    }
                }

                scanStatus.setEndScanTime (toolEndScanTime);

                //更新扫描用时
                int toolScanTime = toolStatus.getScanTime();
                if(scanTime < toolScanTime){
                    scanTime = toolScanTime;
                }
                scanStatus.setScanTime (toolScanTime);
                scanStatus.updateElapsedTime ();
            }
        }

        ScanStatus overallScanStatus = new ScanStatus ();
        overallScanStatus.setStartScanTime (DateTimeUtil.localDateTimeToString (startScanDateTime));
        if (endScanDateTime.equals(LocalDateTime.MIN)) {
            endScanDateTime = LocalDateTime.now();
        }
        overallScanStatus.setEndScanTime (DateTimeUtil.localDateTimeToString (endScanDateTime));

        if(overAllStatus == null ){
            overAllStatus = ScanStatusEnum.COMPLETE.getType ();
        }
        overallScanStatus.setStatus (overAllStatus);
        overallScanStatus.setScanTime (scanTime);
        overallScanStatus.updateElapsedTime ();

        JSONObject jsonObject = new JSONObject ();


        jsonObject.put ("overall_status", overallScanStatus);
        jsonObject.put ("tool_status", toolScanStatuses);

        return jsonObject;
    }

    private String judgeOverStatus(String overAllStatus, ScanStatusEnum targetValue) {
        ScanStatusEnum curStatus = ScanStatusEnum.getScanStatusEnum(overAllStatus);
        return curStatus.getPriority() > targetValue.getPriority() ? targetValue.getType() : curStatus.getType();
    }

    /**
     * 此方法用于在获取扫描状态时，发现有工具在之前调用失败的情况下，再次对该工具进行扫描请求
     * @param scan 上次scan表里的记录
     * @param tool 工具类型
     */
    private void reScan(Scan scan, Tool tool){
        String repoId = scan.getRepoId();
        String beginCommit = scan.getStartCommit();
        Map<Integer,Integer> toolInvokeMap = scan.analyzeInvokeResult ();

        JSONObject projectInfo = restInterfaceManager.getProjectsOfRepo(repoId);
        String branch = projectInfo.getJSONObject("data").getString("branch");
        boolean status = restInterfaceManager.invokeTools(tool.getToolType(), tool.getToolName(), repoId, branch, beginCommit);
        // 更新工具调用结果的状态
        if (status) {
            toolInvokeMap.put (tool.getId (),INVOKE_SUCCESS);
            log.info("tool {} rescan success, repoUuid is [{}], beginCommit is [{}]", tool.getToolName(), repoId, beginCommit);
        } else {
            toolInvokeMap.put (tool.getId (),INVOKE_FAILED);
            log.error("tool {} rescan failed, repoUuid is [{}], beginCommit is [{}]", tool.getToolName(), repoId, beginCommit);
        }
        StringBuilder invokeResult = new StringBuilder();
        for (Map.Entry<Integer,Integer> entry : toolInvokeMap.entrySet()){
            invokeResult.append(entry.getKey().toString()).append(":").append(entry.getValue().toString()).append(",");
        }
        String result = invokeResult.toString();
        if(result.length () > 0){
            result = result.substring (0,result.length ()-1);
        }
        scan.setInvokeResult(result);

        // 更新scan表该记录
        scanDao.updateOneScan(scan);

    }

    @Override
    @Async("taskExecutor")
    public void deleteOneRepo(String repoId, String token) throws InterruptedException {
        scanDao.deleteScanByRepoId(repoId);
        for (int i = 0; i < 30; i++) {
            if (scanDao.checkDeleteSuccessful(repoId)) {
                if(restInterfaceManager.deleteRecall(repoId, token)){
                    return;
                }
            }
            TimeUnit.SECONDS.sleep(1);
        }
    }

    @Autowired
    public void setScanDao(ScanDao scanDao) {
        this.scanDao = scanDao;
    }

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }

    @Autowired
    public void setToolDao(ToolDao toolDao) {
        this.toolDao = toolDao;
    }


}
