package cn.edu.fudan.dependservice.service;

import cn.edu.fudan.dependservice.domain.DependencyInfo;

import java.util.List;

public interface DependencyService {

    List<DependencyInfo> getDependencyNumWithDate(String beginDate, String endDate, String projectIds, String interval, String showDetail, String level);

    List<DependencyInfo> getDependencyNumIfHave(String beginDate, String endDate, String projectIds, String interval, String showDetail);

    List<DependencyInfo> getDependencyNum(String beginDate, String endDate, String projectIds, String interval, String showDetail, String level);

    }
