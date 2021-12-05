package com.ph.timer.delay.wheel.starter;

import com.ph.timer.delay.wheel.core.WheelTimer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class WheelTimerStarter {

    public static void main(String[] args) throws ParseException {
        WheelTimer timer = new WheelTimer();
        timer.start();
        System.out.println("my wheel-timer is started.....");

        Scanner scanner = new Scanner(System.in);
        System.out.println("please add task:");
        String input = scanner.nextLine();
        addTask(input,timer);

        while(!"exit".endsWith(input)){
            input = scanner.nextLine();
            if(!"exit".endsWith(input)){
                addTask(input,timer);
            }
        }
    }

    private static void addTask(String input,WheelTimer timer) throws ParseException {
        String[] s = input.split(",");
        if(s.length < 3){
            throw new IllegalArgumentException("params illegal");
        }

        int taskType = Integer.parseInt(s[0]);
        if(taskType == 1){
            //添加任务，格式：【1,3,s,1】，表示添加一个3秒钟后执行且任务id=1的任务
            TimeUnit unit = TimeUnit.SECONDS;
            if(s[2].equals("m")){
                unit = TimeUnit.MINUTES;
            }else if(s[2].equals("h")){
                unit = TimeUnit.HOURS;
            }
            timer.addTask(Long.parseLong(s[1]),unit,Long.parseLong(s[3]));
        }else {
            //取消任务，格式：【0,2021-11-29 20:00:00,1】,表示取消一个晚上8点钟执行且id=1的任务
            timer.cancelTask(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(s[1]),Long.parseLong(s[2]));
        }
    }
}
