package cn.edu.fudan.issueservice.controller;

import cn.edu.fudan.issueservice.IssueServiceApplicationTest;
import cn.edu.fudan.issueservice.domain.ResponseBean;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author beethoven
 * @date 2021-05-24 10:11:02
 */
public class IssueMeasureControllerTest {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String HTTP = "http://";
    private final String IP = "127.0.0.1";
    private final String PORT = "8005";
    HttpEntity<HttpHeaders> request;

    @Before
    public void init() throws UnknownHostException {
        if (!checkPortIsRunning()) {
            System.out.println("your code is not running, can not send http request, please run your code.\n");
            System.exit(1);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("token", "ec15d79e36e14dd258cfff3d48b73d35");
        request = new HttpEntity<>(headers);
    }

    private boolean checkPortIsRunning() throws UnknownHostException {
        boolean flag = false;
        InetAddress Address = InetAddress.getByName("127.0.0.1");
        try (Socket ignored = new Socket(Address, 8005)) {
            flag = true;
        } catch (IOException ignored) {

        }
        return flag;
    }

    @Test
    public void developerCodeQualityTest() {
        String url = "/codewisdom/issue/developer/code-quality?all=false&developers=Guicheng Wang,heyue,zhangjingfu,zhiwuzhu,Zrq-Q&repo_uuids=a140dc46-50db-11eb-b7c3-394c0d058805&since=2021-05-18&until=2021-05-24";
        ResponseEntity<ResponseBean> response = restTemplate.exchange(HTTP + IP + ":" + PORT + url, HttpMethod.GET, request, ResponseBean.class);
        Assert.assertNotNull(response.getBody());
        Assert.assertEquals(200, response.getBody().getCode());
    }
}
