package com.coc.camera.cameradialog;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by coc on 2017/7/3.
 */
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Camera.AutoFocusCallback {

    private static final String TAG = "CameraSurfaceView";
    private CameraEventListener mCameraEventListener;
    private Context mContext;
    private SurfaceHolder holder;
    private Camera mCamera;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mCurrentFlashMode = 0;
    // 拍照瞬间调用
    private Camera.ShutterCallback shutter = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            Log.i(TAG, "shutter");
        }
    };
    // 获得没有压缩过的图片数据
    private Camera.PictureCallback raw = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera Camera) {
            Log.i(TAG, "raw");

        }
    };
    //创建jpeg图片回调数据对象
    private Camera.PictureCallback jpeg = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (mCameraEventListener != null) {
                //如果是连续拍 可以打开此项、但是需要处理 对应crash
                //mCamera.stopPreview();// 关闭预览
                //mCamera.startPreview();// 开启预览
                mCameraEventListener.onTakePic(data, camera.getParameters());
            }
        }
    };

    public CameraSurfaceView(Context context) {
        this(context, null);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        getScreenMetrix(context);
        initView();
    }

    public void SetCameraEventListener(CameraEventListener cameraEventListener) {
        this.mCameraEventListener = cameraEventListener;
    }

    private void getScreenMetrix(Context context) {
        WindowManager WM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        WM.getDefaultDisplay().getMetrics(outMetrics);
        mScreenWidth = outMetrics.widthPixels;
        mScreenHeight = outMetrics.heightPixels;
    }

    private void initView() {
        holder = getHolder();//获得surfaceHolder引用
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//设置类型
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
        if (mCamera == null) {
            mCamera = Camera.open();//开启相机
            try {
                mCamera.setPreviewDisplay(holder);//摄像头画面显示在Surface上
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged");
        //设置参数并开始预览
        setCameraParams(mCamera, mScreenWidth, mScreenHeight);
        mCamera.startPreview();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
        mCamera.stopPreview();//停止预览
        mCamera.release();//释放相机资源
        mCamera = null;
        holder = null;
    }

    @Override
    public void onAutoFocus(boolean success, Camera Camera) {
        if (success) {
            Log.i(TAG, "onAutoFocus success=" + success);
        }
    }

    /**
     * 设置闪光灯模式
     * 0：FLASH_MODE_OFF
     * 1：FLASH_MODE_ON
     * 2：FLASH_MODE_AUTO
     */
    public int setFlashMode() {
        String nextFlash = null;
        if (mCurrentFlashMode == 0) {
            nextFlash = Camera.Parameters.FLASH_MODE_ON;
        } else if (mCurrentFlashMode == 1) {
            nextFlash = Camera.Parameters.FLASH_MODE_AUTO;
        } else if (mCurrentFlashMode == 2) {
            nextFlash = Camera.Parameters.FLASH_MODE_OFF;
        } else {
            Toast.makeText(mContext, "当前不支持 设置闪光灯", Toast.LENGTH_SHORT).show();
            return 0;
        }

        if (mCamera == null || mCamera.getParameters() == null
                || mCamera.getParameters().getSupportedFlashModes() == null
                || mCamera.getParameters().getSupportedFlashModes().size() == 0) {
            Toast.makeText(mContext, "当前不支持 设置闪光灯", Toast.LENGTH_SHORT).show();
            return 0;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        for (String item : supportedFlashModes) {
            Log.e("CameraSurfaceView", "support Fl" + item);
        }
        Log.e("CameraSurfaceView", "nextFlash Fl" + nextFlash);


        if (supportedFlashModes.contains(nextFlash)) {
            parameters.setFlashMode(nextFlash);
            mCamera.setParameters(parameters);
            mCurrentFlashMode = (mCurrentFlashMode + 1) % 3;
            return mCurrentFlashMode;
        } else {
            Toast.makeText(mContext, "当前不支持 该闪光灯模式", Toast.LENGTH_SHORT).show();
            return 0;
        }
    }

    public void reStartPreview() {
        setCameraParams(mCamera, mScreenWidth, mScreenHeight);
        mCamera.stopPreview();// 关闭预览
        mCamera.startPreview();// 开启预览
    }

    public void setAutoFocus() {
        try {
            mCamera.autoFocus(this);
        } catch (Exception e) {
            Log.e("CameraSurfaceView", "takePicture exception:", e);
            Toast.makeText(getContext(), "对焦失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }

//    public Camera getCamera() {
//        return mCamera;
//    }

    public void takePicture() {
        //设置参数,并拍照
        // setCameraParams(mCamera, mScreenWidth, mScreenHeight);
        // 当调用camera.takePiture方法后，camera关闭了预览，这时需要调用startPreview()来重新开启预览
        try {
            mCamera.takePicture(null, null, jpeg);
        } catch (Exception e) {
            Log.e("CameraSurfaceView", "takePicture exception:", e);
            Toast.makeText(getContext(), "拍照失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }

    private void setCameraParams(Camera camera, int width, int height) {
        Log.i(TAG, "setCameraParams  width=" + width + "  height=" + height);
        Camera.Parameters parameters = mCamera.getParameters();
        // 获取摄像头支持的PictureSize列表
        List<Camera.Size> pictureSizeList = parameters.getSupportedPictureSizes();
        for (Camera.Size size : pictureSizeList) {
            Log.i(TAG, "pictureSizeList size.width=" + size.width + "  size.height=" + size.height);
        }
        /**从列表中选取合适的分辨率*/
        Camera.Size picSize = getProperSize(pictureSizeList, ((float) height / width));
        if (null == picSize) {
            Log.i(TAG, "null == picSize");
            picSize = parameters.getPictureSize();
        }
        Log.i(TAG, "picSize.width=" + picSize.width + "  picSize.height=" + picSize.height);
        // 根据选出的PictureSize重新设置SurfaceView大小
        float w = picSize.width;
        float h = picSize.height;
        parameters.setPictureSize(picSize.width, picSize.height);
        this.setLayoutParams(new FrameLayout.LayoutParams((int) (height * (h / w)), height));

        // 获取摄像头支持的PreviewSize列表
        List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();

        for (Camera.Size size : previewSizeList) {
            Log.i(TAG, "previewSizeList size.width=" + size.width + "  size.height=" + size.height);
        }
        Camera.Size preSize = getProperSize(previewSizeList, ((float) height) / width);
        if (null != preSize) {
            Log.i(TAG, "preSize.width=" + preSize.width + "  preSize.height=" + preSize.height);
            parameters.setPreviewSize(preSize.width, preSize.height);
        }

        parameters.setJpegQuality(100); // 设置照片质量
//        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
//            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 连续对焦模式
//        }


        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);// 自动对焦模式
        }

//        mCamera.cancelAutoFocus();//自动对焦。
        parameters.setRotation(90);
        mCamera.setDisplayOrientation(90);// 设置PreviewDisplay的方向，效果就是将捕获的画面旋转多少度显示
        mCamera.setParameters(parameters);

    }

    /**
     * 从列表中选取合适的分辨率
     * 默认w:h = 4:3
     * <p>注意：这里的w对应屏幕的height
     * h对应屏幕的width<p/>
     */
    private Camera.Size getProperSize(List<Camera.Size> pictureSizeList, float screenRatio) {
        Log.i(TAG, "screenRatio=" + screenRatio);
        List<Camera.Size> picsizes = pictureSizeList;
        Comparator<Camera.Size> b2sSize = new Comparator<Camera.Size>() {

            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                return rhs.width - lhs.width;
            }
        };
        Collections.sort(picsizes, b2sSize);//从大到小排序

        Camera.Size result = null;
        for (Camera.Size size : picsizes) {
            float currentRatio = ((float) size.width) / size.height;
            if (currentRatio - screenRatio == 0) {
                result = size;
                break;
            }
        }

        if (null == result) {
            for (Camera.Size size : picsizes) {
                float curRatio = ((float) size.width) / size.height;
                if (curRatio == 4f / 3) {// 默认w:h = 4:3
                    result = size;
                    break;
                }
            }
        }

        return result;
    }

    public interface CameraEventListener {
        void onTakePic(byte[] data, Camera.Parameters parameters);
    }


}
