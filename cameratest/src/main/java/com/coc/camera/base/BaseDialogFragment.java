package com.coc.camera.base;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;



/**
 * Created by tang on 2017/3/6.
 * 基础的自定义DialogFragment
 * 相比DialogFragment不同处：
 * 1.提供了 宽度 大小的快速设置方法
 * ------1.自定义宽度(默认 80%)；2.wrap宽度；3.match
 * 2.提供了是否显示遮罩
 * 3.提供了是否可取消：返回键和遮罩
 * 4.提供了设置 cancellistener监听 cancle事件
 * 5.除去了title问题 ；以及圆角出现白边的问题
 */
public abstract class BaseDialogFragment extends DialogFragment {

    public static final int WIDTH_CUSTOM = 0;//默认0.9
    public static final int WIDTH_WRAP = 1;
    public static final int WIDTH_MATCH = 2;
    protected onBackPressedCancelListener mCancelListener;//dialog取消监听器


    //用于控制屏幕宽度 默认90%的宽度   子类可以覆写
    protected boolean mCancelable = true;// 默认可以取消 (是否可以取消)
    protected boolean mTouchOutCancelable = true;//默认可以取消 (触摸对话框外部)
    protected boolean mMask = true;//是否需要遮罩 默认有
    protected int mWidthMode = 0;
    protected float mWidthPercent = 0.9f;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCancelable = savedInstanceState.getBoolean("mCancelable", true);
            mTouchOutCancelable = savedInstanceState.getBoolean("mTouchOutCancelable", true);
            mMask = savedInstanceState.getBoolean("mMask", true);
            mWidthMode = savedInstanceState.getInt("mWidthMode");
            mWidthPercent = savedInstanceState.getFloat("mWidthPercent");
            Log.e("save", "restoreFieldStateFromSaved");
            restoreFieldStateFromSaved(savedInstanceState);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            if (getDialog().getWindow() != null)
                getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//圆角去白边
        }

        View rootView = realOnCreateView(inflater, container, savedInstanceState);
        setDialogCancelable();//设置是否可取消
        setSomeEventListener();

        return rootView;
    }

    /**
     * 三种宽度设置
     * 1.matchParent
     * 2.wrapContent
     * 3.指定宽度 默认0.9
     */
    @Override
    public void onStart() {
        super.onStart();
        setDialogWidth();//设置宽度
        setDialogMaskable();//设置遮罩
    }

    @Override
    public void onDestroy() {
        doSomeThingonDestroy();
        super.onDestroy();
    }

    //TODO:尽快完成相关类的变更，随后将会 设置为抽象方法
//    protected abstract void saveFieldStateForReCreate(Bundle outState);
    protected void saveFieldStateForReCreate(Bundle outState) {
    }


    //TODO:尽快完成相关类的变更，随后将会 设置为抽象方法
//    protected abstract void restoreFieldStateFromSaved(Bundle savedInstanceState);
    protected void restoreFieldStateFromSaved(Bundle savedInstanceState) {
    }


    //务必进行onSaveInstanceState 和onActivityCreated 的保存变量
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.putBoolean("mCancelable", mCancelable);
            outState.putBoolean("mTouchOutCancelable", mTouchOutCancelable);
            outState.putBoolean("mMask", mMask);
            outState.putInt("mWidthMode", mWidthMode);
            outState.putFloat("mWidthPercent", mWidthPercent);

            Log.e("save", "saveFieldStateForReCreate");
            saveFieldStateForReCreate(outState);
        }
    }


    private void setSomeEventListener() {
        Dialog dialog = getDialog();
        if (dialog == null) return;
        this.getDialog().setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {

                //仅在调用对应时间的时候才 消费 back事件
                if (keyCode == KeyEvent.KEYCODE_BACK && mCancelable && mCancelListener != null) {
                    mCancelListener.onBackPressedCancel();
                    return true;
                }
                return false;
            }
        });
    }


    public abstract View realOnCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);


    private void setDialogCancelable() {
        setCancelable(mCancelable);
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setCanceledOnTouchOutside(mTouchOutCancelable);
        }
    }


    protected void doSomeThingonDestroy() {
    }


    private void setDialogWidth() {
        Dialog dialog = getDialog();
        if (dialog == null || dialog.getWindow() == null) return;

        if (mWidthMode == WIDTH_CUSTOM) {
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            dialog.getWindow().setLayout((int) (dm.widthPixels * mWidthPercent), ViewGroup.LayoutParams.WRAP_CONTENT);
        } else if (mWidthMode == WIDTH_WRAP) {
            int width = ViewGroup.LayoutParams.WRAP_CONTENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        } else if (mWidthMode == WIDTH_MATCH) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }


    private void setDialogMaskable() {
        if (mMask) return;
        //设置对话框外部颜色为透明
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.dimAmount = 0.0f;
        window.setAttributes(windowParams);
    }


    public BaseDialogFragment setWithdMode(int widthMode) {
        return setWithdMode(widthMode, 0.9f);
    }


    public BaseDialogFragment setWithdMode(int widthMode, float widthpercent) {
        //可能成为libary 这里使用if else代替 switch
        if (widthMode == WIDTH_WRAP) {
            mWidthMode = WIDTH_WRAP;
        } else if (widthMode == WIDTH_MATCH) {
            mWidthMode = WIDTH_MATCH;
        } else {
            mWidthMode = WIDTH_CUSTOM;
            if (widthpercent < 0) widthpercent = 0;
            if (widthpercent > 1) widthpercent = 1;
            mWidthPercent = widthpercent;
        }
        return this;
    }


    /**
     * 对外方法
     * 设置dialog取消监听器
     */
    public BaseDialogFragment setOnBackPressedCancelListener(onBackPressedCancelListener cancelListener) {
        this.mCancelListener = cancelListener;
        return this;
    }


    /**
     * 设置是否可取消
     *
     * @param cancelable             DialogFragment是否可以取消
     * @param touchOutCancelableable 触摸对话框外可取消(仅在cancel为true有效，cancel为false，无论如何也是false)
     */
    public BaseDialogFragment setCancelable(boolean cancelable, boolean touchOutCancelableable) {
        this.mCancelable = cancelable;
        this.mTouchOutCancelable = touchOutCancelableable;
        return this;
    }


    /**
     * 设置是否显示遮罩
     */
    public BaseDialogFragment setMaskable(boolean maskable) {
        this.mMask = maskable;
        return this;
    }


    public void dismissSafe() {
        if (getFragmentManager() != null) {
            //super.dismiss();
            super.dismissAllowingStateLoss();
        }
    }


    public void showSafe(FragmentManager fragmentManager, String tag) {
        if (fragmentManager != null && !isAdded()) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(this, "loading");
            transaction.commitAllowingStateLoss();
        }
    }


    public interface onBackPressedCancelListener {
        void onBackPressedCancel();
    }


}