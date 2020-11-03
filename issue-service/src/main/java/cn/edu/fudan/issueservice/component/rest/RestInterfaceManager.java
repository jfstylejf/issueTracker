package cn.edu.fudan.issueservice.component.rest;

import cn.edu.fudan.issueservice.domain.enums.RestServiceEnum;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;

/**
 * @author WZY
 * @version 1.0
 **/
public class RestInterfaceManager {

    private RestTemplate restTemplate;
    
    private RestAccountService accountService;
    private AbstractRestService abstractRestService;

    public RestInterfaceManager(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public JSONObject rest(RestServiceEnum serviceName, String methodName, String arg1) throws Exception{
        getService(serviceName);
        Method method = abstractRestService.getClass().getMethod(methodName, arg1.getClass());
        return (JSONObject) method.invoke(abstractRestService, arg1);
    }

    public JSONObject rest(RestServiceEnum serviceName, String methodName, String arg1, String arg2) throws Exception{
        getService(serviceName);
        Method method = abstractRestService.getClass().getMethod(methodName, arg1.getClass(), arg2.getClass());
        return (JSONObject) method.invoke(abstractRestService, arg1, arg2);
    }

    public JSONObject rest(RestServiceEnum serviceName, String methodName, String arg1, Integer arg2) throws Exception{
        getService(serviceName);
        Method method = abstractRestService.getClass().getMethod(methodName, arg1.getClass(), arg2.getClass());
        return (JSONObject) method.invoke(abstractRestService, arg1, arg2);
    }




    private void getService(RestServiceEnum serviceName) {
        switch (serviceName) {
            case ACCOUNT_SERVICE:
                abstractRestService = accountService;
                break;
            case PROJECT_SERVICE:
                break;
            default:

        }
    }


    @Autowired
    public void setAccountService(RestAccountService accountService) {
        this.accountService = accountService;
    }
}
