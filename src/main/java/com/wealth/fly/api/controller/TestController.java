package com.wealth.fly.api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

  @RequestMapping("/test")
  public Object proxyOkex(){

    return "test ok...";
  }

}
