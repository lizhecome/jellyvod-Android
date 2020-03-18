/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.alivcsolution;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.aliyun.apsara.alivclittlevideo.constants.AlivcLittleServerApiConstants;
import com.aliyun.apsara.alivclittlevideo.net.LittleHttpManager;
import com.aliyun.apsara.alivclittlevideo.sts.OnStsResultListener;
import com.aliyun.apsara.alivclittlevideo.sts.StsInfoManager;
import com.aliyun.apsara.alivclittlevideo.sts.StsTokenInfo;
import com.aliyun.apsara.alivclittlevideo.view.mine.AlivcLittleUserManager;
import com.aliyun.svideo.common.utils.ToastUtils;
import com.aliyun.svideo.common.widget.AlivcCustomAlertDialog;
//import com.aliyun.vodplayer.media.AliyunVodPlayer;

import java.lang.ref.WeakReference;

/**
 * @author Mulberry
 */
public class SplashActivity extends Activity {
    private static final String LOG_TAG = "AlivcQuVideo";
    /**
     * 动画时间 2000ms
     */
    private static final int ANIMATOR_DURATION = 2000;

    /**
     * 动画样式-- 透明度动画
     */
    private static final String ANIMATOR_STYLE = "alpha";

    /**
     * 动画起始值
     */
    private static final float ANIMATOR_VALUE_START = 0f;

    /**
     * 动画结束值
     */
    private static final float ANIMATOR_VALUE_END = 1f;
    private ObjectAnimator alphaAnimIn;
    private AlivcCustomAlertDialog errorUserTips;
    /**
     * 用户信息是否初始化成功
     */
    private boolean isUserInitSuccess;
    /**
     * sts信息是否初始化成功
     */
    private boolean isStsInitSuccess;
    /**
     * 开始动画是否结束
     */
    private boolean isAniminEnd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //todo 排查错误
        //        requestWindowFeature(Window.FEATURE_NO_TITLE);
        initUserInfo();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_spalash);
        LinearLayout splashView = findViewById(R.id.splash_view);

        alphaAnimIn = ObjectAnimator.ofFloat(splashView, ANIMATOR_STYLE, ANIMATOR_VALUE_START, ANIMATOR_VALUE_END);

        alphaAnimIn.setDuration(ANIMATOR_DURATION);

        alphaAnimIn.start();
        alphaAnimIn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isAniminEnd = true;
                tryJumpToMain();

            }
        });

        Log.d(LOG_TAG, "播放器SDK版本号: " + ""
              + "\n 短视频SDK版本号: " + com.aliyun.common.global.Version.VERSION
              + "\n 短视频SDK BUILD_ID :" + com.aliyun.common.global.Version.BUILD_ID
              + "\n 短视频SDK SRC_COMMIT_ID: " + com.aliyun.common.global.Version.SRC_COMMIT_ID
              + "\n 短视频SDK ALIVC_COMMIT_ID: " + com.aliyun.common.global.Version.ALIVC_COMMIT_ID
              + "\n 短视频SDK ANDROID_COMMIT_ID: " + com.aliyun.common.global.Version.ANDROID_COMMIT_ID
             );
    }


    /**
     * 初始化sts信息
     */
    private void initStsInfo() {
        StsInfoManager.getInstance().refreshStsToken(new OnStsResultListener() {
            @Override
            public void onSuccess(StsTokenInfo tokenInfo) {
                isStsInitSuccess = true;
                tryJumpToMain();
            }

            @Override
            public void onFail() {
                isStsInitSuccess = true;
                tryJumpToMain();
                ToastUtils.show(SplashActivity.this, "获取sts信息失败");
            }
        });
    }

    /**
     * 初始化用户信息
     */
    private void initUserInfo() {
        AlivcLittleUserManager.getInstance().setmRequestInitUserCallback(
        new AlivcLittleUserManager.OnRequestInitUserCallback() {
            @Override
            public void onSuccess() {
                isUserInitSuccess = true;
                initStsInfo();
            }

            @Override
            public void onFailure(final String errorMsg) {
                isUserInitSuccess = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showUserInfoErrorTip(errorMsg);
                    }
                });

            }
        });
        AlivcLittleUserManager.getInstance().init(this);

    }
    WeakReference<SplashActivity> weakReference = new WeakReference<>(this);
    private void showUserInfoErrorTip(String errorMsg) {

        SplashActivity splashActivity = weakReference.get();
        if (splashActivity == null) {
            return;
        }
        AlivcCustomAlertDialog errorTips = new AlivcCustomAlertDialog.Builder(splashActivity)
        .setMessage("获取用户信息失败 \n 错误信息: " + errorMsg)
        .setDialogClickListener("重试", "关闭", new AlivcCustomAlertDialog.OnDialogClickListener() {
            @Override
            public void onConfirm() {
                initUserInfo();
            }

            @Override
            public void onCancel() {
                finish();
            }
        })
        .create();
        errorTips.setCanceledOnTouchOutside(false);
        errorTips.setCancelable(false);
        errorTips.show();
    }
    private void showStsInfoErrorTip() {

        SplashActivity splashActivity = weakReference.get();
        if (splashActivity == null) {
            return;
        }
        AlivcCustomAlertDialog errorTips = new AlivcCustomAlertDialog.Builder(splashActivity)
        .setMessage("获取Sts信息失败")
        .setDialogClickListener("重试", "关闭", new AlivcCustomAlertDialog.OnDialogClickListener() {
            @Override
            public void onConfirm() {
                initStsInfo();
            }

            @Override
            public void onCancel() {
                finish();
            }
        })
        .create();
        errorTips.setCanceledOnTouchOutside(false);
        errorTips.setCancelable(false);
        errorTips.show();
    }
    /**
     * 尝试跳转到主界面
     */
    private void tryJumpToMain() {
        Log.e("SplashActivity", "isStsInitSuccess" + isStsInitSuccess + "isUserInitSuccess" + isUserInitSuccess + "isAniminEnd" + isAniminEnd);
        if (isStsInitSuccess && isUserInitSuccess && isAniminEnd) {
            Intent intent = new Intent();
            intent.setClassName(SplashActivity.this, "com.aliyun.apsara.alivclittlevideo.activity.VideoListActivity");
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.splash_fade_in, R.anim.splash_fade_out);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (alphaAnimIn != null) {

            alphaAnimIn.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alphaAnimIn != null) {
            alphaAnimIn.cancel();
            alphaAnimIn.removeAllListeners();
            alphaAnimIn = null;
        }

        if (errorUserTips != null) {
            errorUserTips.dismiss();
        }

        LittleHttpManager.getInstance().cancelRequest(AlivcLittleServerApiConstants.URL_NEW_GUEST);
        AlivcLittleUserManager.getInstance().setmRequestInitUserCallback(null);
        if (weakReference != null) {
            weakReference.clear();
            weakReference = null;
        }

    }
}
