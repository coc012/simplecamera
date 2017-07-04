package com.coc.camera.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coc.camera.R;


/**
 * Created by tang on 2016/12/29.
 * 功能:一个简易的loadingDialog
 * 可访问方法:
 * 1.设置提示文字:SetContentStr(String content)
 */
public class LoadingDialog extends BaseDialogFragment {

    private String str_Content;


    @Override
    public View realOnCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.comm_func_dialog_loading_normal, container, false);
        initView(view);//配置布局界面
        return view;
    }


    private void initView(View view) {
        TextView tv_loading = (TextView) view.findViewById(R.id.tv_loading);
        if (TextUtils.isEmpty(str_Content)) {
            tv_loading.setText(String.valueOf("请稍后"));
        } else {
            tv_loading.setText(String.valueOf(str_Content));
        }
    }


    @Override
    protected void doSomeThingonDestroy() {
    }


    public LoadingDialog SetContentStr(String content) {
        this.str_Content = content;
        return this;
    }
}
