package cn.edu.fudan.measureservice.mapper;

import cn.edu.fudan.measureservice.domain.core.MeasureScan;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * description:
 *
 * @author fancying
 * create: 2020-06-10 23:43
 **/
@Repository
public interface MeasureScanMapper {

    void insertOneMeasureScan(MeasureScan measureScan);

    void updateMeasureScan(MeasureScan measureScan);

    List<Map<String, Object>> getScanStatus(@Param("repo_id") String repoUuid);



}