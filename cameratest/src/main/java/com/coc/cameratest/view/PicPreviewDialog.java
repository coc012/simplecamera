package com.coc.cameratest.view;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.coc.cameratest.MainBackUpActivity;
import com.coc.cameratest.R;

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
 */

public class PicPreviewDialog extends BaseDialogFragment {
    public static final String DIR_CAMERA_PATH = Environment.getExternalStorageDirectory() + File.separator + "uxin" + File.separator + "newcar2b" + File.separator + "camera";

    private RelativeLayout rl_loading1;
    private RelativeLayout rl_loading_error;
    private RelativeLayout rl_content;

    private ImageView iv_image;
    private TextView re_take_media;
    private TextView confirm_media_result;

    @Override
    public View realOnCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.camerakit_dialog_preview, container, true);
        initView(view);//配置布局界面
        return view;
    }

    private void initView(View view) {
        rl_loading1 = (RelativeLayout) view.findViewById(R.id.rl_loading1);
        rl_loading_error = (RelativeLayout) view.findViewById(R.id.rl_loading_error);
        rl_content = (RelativeLayout) view.findViewById(R.id.rl_content);

        iv_image = (ImageView) view.findViewById(R.id.iv_image);
        re_take_media = (TextView) view.findViewById(R.id.re_take_media);
        confirm_media_result = (TextView) view.findViewById(R.id.confirm_media_result);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadContent();
    }

    private void loadContent() {
        byte[] jpegArray = CameraKitResultHolder.getJpegArray();
        if (jpegArray == null) {//还没有解析成功
            Toast.makeText(getContext(), "照片数据已被系统回收，请重试", Toast.LENGTH_SHORT).show();
            dismissSafe();
        } else {//显示
            showContent(jpegArray);
        }
    }

    private void showContent(byte[] jpegByte) {
        Timestamp.here("showContent 1");

        Glide.with(getContext())
                .load(jpegByte)
                .asBitmap()
                .listener(new RequestListener<byte[], Bitmap>() {
                    @Override
                    public boolean onException(Exception e, byte[] model, Target<Bitmap> target, boolean isFirstResource) {
                        Timestamp.here("showContent 2");

                        if (rl_loading1 != null) rl_loading1.setVisibility(View.GONE);
                        if (rl_loading_error != null) rl_loading_error.setVisibility(View.VISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, byte[] model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        Timestamp.here("showContent 3");

                        if (rl_loading1 != null) rl_loading1.setVisibility(View.GONE);
                        if (rl_content != null) rl_content.setVisibility(View.VISIBLE);
                        onBitmapDecode(resource);


                        return false;
                    }
                }).into(iv_image);


    }

    private void onBitmapDecode(final Bitmap resource) {
        Timestamp.here("showContent 3");

        re_take_media.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentActivity activity = getActivity();
                dismissSafe();
//                if (activity != null) ((MainBackUpActivity) activity).backFromPreview();

            }
        });

        confirm_media_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rl_loading1.setVisibility(View.VISIBLE);
                savePic(resource);
            }
        });
    }

    private void savePic(Bitmap resource) {
        Timestamp.here("savePic 1");

        Observable.just(resource)
                .map(new Function<Bitmap, String>() {
                    @Override
                    public String apply(@NonNull Bitmap bitmap) throws Exception {

                        File file = new File(DIR_CAMERA_PATH);
                        if (!file.exists()) file.mkdirs();
                        String localStoragePath = DIR_CAMERA_PATH + File.separator + System.currentTimeMillis() + ".jpg";
                        return Bitmap2File(localStoragePath, bitmap, 100);
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String imgpath) throws Exception {
                        Log.e("CameraKit", "PreviewActivity保存地址" + imgpath);
                        toPageForResult(imgpath);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        Log.e("CameraKitPreview", "failed,reason:\n" + Log.getStackTraceString(throwable));
                        Toast.makeText(getContext(), "保存照片失败，请检查存储卡是否可用", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    /**
     * 跳转到请求页面
     */
    private void toPageForResult(String imgPath) {
        CameraKitResultHolder.dispose();
        FragmentActivity activity = getActivity();
        if (activity != null) {
            ((MainBackUpActivity) activity).returnResult(imgPath);
            dismissSafe();
        }

    }

    /**
     * 保存bitmap到文件系统
     */
    public String Bitmap2File(String filePath, Bitmap bm, int quality) {
        File f = new File(filePath);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, quality, out);
            out.flush();
            out.close();
            return filePath;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
