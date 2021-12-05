package com.ph.timer.delay.wheel.core;

import com.ph.timer.delay.wheel.ds.Subject;

import java.util.Observable;

/**
 * 被观察者
 * @author cdl
 */
class TaskPublisher extends Observable {

    private final TaskListener listener;

    TaskPublisher(int size){
        this.listener = new TaskListener(size);
        register();
    }

    /**
     * 将被观察者注册到观察者
     */
    private void register(){
        this.addObserver(listener);
    }

    /**
     * 启动任务
     */
    void start(){
       this.listener.start();
    }

    /**
     * 事件发布
     * @param subject 被监听的对象
     */
    public void publish(Subject subject){
        //更新变动的属性
        setChanged();

        //通知观察者
        notifyObservers(subject);
    }
}
