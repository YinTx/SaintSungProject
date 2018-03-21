package com.saintsung.saintpmc.lock;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.saintsung.saintpmc.MainActivity;
import com.saintsung.saintpmc.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressLint("NewApi")
public class DeviceScanActivity0 extends AppCompatActivity implements BleDeviceMgr.ScanCallback {
	static final String TAG = "MainActivity";
	private BleDeviceMgr mBleDeviceMgr;
	private boolean mScanning;
	private Handler mHandler;
	int mLastDeviceStatus = DeviceService.DS_UNKNOWN;

	void registerBroadcast() {
		this.registerReceiver(this.mReceiver, DeviceService.makeBroadcasstIntentFilter());
	}

	void unregisterBroadcast() {
		if (mReceiver != null) {
			this.unregisterReceiver(mReceiver);
		}

	}

	//	//[[CXQ
	Handler handler = new Handler();
	Runnable runnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				handler.postDelayed(runnable, 4000);
				fun_ScanBle();
			} catch (Exception e) {

			}

		}
	};
	//]]

	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		DeviceScanActivity0 that = DeviceScanActivity0.this;

		@Override
		public void onReceive(Context context, Intent intent) {
			switch (intent.getAction()) {
				case DeviceService.ACTION_DEVICE_STATUS: {
					Bundle b = intent.getExtras();
					int status = b.getInt(DeviceService.EXTRA_STATUS);
					mLastDeviceStatus = status;
					switch (status) {
						case DeviceService.DS_UNKNOWN: {
							String address = b.getString(DeviceService.EXTRA_ADDRESS);
							if (address == null) {
								return;
							}
							//[[wk
							String name;
							if (MainActivity.bluetoothNameSet != null) {
								name = MainActivity.bluetoothNameSet;
							} else {
								name = b.getString(DeviceService.EXTRA_NAME);
							}
							boolean found = false;
							for (int i = 0; i < that.listItem.size(); i++) {
								HashMap<String, Object> map = that.listItem.get(i);
								if (address.equals(map.get("address").toString())) {
									map.put("status", "断开");
									that.adapter.notifyDataSetChanged();
									found = true;
									break;
								}
							}
							if (found == false) {
								HashMap<String, Object> map = new HashMap<String, Object>();
								map.put("name", name);
								map.put("address", address);
								map.put("status", "连接");
								that.listItem.add(map);
								that.adapter.notifyDataSetChanged();
							}
						}
						break;
						case DeviceService.DS_CONNECTED: {
							if (user_Share.i_Auto == 1) {
								//自动连接
								editor.putString(user_Share.def_AutoConnect, "0");
								editor.commit();
							}
							String address = b.getString(DeviceService.EXTRA_ADDRESS);
							String name = b.getString(DeviceService.EXTRA_NAME);
							if (address == null) {
								return;
							}
							// [[wk
							if ((name == null) || (name.length() <= 0)) {
								name = MainActivity.bluetoothNameSet;
							} else {
								MainActivity.bluetoothNameSet = name;
							}

							boolean found = false;
							for (int i = 0; i < that.listItem.size(); i++) {
								HashMap<String, Object> map = that.listItem.get(i);
								if (address.equals(map.get("address").toString())) {
									map.put("status", "断开");
									that.adapter.notifyDataSetChanged();
									found = true;
									break;
								}
							}
							if (found == false) {
								HashMap<String, Object> map = new HashMap<String, Object>();
								map.put("name", name);
								map.put("address", address);
								map.put("status", "断开");
								that.listItem.add(map);
								that.adapter.notifyDataSetChanged();
							}
						}
						break;
						case DeviceService.DS_DISCONNECTED: {
							// /*
							String name = b.getString(DeviceService.EXTRA_NAME);
							String address = b.getString(DeviceService.EXTRA_ADDRESS);
							if (address == null) {
								return;
							}

							Log.d(TAG, "DS_DISCONNECTED:" + address);
							for (int i = 0; i < that.listItem.size(); i++) {
								HashMap<String, Object> map = that.listItem.get(i);
								String s = map.get("address").toString();
								if (address.equals(s)) {
									map.put("status", "连接");
									that.adapter.notifyDataSetChanged();
									Log.d(TAG, "DS_DISCONNECTED:found");
									break;
								}
							}
						}
						break;
						default: {
						}
						break;
					}
				}
				break;
			}
		}
	};


	class StatusOnClickListener implements OnClickListener {
		DeviceScanActivity0 that = DeviceScanActivity0.this;
		HashMap<String, Object> mItemMap = null;

		StatusOnClickListener(int postion) {
			this.mItemMap = that.listItem.get(postion);
		}

		@Override
		public void onClick(View v) {

			String name = this.mItemMap.get("name").toString();
			String address = this.mItemMap.get("address").toString();
			String status = this.mItemMap.get("status").toString();
			switch (status) {
				case "连接": {
					startLockerProcessAtivity();
					that.connectDevice(address, name);
				}
				break;
				case "断开": {
					that.disconnectDevice(address, name);
				}
				break;
			}
		}
	}

	class ItemOnClickListener implements OnClickListener {
		DeviceScanActivity0 that = DeviceScanActivity0.this;
		HashMap<String, Object> mItemMap = null;

		ItemOnClickListener(int position) {
			this.mItemMap = that.listItem.get(position);
		}

		@Override
		public void onClick(View v) {
			String status = this.mItemMap.get("status").toString();
			switch (status) {
				case "断开": {
					startLockerProcessAtivity();
				}
				break;
			}
		}

	}
	SharedPreferences mySharedPreferences;
	SharedPreferences.Editor editor;
	User_Share user_Share = new User_Share();
	private int i_connect = 0; //连接
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weforpay_scan);

		// 文件保存信息
		user_Share = new User_Share();
		mySharedPreferences = getSharedPreferences(user_Share.MY_PREFS, MODE_PRIVATE);
		editor = mySharedPreferences.edit();

		this.registerBroadcast();
		mHandler = new Handler();
		Button btstart = (Button) findViewById(R.id.btstart);
		Button btstop = (Button) findViewById(R.id.btstop);
		bar = (ProgressBar) findViewById(R.id.bar);
		bar.setVisibility(View.GONE);
		mBleDeviceMgr = BleDeviceMgr.CreateMgr(this);
		btlist = (ListView) findViewById(R.id.list);
		listItem = new ArrayList<HashMap<String, Object>>();
		adapter = new SimpleAdapter(this, listItem, R.layout.device_item, new String[] { "name", "address", "status" }, new int[] { android.R.id.text1, android.R.id.text2, R.id.status }) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				DeviceScanActivity0 that = DeviceScanActivity0.this;
				if (convertView == null) {
					convertView = View.inflate(DeviceScanActivity0.this, R.layout.device_item, null);
				}

				//[[CXQ
				//获取item的地址跟名字
				HashMap<String, Object> mItemMap = null;
				mItemMap = listItem.get(position);
				if (user_Share.i_Auto == 1) {
					//				String str_name = mySharedPreferences.getString(user_Share.def_DeviceName, "");
					String str_address = mySharedPreferences.getString(user_Share.def_DeviceAddress, "");
					String str_auto = mySharedPreferences.getString(user_Share.def_AutoConnect, "0");
					if (str_address.length() > 0 /*&& str_auto.equals("1") */) {
						if (mItemMap.get("address").toString().equals(str_address)) {
							if (i_connect == 1) {
								i_connect = 0;
								fun_ConnectBle(mItemMap.get("name").toString(), mItemMap.get("address").toString());
							}

						} else {
							Button btn = (Button) convertView.findViewById(R.id.status);
							btn.setOnClickListener(new StatusOnClickListener(position));
						}
					}
				} else {
					Button btn = (Button) convertView.findViewById(R.id.status);
					btn.setOnClickListener(new StatusOnClickListener(position));
				}

				//]]
				convertView.setOnClickListener(new ItemOnClickListener(position));
				return super.getView(position, convertView, parent);
			}
		};
		btlist.setAdapter(adapter);
		btstart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//change before
				//				scanLeDevice(true);
				//[[wk change
				if (!mScanning) {
					fun_Check_Ble(0); //检测蓝牙是否开启
					scanLeDevice(true);
				}
				//]]
				Log.e("a", "开始搜寻");
			}
		});
		btstop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//				scanLeDevice(false);
				//				Log.e("a", "停止");
				fun_StopScanBle();
				//				handler.removeCallbacks(runnable);
			}
		});

		//检查蓝牙	0只开启
		fun_Check_Ble(0);

		if (user_Share.i_Auto == 1) {
			//[[CXQ
			if (mySharedPreferences.getString(user_Share.def_AutoConnect, "0").equals("1")) {
				handler.postDelayed(runnable, 4000);

				i_connect = 1;
				//fun_ScanBle();
			}
			//]]

		}
	}

	//检查重启蓝牙	0：表示检查，1：表示重启
	public void fun_Check_Ble(int i_type) {
		//[[wk
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, "没有蓝牙", Toast.LENGTH_SHORT).show();
			finish();
		}
		BluetoothAdapter bluetoothAdapter = getBluetoothManager().getAdapter();
		// 检查设备上是否支持蓝牙
		if (bluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			finish();
			return;
		} else {

			if (!bluetoothAdapter.isEnabled()) {
				//后台打开蓝牙,不做任何提示
				bluetoothAdapter.enable();
			}
			if (i_type == 1) {
				if (bluetoothAdapter.isEnabled()) {
					//	    			bluetoothAdapter.disable();
					//后台打开蓝牙，不做提示。
					bluetoothAdapter.enable();
				}
			}

		}
		//]]

	}

	//扫描蓝牙
	public void fun_ScanBle() {
		//检查重启蓝牙	1,先关闭再开启
		fun_Check_Ble(0);

		if (!mScanning) {

			scanLeDevice(true);
		}
		//]]
		Log.e("a", "开始搜寻");

	}

	//停止扫描蓝牙
	public void fun_StopScanBle() {
		scanLeDevice(false);
		Log.e("a", "停止");
	}

	//连接蓝牙
	public void fun_ConnectBle(String name, String address) {
		startLockerProcessAtivity();
		connectDevice(address, name);
	}

	void connectDevice(String address, String name) {
		if (user_Share.i_Auto == 1) {
			handler.removeCallbacks(runnable);
		}
		Intent i = new Intent();
		i.setClass(this, DeviceService.class);
		i.setAction(DeviceService.ACTION_CONNECT);
		i.putExtra(DeviceService.EXTRA_NAME, name);
		i.putExtra(DeviceService.EXTRA_ADDRESS, address);
		i.putExtra(DeviceService.EXTRA_MANUAL, true);
		this.startService(i);
	}

	void disconnectDevice(String address, String name) {
		Intent i = new Intent();
		i.setClass(this, DeviceService.class);
		i.putExtra(DeviceService.EXTRA_NAME, name);
		i.putExtra(DeviceService.EXTRA_ADDRESS, address);
		i.putExtra(DeviceService.EXTRA_MANUAL, true);
		i.setAction(DeviceService.ACTION_DISCONNECT);
		this.startService(i);
	}

	@Override
	protected void onResume() {
		/*
		//[[wk logout
		//得到已经连接的设备
		this.getDevice();
		//]]
		*/
		listItem.clear();
		this.adapter.notifyDataSetChanged();
		//		/*
		//[[wk
		BluetoothAdapter bluetoothAdapter = getBluetoothManager().getAdapter();
		// 检查设备上是否支持蓝牙
		if (bluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			finish();
			return;
		} else {
			if (bluetoothAdapter.isEnabled()) {
				if (MainActivity.connect_state != MainActivity.CONNECTED) {
					//        			scanLeDevice(true);
				}
			} else {
				//后台打开蓝牙,不做任何提示
				bluetoothAdapter.enable();
			}
		}
		//]]
		if (MainActivity.back != null) {
			//must clear
			MainActivity.back = null;
			//			/*
			//check MainActivity flag_open
			if (!MainActivity.flag_open) {
				Intent intent = new Intent();
				intent.setClass(getBaseContext(), MainActivity.class);
				startActivity(intent);
			}
			//			*/
			finish();
		}
		//]]
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		this.unregisterBroadcast();
		if (user_Share.i_Auto == 1) {
			handler.removeCallbacks(runnable);
		}
		super.onDestroy();
	}

	@SuppressLint("RestrictedApi")
	private void scanLeDevice(final boolean enable) {
		if (enable) {
			// 停止后一个预定义的扫描周期扫描。
			mHandler.postDelayed(new Runnable() {
				@SuppressLint("RestrictedApi")
				@Override
				public void run() {
					mScanning = false;
					bar.setVisibility(View.GONE);
					mBleDeviceMgr.StopScan();
					invalidateOptionsMenu();
				}
			}, 1000);
			bar.setVisibility(View.VISIBLE);
			listItem.clear();

			this.adapter.notifyDataSetChanged();
			/*
			 * //[[wk logout this.getDevice(); //]]
			 */
			mScanning = true;
			mBleDeviceMgr.StartScan(this);
		} else {
			bar.setVisibility(View.GONE);
			mScanning = false;
			mBleDeviceMgr.StopScan();
		}
		invalidateOptionsMenu();
	}

	private ListView btlist;
	private ArrayList<HashMap<String, Object>> listItem;
	private SimpleAdapter adapter;
	private ProgressBar bar;

	@Override
	public void onScan(String address, String name) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				adapter.notifyDataSetChanged();
			}
		});
		HashMap<String, Object> map = new HashMap<String, Object>();
		boolean found = false;
		for (HashMap<String, Object> tmp : this.listItem) {
			String ta = tmp.get("address").toString();
			if (ta.equals(address)) {
				found = true;
			}
		}
		if (found == false) {
			FileStream fileStream = new FileStream();
			byte[] byteArray = fileStream.fileStream(FileStream.connectRecord, FileStream.read, null);
			if (byteArray.length != 0 && new String(byteArray).equals(address)) {
				startLockerProcessAtivity();
				connectDevice(address, name);
				finish();
			} else {
				Log.d("name000", "name000" + name);
				map.put("name", name);
				map.put("address", address);
				map.put("status", "连接");
				listItem.add(map);

			}
			//]]
			// 	  		 */
			adapter.notifyDataSetChanged();
		}
	}

	//
	//[[wk
	public BluetoothManager getBluetoothManager() {
		//检查当前手机是否支持ble蓝牙,如果不支持退出程序
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			finish();
		}
		//初始化Bluetooth adapter,通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		return bluetoothManager;
	}

	//
	String getActivityManager() {
		//get ActivityManager
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		//one
		String string = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
		return string;
	}

	//startLockerProcessAtivity
	void startLockerProcessAtivity() {
		String activityName = getActivityManager();
		switch (activityName) {
			case DeviceService.activity_DeviceScan0: {
				Log.d(TAG, "startLockerProcessActivity");
				Intent intent = new Intent(getBaseContext(), LockerProcessAtivity.class);
				startActivity(intent);
			}
			break;
			default: {
			}
			break;
		}
	}

}
