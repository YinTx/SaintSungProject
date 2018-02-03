/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.saintsung.saintpmc.lock;

import android.app.ActivityManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.saintsung.saintpmc.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service implements IResponser,IListener{
    private final static String TAG = BluetoothLeService.class.getSimpleName();
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    public BluetoothDevice bluetoothDevice;
    BluetoothGatt bluetoothGatt;
    BluetoothGattService bluetoothGattService,bluetoothGattServiceWrite,bluetoothGattServiceSet;
    BluetoothGattCharacteristic bluetoothGattCharacteristicRead,bluetoothGattCharacteristicWrite,bluetoothGattCharacteristicSet,bluetoothGattCharacteristicBaudRate;
    BluetoothGattDescriptor descriptor;
    private int mConnectionState = STATE_DISCONNECTED;

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED           = "com.android.bluetooth.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED        = "com.android.bluetooth.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.android.bluetooth.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE           = "com.android.bluetooth.ACTION_DATA_AVAILABLE";
    public final static String ACTION_DATA_SET           = "com.android.bluetooth.ACTION_DATA_SET";
    public final static String EXTRA_DATA                      = "com.android.bluetooth.EXTRA_DATA";
    public final static String EXTRA_COUNT                      = "com.android.bluetooth.EXTRA_COUNT";
    public final static String WRITE_DATA                      = "com.android.bluetooth.WRITE_DATA";
    public final static String WRITE_COUNT                      = "com.android.bluetooth.WRITE_COUNT";
    Handler handler;
    public static String lock_no,pwd,state;
    public static String temp_pwd="";
    public static String unlockType;
    public static String operateState;
    public static String logState;
    private String request;
    private String requestPacket;
    private String responsePacket;
    private String userId;
    public boolean flagDiscoverServices,statusWrite;
    //	global
    //
    public static final byte opcode_connected = (byte)'l';
    //wait key head fall into the unlock
    public static final byte OPCODE_WAIT_FALL_IN_UNLOCK = (byte)'F';
    //wait key head fall into the lock
    public static final byte OPCODE_WAIT_FALL_IN_LOCK = (byte)'f';
    public static final byte opcode_receivedresponse = (byte)'B';
    public static final byte opcode_sleep = (byte)'K';
    public static final byte opcode_close = (byte)'k';
    // read lock sn
    public static final byte OPCODE_LOCKSN = (byte) 'S';
    //motor open lock
    public static final byte OPCODE_OPEN_LOCK= (byte)'O';
    //motor open lock of 15bit
    public static final byte OPCODE_OPEN_LOCK15= (byte)'o';
    //motor close lock
    public static final byte OPCODE_CLOSE_LOCK=(byte)'C';
    public static final byte opcode_password = (byte) 'V';
    public static final byte opcode_userReload = (byte) 'w';
    public static final byte opcode_sendS00Up= (byte) 'X';
    public static final byte opcode_getS00Up= (byte) 'x';
    public static final byte ERRCODE_SUCC = (byte) (0);
    public static final byte opcode_password_error= (byte) (10);
    public static final byte ERRCODE_MOTORTIMEOUT	 =(byte)(14);
    public static final byte ERRCODE_ReadLockSnFail = (byte) (18);
    public static final byte ERRCODE_OPENLOCKFAIL = (byte) (17);
    public static final byte ERRCODE_OPCODE_SET_PARAM = (byte) (24);
    public final static UUID UUID_HEART_RATE_MEASUREMENT= UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);
    public final static UUID uuid_service_read = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    //    public final static UUID uuid_service = UUID.fromString("0000ff12-0000-1000-8000-00805f9b34fb");
    public static final UUID uuid_service_write = UUID.fromString("0000FFE5-0000-1000-8000-00805f9b34fb");
    public static final UUID uuid_service_set = UUID.fromString("0000ff90-0000-1000-8000-00805f9b34fb");
    public final static UUID uuid_characteristic_read = UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb");
    //    public final static UUID uuid_characteristic_read = UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb");
    public static final UUID uuid_characteristic_write = UUID.fromString("0000FFE9-0000-1000-8000-00805f9b34fb");
    //	public static final UUID uuid_characteristic_write = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");
    public static final UUID uuid_characteristic_set = UUID.fromString("0000ff91-0000-1000-8000-00805f9b34fb");
    public static final UUID uuid_characteristic_baudRate = UUID.fromString("0000ff93-0000-1000-8000-00805f9b34fb");
    public static final String upload_log_serviceId="L004";
    public static final String operateState_disConnect="operateState_disConnect";
    public static final String operatestate_unlock="operatestate_unlock";
    public static final String operatestate_lock="operatestate_lock";
    public static final String operateState_uploadLog="operateState_uploadLog";
    public static final String string_unlockPwd="甇�����������";
    public static final String string_lockPermission="���������甇日��!";
    public static final String string_lockDelay="������,�摰���氖撘�";
    public static final String string_sleep="string_sleep";
    public static final String string_close="string_close";
    public static final String split="split";
    public StringBuffer stringBuffer=new StringBuffer();
    public static int unlockCount,receivedLength;
    private int sepIdx,sendCount,length,schedule=0,j=1;
    byte[] byteArray,bDataSend;
    public byte[] bDataSend1;
    public String string;
    private String[] stringArray;
    private int pwdOffsetTwo10=2;
    private int pwdOffsetTwo15=12;
    private int pwdOffsetThree10=3;
    private int pwdOffsetThree15=18;
    private int pwdOffsetFour10=4;
    private int pwdOffsetFour15=24;
    //
    FileStream fileStream=new FileStream();
    public static final Intent intent = new Intent(ACTION_DATA_AVAILABLE);
    public static Intent intent0=new Intent();
    CommandPacker packet = new CommandPacker();
    byte[] splitArray=new byte[18];


    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
//		handler = new Handler(getMainLooper());
        handler = new Handler();
    }
    //	@Override
//	public int onStartCommand(Intent intent, int flags, int startId) {
//		// TODO Auto-generated method stub
//		string=intent.getStringExtra(DeviceScanActivity.device_address);
//		connect(string);
//		return super.onStartCommand(intent, flags, startId);
//	}
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                Log.w(TAG, "BluetoothGattCallback餈�嚙�??+Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:");
                flagDiscoverServices=bluetoothGatt.discoverServices();
                broadcastUpdate(intentAction);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.e(TAG, "BluetoothGattCallback餈�嚙�??+Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //setCharacteristicNotification
//            	bluetoothGattService=gatt.getService(uuid_service);
                bluetoothGattService=gatt.getService(uuid_service_read);
                bluetoothGattCharacteristicRead=bluetoothGattService.getCharacteristic(uuid_characteristic_read);
                final int characteristicProperties = bluetoothGattCharacteristicRead.getProperties();
                if ((characteristicProperties | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    // If there is an active notification on a characteristic, clear
                    // it first so it doesn't update the data field on the user interface.
                    if (bluetoothGattCharacteristicRead != null) {
                        setCharacteristicNotification(bluetoothGattCharacteristicRead, false);
                    }
                    readCharacteristic(bluetoothGattCharacteristicRead);
                }
                if ((characteristicProperties | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    setCharacteristicNotification(bluetoothGattCharacteristicRead, true);
                }
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
//                bluetoothGattCharacteristicWrite= bluetoothGattService.getCharacteristic(uuid_characteristic_write);
                bluetoothGattServiceWrite = bluetoothGatt.getService(uuid_service_write);
                bluetoothGattServiceSet = bluetoothGatt.getService(uuid_service_set);
                bluetoothGattCharacteristicSet = bluetoothGattServiceSet.getCharacteristic(uuid_characteristic_set);
                bluetoothGattCharacteristicWrite= bluetoothGattServiceWrite.getCharacteristic(uuid_characteristic_write);
                bluetoothGattCharacteristicBaudRate = bluetoothGattService.getCharacteristic(uuid_characteristic_baudRate);
            } else {
                Log.w(TAG, "�撘����蝡荔蕭?+onServicesDiscovered received: " + status);
            }
        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic,int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }
        //		// ================================
//		// Jerry add WRITE callback
//		@Override
//		public void onCharacteristicWrite(BluetoothGatt gatt,
//				BluetoothGattCharacteristic characteristic, int status) {
//			if (status == BluetoothGatt.GATT_SUCCESS) {
//				broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
//
//			}
//		}
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
//            	if (bDataSend1!=null) {
//            		writeLlsAlertLevel(null,bDataSend1);
//            		bDataSend1=null;
//				}
            }
        }
        @Override
        //撟踵��
        public void onCharacteristicChanged(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            string=getActivityManager();
            if(string!=null){
                //check DeviceControlActivity is open or die
                if(string.equals("com.android.bluetooth.DeviceControlActivity")){
                    //refresh DeviceControlActivity
                }else if(string.equals("com.example.app.SetActivity")){
                    //refresh SetActivity
                }else if(MainActivity.setS00!=null){
                    //refresh DeviceScanActivity
                }else{
                    //start DeviceControlActivity
                    intent0.setClass(getBaseContext(), DeviceControlActivity.class);
                    intent0.setFlags(intent0.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent0);
                }
            }
        }
    };
    //
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
    //
    private void broadcastUpdate(final String action,final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else if (characteristic.getUuid().equals(uuid_characteristic_read)) {
            final byte[] data = characteristic.getValue();
            Response response=new Response();
            response.Register(this);
            response.dataComing(data);
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        }
    }
    //
    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
    //
    private final IBinder mBinder = new LocalBinder();
    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }
    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (bluetoothAdapter == null || address == null) {
            Log.e(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        // Previously connected device.  Try to reconnect.
        if (address != null && bluetoothGatt != null) {
            Log.w(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (bluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                //callback intentAction
                string = ACTION_GATT_CONNECTED;
                broadcastUpdate(string);
                return true;
            } else {
                return false;
            }
        }
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
        if (bluetoothDevice == null) {
            Log.e(TAG, "Device not found,Unable to connect!");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        bluetoothGatt = bluetoothDevice.   connectGatt(this, false, mGattCallback);
        //experiment
//        bluetoothGatt = bluetoothDevice.connectGatt(this, true, mGattCallback);
        Log.w(TAG, "Trying to create a new connection.");
        mConnectionState = STATE_CONNECTING;
        return true;
    }
    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            if (bluetoothAdapter == null) {
                Log.w(TAG, "���撘�憭梯揖+BluetoothAdapter is null");
            }else if (bluetoothGatt == null) {
                Log.w(TAG, "���撘�憭梯揖+mBluetoothGatt is null");
            }
            return;
        }
        bluetoothGatt.disconnect();
        state=FileStream.connectRecord;
        //delete ��?? connectRecord
        byteArray=fileStream.fileStream(state,FileStream.write,null);
//		//service��歲頧祇△??
//	    Intent intent=new Intent();
//		intent.setClass(getBaseContext(), MainActivity.class);
//		intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
//		startActivity(intent);
//		Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
//		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		startActivity(intent);
    }
    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
    }
    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "read���垢�+BluetoothAdapter not initialized");
            return;
        }
        bluetoothGatt.readCharacteristic(characteristic);
    }
    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,boolean enabled) {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "�����垢�������虜+BluetoothAdapter not initialized");
            return;
        }
        bluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(descriptor);
        }
//        else if (uuid_characteristic_read.equals(characteristic.getUuid())) {
//        	//�撠噢�
//        	descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
//			if (descriptor != null) {
//				Log.e(TAG,"set descriptor" + descriptor + descriptor.getValue());
//				if (enabled)
//					descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//				else
//					descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
//				bluetoothGatt.writeDescriptor(descriptor);
//			}
//        }
    }
    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (bluetoothGatt == null) return null;
        return bluetoothGatt.getServices();
    }
    //��蝐餉��??
    public void writeLlsAlertLevel(String state,byte[] bb) {
        if (bluetoothAdapter == null || bluetoothGatt == null||bluetoothGattCharacteristicWrite == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
//		// Log.i("iDevice", iDevice);
//		if (bluetoothGattServiceWrite == null||bluetoothGattCharacteristicWrite == null) {
//			if (bluetoothGattServiceWrite == null) {
//				Log.e(TAG, "bluetoothGattServiceWrite not found!");
//			} else if (bluetoothGattCharacteristicWrite == null){
//				Log.e(TAG, "bluetoothGattCharacteristicWrite not found!");
//			}
//			return;
//		}
        else{
            int storedLevel = bluetoothGattCharacteristicWrite.getWriteType();
            Log.d(TAG, "(int)bluetoothGattCharacteristicWrite.getWriteType()=" + storedLevel);
            bluetoothGattCharacteristicWrite.setValue(bb);
            Log.i("����捆:", new String(bb));
//    		bluetoothGattCharacteristicWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
//    		bluetoothGattCharacteristicWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_SIGNED);
//    		bluetoothGattCharacteristicWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            if (split.equals(state)) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //��餈�oolean??,���蝏�挽ble嚙�????��嚙�???
                        statusWrite = bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristicWrite);
                    }
                },100);// �Handler銝剜銵�瑪蝔僎撱�??3s;
//    				try {
//    					//thread 30ms
////    					Thread.sleep(30);
//    					Thread.sleep(100);
//    				} catch (InterruptedException e) {
//    					e.printStackTrace();
//    				}
            }else{
                //��餈�oolean??,���蝏�挽ble嚙�????��嚙�???
                statusWrite = bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristicWrite);
            }
            Log.i(TAG, "嚙�????嚙�???:" + statusWrite);
        }
    }
    //	public void writeLlsAlertLevel(String state,byte[] bb) {
//		if (bb!=null) {
//			if (MainActivity.bluetoothAdapter == null || bluetoothGatt == null||bluetoothGattCharacteristicWrite == null) {
//				Log.w(TAG, "BluetoothAdapter not initialized");
//				return;
//			}else{
//				CharSequence bleString = new String(bb);
//				boolean flag=false;
//				// =====================================================================
//				if(bleString.length()>20){
//					int endIndex = 1;
//					bluetoothGattCharacteristicWrite.setValue(bleString.subSequence(0, 20).toString());
//					while(endIndex*20 < bleString.length()){
//						bleString.subSequence((endIndex-1)*20, endIndex*20);
//						endIndex++;
//					}
//				}
//				StringBuilder sb = new StringBuilder();
//				boolean end = false;
//				for(int i=0;i<bleString.length();i++){
//					sb.append(bleString.charAt(i));
//					if(i%20==19){
//						bluetoothGattCharacteristicWrite.setValue(sb.toString());
//						flag=bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristicWrite);
//						Log.i("����捆:", new String(sb));
//						sb = new StringBuilder();
//						end = true;
//					}else if(i%20==0){
//						end = false;
//					}
//				}
//				if(!end){
//					bluetoothGattCharacteristicWrite.setValue(sb.toString());
//					flag=bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristicWrite);
//					Log.i("����捆:", new String(sb));
//				}
//				if(!flag){
//					Log.e(TAG, "send flag:"+flag);
//				}
//			}
//		} else {
//			Log.e(TAG, "NO input, NO send!!"+null);
//		}
//	}
    @Override
    public void notify(byte cmdCode, byte errCode,  byte[] bData){
        if ((unlockCount==0||unlockCount==1||unlockCount==2)&(cmdCode==OPCODE_WAIT_FALL_IN_UNLOCK||cmdCode==OPCODE_WAIT_FALL_IN_LOCK)) {
            operateState=null;
        }
        // TODO Auto-generated catch blockaaaa
        if (operateState==null&&(cmdCode==OPCODE_WAIT_FALL_IN_UNLOCK||cmdCode==OPCODE_WAIT_FALL_IN_LOCK)) {
            //嚙�????received success��誘
            if(packet.setPacketParam(opcode_receivedresponse, null, null)){
                bDataSend = packet.encodePacket(null);
            }
            writeLlsAlertLevel(null,bDataSend);
            //wait 200ms
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //wait 200ms
                }
            },200);
            //get unlockType
            unlockType=LockSetActivity.unlockType;
            //���銵�
            if (cmdCode==OPCODE_WAIT_FALL_IN_UNLOCK) {
                MainActivity.setS00=null;
                if (errCode==ERRCODE_SUCC) {
                    operateState=operatestate_unlock;
                    //validate
                    if (MainActivity.handLockNumber!=null) {
                        //validate
                        intent.putExtra(EXTRA_DATA, "颲��??:"+lock_no + "\n");
                        intent0.putExtra(EXTRA_DATA, "颲��??:"+lock_no + "\n");
                        intent.putExtra(WRITE_DATA, "������!\n");
                        intent0.putExtra(WRITE_DATA, "������!\n");
                        if (unlockType.equals(LockSetActivity.unlockLogin)) {
                            //撉����
                            boolean wlanFlag=NetworkConnect.checkNet(getApplicationContext());
                            if (wlanFlag) {
                                intent.putExtra(WRITE_DATA, "甇������??����??,霂瑞��??..." + "\n");
                                intent0.putExtra(WRITE_DATA, "甇������??����??,霂瑞��??..." + "\n");
                                download_single_lock_sn();
                            } else {
                                intent.putExtra(WRITE_DATA, "蝵�撘�餈,霂瑁��??..." + "\n");
                                intent0.putExtra(WRITE_DATA, "蝵�撘�餈,霂瑁��??..." + "\n");
                            }
                        } else if (unlockType.equals(LockSetActivity.unlockUser)) {
                            //????(��蝳餌瑪����):
//							readStream(FileStream.userDownload,operateState,string);
                            userDownload();
                        }else if (unlockType.equals(LockSetActivity.readLockNumber)) {
                            //嚙�????霂駁�蕭???
                            if(packet.setPacketParam(OPCODE_LOCKSN, null, null)){
                                bDataSend = packet.encodePacket(null);
                            }
                            writeLlsAlertLevel(null,bDataSend);
                        }else if (unlockType.equals(LockSetActivity.unlockOriginal)||unlockType.equals(LockSetActivity.unlockTwo)||unlockType.equals(LockSetActivity.unlockThree)||unlockType.equals(LockSetActivity.unlockFour)) {
//							intent.putExtra(WRITE_DATA, string_unlockPwd);
//							intent0.putExtra(WRITE_DATA, string_unlockPwd);
                            new MyTask().execute("");
                        }else{
                            intent.putExtra(EXTRA_DATA, "�摰����??(����)??,�摰���氖??..." + "\n");
                            intent0.putExtra(EXTRA_DATA, "�摰����??(����)??,�摰���氖??..." + "\n");
                        }
                    } else {
                        //validate ��內
                        if (unlockType.equals(LockSetActivity.unlock_screw)) {
                            intent.putExtra(EXTRA_DATA, "������(????),�摰���氖??..." + "\n");
                            intent0.putExtra(EXTRA_DATA, "������(????),�摰���氖??..." + "\n");
                            intent.putExtra(WRITE_DATA, string_lockPermission);
                            intent0.putExtra(WRITE_DATA, string_lockPermission);
                        }else{
                            intent.putExtra(EXTRA_DATA, "������(????),�摰���氖??..." + "\n");
                            intent0.putExtra(EXTRA_DATA, "������(????),�摰���氖??..." + "\n");
                            intent.putExtra(WRITE_DATA, "霂駁�銝�,�摰���氖??..." + "\n");
                            intent0.putExtra(WRITE_DATA, "霂駁�銝�,�摰���氖??..." + "\n");
                            //send readLockNumber
                            if(packet.setPacketParam(OPCODE_LOCKSN, null, null)){
                                bDataSend = packet.encodePacket(null);
                            }
                            writeLlsAlertLevel(null,bDataSend);
                        }
                    }
                } else{
                    switch(errCode){
                        case ERRCODE_MOTORTIMEOUT:
                            //must clear cache
                            lock_no=null;
                            //敹◆餈��??0,���撅�����誧蝏剖��??
                            unlockCount=0;
                            //must clear
                            MainActivity.handLockNumber=null;
                            //must clear
                            operateState=null;
                            if(unlockType.equals(LockSetActivity.unlockContinue)){
                                intent.putExtra(EXTRA_DATA, "���仃韐�(????)!" + "\n");
                                intent0.putExtra(EXTRA_DATA, "���仃韐�(????)!" + "\n");
                                intent.putExtra(WRITE_DATA, "\n"+"����????,�摰���氖??..."+ "\n");
                                intent0.putExtra(WRITE_DATA, "\n"+"����????,�摰���氖??..."+ "\n");
                                //嚙�????���蕭???
                                if(packet.setPacketParam(OPCODE_WAIT_FALL_IN_UNLOCK, null, null)){
                                    bDataSend = packet.encodePacket(null);
                                }
                                writeLlsAlertLevel(null,bDataSend);
                            }
//						else if(unlockType.equals(LockSetActivity.readLockNumber)){
//							intent.putExtra(EXTRA_DATA, "���仃韐�(????)!" + "\n");
//							intent0.putExtra(EXTRA_DATA, "���仃韐�(????)!" + "\n");
//							intent.putExtra(WRITE_DATA, "\n"+"����霂駁��??,�摰���氖??..."+ "\n");
//							intent0.putExtra(WRITE_DATA, "\n"+"����霂駁��??,�摰���氖??..."+ "\n");
//							//嚙�????���蕭???
//							if(packet.setPacketParam(OPCODE_WAIT_FALL_IN_UNLOCK, null, null)){
//								bDataSend = packet.encodePacket(null);
//							}
//							writeLlsAlertLevel(null,bDataSend);
//						}
                            else if(unlockType.equals(LockSetActivity.unlockTwo)){
                                intent.putExtra(EXTRA_DATA, "���仃韐�(????)!" + "\n");
                                intent0.putExtra(EXTRA_DATA, "���仃韐�(????)!" + "\n");
                                intent.putExtra(WRITE_DATA, "\n"+"����????,�摰���氖??..."+ "\n");
                                intent0.putExtra(WRITE_DATA, "\n"+"����????,�摰���氖??..."+ "\n");
                                //嚙�????���蕭???
                                if(packet.setPacketParam(OPCODE_WAIT_FALL_IN_UNLOCK, null, null)){
                                    bDataSend = packet.encodePacket(null);
                                }
                                writeLlsAlertLevel(null,bDataSend);
                            }
                            else{
                                intent.putExtra(EXTRA_DATA, "���仃韐�(????)!" + "\n");
                                intent0.putExtra(EXTRA_DATA, "���仃韐�(????)!" + "\n");
                                intent.putExtra(WRITE_DATA, "���仃韐�,霂琿����??!" + "\n");
                                intent0.putExtra(WRITE_DATA, "���仃韐�,霂琿����??!" + "\n");
                            }
                            break;
                    }
                }
            } else if (cmdCode==OPCODE_WAIT_FALL_IN_LOCK) {
                MainActivity.setS00=null;
                if (errCode==ERRCODE_SUCC) {
                    operateState=operatestate_lock;
                    //validate ��內
                    if (unlockType.equals(LockSetActivity.unlock_screw)) {
                        intent.putExtra(EXTRA_DATA, "������(����),�摰���氖??..." + "\n");
                        intent0.putExtra(EXTRA_DATA, "������(����),�摰���氖??..." + "\n");
                        intent.putExtra(WRITE_DATA, "����??,�摰���氖??..." + "\n");
                        intent0.putExtra(WRITE_DATA, "����??,�摰���氖??..." + "\n");
                        //嚙�????���蕭???
                        if(packet.setPacketParam(OPCODE_CLOSE_LOCK, null, null)){
                            bDataSend = packet.encodePacket(state);
                        }
                        writeLlsAlertLevel(null,bDataSend);
                    }else{
                        intent.putExtra(EXTRA_DATA, "������(����),�摰���氖??..." + "\n");
                        intent0.putExtra(EXTRA_DATA, "������(����),�摰���氖??..." + "\n");
                        intent.putExtra(WRITE_DATA, "霂駁�銝�,�摰���氖??..." + "\n");
                        intent0.putExtra(WRITE_DATA, "霂駁�銝�,�摰���氖??..." + "\n");
                        //send readLockNumber
                        if(packet.setPacketParam(OPCODE_LOCKSN, null, null)){
                            bDataSend = packet.encodePacket(null);
                        }
                        writeLlsAlertLevel(null,bDataSend);
                    }
                } else{
                    switch(errCode){
                        case ERRCODE_MOTORTIMEOUT:
                            //must clear cache
                            lock_no=null;
                            //敹◆餈��??0,���撅�����誧蝏剖��??
                            unlockCount=0;
                            //must clear
                            operateState=null;
                            intent.putExtra(EXTRA_DATA, "���仃韐�(����)!" + "\n");
                            intent0.putExtra(EXTRA_DATA, "���仃韐�(����)!" + "\n");
                            intent.putExtra(WRITE_DATA, "���仃韐�,霂琿����??!" + "\n");
                            intent0.putExtra(WRITE_DATA, "���仃韐�,霂琿����??!" + "\n");
                            break;
                    }
                }
            }
        }else if(operatestate_unlock.equals(operateState)||operatestate_lock.equals(operateState)){
            if (cmdCode==OPCODE_LOCKSN) {
                if (errCode==ERRCODE_SUCC) {
                    lock_no="276258026";
//					lock_no = Integer.toString(TypeConvert.bigEndian_byte2int(bData, 0));
                    intent.putExtra(EXTRA_DATA, "霂餃��:"+lock_no + "\n");
                    intent0.putExtra(EXTRA_DATA, "霂餃��:"+lock_no + "\n");
//			            String string="0123456789abcdefghijk";
//			            bDataSend=string.getBytes();
//			            writeLlsAlertLevel(bDataSend);
//			            opt_pwd=new StringBuffer("3305330953");
//			            opt_pwd=new StringBuffer("100085172118195");
                    //���銵�
                    if(operatestate_unlock.equals(operateState)){
                        if (unlockType.equals(LockSetActivity.unlockLogin)) {
                            /**
                             * �蝥踹�????
                             */
                            //撉����
                            boolean wlanFlag=NetworkConnect.checkNet(getApplicationContext());
                            if (wlanFlag) {
                                //"276212006":"100085172118195",
                                intent.putExtra(WRITE_DATA, "甇������??����??,霂瑞��??..." + "\n");
                                intent0.putExtra(WRITE_DATA, "甇������??����??,霂瑞��??..." + "\n");
                                download_single_lock_sn();
                            } else {
                                //must clear
                                operateState=null;
                                intent.putExtra(WRITE_DATA, "蝵�撘�餈,霂瑁��??..." + "\n");
                                intent0.putExtra(WRITE_DATA, "蝵�撘�餈,霂瑁��??..." + "\n");
                            }
                        } else if (unlockType.equals(LockSetActivity.unlockUser)) {
                            //????(��蝳餌瑪����):
                            userDownload();
                        }else if (unlockType.equals(LockSetActivity.readLockNumber)) {
                            //must clear
                            operateState=null;
                            intent.putExtra(WRITE_DATA, "霂駁�蝏�??!" + "\n");
                            intent0.putExtra(WRITE_DATA, "霂駁�蝏�??!" + "\n");
//								intent.putExtra(WRITE_DATA, "\n"+"霂駁�蝏�??,����霂駁��??,�摰���氖??..."+ "\n");
//								intent0.putExtra(WRITE_DATA, "\n"+"霂駁�蝏�??,����霂駁��??,�摰���氖??..."+ "\n");
//								//嚙�????���蕭???
//								if(packet.setPacketParam(OPCODE_WAIT_FALL_IN_UNLOCK, null, null)){
//									bDataSend = packet.encodePacket(null);
//								}
//								writeLlsAlertLevel(null,bDataSend);
                        } else if (unlockType.equals(LockSetActivity.unlockOriginal)||unlockType.equals(LockSetActivity.unlockTwo)
                                ||unlockType.equals(LockSetActivity.unlockThree)||unlockType.equals(LockSetActivity.unlockFour)
                                ||unlockType.equals(LockSetActivity.unlockContinue)) {
                            //send disposable
//								string="1C6F0064005300AB007600C510719DCA19";
//								writeLlsAlertLevel(null,string.getBytes());
                            //unpacket
//							string="1C6F0064005300AB007";
//							writeLlsAlertLevel(null,string.getBytes());
//							string="600C510719DCA19";
//							writeLlsAlertLevel(split,string.getBytes());
//								intent.putExtra(WRITE_DATA, string_unlockPwd);
//								intent0.putExtra(WRITE_DATA, string_unlockPwd);
                            new MyTask().execute("");
                        }
                    }else if(operatestate_lock.equals(operateState)){
                        //嚙�????���蕭???
                        if(packet.setPacketParam(OPCODE_CLOSE_LOCK, null, null)){
                            bDataSend = packet.encodePacket(state);
                        }
                        writeLlsAlertLevel(null,bDataSend);
                        intent.putExtra(WRITE_DATA, "����??,�摰���氖??..." + "\n");
                        intent0.putExtra(WRITE_DATA, "����??,�摰���氖??..." + "\n");
                    } else {
                        //
                        intent.putExtra(WRITE_DATA, "�摰�粉��??!" + "\n");
                        intent0.putExtra(WRITE_DATA, "�摰�粉��??!" + "\n");
                    }
                } else {
                    switch(errCode){
                        case ERRCODE_ReadLockSnFail:
                            //must clear cache
                            lock_no=null;
                            //敹◆餈��??0,���撅�����誧蝏剖��??
                            unlockCount=0;
                            //must clear
                            MainActivity.handLockNumber=null;
                            //must clear
                            operateState=null;
                            if(unlockType.equals(LockSetActivity.unlockContinue)){
                                intent.putExtra(EXTRA_DATA, "霂駁�憭�??!" + "\n");
                                intent0.putExtra(EXTRA_DATA, "霂駁�憭�??!" + "\n");
                                intent.putExtra(WRITE_DATA, "\n"+"����????,�摰���氖??..."+ "\n");
                                intent0.putExtra(WRITE_DATA, "\n"+"����????,�摰���氖??..."+ "\n");
                                //嚙�????���蕭???
                                if(packet.setPacketParam(OPCODE_WAIT_FALL_IN_UNLOCK, null, null)){
                                    bDataSend = packet.encodePacket(null);
                                }
                                writeLlsAlertLevel(null,bDataSend);
                            }
//						else if(unlockType.equals(LockSetActivity.readLockNumber)){
//							intent.putExtra(EXTRA_DATA, "霂駁�憭�??!" + "\n");
//							intent0.putExtra(EXTRA_DATA, "霂駁�憭�??!" + "\n");
//							intent.putExtra(WRITE_DATA, "\n"+"����霂駁��??,�摰���氖??..."+ "\n");
//							intent0.putExtra(WRITE_DATA, "\n"+"����霂駁��??,�摰���氖??..."+ "\n");
//							//嚙�????���蕭???
//							if(packet.setPacketParam(OPCODE_WAIT_FALL_IN_UNLOCK, null, null)){
//								bDataSend = packet.encodePacket(null);
//							}
//							writeLlsAlertLevel(null,bDataSend);
//						}
                            else if(unlockType.equals(LockSetActivity.unlockTwo)){
                                intent.putExtra(EXTRA_DATA, "霂駁�憭�??!" + "\n");
                                intent0.putExtra(EXTRA_DATA, "霂駁�憭�??!" + "\n");
                                intent.putExtra(WRITE_DATA, "\n"+"����????,�摰���氖??..."+ "\n");
                                intent0.putExtra(WRITE_DATA, "\n"+"����????,�摰���氖??..."+ "\n");
                                //嚙�????���蕭???
                                if(packet.setPacketParam(OPCODE_WAIT_FALL_IN_UNLOCK, null, null)){
                                    bDataSend = packet.encodePacket(null);
                                }
                                writeLlsAlertLevel(null,bDataSend);
                            }
                            else{
                                intent.putExtra(EXTRA_DATA, "霂駁�憭�??!" + "\n");
                                intent0.putExtra(EXTRA_DATA, "霂駁�憭�??!" + "\n");
                                intent.putExtra(WRITE_DATA, "霂駁�憭�??,霂琿����??!" + "\n");
                                intent0.putExtra(WRITE_DATA, "霂駁�憭�??,霂琿����??!" + "\n");
                            }
                            break;
                    }
                }
            } else if (cmdCode==OPCODE_OPEN_LOCK||cmdCode==OPCODE_OPEN_LOCK15) {
                if (errCode==ERRCODE_SUCC) {
                    logState=operatestate_unlock;
                    if (unlockType.equals(LockSetActivity.unlockLogin)||unlockType.equals(LockSetActivity.unlockUser)) {
                        //must clear
                        operateState=null;
                        intent.putExtra(WRITE_DATA, "??����??!" + "\n");
                        intent0.putExtra(WRITE_DATA, "??����??!" + "\n");
                        //銝��??��扇敶��??
                        uploadLog(logState);
                        //敹◆��皜征
                        MainActivity.handLockNumber=null;
                    } else if (unlockType.equals(LockSetActivity.unlockOriginal)) {
                        //must clear
                        operateState=null;
                        intent.putExtra(WRITE_DATA, "??����??!" + "\n");
                        intent0.putExtra(WRITE_DATA, "??����??!" + "\n");
                        //瘜冽��,銝�����扇敶��??
                    } else if (unlockType.equals(LockSetActivity.unlockTwo)||unlockType.equals(LockSetActivity.unlockThree)
                            ||unlockType.equals(LockSetActivity.unlockFour)) {
                        //��內靽⊥
                        if(unlockCount==1){
                            intent.putExtra(EXTRA_DATA, "\n"+"��揮������������澆�??..."+ "\n");
                            intent0.putExtra(EXTRA_DATA, "\n"+"��揮������������澆�??..."+ "\n");
                        }else if(unlockCount==2){
                            intent.putExtra(EXTRA_DATA, "\n"+"��揮��������..."+ "\n");
                            intent0.putExtra(EXTRA_DATA, "\n"+"��揮��������..."+ "\n");
                        }
//							intent.putExtra(WRITE_DATA, string_lockDelay);
//							intent0.putExtra(WRITE_DATA, string_lockDelay);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //嚙�????���蕭???
                                if(packet.setPacketParam(OPCODE_CLOSE_LOCK, null, null)){
                                    bDataSend = packet.encodePacket(null);
                                }
                                writeLlsAlertLevel(null,bDataSend);
                            }
                        },2*1000);
                    } else if (unlockType.equals(LockSetActivity.unlockContinue)) {
                        intent.putExtra(EXTRA_DATA, "\n"+"??����??,��揮��������..."+ "\n");
                        intent0.putExtra(EXTRA_DATA, "\n"+"??����??,��揮��������..."+ "\n");
//						intent.putExtra(WRITE_DATA, string_lockDelay);
//						intent0.putExtra(WRITE_DATA, string_lockDelay);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //嚙�????���蕭???
                                if(packet.setPacketParam(OPCODE_CLOSE_LOCK, null, null)){
                                    bDataSend = packet.encodePacket(null);
                                }
                                MainActivity.bluetoothLeService.writeLlsAlertLevel(null,bDataSend);
                            }
                        },2*1000);
//						//嚙�????���蕭???
//						if(packet.setPacketParam(OPCODE_CLOSE_LOCK, null, null)){
//							bDataSend = packet.encodePacket(null);
//						}
//						MainActivity.bluetoothLeService.writeLlsAlertLevel(null,bDataSend);
                    }else {
                        //must clear
                        operateState=null;
                        intent.putExtra(WRITE_DATA, "�摰����??!" + "\n");
                        intent0.putExtra(WRITE_DATA, "�摰����??!" + "\n");
                    }
                } else {
                    switch(errCode){
                        case ERRCODE_OPENLOCKFAIL:
                            //must clear cache
                            lock_no=null;
                            //敹◆餈��??0,���撅�����誧蝏剖��??
                            unlockCount=0;
                            //must clear
                            MainActivity.handLockNumber=null;
                            //must clear
                            operateState=null;
                            if(unlockType.equals(LockSetActivity.unlockContinue)){
                                intent.putExtra(EXTRA_DATA, "??��仃??!" + "\n");
                                intent0.putExtra(EXTRA_DATA, "??��仃??!" + "\n");
                                intent.putExtra(WRITE_DATA, "\n"+"����????,�摰���氖??..."+ "\n");
                                intent0.putExtra(WRITE_DATA, "\n"+"����????,�摰���氖??..."+ "\n");
                                //嚙�????���蕭???
                                if(packet.setPacketParam(OPCODE_WAIT_FALL_IN_UNLOCK, null, null)){
                                    bDataSend = packet.encodePacket(null);
                                }
                                writeLlsAlertLevel(null,bDataSend);
                            }
                            else if(unlockType.equals(LockSetActivity.unlockTwo)){
                                intent.putExtra(EXTRA_DATA, "??��仃??!" + "\n");
                                intent0.putExtra(EXTRA_DATA, "??��仃??!" + "\n");
                                intent.putExtra(WRITE_DATA, "\n"+"����????,�摰���氖??..."+ "\n");
                                intent0.putExtra(WRITE_DATA, "\n"+"����????,�摰���氖??..."+ "\n");
                                //嚙�????���蕭???
                                if(packet.setPacketParam(OPCODE_WAIT_FALL_IN_UNLOCK, null, null)){
                                    bDataSend = packet.encodePacket(null);
                                }
                                writeLlsAlertLevel(null,bDataSend);
                            }
                            else{
                                intent.putExtra(EXTRA_DATA, "??��仃??!" + "\n");
                                intent0.putExtra(EXTRA_DATA, "??��仃??!" + "\n");
                                intent.putExtra(WRITE_DATA, "??��仃??,霂琿����??!" + "\n");
                                intent0.putExtra(WRITE_DATA, "??��仃??,霂琿����??!" + "\n");
                            }
                            break;
                    }
                }
            } else if (cmdCode==OPCODE_CLOSE_LOCK) {
                if (errCode==ERRCODE_SUCC) {
                    //must clear
                    operateState=null;
                    logState=operatestate_lock;
                    if (unlockType.equals(LockSetActivity.unlockLogin)||unlockType.equals(LockSetActivity.unlockUser)) {
                        intent.putExtra(EXTRA_DATA, "��:"+lock_no + "\n");
                        intent0.putExtra(EXTRA_DATA, "��:"+lock_no + "\n");
                        intent.putExtra(WRITE_DATA, "������!" + "\n");
                        intent0.putExtra(WRITE_DATA, "������!" + "\n");
                        //銝���扇敶���
                        uploadLog(logState);
                    }else if (unlockType.equals(LockSetActivity.unlock_screw)) {
                        intent.putExtra(EXTRA_DATA, "������!\n");
                        intent0.putExtra(EXTRA_DATA, "������!\n");
                        intent.putExtra(WRITE_DATA, "������!" + "\n");
                        intent0.putExtra(WRITE_DATA, "������!" + "\n");
                    } else if (unlockType.equals(LockSetActivity.readLockNumber)||unlockType.equals(LockSetActivity.unlockOriginal)) {
                        intent.putExtra(EXTRA_DATA, "��:"+lock_no + "\n");
                        intent0.putExtra(EXTRA_DATA, "��:"+lock_no + "\n");
                        intent.putExtra(WRITE_DATA, "������!" + "\n");
                        intent0.putExtra(WRITE_DATA, "������!" + "\n");
                    } else if (unlockType.equals(LockSetActivity.unlockTwo)||unlockType.equals(LockSetActivity.unlockThree)||unlockType.equals(LockSetActivity.unlockFour)) {
                        if(unlockCount==1){
                            //��內靽⊥
                            intent.putExtra(EXTRA_DATA, "\n"+"���蕭????嚙�???,�摰���氖??..."+ "\n");
                            intent0.putExtra(EXTRA_DATA, "\n"+"���蕭????嚙�???,�摰���氖??..."+ "\n");
                            intent.putExtra(WRITE_DATA, "??��葉,�摰���氖??..." + "\n");
                            intent0.putExtra(WRITE_DATA, "??��葉,�摰���氖??..." + "\n");
                            //嚙�????���蕭???
                            if(packet.setPacketParam(OPCODE_WAIT_FALL_IN_UNLOCK, null, null)){
                                bDataSend = packet.encodePacket(null);
                            }
                            writeLlsAlertLevel(null,bDataSend);
                        } else if(unlockCount==2){
                            //敹◆餈��??0,���撅�����誧蝏剖��??
                            unlockCount=0;
                            intent.putExtra(EXTRA_DATA, "��:"+lock_no + "\n");
                            intent0.putExtra(EXTRA_DATA, "��:"+lock_no + "\n");
//							intent.putExtra(WRITE_DATA, "������!" + "\n");
//							intent0.putExtra(WRITE_DATA, "������!" + "\n");
                            intent.putExtra(WRITE_DATA, "������,蝏抒賒����????,�摰���氖??..." + "\n");
                            intent0.putExtra(WRITE_DATA, "������,蝏抒賒����????,�摰���氖??..." + "\n");
                            //嚙�????���蕭???
                            if(packet.setPacketParam(OPCODE_WAIT_FALL_IN_UNLOCK, null, null)){
                                bDataSend = packet.encodePacket(null);
                            }
                            writeLlsAlertLevel(null,bDataSend);
                        }else{
                            intent.putExtra(EXTRA_DATA, "��:"+lock_no + "\n");
                            intent0.putExtra(EXTRA_DATA, "��:"+lock_no + "\n");
                            intent.putExtra(WRITE_DATA, "������!" + "\n");
                            intent0.putExtra(WRITE_DATA, "������!" + "\n");
                        }
                    } else if(unlockType.equals(LockSetActivity.unlockContinue)){
                        intent.putExtra(EXTRA_DATA, "������!" + "\n");
                        intent0.putExtra(EXTRA_DATA, "������!" + "\n");
                        intent.putExtra(WRITE_DATA, "\n"+"���誧蝏�????,�摰���氖??..."+ "\n");
                        intent0.putExtra(WRITE_DATA, "\n"+"���誧蝏�????,�摰���氖??..."+ "\n");
                        //嚙�????���蕭???
                        if(packet.setPacketParam(OPCODE_WAIT_FALL_IN_UNLOCK, null, null)){
                            bDataSend = packet.encodePacket(null);
                        }
                        writeLlsAlertLevel(null,bDataSend);
                    } else {
                        intent.putExtra(EXTRA_DATA, lock_no+"�摰���??!"+"\n");
                        intent0.putExtra(EXTRA_DATA, lock_no+"�摰���??!"+"\n");
                        intent.putExtra(WRITE_DATA, "������!" + "\n");
                        intent0.putExtra(WRITE_DATA, "������!" + "\n");
                    }
                    //must clear cache
                    lock_no=null;
                } else {
                    switch(errCode){
                        case ERRCODE_MOTORTIMEOUT:
                            //must clear cache
                            lock_no=null;
                            //敹◆餈��??0,���撅�����誧蝏剖��??
                            unlockCount=0;
                            //must clear
                            MainActivity.handLockNumber=null;
                            //must clear
                            operateState=null;
                            if(unlockType.equals(LockSetActivity.unlockContinue)){
                                intent.putExtra(EXTRA_DATA, "���仃韐�!" + "\n");
                                intent0.putExtra(EXTRA_DATA, "���仃韐�!" + "\n");
                                intent.putExtra(WRITE_DATA, "\n"+"����????,�摰���氖??..."+ "\n");
                                intent0.putExtra(WRITE_DATA, "\n"+"����????,�摰���氖??..."+ "\n");
                                //嚙�????���蕭???
                                if(packet.setPacketParam(OPCODE_WAIT_FALL_IN_UNLOCK, null, null)){
                                    bDataSend = packet.encodePacket(null);
                                }
                                writeLlsAlertLevel(null,bDataSend);
                            }
                            else if(unlockType.equals(LockSetActivity.unlockTwo)){
                                intent.putExtra(EXTRA_DATA, "���仃韐�!" + "\n");
                                intent0.putExtra(EXTRA_DATA, "���仃韐�!" + "\n");
                                intent.putExtra(WRITE_DATA, "\n"+"����????,�摰���氖??..."+ "\n");
                                intent0.putExtra(WRITE_DATA, "\n"+"����????,�摰���氖??..."+ "\n");
                                //嚙�????���蕭???
                                if(packet.setPacketParam(OPCODE_WAIT_FALL_IN_UNLOCK, null, null)){
                                    bDataSend = packet.encodePacket(null);
                                }
                                writeLlsAlertLevel(null,bDataSend);
                            }
                            else{
                                intent.putExtra(EXTRA_DATA, "���仃韐�!" + "\n");
                                intent0.putExtra(EXTRA_DATA, "���仃韐�!" + "\n");
                                intent.putExtra(WRITE_DATA, "���仃韐�,霂琿����??!" + "\n");
                                intent0.putExtra(WRITE_DATA, "���仃韐�,霂琿����??!" + "\n");
                            }
                            break;
                    }
                }
            }
        }else if (cmdCode==SetActivity.OPCODE_GET_PARAM) {
            if (errCode==ERRCODE_SUCC) {
                if (sendCount==0) {
                    sendCount++;
                    string=new String(bData);
                    state=getActivityManager();
                    if(state!=null){
                        //check DeviceControlActivity is open or die
                        if(state.equals("com.android.bluetooth.DeviceScanActivity")||state.equals("com.android.bluetooth.DeviceControlActivity")){
                            //service��歲頧祇△����??
                            intent0.setClass(this, SetActivity.class);
                            intent0.setFlags(intent0.FLAG_ACTIVITY_NEW_TASK);
                            intent0.putExtra(SetActivity.S00SerialNumber, string);
                        }else if(state.equals("com.example.app.SetActivity")){
                            //refresh SetActivity
                            intent.putExtra(SetActivity.S00SerialNumber, string);
                        }else if(MainActivity.setS00!=null){
                            //refresh DeviceScanActivity
                        }
                    }
                    //���00蝖砌辣��
                    getS00Data(SetActivity.VS_FW51_VER_GOT);
                }else if (sendCount==SetActivity.VS_S00Name) {
                    //����??
                }else if (sendCount==SetActivity.VS_FW51_VER_GOT) {
                    sendCount++;
                    intent0.putExtra(SetActivity.S00SoftwareVersion, TypeConvert.byte2hex(bData[1]));
                    intent.putExtra(SetActivity.S00SoftwareVersion, TypeConvert.byte2hex(bData[1]));
                    //���00蝖砌辣��
                    getS00Data(SetActivity.VS_HW_GOT);
                }else if (sendCount==SetActivity.VS_HW_GOT) {
                    sendCount++;
                    intent0.putExtra(SetActivity.S00HardwareVersion, TypeConvert.byte2hex(bData[1]));
                    intent.putExtra(SetActivity.S00HardwareVersion, TypeConvert.byte2hex(bData[1]));
                    //���00��宏??
                    getS00Data(SetActivity.VS_LOFFSET_GOT);
                }else if (sendCount==SetActivity.VS_LOFFSET_GOT) {
                    sendCount++;
                    if (bData[1]<0) {
                        sepIdx= bData[1]&0x7f;
                        string=SetActivity.negative;
                    } else {
                        sepIdx= bData[1];
                        string=SetActivity.positive;
                    }
                    intent0.putExtra(SetActivity.direction, string);
                    intent.putExtra(SetActivity.direction, string);
                    intent0.putExtra(SetActivity.offset, Integer.toString(sepIdx));
                    intent.putExtra(SetActivity.offset, Integer.toString(sepIdx));
                    //���00霈曄蔭??
                    getS00Data(SetActivity.VS_ALL_GOT);
                }else if (sendCount==SetActivity.VS_ALL_GOT) {
                    if(state!=null){
                        //check DeviceControlActivity is open or die
                        if(state.equals("com.android.bluetooth.DeviceScanActivity")||state.equals("com.android.bluetooth.DeviceControlActivity")){
                            intent0.putExtra(SetActivity.set, TypeConvert.byte2hex(bData[1]));
                            startActivity(intent0);
                        }else if(state.equals("com.example.app.SetActivity")){
                            //refresh SetActivity
                            intent.putExtra(SetActivity.set, TypeConvert.byte2hex(bData[1]));
                        }else if(MainActivity.setS00!=null){
                            //refresh DeviceScanActivity
                        }
                    }
                    //??0
                    sendCount=0;
                }
            } else {
                switch(errCode){
                    case opcode_password_error:
                        //�撘�餈
                        intent.putExtra(SetActivity.error, "���00��憭梯揖!" + "\n");
                        break;
                }
            }
        } else if (cmdCode==SetActivity.OPCODE_SET_PARAM) {
            if (errCode==ERRCODE_SUCC) {
                intent.putExtra(EXTRA_DATA, "靽格������"+ "\n");
                //get S00SerialNumber to refresh changed
                getS00Data(SetActivity.VS_S00SerialNumber);
            } else {
                switch(errCode){
                    case ERRCODE_OPCODE_SET_PARAM:
                        intent.putExtra(EXTRA_DATA, "靽格憭梯揖!" + "\n");
                        break;
                }
            }
        } else if (cmdCode==opcode_sleep) {
            if (errCode==ERRCODE_SUCC) {
                state=getActivityManager();
                if(state!=null){
                    //check DeviceControlActivity is open or die
                    if(state.equals("com.android.bluetooth.DeviceControlActivity")||state.equals("com.example.app.SetActivity")){
                        intent.putExtra(WRITE_DATA, string_sleep);
                    }
                }
            } else {
                switch(errCode){
                    case opcode_password_error:
                        //�撘�餈
                        intent.putExtra(EXTRA_DATA, "��笆憭梯揖,颲撖��秤!" + "\n");
                        break;
                }
            }
        } else if (cmdCode==opcode_close) {
            if (errCode==ERRCODE_SUCC) {
                state=getActivityManager();
                if(state!=null){
                    //check DeviceControlActivity is open or die
                    if(state.equals("com.example.app.SetActivity")||state.equals("com.android.bluetooth.DeviceControlActivity")||state.equals("com.android.bluetooth.DeviceScanActivity")){
                        intent.putExtra(WRITE_DATA, string_close);
                    }
                }
            } else {
                switch(errCode){
                    case opcode_password_error:
                        //�撘�餈
                        intent.putExtra(EXTRA_DATA, "��笆憭梯揖,颲撖��秤!" + "\n");
                        break;
                }
            }
        } else if (cmdCode==opcode_password) {
            if (errCode==ERRCODE_SUCC) {
                intent.putExtra(EXTRA_DATA, bData + "\n");
                state=FileStream.connectRecord;
                readStream(state,null,string);
            } else {
                switch(errCode){
                    case opcode_password_error:
                        //�撘�餈
                        intent.putExtra(EXTRA_DATA, "��笆憭梯揖,颲撖��秤!" + "\n");
                        break;
                }
            }
        } else if (cmdCode==opcode_sendS00Up) {
            if (errCode==ERRCODE_SUCC) {
                //get S00Up data
                getS00UpData();
            } else{
                switch(errCode){
                    case opcode_password_error:

                }
            }
        } else if (cmdCode==opcode_userReload) {
            if (errCode==ERRCODE_SUCC) {
                state=FileStream.userDownload;
                String defineResult=fileStream.checkCatalog();
                if (defineResult.equals(state)) {
                    //霂餃��辣��
                    byte[] resultArray=fileStream.readFile("xxxxx");
                    byte[] lockArray=new byte[9];
                    byte[] pwdArray=new byte[15];
                    if (resultArray!=null) {
                        for (int i = schedule; i <=resultArray.length; i++) {
                            //�甈∠�霈啣�蛹銝活韏�??
                            schedule=i+1;
//							string
                            //??1??
                            if (i==8) {
                                //����
                                System.arraycopy(resultArray, 0, lockArray, 0, 9);
                                //嚙�????嚙�???
                                decodeUserLock(lockArray);
                                break;
                            } else if (i==35) {
                                //��撖��
                                System.arraycopy(resultArray, 21, pwdArray, 0, 15);
                                //嚙�????嚙�?����??
                                decodeUserLock(pwdArray);
                                break;
                            }else if (i>35) {
                                if ((i-36*j)==9) {
                                    //����
                                    System.arraycopy(resultArray, 36*j, lockArray, 0, 9);
                                    //嚙�????嚙�???
                                    decodeUserLock(lockArray);
                                    break;
                                } else if ((i-36*j)==36) {
                                    //��撖��
                                    System.arraycopy(resultArray, 21+36*j, pwdArray, 0, 15);
                                    j++;
                                    //嚙�????嚙�?����??
                                    decodeUserLock(pwdArray);
                                    break;
                                } else{
                                    //
                                }
                            } else {
                                //
                            }
                        }
                        //嚙�????蝏�蕭???
                        if (schedule==resultArray.length+1) {
                            //嚙�????嚙�???
                            state=CommandPacker.encode_userRelationFix;
                            if(packet.setPacketParam(CommandPacker.opcode_userRelationSuffix, null, state)){
                                bDataSend = packet.encodePacket(state);
                            }
                            writeLlsAlertLevel(null,bDataSend);
                        } else {

                        }
                    } else {
                        //
                    }
                } else {

                }
            } else{
                switch(errCode){
                    case opcode_password_error:

                }
            }
        } else {
            intent.putExtra(EXTRA_DATA, "���隞�!" + "\n");
        }
        sendBroadcast(intent);
    }
    //get S00Up data only one
    private void getS00UpData(){
        state=FileStream.S00Up;
        string=fileStream.checkCatalog();
        if (string.equals(fileStream.catalog)) {
            string=fileStream.checkFile(state);
            if (string.equals(fileStream.S00UpFileName)) {
                //get S00Up data
                byteArray=fileStream.readFile("xxxx");
                if (byteArray!=null) {
                    for (int i = sepIdx; i <=byteArray.length; i++) {
                        //�甈∠�霈啣�蛹銝活韏�??
                        sepIdx=i+1;
                        //cycle send
                        if (i==17) {
                            //split send S00Up start
                            System.arraycopy(byteArray, 0, splitArray, 0, 18);
                            //send
                            decodeS00Up(splitArray);
                            schedule=1;
                            sendBroadcast(intent);
                        }else if ((i-18*j)==18) {
                            //split S00Up
                            System.arraycopy(byteArray, 18*j, splitArray, 0, 18);
                            j++;
                            //send
                            decodeS00Up(splitArray);
                            if (j==57*schedule) {
                                schedule++;
                                break;
                            } else {
                                //
                            }

                        } else if ((byteArray.length-18*j)<18) {
                            //split S00Up end
                            System.arraycopy(byteArray, 18*j, splitArray, 0, byteArray.length-18*j);
                            j++;
                            sepIdx=byteArray.length;
                            //send
                            decodeS00Up(splitArray);
                        } else{
                            //
                        }
                    }
                } else {
                    //
                }
            }else{
                intent.putExtra(WRITE_DATA, "��辣:"+string+ "\n");
            }
        } else {
            //
            intent.putExtra(WRITE_DATA, "��辣頝臬��:"+string+ "\n");
        }
        sendBroadcast(intent);
    }
    //get S00 data
    private void getS00Data(byte state){
        //get S00�隞嗥�,蝖砌辣��,��宏??,霈曄蔭??
        SetActivity.param_data[0]=state;
        SetActivity.param_data[1]=(byte)0;
        if(packet.setPacketParam(SetActivity.OPCODE_GET_PARAM, SetActivity.param_data, null)){
            bDataSend = packet.encodePacket(null);
            writeLlsAlertLevel(null,bDataSend);
        }
    }
    //split send S00Up
    private void decodeS00Up(byte[] byteArray){
        state=UpS00Activity.dataUpS00;
        if(packet.setPacketParam((byte)0, byteArray, state)){
            bDataSend = packet.encodePacket(state);
        }
        writeLlsAlertLevel(null,bDataSend);
        if (sepIdx==byteArray.length) {
            intent.putExtra(WRITE_DATA, "S00�隞嗆�撌脣��??!" + "\n");
            //clear cache
            sepIdx=0;
            schedule=0;
        } else {
            intent.putExtra(WRITE_DATA, new String(bDataSend) + "\n");
        }
    }
    //������������??
    private void decodeUserLock(byte[] byteArray){
        state=CommandPacker.encode_userRelation;
        if(packet.setPacketParam(CommandPacker.opcode_userRelationPrefix, byteArray, state)){
            bDataSend = packet.encodePacket(state);
        }
        writeLlsAlertLevel(null,bDataSend);
        intent.putExtra(WRITE_DATA, new String(bDataSend) + "\n");
    }
    //
    public void download_single_lock_sn() {
        //get userNameValue
        byteArray=fileStream.fileStream(FileStream.userLogin, FileStream.read, null);
        string=new String(byteArray);
        stringArray=string.split(",");
        string=stringArray[0];
        request = new String(CommonResources.getParam("lock_auth_url"))+ "u=" + string + "&l=" + lock_no + "&k="+ MainActivity.IMEI + "&a=2";
        //must instantiation
        HttpConnector httpConnector=new HttpConnector();
        //Cannot make a static reference to the non-static method setUrl(String) from the type HttpConnector
        httpConnector.setUrl(request);
        httpConnector.open();
        httpConnector.start();
        //callback dataComing
        httpConnector.Register(this);
    }
    //
    public void dataComing(byte[] bData) {
        int startI = -1;
        int endI = -1;
        String recv = new String(bData);
        if ((startI = recv.indexOf("true")) != -1) {
            endI = recv.indexOf("endop", startI);
            pwd= recv.substring(startI + 4, endI);
            //���蕭????嚙�?����??
            fileStream.decodeOpt_pwd();
        } else {
            pwd =null;
            //must clear
            operateState=null;
            //撉���仃韐�
            intent.putExtra(WRITE_DATA, "霂亦��摰���迨���!" + "\n");
            intent0.putExtra(WRITE_DATA, "霂亦��摰���迨���!" + "\n");
            sendBroadcast(intent);
        }
    }
    //
    public void showData(){
        if(unlockCount==1){
            if (unlockType.equals(LockSetActivity.unlockTwo)) {
                intent.putExtra(EXTRA_DATA, "甇�韐�2嚙�?��葉...\n"+"??:"+pwd + "\n"+"??:"+temp_pwd + "\n");
                intent0.putExtra(EXTRA_DATA, "甇�韐�2嚙�?��葉...\n"+"??:"+pwd + "\n"+"??:"+temp_pwd + "\n");
            } else if (unlockType.equals(LockSetActivity.unlockThree)) {
                intent.putExtra(EXTRA_DATA, "甇�韐�3嚙�?��葉...\n"+"??:"+pwd + "\n"+"??:"+temp_pwd + "\n");
                intent0.putExtra(EXTRA_DATA, "甇�韐�3嚙�?��葉...\n"+"??:"+pwd + "\n"+"??:"+temp_pwd + "\n");
            } else if (unlockType.equals(LockSetActivity.unlockFour)) {
                intent.putExtra(EXTRA_DATA, "甇�韐�4嚙�?��葉...\n"+"??:"+pwd + "\n"+"??:"+temp_pwd + "\n");
                intent0.putExtra(EXTRA_DATA, "甇�韐�4嚙�?��葉...\n"+"??:"+pwd + "\n"+"??:"+temp_pwd + "\n");
            }
        } else if(unlockCount==2){
            if (unlockType.equals(LockSetActivity.unlockTwo)) {
                intent.putExtra(EXTRA_DATA, "甇�韐�2??��葉...\n"+"??:"+pwd + "\n"+"??"+temp_pwd + "\n");
                intent0.putExtra(EXTRA_DATA, "甇�韐�2??��葉...\n"+"??:"+pwd + "\n"+"??"+temp_pwd + "\n");
            } else if (unlockType.equals(LockSetActivity.unlockThree)) {
                intent.putExtra(EXTRA_DATA, "甇�韐�3??��葉...\n"+"??:"+pwd + "\n"+"??"+temp_pwd + "\n");
                intent0.putExtra(EXTRA_DATA, "甇�韐�3??��葉...\n"+"??:"+pwd + "\n"+"??"+temp_pwd + "\n");
            } else if (unlockType.equals(LockSetActivity.unlockFour)) {
                intent.putExtra(EXTRA_DATA, "甇�韐�4??��葉...\n"+"??:"+pwd + "\n"+"??"+temp_pwd + "\n");
                intent0.putExtra(EXTRA_DATA, "甇�韐�4??��葉...\n"+"??:"+pwd + "\n"+"??"+temp_pwd + "\n");
            }
        }
    }
    //
    public void readStream(String state,String operateState,String string){
        string=fileStream.checkCatalog();
        if (string!=null) {
            string=fileStream.checkFile(state);
            if (string!=null) {
                byteArray=fileStream.readFile("xxx");
                string=new String(byteArray);
                if (string!=null) {
                    if (state.equals(FileStream.connectRecord)) {
                        if (operateState.equals(FileStream.connectRecord)) {
                            byteArray = (bluetoothDevice.getAddress()+",").getBytes();
                            byteArray=fileStream.writeFile(state,byteArray);
                        } else if (operateState.equals(operateState_disConnect)) {
                            //�������霈�??
                            byteArray=fileStream.writeFile(state,null);
                        }
                    } else if (state.equals(FileStream.userDownload)||state.equals(FileStream.log)) {
                        if (string.equals("")) {
                            if (state.equals(FileStream.userDownload)) {
                                //must clear
                                operateState=null;
                                intent.putExtra(WRITE_DATA, "霂瑕��蝸������靽⊥??,�����嚙�?!" + "\n");
                                intent0.putExtra(WRITE_DATA, "霂瑕��蝸������靽⊥??,�����嚙�?!" + "\n");
                            } else if (state.equals(FileStream.log)) {
                                Log.e(TAG, "log��辣銝箇征!");
                            }
                        } else {
                            stringArray=string.split(";");
                            length=stringArray.length;
                            //���銵�
                            if (state.equals(FileStream.userDownload)) {
                                //���銵�
                                if (operateState.equals(operatestate_unlock)) {
                                    for(int i = 0; i <length ; i++){
                                        string=stringArray[i];
                                        String[] stringArray=string.split(",");
                                        string=stringArray[0];
                                        pwd=stringArray[1];
                                        //蝳餌瑪????
                                        if (string.equals(lock_no)) {
                                            string=string_lockPermission;
                                            //���蕭????嚙�?����??
                                            decodeOpt_pwd();
                                            break;
                                        }
                                    }
                                    //lockPwd not exist
                                    if (!string.equals(string_lockPermission)) {
                                        //must clear
                                        operateState=null;
                                        intent.putExtra(WRITE_DATA, string_lockPermission);
                                        intent0.putExtra(WRITE_DATA, string_lockPermission);
                                    }
                                } else if (operateState.equals(operateState_uploadLog)||state.equals(FileStream.log)) {
                                    //蝳餌瑪??�������扇??
                                    //get userId
                                    byteArray=fileStream.fileStream(FileStream.userLogin, FileStream.read, string.getBytes());
                                    string=new String(byteArray);
                                    stringArray=string.split(",");
                                    userId=stringArray[2];
                                }
                            }
                        }
                    }
                }
            } else {
                //
            }
        } else {
            //
        }
    }
    //銝��??��扇敶��??
    private void uploadLog(String logState){
        //���頂蝏�
        SimpleDateFormat formatter = new SimpleDateFormat ("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());//������
        String systemTime = formatter.format(curDate);
        //���serID
        if (unlockType.equals(LockSetActivity.unlockLogin)) {
            //�蝥踹�????:
            //get userId
            string=FileStream.userLogin;
            byteArray=fileStream.fileStream(string, FileStream.read, string.getBytes());
            string=new String(byteArray);
            stringArray=string.split(",");
            userId=stringArray[2];
        } else if (unlockType.equals(LockSetActivity.unlockUser)) {
            //蝳餌瑪????(������):
//			readStream(FileStream.userDownload,operateState_uploadLog,null);
            //蝳餌瑪??�������扇??
            //get userId
            byteArray=fileStream.fileStream(FileStream.userLogin, FileStream.read, null);
            string=new String(byteArray);
            stringArray=string.split(",");
            userId=stringArray[2];
        } else {
            //must clear
            operateState=null;
            intent.putExtra(WRITE_DATA, "��/蝳餌瑪�撘�????!" + "\n");
            intent0.putExtra(WRITE_DATA, "��/蝳餌瑪�撘�????!" + "\n");
        }
        if (logState.equals(operatestate_unlock)) {
            //??��扇??
            request = upload_log_serviceId + MainActivity.IMEI+userId+lock_no+systemTime+1;
        } else if (logState.equals(operatestate_lock)) {
            //���扇敶�
            request = upload_log_serviceId + MainActivity.IMEI+userId+lock_no+systemTime+0;
        } else {
            //
        }
        //霂瑟����
        requestPacket = CommonResources.createRequestPacket(request);
        //撉����
        boolean wlanFlag=NetworkConnect.checkNet(getApplicationContext());
        if (wlanFlag) {
            //餈��??,嚙�????嚙�?/���扇敶縑�.
            SocketConnect socketConnect=new SocketConnect();
            responsePacket=socketConnect.sendDate(requestPacket);
            //�����餈�??
            CommandPacker commandPacker=new CommandPacker();
            commandPacker.decodeResultFlag(responsePacket);
            // 蝑��悖����
            if (CommandPacker.succ_flag) {
                //??/���扇敶�����
            } else {
                //??/���扇敶��仃韐�
                writeStream(requestPacket);
            }
            //���銵�
        } else {
            //餈蝵��??,����隞��??!
            writeStream(requestPacket);
        }
    }
    //??/���扇敶���APP/log.txt��辣??,銋����
    private void writeStream(String requestPacket){
        String state=FileStream.log;
        String string=fileStream.checkCatalog();
        if (string!=null) {
            //����(����??)��辣
            string=fileStream.checkFile(state);
            if (string!=null) {
                //����
                bDataSend=(requestPacket+",").getBytes();
                bDataSend=fileStream.writeFile(state,bDataSend);
                //������������
                if (bDataSend.equals("OK")) {
                    //蝵�撘�,??/���扇敶歇摮��APP/log.txt��辣??!
                } else {
                    //

                }
            } else {
                //銝�遙雿�����
            }
        } else {
            //銝�遙雿�����
        }
    }
    //嚙�????������嚙�???
    public void send() {
        //嚙�????嚙�???
        state=CommandPacker.encode_userRelationFix;
        if (bluetoothDevice.getAddress()!=null) {
            stringArray=bluetoothDevice.getAddress().split(":");
            for (int i = 0; i < stringArray.length; i++) {
                stringBuffer.append(stringArray[i]);
            }
            string=stringBuffer.toString();
            bDataSend=string.getBytes();
        } else {

        }
        if(packet.setPacketParam(CommandPacker.opcode_userRelationPrefix, bDataSend, state)){
            bDataSend = packet.encodePacket(state);
        }
        writeLlsAlertLevel(null,bDataSend);
        sendBroadcast(intent);
    }
    //
    String getActivityManager(){
        //get ActivityManager
        ActivityManager activityManager=(ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        //one
        state=activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
        //split one by one
//		List<RunningTaskInfo> list=activityManager.getRunningTasks(1);
//		RunningTaskInfo info=list.get(0);
//		ComponentName componentName=info.topActivity;
//		string=componentName.getClassName();
        return state;
    }
    public boolean bluetoothNameSet(byte[] byteArray){
        if (bluetoothGattServiceSet== null|bluetoothGattCharacteristicSet == null) {
            Log.e(TAG, "service or charateristic not found!");
            return false;
        }
        boolean status = false;
        int storedLevel = bluetoothGattCharacteristicSet.getWriteType();
        Log.w(TAG, "靽格BluetoothName��掩??+storedLevel() - storedLevel=" + storedLevel);
        bluetoothGattCharacteristicSet.setValue(byteArray);
        bluetoothGattCharacteristicSet.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        //��餈�oolean??,���蝏�挽ble嚙�????��嚙�???
        status = bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristicSet);
        return status;
    }
    //
    public String getBaudRate(){
        if (bluetoothGattServiceSet == null|bluetoothGattCharacteristicBaudRate == null) {
            Log.e(TAG, "service or charateristic not found!");
            return null;
        }
//    	bluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic0,false);
        byteArray = bluetoothGattCharacteristicBaudRate.getValue();
        bluetoothGatt.readCharacteristic(bluetoothGattCharacteristicBaudRate);
        if (byteArray==null) {
            string="null";
        } else {
            string=new String(byteArray);
        }
        return string;
    }
    //
    public boolean setBaudRate(){
        if (bluetoothGattServiceSet == null|bluetoothGattCharacteristicBaudRate == null) {
            Log.e(TAG, "service or charateristic not found!");
            return false;
        }
        boolean status = false;
        int storedLevel = bluetoothGattCharacteristicBaudRate.getWriteType();
        Log.w(TAG, "靽格BluetoothName��掩??+storedLevel() - storedLevel=" + storedLevel);
        string=SetActivity.baudRateDefaultValue;
        bluetoothGattCharacteristicBaudRate.setValue(string.getBytes());
        bluetoothGattCharacteristicBaudRate.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        //��餈�oolean??,���蝏�挽ble嚙�????��嚙�???
        status = bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristicBaudRate);
        return status;
    }
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }
    //
    class MyTask extends AsyncTask<String, String, String>{
        @Override
        protected String doInBackground(String... arg0) {
            // TODO Auto-generated method stub
//			fileStream.PWD();
            PWD();
            return null;
        }
    }
    //
    public boolean PWD(){
        boolean flag=false;
        byteArray=fileStream.fileStream(FileStream.jsonFile, FileStream.read, null);
        if (byteArray==null) {
            //must clear
            operateState=null;
            //��內靽⊥
            intent.putExtra(WRITE_DATA, "霂瑟�撖澆lock.json��辣�����PP��辣憭嫣��!" + "\n");
            intent0.putExtra(WRITE_DATA, "霂瑟�撖澆lock.json��辣�����PP��辣憭嫣��!" + "\n");
        }else if (byteArray.equals("")) {
            //must clear
            operateState=null;
            //��內靽⊥
            intent.putExtra(WRITE_DATA, "APP��辣憭寥��ock.json銝箇征!" + "\n");
            intent0.putExtra(WRITE_DATA, "APP��辣憭寥��ock.json銝箇征!" + "\n");
        }else {
            try {
                JSONObject jsonObject = new JSONObject(new String(byteArray));
                if (!jsonObject.has(lock_no)) {
                    //must clear
                    operateState=null;
                    //��內靽⊥
                    intent.putExtra(WRITE_DATA, "��Json��辣,銝���甇日��!" + "\n");
                    intent0.putExtra(WRITE_DATA, "��Json��辣,銝���甇日��!" + "\n");
                } else {
                    pwd = jsonObject.getString(lock_no);
                    //���蕭????嚙�?����??
                    decodeOpt_pwd();
                    sendBroadcast(intent);
                    flag=true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                //must clear
                operateState=null;
                intent.putExtra(WRITE_DATA, e + "\n");
                intent0.putExtra(WRITE_DATA, e + "\n");
            }
        }
        return flag;
    }
    //���蕭????嚙�?����??
    public void decodeOpt_pwd(){
        int temp=0;
        if(pwd!=null && (pwd.length()==10||pwd.length()==15)){
            state=CommandPacker.encode_openLock;
            if(pwd.length()==10){
                //���銵�
                if (unlockType.equals(LockSetActivity.unlockTwo)||unlockType.equals(LockSetActivity.unlockThree)||unlockType.equals(LockSetActivity.unlockFour)) {
                    if (unlockCount>=2) {
                        unlockCount=0;
                    }
                    unlockCount++;
                    for(sepIdx=0;sepIdx<5;sepIdx++){
                        if(sepIdx==0){
                            if(unlockCount==1){
                                if (unlockType.equals(LockSetActivity.unlockTwo)) {
                                    temp=((Integer.parseInt(pwd.substring(sepIdx*2, sepIdx*2+2)))+pwdOffsetTwo10);
                                } else if (unlockType.equals(LockSetActivity.unlockThree)) {
                                    temp=((Integer.parseInt(pwd.substring(sepIdx*2, sepIdx*2+2)))+pwdOffsetThree10);
                                } else if (unlockType.equals(LockSetActivity.unlockFour)) {
                                    temp=((Integer.parseInt(pwd.substring(sepIdx*2, sepIdx*2+2)))+pwdOffsetFour10);
                                }else {

                                }
                            }else if(unlockCount==2){
                                if (unlockType.equals(LockSetActivity.unlockTwo)) {
                                    temp=((Integer.parseInt(pwd.substring(sepIdx*2, sepIdx*2+2)))-pwdOffsetTwo10);
                                } else if (unlockType.equals(LockSetActivity.unlockThree)) {
                                    temp=((Integer.parseInt(pwd.substring(sepIdx*2, sepIdx*2+2)))-pwdOffsetThree10);
                                } else if (unlockType.equals(LockSetActivity.unlockFour)) {
                                    temp=((Integer.parseInt(pwd.substring(sepIdx*2, sepIdx*2+2)))-pwdOffsetFour10);
                                }else {

                                }
                            }
                        }else{
                            temp=(Integer.parseInt(pwd.substring(sepIdx*2, sepIdx*2+2)));
                        }
                        if (temp<10) {
                            temp_pwd+="0"+temp;
                        }else{
                            temp_pwd+=temp;
                        }
                    }
                    showData();
                }else{
                    //�隞�??��??
                    temp_pwd=pwd;
                }
                byte[] open_lst = new byte[9];
                for(sepIdx=0;sepIdx<5;sepIdx++){
                    open_lst[sepIdx]=(byte)(Integer.parseInt(temp_pwd.substring(sepIdx*2, sepIdx*2+2)));
                }
                byte[] lock_no_bytes = TypeConvert.bigEndian_int2byte(Integer.parseInt(lock_no));
                for(int i=5;i<9;i++){
                    open_lst[i]=lock_no_bytes[i-5];
                }
                if(packet.setPacketParam(CommandPacker.OPCODE_OPEN_LOCK, open_lst,state)){
                    bDataSend = packet.encodePacket(state);
//				send(bDataSend,0,bDataSend.length);
                }
            }else if((pwd.length()==15)){
                //���銵�
                if (unlockType.equals(LockSetActivity.unlockTwo)||unlockType.equals(LockSetActivity.unlockThree)||unlockType.equals(LockSetActivity.unlockFour)) {
                    if (unlockCount>=2) {
                        unlockCount=0;
                    }
                    unlockCount++;
                    for(sepIdx=0;sepIdx<5;sepIdx++){
                        if(sepIdx==0){
                            if(unlockCount==1){
                                if (unlockType.equals(LockSetActivity.unlockTwo)) {
                                    temp=((Integer.parseInt(pwd.substring(sepIdx*3, sepIdx*3+3)))+pwdOffsetTwo15);
                                } else if (unlockType.equals(LockSetActivity.unlockThree)) {
                                    temp=((Integer.parseInt(pwd.substring(sepIdx*3, sepIdx*3+3)))+pwdOffsetThree15);
                                } else if (unlockType.equals(LockSetActivity.unlockFour)) {
                                    temp=((Integer.parseInt(pwd.substring(sepIdx*3, sepIdx*3+3)))+pwdOffsetFour15);
                                }
                            }else if(unlockCount==2){
                                if (unlockType.equals(LockSetActivity.unlockTwo)) {
                                    temp=((Integer.parseInt(pwd.substring(sepIdx*3, sepIdx*3+3)))-pwdOffsetTwo15);
                                } else if (unlockType.equals(LockSetActivity.unlockThree)) {
                                    temp=((Integer.parseInt(pwd.substring(sepIdx*3, sepIdx*3+3)))-pwdOffsetThree15);
                                } else if (unlockType.equals(LockSetActivity.unlockFour)) {
                                    temp=((Integer.parseInt(pwd.substring(sepIdx*3, sepIdx*3+3)))-pwdOffsetFour15);
                                }
                            }
                        }else{
                            temp=(Integer.parseInt(pwd.substring(sepIdx*3, sepIdx*3+3)));
                        }
                        if (temp<10) {
                            temp_pwd+="00"+temp;
                        }else if((temp>9)&&(temp<100)){
                            temp_pwd+="0"+temp;
                        }else{
                            temp_pwd+=temp;
                        }
                    }
                    showData();
                } else {
                    //�隞�??��??
                    temp_pwd=pwd;
                }
                byte[] open_lst = new byte[14];
                sepIdx=0;
                for(int j=0;j<5;j++){
                    open_lst[sepIdx]=(byte)((Integer.parseInt(temp_pwd.substring(j*3, j*3+3))>>8)&0x000000FF);
                    sepIdx++;
                    open_lst[sepIdx]=(byte)(Integer.parseInt(temp_pwd.substring(j*3, j*3+3))&0x000000FF);
                    sepIdx++;
                }
                //蝥輻�憸�
                byte[] lock_no_bytes = TypeConvert.bigEndian_int2byte(Integer.parseInt(lock_no));
                for(int i=10;i<14;i++){
                    open_lst[i]=lock_no_bytes[i-10];
                }
                if(packet.setPacketParam(CommandPacker.OPCODE_OPEN_LOCK15, open_lst,state)){
                    bDataSend = packet.encodePacket(state);
                }
            }
            //must clear
            temp_pwd="";
//        	writeLlsAlertLevel(null,bDataSend);
            int Dlength=bDataSend.length;
            if (Dlength<21) {
                writeLlsAlertLevel(null,bDataSend);
            } else {
                //���蕭????
                byte[] bDataSend0=new byte[20];
                System.arraycopy(bDataSend, 0, bDataSend0, 0, 20);
                writeLlsAlertLevel(null,bDataSend0);
                Dlength-=20;
                int i=0;
                if((Dlength-20)>20) {
                    i++;
                    Dlength-=20;
                    bDataSend1=new byte[20];
                    System.arraycopy(bDataSend, 20*i, bDataSend1, 0, 20);
//					writeLlsAlertLevel(null,bDataSend1);
                    writeLlsAlertLevel(split,bDataSend1);
                }else {
                    bDataSend1=new byte[Dlength];
                    System.arraycopy(bDataSend, 20*(i+1), bDataSend1, 0, Dlength);
//					writeLlsAlertLevel(null,bDataSend1);
                    writeLlsAlertLevel(split,bDataSend1);
                }
            }
            intent.putExtra(WRITE_DATA, "??��葉,�摰���氖??..." + "\n");
            intent0.putExtra(WRITE_DATA, "??��葉,�摰���氖??..." + "\n");
        }else if (pwd==null||pwd.contains("")) {
            //must clear
            operateState=null;
            intent.putExtra(WRITE_DATA, "撘������銝�??!" + "\n");
            intent0.putExtra(WRITE_DATA, "撘������銝�??!" + "\n");
        }else{
            //must clear
            operateState=null;
            intent.putExtra(WRITE_DATA, "颲����摨行�秤,�������10���15雿摨衣�������!" + "\n");
            intent0.putExtra(WRITE_DATA, "颲����摨行�秤,�������10���15雿摨衣�������!" + "\n");
        }
        MainActivity.handLockNumber=null;
    }
    //
    void userDownload(){
        byteArray=fileStream.fileStream(FileStream.userDownload, FileStream.read, null);
        string=new String(byteArray);
        if (string.equals("")) {
            //must clear
            operateState=null;
            intent.putExtra(WRITE_DATA, "霂瑕��蝸������靽⊥,����������" + "\n");
            intent0.putExtra(WRITE_DATA, "霂瑕��蝸������靽⊥,����������" + "\n");
        } else {
            stringArray=string.split(";");
            length=stringArray.length;
            for(int i = 0; i <length ; i++){
                string=stringArray[i];
                String[] stringArray=string.split(",");
                string=stringArray[0];
                pwd=stringArray[1];
                //蝳餌瑪????
                if (string.equals(lock_no)) {
                    string=string_lockPermission;
                    //���蕭????嚙�?����??
                    decodeOpt_pwd();
                    break;
                }
            }
            //lockPwd not exist
            if (!string.equals(string_lockPermission)) {
                //must clear
                operateState=null;
                intent.putExtra(WRITE_DATA, string_lockPermission);
                intent0.putExtra(WRITE_DATA, string_lockPermission);
            }
        }
    }
}
