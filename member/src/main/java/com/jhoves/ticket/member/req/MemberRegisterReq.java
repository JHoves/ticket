package com.jhoves.ticket.member.req;

import jakarta.validation.constraints.NotBlank;

//member register的请求参数
public class MemberRegisterReq {

    @NotBlank(message = "手机号不能为空")
    private String mobile;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
