package com.ph.timer.delay.redis.handler.zset;

import com.ph.timer.delay.redis.dto.OrderCloseDto;
import com.ph.timer.delay.wheel.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author cdl
 */
@Component
public class OrderCloseTask {

    @Value("${order.close.prefix}")
    private String orderClosePrefix;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final int CORE_SIZE = 8;

    @PostConstruct
    public void init(){
        close(new OrderCloseDto("001",10));
        close(new OrderCloseDto("002",16));
        start();
    }

    public void close(OrderCloseDto orderCloseDto){
        System.out.println("添加关单任务，orderNo = " + orderCloseDto.getOrderNo() +
                ";currentTime = " + DateUtil.dateTimeFormat(System.currentTimeMillis()));
        stringRedisTemplate.opsForZSet().addIfAbsent(orderClosePrefix, orderCloseDto.getOrderNo(),
                System.currentTimeMillis() + orderCloseDto.getUnit().toMillis(orderCloseDto.getDelay()));
    }

    public void start(){
        new ScheduledThreadPoolExecutor(CORE_SIZE).scheduleAtFixedRate(() -> {
            long min = DateUtil.getSeconds(System.currentTimeMillis());
            long max = min + 1000;
            Set<String> orderNos = stringRedisTemplate.opsForZSet().rangeByScore(orderClosePrefix, min, max);
            if(Objects.nonNull(orderNos) && !orderNos.isEmpty()){
                orderNos.forEach(e -> System.out.println("正在关闭订单，orderNo = " + e
                        + ";closeOrderTime = " + DateUtil.dateTimeFormat(min)));
            }
        },0,1, TimeUnit.SECONDS);
    }

}
