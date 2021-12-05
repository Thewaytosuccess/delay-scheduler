package com.ph.timer.delay.redis;

import com.ph.timer.delay.wheel.util.DateUtil;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @author cdl
 */
@Component
public class OrderCloseHandler {

    @Autowired
    private RedissonClient redissonClient;

    private RBlockingDeque<OrderDto> blockingDeque;

    private RDelayedQueue<OrderDto> delayedQueue;

    private static final String ORDER_CLOSE_QUEUE = "order_close_queue";

    @PostConstruct
    public void init(){
        blockingDeque = redissonClient.getBlockingDeque(ORDER_CLOSE_QUEUE);
        delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
        addTask(new OrderDto("001",10));
        addTask(new OrderDto("002",16));
        //检查是否有需要执行的关单任务
        closeOrder();
    }

    private void closeOrder() {
        CompletableFuture.runAsync(() -> {
            while(true){
                OrderDto orderDto = blockingDeque.poll();
                if(Objects.nonNull(orderDto)){
                    System.out.println("关闭订单，orderNo = " + orderDto.getOrderNo() +
                            ";closeTime = " + DateUtil.dateTimeFormat(System.currentTimeMillis()));
                }else{
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    void addTask(OrderDto orderDto){
        System.out.println("添加任务，orderNo = " + orderDto.getOrderNo() +
                ";now = " + DateUtil.dateTimeFormat(System.currentTimeMillis()));
        delayedQueue.offer(orderDto, orderDto.getDelay(), orderDto.getUnit());
    }

}
