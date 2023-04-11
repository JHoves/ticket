package com.jhoves.ticket.member.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.jhoves.ticket.common.exception.BusinessException;
import com.jhoves.ticket.common.exception.BusinessExceptionEnum;
import com.jhoves.ticket.common.util.SnowUtil;
import com.jhoves.ticket.member.domain.Member;
import com.jhoves.ticket.member.domain.MemberExample;
import com.jhoves.ticket.member.mapper.MemberMapper;
import com.jhoves.ticket.member.req.MemberRegisterReq;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    @Resource
    private MemberMapper memberMapper;

    public int count(){
        return Math.toIntExact(memberMapper.countByExample(null));
    }

    //注册
    public long register(MemberRegisterReq req){
        String mobile = req.getMobile();
        //判断数据库中是否存在值
        MemberExample memberExample = new MemberExample();
        //构造条件
        memberExample.createCriteria().andMobileEqualTo(mobile);
        //数据库中查
        List<Member> list = memberMapper.selectByExample(memberExample);
        if(CollUtil.isNotEmpty(list)){
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_EXIST);
        }
        //如果不存在就注册
        Member member = new Member();
        //workedId：是机器编号，datacenterId：是数据中心的是机器编号
        member.setId(SnowUtil.getSnowflakeNextId());
        member.setMobile(mobile);
        memberMapper.insert(member);
        return member.getId();
    }
}
