//package com.jhoves.ticket.batch.config;
//
// import com.jhoves.ticket.batch.job.TestJob;
// import org.quartz.*;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// //自己手动声明的配置类（不经常使用）
// @Configuration
// public class QuartzConfig {
//
//     /**
//      * 声明一个任务
//      * @return
//      */
//     @Bean
//     public JobDetail jobDetail() {
//         return JobBuilder.newJob(TestJob.class)
//                 .withIdentity("TestJob", "test")
//                 .storeDurably()
//                 .build();
//     }
//
//     /**
//      * 声明一个触发器，什么时候触发这个任务
//      * @return
//      */
//     @Bean
//     public Trigger trigger() {
//         return TriggerBuilder.newTrigger()
//                 .forJob(jobDetail())
//                 .withIdentity("trigger", "trigger")
//                 .startNow()
//                 .withSchedule(CronScheduleBuilder.cronSchedule("*/2 * * * * ?"))
//                 .build();
//     }
// }
