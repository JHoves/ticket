package com.jhoves.ticket.business.controller.admin;

import com.jhoves.ticket.common.context.LoginMemberContext;
import com.jhoves.ticket.common.resp.CommonResp;
import com.jhoves.ticket.common.resp.PageResp;
import com.jhoves.ticket.business.req.DailyTrainStationQueryReq;
import com.jhoves.ticket.business.req.DailyTrainStationSaveReq;
import com.jhoves.ticket.business.resp.DailyTrainStationQueryResp;
import com.jhoves.ticket.business.service.DailyTrainStationService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/daily-train-station")
public class DailyTrainStationAdminController {

    @Resource
    private DailyTrainStationService dailyTrainStationService;

    @PostMapping("/save")
    public CommonResp<Object> save(@Valid @RequestBody DailyTrainStationSaveReq req) {
        dailyTrainStationService.save(req);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<PageResp<DailyTrainStationQueryResp>> queryList(@Valid DailyTrainStationQueryReq req) {
        PageResp<DailyTrainStationQueryResp> list = dailyTrainStationService.queryList(req);
        return new CommonResp<>(list);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id) {
        dailyTrainStationService.delete(id);
        return new CommonResp<>();
    }

}
