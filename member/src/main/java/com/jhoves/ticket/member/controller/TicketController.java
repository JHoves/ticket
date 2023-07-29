package com.jhoves.ticket.member.controller;

import com.jhoves.ticket.common.context.LoginMemberContext;
import com.jhoves.ticket.common.resp.CommonResp;
import com.jhoves.ticket.common.resp.PageResp;
import com.jhoves.ticket.member.req.TicketQueryReq;
import com.jhoves.ticket.member.resp.TicketQueryResp;
import com.jhoves.ticket.member.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ticket")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @GetMapping("/query-list")
    public CommonResp<PageResp<TicketQueryResp>> query(@Valid TicketQueryReq req) {
        CommonResp<PageResp<TicketQueryResp>> commonResp = new CommonResp<>();
        req.setMemberId(LoginMemberContext.getId());
        PageResp<TicketQueryResp> pageResp = ticketService.queryList(req);
        commonResp.setContent(pageResp);
        return commonResp;
    }

}
