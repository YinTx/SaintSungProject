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

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.saintsung.saintpmc.MainActivity;
import com.saintsung.saintpmc.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivity extends AppCompatActivity {
	private BluetoothAdapter bluetoothAdapter;
	RadioButton radioButton;
	private boolean mScanning,b;
	private Handler handler;
	private static final int REQUEST_ENABLE_BT = 1;
	// 10秒后停止查找搜索.
	private static final long SCAN_PERIOD = 10000;
	public static int intState=3;
	private String string,deviceAddress,dialogOperateSelected="disconnect";
	public static final String login_operate = "loginOperate";
	public static final String disconnect = "disconnect";
	public static final String handLockNumber = "handLockNumber";
	public static final String setS00 = "setS00";
	//add new
	ArrayList<BluetoothDevice> disconnectedDeviceArrayList= new ArrayList<BluetoothDevice>();
	ArrayList<BluetoothDevice> connectedDeviceArrayList= new ArrayList<BluetoothDevice>();
	public static final String device_name = "device_name";
	public static final String device_address = "device_address";
	private ListView disconnectedDeviceListView;
	private ListView connectedDeviceListView;
	BluetoothLeService bluetoothLeService;
	private byte[] byteArray;
	CommandPacker packet = new CommandPacker();
	FileStream fileStream=new FileStream();


	// Code to manage Service lifecycle.
	private final ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName,IBinder service) {
			bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			MainActivity.bluetoothLeService=bluetoothLeService;
			if (!bluetoothLeService.initialize()) {
				Log.e("蓝牙连接失败","Unable to initialize Bluetooth");
				finish();
			}
			// Automatically connects to the device upon successful start-up
			// initialization.
			bluetoothLeService.connect(deviceAddress);
		}
		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			bluetoothLeService = null;
		}
	};
	// Handles various events fired by the Service.
	// ACTION_GATT_CONNECTED: connected to a GATT server.
	// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
	// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
	// ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
	//                        or notification operations.
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (action.equals(BluetoothLeService.ACTION_GATT_CONNECTED)) {
				//
				if (!bluetoothLeService.flagDiscoverServices) {
					refreshWidget(BluetoothLeService.ACTION_GATT_DISCONNECTED);
				}
			} else if (action.equals(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)) {
				intState=BluetoothLeService.STATE_CONNECTED;
				deviceAddress=null;
				//refresh refreshWidget
				refreshWidget(action);
				//发送connect success指令
				if(packet.setPacketParam(BluetoothLeService.opcode_connected, null, null)){
					byteArray = packet.encodePacket(null);
				}
				MainActivity.bluetoothLeService.writeLlsAlertLevel(null,byteArray);
				//连接记录
				string=FileStream.connectRecord;
				byteArray = (MainActivity.bluetoothLeService.bluetoothDevice.getAddress()+",").getBytes();
				fileStream.fileStream(string, FileStream.write, byteArray);
			} else if (action.equals(BluetoothLeService.ACTION_GATT_DISCONNECTED)) {
				//refresh refreshWidget
				refreshWidget(action);
			} else if (action.equals(BluetoothLeService.ACTION_DATA_AVAILABLE)) {
				//before closeS00/shutDown must refresh DeviceScanActivity
				string=intent.getStringExtra(BluetoothLeService.WRITE_DATA);
				if (BluetoothLeService.string_close.equals(string)) {
					finish();
				}else if (BluetoothLeService.string_unlockPwd.equals(string)) {
					new MyTask().execute("");
				}else if (BluetoothLeService.string_lockDelay.equals(string)) {
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							//发送关锁指令
							if(packet.setPacketParam(BluetoothLeService.OPCODE_CLOSE_LOCK, null, null)){
								byteArray = packet.encodePacket(null);
							}
							MainActivity.bluetoothLeService.writeLlsAlertLevel(null,byteArray);
						}
					},2*1000);
				}
				if (intState==BluetoothLeService.STATE_DISCONNECTED) {
					//refresh refreshWidget
					refreshWidget(BluetoothLeService.ACTION_GATT_DISCONNECTED);
				}
			}
		}
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setTitle(R.string.title_devices);
		setContentView(R.layout.bluetooth_device_scan);
		handler = new Handler();
		// 检查当前手机是否支持ble 蓝牙,如果不支持退出程序
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			finish();
		}
		// 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		bluetoothAdapter= bluetoothManager.getAdapter();
		// 检查设备上是否支持蓝牙
		if (bluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}else{
			if (bluetoothAdapter.isEnabled()) {
				//后台打开蓝牙,不做任何提示
				bluetoothAdapter.enable();
			}
		}
		if (MainActivity.connect_state!=null) {
			MainActivity.connect_state=null;
		}
		if (MainActivity.back!=null) {
			MainActivity.back=null;
		}
		disconnectedDeviceListView = (ListView) findViewById(R.id.disconnectedDeviceList);
		disconnectedDeviceListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				// TODO Auto-generated method stub
				BluetoothDevice bluetoothDevice=disconnectedDeviceArrayList.get(arg2);
				if (bluetoothDevice == null) return;
				//must stopLeScan
				if (mScanning) {
					bluetoothAdapter.stopLeScan(mLeScanCallback);
					mScanning = false;
				}
				deviceAddress=bluetoothDevice.getAddress();
				if (intState==BluetoothLeService.STATE_CONNECTING) {

				} else if (intState==BluetoothLeService.STATE_CONNECTED) {
//					if (BluetoothLeService.bluetoothGatt!=null) {
//						//clear connectRecord
//						string=FileStream.connectRecord;
//						fileStream.fileStream(string, FileStream.write, null);
//						//disconnect
//						MainActivity.mBluetoothLeService.disconnect();
//					}
				} else if (intState==3){
					Intent intent=new Intent(getBaseContext(), BluetoothLeService.class);
					b=bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
//					if (MainActivity.intentBluetoothLeService==null) {
//						MainActivity.intentBluetoothLeService= new Intent(getBaseContext(), BluetoothLeService.class);
//					}
//					bindService(MainActivity.intentBluetoothLeService, mServiceConnection, BIND_AUTO_CREATE);
//					MainActivity.intentBluetoothLeService.putExtra(device_address, deviceAddress);
//					startService(intent);
					intState=BluetoothLeService.STATE_CONNECTING;
				}
			}
		});
		connectedDeviceListView = (ListView) findViewById(R.id.connectedDeviceList);
		connectedDeviceListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				// TODO Auto-generated method stub
				dialogOperate();
			}
		});
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		if (!mScanning) {
			menu.findItem(R.id.menu_stop).setVisible(false);
			menu.findItem(R.id.menu_scan).setVisible(true);
			menu.findItem(R.id.menu_refresh).setActionView(null);
		} else {
			menu.findItem(R.id.menu_stop).setVisible(true);
			menu.findItem(R.id.menu_scan).setVisible(false);
			menu.findItem(R.id.menu_refresh).setActionView(
					R.layout.actionbar_indeterminate_progress);
		}
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_scan:
				disconnectedDeviceArrayList.clear();
				scanLeDevice(true);
				break;
			case R.id.menu_stop:
				scanLeDevice(false);
				break;
		}
		return true;
	}
	@Override
	protected void onResume() {
		super.onResume();
		// Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
		// fire an intent to display a dialog asking the user to grant permission to enable it.
		if (!bluetoothAdapter.isEnabled()) {
			if (!bluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}
		if (MainActivity.bluetoothLeService!=null) {
			connectedDeviceArrayList.add(MainActivity.bluetoothLeService.bluetoothDevice);
			//refresh connectedDeviceListView
			displayDevices(connectedDeviceArrayList,BluetoothLeService.STATE_CONNECTED);
		} else {
			scanLeDevice(true);
		}
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		//after DeviceControlActivity disconnected must refresh DeviceScanActivity
		if (intState==BluetoothLeService.STATE_DISCONNECTED) {
			//refresh refreshWidget
			refreshWidget(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		}
		if (MainActivity.bluetoothNameSet!=null) {
			displayDevices(connectedDeviceArrayList,BluetoothLeService.STATE_CONNECTED);
		}
		if (MainActivity.back!=null) {
			MainActivity.back=null;
			finish();
		}
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//must stopLeScan
		if (mScanning) {
			//stop scan
			bluetoothAdapter.stopLeScan(mLeScanCallback);
			mScanning = false;
		}
		unregisterReceiver(mGattUpdateReceiver);
		connectedDeviceArrayList.clear();
		disconnectedDeviceArrayList.clear();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (b) {
			b=false;
			unbindService(mServiceConnection);
		}
	}
	//
	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}
	//
	private void scanLeDevice(final boolean enable) {
		disconnectedDeviceArrayList.clear();
		if (enable) {
			// Stops scanning after a pre-defined scan period.
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					bluetoothAdapter.stopLeScan(mLeScanCallback);
					invalidateOptionsMenu();
				}
			}, SCAN_PERIOD);
			mScanning = true;
			bluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			mScanning = false;
			bluetoothAdapter.stopLeScan(mLeScanCallback);
		}
		invalidateOptionsMenu();
	}
	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					addDevice(device);
					displayDevices(disconnectedDeviceArrayList,BluetoothLeService.STATE_DISCONNECTED);
				}
			});
		}
	};
	//
	public void addDevice(BluetoothDevice device) {
		if(!disconnectedDeviceArrayList.contains(device)) {
			disconnectedDeviceArrayList.add(device);
		}
//		//读取数据
//        string=FileStream.connectRecord;
//		byteArray=fileStream.fileStream(string, FileStream.read, null);
//		string=new String(byteArray);
//		if (string!=null) {
//			String[] stringArray=string.split(",");
//			for (String string : stringArray) {
//				if (string.equals(device.getAddress())) {
//					//before connect must stopLeScan
//					if (mScanning) {
//						//stop scan
//						mScanning = false;
//						MainActivity.bluetoothAdapter.stopLeScan(mLeScanCallback);
//						invalidateOptionsMenu();
//					}
//					//auto connect
//					deviceAddress=string;
//					Intent intent=new Intent(getBaseContext(), BluetoothLeService.class);
//					b=bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
//					intState=BluetoothLeService.STATE_CONNECTING;
//				}
//			}
//		}
	}
	//refresh disconnectedDeviceListView and connectedDeviceListView
	private void refreshWidget(final String state) {
		//Validate
		if (state.equals(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)) {
			//reinitialize disconnectedDeviceArrayList
			for (BluetoothDevice bluetoothDevice : disconnectedDeviceArrayList) {
				if (bluetoothDevice.getAddress().equals(MainActivity.bluetoothLeService.bluetoothDevice.getAddress())) {
					//remove connectedDevice from disconnectedDeviceArrayList
					disconnectedDeviceArrayList.remove(bluetoothDevice);
					//add connectedDevice to the connectedDeviceArrayList
					connectedDeviceArrayList.add(bluetoothDevice);
					break;
				}
			}
			//refresh connectedDeviceListView
			displayDevices(connectedDeviceArrayList,BluetoothLeService.STATE_CONNECTED);
			//refresh disconnectedDeviceListView
			displayDevices(disconnectedDeviceArrayList,BluetoothLeService.STATE_DISCONNECTED);
		}else if (state.equals(BluetoothLeService.ACTION_GATT_DISCONNECTED)) {
			//reinitialize connectedDeviceArrayList
			for (BluetoothDevice bluetoothDevice : connectedDeviceArrayList) {
				if (bluetoothDevice.getAddress().equals(MainActivity.bluetoothLeService.bluetoothDevice.getAddress())) {
					//remove connectedDevice from disconnectedDeviceArrayList
					connectedDeviceArrayList.remove(bluetoothDevice);
				}
			}
			if (intState==BluetoothLeService.STATE_CONNECTING||intState==BluetoothLeService.STATE_CONNECTED) {
				MainActivity.connect_state=disconnect;
//    			MainActivity.mBluetoothLeService.disconnect();
				MainActivity.bluetoothLeService.close();
				MainActivity.bluetoothNameSet=null;
				MainActivity.bluetoothLeService = null;
				intState=3;
				finish();
			}else if (intState==BluetoothLeService.STATE_DISCONNECTED) {
				MainActivity.connect_state=disconnect;
				MainActivity.bluetoothLeService.close();
				MainActivity.bluetoothNameSet=null;
				MainActivity.bluetoothLeService = null;
				intState=3;
				finish();
			}else if (deviceAddress!=null) {
				Intent intent=new Intent(getBaseContext(), BluetoothLeService.class);
				b=bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
				intState=BluetoothLeService.STATE_CONNECTING;
			}
			//refresh connectedDeviceListView
			displayDevices(connectedDeviceArrayList,BluetoothLeService.STATE_CONNECTED);
			//refresh disconnectedDeviceListView
			scanLeDevice(true);
		}
	}
	//display disconnectedDevice or connectedDevice
	private void displayDevices(ArrayList<BluetoothDevice> arrayList,int state) {
		ArrayList<HashMap<String, String>> arrayListHashMap = new ArrayList<HashMap<String, String>>();
		for (BluetoothDevice device : arrayList) {
			//must initialize in cycle
			HashMap<String, String> hashMap = new HashMap<String, String>();
			if (device.getName()==null) {
				hashMap.put(device_name,getResources().getString(R.string.unknown_deviceName));
			}else if (MainActivity.bluetoothNameSet!=null&state==BluetoothLeService.STATE_CONNECTED) {
				hashMap.put(device_name,MainActivity.bluetoothNameSet);
			}else{
				hashMap.put(device_name, device.getName());
			}
			hashMap.put(device_address, device.getAddress());
			arrayListHashMap.add(hashMap);
		}
		SimpleAdapter simpleAdapter = new SimpleAdapter(
				this,
				arrayListHashMap,
				android.R.layout.simple_expandable_list_item_2,
				new String[] {device_name, device_address},
				new int[] { android.R.id.text1, android.R.id.text2 }
		);
		if (state==BluetoothLeService.STATE_DISCONNECTED) {
			//display disconnectedDeviceListView
			disconnectedDeviceListView.setAdapter(simpleAdapter);
		} else if (state==BluetoothLeService.STATE_CONNECTED) {
			//display connectedDeviceListView
			connectedDeviceListView.setAdapter(simpleAdapter);
		}
	}
	//
	private void dialogOperate(){
		LayoutInflater inflater = getLayoutInflater();
		final View layout = inflater.inflate(R.layout.unlock_operate,(ViewGroup) findViewById(R.id.dialog));
		//initialize selected RadioButton
		if (dialogOperateSelected.equals(handLockNumber)) {
//    		radioButton=(RadioButton)layout.findViewById(R.id.handLockNumber);
			radioButton.setChecked(true);
		}else if (dialogOperateSelected.equals(setS00)) {
//    		radioButton=(RadioButton)layout.findViewById(R.id.setS00Data);
			radioButton.setChecked(true);
		}else{
			radioButton=(RadioButton)layout.findViewById(R.id.disconnect);
			radioButton.setChecked(true);
		}
		//根据ID找到RadioGroup实例
		RadioGroup radioGroup=(RadioGroup)layout.findViewById(R.id.radioGroup);
		//绑定一个匿名监听器
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int checkedId) {
				// TODO Auto-generated method stub
				//select operate
				if (checkedId==R.id.disconnect) {
					//disconnect
					dialogOperateSelected=disconnect;
				}
//		        else if (checkedId==R.id.handLockNumber) {
//		        	//unlockHandLockNumber
//		        	dialogOperateSelected=handLockNumber;
//		        }else if (checkedId==R.id.setS00Data) {
//		        	dialogOperateSelected=setS00;
//		        }
			}
		});
		//对话框   Builder是AlertDialog的静态内部类
		Dialog dialog = new AlertDialog.Builder(this)
				//设置对话框的标题
				.setTitle("选择下列操作:")
				//设置对话框要显示的消息
				.setView(layout)
				//给对话框来个按钮 叫“确定” ，并且设置监听器 这种写法也真是有些BT
				.setPositiveButton("确定", new DialogInterface.OnClickListener(){
					//点击 "确定"按钮之后要执行的操作就写在这里
					public void onClick(DialogInterface dialog, int which) {
						if (dialogOperateSelected.equals(handLockNumber)) {
							//unlockHandLockNumber
							DialogHandLockNumber();
						}else if (dialogOperateSelected.equals(setS00)) {
							DialogSet();
						}else{
							//disconnect
							MainActivity.bluetoothLeService.disconnect();
						}
					}
				}).setNeutralButton("取消", new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int whichButton){
						dialog.cancel();
					}
				}).create();//创建按钮
		dialog.show();//显示一把
	}
	//
	private void DialogHandLockNumber(){
		LayoutInflater inflater = getLayoutInflater();
		final View layout = inflater.inflate(R.layout.lock_number_dialog,(ViewGroup) findViewById(R.id.dialog));
		//对话框   Builder是AlertDialog的静态内部类
		Dialog dialog = new AlertDialog.Builder(this)
				//设置对话框的标题
				.setTitle("请手动输入锁号:")
				//设置对话框要显示的消息
				.setView(layout)
				//给对话框来个按钮 叫“确定” ，并且设置监听器 这种写法也真是有些BT
				.setPositiveButton("确定", new DialogInterface.OnClickListener(){
					//点击 "确定"按钮之后要执行的操作就写在这里
					public void onClick(DialogInterface dialog, int which) {
						//
						EditText lockNumber = (EditText) layout.findViewById(R.id.lockNumber_value);
						BluetoothLeService.lock_no = lockNumber.getText().toString();
						MainActivity.handLockNumber=DeviceControlActivity.HAND_SET_LOCKNUMBER;
						//发送落锁指令
						if(packet.setPacketParam(BluetoothLeService.OPCODE_WAIT_FALL_IN_UNLOCK, null, null)){
							byteArray = packet.encodePacket(null);
						}
						MainActivity.bluetoothLeService.writeLlsAlertLevel(null,byteArray);
					}
				}).setNeutralButton("取消", new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int whichButton){
						dialog.cancel();
					}
				}).create();//创建按钮
		dialog.show();//显示一把
	}
	//
	private void dialogLockPwd(){
		LayoutInflater inflater = getLayoutInflater();
		final View layout = inflater.inflate(R.layout.lock_pwd,(ViewGroup) findViewById(R.id.dialog));
		//对话框   Builder是AlertDialog的静态内部类
		Dialog dialog = new AlertDialog.Builder(this)
				//设置对话框的标题
				.setTitle("请手动输入开锁密码:")
				//设置对话框要显示的消息
				.setView(layout)
				//给对话框来个按钮 叫“确定” ，并且设置监听器 这种写法也真是有些BT
				.setPositiveButton("确定", new DialogInterface.OnClickListener(){
					//点击 "确定"按钮之后要执行的操作就写在这里
					public void onClick(DialogInterface dialog, int which) {
						//
						EditText lockPwd = (EditText) layout.findViewById(R.id.lockPwdValue);
						BluetoothLeService.pwd = lockPwd.getText().toString();
						//send unlock
						fileStream.decodeOpt_pwd();
					}
				}).setNeutralButton("取消", new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int whichButton){
						dialog.cancel();
					}
				}).create();//创建按钮
		dialog.show();//显示一把
	}
	//
	private void DialogSet(){
		MainActivity.setS00=setS00;
		SetActivity.param_data[0]=(byte)SetActivity.VS_S00SerialNumber;
		SetActivity.param_data[1]=(byte)0;
		if(packet.setPacketParam(SetActivity.OPCODE_GET_PARAM, SetActivity.param_data, null)){
			byteArray = packet.encodePacket(null);
			MainActivity.bluetoothLeService.writeLlsAlertLevel(null,byteArray);
		}
	}
	//
	class MyTask extends AsyncTask<String, String, String>{
		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			fileStream.PWD();
			if (MainActivity.bluetoothLeService.bDataSend1!=null) {
				try {
					Thread.sleep(100);
					MainActivity.bluetoothLeService.writeLlsAlertLevel(null,MainActivity.bluetoothLeService.bDataSend1);
					MainActivity.bluetoothLeService.bDataSend1=null;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
		}

	}
	//RunningServicesInfo
	public static String getRunningServicesInfo(Context context) {
		StringBuffer serviceInfo = new StringBuffer();
		final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> services = activityManager.getRunningServices(100);
		Iterator<RunningServiceInfo> iterator = services.iterator();
		while (iterator.hasNext()) {
			RunningServiceInfo runningServiceInfo = (RunningServiceInfo) iterator.next();
			if(runningServiceInfo.service.getClassName().toString().equals("com.android.bluetooth.BluetoothLeService")){

			}
//                    serviceInfo.append("pid: ").append(runningServiceInfo.pid);
//                    serviceInfo.append("\nprocess: ").append(runningServiceInfo.process);
//                    serviceInfo.append("\nservice: ").append(runningServiceInfo.service);
//                    serviceInfo.append("\ncrashCount: ").append(runningServiceInfo.crashCount);
//                    serviceInfo.append("\nclientCount: ").append(runningServiceInfo.clientCount);
//                    serviceInfo.append("\nactiveSince: ").append(ToolHelper.formatData(runningServiceInfo.activeSince));
//                    serviceInfo.append("\nlastActivityTime: ").append(ToolHelper.formatData(runningServiceInfo.lastActivityTime));
//                    serviceInfo.append("\n\n");
		}
		return serviceInfo.toString();
	}


}