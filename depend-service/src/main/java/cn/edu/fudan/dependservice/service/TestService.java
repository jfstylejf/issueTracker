package cn.edu.fudan.dependservice.service;

import cn.edu.fudan.dependservice.domain.ScanRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TestService {


//    List<String> getAllRepoUuidThatNeedScan();
    List<ScanRepo> scanAllRepo();
    List<ScanRepo> scanAllRepoNearToOneDate(String toScanDate);
    List<ScanRepo> scanAllRepoNew();
}

