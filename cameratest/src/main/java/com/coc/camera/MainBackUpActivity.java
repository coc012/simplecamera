package com.coc.camera;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coc.camera.view.BaseDialogFragment;
import com.coc.camera.view.CameraDialg;
import com.coc.camera.view.CameraKitResultHolder;
import com.coc.camera.view.PicPreviewDialog;
import com.coc.camera.view.Timestamp;

public class MainBackUpActivity extends AppCompatActivity implements CameraDialg.FileSavedEventListener {


    public static final String KEY_IMG_PATH = "KEY_IMGPATH";
    private Button takePicBtn;
    private ImageView iv_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 全屏显示
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main_backup);
        takePicBtn = (Button) findViewById(R.id.iv_preview_bottom_takePic);
        iv_content = (ImageView) findViewById(R.id.iv_content);

    }

    private void onPicTaken(byte[] data, Camera.Parameters parameters) {
        CameraKitResultHolder.dispose();
        CameraKitResultHolder.setJpegArray(data);
        Timestamp.here("CameraKitActivity onPictureTaken 2");

//                Intent intent = new Intent(CameraKitActivity.this, CameraKitPreviewActivity.class);
//                startActivityForResult(intent, CameraKitPreviewActivity.REQUEST_CODE);
        PicPreviewDialog picPreviewDialog = new PicPreviewDialog();
        picPreviewDialog.setCancelable(true, false)
                .setWithdMode(BaseDialogFragment.WIDTH_MATCH)
                .showSafe(getSupportFragmentManager(), "preview");
    }

    public void tapFocus(View view) {
        Log.e("mainActivity", "tapFous");
//        if (mCameraSurfaceView != null) {
//            mCameraSurfaceView.setAutoFocus();
//        }
    }

    public void takePic(View view) {
        Log.e("mainActivity", "tapFous");
        CameraDialg cameradialg = new CameraDialg();
//        cameradialg.setFileSavedEventListener(new CameraDialg.FileSavedEventListener() {
//            @Override
//            public void onFileSaved(String imgPath) {
//                Toast.makeText(MainBackUpActivity.this, "图片地址：" + imgPath, Toast.LENGTH_SHORT).show();
//                Glide.with(MainBackUpActivity.this).load(imgPath).into(iv_content);
//            }
//        });
        cameradialg.setCancelable(true, false)
                .setWithdMode(BaseDialogFragment.WIDTH_MATCH)
                .showSafe(getSupportFragmentManager(), "preview");
    }

    public void returnResult(String imgPath) {
        Toast.makeText(MainBackUpActivity.this, "图片地址：" + imgPath, Toast.LENGTH_SHORT).show();
        Log.e("mainActivity", "returnResult imgPath：" + imgPath);

//        Intent intent = new Intent();
//        intent.putExtra(KEY_IMG_PATH, imgPath);
//        setResult(RESULT_OK, intent);
//        finish();
    }

    public void backFromPreview() {
//        mCamera.stopPreview();// 关闭预览
//        fl_mask.setVisibility(View.GONE);//拦截后续事件

    }

    @Override
    public void onFileSaved(String imgPath) {
        Toast.makeText(MainBackUpActivity.this, "图片地址：" + imgPath, Toast.LENGTH_SHORT).show();
        Glide.with(MainBackUpActivity.this).load(imgPath).into(iv_content);
    }
}
