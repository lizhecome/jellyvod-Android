package com.aliyun.alivcsolution;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.multidex.MultiDex;


import com.aliyun.apsara.alivclittlevideo.net.LittleHttpManager;
import com.aliyun.common.httpfinal.QupaiHttpFinal;
import com.aliyun.svideo.downloader.DownloaderManager;
import com.aliyun.private_service.PrivateService;
import com.aliyun.sys.AlivcSdkCore;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

/**
 * Created by Mulberry on 2018/2/24.
 */
public class MutiApplication extends Application {
    /**
     * 友盟数据统计key值
     */
    private static final String UM_APP_KEY = "5c6d17e1b465f523fb00040a";
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化播放器
        //AliVcMediaPlayer.init(getApplicationContext());
        //初始化内存检测
        
        initHttp();
        initDownLoader();
        //短视频sdk，暂时只支持api 18以上的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            AlivcSdkCore.register(getApplicationContext());
            AlivcSdkCore.setLogLevel(AlivcSdkCore.AlivcLogLevel.AlivcLogDebug);

        }
        /**
         * 注意: 即使您已经在AndroidManifest.xml中配置过appkey和channel值，也需要在App代码中调
         * 用初始化接口（如需要使用AndroidManifest.xml中配置好的appkey和channel值，
         * UMConfigure.init调用中appkey和channel参数请置为null）。
         */
        UMConfigure.init(this, UM_APP_KEY, "Aliyun", UMConfigure.DEVICE_TYPE_PHONE, null);
        // 选用AUTO页面采集模式
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
        LittleHttpManager.getInstance().init(this);
    }

    /**
     * 短视频需要的http依赖
     */
    private void initHttp() {
        QupaiHttpFinal.getInstance().initOkHttpFinal();
    }
    private void initDownLoader() {
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/aliyun/encryptedApp.dat";
        PrivateService.initService(this, filePath );
        DownloaderManager.getInstance().init(this);

    }


}
