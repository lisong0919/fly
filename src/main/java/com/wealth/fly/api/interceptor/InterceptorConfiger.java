package com.wealth.fly.api.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfiger implements WebMvcConfigurer {

  @Autowired
  private ProxyIntercetor proxyIntercetor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(proxyIntercetor);
  }

}
