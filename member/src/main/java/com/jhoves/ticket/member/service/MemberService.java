package com.jhoves.ticket.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.jhoves.ticket.common.exception.BusinessException;
import com.jhoves.ticket.common.exception.BusinessExceptionEnum;
import com.jhoves.ticket.common.util.JwtUtil;
import com.jhoves.ticket.common.util.SnowUtil;
import com.jhoves.ticket.member.domain.Member;
import com.jhoves.ticket.member.domain.MemberExample;
import com.jhoves.ticket.member.mapper.MemberMapper;
import com.jhoves.ticket.member.req.MemberLoginReq;
import com.jhoves.ticket.member.req.MemberRegisterReq;
import com.jhoves.ticket.member.req.MemberSendCodeReq;
import com.jhoves.ticket.member.resp.MemberLoginResp;
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

    //发送验证码
    public void sendCode(MemberSendCodeReq req){
        String mobile = req.getMobile();
        //判断数据库中是否存在值
        MemberExample memberExample = new MemberExample();
        //构造条件
        memberExample.createCriteria().andMobileEqualTo(mobile);
        //数据库中查
        List<Member> list = memberMapper.selectByExample(memberExample);

        //如果数据库中为空，即手机号不存在，插入到数据库中
        if(CollUtil.isEmpty(list)){
            //如果不存在就注册
            Member member = new Member();
            //workedId：是机器编号，datacenterId：是数据中心的是机器编号
            member.setId(SnowUtil.getSnowflakeNextId());
            member.setMobile(mobile);
            memberMapper.insert(member);
        }
        //生成验证码
        //String code = RandomUtil.randomString(4);
        String code = "8888";
        //对接短信平台
        //保存短信到redis中并且设置过期时间

    }

    public MemberLoginResp login(MemberLoginReq req) {
        String mobile = req.getMobile();
        String code = req.getCode();
        Member memberDB = selectByMobile(mobile);

        // 如果手机号不存在，则插入一条记录
        if (ObjectUtil.isNull(memberDB)) {
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_NOT_EXIST);
        }

        // 校验短信验证码
        if (!"8888".equals(code)) {
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_CODE_ERROR);
        }

        MemberLoginResp memberLoginResp = BeanUtil.copyProperties(memberDB, MemberLoginResp.class);
        //使用JWT实现单点登录
        String token = JwtUtil.createToken(memberLoginResp.getId(),memberLoginResp.getMobile());
        memberLoginResp.setToken(token);
        return memberLoginResp;

    }

    private Member selectByMobile(String mobile) {
        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andMobileEqualTo(mobile);
        List<Member> list = memberMapper.selectByExample(memberExample);
        if (CollUtil.isEmpty(list)) {
            return null;
        } else {
            return list.get(0);
        }
    }
}
