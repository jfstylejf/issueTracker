package cn.edu.fudan.dependservice.service;

import cn.edu.fudan.dependservice.domain.ScanStatus;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author shaoxi
 * @version 0.1.0
 * @create 2021-04-28 15:08
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class StatusServiceTest {
    @Autowired
    StatusService statusService;
    @Test
    public void getScanStatusTest(){
        ScanStatus scanStatus= statusService.getScanStatus("60930a84-4f50-11eb-b7c3-394c0d058805");
        System.out.println("status: "+scanStatus.getStatus());
         Assert.assertThat(scanStatus.getStatus(), Matchers.is("success"));
    }


}