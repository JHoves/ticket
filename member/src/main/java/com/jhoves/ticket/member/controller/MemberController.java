package com.jhoves.ticket.member.controller;

import com.jhoves.ticket.common.resp.CommonResp;
import com.jhoves.ticket.member.req.MemberRegisterReq;
import com.jhoves.ticket.member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @GetMapping("/count")
    public CommonResp<Integer> count(){
        int count = memberService.count();
        CommonResp<Integer> commonResp = new CommonResp<>();
        commonResp.setContent(count);
        return commonResp;
    }

    //注册接口
    @PostMapping("/register")
    public CommonResp<Long> register(@Valid MemberRegisterReq req){
        long registerId = memberService.register(req);
        return new CommonResp<>(registerId);
    }
}
