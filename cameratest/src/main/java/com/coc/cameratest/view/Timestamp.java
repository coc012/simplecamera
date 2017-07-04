package com.coc.cameratest.view;

import android.util.Log;

/**
 * Created by tang on 2017/1/18.
 */

public class Timestamp {
    private static String sPreOperation = "开始";//上一个打点名
    private static long sPreTimeMills;

    public static void here(String operation) {
        long nowtime = System.currentTimeMillis();
        Log.e("stamp:", sPreOperation + "-->" + operation + "\n 耗时：" + (int) (nowtime - sPreTimeMills));
        sPreTimeMills = nowtime;
        sPreOperation = operation;
    }
}
