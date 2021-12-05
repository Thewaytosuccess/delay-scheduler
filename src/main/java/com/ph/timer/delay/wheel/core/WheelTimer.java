package com.ph.timer.delay.wheel.core;

import com.ph.timer.delay.wheel.ds.ListNode;
import com.ph.timer.delay.wheel.ds.Subject;
import com.ph.timer.delay.wheel.enums.EventTypeEnum;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.ph.timer.delay.wheel.util.DateUtil.getExecuteTime;
import static com.ph.timer.delay.wheel.util.DateUtil.getSeconds;


/**
 * 引入观察者模式
 * @author cdl
 */
public class WheelTimer {

    /**
     * 缓冲队列，用于临时存放新增的任务，以提升新增任务的效率
     */
    private List<ListNode> cachedQueue;

    /**
     * bucket的个数
     */
    private int bucketSize = 60;

    /**
     * 事件发布者，被观察者，用于发布异步任务
     */
    private TaskPublisher publisher;

    public WheelTimer(){
       init();
    }

    WheelTimer(int bucketSize){
       this.bucketSize = bucketSize;
       init();
    }

    private void init(){
        this.cachedQueue = new ArrayList<>(bucketSize * 2);
        this.publisher = new TaskPublisher(bucketSize);
    }

    public void start(){
       this.publisher.start();
    }

    public void cancelTask(Date date, long taskId){
        long executeTime = date.getTime();
        if(executeTime < System.currentTimeMillis()){
            return ;
        }

        //先从缓存队列中查找,删除
        this.cachedQueue.stream().filter(e -> getSeconds(e.key) == getSeconds(executeTime)).forEach(e -> {
            if(e.tasks.contains(taskId)){
                e.tasks.removeIf(t -> t.equals(taskId));
            }
        });

        //清除任务集合为空的元素
        this.cachedQueue.removeIf(e -> e.tasks.isEmpty());

        //发布删除任务的事件
        publishRemoveEvent(executeTime,taskId);
    }

    public void addTask(long delay, TimeUnit unit,long taskId){
        long executeTime = getExecuteTime(delay,unit);

        //先放到缓存队列
        if(this.cachedQueue.stream().anyMatch(e -> getSeconds(e.key) == getSeconds(executeTime))){
            this.cachedQueue.stream().filter(e -> getSeconds(e.key) == getSeconds(executeTime)).forEach(e -> e.tasks.add(taskId));
        }else{
            this.cachedQueue.add(new ListNode(executeTime, new ArrayList<>(Collections.singletonList(taskId))));
        }

        //发布添加任务的事件
        publishAddEvent();
    }

    /**
     * 发布添加任务的事件
     */
    private void publishAddEvent(){
        Subject subject = new Subject();
        subject.eventType = EventTypeEnum.ADD;
        subject.cacheQueue = this.cachedQueue;
        this.publisher.publish(subject);
    }

    /**
     * 发布删除任务的事件
     * @param executeTime 执行时间
     * @param taskId 任务id
     */
    private void publishRemoveEvent(long executeTime,long taskId){
        Subject subject = new Subject();
        subject.eventType = EventTypeEnum.REMOVE;
        subject.executeTime = executeTime;
        subject.taskId = taskId;
        this.publisher.publish(subject);
    }

}
