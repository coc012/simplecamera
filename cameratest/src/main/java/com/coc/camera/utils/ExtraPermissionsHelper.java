package com.coc.camera.utils;

/**
 * Created by tang on 2017/7/3.
 */

import android.hardware.Camera;

/**
 * 额外权限检查类，用于处理一些自定义rom的 权限问题
 */
public class ExtraPermissionsHelper {
    private boolean isCheckedCamera;//标记是否 检查过相机权限
    private boolean isCameraGranted;//标记相机是否获取权限；


    private ExtraPermissionsHelper() {
    }

    public static ExtraPermissionsHelper getInstance() {
        return SingleonHolder.mInstance;
    }

    /**
     * 如果已经检查过相机权限，那就返回上次的 检查结果
     * 避免 isCameraUseable 耗时方法 每次都调用
     *
     * @return
     */
    public boolean checkPermission_camera() {
        if (isCheckedCamera) return isCameraGranted;//
        isCameraGranted = isCameraUseable(0);
        isCheckedCamera = true;
        return isCameraGranted;
    }

    //处理Flyme 相机的异常
    private synchronized boolean isCameraUseable(int cameraID) {
        boolean canUse = true;
        Camera mCamera = null;
        try {
            //对于一些奇特的Rom比如Flyme、MIUI ，就算禁用了权限 权限检查还是会返回true，所以需要手动try catch进行判断
            mCamera = Camera.open(cameraID);
            Camera.Parameters mParameters = mCamera.getParameters();
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            canUse = false;
        } finally {
            if (mCamera != null) mCamera.release();
        }
        return canUse;

    }

    private static class SingleonHolder {
        private static final ExtraPermissionsHelper mInstance = new ExtraPermissionsHelper();
    }
}
