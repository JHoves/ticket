package com.jhoves.ticket.member.controller;

import com.jhoves.ticket.common.resp.CommonResp;
import com.jhoves.ticket.member.req.MemberLoginReq;
import com.jhoves.ticket.member.req.MemberRegisterReq;
import com.jhoves.ticket.member.req.MemberSendCodeReq;
import com.jhoves.ticket.member.resp.MemberLoginResp;
import com.jhoves.ticket.member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.awt.*;

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

    //发送验证码接口
    @PostMapping("/send-code")
    public CommonResp<String> sendCode(@Valid @RequestBody MemberSendCodeReq req){
        memberService.sendCode(req);
        return new CommonResp<>();
    }

    //登录接口
    @PostMapping("/login")
    public CommonResp<MemberLoginResp> login(@Valid MemberLoginReq req) {
        MemberLoginResp resp = memberService.login(req);
        return new CommonResp<>(resp);
    }
}
