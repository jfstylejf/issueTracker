package cn.edu.fudan.measureservice.service.impl;

import cn.edu.fudan.measureservice.mapper.FileMeasureMapper;
import cn.edu.fudan.measureservice.service.MeasureDevHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * description:
 *
 * @author fancying
 * create: 2020-06-11 10:48
 **/
@Slf4j
@Service
public class MeasureDevHistoryServiceImpl implements MeasureDevHistoryService {

    private final FileMeasureMapper fileMeasureMapper;

    public MeasureDevHistoryServiceImpl(FileMeasureMapper fileMeasureMapper) {
        this.fileMeasureMapper = fileMeasureMapper;
    }

    @Override
    public Object getDevHistoryCommitInfo(String repoId, String beginDate, String endDate) {
        List<Map<String, Object>> result;
        result = fileMeasureMapper.getDevHistoryCommitInfo(repoId,beginDate,endDate);
        for (Map<String, Object> stringObjectMap : result) {
            Map<String, Object> map;
            map = stringObjectMap;
            //将数据库中timeStamp/dateTime类型转换成指定格式的字符串 map.get("commit_time") 这个就是数据库中dateTime类型
            String commit_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(map.get("commit_time"));
            map.put("commit_time", commit_time);
        }
        return result;
    }

    @Override
    public Object getDevHistoryFileInfo(String commitId) {
        List<Map<String, Object>> fileInfoList = fileMeasureMapper.getDevHistoryFileInfo(commitId);
        for (Map<String, Object> stringObjectMap : fileInfoList) {
            Map<String, Object> map;
            map = stringObjectMap;
            Integer currentCcn = (Integer) map.get("ccn");
            Integer diffCcn = (Integer) map.get("diff_ccn");
            map.put("lastCcn", currentCcn - diffCcn);
        }
        return fileInfoList;
    }
}