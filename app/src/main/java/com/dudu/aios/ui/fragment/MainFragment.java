package com.dudu.aios.ui.fragment;

import android.content.Intent;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dudu.aios.ui.activity.CarCheckingActivity;
import com.dudu.aios.ui.fragment.base.BaseFragment;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.ui.activity.bluetooth.BtDialActivity;
import com.dudu.android.launcher.utils.WeatherUtils;
import com.dudu.map.NavigationProxy;
import com.dudu.voice.VoiceManagerProxy;
import com.dudu.weather.WeatherFlow;
import com.dudu.weather.WeatherInfo;
import com.dudu.weather.WeatherStream;
import com.dudu.workflow.obd.OBDStream;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class MainFragment extends BaseFragment implements View.OnClickListener {

    private LinearLayout vehicleInspection, drivingRecord, navigation, bluetoothPhone, flow, preventRob;

    private TextView mDateTextView, mWeatherView, mTemperatureView;

    private ImageView mWeatherImage;

    private ImageButton voice_imageBtn;

    private ImageButton iconVehicleInspection, iconDrivingRecord, iconNavigation, iconBt, iconFlow, iconRob;

    @Override
    public View getView() {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_main_layout, null);

        initFragmentView(view);

        initOnClickListener(view);

        initData();

        return view;
    }

    private void initData() {

        initDate();

        getWeather();
    }

    private void initDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy/MM/dd");
        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String mWay = String.valueOf(c.get(Calendar.DAY_OF_WEEK));
        if ("1".equals(mWay)) {
            mWay = "星期天";
        } else if ("2".equals(mWay)) {
            mWay = "星期一";
        } else if ("3".equals(mWay)) {
            mWay = "星期二";
        } else if ("4".equals(mWay)) {
            mWay = "星期三";
        } else if ("5".equals(mWay)) {
            mWay = "星期四";
        } else if ("6".equals(mWay)) {
            mWay = "星期五";
        } else if ("7".equals(mWay)) {
            mWay = "星期六";
        }

        mDateTextView.setText(dateFormat.format(new Date()) + " " + mWay);
    }

    private void getWeather() {
        weatherSubscriber(WeatherFlow.getInstance().requestWeather());
        WeatherStream.getInstance().startService();

    }

    private void weatherSubscriber(Observable<WeatherInfo> observable) {
        observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(weatherInfo -> {
                            updateWeatherInfo(weatherInfo.getWeather(), weatherInfo.getTemperature());
                            initDate();
                        },
                        throwable -> {
                        });
    }

    private void initOnClickListener(View view) {
        vehicleInspection.setOnClickListener(this);
        drivingRecord.setOnClickListener(this);
        navigation.setOnClickListener(this);
        bluetoothPhone.setOnClickListener(this);
        flow.setOnClickListener(this);
        preventRob.setOnClickListener(this);
        voice_imageBtn.setOnClickListener(this);

        iconVehicleInspection.setOnClickListener(this);
        iconDrivingRecord.setOnClickListener(this);
        iconNavigation.setOnClickListener(this);
        iconBt.setOnClickListener(this);
        iconFlow.setOnClickListener(this);
        iconRob.setOnClickListener(this);

        view.findViewById(R.id.vehicle_inspection_icon).setOnClickListener(this);
        view.findViewById(R.id.driving_record_icon).setOnClickListener(this);
        view.findViewById(R.id.navigation_icon).setOnClickListener(this);
        view.findViewById(R.id.bluetooth_phone_icon).setOnClickListener(this);
        view.findViewById(R.id.flow_icon).setOnClickListener(this);
        view.findViewById(R.id.prevent_rob_icon).setOnClickListener(this);

        mWeatherImage.setOnLongClickListener(v -> {
            try {
                OBDStream.getInstance().exec("ATSETVEHICLE=1");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        vehicleInspection.setEnabled(true);
        drivingRecord.setEnabled(true);
        navigation.setEnabled(true);
        bluetoothPhone.setEnabled(true);
        flow.setEnabled(true);
        preventRob.setEnabled(true);
        iconVehicleInspection.setEnabled(true);
        iconDrivingRecord.setEnabled(true);
        iconNavigation.setEnabled(true);
        iconBt.setEnabled(true);
        iconFlow.setEnabled(true);
        iconRob.setEnabled(true);
        voice_imageBtn.setEnabled(true);
    }

    private void initFragmentView(View view) {
        vehicleInspection = (LinearLayout) view.findViewById(R.id.vehicle_inspection);
        drivingRecord = (LinearLayout) view.findViewById(R.id.driving_record_button);
        navigation = (LinearLayout) view.findViewById(R.id.navigation_button);
        bluetoothPhone = (LinearLayout) view.findViewById(R.id.bluetooth_phone_button);
        flow = (LinearLayout) view.findViewById(R.id.flow_button);
        preventRob = (LinearLayout) view.findViewById(R.id.prevent_rob);
        mDateTextView = (TextView) view.findViewById(R.id.text_date);
        mTemperatureView = (TextView) view.findViewById(R.id.text_temperature);
        mWeatherView = (TextView) view.findViewById(R.id.text_weather);
        mWeatherImage = (ImageView) view.findViewById(R.id.weather_icon);
        voice_imageBtn = (ImageButton) view.findViewById(R.id.voice_imageBtn);

        iconVehicleInspection = (ImageButton) view.findViewById(R.id.vehicle_inspection_icon);
        iconDrivingRecord = (ImageButton) view.findViewById(R.id.driving_record_icon);
        iconNavigation = (ImageButton) view.findViewById(R.id.navigation_icon);
        iconBt = (ImageButton) view.findViewById(R.id.bluetooth_phone_icon);
        iconFlow = (ImageButton) view.findViewById(R.id.flow_icon);
        iconRob = (ImageButton) view.findViewById(R.id.prevent_rob_icon);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.vehicle_inspection:
            case R.id.vehicle_inspection_icon:
//                vehicleInspection.setEnabled(false);
//                iconVehicleInspection.setEnabled(false);
                startActivity(new Intent(getActivity(), CarCheckingActivity.class));

                break;

            case R.id.driving_record_button:
            case R.id.driving_record_icon:
//                drivingRecord.setEnabled(false);
//                iconDrivingRecord.setEnabled(false);
                replaceFragment(FragmentConstants.FRAGMENT_DRIVING_RECORD);

                break;

            case R.id.navigation_button:
            case R.id.navigation_icon:
//                navigation.setEnabled(false);
//                iconNavigation.setEnabled(false);
                NavigationProxy.getInstance().openNavi(NavigationProxy.OPEN_MANUAL);

                break;

            case R.id.bluetooth_phone_button:
            case R.id.bluetooth_phone_icon:
//                bluetoothPhone.setEnabled(false);
//                iconBt.setEnabled(false);
                startActivity(new Intent(getActivity(), BtDialActivity.class));

                break;

            case R.id.flow_button:
            case R.id.flow_icon:
//                flow.setEnabled(false);
//                iconFlow.setEnabled(false);
                replaceFragment(FragmentConstants.FRAGMENT_FLOW);

                break;

            case R.id.prevent_rob:
            case R.id.prevent_rob_icon:
//                preventRob.setEnabled(false);
//                iconRob.setEnabled(false);
                replaceFragment(FragmentConstants.FRAGMENT_VEHICLE_INSPECTION);

                break;

            case R.id.voice_imageBtn:
//                voice_imageBtn.setEnabled(false);
                VoiceManagerProxy.getInstance().startVoiceService();

                break;
        }
    }

    private void updateWeatherInfo(String weather, String temperature) {

        if (!TextUtils.isEmpty(weather) && !TextUtils.isEmpty(temperature)) {
            if (weather.contains("-")) {
                weather = weather
                        .replace("-", getString(R.string.weather_turn));
            }

            mTemperatureView.setText(temperature
                    + getString(R.string.temperature_degree));

            mWeatherView.setText(weather);
            mWeatherImage.setImageResource(WeatherUtils
                    .getWeatherIcon(WeatherUtils.getWeatherType(weather)));
        } else {
            //获取天气失败
            mWeatherView.setGravity(Gravity.CENTER);
            mWeatherView.setText(R.string.unkown_weather_info);
            mTemperatureView.setText("");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(getActivity());
    }

}
