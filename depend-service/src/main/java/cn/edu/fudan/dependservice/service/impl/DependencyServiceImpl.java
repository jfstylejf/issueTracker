package cn.edu.fudan.dependservice.service.impl;


import cn.edu.fudan.dependservice.constants.PublicConstants;
import cn.edu.fudan.dependservice.dao.StatisticsDao;
import cn.edu.fudan.dependservice.domain.DependencyInfo;
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

    private StatisticsDao statisticsDao;

    @Autowired
    public void setStatisticsDao(StatisticsDao statisticsDao) {
        this.statisticsDao=statisticsDao;
    }

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
//    todo  one sql
    public List<DependencyInfo> getDependencyNum(String beginDate, String endDate, String projectIds, String interval, String showDetail, String level) {
        List<DependencyInfo> numInfo = new ArrayList<>();
        String time1 = " 00:00:00";
        String time2 = " 24:00:00";
        // todo
        if (projectIds==null||projectIds.isEmpty()||projectIds.equals("\"\"")) {
            projectIds = statisticsDao.getAllProjectIds();
        }
        // to do  make projectid is ok for next sql
            if (projectIds.length() != 0) {
                String tempDateBegin = beginDate.split(" ")[0] + time1;
                String tempDateEnd;
                switch (interval) {
                    case "day":
                        tempDateEnd = beginDate.split(" ")[0] + time2;
                        while (tempDateBegin.compareTo(endDate) < 1) {
                            numInfo.addAll(statisticsDao.getDependencyNum2(tempDateEnd,projectIds, showDetail));
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
                            numInfo.addAll(statisticsDao.getDependencyNum2(tempDateEnd,projectIds, showDetail));

                            tempDateBegin = datePlus(tempDateEnd).split(" ")[0] + time1;
                        }
                        break;
                    case "year":
                        while (tempDateBegin.compareTo(endDate) < 1) {
                            tempDateEnd = tempDateBegin;
                            int year = Integer.parseInt(tempDateEnd.split(" ")[0].split("-")[0]);
                            tempDateEnd = lastDayOfMonth(year, 12) + time2;
                            numInfo.addAll(statisticsDao.getDependencyNum2(tempDateEnd,projectIds, showDetail));

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
                            numInfo.addAll(statisticsDao.getDependencyNum2(tempDateEnd,projectIds, showDetail));

                            tempDateBegin = datePlus(tempDateEnd).split(" ")[0] + time1;
                        }
                        break;
                }
            }



        return numInfo;

    }
    @Deprecated
    public List<DependencyInfo> getDependencyNumWithDate(String beginDate, String endDate, String projectIds, String interval, String showDetail, String level) {
        List<DependencyInfo> numInfo = new ArrayList<>();
        String time1 = " 00:00:00";
        String time2 = " 24:00:00";
        // todo
        if (projectIds==null||projectIds.isEmpty()||projectIds.equals("\"\"")) {
            projectIds = statisticsDao.getAllProjectIds();
        }
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
    public List<DependencyInfo> getDependencyNumIfHave(String beginDate, String endDate, String projectIds, String interval, String showDetail) {
        List<DependencyInfo> numInfo=new ArrayList<>();
        log.info("projectIds : "+projectIds);
        if (projectIds==null||projectIds.isEmpty()) {
            projectIds = statisticsDao.getAllProjectIds();
        }
        log.info("projectIds : "+projectIds);
        for (String projectId : projectIds.split(",")) {
            if (projectId.length() != 0) {
                numInfo.addAll(statisticsDao.getNumifHaveCommit(projectId));
            }
        }
        return numInfo;

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
