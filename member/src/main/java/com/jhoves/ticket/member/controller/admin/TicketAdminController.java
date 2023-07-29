package com.jhoves.ticket.member.controller.admin;

import com.jhoves.ticket.common.resp.CommonResp;
import com.jhoves.ticket.common.resp.PageResp;
import com.jhoves.ticket.member.req.TicketQueryReq;
import com.jhoves.ticket.member.req.TicketSaveReq;
import com.jhoves.ticket.member.resp.TicketQueryResp;
import com.jhoves.ticket.member.service.TicketService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/ticket")
public class TicketAdminController {

    @Resource
    private TicketService ticketService;

    @GetMapping("/query-list")
    public CommonResp<PageResp<TicketQueryResp>> queryList(@Valid TicketQueryReq req) {
        PageResp<TicketQueryResp> list = ticketService.queryList(req);
        return new CommonResp<>(list);
    }


}
