package com.coc.camera.cameradialog;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.coc.camera.R;
import com.coc.camera.base.BaseDialogFragment;
import com.coc.camera.config.CommonConfig;
import com.coc.camera.utils.ExtraPermissionsHelper;
import com.coc.camera.utils.Timestamp;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by tang on 2017/7/3.
 * 使用说明
 * 最好使用静态方法进行调用newInstance()
 * 返回值获取：(优先级 降低)
 * 1.使用 匿名内部类FileSavedEventListener获取
 * 2. 对应的activity 实现FileSavedEventListener
 */

public class CameraDialg2 extends BaseDialogFragment {
    private CameraSurfaceView mCameraSurfaceView;
    private RelativeLayout rl_priview_content;
    private RelativeLayout rl_saving_loading;

    private LinearLayout ll_preview_top_panel;
    private ImageView iv_preview_top_toggleFlash;

    private RelativeLayout rl_preview_botton_panel;
    private ImageView iv_preview_bottom_takePic;
    private TextView tv_preview_bottom_cancel;

    private LinearLayout ll_saving_bottom_panel;

    private TextView tv_saving_bottom_cancel;
    private TextView tv_saving_bottom_retake;
    private TextView tv_saving_bottom_save;
    private FileSavedEventListener mFileSavedEventListener;
    private FrameLayout fl_preview;
    private FrameLayout fl_saving;
    private ImageView iv_saving_pic;
    private boolean currentMode;//false:相机预览 true：图片预览

    @Nullable
    public static CameraDialg2 newInstance() {
        //检查相机权限
        boolean isCameraable = ExtraPermissionsHelper.getInstance().checkPermission_camera();
        if (isCameraable) {
            CameraDialg2 cameradialg = new CameraDialg2();
            cameradialg.setCancelable(true, false)
                    .setMaskable(false)
                    .setWithdMode(BaseDialogFragment.WIDTH_MATCH);
            return cameradialg;
        } else {
            //Prompt.showToast("请开启相机权限后重试");
            return null;
        }
    }

    @Override
    public View realOnCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_camera_2, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        fl_preview = (FrameLayout) view.findViewById(R.id.fl_preview);
        fl_saving = (FrameLayout) view.findViewById(R.id.fl_saving);

        showSaveMode(false);//显示为预览模式


        mCameraSurfaceView = (CameraSurfaceView) view.findViewById(R.id.cameraSurfaceView);
        rl_priview_content = (RelativeLayout) view.findViewById(R.id.rl_priview_content);

        //预览时 顶部操作面板
        ll_preview_top_panel = (LinearLayout) view.findViewById(R.id.ll_preview_top_panel);
        iv_preview_top_toggleFlash = (ImageView) view.findViewById(R.id.iv_preview_top_toggleFlash);

        //预览时 底部操作面板
        rl_preview_botton_panel = (RelativeLayout) view.findViewById(R.id.rl_preview_botton_panel);
        iv_preview_bottom_takePic = (ImageView) view.findViewById(R.id.iv_preview_bottom_takePic);
        tv_preview_bottom_cancel = (TextView) view.findViewById(R.id.tv_preview_bottom_cancel);


        //保存时  底部操作面板
        iv_saving_pic = (ImageView) view.findViewById(R.id.iv_saving_pic);

        ll_saving_bottom_panel = (LinearLayout) view.findViewById(R.id.ll_saving_bottom_panel);
        tv_saving_bottom_cancel = (TextView) view.findViewById(R.id.tv_saving_bottom_cancel);
        tv_saving_bottom_retake = (TextView) view.findViewById(R.id.tv_saving_bottom_retake);
        tv_saving_bottom_save = (TextView) view.findViewById(R.id.tv_saving_bottom_save);


        rl_saving_loading = (RelativeLayout) view.findViewById(R.id.rl_saving_loading);


        //相机拍照 事件
        mCameraSurfaceView.SetCameraEventListener(new CameraSurfaceView.CameraEventListener() {
            @Override
            public void onTakePic(byte[] data, Camera.Parameters parameters) {
                //startPreview();
                onPicTaken(data, parameters);
            }
        });

        //自动对焦 事件
        rl_priview_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tapFocus();
            }
        });


        //切换闪光灯 事件
        iv_preview_top_toggleFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int flashMode = mCameraSurfaceView.setFlashMode();
                iv_preview_top_toggleFlash.setImageLevel(flashMode);
            }
        });


        rl_preview_botton_panel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //只为拦截 事件
            }
        });

        //拍照 事件
        iv_preview_bottom_takePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePic();
            }
        });

        //预览时  取消按钮
        tv_preview_bottom_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissSafe();
            }
        });


        //保存用面板

        //保存时 拦截 事件
        fl_saving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        //取消按键
        tv_saving_bottom_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getContext(), "点击了取消", Toast.LENGTH_SHORT).show();
                dismissSafe();
            }
        });

        //重拍
        tv_saving_bottom_retake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getContext(), "点击了重拍", Toast.LENGTH_SHORT).show();
                methodRetake();
            }
        });

        //保存
        tv_saving_bottom_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getContext(), "点击了保存", Toast.LENGTH_SHORT).show();
                toSaveFile();
            }
        });


    }

    private void methodRetake() {
        startPreview();
        showSaveMode(false);
    }

    //是否显示保存为保存模式 ui
    private void showSaveMode(boolean saveMode) {
        currentMode = saveMode;
        if (saveMode) {//查看预览照片
//            rl_priview_content.setClickable(false);
//            ll_preview_top_panel.setVisibility(View.GONE);
//            rl_preview_botton_panel.setVisibility(View.GONE);
//
//            ll_saving_bottom_panel.setVisibility(View.VISIBLE);
            fl_preview.setVisibility(View.GONE);
            fl_saving.setVisibility(View.VISIBLE);
        } else {//预览模式
//            rl_priview_content.setClickable(true);
//
//            ll_preview_top_panel.setVisibility(View.VISIBLE);
//            rl_preview_botton_panel.setVisibility(View.VISIBLE);
//
//            ll_saving_bottom_panel.setVisibility(View.GONE);


            fl_preview.setVisibility(View.VISIBLE);
            fl_saving.setVisibility(View.GONE);
        }
    }

    //重新预览
    private void startPreview() {
        mCameraSurfaceView.reStartPreview();
    }

    private void onPicTaken(byte[] data, Camera.Parameters parameters) {
        CameraKitResultHolder.dispose();
        CameraKitResultHolder.setJpegArray(data);

        showSaveMode(true);//切换成 照片预览模式
        showSavePanel(false);

        Timestamp.here("CameraDialg onPictureTaken 2");
        Glide.with(this)
                .load(data)
                .asBitmap()
                .listener(new RequestListener<byte[], Bitmap>() {
                    @Override
                    public boolean onException(Exception e, byte[] model, Target<Bitmap> target, boolean isFirstResource) {
                        methodRetake();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, byte[] model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        showSavePanel(true);
                        return false;
                    }
                })
                .into(iv_saving_pic);
    }

    private void showLoading(boolean isShowLoading) {
        //rl_saving_loading.setVisibility(isShowLoading ? View.VISIBLE : View.GONE);
    }

    private void showSavePanel(boolean isShowSavePanel) {
        ll_saving_bottom_panel.setVisibility(isShowSavePanel ? View.VISIBLE : View.GONE);
    }

    //用户响应：触摸对焦
    public void tapFocus() {
        Log.e("CameraDialg", "tapFous");
        if (mCameraSurfaceView != null) {
            mCameraSurfaceView.setAutoFocus();
        }
    }

    //用户响应：拍照
    public void takePic() {
        Log.e("CameraDialg", "tapFous");
        if (mCameraSurfaceView != null) {
            // showSaveMode(true);
            mCameraSurfaceView.takePicture();
        }
    }

    //回传 保存的图片文件地址

    /**
     * 回传 保存的图片文件地址
     * 优先使用 mFileSavedEventListener 进行回传
     * 如果没有设置 mFileSavedEventListener，就判断 接口实现回传
     *
     * @param imgpath
     */
    private void toPageForResult(String imgpath) {
        //Toast.makeText(getContext(), "保存照片成功，地址：!" + imgpath, Toast.LENGTH_SHORT).show();
        dismissSafe();
        CameraKitResultHolder.dispose();//清理为使用的

        if (mFileSavedEventListener != null) {
            mFileSavedEventListener.onFileSaved(imgpath);
            return;
        }

        FragmentActivity activity = getActivity();
        if (activity != null && activity instanceof FileSavedEventListener) {
            ((FileSavedEventListener) activity).onFileSaved(imgpath);
        } else {
            Log.i("CameraDialg", "can't send imgpath,may not set Listener! imgpath:" + imgpath);
        }

    }


    //保存照片到文件，成功后回传 图片文件地址
    private void toSaveFile() {
        showSavePanel(false);
        byte[] jpegArray = CameraKitResultHolder.getJpegArray();
        if (jpegArray == null) {
            Toast.makeText(getContext(), "数据已被系统回收，请重新拍摄", Toast.LENGTH_SHORT).show();
            return;
        }
        Observable.just(jpegArray)
                .map(new Function<byte[], String>() {
                    @Override
                    public String apply(@NonNull byte[] bytes) throws Exception {
                        File file = new File(CommonConfig.DIR_CAMERA_PATH);
                        if (!file.exists()) file.mkdirs();
                        String localStoragePath = CommonConfig.DIR_CAMERA_PATH + File.separator + System.currentTimeMillis() + ".jpg";
                        return bytes2File(bytes, localStoragePath);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String imgpath) throws Exception {
                        Log.e("CameraKit", "PreviewActivity保存地址" + imgpath);
                        showLoading(false);

                        if (!TextUtils.isEmpty(imgpath)) {
                            toPageForResult(imgpath);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        showSavePanel(true);
                        showLoading(false);

                        Log.e("CameraKitPreview", "failed,reason:\n" + Log.getStackTraceString(throwable));
                        Toast.makeText(getContext(), "保存照片失败，请检查存储卡是否可用!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void saveFieldStateForReCreate(Bundle outState) {
        super.saveFieldStateForReCreate(outState);
        outState.putBoolean("currentMode", currentMode);
    }

    @Override
    protected void restoreFieldStateFromSaved(Bundle savedInstanceState) {
        super.restoreFieldStateFromSaved(savedInstanceState);
        this.currentMode = savedInstanceState.getBoolean("currentMode");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentMode) mCameraSurfaceView.stopPreview();
        Log.e("CameraDialg2", "onResume");
    }

    @Override
    public void onPause() {
        Log.e("CameraDialg2", "onPause");
        super.onPause();
    }


    /**
     * 保存图片字节数组到 文件
     */
    public String bytes2File(byte[] bytes, String outputFile) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            file = new File(outputFile);
            if (file.exists()) {
                file.delete();
            }

            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bytes);
            return outputFile;
        } catch (FileNotFoundException e) {
            Toast.makeText(getContext(), "无法创建文件，请检查存储卡是否可用", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            Toast.makeText(getContext(), "保存照片失败，请检查存储卡是否可用", Toast.LENGTH_SHORT).show();

            e.printStackTrace();
            return null;
        } finally {

            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public CameraDialg2 setFileSavedEventListener(FileSavedEventListener fileSavedEventListener) {
        this.mFileSavedEventListener = fileSavedEventListener;
        return this;
    }

    public interface FileSavedEventListener {
        void onFileSaved(String imgPath);
    }

}