package com.coc.camera.camerakit;

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
import com.coc.camera.R;
import com.coc.camera.base.BaseDialogFragment;

public class CameraDialogTestActivity extends AppCompatActivity implements CameraDialg.FileSavedEventListener {


    public static final String KEY_IMG_PATH = "KEY_IMGPATH";
    private Button takePicBtn;
    private ImageView iv_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 全屏显示
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera_test);
        takePicBtn = (Button) findViewById(R.id.iv_preview_bottom_takePic);
        iv_content = (ImageView) findViewById(R.id.iv_content);

    }


    public void takePic(View view) {
        Log.e("mainActivity", "tapFous");
        CameraDialg cameradialg = new CameraDialg();
//        cameradialg.setFileSavedEventListener(new CameraDialg.FileSavedEventListener() {
//            @Override
//            public void onFileSaved(String imgPath) {
//                Toast.makeText(CameraDialogTestActivity.this, "图片地址：" + imgPath, Toast.LENGTH_SHORT).show();
//                Glide.with(CameraDialogTestActivity.this).load(imgPath).into(iv_content);
//            }
//        });
        cameradialg.setCancelable(true, false)
                .setMaskable(false)
                .setWithdMode(BaseDialogFragment.WIDTH_MATCH)
                .showSafe(getSupportFragmentManager(), "preview");
    }


    @Override
    public void onFileSaved(String imgPath) {
        Toast.makeText(CameraDialogTestActivity.this, "图片地址：" + imgPath, Toast.LENGTH_SHORT).show();
        Glide.with(CameraDialogTestActivity.this).load(imgPath).into(iv_content);
    }
}
