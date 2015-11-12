package com.dudu.android.launcher.ui.activity.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import com.dudu.android.launcher.utils.ActivitiesManager;
import com.dudu.android.launcher.utils.DensityUtil;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnTouchListener;

public abstract class BaseActivity extends Activity implements OnGestureListener, OnTouchListener {

    protected Context mContext;
    protected Activity mActivity;

    private GestureDetector gestureDetector;
    private AudioManager audiomanager;
    private int maxVolume, currentVolume;
    private int maxBrightness, currentBrightness;
    private static final float STEP_BRIGHTNESS = 2f;// 协调亮度滑动时的步长，避免每次滑动都改变，导致改变过快
    private static final float STEP_VOLUME = 2f;// 协调音量滑动时的步长，避免每次滑动都改变，导致改变过快
    private boolean firstScroll = false;// 每次触摸屏幕后，第一次scroll的标志
    private int GESTURE_FLAG = 0;// 1，调节音量 2，调节亮度
    private static final int GESTURE_MODIFY_VOLUME = 1;
    private static final int GESTURE_MODIFY_BRIGHTNESS = 2;
    int width;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(initContentView());

        ActivitiesManager.getInstance().addActivity(this);

        initView(savedInstanceState);

        initDatas();

        initListener();


        gestureDetector = new GestureDetector(this, this);
        gestureDetector.setIsLongpressEnabled(true);
        audiomanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audiomanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 获取系统最大音量
        currentVolume = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
        maxBrightness = 255;
        currentBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 255);//取得当前亮度
        if (currentBrightness < 80) {
            currentBrightness = 80;
        }
        WindowManager wm = this.getWindowManager();
        width = wm.getDefaultDisplay().getWidth();
        Context mContext = this;
        mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS), true, mBrightnessObserver);
    }

    public abstract int initContentView();

    public abstract void initView(Bundle savedInstanceState);

    public abstract void initListener();

    public abstract void initDatas();

    public Context getContext() {
        return mContext;
    }

    public Activity getActivity() {
        return mActivity;
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
        this.mActivity = (Activity) mContext;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivitiesManager.getInstance().removeActivity(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (touchX > (float) width / 2) {
                GESTURE_FLAG = GESTURE_MODIFY_VOLUME;
            } else {
                GESTURE_FLAG = GESTURE_MODIFY_BRIGHTNESS;
            }
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            GESTURE_FLAG = 0;// 手指离开屏幕后，重置调节音量或进度的标志
        }
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float touchX = event.getX();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (touchX > (float) width / 2) {
                GESTURE_FLAG = GESTURE_MODIFY_VOLUME;
            } else {
                GESTURE_FLAG = GESTURE_MODIFY_BRIGHTNESS;
            }
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            GESTURE_FLAG = 0;// 手指离开屏幕后，重置调节音量或进度的标志
        }
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        firstScroll = true;// 设定是触摸屏幕后第一次scroll的标志
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // 如果每次触摸屏幕后第一次scroll是调节音量，那之后的scroll事件都处理音量调节，直到离开屏幕执行下一次操作
        if (GESTURE_FLAG == GESTURE_MODIFY_VOLUME) {
            currentVolume = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
            if (Math.abs(distanceY) > Math.abs(distanceX)) {
// 纵向移动大于横向移动
                if (distanceY >= DensityUtil.dip2px(this, STEP_VOLUME)) {
// 音量调大,注意横屏时的坐标体系,尽管左上角是原点，但横向向上滑动时distanceY为正
                    if (currentVolume < maxVolume) {
// 为避免调节过快，distanceY应大于一个设定值
                        currentVolume++;
                    }
                } else if (distanceY <= -DensityUtil.dip2px(this, STEP_VOLUME)) {
// 音量调小
                    if (currentVolume > 0) {
                        currentVolume--;
                        if (currentVolume <= 0) {
                        }
                    }
                }
                Log.i("lcc", "currentVolume===" + currentVolume);
                Log.i("lcc", "maxVolume===" + maxVolume);
                int percentage = (currentVolume * 100) / maxVolume;
                audiomanager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_SHOW_UI);
            }
        } else if (GESTURE_FLAG == GESTURE_MODIFY_BRIGHTNESS) {
            currentBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 255);//取得当前亮度
            if (Math.abs(distanceY) > Math.abs(distanceX)) {
// 纵向移动大于横向移动
                if (distanceY >= DensityUtil.dip2px(this, STEP_BRIGHTNESS)) {
// 亮度调大,注意横屏时的坐标体系,尽管左上角是原点，但横向向上滑动时distanceY为正
                    if (currentBrightness < maxBrightness) {
// 为避免调节过快，distanceY应大于一个设定值
                        currentBrightness = currentBrightness + 17;
                        if (currentBrightness > maxBrightness) {
                            currentBrightness = maxBrightness;
                        }
                    }
                } else if (distanceY <= -DensityUtil.dip2px(this, STEP_BRIGHTNESS)) {
// 亮度调小
                    if (currentBrightness >= 17) {

                        currentBrightness = currentBrightness - 17;
                        if (currentBrightness < 17) {
                            currentBrightness = 0;
                        }
                        if (currentBrightness == 0) {
                        }
                    }
                }
                int percentage = (currentBrightness * 100) / maxBrightness;
// 根据当前进度改变亮度
                Settings.System.putInt(getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS, currentBrightness);
                currentBrightness = Settings.System.getInt(getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS, -1);
                WindowManager.LayoutParams wl = getWindow().getAttributes();

                float tmpFloat = (float) currentBrightness / 255;
                if (tmpFloat > 0 && tmpFloat <= 1) {
                    wl.screenBrightness = tmpFloat;
                }
                getWindow().setAttributes(wl);

            }

        }

        firstScroll = false;
        return false;

    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    /**
     * 处理音量变化时的界面显示
     */
    private void myRegisterReceiver() {
        MyVolumeReceiver mVolumeReceiver = new MyVolumeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.media.VOLUME_CHANGED_ACTION");
        registerReceiver(mVolumeReceiver, filter);
    }

    private class MyVolumeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
                int currVolume = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC);// 当前的媒体音量
                int percentage = (currVolume * 100) / maxVolume;

            }
        }
    }

    /**
     * 处理亮度变化时的界面显示
     */
    private ContentObserver mBrightnessObserver = new ContentObserver(new Handler()) {


        @Override
        public void onChange(boolean selfChange) {

            currentBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
            int percentage = (currentBrightness * 100) / maxBrightness;
// 根据当前进度改变亮度
            WindowManager.LayoutParams wl = getWindow().getAttributes();
            float tmpFloat = (float) currentBrightness / 255;
            if (tmpFloat > 0 && tmpFloat <= 1) {
                wl.screenBrightness = tmpFloat;
            }
            getWindow().setAttributes(wl);
        }
    };

}
