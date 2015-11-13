package com.dudu.android.launcher.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;


public class WifiApAdmin {

    public static final String TAG = "WifiApAdmin";

    public static final String KEY_WIFI_AP_STATE = "wifi_ap_state";
    public static final String KEY_WIFI_AP_SSID = "wifi_ssid";
    public static final String KEY_WIFI_AP_PASSWORD = "wifi_password";

    public static final String DEFAULT_SSID = "DuduHotSpot";
    public static final String DEFAULT_PASSWORD = "88888888";

    private static final String WIFI_CONF_DIRECTORY = "nodogsplash";
    private static final String WIFI_CONF_NAME = "nodogsplash.conf";

    private static WifiManager mWifiManager = null;

    /**
     * 设置WIFI热点密码
     *
     * @param password
     */
    public static boolean setWifiApPassword(Context context, String password) {
        SharedPreferencesUtil.putStringValue(context, KEY_WIFI_AP_PASSWORD, password);
        return startWifiAp(context, SharedPreferencesUtil.getStringValue(context,
                KEY_WIFI_AP_SSID, DEFAULT_SSID), password, null);
    }

    private static boolean checkWifiConfigFile(Context context) {
        File dir = new File(FileUtils.getExternalStorageDirectory(), WIFI_CONF_DIRECTORY);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, WIFI_CONF_NAME);
        if(!file.exists()){
            try {
                file.createNewFile();

                InputStream isAsset = context.getAssets().open(WIFI_CONF_NAME);

                FileUtils.copyFileToSd(isAsset, file);
            } catch (IOException e) {
                LogUtils.e(TAG, e.getMessage() + "");
                return false;
            }
        }

        return true;
    }

    public static boolean initWifiApState(Context context) {
        if (!checkWifiConfigFile(context)) {
            return false;
        }

        String ssid = SharedPreferencesUtil.getStringValue(context, KEY_WIFI_AP_SSID, DEFAULT_SSID);
        String password = SharedPreferencesUtil.getStringValue(context, KEY_WIFI_AP_PASSWORD, DEFAULT_PASSWORD);
        if (Util.isTaxiVersion()) {
            startWifiAp(context, DEFAULT_SSID, "", null);
        } else {
            if (SharedPreferencesUtil.getBooleanValue(context, KEY_WIFI_AP_STATE, false)) {
                startWifiAp(context, ssid, password, null);
            }
        }

        return startWifiAp(context, ssid, password, null);
    }

    public static boolean startWifiAp(Context context) {
        String ssid = SharedPreferencesUtil.getStringValue(context, KEY_WIFI_AP_SSID, DEFAULT_SSID);
        String password = SharedPreferencesUtil.getStringValue(context, KEY_WIFI_AP_PASSWORD, DEFAULT_PASSWORD);
        return startWifiAp(context, ssid, password, null);
    }

    public static boolean startWifiAp(Context context, String ssid,
                                      String password, WifiSettingStateCallback callback) {
        mWifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);

        closeWifiAp(context);

        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }

        if (!startWifiAp(ssid, password)) {
            return false;
        }

        SharedPreferencesUtil.putBooleanValue(context, KEY_WIFI_AP_STATE, true);

        if (callback != null) {
            callback.onWifiStateChanged(true);
        }

        return true;
    }

    private static boolean startWifiAp(String ssid, String password) {
        Method method;
        try {
            method = mWifiManager.getClass().getMethod("setWifiApEnabled",
                    WifiConfiguration.class, boolean.class);
            WifiConfiguration netConfig = new WifiConfiguration();
            netConfig.SSID = ssid;
            if (DEFAULT_PASSWORD.isEmpty()) {
                netConfig.allowedAuthAlgorithms.clear();
                netConfig.allowedGroupCiphers.clear();
                netConfig.allowedKeyManagement.clear();
                netConfig.allowedPairwiseCiphers.clear();
                netConfig.allowedProtocols.clear();
                netConfig.wepKeys[0] = "";
                netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                netConfig.wepTxKeyIndex = 0;
            } else {
                netConfig.preSharedKey = password;
                netConfig.allowedAuthAlgorithms
                        .set(WifiConfiguration.AuthAlgorithm.OPEN);
                netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                netConfig.allowedKeyManagement
                        .set(WifiConfiguration.KeyMgmt.WPA_PSK);
                netConfig.allowedPairwiseCiphers
                        .set(WifiConfiguration.PairwiseCipher.CCMP);
                netConfig.allowedPairwiseCiphers
                        .set(WifiConfiguration.PairwiseCipher.TKIP);
                netConfig.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.CCMP);
                netConfig.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.TKIP);
            }

            return (Boolean) method.invoke(mWifiManager, netConfig, true);
        } catch (Exception e) {
            LogUtils.e(TAG, e.getMessage() + "");
        }

        return false;
    }

    public static boolean closeWifiAp(Context context) {
        if (isWifiApEnabled(context)) {
            mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            try {
                Method method = mWifiManager.getClass().getMethod(
                        "getWifiApConfiguration");
                method.setAccessible(true);

                WifiConfiguration config = (WifiConfiguration) method
                        .invoke(mWifiManager);

                Method method2 = mWifiManager.getClass().getMethod(
                        "setWifiApEnabled", WifiConfiguration.class,
                        boolean.class);

                boolean isClosed = (Boolean) method2.invoke(mWifiManager, config, false);
                if (isClosed) {
                    SharedPreferencesUtil.putBooleanValue(context, KEY_WIFI_AP_STATE, false);
                }

                return isClosed;
            } catch (Exception e) {
                LogUtils.e(TAG, e.getMessage() + "");
            }
        }

        return false;
    }

    public static boolean isWifiApEnabled(Context context) {
        try {
            if (mWifiManager == null) {
                mWifiManager = (WifiManager) context
                        .getSystemService(Context.WIFI_SERVICE);
            }
            Method method = mWifiManager.getClass()
                    .getMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(mWifiManager);
        } catch (Exception e) {
            LogUtils.e(TAG, e.getMessage() + "");
        }

        return false;
    }

    /**
     * wifi 密码设置后的回调
     */
    public interface WifiSettingStateCallback {
        void onWifiStateChanged(boolean open);
    }

}
