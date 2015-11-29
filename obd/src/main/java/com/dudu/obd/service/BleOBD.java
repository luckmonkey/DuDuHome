package com.dudu.obd.service;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import com.dudu.android.libble.BleConnectMain;
import com.dudu.moniter.DrivingEvent;
import com.dudu.obd.entity.FlamoutData;
import com.dudu.obd.entity.OBDData;
import com.dudu.obd.util.TimeUtils;
import com.dudu.obd.valueObject.CarStatus;

import org.scf4a.ConnSession;
import org.scf4a.Event;
import org.scf4a.EventRead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.functions.Action1;

public class BleOBD {
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mWriteChara;
    private BluetoothDevice mBluetoothDevice;
    private PrefixReadL1 readL1;
    private Logger log;
    private static final String REALTIME = "$OBD-RT";                  // 实时数据标志
    private static final String TOTALDATA = "$OBD-TT";                 // 统计数据标志
    private static final String FLAMOUT = "$OBD-ST";                   // 熄火数据标志

    private boolean isNotice_start = false;
    private boolean isNotice_flamout = false;

    private List<OBDData> obdCollectionList = new ArrayList<>(); // OBD 数据

    private FlamoutData flamoutData = null;

    private int speed = 0;

    private float revolution = 0;

    private float battery = 0;

    private  String[] rtData, stData,ttData;

    private int acc_spd, break_spd;

    private Context mContext;

    public BleOBD() {
        readL1 = new PrefixReadL1();
        log = LoggerFactory.getLogger("ble.odb.server");
    }

    public void initOBD(Context context) {
        log.debug("initOBD");
        ConnSession.getInstance();
        BleConnectMain.getInstance().init(context);
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().register(this);
        EventBus.getDefault().unregister(readL1);
        EventBus.getDefault().register(readL1);
        EventBus.getDefault().post(new Event.StartScanner());
        obdCollectionList = new ArrayList<OBDData>();

        mContext = context;
    }

    public void onEventMainThread(Event.BackScanResult event) {
        BluetoothDevice device = event.getDevice();
        log.debug("Try Connect {}[{}]", device.getName(), device.getAddress());
        EventBus.getDefault().post(new Event.Connect(device.getAddress(), Event.ConnectType.BLE, false));
    }


    public void onEventMainThread(Event.BLEInit event) {
        log.debug("ble BLEInit");
        mBluetoothGatt = event.getBluetoothGatt();
        mWriteChara = event.getWriteChara();

        mBluetoothDevice = event.getDevice();
        final String devAddr = mBluetoothDevice.getAddress();
    }

    public void onEventBackgroundThread(EventRead.L1ReadDone event) {
        final byte[] data = event.getData();

        try {
            log.debug("Receive OBD Data: = {}", new String(data, "UTF-8"));
            parseOBDData(new String(data, "UTF-8"));
        } catch (Exception e) {
            log.error("OBD Parse exception", e);
            e.printStackTrace();
        }
    }


    public void onEventBackgroundThread(Event.Disconnected event){
        log.debug("ble Disconnected");
        Observable.timer(10, TimeUnit.SECONDS)
                    .subscribe(new Action1<Long>() {
                        @Override
                        public void call(Long aLong) {
                            EventBus.getDefault().post(new Event.StartScanner());
                        }
                    });

        }

    
    public void onEventBackgroundThread(Event.BTConnected event){
        log.debug("ble BTConnected");
    }

    private void parseOBDData(String result){

        if(result.startsWith(REALTIME)){

            parseRealtimeData(result);

        }else if(result.startsWith(TOTALDATA)){
            parseTotalData(result);

        }else if(result.startsWith(FLAMOUT)){

            parseFlamoutData(result);
        }

    }


    private void parseRealtimeData(String result){

        rtData = result.split(",");

        speed = Integer.parseInt(rtData[2]);
        revolution = Float.parseFloat(rtData[1]);
        battery = Float.parseFloat(rtData[0].split("=")[1]);

        OBDData obdData = new OBDData();
        obdData.setSpd(speed);
        obdData.setBatteryV(battery);
        obdData.setEngSpd(revolution);
        obdData.setEngLoad(Float.parseFloat(rtData[4]));
        obdData.setCuron(Float.parseFloat(rtData[5]));
        obdData.setEngCoolant(Float.parseFloat(rtData[3]));
        obdData.setTime(TimeUtils.dateLongFormatString(
                System.currentTimeMillis(), TimeUtils.format1));
        obdData.setRunState(1);

        obdCollectionList.add(obdData);

        misMatch();

        if (!isNotice_start) {
            isNotice_flamout = false;
            isNotice_start = true;
           EventBus.getDefault().post(CarStatus.IGNITION);

        }

    }

    private void parseTotalData(String result){
        ttData = result.split(",");

        int acc = Integer.parseInt(new String(ttData[9]));
        if(acc > acc_spd){
        }
        acc_spd = acc;
        int b_spd = Integer.parseInt(new String(ttData[10].trim()));

        if(b_spd > break_spd){

        }
       break_spd = b_spd;
    }

    private void parseFlamoutData(String result){

        stData = result.split(",");
        flamoutData = new FlamoutData();
        flamoutData.setFuels(Float.parseFloat(stData[6]));
        flamoutData.setMiles(Float.parseFloat(stData[3]));
        flamoutData.setTimes(Integer.parseInt(stData[2]) * 60);
        flamoutData.setMaxrpm(Integer.parseInt(stData[8]));
        flamoutData.setMaxspd(Integer.parseInt(stData[7]));
        flamoutData.setCreateTime(TimeUtils.dateLongFormatString(
                System.currentTimeMillis(), TimeUtils.format1));
        flamoutData.setPower(0);


        if (!isNotice_flamout) {
            isNotice_start = false;
            isNotice_flamout = true;
            EventBus.getDefault().post(CarStatus.FLAMEOUT);
        }

    }

    public List<OBDData> getObdCollectionList(){
        return obdCollectionList;
    }

    public FlamoutData getFlamoutData(){

        return flamoutData;
    }

    // 转速不匹配判定
    private void misMatch() {
        boolean first = speed < 30 && revolution > 3000;
        boolean second = (speed < 60 && speed > 30) && revolution > 3500;
        boolean third = (speed < 90 && speed > 60) && revolution > 4000;
        boolean forth = (speed < 110 && speed > 90) && revolution > 4500;
        boolean five = (speed < 130 && speed > 110) && revolution > 5000;
        boolean six = (speed < 150 && speed > 130) && revolution > 5500;
        if (first || second || third || forth || five || six) {

            EventBus.getDefault().post(new DrivingEvent(DrivingEvent.TYPE_MISMATCH));
        }
    }



}