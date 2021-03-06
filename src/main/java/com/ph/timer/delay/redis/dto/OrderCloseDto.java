package com.ph.timer.delay.redis.dto;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * @author cdl
 */
public class OrderCloseDto implements Serializable {

    private String orderNo;

    private long delay;

    private TimeUnit unit;

    public OrderCloseDto(String orderNo, long delay){
        this.orderNo = orderNo;
        this.delay = delay;
        this.unit = TimeUnit.SECONDS;
    }

    public OrderCloseDto(String orderNo, long delay, TimeUnit unit) {
        this.orderNo = orderNo;
        this.delay = delay;
        this.unit = unit;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public void setUnit(TimeUnit unit) {
        this.unit = unit;
    }
}
