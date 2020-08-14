package com.eyuan.wmpf.screen;

import android.content.Context;
import android.support.multidex.MultiDex;

import com.tencent.mmkv.MMKV;
import com.tencent.wmpf.app.WMPFApplication;
import com.eyuan.wmpf.screen.utils.InvokeTokenHelper;

/**
 * For 4.4 multi dex support
 */
public class DemoApplication extends WMPFApplication {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);

        InvokeTokenHelper.INSTANCE.initInvokeToken(this);

        String rootDir = MMKV.initialize(this);
        System.out.println("mmkv root: " + rootDir);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        MMKV.onExit();
    }
}
