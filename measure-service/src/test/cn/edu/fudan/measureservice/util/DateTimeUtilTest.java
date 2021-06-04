package cn.edu.fudan.measureservice.util;

import cn.edu.fudan.measureservice.domain.enums.GranularityEnum;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DateTimeUtilTest {

    private final String interval0 = GranularityEnum.Day.getType();
    private final String interval1 = GranularityEnum.Week.getType();
    private final String interval2 = GranularityEnum.Month.getType();
    private final String interval3 = GranularityEnum.Year.getType();


    @Test
    public void initBeginTimeByInterval() {
        LocalDate localDate = LocalDate.of(2021,4,14);
        LocalDate beginTime1 = DateTimeUtil.initBeginTimeByInterval(localDate,interval1);
        LocalDate beginTime2 = DateTimeUtil.initBeginTimeByInterval(localDate,interval2);
        LocalDate beginTime3 = DateTimeUtil.initBeginTimeByInterval(localDate,interval3);

        Assert.assertEquals("按周初始化时间错误",beginTime1,LocalDate.of(2021,4,12));
        Assert.assertEquals("按月初始化时间错误",beginTime2,LocalDate.of(2021,4,1));
        Assert.assertEquals("按年初始化时间错误",beginTime3,LocalDate.of(2021,1,1));
    }

    @Test
    public void initEndTimeByInterval() {
        LocalDate localDate = LocalDate.of(2021,4,14);
        LocalDate endTime1 = DateTimeUtil.initEndTimeByInterval(localDate,interval1);
        LocalDate endTime2 = DateTimeUtil.initEndTimeByInterval(localDate,interval2);
        LocalDate endTime3 = DateTimeUtil.initEndTimeByInterval(localDate,interval3);

        Assert.assertEquals("按周初始化时间错误",endTime1,LocalDate.of(2021,4,18));
        Assert.assertEquals("按月初始化时间错误",endTime2,LocalDate.of(2021,4,30));
        Assert.assertEquals("按年初始化时间错误",endTime3,LocalDate.of(2021,12,31));
    }

    @Test
    public void selectTimeIncrementByInterval() {
        LocalDate localDate = LocalDate.of(2021,4,14);
        LocalDate afterTime1 = DateTimeUtil.selectTimeIncrementByInterval(localDate,interval0);
        LocalDate afterTime2 = DateTimeUtil.selectTimeIncrementByInterval(localDate,interval1);
        LocalDate afterTime3 = DateTimeUtil.selectTimeIncrementByInterval(localDate,interval2);
        LocalDate afterTime4 = DateTimeUtil.selectTimeIncrementByInterval(localDate,interval3);

        Assert.assertEquals("按天增量时间错误",afterTime1,LocalDate.of(2021,4,15));
        Assert.assertEquals("按周增量时间错误",afterTime2,LocalDate.of(2021,4,21));
        Assert.assertEquals("按月增量时间错误",afterTime3,LocalDate.of(2021,5,14));
        Assert.assertEquals("按年增量时间错误",afterTime4,LocalDate.of(2022,4,14));
    }

    /**
     *
     * Method: getSumDays(String date1, String date2)
     *
     */
    @Test
    public void testGetSumDays() throws Exception {
        String date1 = "2021-03-01";
        String date2 = "2021-01-01";
        int sumDays1 = (int) DateTimeUtil.getSumDays(date1,date2);
        Assert.assertEquals("两日期差值不对",sumDays1,60);
        try {
            DateTimeUtil.getSumDays(null,date2);
        }catch (Exception e) {
            e.getMessage();
            Assert.fail();
        }
    }
}