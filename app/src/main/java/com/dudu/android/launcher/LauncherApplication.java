package com.dudu.android.launcher;

import android.app.Application;
import android.os.StrictMode;
import android.support.multidex.MultiDex;

import com.dudu.android.launcher.broadcast.ReceiverRegister;
import com.dudu.android.launcher.exception.CrashHandler;
import com.dudu.android.launcher.utils.Constants;
import com.dudu.android.launcher.utils.NetworkUtils;
import com.dudu.commonlib.CommonLib;
import com.dudu.rest.common.Request;
import com.dudu.workflow.common.CommonParams;
import com.dudu.workflow.common.DataFlowFactory;
import com.dudu.workflow.common.ObservableFactory;
import com.dudu.workflow.common.RequestFactory;
import com.dudu.workflow.obd.OBDStream;
import com.dudu.workflow.robbery.RobberyStateModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import de.greenrobot.event.EventBus;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LauncherApplication extends Application {

    public static LauncherApplication mApplication;

    private Logger logger;


    public static String lastFragment = "default";

    public static boolean startRecord = false;
    public static LauncherApplication getContext() {
        return mApplication;
    }

    @Override
    public void onCreate() {
        MultiDex.install(this);

        super.onCreate();
        mApplication = this;

        logger = LoggerFactory.getLogger("init.application");
        logger.debug("正在初始化application");

        CommonLib.getInstance().init(this);

        if (Constants.DEBUG_STRICT_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyDialog()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build());
        }

        CrashHandler crashHandler = CrashHandler.getInstance();

        // 注册crashHandler
        crashHandler.init(getApplicationContext());

        NetworkUtils.writePortalConfig(this);

        CommonLib.getInstance().init();
        DataFlowFactory.getInstance().init();
        try {
            OBDStream.getInstance().init();
        } catch (IOException e) {
            e.printStackTrace();
        }

        CommonParams.getInstance().init();
        ObservableFactory.init();
        Request.getInstance().init();
        RequestFactory.getInstance().init();

        ReceiverRegister.registPushManager(CommonParams.getInstance().getUserName());
        checkGunSwitch();
    }

    private void checkGunSwitch() {
        try {
            ObservableFactory.gun3Toggle()
                    .subscribeOn(Schedulers.newThread())
//                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aBoolean -> {
                    }
                            , throwable -> {
                        if (!(throwable instanceof TimeoutException)) {
                            logger.debug("Gun toggle fail, try again");
                            checkGunSwitch();
                        }
                    }
                            , () -> {
                        logger.debug("Gun toggle robbery, sync to app");
                        EventBus.getDefault().post(new RobberyStateModel(true));
                    });
        } catch (IOException e) {
            logger.error("gun3Toggle exception", e);
        }
    }
}

