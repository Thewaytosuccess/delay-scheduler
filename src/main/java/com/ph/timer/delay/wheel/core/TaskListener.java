package com.ph.timer.delay.wheel.core;

import com.ph.timer.delay.wheel.ds.ListNode;
import com.ph.timer.delay.wheel.ds.Subject;
import com.ph.timer.delay.wheel.enums.EventTypeEnum;

import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.ph.timer.delay.wheel.util.DateUtil.dateTimeFormat;
import static com.ph.timer.delay.wheel.util.DateUtil.getSeconds;

/**
 * 观察者
 * @author cdl
 */
public class TaskListener implements Observer {

    /**
     * bucket和链表头节点的映射
     */
    private final Map<Integer, ListNode> map;

    /**
     * 缓冲队列，用于临时存放新增的任务，以提升新增任务的效率
     */
    private List<ListNode> cachedQueue;

    /**
     * 时间轮bucket的个数
     */
    private final int size;

    TaskListener(int size){
        this.size = size;
        this.map = new HashMap<>(size);
        //初始化bucket
        for(int i = 0; i < size; ++i){
            map.put(i,null);
        }
    }

    /**
     * 监听到缓存队列发生变化时，重构时间轮
     */
    @Override
    public void update(Observable o, Object arg) {
        Subject subject = (Subject) arg;
        //目前只支持两种事件：添加任务、取消任务
        if(subject.eventType == EventTypeEnum.ADD){
            this.cachedQueue = subject.cacheQueue;
            onAdd(this.cachedQueue);
        }else{
            onRemoval(subject.executeTime,subject.taskId);
        }
    }

    private int getBucket(long executeTime){
        return (int)(getSeconds(executeTime) % size);
    }

    private void onRemoval(long executeTime, long taskId){
        //从时间轮中查找，先定位到bucket
        int bucket = getBucket(executeTime);
        ListNode head = map.get(bucket);
        if(head == null){
            return ;
        }

        //小于头节点的不考虑，只考虑即将执行的任务中是否有指定删除的任务
        long key = getSeconds(head.key);
        executeTime = getSeconds(executeTime);

        if(executeTime == key){
            head.tasks.removeIf(e -> e.equals(taskId));

            //将任务为空的节点删除
            if(head.tasks.isEmpty()){
                map.put(bucket,head.next);
            }
        }else if(executeTime > key){
            //非最后一个节点且不等于指定删除的任务的时戳
            while(head.next != null && getSeconds(head.next.key) != executeTime){
                head = head.next;
            }

            //最后一个节点刚好包含指定删除的任务
            if(head.next == null && getSeconds(head.key) == executeTime){
                head.tasks.removeIf(e -> e.equals(taskId));
            }

            //中间某个节点包含指定删除的任务
            if(head.next != null){
                head.next.tasks.removeIf(e -> e.equals(taskId));

                //将任务为空的节点删除
                if(head.next.tasks.isEmpty()){
                    head.next = head.next.next;
                }
            }
        }

        System.out.println("任务取消成功，任务id = " + taskId + ";原定执行时间 = " + dateTimeFormat(executeTime));
    }

    private void printTasks(){
        this.map.forEach((k,v) -> {
            if(Objects.nonNull(v)){
                System.out.println("添加任务成功：bucket = " + k +
                        ";当前时间 = " + dateTimeFormat(System.currentTimeMillis()) +
                        ";预定执行时间 = " + dateTimeFormat(v.key) + ":" + v.tasks + ";\n");
            }
        });
    }

    private void onAdd(List<ListNode> cacheQueue){
        //发布订阅，异步将缓存队列中的任务加入到时间轮
        if(!cacheQueue.isEmpty()){
            Iterator<ListNode> iterator = cacheQueue.iterator();
            while (iterator.hasNext()){
                ListNode e = iterator.next();
                long executeTime = getSeconds(e.key);
                int bucket = getBucket(executeTime);
                ListNode head = map.get(bucket);
                if(head == null){
                    map.put(bucket,new ListNode(executeTime,e.tasks));
                    printTasks();
                    return ;
                }

                long key = getSeconds(head.key);
                if(executeTime == key){
                    head.tasks.addAll(e.tasks);
                }else if(executeTime > key){
                    //如果下一个节点不为空，且key小于任务的执行时间，继续向后查找
                    while(head.next != null && getSeconds(head.next.key) < executeTime){
                        head = head.next;
                    }

                    //对于最后一个节点，直接插到队尾
                    if(head.next == null){
                        if(getSeconds(head.key) < executeTime){
                            //创建新节点，添加到队列尾部
                            head.next = new ListNode(executeTime,e.tasks);
                        }
                    }else{
                        //中间某个节点的key等于执行时间
                        if(getSeconds(head.next.key) == executeTime){
                            head.next.tasks.addAll(e.tasks);
                        }else{
                            //创建新节点，添加到当前节点和下一个节点之间
                            ListNode node = new ListNode(executeTime,e.tasks);
                            node.next = head.next;
                            head.next = node;
                        }
                    }
                }else{
                    //向前插入
                    ListNode node = new ListNode(executeTime,e.tasks);
                    node.next = head;
                    //重置头节点
                    map.put(bucket,node);
                }
                iterator.remove();
            }

        }
    }

    void start(){
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(size);
        executor.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            int bucket = getBucket(now);

            //查询此时有没有待执行任务
            ListNode listNode = this.map.get(bucket);
            if(listNode != null && getSeconds(listNode.key) == getSeconds(now)){
                System.out.println("开始执行任务：bucket = " + bucket + ";原定任务执行时间 = " +
                        dateTimeFormat(listNode.key) + ";当前时间 = " + dateTimeFormat(now));
                doTask(listNode.tasks);

                //任务执行完成之后，先删除任务，再更新任务节点，将下一个节点作为头节点
                removeTask(listNode);
                this.map.put(bucket,listNode.next);
            }
        },0,1, TimeUnit.SECONDS);
    }

    private void removeTask(ListNode node){
        if(Objects.nonNull(node)){
            this.cachedQueue.removeIf(e -> getSeconds(e.key) == getSeconds(node.key));
        }
    }

    /**
     * 模拟任务执行，可以异步，但是这里采用同步
     * @param tasks 任务id
     */
    void doTask(List<Long> tasks){
        if(!tasks.isEmpty()){
            tasks.forEach(e -> {
                if(e == 1){
                    //模拟订单支付成功通知
                    System.out.println("任务id：" + e + ",订单支付成功");
                }else if(e == 2){
                    //模拟关闭订单操作
                    System.out.println("任务id：" + e + ",订单关闭成功");
                }
            });
        }
    }

}
