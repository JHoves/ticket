package com.jhoves.ticket.business.controller.admin;

import com.jhoves.ticket.common.context.LoginMemberContext;
import com.jhoves.ticket.common.resp.CommonResp;
import com.jhoves.ticket.common.resp.PageResp;
import com.jhoves.ticket.business.req.DailyTrainTicketQueryReq;
import com.jhoves.ticket.business.req.DailyTrainTicketSaveReq;
import com.jhoves.ticket.business.resp.DailyTrainTicketQueryResp;
import com.jhoves.ticket.business.service.DailyTrainTicketService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/daily-train-ticket")
public class DailyTrainTicketAdminController {

    @Resource
    private DailyTrainTicketService dailyTrainTicketService;

    @PostMapping("/save")
    public CommonResp<Object> save(@Valid @RequestBody DailyTrainTicketSaveReq req) {
        dailyTrainTicketService.save(req);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<PageResp<DailyTrainTicketQueryResp>> queryList(@Valid DailyTrainTicketQueryReq req) {
        PageResp<DailyTrainTicketQueryResp> list = dailyTrainTicketService.queryList(req);
        return new CommonResp<>(list);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id) {
        dailyTrainTicketService.delete(id);
        return new CommonResp<>();
    }

}
