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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.acl.LastOwnerException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author fancying
 */
@Slf4j
@Service
public class ScanInfoServiceImpl implements ScanInfoService {

    private RestInterfaceManager restInterfaceManager;
    private ScanDao scanDao;
    private ToolDao toolDao;


    @Override
    public Object getAllScanStatus(String repoId) {
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

                if(((Integer)Scan.INVOKE_FAILED).equals(toolInvokeResult)){
                    scanStatus.setStatus (ScanStatusEnum.INVOKE_FAILED.getType ());
                    overAllStatus = ScanStatusEnum.INVOKE_FAILED.getType ();
                    continue;
                }
                //3. 调用成功 获取各个服务的扫描状态
                log.debug("start get toolStatus");
                JSONObject toolStatusJson = restInterfaceManager.getToolsScanStatus(tool.getToolType (), tool.getToolName (), repoId);
                if (toolStatusJson == null) {
                    log.debug("toolStatus is null! set default status");
                    scanStatus.setStatus (ScanStatusEnum.WAITING_FOR_SCAN.getType ());
                    overAllStatus = judgeOverStatus(overAllStatus, ScanStatusEnum.WAITING_FOR_SCAN);
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
