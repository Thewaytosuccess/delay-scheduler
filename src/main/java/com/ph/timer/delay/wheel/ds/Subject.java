package com.ph.timer.delay.wheel.ds;

import com.ph.timer.delay.wheel.enums.EventTypeEnum;

import java.util.List;

/**
 * 被监听的对象，该对象属性发生变动，触发发布订阅事件
 * @author cdl
 */
public class Subject {

    /**
     * 缓冲队列，用于临时存放新增的任务，以提升新增任务的效率
     */
    public List<ListNode> cacheQueue;

    /**
     * 事件类型
     */
    public EventTypeEnum eventType;

    /**
     * 执行时间
     */
    public long executeTime;

    /**
     * 任务id
     */
    public long taskId;
}
