package com.jhoves.ticket.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jhoves.ticket.business.domain.*;
import com.jhoves.ticket.business.enums.ConfirmOrderStatusEnum;
import com.jhoves.ticket.business.enums.RedisKeyPreEnum;
import com.jhoves.ticket.business.enums.SeatColEnum;
import com.jhoves.ticket.business.enums.SeatTypeEnum;
import com.jhoves.ticket.business.req.ConfirmOrderDoReq;
import com.jhoves.ticket.business.req.ConfirmOrderTicketReq;
import com.jhoves.ticket.common.context.LoginMemberContext;
import com.jhoves.ticket.common.exception.BusinessException;
import com.jhoves.ticket.common.exception.BusinessExceptionEnum;
import com.jhoves.ticket.common.resp.PageResp;
import com.jhoves.ticket.common.util.SnowUtil;
import com.jhoves.ticket.business.mapper.ConfirmOrderMapper;
import com.jhoves.ticket.business.req.ConfirmOrderQueryReq;
import com.jhoves.ticket.business.resp.ConfirmOrderQueryResp;
import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
public class ConfirmOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(ConfirmOrderService.class);

    @Resource
    private ConfirmOrderMapper confirmOrderMapper;

    @Resource
    private DailyTrainTicketService dailyTrainTicketService;

    @Resource
    private DailyTrainCarriageService dailyTrainCarriageService;

    @Resource
    private DailyTrainSeatService dailyTrainSeatService;

    @Resource
    private AfterConfirmOrderService afterConfirmOrderService;

    @Resource
    private SkTokenService skTokenService;

    @Autowired
    private StringRedisTemplate redisTemplate;

//    @Autowired
//    private RedissonClient redissonClient;

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

    //@SentinelResource("doConfirm") // 限流熔断的接口
    @SentinelResource(value = "doConfirm",blockHandler = "doConfirmBlock")
    public void doConfirm(ConfirmOrderDoReq req) {
         // 校验令牌余量
         boolean validSkToken = skTokenService.validSkToken(req.getDate(), req.getTrainCode(), LoginMemberContext.getId());
         if (validSkToken) {
             LOG.info("令牌校验通过");
         } else {
             LOG.info("令牌校验不通过");
             throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_SK_TOKEN_FAIL);
         }

        //分布式锁解决超卖问题
        String lockKey = RedisKeyPreEnum.CONFIRM_ORDER + "-" + DateUtil.formatDate(req.getDate()) + "-" + req.getTrainCode();
        Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent(lockKey, lockKey, 5, TimeUnit.SECONDS);
        if(setIfAbsent){
            LOG.info("恭喜，抢到锁了！");
        }else{
            //只是没抢到锁，并不知道票抢完了没，所以提示稍后再试
            LOG.info("很遗憾，没抢到锁！");
            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_LOCK_FAIL);
        }

        //利用redisson看门狗来写
        //RLock lock = null;
        try {
//            //使用redisson，自带看门狗
//            lock = redissonClient.getLock(lockKey);
//            /**
//             * waitTime - the maximum time to acquire the lock 等待获取锁时间（最大尝试获得锁的时间），超时返回false
//             * leaseTime - lease time 锁时长，即n秒后自动释放锁
//             * time unit - time unit 时间单位
//             */
//            // boolean tryLock = lock.tryLock(0, 10, TimeUnit.SECONDS); //不带看门狗
//            boolean tryLock = lock.tryLock(0, TimeUnit.SECONDS); //带看门狗
//            if(tryLock) {
//                LOG.info("恭喜，抢到锁了！");
//            }else{
//                //只是没抢到锁，并不知道票抢完了没，所以提示稍后再试
//                LOG.info("很遗憾，没抢到锁！");
//                throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_LOCK_FAIL);
//            }

            //保存确认订单表，状态初始
            DateTime now = DateTime.now();
            Date date = req.getDate();
            String trainCode = req.getTrainCode();
            String start = req.getStart();
            String end = req.getEnd();
            List<ConfirmOrderTicketReq> tickets = req.getTickets();
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
            confirmOrder.setTickets(JSON.toJSONString(tickets));

            confirmOrderMapper.insert(confirmOrder);

            /**
             * 会出现超卖的地方，超卖原因：
             * 假设库存为1，多个线程同时读到余票记录，都认为库存为1，都往后去选座购票，最终导致超卖
             * 测试数据：500个线程什么都没加时执行4秒
             * 解决方案：加锁
             * 1、加synchronized关键字：吞吐量/TPS变低，6s（单机的情况就可以，但是多节点的话还是会出现超卖问题）
             */
            //查出余票记录，需要得到真实的库存
            DailyTrainTicket dailyTrainTicket = dailyTrainTicketService.selectByUnique(date, trainCode, start, end);
            LOG.info("查出余票记录:{}",dailyTrainTicket);

            //扣减余票数量，并判断余票是否足够
            reduceTickets(req, dailyTrainTicket);

            //难点:↓
            //最终的选座结果
            List<DailyTrainSeat> finalSeatList = new ArrayList<>();
            //计算相对第一个座位的偏离值
            //比如选择的是C1，D2，则偏移值是：[0,5]
            //比如选择的是A1，B1，C1，则偏移值是：[0，1，2]
            ConfirmOrderTicketReq ticketReq0 = tickets.get(0);
            if(StrUtil.isNotBlank(ticketReq0.getSeat())){
                LOG.info("本次购票有选座");
                //查出本次选座的座位类型都有那些列，用于计算所选座位与第一个座位的偏离值
                List<SeatColEnum> colEnumList = SeatColEnum.getColsByType(ticketReq0.getSeatTypeCode());
                LOG.info("本次选座的座位类型包含的列：{}",colEnumList);

                //组成和前端两排选座一样的列表，用于做参照的座位列表，例：referSeatList = {A1,C1,D1,F1,A2,C2,D2,F2}
                ArrayList<Object> referSeatList = new ArrayList<>();
                for(int i = 1;i <= 2; i++){
                    for (SeatColEnum seatColEnum : colEnumList) {
                        referSeatList.add(seatColEnum.getCode() + i);
                    }
                }
                LOG.info("用于做参照的两排座位：{}",referSeatList);

                List<Integer> offsetList = new ArrayList<>();
                //绝对偏移值，即：在参照座位列表中的位置
                List<Integer> aboluteOffsetList = new ArrayList<>();
                for(ConfirmOrderTicketReq ticketReq : tickets) {
                    int index = referSeatList.indexOf(ticketReq.getSeat());
                    aboluteOffsetList.add(index);
                }
                LOG.info("计算得到所有座位的绝对值偏移值：{}",aboluteOffsetList);
                for(Integer index : aboluteOffsetList) {
                    int offset = index - aboluteOffsetList.get(0);
                    offsetList.add(offset);
                }
                LOG.info("计算得到所有座位的相对第一个座位的偏移值：{}",offsetList);

                getSeat(
                        finalSeatList,
                        date,
                        trainCode,
                        ticketReq0.getSeatTypeCode(),
                        ticketReq0.getSeat().split("")[0],  //从A1得到A
                        offsetList,
                        dailyTrainTicket.getStartIndex(),
                        dailyTrainTicket.getEndIndex()
                );

            }else{
                LOG.info("本次购票没有选座");
                for(ConfirmOrderTicketReq ticketReq : tickets){
                    getSeat(
                            finalSeatList,
                            date,
                            trainCode,
                            ticketReq.getSeatTypeCode(),
                            null,
                            null,
                            dailyTrainTicket.getStartIndex(),
                            dailyTrainTicket.getEndIndex()
                    );
                }
            }
            LOG.info("最终选座：{}",finalSeatList);


            //选中座位后事务处理：
            // 座位表修改售卖情况sell
            //余票详情表修改余票
            //为会员增加购票记录
            //更新确认订单为成功
            try {
                afterConfirmOrderService.afterDoConfirm(dailyTrainTicket,finalSeatList,tickets,confirmOrder);
            } catch (Exception e){
                LOG.error("保存购票信息失败",e);
                throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_EXCEPTION);
            }
        //} catch (InterruptedException e) {
        //    LOG.error("购票异常",e);
        }finally {
            LOG.info("购票流程结束，释放锁！lockKey：{}",lockKey);
            redisTemplate.delete(lockKey);
            //if(null != lock && lock.isHeldByCurrentThread()){
            //    lock.unlock();
            //}
        }
    }

    /**
     * 挑座位，如果有选座，则一次性挑完，如果无选座，则一个一个挑
     * @param date
     * @param trainCode
     * @param seatType
     * @param column
     * @param offsetList
     */
    private void getSeat(List<DailyTrainSeat> finalSeatList,Date date,String trainCode,String seatType,String column,List<Integer> offsetList,Integer startIndex,Integer endIndex) {
        List<DailyTrainSeat> getSeatList = new ArrayList<>();
        List<DailyTrainCarriage> carriageList = dailyTrainCarriageService.selectBySeatType(date, trainCode, seatType);
        LOG.info("共查出{}个符合条件的车厢",carriageList.size());

        // 一个车厢一个车厢的获取座位数据
        for(DailyTrainCarriage dailyTrainCarriage : carriageList) {
            LOG.info("开始从车厢{}选座",dailyTrainCarriage.getIndex());
            getSeatList = new ArrayList<>();
            List<DailyTrainSeat> seatList = dailyTrainSeatService.selectByCarriage(date,trainCode,dailyTrainCarriage.getIndex());
            LOG.info("车厢{}的座位数：{}",dailyTrainCarriage.getIndex(),seatList.size());
            for (int i = 0;i < seatList.size(); i++) {
                DailyTrainSeat dailyTrainSeat = seatList.get(i);
                Integer seatIndex = dailyTrainSeat.getCarriageIndex();
                String col = dailyTrainSeat.getCol();

                //判断当前座位不能被选中过
                boolean alreadyChooseFlag = false;
                for (DailyTrainSeat finalSeat : finalSeatList) {
                    if(finalSeat.getId().equals(dailyTrainSeat.getId())){
                        alreadyChooseFlag = true;
                        break;
                    }
                }
                if(alreadyChooseFlag) {
                    LOG.info("座位{}被选中过，不能重复选中，继续判断下一个座位",seatIndex);
                    continue;
                }

                //判断column，有值的话要比对列号
                if(StrUtil.isBlank(column)){
                    LOG.info("无选座");
                }else {
                    if(!column.equals(col)){
                        LOG.info("座位{}列值不对，继续判断下一个座位，当前列值：{}，目标列值：{}",seatIndex,col,column);
                        continue;
                    }
                }
                boolean isChoose = calSell(dailyTrainSeat, startIndex, endIndex);
                if(isChoose) {
                    LOG.info("选中座位");
                    getSeatList.add(dailyTrainSeat);
                }else{
                    continue;
                }

                //根据offset选剩下的座位
                boolean isGetAllOffsetSeat = true;
                if(CollUtil.isNotEmpty(offsetList)) {
                    LOG.info("有偏移值：{}，校验偏移的座位是否可选",offsetList);
                    //从索引1开始，索引0就是当前已选中的票
                    for (int j = 1; j < offsetList.size(); j++){
                        Integer offset = offsetList.get(j);
                        //座位在库的索引是从1开始
                        //int nextIndex = seatIndex + oiffset - 1;
                        int nextIndex =  + offset;

                        //有选座时，一定是在同一个车厢
                        if(nextIndex >= seatList.size()){
                            LOG.info("座位{}不可选，偏移后的索引超出这个车厢的座位数",nextIndex);
                            isGetAllOffsetSeat = false;
                            break;
                        }

                        DailyTrainSeat nextDailyTrainSeat = seatList.get(nextIndex);
                        boolean isChooseNext = calSell(nextDailyTrainSeat, startIndex, endIndex);
                        if(isChooseNext) {
                            LOG.info("座位{}被选中",nextDailyTrainSeat.getCarriageIndex());
                            getSeatList.add(nextDailyTrainSeat);
                        }else{
                            LOG.info("座位{}不可选",nextDailyTrainSeat.getCarriageIndex());
                            isGetAllOffsetSeat = false;
                            break;
                        }
                    }
                }
                if(!isGetAllOffsetSeat){
                    getSeatList = new ArrayList<>();
                    continue;
                }

                //保存选好的座位
                finalSeatList.addAll(getSeatList);
                return;
            }
        }
    }

    /**
     * 计算某座位在区间内是否可卖
     * 例：sell = 10001，本次购买区间站1-4，则区间已售000
     * 全部是0，表示这个区间可买，只要有1，就表示区间内已售过票
     *
     * 选中后，要计算购票后的sell，比如原来是10001，本次购买区间站1~4
     * 方案：构造本次购票造成的售卖信息01110，和原sell 10001按位与，最终得到11111
     * @param dailyTrainSeat
     */
    private boolean calSell(DailyTrainSeat dailyTrainSeat,Integer startIndex,Integer endIndex){
        //10001
        String sell = dailyTrainSeat.getSell();
        //000
        String sellPart = sell.substring(startIndex, endIndex);
        if(Integer.parseInt(sellPart) > 0){
            LOG.info("座位{}在本次车站区间{}~~~{}已售过票，不可选中该座位",dailyTrainSeat.getCarriageIndex(),startIndex,endIndex);
            return false;
        }else {
            LOG.info("座位{}在本次车站区间{}~~~{}未售过票，可选中该座位",dailyTrainSeat.getCarriageIndex(),startIndex,endIndex);
            //111
            String curSell = sellPart.replace('0', '1');
            //0111
            curSell = StrUtil.fillBefore(curSell,'0',endIndex);
            //01110
            curSell = StrUtil.fillAfter(curSell,'0',sell.length());

            //当前区间售票信息curSell 与 库存里的已售信息sell 按位与，即可得到该座位卖出此票后的售票详情
            //15（01111）
            int newSellInt = NumberUtil.binaryToInt(curSell) | NumberUtil.binaryToInt(sell);
            //1111
            String newSell = NumberUtil.getBinaryStr(newSellInt);
            //01111
            newSell = StrUtil.fillBefore(newSell,'0',sell.length());
            LOG.info("座位{}被选中，原售票信息：{}，车站区间：{}~~~{}，即：{}，最终售票信息：{}",dailyTrainSeat.getCarriageIndex(),sell,startIndex,endIndex,curSell,newSell);
            dailyTrainSeat.setSell(newSell);
            return true;
        }


    }

    private static void reduceTickets(ConfirmOrderDoReq req, DailyTrainTicket dailyTrainTicket) {
        for(ConfirmOrderTicketReq ticketReq : req.getTickets()){
            String seatTypeCode = ticketReq.getSeatTypeCode();
            SeatTypeEnum seatTypeEnum = EnumUtil.getBy(SeatTypeEnum::getCode, seatTypeCode);
            switch (seatTypeEnum){
                case YDZ:
                    int YDZCountLeft = dailyTrainTicket.getYdz() - 1;
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

    /**
     * 降级方法，需包含限流方法的所有参数和BlockException参数
     * @param req
     * @param e
     */
    public void doConfirmBlock(ConfirmOrderDoReq req, BlockException e) {
        LOG.info("购票请求被限流：{}", req);
        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_FLOW_EXCEPTION);
    }
}