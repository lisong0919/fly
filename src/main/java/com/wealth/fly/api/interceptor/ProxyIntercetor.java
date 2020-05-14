package com.wealth.fly.api.interceptor;

import com.wealth.fly.common.HttpClientUtil;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.HandlerInterceptor;

@ControllerAdvice
@Component
public class ProxyIntercetor implements HandlerInterceptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProxyIntercetor.class);
  private static final String OKEX_API_PREFIX = "/api-ok/";
  private static final String OKEX_HOST = "https://www.okex.com/";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    if (request.getRequestURI().startsWith(OKEX_API_PREFIX)) {
      String okUri = request.getRequestURI().substring(OKEX_API_PREFIX.length());

      String okResponse = null;

      if (request.getMethod().toLowerCase().equals("get")) {


        String paramStr="";
        Map<String,String> requestParam=getRequestParam(request);
        for(String paramName:requestParam.keySet()){
          paramStr=paramStr+"&"+paramName+"="+requestParam.get(paramName);
        }

        okResponse = HttpClientUtil.get(OKEX_HOST + okUri+"?"+paramStr.substring(1));

      }

      response.getWriter().print(okResponse);
      response.getWriter().flush();
      return false;
    }

    return true;
  }

  private Map<String,String> getRequestParam(HttpServletRequest request){
    Map<String,String[]> params=request.getParameterMap();

    if(params==null){
      return Collections.emptyMap();
    }

    Map<String,String> result=new LinkedHashMap<>();
    for(String paramName:params.keySet()){
      result.put(paramName,params.get(paramName)[0]);
    }

    return result;
  }

}
