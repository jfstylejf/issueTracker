package cn.edu.fudan.dependservice.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TestService {


    List<String> getAllRepoUuidThatNeedScan();
    void scanAllRepo();
}

