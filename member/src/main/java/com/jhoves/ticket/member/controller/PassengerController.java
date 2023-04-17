package com.jhoves.ticket.member.controller;

import com.jhoves.ticket.common.resp.CommonResp;
import com.jhoves.ticket.member.req.PassengerSaveReq;
import com.jhoves.ticket.member.service.PassengerService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/passenger")
public class PassengerController {

    @Resource
    private PassengerService passengerService;

    @PostMapping("/save")
    public CommonResp<Object> save(@Valid @RequestBody PassengerSaveReq req) {
        passengerService.save(req);
        return new CommonResp<>();
    }


}
