package com.coc.cameratest.view;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coc.cameratest.R;

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

import static com.coc.cameratest.view.PicPreviewDialog.DIR_CAMERA_PATH;

/**
 * Created by tang on 2017/7/3.
 */

public class CameraDialg extends BaseDialogFragment {
    private CameraSurfaceView mCameraSurfaceView;
    private RelativeLayout rl_content;
    private Button takePicBtn;
    private LinearLayout ll_top_preview_panel;
    private LinearLayout preview_control_panel;
    private ImageView toggleFlash;
    private TextView cancel_media_action;
    private TextView re_take_media;
    private TextView confirm_media_result;

    @Override
    public View realOnCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_camera, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mCameraSurfaceView = (CameraSurfaceView) view.findViewById(R.id.cameraSurfaceView);
        ll_top_preview_panel = (LinearLayout) view.findViewById(R.id.ll_top_preview_panel);
        toggleFlash = (ImageView) view.findViewById(R.id.toggleFlash);
        toggleFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int flashMode = mCameraSurfaceView.setFlashMode();
                toggleFlash.setImageLevel(flashMode);
            }
        });

        rl_content = (RelativeLayout) view.findViewById(R.id.rl_content);
        takePicBtn = (Button) view.findViewById(R.id.takePic);
        mCameraSurfaceView.SetCameraEventListener(new CameraSurfaceView.CameraEventListener() {
            @Override
            public void onTakePic(byte[] data, Camera.Parameters parameters) {
                onPicTaken(data, parameters);
            }
        });

        rl_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tapFocus();
            }
        });


        takePicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePic();
            }
        });

        //保存用面板

        preview_control_panel = (LinearLayout) view.findViewById(R.id.preview_control_panel);
        cancel_media_action = (TextView) view.findViewById(R.id.cancel_media_action);
        re_take_media = (TextView) view.findViewById(R.id.re_take_media);
        confirm_media_result = (TextView) view.findViewById(R.id.confirm_media_result);

        cancel_media_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "点击了取消", Toast.LENGTH_SHORT).show();
                dismissSafe();
            }
        });

        re_take_media.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "点击了重拍", Toast.LENGTH_SHORT).show();
                showSavePanel(false);
                startPreview();
            }
        });

        confirm_media_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "点击了保存", Toast.LENGTH_SHORT).show();
                toSaveFile();
            }
        });

        showSavePanel(false);

    }

    private void showSavePanel(boolean saveMode) {
        if (saveMode) {//查看预览照片
            rl_content.setClickable(false);
            ll_top_preview_panel.setVisibility(View.GONE);
            takePicBtn.setVisibility(View.GONE);
            preview_control_panel.setVisibility(View.VISIBLE);
        } else {//预览模式
            rl_content.setClickable(true);
            ll_top_preview_panel.setVisibility(View.VISIBLE);
            takePicBtn.setVisibility(View.VISIBLE);

            preview_control_panel.setVisibility(View.GONE);
        }
    }

    private void startPreview() {
        mCameraSurfaceView.reStartPreview();

    }


    private void onPicTaken(byte[] data, Camera.Parameters parameters) {
        CameraKitResultHolder.dispose();
        CameraKitResultHolder.setJpegArray(data);
        Timestamp.here("CameraDialg onPictureTaken 2");


//        PicPreviewDialog picPreviewDialog = new PicPreviewDialog();
//        picPreviewDialog.setCancelable(true, false)
//                .setWithdMode(BaseDialogFragment.WIDTH_MATCH)
//                .showSafe(getFragmentManager(), "preview");
    }


    public void tapFocus() {
        Log.e("CameraDialg", "tapFous");
        if (mCameraSurfaceView != null) {
            mCameraSurfaceView.setAutoFocus();
        }
    }

    public void takePic() {
        Log.e("CameraDialg", "tapFous");
        if (mCameraSurfaceView != null) {
            showSavePanel(true);
            mCameraSurfaceView.takePicture();
        }
    }

    private void toSaveFile() {
        byte[] jpegArray = CameraKitResultHolder.getJpegArray();
        if (jpegArray == null) return;
        Observable.just(jpegArray)
                .map(new Function<byte[], String>() {
                    @Override
                    public String apply(@NonNull byte[] bytes) throws Exception {
                        File file = new File(DIR_CAMERA_PATH);
                        if (!file.exists()) file.mkdirs();
                        String localStoragePath = DIR_CAMERA_PATH + File.separator + System.currentTimeMillis() + ".jpg";
                        return bytes2File(bytes, localStoragePath);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String imgpath) throws Exception {
                        Log.e("CameraKit", "PreviewActivity保存地址" + imgpath);
                        if (!TextUtils.isEmpty(imgpath))
                            toPageForResult(imgpath);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        Log.e("CameraKitPreview", "failed,reason:\n" + Log.getStackTraceString(throwable));
                        Toast.makeText(getContext(), "保存照片失败，请检查存储卡是否可用!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void toPageForResult(String imgpath) {
        Toast.makeText(getContext(), "保存照片成功，地址：!" + imgpath, Toast.LENGTH_SHORT).show();
        dismissSafe();

    }


    /**
     * @param bytes
     * @param outputFile
     * @return
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
}
