package cn.edu.fudan.dependservice.remote;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.client.RestTemplate;

/**
 * @author shaoxi
 * @version 0.1.0
 * @create 2021-04-29 10:55
 **/
public class ScanTest {
    String url ="http://10.176.34.85:8999/depend/scanAllByDate";

    //todo write test
    public void postApi() {
        //目标接口地址
        //请求参数JOSN类型
        JSONObject postData = new JSONObject();
        postData.put("datetime", "2021-05-01 00:00");
        RestTemplate client = new RestTemplate();

        JSONObject json = client.postForEntity(url, postData, JSONObject.class).getBody();
        System.out.println(json.toString());
    }
//    @Test
    public void test(){

    }

}
