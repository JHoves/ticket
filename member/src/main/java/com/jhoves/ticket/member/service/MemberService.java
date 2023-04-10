package com.jhoves.ticket.member.service;

import com.jhoves.ticket.member.mapper.MemberMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    @Resource
    private MemberMapper memberMapper;

    public int count(){
        return memberMapper.count();
    }
}
