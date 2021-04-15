package cn.edu.fudan.dependservice.service;

import cn.edu.fudan.dependservice.domain.DependencyInfo;
import cn.edu.fudan.dependservice.domain.MethodOrFileNumInfo;
import cn.edu.fudan.dependservice.domain.RelationshipView;

import java.util.List;

public interface DependencyService {

    List<DependencyInfo> getDependencyNumWithDate(String beginDate, String endDate, String projectIds, String interval, String showDetail, String level);

    List<DependencyInfo> getDependencyNumIfHave(String beginDate, String endDate, String projectIds, String interval, String showDetail);


}
