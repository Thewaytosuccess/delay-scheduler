package com.ph.timer.delay.redis.handler.expiration;

import com.ph.timer.delay.redis.dto.OrderCloseDto;
import com.ph.timer.delay.wheel.util.DateUtil;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

//@Component
public class OrderCloseScheduler {

    @Value("${order.close.prefix}")
    private String orderClosePrefix;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonConnectionFactory redissonConnectionFactory;

    @Bean
    public RedisMessageListenerContainer listenerContainer(){
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redissonConnectionFactory);
        return container;
    }

    @PostConstruct
    public void initialize(){
        close(new OrderCloseDto("001",10));
        close(new OrderCloseDto("002",16));
    }

    public void close(OrderCloseDto orderCloseDto){
        String orderNo = orderCloseDto.getOrderNo();
        System.out.println("添加任务，orderNo = " + orderNo + ";currentTime = "
                + DateUtil.dateTimeFormat(System.currentTimeMillis()));
        stringRedisTemplate.opsForValue().set(orderClosePrefix + orderNo,
                orderNo, orderCloseDto.getDelay(),orderCloseDto.getUnit());
    }
}
