package com.jhoves.ticket.business.config;

import com.alibaba.csp.sentinel.slots.block.Rule;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@ComponentScan("com.jhoves")
@MapperScan("com.jhoves.ticket.*.mapper")
@EnableFeignClients("com.jhoves.ticket.business.feign")
@EnableCaching
public class BusinessApplication {
    //增加日志
    private static final Logger LOG = LoggerFactory.getLogger(BusinessApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BusinessApplication.class);
        ConfigurableEnvironment env = app.run(args).getEnvironment();
        LOG.info("启动成功！！");
        LOG.info("地址：\thttp://127.0.0.1:{}",env.getProperty("server.port"));

        //限流规则
        //直接控制进行设置规则了，所以不用了
//        initFlowRules();
//        LOG.info("限流规则");
    }

    public static void initFlowRules(){
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule();
        rule.setResource("doConfirm");
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        //set limit qps to 1
        rule.setCount(20);
        rules.add(rule);
        FlowRuleManager.loadRules(rules);

    }

}
