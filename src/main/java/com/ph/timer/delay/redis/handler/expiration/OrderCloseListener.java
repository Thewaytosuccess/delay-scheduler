package com.ph.timer.delay.redis.handler.expiration;

import com.ph.timer.delay.wheel.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
/**
 * 通过监听key过期实现的延迟队列
 * @author lenovo
 */
//@Component
public class OrderCloseListener extends KeyExpirationEventMessageListener {

    @Value("${order.close.prefix}")
    private String orderClosePrefix;

    @Autowired
    public OrderCloseListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        //msg = key,so you should put orderNo into key
        String msg = message.toString();
        if(msg.startsWith(orderClosePrefix)){
            System.out.println("开始关闭订单：closeTime = " + DateUtil.dateTimeFormat(
                    System.currentTimeMillis()) + ";orderNo = " +
                    msg.substring(orderClosePrefix.length()));
        }
    }


}
