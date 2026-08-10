package com.sky.task;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;

import lombok.extern.slf4j.Slf4j;
/*
 * 定时任务类
 */
@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;
    /**
     * 处理订单超时
     */
    @Scheduled(cron = "0 * * * * ?")
    public void ProcessTimeoutOrders(){
        log.info("定时处理超时订单：{}...",LocalDateTime.now());

        List<Orders> ordersList = orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT, LocalDateTime.now().plusMinutes(-15));

        if(ordersList != null && ordersList.size() > 0){
            for(Orders orders : ordersList){
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("支付超时");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
            
        }

    }

    /**
     * 处理订单派送
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrders(){
        log.info("定时处理派送中订单：{}...", LocalDateTime.now());

        List<Orders> ordersList = orderMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS,
                LocalDateTime.now().plusMinutes(-60));

        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orders.setCancelReason("自动完成派送订单");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }

        }
    }
}
