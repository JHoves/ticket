package com.jhoves.ticket.business.controller;

import cn.hutool.core.util.RandomUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public String hello() throws InterruptedException {
        //Thread.sleep(500);   //测试熔断的慢调用机制
        int i = RandomUtil.randomInt(1, 10);
        if (i <= 2) {
            throw new RuntimeException("测试异常");
        }
        return "test business";
    }
}
