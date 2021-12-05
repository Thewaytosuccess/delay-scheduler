package com.ph.timer.delay.wheel.ds;

import java.util.List;

/**
 * 任务队列
 * @author cdl
 */
public class ListNode {

    /**
     * 下一个节点，指向下一个节点的指针
     */
    public ListNode next;

    /**
     * 时间戳，执行任务的时间
     */
    public long key;

    /**
     * 任务id的集合
     */
    public List<Long> tasks;

    public ListNode(long key,List<Long> tasks){
        this.key = key;
        this.tasks = tasks;
    }

}
