package cn.edu.fudan.dependservice.mapper;

import org.springframework.stereotype.Repository;


@Repository
public interface ScanMapper {
    void insert(String repo_uuid,String commit_id,String status,String end_time);
}
