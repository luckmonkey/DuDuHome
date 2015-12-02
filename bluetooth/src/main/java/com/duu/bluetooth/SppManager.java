package com.duu.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import org.scf4a.Event;
import org.scf4a.EventRead;
import org.scf4a.EventWrite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import de.greenrobot.event.EventBus;

/**
 * 通过蓝牙连接的BluetoothService
 *
 * @version 1.0
 *          <p/>
 *          Created by zhanghuan on 15-6-20
 */
public class SppManager {

    private static final UUID MY_UUID_SECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter mAdapter;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_CONNECTING = 1; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 2;  // now connected to a remote device

    private Logger log;
    private BluetoothDevice remoteDevice;
    private InputStream mmInStream = null;
    private OutputStream mmOutStream = null;

    private static final int MAX_SIZE = 256;

    public SppManager() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        log = LoggerFactory.getLogger("obd.pod.spp");
    }

    private synchronized void setState(int state) {
        log.info("mState = {}, state = {} " , mState , state);
        mState = state;
        switch (mState) {
            case STATE_NONE:
                log.debug("onReceive Disconnect from device,error : DeviceDisConnected");
                EventBus.getDefault().post(new Event.Disconnected(Event.ErrorCode.DeviceDisConnected));
                break;
            case STATE_CONNECTING:
                log.debug("onReceive connecting the device");
                EventBus.getDefault().post(new Event.Connecting());
                break;
            case STATE_CONNECTED:
                log.debug("onReceive connected to device");
                EventBus.getDefault().post(new Event.SPPInitOutStream(mmOutStream));
                EventBus.getDefault().post(new Event.BTConnected(remoteDevice.getName(),remoteDevice.getAddress()));
                break;
            default:
                log.debug("onReceive Disconnect from device,error : ConnectInvokeFail");
                EventBus.getDefault().post(new Event.Disconnected(Event.ErrorCode.ConnectInvokeFail));
                break;
        }
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        log.debug("start");

        if (mConnectThread != null) {
            log.debug("Cancel any thread attempting to make a connection");
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            log.debug("Cancel any thread currently running a connection");
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

    }


    public synchronized void connect(String macAddr, boolean secure) {
        log.info("connect address = {} " , macAddr);

        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectThread = new ConnectThread(macAddr, secure);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }


    public synchronized void connected(BluetoothSocket socket, final String socketType) {
        log.info(" Socket Type = {}" , socketType);

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        setState(STATE_CONNECTED);
    }

    public synchronized void stop() {
        log.debug("stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
    }


    public void write(byte[] out) {
        ConnectedThread r;
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        r.write(out);
    }


    private void connectionFailed() {
        SppManager.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        SppManager.this.start();
    }


    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private String mSocketType;

        public ConnectThread(String macAddr, boolean secure) {
            remoteDevice = mAdapter.getRemoteDevice(macAddr);
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";
            try {
                if (secure) {
                    tmp = remoteDevice.createRfcommSocketToServiceRecord(
                            MY_UUID_SECURE);
                } else {
                    tmp = remoteDevice.createInsecureRfcommSocketToServiceRecord(
                            MY_UUID_SECURE);
                }
            } catch (IOException e) {
                log.warn("Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            log.info( "BEGIN mConnectThread SocketType = {}" , mSocketType);
            setName("ConnectThread" + mSocketType);
            mAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
            } catch (IOException e) {
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    log.warn("unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }
            synchronized (SppManager.this) {
                mConnectThread = null;
            }
            connected(mmSocket, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                log.warn("close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread {

        private final BluetoothSocket mmSocket;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            log.debug("create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                log.warn("temp sockets not created", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            log.info( "BEGIN mConnectedThread--Keep listening to the InputStream while connected");
            int bytes;
            byte[] buffer = new byte[MAX_SIZE];
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    byte[] mResponseData = new byte[bytes];
                    System.arraycopy(buffer, 0, mResponseData, 0, bytes);
                    EventBus.getDefault().post(new EventRead.L0ReadDone(mResponseData));
                } catch (Exception e) {
                    log.warn("disconnected", e);
                    connectionLost();
                    SppManager.this.start();
                }
            }
        }

        /**
         * 写数据
         *
         * @param buffer 写入的数据
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                EventBus.getDefault().post(new EventWrite.L0WriteDone());
            } catch (IOException e) {
                log.warn("Exception during write", e);
                EventBus.getDefault().post(new EventWrite.L0WriteFail());
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                log.warn("close() of connect socket failed", e);
            }
        }
    }

}