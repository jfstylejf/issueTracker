package cn.edu.fudan.dependservice.interceptor;

import cn.edu.fudan.dependservice.component.RestInterfaceManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author shaoxi
 * @version 0.1.0
 * @create 2021-04-28 19:33
 **/
@Slf4j
public class MyInterceptor  implements HandlerInterceptor {
    private RestInterfaceManager restInterfaceManager;

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) throws Exception {
        httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "POST,GET,OPTIONS,DELETE");
        String requestHeaders = httpServletRequest.getHeader("Access-Control-Request-Headers");
        if (requestHeaders != null) {
            requestHeaders = HtmlUtils.htmlEscape(requestHeaders, "UTF-8");
        }
        httpServletResponse.setHeader("Access-Control-Allow-Headers", requestHeaders);
        // 跨域时会首先发送一个option请求，该请求不会携带header 这里我们给option请求直接返回正常状态
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(HttpStatus.OK.value());
            return false;
        }
        String userToken = httpServletRequest.getHeader("token");
        if (userToken == null) {
            log.warn("need user token");
        }
        restInterfaceManager.userAuth(userToken);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }

}
