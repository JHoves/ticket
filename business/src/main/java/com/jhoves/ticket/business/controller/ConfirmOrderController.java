package com.jhoves.ticket.business.controller;

import com.jhoves.ticket.business.req.ConfirmOrderDoReq;
import com.jhoves.ticket.business.req.ConfirmOrderQueryReq;
import com.jhoves.ticket.business.resp.ConfirmOrderQueryResp;
import com.jhoves.ticket.business.service.ConfirmOrderService;
import com.jhoves.ticket.common.resp.CommonResp;
import com.jhoves.ticket.common.resp.PageResp;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/confirm-order")
public class ConfirmOrderController {

    @Resource
    private ConfirmOrderService confirmOrderService;

    @PostMapping("/do")
    public CommonResp<Object> doConfirm(@Valid @RequestBody ConfirmOrderDoReq req) {
        confirmOrderService.doConfirm(req);
        return new CommonResp<>();
    }

}
