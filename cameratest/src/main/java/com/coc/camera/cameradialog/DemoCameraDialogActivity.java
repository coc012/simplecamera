package com.coc.camera.cameradialog;

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

public class DemoCameraDialogActivity extends AppCompatActivity implements CameraDialg.FileSavedEventListener, CameraDialg2.FileSavedEventListener {


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


    //用户响应：去拍照
    public void takePic(View view) {
        Log.e("mainActivity", "tapFous");

        //添加权限检查 适配特殊定制机型
        CameraDialg2 cameraDialg = CameraDialg2.newInstance();
        if (cameraDialg != null) {
            //可以使用匿名内部类的样式进行结果回调，也可以是 容器实行相关接口；最好的使用EventBus进行解耦
//        cameradialg.setFileSavedEventListener(new CameraDialg.FileSavedEventListener() {
//            @Override
//            public void onFileSaved(String imgPath) {
//                Toast.makeText(DemoCameraDialogActivity.this, "图片地址：" + imgPath, Toast.LENGTH_SHORT).show();
//                Glide.with(DemoCameraDialogActivity.this).load(imgPath).into(iv_content);
//            }
//        });
            cameraDialg.showSafe(getSupportFragmentManager(), "camera");
        }

    }


    @Override
    public void onFileSaved(String imgPath) {
        Toast.makeText(DemoCameraDialogActivity.this, "图片地址：" + imgPath, Toast.LENGTH_SHORT).show();
        Glide.with(DemoCameraDialogActivity.this).load(imgPath).into(iv_content);
    }
}
