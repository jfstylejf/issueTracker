package cn.edu.fudan.dependservice.service;

import cn.edu.fudan.dependservice.domain.ScanRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ScanService {


    List<ScanRepo> scanAllRepoNearToOneDate(String toScanDate);
}

