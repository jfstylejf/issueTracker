package cn.edu.fudan.dependservice.mapper;

import cn.edu.fudan.dependservice.domain.ScanStatus;
import org.springframework.stereotype.Repository;


@Repository
public interface ScanMapper {
    void insert(String repo_uuid,String commit_id,String status,String end_time);
    ScanStatus getScanStatus(String repo_uuid);
}
