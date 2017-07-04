package com.coc.camera.config;

import android.os.Environment;

import java.io.File;

/**
 * Created by tang on 2017/5/17.
 */

/**
 * 静态配置类
 * 功能：主要用于设定一些app静态常量
 * 考虑到library可能会被多个app共用，
 * 比如
 * 1.文件目录
 * 2.某些需要静态配置的key，比如图片上传的APP、KEY字段
 */
public class CommonConfig {


    //url 适用于 新车购车H5


    //图片上传

    //相机拍照路径  /uxin/newcar2b/camera
    public static final String DIR_CAMERA_PATH = Environment.getExternalStorageDirectory() + File.separator + "uxin" + File.separator + "newcar2b" + File.separator + "camera";

    public static final String CACHE_DIR_PATH = Environment.getExternalStorageDirectory() + File.separator + "uxin" + File.separator + "newcar2b" + File.separator + "cache";
    //人脸识别 保存图片露肩
    public static final String LIVE_DIR_PATH = Environment.getExternalStorageDirectory() + File.separator + "uxin" + File.separator + "newcar2b" + File.separator + "Liveness";


}
