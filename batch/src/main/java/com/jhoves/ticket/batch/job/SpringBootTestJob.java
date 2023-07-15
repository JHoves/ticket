package com.jhoves.ticket.batch.job;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * springboot自带的定时任务
 * 问题：1、适合单体项目，不适合集群 2、没法实时更改定时任务和策略
 * 解决：可以增加分布式锁，解决集群问题
 */

@Component
@EnableScheduling
public class SpringBootTestJob {

    //cron从左到右（用空格隔开）：秒 分 小时 月份中的日期 月份 星期中的日期 年份
    // 0/5 : 当前秒数除以5，余数是0，就触发

//    @Scheduled(cron = "0/5 * * * * ?")
//    private void test(){
//        System.out.println("SpringBootTestJob TEST");
//    }
}
