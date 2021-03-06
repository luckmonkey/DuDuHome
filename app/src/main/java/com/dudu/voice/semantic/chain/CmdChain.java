package com.dudu.voice.semantic.chain;

import android.app.Activity;
import android.content.Intent;

import com.dudu.aios.ui.activity.CarCheckingActivity;
import com.dudu.aios.ui.activity.MainRecordActivity;
import com.dudu.aios.ui.activity.VehicleAnimationActivity;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.android.launcher.LauncherApplication;
import com.dudu.android.launcher.utils.ActivitiesManager;
import com.dudu.android.launcher.utils.Constants;
import com.dudu.android.launcher.utils.WifiApAdmin;
import com.dudu.commonlib.CommonLib;
import com.dudu.drivevideo.DriveVideo;
import com.dudu.map.NavigationProxy;
import com.dudu.voice.FloatWindowUtils;
import com.dudu.voice.semantic.bean.CmdBean;
import com.dudu.voice.semantic.bean.SemanticBean;
import com.dudu.voice.semantic.constant.SceneType;
import com.dudu.voice.semantic.constant.SemanticConstant;
import com.dudu.voice.semantic.constant.TTSType;
import com.dudu.voice.semantic.engine.SemanticEngine;

/**
 * Created by 赵圣琪 on 2015/10/29.
 */
public class CmdChain extends SemanticChain {

    @Override
    public boolean matchSemantic(String service) {
        return SemanticConstant.SERVICE_CMD.equals(service);
    }

    @Override
    public boolean doSemantic(SemanticBean semantic) {
        return handleCmd((CmdBean) semantic);
    }

    private boolean handleCmd(CmdBean bean) {
        String action = bean.getAction();
        String target = bean.getTarget();


        if (target == null) {

        } else {
            if (target.contains(Constants.NAVIGATION)) {
                return handleNavigationCmd(action);
            } else if (target.contains(SemanticConstant.RECORD_CN)) {
                handleVideoCmd(action);
                return true;
            } else if (target.equals(Constants.WIFI)) {
                handleWifi(action);
                return true;
            } else if (target.contains(Constants.SPEECH)) {
                handleExitCmd();
                return true;
            } else if (target.contains(Constants.EXIT)) {
                handleExitCmd();
                return true;
            } else if (target.contains(Constants.BACK)) {
                handleBackCmd();
                return true;
            } else if (target.contains(Constants.MAP)) {
                return handleMapCmd(action);
            } else if (target.contains(Constants.SELF_CHECKING)) {
                return handleSelfChecking(action);
            }
        }

        return false;
    }

    private boolean handleNavigationCmd(String option) {
        switch (option) {
            case Constants.OPEN:
            case Constants.START:
                return NavigationProxy.getInstance().openNavi(NavigationProxy.OPEN_VOICE);
            case Constants.CLOSE:
            case Constants.EXIT:
                FloatWindowUtils.removeFloatWindow();
                NavigationProxy.getInstance().existNavi();
                break;
        }
        return true;
    }

    private void handleVideoCmd(String option) {
        FloatWindowUtils.removeFloatWindow();
        switch (option) {
            case Constants.OPEN:
            case Constants.QIDONG:
            case Constants.KAIQI:
//                Intent intent = new Intent();
//                intent.setClass(mContext, VideoActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                mContext.startActivity(intent);

                toMainRecord();

                break;
            case Constants.CLOSE:
            case Constants.EXIT:
            case Constants.GUANDIAO:
//                toMainRecord();
                try {
                    ((MainRecordActivity) ActivitiesManager.getInstance().getTopActivity()).replaceFragment(FragmentConstants.FRAGMENT_MAIN_PAGE);
                    ((MainRecordActivity) ActivitiesManager.getInstance().getTopActivity()).setBlur();
                } catch (Exception e) {

                }
                DriveVideo.getInstance().stopDriveVideo();
                mVoiceManager.startSpeaking("录像预览已关闭", TTSType.TTS_DO_NOTHING, false);
                break;
        }
    }

    private void toMainRecord() {
        try {
            if (!(ActivitiesManager.getInstance().getTopActivity() instanceof MainRecordActivity)) {

                LauncherApplication.startRecord = true;
                Intent intent = new Intent();
                intent.setClass(mContext, MainRecordActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
            ((MainRecordActivity) ActivitiesManager.getInstance().getTopActivity()).replaceFragment(FragmentConstants.FRAGMENT_DRIVING_RECORD);
        } catch (Exception e) {

        }

    }

    private void handleBackCmd() {
        FloatWindowUtils.removeFloatWindow();

        Activity topActivity = ActivitiesManager.getInstance()
                .getTopActivity();
        if (topActivity != null && !(topActivity instanceof MainRecordActivity)) {
            Intent intent = new Intent(topActivity,
                    MainRecordActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            topActivity.startActivity(intent);
        }
    }

    private void handleExitCmd() {
        FloatWindowUtils.removeFloatWindow();
        SemanticEngine.getProcessor().clearSemanticStack();

        ActivitiesManager.getInstance().closeTargetActivity(VehicleAnimationActivity.class);

        SemanticEngine.getProcessor().switchSemanticType(SceneType.HOME);
    }

    private boolean handleMapCmd(String option) {
        FloatWindowUtils.removeFloatWindow();
        switch (option) {
            case Constants.OPEN:
            case Constants.START:
            case Constants.KAIQI:
                if (NavigationProxy.getInstance().openNavi(NavigationProxy.OPEN_MAP)) {
                    FloatWindowUtils.removeFloatWindow();
                    return true;
                }
                return false;
            case Constants.CLOSE:
            case Constants.EXIT:
                NavigationProxy.getInstance().closeMap();
                break;
        }
        return true;
    }

    private boolean handleSelfChecking(String action) {
        switch (action) {
            case Constants.OPEN:
            case Constants.QIDONG:
            case Constants.KAIQI:
                FloatWindowUtils.removeFloatWindow();

                Intent intent = new Intent(CommonLib.getInstance().getContext(), CarCheckingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                CommonLib.getInstance().getContext().startActivity(intent);
                break;
            case Constants.CLOSE:
            case Constants.EXIT:
            case Constants.GUANDIAO:
                FloatWindowUtils.removeFloatWindow();

                ActivitiesManager.getInstance().closeTargetActivity(CarCheckingActivity.class);
                break;
            default:
                return false;
        }

        return true;
    }

    private void handleWifi(String option) {
        switch (option) {
            case Constants.OPEN:
                WifiApAdmin.startWifiAp(mContext);
                mVoiceManager.startSpeaking("Wifi热点已打开", TTSType.TTS_START_UNDERSTANDING, true);
                break;
            case Constants.CLOSE:
                WifiApAdmin.startWifiAp(mContext);
                mVoiceManager.startSpeaking("Wifi热点已关闭", TTSType.TTS_START_UNDERSTANDING, true);
                break;
        }
    }

}
