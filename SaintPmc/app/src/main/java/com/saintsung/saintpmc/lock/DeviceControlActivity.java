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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.saintsung.saintpmc.MainActivity;
import com.saintsung.saintpmc.R;


/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
	public static final String HAND_SET_LOCKNUMBER = "HAND_SET_LOCKNUMBER";
	private TextView mConnectionState,unlockName,mDataField,sendDataField;
	private Button button;
	private String string;
	private boolean mConnected = false;
	Handler handler;
	//
	public static String unlockType;
	private byte[] bDataSend;
	CommandPacker packet = new CommandPacker();
	FileStream fileStream=new FileStream();



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
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				mConnected = true;
				updateConnectionState(R.string.connected);
				invalidateOptionsMenu();
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
				//refresh DeviceScanActivity
				DeviceScanActivity.intState=BluetoothLeService.STATE_DISCONNECTED;
				//close DeviceControlActivity
				finish();
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
				// Show all the supported services and characteristics on the user interface.
//                displayGattServices(mBluetoothLeService.getSupportedGattServices());
/**
 //发送配对密码
 String pwd="123456";
 byte[] pwd_lst = pwd.getBytes();
 //
 CommandPacker packet = new CommandPacker();
 String state=packet.encode_connectBluetoothPwd;
 if(packet.setPacketParam(packet.opcode_requestPwd, pwd_lst, state)){
 byte[] bDataSend = packet.encodePacket(state);
 mBluetoothLeService.writeLlsAlertLevel(mBluetoothLeService.mBluetoothGatt,bDataSend);
 }
 */
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				displayReceivedData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
				string=intent.getStringExtra(BluetoothLeService.WRITE_DATA);
				displaySendData(string);
				if (MainActivity.handLockNumber!=null||LockSetActivity.unlock_screw.equals(LockSetActivity.unlockType)) {
					if (BluetoothLeService.string_lockPermission.equals(string)) {
						dialogLockPwd();
					}
				}else if (BluetoothLeService.string_sleep.equals(string)) {
					string=null;
					finish();
				}else if (BluetoothLeService.string_close.equals(string)) {
					//refresh DeviceScanActivity
					DeviceScanActivity.intState=BluetoothLeService.STATE_DISCONNECTED;
					finish();
				}
//                	else if (string.equals(BluetoothLeService.string_unlockPwd)) {
//    					new MyTask().execute("");
//    				}else if (string.equals(BluetoothLeService.string_lockDelay)) {
//    					handler.postDelayed(new Runnable() {
//    		                @Override
//    		                public void run() {
//    							//发送关锁指令
//    							if(packet.setPacketParam(BluetoothLeService.OPCODE_CLOSE_LOCK, null, null)){
//    								bDataSend = packet.encodePacket(null);
//    							}
//    							MainActivity.bluetoothLeService.writeLlsAlertLevel(null,bDataSend);
//    		                }
//    		            },2*1000);
//                	}
			}
		}
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//unlock and turn on telephoneScreen
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		setContentView(R.layout.gatt_services_characteristics);
		handler = new Handler(getMainLooper());
		final Intent intent = getIntent();
		getActionBar().setTitle(MainActivity.bluetoothLeService.bluetoothDevice.getName());
		getActionBar().setDisplayHomeAsUpEnabled(true);
		// Sets up UI references.
		((TextView) findViewById(R.id.device_address)).setText(MainActivity.bluetoothLeService.bluetoothDevice.getAddress());
		mConnectionState = (TextView) findViewById(R.id.connection_state);
		unlockName = (TextView) findViewById(R.id.unlockName);
		//get unlockType
		unlockType = LockSetActivity.unlockType;
		if (unlockType.equals(LockSetActivity.unlockLogin)) {
			unlockName.setText(R.string.unlockLogin);
		}else if (unlockType.equals(LockSetActivity.unlockUser)) {
			unlockName.setText(R.string.unlockUser);
		}else if (unlockType.equals(LockSetActivity.unlock_screw)) {
			unlockName.setText(R.string.unlock_screw);
		}else if (unlockType.equals(LockSetActivity.readLockNumber)) {
			unlockName.setText(R.string.readLockNumber);
		}else if (unlockType.equals(LockSetActivity.unlockOriginal)) {
			unlockName.setText(R.string.unlockOriginal);
		}else if (unlockType.equals(LockSetActivity.unlockTwo)) {
			unlockName.setText(R.string.unlockTwo);
		}else if (unlockType.equals(LockSetActivity.unlockThree)) {
			unlockName.setText(R.string.unlockThree);
		}else if (unlockType.equals(LockSetActivity.unlockFour)) {
			unlockName.setText((R.string.unlockFour));
		}else if (unlockType.equals(LockSetActivity.unlockContinue)) {
			unlockName.setText((R.string.unlockContinue));
		}else{
			unlockName.setText("未定义开锁方式!");
		}
		mDataField = (TextView) findViewById(R.id.data_value);
//        mCountField = (TextView) findViewById(R.id.received_count);
		sendDataField = (TextView) findViewById(R.id.sendData_value);
//        sendCountField = (TextView) findViewById(R.id.send_count);
		button=(Button) findViewById(R.id.buttonBack);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				MainActivity.back=MainActivity.string_back;
				finish();
			}
		});
		if (MainActivity.bluetoothLeService!=null) {
			//keep former service
			mConnected = true;
			updateConnectionState(R.string.connected);
			invalidateOptionsMenu();
			displayReceivedData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
			string=intent.getStringExtra(BluetoothLeService.WRITE_DATA);
			displaySendData(string);
			if (MainActivity.handLockNumber!=null||LockSetActivity.unlockType.equals(LockSetActivity.unlock_screw)) {
				if (string.equals(BluetoothLeService.string_lockPermission)) {
					dialogLockPwd();
				}
			}
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
//        if (mBluetoothLeService != null) {
//            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
//            Log.w(TAG, "连接结果+Connect request result=" + result);
//        }
		if (MainActivity.bluetoothNameSet!=null) {
			getActionBar().setTitle(MainActivity.bluetoothNameSet);
		}
		if (MainActivity.back!=null) {
			MainActivity.back=null;
			finish();
		}
		if (DeviceScanActivity.intState==BluetoothLeService.STATE_DISCONNECTED) {
			finish();
		}
	}
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mGattUpdateReceiver);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.gatt_services, menu);
		if (mConnected) {
			menu.findItem(R.id.menu_connect).setVisible(false);
			menu.findItem(R.id.menu_disconnect).setVisible(true);
			menu.findItem(R.id.menu_sheet_send).setVisible(true);
			menu.findItem(R.id.menu_set).setVisible(true);
			menu.findItem(R.id.menu_back).setVisible(true);
			menu.findItem(R.id.menu_upS00).setVisible(false);
		} else {
			menu.findItem(R.id.menu_connect).setVisible(true);
			menu.findItem(R.id.menu_disconnect).setVisible(false);
			menu.findItem(R.id.menu_sheet_send).setVisible(false);
			menu.findItem(R.id.menu_set).setVisible(false);
			menu.findItem(R.id.menu_back).setVisible(true);
			menu.findItem(R.id.menu_upS00).setVisible(false);
		}
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.menu_connect:
				MainActivity.bluetoothLeService.connect(MainActivity.bluetoothLeService.bluetoothDevice.getAddress());
				return true;
			case R.id.menu_disconnect:
				MainActivity.bluetoothLeService.disconnect();
				return true;
			//这里的R为android.R,非R.
			case android.R.id.home:
				onBackPressed();
				return true;
			case R.id.menu_sheet_send:
//            	mBluetoothLeService.send();
				dialogLockNumber();
				return true;
			case R.id.menu_set:
				showSet();
				return true;
			case R.id.menu_back:
				MainActivity.back=MainActivity.string_back;
				finish();
				return true;
			case R.id.menu_upS00:
				upS00();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	//刷新局部
	private void updateConnectionState(final int resourceId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mConnectionState.setText(resourceId);
			}
		});
	}
	//
	public void displayReceivedData(String data) {
		if (data != null) {
			mDataField.setText(data);
		}
	}
	//
	public void displaySendData(String data) {
		if (data != null) {
			sendDataField.setText(data);
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
	private void dialogLockNumber(){
		LayoutInflater inflater = getLayoutInflater();
		final View layout = inflater.inflate(R.layout.lock_number_dialog,(ViewGroup) findViewById(R.id.dialog));
		//对话框   Builder是AlertDialog的静态内部类
		Dialog dialog = new AlertDialog.Builder(this)
				//设置对话框的标题
				.setTitle("请输入锁号:")
				//设置对话框要显示的消息
				.setView(layout)
				//给对话框来个按钮 叫“确定” ，并且设置监听器 这种写法也真是有些BT
				.setPositiveButton("确定", new DialogInterface.OnClickListener(){
					//点击 "确定"按钮之后要执行的操作就写在这里
					public void onClick(DialogInterface dialog, int which) {
						//
						EditText lockNumber = (EditText) layout.findViewById(R.id.lockNumber_value);
						BluetoothLeService.lock_no = lockNumber.getText().toString();
						MainActivity.handLockNumber=HAND_SET_LOCKNUMBER;
						//发送落锁指令
						if(packet.setPacketParam(BluetoothLeService.OPCODE_WAIT_FALL_IN_UNLOCK, null, null)){
							bDataSend = packet.encodePacket(null);
						}
						MainActivity.bluetoothLeService.writeLlsAlertLevel(null,bDataSend);
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
						FileStream fileStream=new FileStream();
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
	private void showSet(){
		MainActivity.setS00=DeviceScanActivity.setS00;
		SetActivity.param_data[0]=(byte)SetActivity.VS_S00SerialNumber;
		SetActivity.param_data[1]=(byte)0;
		if(packet.setPacketParam(SetActivity.OPCODE_GET_PARAM, SetActivity.param_data, null)){
			bDataSend = packet.encodePacket(null);
			MainActivity.bluetoothLeService.writeLlsAlertLevel(null,bDataSend);
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
	//
	private void upS00(){
		CommandPacker packet = new CommandPacker();
		String string="iap_down\r\n";
		byte[] byteArray=string.getBytes();
		if(packet.setPacketParam((byte)0, byteArray, UpS00Activity.prefixUpS00)){
			byteArray = packet.encodePacket(UpS00Activity.prefixUpS00);
			MainActivity.bluetoothLeService.writeLlsAlertLevel(null,byteArray);
		}
//		//jump intent
//		Intent intent = new Intent(this,UpS00Activity.class);
//		startActivity(intent);
	}
}
