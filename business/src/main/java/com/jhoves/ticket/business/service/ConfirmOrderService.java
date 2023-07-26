package com.jhoves.ticket.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jhoves.ticket.business.domain.DailyTrainTicket;
import com.jhoves.ticket.business.enums.ConfirmOrderStatusEnum;
import com.jhoves.ticket.business.enums.SeatTypeEnum;
import com.jhoves.ticket.business.req.ConfirmOrderDoReq;
import com.jhoves.ticket.business.req.ConfirmOrderTicketReq;
import com.jhoves.ticket.common.context.LoginMemberContext;
import com.jhoves.ticket.common.exception.BusinessException;
import com.jhoves.ticket.common.exception.BusinessExceptionEnum;
import com.jhoves.ticket.common.resp.PageResp;
import com.jhoves.ticket.common.util.SnowUtil;
import com.jhoves.ticket.business.domain.ConfirmOrder;
import com.jhoves.ticket.business.domain.ConfirmOrderExample;
import com.jhoves.ticket.business.mapper.ConfirmOrderMapper;
import com.jhoves.ticket.business.req.ConfirmOrderQueryReq;
import com.jhoves.ticket.business.resp.ConfirmOrderQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


@Service
public class ConfirmOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(ConfirmOrderService.class);

    @Resource
    private ConfirmOrderMapper confirmOrderMapper;

    @Resource
    private DailyTrainTicketService dailyTrainTicketService;


    //个接口根据id是否为空来辨别是保存还是更新
    public void save(ConfirmOrderDoReq req) {
        DateTime now = DateTime.now();
        ConfirmOrder confirmOrder = BeanUtil.copyProperties(req, ConfirmOrder.class);
        //如果id是空，则是保存
        if (ObjectUtil.isNull(confirmOrder.getId())) {
            confirmOrder.setId(SnowUtil.getSnowflakeNextId());
            confirmOrder.setCreateTime(now);
            confirmOrder.setUpdateTime(now);
            confirmOrderMapper.insert(confirmOrder);
            //如果不为空，则更新
        } else {
            confirmOrder.setUpdateTime(now);
            confirmOrderMapper.updateByPrimaryKey(confirmOrder);
        }
    }

    public PageResp<ConfirmOrderQueryResp> queryList(ConfirmOrderQueryReq req) {
        ConfirmOrderExample confirmOrderExample = new ConfirmOrderExample();
        confirmOrderExample.setOrderByClause("id desc");
        ConfirmOrderExample.Criteria criteria = confirmOrderExample.createCriteria();

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());

        PageHelper.startPage(req.getPage(), req.getSize());
        List<ConfirmOrder> confirmOrderList = confirmOrderMapper.selectByExample(confirmOrderExample);

        PageInfo<ConfirmOrder> pageInfo = new PageInfo<>(confirmOrderList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<ConfirmOrderQueryResp> list = BeanUtil.copyToList(confirmOrderList, ConfirmOrderQueryResp.class);

        PageResp<ConfirmOrderQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        confirmOrderMapper.deleteByPrimaryKey(id);
    }

    public void doConfirm(ConfirmOrderDoReq req) {
        //省略业务数据校验，如：车次是否存在，余票是否存在，车次是否在有效期内，tickets条数>0，同乘客同车次是否已买过

        //保存确认订单表，状态初始
        DateTime now = DateTime.now();
        Date date = req.getDate();
        String trainCode = req.getTrainCode();
        String start = req.getStart();
        String end = req.getEnd();
        ConfirmOrder confirmOrder = new ConfirmOrder();
        confirmOrder.setId(SnowUtil.getSnowflakeNextId());
        confirmOrder.setCreateTime(now);
        confirmOrder.setUpdateTime(now);
        confirmOrder.setMemberId(LoginMemberContext.getId());
        confirmOrder.setDate(date);
        confirmOrder.setTrainCode(trainCode);
        confirmOrder.setStart(start);
        confirmOrder.setEnd(end);
        confirmOrder.setDailyTrainTicketId(req.getDailyTrainTicketId());
        confirmOrder.setStatus(ConfirmOrderStatusEnum.INIT.getCode());
        confirmOrder.setTickets(JSON.toJSONString(req.getTickets()));

        confirmOrderMapper.insert(confirmOrder);

        //查出余票记录，需要得到真实的库存
        DailyTrainTicket dailyTrainTicket = dailyTrainTicketService.selectByUnique(date, trainCode, start, end);
        LOG.info("查出余票记录:{}",dailyTrainTicket);

        //扣减余票数量，并判断余票是否足够
        reduceTickets(req, dailyTrainTicket);


        //选座
            // 一个车厢一个车厢的获取座位数据

            //挑选符合条件的座位，如果这个车厢不满足，则进入下个车厢（多个选座应该在同一个车厢）

        //选中座位后事务处理：
            // 座位表修改售卖情况sell
            //余票详情表修改余票
            //为会员增加购票记录
            //更新确认订单为成功
    }

    private static void reduceTickets(ConfirmOrderDoReq req, DailyTrainTicket dailyTrainTicket) {
        for(ConfirmOrderTicketReq ticketReq : req.getTickets()){
            String seatTypeCode = ticketReq.getSeatTypeCode();
            SeatTypeEnum seatTypeEnum = EnumUtil.getBy(SeatTypeEnum::getCode, seatTypeCode);
            switch (seatTypeEnum){
                case YDZ:
                    int YDZCountLeft = dailyTrainTicket.getYdz() - 1;
                    LOG.info("查出YDZ余票记录:{}",YDZCountLeft);
                    if(YDZCountLeft < 0){
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    }
                    dailyTrainTicket.setYdz(YDZCountLeft);
                    break;
                case EDZ:
                    int EDZCountLeft = dailyTrainTicket.getEdz() - 1;
                    if(EDZCountLeft < 0){
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    }
                    dailyTrainTicket.setEdz(EDZCountLeft);
                    break;
                case RW:
                    int RWCountLeft = dailyTrainTicket.getRw() - 1;
                    if(RWCountLeft < 0){
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    }
                    dailyTrainTicket.setRw(RWCountLeft);
                    break;
                case YW:
                    int YWCountLeft = dailyTrainTicket.getYw() - 1;
                    if(YWCountLeft < 0){
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    }
                    dailyTrainTicket.setYw(YWCountLeft);
                    break;
            }
        }
    }
}