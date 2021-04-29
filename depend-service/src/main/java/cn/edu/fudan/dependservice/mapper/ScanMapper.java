package cn.edu.fudan.dependservice.mapper;

import cn.edu.fudan.dependservice.domain.ScanRepo;
import cn.edu.fudan.dependservice.domain.ScanStatus;
import org.springframework.stereotype.Repository;


@Repository
public interface ScanMapper {
    int  insert(ScanRepo scanRepo);
    ScanStatus getScanStatus(String repo_uuid);
}
