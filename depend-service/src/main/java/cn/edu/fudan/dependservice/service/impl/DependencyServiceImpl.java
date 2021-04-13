package cn.edu.fudan.dependservice.service.impl;

import cn.edu.fudan.common.component.BaseRepoRestManager;
import cn.edu.fudan.common.domain.po.scan.RepoScan;
import cn.edu.fudan.common.scan.CommonScanProcess;
import cn.edu.fudan.common.scan.ToolScan;
import cn.edu.fudan.dependservice.constants.PublicConstants;
import cn.edu.fudan.dependservice.dao.StatisticsDao;
import cn.edu.fudan.dependservice.domain.DependencyInfo;
import cn.edu.fudan.dependservice.domain.MethodOrFileNumInfo;
import cn.edu.fudan.dependservice.domain.RelationshipView;
import cn.edu.fudan.dependservice.domain.RepoRestManager;
import cn.edu.fudan.dependservice.mapper.FileMapper;
import cn.edu.fudan.dependservice.mapper.GroupMapper;
import cn.edu.fudan.dependservice.service.DependencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * description: 依赖分析服务接口
 *
 * @author shaoxi
 * create: 2021-03-02 21:04
 **/
@Slf4j
@Service
public class DependencyServiceImpl implements DependencyService {
    ApplicationContext applicationContext;
    public static  String TIME_FORMAT = "yyyy-MM-dd";


    @Autowired
    GroupMapper groupMapper;

    @Autowired
    FileMapper fileMapper;

    private StatisticsDao statisticsDao;

    @Autowired
    public void setStatisticsDao(StatisticsDao statisticsDao) {
        this.statisticsDao=statisticsDao;
    }




    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    @Override
    public List<DependencyInfo>  getDependencyNum(String beginDate, String endDate, String projectIds, String interval, String showDetail, String level){
        List<DependencyInfo> res = new ArrayList<>();

        if (projectIds==null||projectIds.isEmpty()) {
            projectIds = statisticsDao.getAllProjectIds();
        }
        for (String projectId : projectIds.split(",")) {
            if (projectId.length() != 0) {
                //
                res.add(statisticsDao.getDependencyNum(null,null,projectId,showDetail));

            }
        }
        return res ;

    }


    public List<DependencyInfo> getDependencyNumWithDate(String beginDate, String endDate, String projectIds, String interval, String showDetail, String level) {
        List<DependencyInfo> numInfo = new ArrayList<>();
        String time1 = " 00:00:00";
        String time2 = " 24:00:00";
        if (projectIds==null||projectIds.isEmpty()) {
            projectIds = statisticsDao.getAllProjectIds();
        }
        log.info("projectIds : "+projectIds);
        for (String projectId : projectIds.split(",")) {
            if (projectId.length() != 0) {
                String tempDateBegin = beginDate.split(" ")[0] + time1;
                String tempDateEnd;
                switch (interval) {
                    case "day":
                        tempDateEnd = beginDate.split(" ")[0] + time2;
                        while (tempDateBegin.compareTo(endDate) < 1) {
                            numInfo.add(statisticsDao.getDependencyNum( tempDateBegin, tempDateEnd,projectId, showDetail));
                            tempDateBegin = datePlus(tempDateBegin.split(" ")[0]) + time1;
                            tempDateEnd = tempDateBegin.split(" ")[0] + time2;
                        }
                        break;
                    case "month":
                        while (tempDateBegin.compareTo(endDate) < 1) {
                            tempDateEnd = tempDateBegin;
                            int year = Integer.parseInt(tempDateEnd.split(" ")[0].split("-")[0]);
                            int month = Integer.parseInt(tempDateEnd.split(" ")[0].split("-")[1]);
                            tempDateEnd = lastDayOfMonth(year, month) + time2;
                            numInfo.add(statisticsDao.getDependencyNum(tempDateBegin, tempDateEnd,projectId, showDetail));

                            tempDateBegin = datePlus(tempDateEnd).split(" ")[0] + time1;
                        }
                        break;
                    case "year":
                        while (tempDateBegin.compareTo(endDate) < 1) {
                            tempDateEnd = tempDateBegin;
                            int year = Integer.parseInt(tempDateEnd.split(" ")[0].split("-")[0]);
                            tempDateEnd = lastDayOfMonth(year, 12) + time2;
                            numInfo.add(statisticsDao.getDependencyNum(tempDateBegin, tempDateEnd,projectId, showDetail));

                            tempDateBegin = datePlus(tempDateEnd).split(" ")[0] + time1;
                        }
                        break;
                    default:
                        while (tempDateBegin.compareTo(endDate) < 1) {
                            tempDateEnd = tempDateBegin;
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat(PublicConstants.TIME_FORMAT);
                                Calendar cal = Calendar.getInstance();
                                Date time = sdf.parse(tempDateEnd.split(" ")[0]);
                                cal.setTime(time);
                                int dayWeek = cal.get(Calendar.DAY_OF_WEEK);
                                if (1 == dayWeek) {
                                    cal.add(Calendar.DAY_OF_MONTH, -1);
                                }
                                cal.setFirstDayOfWeek(Calendar.MONDAY);
                                int day = cal.get(Calendar.DAY_OF_WEEK);
                                cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - day);
                                cal.add(Calendar.DATE, 6);
                                tempDateEnd = sdf.format(cal.getTime()) + time2;
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            numInfo.add(statisticsDao.getDependencyNum(tempDateBegin, tempDateEnd,projectId, showDetail));

                            tempDateBegin = datePlus(tempDateEnd).split(" ")[0] + time1;
                        }
                        break;
                }
            }
        }



        return numInfo;

    }

    @Override
    public List<RelationshipView> getRe(String ps, String page, String asc, String order) {

        return null;
    }

    public String datePlus(String tempDate) {
        Date date = null;
        try {
            date = (new SimpleDateFormat(TIME_FORMAT)).parse(tempDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, 1);
        date = cal.getTime();
        tempDate = (new SimpleDateFormat(TIME_FORMAT)).format(date);
        return tempDate;
    }
    public String lastDayOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
        return sdf.format(cal.getTime());
    }
}
