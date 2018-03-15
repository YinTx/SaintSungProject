package com.saintsung.saintpmc.lock;

import java.util.Arrays;
import java.util.Calendar;


import com.saintsung.saintpmc.MainActivity;
import com.saintsung.saintpmc.R;
import com.saintsung.saintpmc.lock.Command;
import com.saintsung.saintpmc.lock.DeviceService;
import com.saintsung.saintpmc.lock.MCUCommand;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SetAllActivity extends Activity {
	private TextView textView, serviceURL, APPName, backgroundPicture, iconPicture, textViewBluetoothName, textViewS00SerialNumber, textViewS00SoftwareVersion, textViewS00HardwareVersion, textViewBaudRate, IMEI, userName, userDownload, unload, APN, version, date, time, textViewBattery;
	private Spinner spinner, spinner_set_unlock_type, spinner_effective_days;
	public static final byte VS_FW51_VER_GOT = 1;
	public static final byte VS_HW_GOT = 2;
	// public static final byte VS_LOFFSET_GOT = 3;
	// public static final byte VS_ALL_GOT = 4;
	public static final byte VS_LOFFSET_GOT = (byte) 0xd5;
	public static final byte VS_ALL_GOT = (byte) 0xd6;
	public static final byte VS_S00SerialNumber = 12;
	public static final byte VS_S00Name = 13;
	// public static final byte VS_UNLOCK_TYPE = 14;
	public static final byte VS_UNLOCK_TYPE = (byte) 0xd8;
	public static final byte VS_update_time = 15;
	// public static final byte VS_update_time = (byte) 0xd9;
	public static final byte VS_effective_days = 16;
	public static final byte OPCODE_SET_PARAM = (byte) 'D';// set param
	public static final byte OPCODE_GET_PARAM = (byte) 'G';// get param
	// [[cxq
	// 获取电池电量发送0xd7
	// public static final byte OPCODE_GET_BATTERY = (byte) 0xd7;
	// ]]

	public static byte[] param_data = new byte[2];
	byte[] byteArray20 = new byte[20];

	private final byte[] by_SetValue = new byte[6];

	private EditText editText, editTextS00SerialNumber, editTextOffset, editTextSet;
	private Button saveButton, cancelButton;
	public final static String bluetoothName = "bluetoothName";
	public final static String bluetoothAddress = "bluetoothAddress";
	public final static String S00SerialNumber = "S00SerialNumber";
	public final static String serviceURL_value = "serviceURL_value";
	public final static String S00SoftwareVersion = "S00SoftwareVersion";
	public final static String S00HardwareVersion = "S00HardwareVersion";
	public final static String direction = "direction";
	public final static String positive = "+";
	public final static String negative = "-";
	public static String directionValue = positive;
	public final static String unlock_type = "unlock_type";
	public final static String unlock_bluetooth = "蓝牙通信开/关锁;";
	public final static String unlock_only = "掌机独立开/关锁;";
	public final static String unlock_screw = "开/关螺丝;";
	public final static String unlock_unknown = "未定义开/关锁方式;";
	public static final String get_setValue = "get_setValue";
	public static final String send_setValue = "send_setValue";
	public static final String effective_days = "effective_days";
	private static final String[] stringArray0 = { unlock_bluetooth, unlock_only, unlock_screw };
	public final static String offset = "offset";
	// public final static String battery="battery";
	public final static String set = "set";
	public final static String update_time = "update_time";
	public final static String error = "error";
	public final static String baudRateDefaultValue = "9600";
	public final static String disenabled = "禁止更新下载", enabled = "允许更新下载";
	private ArrayAdapter<String> arrayAdapter;
	public int unlock_type_value, effective_days_value;
	public String string, offsetValue, setValue, bluetoothNameValue, S00SerialNumberValue, directionChange, baudRateValue;
	private byte[] byteArray;
	String[] stringArray;
	// CXQ m ="";
	private String bluetoothAddressValue = "";
	private String str_SerialNumber = "";

	private final int i_name_serial_lenght = 18;
	// 新协议
	private String str_NewBluetoothName = "";
	private String str_NewSerialNum = "";
	private byte[] by_NewOffset;
	private byte[] by_NewSetValue;
	private byte[] m_by_DateTime;

	SharedPreferences mySharedPreferences;
	SharedPreferences.Editor editor;
	User_Share user_Share = new User_Share();
	//
	Intent intent = new Intent();
	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle b = intent.getExtras();
			String string = b.getString(bluetoothName);
			if (string != null) {
				if (intent.getStringExtra(bluetoothName).equals("close")) {

					finish();
					return;
				}

			}
			if (MainActivity.bluetoothNameSet != null) {
				bluetoothNameValue = MainActivity.bluetoothNameSet;
				if (bluetoothAddressValue == null || bluetoothAddressValue.length() == 0) {
					bluetoothAddressValue = intent.getStringExtra(bluetoothAddress);
				}

			} else {
				bluetoothNameValue = intent.getStringExtra(bluetoothName);
				if (bluetoothAddressValue == null || bluetoothAddressValue.length() == 0) {
					bluetoothAddressValue = intent.getStringExtra(bluetoothAddress);
				}
			}

			textViewBluetoothName.setText(bluetoothNameValue); // 设备名字智能掌机1+mac地址
			S00SerialNumberValue = intent.getStringExtra(S00SerialNumber);

			textViewS00SerialNumber.setText(S00SerialNumberValue);

			textViewS00SoftwareVersion.setText(b.getString(S00SoftwareVersion));
			textViewS00HardwareVersion.setText(b.getString(S00HardwareVersion));
			// 判断下拉框默认选择的方向
			try {
				int iValue = Integer.parseInt(offsetValue, 16);
				if (iValue >= 0x80) {
					spinner.setSelection(1);
					//设置偏移值
					editTextOffset.setText(String.valueOf(iValue & 0x0f));
				} else {
					spinner.setSelection(0);
					editTextOffset.setText(String.valueOf(iValue & 0x0f));
				}
			} catch (Exception e) {
				// TODO: handle exception
				spinner.setSelection(0);
			}

			/*
			directionValue = intent.getStringExtra(direction);
			if (directionValue != null) {
				if (directionValue.equals(positive)) {
					spinner.setSelection(0);
				} else {
					spinner.setSelection(1);
				}
			} else {
				// default selected positive
				spinner.setSelection(0);
			}
			offsetValue = intent.getStringExtra(offset);

			editTextOffset.setText(offsetValue);
			 */
			setValue = intent.getStringExtra(set);
			editTextSet.setText(setValue);
			if (intent.getStringExtra(unlock_type) != null) {
				switch (Integer.parseInt(intent.getStringExtra(unlock_type))) {
					case 0:
						spinner_set_unlock_type.setSelection(0);
						unlock_type_value = 0;
						break;
					case 1:
						spinner_set_unlock_type.setSelection(1);
						unlock_type_value = 1;
						break;
					case 2:
						spinner_set_unlock_type.setSelection(2);
						unlock_type_value = 2;
						break;

					default:
						spinner_set_unlock_type.setSelection(0);
						unlock_type_value = 0;
						break;
				}
			}
			if (intent.getStringExtra(effective_days) != null) {
				switch (Integer.parseInt(intent.getStringExtra(effective_days))) {
					case 0:
						spinner_effective_days.setSelection(0);
						unlock_type_value = 0;
						break;
					case 1:
						spinner_effective_days.setSelection(1);
						unlock_type_value = 1;
						break;
					case 2:
						spinner_effective_days.setSelection(2);
						unlock_type_value = 2;
						break;
					default:
						spinner_effective_days.setSelection(0);
						unlock_type_value = 0;
						break;
				}
			}
			if (MCUCommand.string_sleep.equals(b.getString(MCUCommand.string_sleep))) {
				// close SetActivity0 before S00 sleep
				MainActivity.back = MCUCommand.string_sleep;
				finish();
			}
			int status = b.getInt(DeviceService.EXTRA_STATUS);
			switch (status) {
				case DeviceService.DS_DISCONNECTED: {
					// must refresh DeviceScanActivity
					DeviceScanActivity.intState = BluetoothLeService.STATE_DISCONNECTED;
					// close DeviceControlActivity
					finish();
				}
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 去除标题
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.set_all);

		// 文件保存信息
		user_Share = new User_Share();
		mySharedPreferences = getSharedPreferences(user_Share.MY_PREFS, MODE_PRIVATE);
		editor = mySharedPreferences.edit();

		FileStream fileStream = new FileStream();

		Intent intent = getIntent();

		// get telephone date and time //获取手机上的时间
		final Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);

		// 获取S00数据信息
		string = intent.getStringExtra(error);
		// 判断执行
		if (string != null) {
			Toast.makeText(this, string, Toast.LENGTH_LONG).show();
		} else {
			textViewBluetoothName = (TextView) this.findViewById(R.id.bluetoothNameValue);
			if (MainActivity.bluetoothNameSet != null) {
				bluetoothNameValue = MainActivity.bluetoothNameSet;
				bluetoothAddressValue = intent.getStringExtra(bluetoothAddress);
			} else {
				bluetoothNameValue = intent.getStringExtra(bluetoothName);
				bluetoothAddressValue = intent.getStringExtra(bluetoothAddress);
			}

			textViewBluetoothName.setText(bluetoothNameValue);
			textViewBluetoothName.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					dialogBluetoothName();
				}
			});
			textViewS00SerialNumber = (TextView) this.findViewById(R.id.S00SerialNumberValue);
			S00SerialNumberValue = intent.getStringExtra(S00SerialNumber);
			Log.d("s2", "s2" + S00SerialNumberValue.toString());
			if (S00SerialNumberValue.length() > 0) {
				fileStream.fileStream(FileStream.deviceFile, FileStream.write, S00SerialNumberValue.getBytes());
			}

			// 序列号
			str_SerialNumber = "cx" + String.format("%02d", year % 100) + String.format("%02d", month + 1) + String.format("%02d", day) + "0000";
			// [[cxq 如果收到小于0x1f,或大于0x7f，即不能解释的符号时用代替字符
			if (S00SerialNumberValue.getBytes()[0] < (byte) 0x1f || S00SerialNumberValue.getBytes()[0] > (byte) 0x7f) {
				S00SerialNumberValue = str_SerialNumber;
			}

			textViewS00SerialNumber.setText(S00SerialNumberValue);
			textViewS00SerialNumber.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					dialogPWD(S00SerialNumber);
				}
			});
			textViewS00SoftwareVersion = (TextView) this.findViewById(R.id.S00SoftwareVersionValue);
			textViewS00SoftwareVersion.setText(intent.getStringExtra(S00SoftwareVersion));
			textViewS00HardwareVersion = (TextView) this.findViewById(R.id.S00HardwareVersionValue);
			textViewS00HardwareVersion.setText(intent.getStringExtra(S00HardwareVersion));

			//设置偏移值
			editTextOffset = (EditText) this.findViewById(R.id.offsetValue);
			offsetValue = intent.getStringExtra(offset);

			spinner = (Spinner) this.findViewById(R.id.spinner);
			stringArray = new String[] { positive, negative };
			// 将可选内容与ArrayAdapter连接起来
			arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, stringArray);
			// 将arrayAdapter添加到spinner中
			spinner.setAdapter(arrayAdapter);
			// 设置下拉列表的风格
			arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// 判断下拉框默认选择的方向
			try {
				int iValue = Integer.parseInt(offsetValue, 16);
				if (iValue >= 0x80) {
					spinner.setSelection(1);
					//设置偏移值
					editTextOffset.setText(String.valueOf(iValue & 0x0f));
				} else {
					spinner.setSelection(0);
					editTextOffset.setText(String.valueOf(iValue & 0x0f));
				}
			} catch (Exception e) {
				// TODO: handle exception
				spinner.setSelection(0);
			}
			// 添加事件Spinner事件监听
			spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					// TODO Auto-generated method stub
					// 获取选择的项的值
					directionChange = arg0.getItemAtPosition(arg2).toString();
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub

				}
			});
			/*
						editTextOffset = (EditText) this.findViewById(R.id.offsetValue);
						offsetValue = intent.getStringExtra(offset);
						editTextOffset.setText(offsetValue);
			*/

			editTextSet = (EditText) this.findViewById(R.id.setValue);
			setValue = intent.getStringExtra(set);
			editTextSet.setText(setValue);
			spinner_set_unlock_type = (Spinner) this.findViewById(R.id.spinner_unlock_type);
			arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, stringArray0);
			spinner_set_unlock_type.setAdapter(arrayAdapter);
			arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			if (intent.getStringExtra(unlock_type) != null) {
				switch (Integer.parseInt(intent.getStringExtra(unlock_type))) {
					case 0:
						spinner_set_unlock_type.setSelection(0);
						unlock_type_value = 0;
						break;
					case 1:
						spinner_set_unlock_type.setSelection(1);
						unlock_type_value = 1;
						break;
					case 2:
						spinner_set_unlock_type.setSelection(2);
						unlock_type_value = 2;
						break;
					default:
						spinner_set_unlock_type.setSelection(0);
						unlock_type_value = 0;
						break;
				}
			}
			spinner_set_unlock_type.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					// TODO Auto-generated method stub
					if (unlock_type_value != arg2) {
						unlock_type_value = arg2;
					}
					fun_SetValue(VS_UNLOCK_TYPE, arg2);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub

				}
			});
		}
		saveButton = (Button) this.findViewById(R.id.save);
		cancelButton = (Button) this.findViewById(R.id.cancel);
		// saveButton监听事件
		saveButton.setOnClickListener(new OnClickListener() {
			int i_outside = 0; // 超 出范围则不修改也不保存，重新输入

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// /*
				// 判断偏移值方向是否被修改
				string = editTextOffset.getText().toString();
				if (string.length() <= 0) {
					Toast.makeText(getBaseContext(), "输入有误,请检查输入数据", Toast.LENGTH_SHORT).show();
					return;
				}

				int i = Integer.parseInt(string);
				if (directionChange.equals(directionValue)) {
					// 确定偏移值方向未改变,继续判断偏移值是否被修改
					// validate
					if (-1 < i && i < 101) {
						// if (!string.equals(offsetValue))
						{
							if (directionChange.equals(positive)) {
								fun_SetValue(VS_LOFFSET_GOT, i);
								// ]]
							} else {
								i = (i | (1 << 7));
								fun_SetValue(VS_LOFFSET_GOT, i);
								// ]]
							}
						}
						i_outside = 0;
					} else {
						Toast.makeText(getBaseContext(), "您输入的数字范围超出0-100之间,请重新输入!", Toast.LENGTH_LONG).show();
						i_outside = 1;
					}
				} else {
					// validate
					if (directionChange.equals(positive)) {
						i = Integer.parseInt(string);
						// [[CXQ
						fun_SetValue(VS_LOFFSET_GOT, i);
						// ]]
					} else {
						i = Integer.parseInt(string);
						i = (i | (1 << 7));
						// [[CXQ
						by_NewOffset = String.format("%02x", i).getBytes();
						fun_SetValue(VS_LOFFSET_GOT, i);
						// ]]
					}
				}
				// 判断设置值是否被修改
				string = editTextSet.getText().toString();
				if (string.length() > 0) {
					string = String.format("%02d", Integer.parseInt(string));
				} else {
					if (string.length() <= 0) {
						Toast.makeText(getBaseContext(), "输入有误,请检查输入数据", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				int i0 = 0;
				byte[] set = string.getBytes();

				TypeConvert.A2HCvtRtnT act = TypeConvert.AsciiToHex(set[0], set[1]);
				if (act.rlt) {
					i0 = act.val;
				}

				// if (!string.equals(setValue))
				{
					if (-1 < i0 && i0 < 31) {
						// send changed set

						// [[CXQ
						fun_SetValue(VS_ALL_GOT, i0);
						// ]]
						i_outside = 0;
					} else {
						Toast.makeText(getBaseContext(), "您输入的数字范围超出0-30之间,请重新输入!", Toast.LENGTH_LONG).show();
						i_outside = 2;
					}
				}

				if (i_outside == 0) {
					// [[CXQ //发送开锁类型
					// fun_SendSetValue();
					// ]]
					// finish();
				}

				fun_NewSendValue();
				// finish();
			}
		});
		// cancelButton监听事件
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// 关闭此界面
				finish();
			}
		});
		serviceURL = (TextView) this.findViewById(R.id.serviceURLValue);
		socketValidate(FileStream.read);
		serviceURL.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				dialogPWD(serviceURL_value);
			}
		});
		spinner = (Spinner) this.findViewById(R.id.spinner_download);
		stringArray = new String[] { disenabled, enabled };
		// 将可选内容与ArrayAdapter连接起来
		arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, stringArray);
		// 将arrayAdapter添加到spinner中
		spinner.setAdapter(arrayAdapter);
		// 设置下拉列表的风格
		arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 判断下拉框默认选择
		if (MainActivity.downloadUpdate.equals(disenabled)) {
			spinner.setSelection(0);
		} else {
			spinner.setSelection(1);
		}
		// 添加事件Spinner事件监听
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				// 获取选择的项的值
				MainActivity.downloadUpdate = arg0.getItemAtPosition(arg2).toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});

		IMEI = (TextView) this.findViewById(R.id.IMEI_value);
		IMEI.setText(MainActivity.IMEI);
		userName = (TextView) this.findViewById(R.id.user_name_value);
		// FileStream fileStream = new FileStream();
		String string = new String(fileStream.fileStream(FileStream.userLogin, FileStream.read, null));
		String[] stringArray = string.split(",");
		userName.setText(stringArray[0]);
		userDownload = (TextView) this.findViewById(R.id.user_download_value);
		byte[] b = fileStream.fileStream(FileStream.userDownload, FileStream.read, null);
		if (b.length == 0) {
			userDownload.setText("未下载/此用户尚未关联任何1把锁具信息;");
		} else {
			string = new String(b);
			stringArray = string.split("\r\n");
			userDownload.setText("" + stringArray.length + "条;");
		}
		unload = (TextView) this.findViewById(R.id.unload_value);
		string = new String(fileStream.fileStream(FileStream.log, FileStream.read, null));
		if (string.equals("")) {
			unload.setText("0条;");
		} else {
			stringArray = string.split("\r\n");
			unload.setText(stringArray.length + "条;");
		}
		APN = (TextView) this.findViewById(R.id.APN_value);
		ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo == null) {
			APN.setText("未连接网络/打开数据连接,单击此处可手动设置APN");
		} else {
			// 获取网络接入点，中国移动:cmwap和cmnet; 中国电信ctwap，ctnet
			String apn = networkInfo.getExtraInfo();
			APN.setText(apn);
		}
		APN.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Settings.ACTION_APN_SETTINGS);
				startActivity(intent);
			}
		});
		version = (TextView) this.findViewById(R.id.APP_version_value);
		// 获取APP版本号
		try {
			PackageInfo packageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
			version.setText(packageInfo.versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		StringBuilder stringBuilder = new StringBuilder().append(year).append((month + 1) < 10 ? "0" + (month + 1) : (month + 1)).append((day < 10) ? "0" + day : day).append(hour).append((minute) < 10 ? "0" + (minute) : (minute)).append((second < 10) ? "0" + second : second);
		string = new String(stringBuilder);
		byteArray = Command.getInstance().setUpdateTime(string.getBytes());
		// 设置时间
		Intent i = new Intent();
		i.setClass(SetAllActivity.this, DeviceService.class);
		i.setAction(update_time);
		i.putExtra(update_time, byteArray);
		startService(i);
		date = (TextView) this.findViewById(R.id.date_value);
		time = (TextView) this.findViewById(R.id.time_value);
		String str_DateTime = intent.getStringExtra(SetAllActivity.update_time);
		// 20160805202839
		if (str_DateTime.length() == 14) {
			int offset = 0;
			date.setText(str_DateTime.substring(offset, offset + 4) + "-" + str_DateTime.substring(offset + 4, offset + 6) + "-" + str_DateTime.substring(offset + 6, offset + 8));
			time.setText(str_DateTime.substring(offset + 8, offset + 10) + ":" + str_DateTime.substring(offset + 10, offset + 12) + ":" + str_DateTime.substring(offset + 12, offset + 14));
		}

		date.setOnClickListener(onClick);
		time.setOnClickListener(onClick);

	}

	// 按钮监听
	OnClickListener onClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Calendar calendar = Calendar.getInstance();
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);
			int second = calendar.get(Calendar.SECOND);

			switch (v.getId()) {
				case R.id.date_value:
					date.setText(new StringBuilder().append(year + "-").append((month + 1) < 10 ? "0" + (month + 1) + "-" : (month + 1) + "-").append((day < 10) ? "0" + day : day));
					break;
				case R.id.time_value:
					time.setText(new StringBuilder().append(String.format("%02d:", hour)).append(String.format("%02d:", minute)).append(String.format("%02d", second)));
					break;

				default:
					break;
			}
		}
	};

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub

		super.onResume();
		this.registerReceiver(this.broadcastReceiver, DeviceService.makeBroadcasstIntentFilter());
		// refresh socket changed
		socketValidate(FileStream.read);

		editor.putString(user_Share.MY_CLASS, user_Share.class_SetAllActivity);
		// 提交保存的结果 将改变写到系统中
		editor.commit();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		unregisterReceiver(broadcastReceiver);
		// must clear
		MainActivity.setS00 = null;
	}

	//
	void dialogWakeUp() {
		// 对话框 Builder是AlertDialog的静态内部类
		Dialog dialog = new AlertDialog.Builder(this)
				// 设置对话框的标题
				.setTitle("操作提示:")
				// 设置对话框要显示的消息
				.setMessage(getString(R.string.wakeUp)).create();// 创建按钮
		dialog.show();// 显示一把
	}

	//
	private void showSet() {
		Intent i = new Intent();
		i.setClass(getBaseContext(), DeviceService.class);
		i.setAction(SetActivity0.get_setValue);
		startService(i);
	}

	// send changed value
	private void sendChanged(byte select, int i, byte state) {
		param_data[0] = select;
		param_data[1] = (byte) i;
		CommandPacker packet = new CommandPacker();
		if (packet.setPacketParam(state, param_data, null)) {
			byte[] bDataSend = packet.encodePacket(null);
			Intent intent = new Intent();
			intent.setClass(SetAllActivity.this, DeviceService.class);
			intent.setAction(send_setValue);
			intent.putExtra(send_setValue, bDataSend);
			startService(intent);
		}
	}

	//
	void dialogPWD(final String state) {
		LayoutInflater inflater = getLayoutInflater();
		final View layout = inflater.inflate(R.layout.password, (ViewGroup) findViewById(R.id.dialog));
		editText = (EditText) layout.findViewById(R.id.passwordValue);
		textView = (TextView) layout.findViewById(R.id.result);
		textView.setTextColor(getResources().getColor(R.color.red));
		Dialog dialog = new AlertDialog.Builder(SetAllActivity.this).setTitle(R.string.passwordIn).setView(layout).setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				string = editText.getText().toString();
				if (string.equals("123456")) {
					if (state.equals(bluetoothName)) {
						dialogBluetoothName();
					} else if (state.equals(S00SerialNumber)) {
						dialogS00SerialNumber();
					} else if (state.equals(serviceURL_value)) {
						socketValidate(FileStream.write);
					}
					DialogMy.dialogCloseReflect(dialog, DialogMy.close);
				} else if (string.equals("")) {
					textView.setText("密码不能为空!");
					DialogMy.dialogCloseReflect(dialog, DialogMy.unclose);
				} else {
					textView.setText("密码输入错误!");
					DialogMy.dialogCloseReflect(dialog, DialogMy.unclose);
				}
			}
		}).setNeutralButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				DialogMy.dialogCloseReflect(dialog, DialogMy.close);
				dialog.cancel();
			}
		}).create();
		dialog.show();
	}

	//
	void dialogBluetoothName() {
		LayoutInflater inflater = getLayoutInflater();
		final View layout = inflater.inflate(R.layout.bluetooth_name, (ViewGroup) findViewById(R.id.dialog));
		editText = (EditText) layout.findViewById(R.id.bluetoothNameValue);
		editText.setText(bluetoothNameValue);
		textView = (TextView) layout.findViewById(R.id.result);
		textView.setTextColor(getResources().getColor(R.color.red));
		Dialog dialog = new AlertDialog.Builder(SetAllActivity.this).setTitle(R.string.bluetoothNameSet).setView(layout).setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				string = editText.getText().toString();
				if (string.equals("")) {
					textView.setText(getResources().getString(R.string.bluetoothName) + "不能为空!");
					DialogMy.dialogCloseReflect(dialog, DialogMy.unclose);
				} else {
					if (!string.equals(bluetoothNameValue)) {
						// user_Share.fun_Set_Change_Cmd((byte)0xd9);
						User_Share.i_Change_Cmd = (byte) 0xd9;
						// 修改蓝牙广播名称
						if ((i_name_serial_lenght - string.length()) > 0) {
							String str_data = "                    ";
							string = string + str_data.substring(0, i_name_serial_lenght - string.length());
						}
						Command command = new Command();
						byte[] by_Name = command.fun_SetS00Name(string.getBytes());

						Intent i = new Intent();
						i.setClass(SetAllActivity.this, DeviceService.class);
						i.setAction(bluetoothName);
						i.putExtra(bluetoothName, by_Name);
						// 新协议蓝牙
						str_NewBluetoothName = string;
						// [[cxq
						// 新协议按保存后发送
						// ]]
						// startService(i);

						bluetoothNameValue = string;
						textViewBluetoothName.setText(string /*
																 * +" "+ bluetoothAddressValue
																 */);
						MainActivity.bluetoothNameSet = string;
					}
					DialogMy.dialogCloseReflect(dialog, DialogMy.close);
				}
			}
		}).setNeutralButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				DialogMy.dialogCloseReflect(dialog, DialogMy.close);
				dialog.cancel();
			}
		}).create();
		dialog.show();
	}

	//
	void dialogS00SerialNumber() {
		LayoutInflater inflater = getLayoutInflater();
		final View layout = inflater.inflate(R.layout.s00_serial_number, (ViewGroup) findViewById(R.id.dialog));
		editTextS00SerialNumber = (EditText) layout.findViewById(R.id.S00SerialNumberValue);
		editTextS00SerialNumber.setText(S00SerialNumberValue);
		textView = (TextView) layout.findViewById(R.id.result);
		textView.setTextColor(getResources().getColor(R.color.red));
		Dialog dialog = new AlertDialog.Builder(SetAllActivity.this).setTitle(R.string.inS00SerialNumber).setView(layout).setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				// 判断 s00SerialNumber changed yes or no;
				string = editTextS00SerialNumber.getText().toString();
				if (string.equals("")) {
					textView.setText(getResources().getString(R.string.S00SerialNumber) + "不能为空!");
					DialogMy.dialogCloseReflect(dialog, DialogMy.unclose);
				} else {
					if (!string.equals(S00SerialNumberValue)) {
						// S00SerialNumberValue changed
						byteArray = string.getBytes();
						if (byteArray.length > 20) {
							textView.setText(getResources().getString(R.string.S00SerialNumber) + "长度超出20byte,要求长度<20!");
							DialogMy.dialogCloseReflect(dialog, DialogMy.unclose);
						}
						if ((i_name_serial_lenght - string.length()) > 0) {
							String str_data = "                    ";
							string = string + str_data.substring(0, i_name_serial_lenght - string.length());
						}
						textViewS00SerialNumber.setText(string);

						byte[] byteArray0 = new byte[byteArray.length + 2];
						byteArray0[0] = VS_S00SerialNumber;
						byteArray0[1] = (byte) byteArray.length;
						for (int i = 0; i < byteArray.length; i++) {
							byteArray0[i + 2] = byteArray[i];
						}
						CommandPacker packet = new CommandPacker();
						if (packet.setPacketParam(OPCODE_SET_PARAM, byteArray0, null)) {
							byteArray = packet.encodePacket(null);
						}
						Intent i = new Intent();
						i.setClass(SetAllActivity.this, DeviceService.class);
						i.setAction(send_setValue);
						i.putExtra(send_setValue, byteArray);
						// [[cxq
						// 新协议按保存后发送
						str_NewSerialNum = string;
						// ]]
						// startService(i);

					}
					DialogMy.dialogCloseReflect(dialog, DialogMy.close);
				}
			}
		}).setNeutralButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				DialogMy.dialogCloseReflect(dialog, DialogMy.close);
				dialog.cancel();
			}
		}).create();
		dialog.show();
	}

	// socket Validate //设置网络
	void socketValidate(String state) {
		// get socket
		FileStream fileStream = new FileStream();
		byteArray = fileStream.fileStream(FileStream.socket, FileStream.read, null);
		if (byteArray.length == 0) {
			// start SocketAddress
//			intent.setClass(this, SocketActivity.class);
//			startActivity(intent);
		} else {
			if (FileStream.read.equals(state)) {
//				string = new String(byteArray);
//				stringArray = string.split(";");
//				serviceURL.setText("地址:" + stringArray[0] + "\n端口:" + stringArray[1]);
			} else if (FileStream.write.equals(state)) {
				// start SocketAddress
//				intent.setClass(this, SocketActivity.class);
//				intent.putExtra(SocketConnect.socketAddress, stringArray[0]);
//				intent.putExtra(SocketConnect.socketPort, stringArray[1]);
//				startActivity(intent);
			}
		}
	}

	// 设置开锁类型
	public void fun_SetValue(byte by_Item, int i_Value) {
		if (by_Item == VS_UNLOCK_TYPE) {
			by_SetValue[2] = by_Item;
			by_SetValue[5] = (byte) (i_Value & 0xff);
		} else if (by_Item == VS_ALL_GOT) {
			by_SetValue[1] = by_Item;
			by_SetValue[4] = (byte) (i_Value & 0xff);
			by_NewSetValue = String.format("%02x", (byte) (i_Value & 0xff)).getBytes();
		} else if (by_Item == VS_LOFFSET_GOT) {
			by_SetValue[0] = by_Item;
			by_SetValue[3] = (byte) (i_Value & 0xff);
			by_NewOffset = String.format("%02x", (byte) (i_Value & 0xff)).getBytes();
		}
	}

	// [[CXQ
	public void fun_SendSetValue() {

		// //写放文件开锁界面直接读取文件
		// fun_WriteValue(new
		// byte[]{by_SetValue[0],by_SetValue[3],by_SetValue[1],by_SetValue[4]});

		Command command = new Command();
		byte[] b = command.fun_setS00SetValue(by_SetValue);
		Intent i = new Intent();
		i.setClass(SetAllActivity.this, DeviceService.class);
		i.setAction(send_setValue);
		i.putExtra(send_setValue, b);
		// [[CXQ
		// 设置值
		// ]]
		// startService(i);
	}

	// ]]

	// [[CXQ 写入文件
	public void fun_WriteValue(byte[] data) {
		FileStream fileStream = new FileStream();
		fileStream.fileStream(FileStream.config_value, FileStream.write, data);
	}

	// ]]

	// [[CXQ
	// 发送配置信息
	public void fun_NewSendValue() {

		DeviceService.STR_CMD = "P";
		// 设备名字
		byte[] by_NewBluetoothName = new byte[20];
		Arrays.fill(by_NewBluetoothName, (byte) ' ');
		// 序列号
		byte[] by_NewSerialNum = new byte[20];
		Arrays.fill(by_NewBluetoothName, (byte) ' ');
		// 设备名字
		if (str_NewBluetoothName.length() > 0) {
			byte[] by_bluetoothName = str_NewBluetoothName.getBytes();
			System.arraycopy(by_bluetoothName, 0, by_NewBluetoothName, 0, by_bluetoothName.length);
		} else {
			byte[] by_bluetoothName = textViewBluetoothName.getText().toString().getBytes();
			System.arraycopy(by_bluetoothName, 0, by_NewBluetoothName, 0, by_bluetoothName.length);
		}
		// 序列号
		if (str_NewSerialNum.length() > 0) {
			byte[] by_SerialNum = str_NewSerialNum.getBytes();
			System.arraycopy(by_SerialNum, 0, by_NewSerialNum, 0, 18); //只能18位
		} else {
			byte[] by_SerialNum = textViewS00SerialNumber.getText().toString().getBytes();
			System.arraycopy(by_SerialNum, 0, by_NewSerialNum, 0, by_SerialNum.length);
		}
		//偏移值
		String str_temp = "";
		str_temp = editTextOffset.getText().toString();
		if (str_temp.length() > 0) {
			//by_NewOffset = String.format("%02d", Integer.parseInt(str_temp)).getBytes();
			Log.d("offset", "offset" + String.valueOf(by_NewOffset));

		} else {
			Toast.makeText(getBaseContext(), "输入有误检查输入!", Toast.LENGTH_SHORT).show();
			return;
		}

		str_temp = editTextSet.getText().toString();
		if (str_temp.length() > 0) {
			by_NewSetValue = String.format("%02d", Integer.parseInt(str_temp)).getBytes();
		} else {
			Toast.makeText(getBaseContext(), "输入有误检查输入!", Toast.LENGTH_SHORT).show();
			return;
		}

		String str_date_time = date.getText().toString() + time.getText().toString();
		str_date_time = str_date_time.replace("-", "");
		str_date_time = str_date_time.replace(":", "");
		m_by_DateTime = str_date_time.getBytes();

		if (by_NewBluetoothName.length > 0 && by_NewSerialNum.length > 0 && by_NewOffset.length > 0 && by_NewSetValue.length > 0) {
			byte[] cmdBytes = Command.getInstance().setS00SetValue(m_by_DateTime, by_NewBluetoothName, by_NewSerialNum, by_NewOffset, by_NewSetValue);
			Intent i = new Intent();
			i.setClass(SetAllActivity.this, DeviceService.class);
			i.setAction(send_setValue);
			i.putExtra(send_setValue, cmdBytes);
			startService(i);
		} else {
			Toast.makeText(getBaseContext(), "输入有误,请检查输入,不要操作掌机,如掌机休眠先退出", Toast.LENGTH_SHORT).show();
			return;
		}

		finish(); // 发送完后结束
		// mDevice.Write(FileStream.write, cmdBytes);
	}

	// ]]

}
