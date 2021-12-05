package com.ph.timer.delay.wheel.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 日期时间处理类
 */
public class DateUtil {

    private static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final String SECOND_SUFFIX = "000";

    private static final long ONE_THOUSAND = 1000;

    public static String dateTimeFormat(long executeTime){
        return new SimpleDateFormat(DEFAULT_FORMAT).format(new Date(executeTime));
    }

   public static long getSeconds(long executeTime){
        if(executeTime < ONE_THOUSAND){
            throw new IllegalArgumentException("execute time can not be less than " + ONE_THOUSAND);
        }
        String s = String.valueOf(executeTime);
        return s.endsWith(SECOND_SUFFIX) ? executeTime : Long.parseLong(s.substring(0,s.length() - 3).concat(SECOND_SUFFIX));
    }

    @Deprecated
    public static long getSecond(long executeTime){
        SimpleDateFormat format = new SimpleDateFormat(DEFAULT_FORMAT);
        try {
            return format.parse(format.format(new Date(executeTime))).getTime();
        } catch (ParseException e){
            throw new IllegalArgumentException(e);
        }
    }

    public static long getExecuteTime(long delay, TimeUnit unit){
        return System.currentTimeMillis() + unit.toMillis(delay);
    }
}
