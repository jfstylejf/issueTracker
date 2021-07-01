package cn.edu.fudan.projectmanager.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description: service name
 *
 * @author Richy
 * create: 2021-04-09 16:28
 **/
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
public class ServicesManager {

    public enum Services {

        /**
         * 八个服务的删除状态
         */
        RECYCLED, JIRA, DEPENDENCY, CLONE, MEASURE, CODETRACKER, ISSUE, SCAN, REPOSITORY;

//        /**
//         第一位 jira服务
//         */
//        JIRA("jiraService",0),
//        /**
//         第二位 dependency服务
//         */
//        DEPENDENCY("dependencyService",0),
//        /**
//         第三位 clone服务
//         */
//        CLONE("cloneService",0),
//        /**
//         第四位 measure服务
//         */
//        MEASURE("measureService",0),
//        /**
//         第五位 codeTracker服务
//         */
//        CODETRACKER("codeTrackerService",0),
//        /**
//         第六位 issue服务
//         */
//        ISSUE("issueService", 0),
//        /**
//         第七位 issue服务
//         */
//        SCAN("scanService", 0),
//        /**
//         第八位 issue服务
//         */
//        RECYCLED("recycled", 0);
//
//        private String serviceName;
//       private int status;
    }

    
}