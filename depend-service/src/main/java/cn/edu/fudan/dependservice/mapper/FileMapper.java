package cn.edu.fudan.dependservice.mapper;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileMapper {
    List<Integer> getFileinCircleNum(String repoUuid,String CommitId);
}
