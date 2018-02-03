package com.saintsung.saintpmc.lock;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import java.io.IOException;
import java.security.PublicKey;
import java.util.Calendar;

import com.saintsung.saintpmc.MainActivity;
import com.saintsung.saintpmc.R;
import com.saintsung.saintpmc.lock.Command;
import com.saintsung.saintpmc.lock.DeviceService;
import com.saintsung.saintpmc.lock.MCUCommand;
import com.saintsung.saintpmc.lock.WLog;

public class SetActivity0 extends Activity{
	public static final byte VS_FW51_VER_GOT=1;
	public static final byte VS_HW_GOT = 2;
	//    public static final byte VS_LOFFSET_GOT = 3;
//    public static final byte VS_ALL_GOT = 4;
	public static final byte VS_LOFFSET_GOT = (byte) 0xd5;
	public static final byte VS_ALL_GOT = (byte) 0xd6;
	public static final byte VS_S00SerialNumber = 12;
	public static final byte VS_S00Name = 13;
	//    public static final byte VS_UNLOCK_TYPE = 14;
	public static final byte VS_UNLOCK_TYPE = (byte) 0xd8;
//[[cxq
	//获取电池电量发送0xd7
//  	public static final byte OPCODE_GET_BATTERY = (byte) 0xd7;
//]]

	private byte[] byteArray;
	public static byte[] param_data = new byte[2];
	private byte[] by_ArrayUnlockType =  new byte[2];
	byte[] byteArray20=new byte[20];

	byte[] by_SetValue = new byte[6];

	private TextView textView,textViewBluetoothName,textViewS00SerialNumber,textViewS00SoftwareVersion,textViewS00HardwareVersion,textViewBaudRate;
	private Spinner spinner,spinner_set_unlock_type;
	private EditText editText,editTextS00SerialNumber,editTextOffset,editTextSet;
	private Button saveButton,cancelButton;
	public final static String bluetoothName="bluetoothName";
	public final static String bluetoothAddress = "bluetoothAddress";
	public final static String S00SerialNumber="S00SerialNumber";
	public final static String S00SoftwareVersion="S00SoftwareVersion";
	public final static String S00HardwareVersion="S00HardwareVersion";
	public final static String direction="direction";
	public final static String positive="+";
	public final static String negative="-";
	public static String directionValue=positive;
	public final static String unlock_type="unlock_type";
	public final static String unlock_bluetooth="蓝牙通信开/关锁;";
	public final static String unlock_only="掌机独立开/关锁;";
	public final static String unlock_screw="开/关螺丝;";
	public final static String unlock_unknown="未定义开/关锁方式;";
	public final static String unlock_type_value="unlock_type_value";
	//   public final static String unlock_type_value="";
	public static final String get_setValue = "get_setValue";
	public static final String send_setValue = "send_setValue";
	//	public static final String get_batteryValue = "get_batteryValue";
//	public static final String send_batteryValue = "send_batteryValue";
	private static final String[] stringArray={positive,negative};
	private static final String[] stringArray0={unlock_bluetooth,unlock_only,unlock_screw};
	private ArrayAdapter<String> arrayAdapter;
	public final static String offset="offset";
	public final static String set="set";
	//[[cxq
//    public final static String battery="battery";
	private String bluetoothAddressValue;
	private String str_SerialNumber="";
	private int i_name_serial_lenght = 20;

	SharedPreferences mySharedPreferences;
	SharedPreferences.Editor editor;
	User_Share user_Share = new User_Share();

	//]]
	public final static String error="error";
	public final static String baudRateDefaultValue="9600";
	private String string,offsetValue,setValue,bluetoothNameValue,S00SerialNumberValue,directionChange,unlock_type_change,baudRateValue;
	boolean b;

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		SetActivity0 that = SetActivity0.this;
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle b = intent.getExtras();

			String string = b.getString(bluetoothName);
			if(string != null)
			{
				if (intent.getStringExtra(bluetoothName).equals("close")) {
					finish();
				}
			}

			that.textViewBluetoothName.setText(b.getString(bluetoothName));
//[[cxq	获取它的MAC地址
//			bluetoothAddressValue =" "+b.getString(bluetoothAddress);
//			if(bluetoothAddressValue == null || bluetoothAddressValue.length() <= 0)
			{
				//			bluetoothAddressValue =  b.getString(bluetoothAddress);
			}

//			that.textViewBluetoothName.setText(str_Device_Name +" "+ bluetoothAddressValue);
//]
			str_SerialNumber = b.getString(S00SerialNumber);

			that.textViewS00SerialNumber.setText(str_SerialNumber);
			that.textViewS00SoftwareVersion.setText(b.getString(S00SoftwareVersion));
			that.textViewS00HardwareVersion.setText(b.getString(S00HardwareVersion));
			//判断下拉框默认选择的方向
			directionValue=intent.getStringExtra(direction);
			if (directionValue!=null) {
				if (directionValue.equals(positive)) {
					spinner.setSelection(0);
				} else {
					spinner.setSelection(1);
				}
			} else {
				//default selected positive
				spinner.setSelection(0);
			}
			offsetValue= intent.getStringExtra(offset);
			editTextOffset.setText(offsetValue);
			setValue= intent.getStringExtra(set);
			editTextSet.setText(setValue);
			if (intent.getStringExtra(unlock_type)!=null) {
				unlock_type_change=unlock_type_value;
				switch (intent.getStringExtra(unlock_type)) {
					case "00":
						spinner_set_unlock_type.setSelection(0);
						break;
					case "01":
						spinner_set_unlock_type.setSelection(1);
						break;
					case "02":
						spinner_set_unlock_type.setSelection(2);
						break;
					default:
						spinner_set_unlock_type.setSelection(0);
						break;
				}
			}
			if (MCUCommand.string_sleep.equals(b.getString(MCUCommand.string_sleep))) {
				//close SetActivity0 before S00 sleep
				MainActivity.back=MCUCommand.string_sleep;
				finish();
			}
			int status = b.getInt(DeviceService.EXTRA_STATUS);
			switch(status){
				case DeviceService.DS_DISCONNECTED:{
					//must refresh DeviceScanActivity
					DeviceScanActivity.intState=BluetoothLeService.STATE_DISCONNECTED;
					//close DeviceControlActivity
					finish();
				}break;
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//去除标题
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.set);

		// 文件保存信息
		user_Share = new User_Share();
		mySharedPreferences = getSharedPreferences(user_Share.MY_PREFS,
				MODE_PRIVATE);
		editor = mySharedPreferences.edit();



		//get
		Intent intent=getIntent();
		//get telephone date and time		//获取手机上的时间
		final Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);


		//获取S00数据信息
		string=intent.getStringExtra(error);
		//判断执行
		if (string!=null) {
			Toast.makeText(this, string, Toast.LENGTH_LONG).show();
		} else {
			textViewBluetoothName=(TextView) this.findViewById(R.id.bluetoothNameValue);
			if (MainActivity.bluetoothNameSet!=null) {

				bluetoothNameValue=MainActivity.bluetoothNameSet;
				if(bluetoothAddressValue == null || bluetoothAddressValue.length() ==0)
				{
					bluetoothAddressValue  = intent.getStringExtra(bluetoothAddress);
				}

			}else{
				bluetoothNameValue=intent.getStringExtra(bluetoothName);
				if(bluetoothAddressValue == null || bluetoothAddressValue.length() ==0)
				{
					bluetoothAddressValue  = intent.getStringExtra(bluetoothAddress);
				}
			}

			textViewBluetoothName.setText(bluetoothNameValue );
			textViewBluetoothName.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					dialogPWD(bluetoothNameValue);
				}
			});
			textViewS00SerialNumber=(TextView) this.findViewById(R.id.S00SerialNumberValue);


			S00SerialNumberValue = intent.getStringExtra(S00SerialNumber);
			Log.d("s2","s2"+S00SerialNumberValue);


			//序列号
			str_SerialNumber = "cx" + String.format("%02d",year%100) +String.format("%02d",month+1)+String.format("%02d",day)+"0000";

			//[[cxq 如果收到4个F就用时间代替
			if (S00SerialNumberValue.equals("��������������������")) {
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
			textViewS00SoftwareVersion=(TextView) this.findViewById(R.id.S00SoftwareVersionValue);
			textViewS00SoftwareVersion.setText(intent.getStringExtra(S00SoftwareVersion));
			textViewS00HardwareVersion=(TextView) this.findViewById(R.id.S00HardwareVersionValue);
			textViewS00HardwareVersion.setText(intent.getStringExtra(S00HardwareVersion));
			spinner=(Spinner) this.findViewById(R.id.spinner);
			//将可选内容与ArrayAdapter连接起来
			arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,stringArray);
			//将arrayAdapter添加到spinner中
			spinner.setAdapter(arrayAdapter);
			//设置下拉列表的风格
			arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			//判断下拉框默认选择的方向
			directionValue=intent.getStringExtra(direction);
			if (directionValue!=null) {
				if (directionValue.equals(positive)) {
					spinner.setSelection(0);
				} else {
					spinner.setSelection(1);
				}
			} else {
				//default selected positive
				spinner.setSelection(0);
			}
			//添加事件Spinner事件监听
			spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
					// TODO Auto-generated method stub
					//获取选择的项的值
					directionChange=arg0.getItemAtPosition(arg2).toString();
				}
				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub

				}
			});
			editTextOffset=(EditText) this.findViewById(R.id.offsetValue);
			offsetValue= intent.getStringExtra(offset);
			editTextOffset.setText(offsetValue);
			editTextSet=(EditText) this.findViewById(R.id.setValue);
			setValue= intent.getStringExtra(set);
			editTextSet.setText(setValue);
			spinner_set_unlock_type=(Spinner) this.findViewById(R.id.spinner_unlock_type);
			arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,stringArray0);
			spinner_set_unlock_type.setAdapter(arrayAdapter);
			arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			switch (intent.getStringExtra(unlock_type)) {
				case "00":
					spinner_set_unlock_type.setSelection(0);
					break;
				case "01":
					spinner_set_unlock_type.setSelection(1);
					break;
				case "02":
					spinner_set_unlock_type.setSelection(2);
					break;
				default:
					spinner_set_unlock_type.setSelection(0);
					break;
			}
			unlock_type_change=unlock_type_value;
			spinner_set_unlock_type.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
					// TODO Auto-generated method stub
					if (unlock_type_change!=null) {
						switch (unlock_type_change) {
							case unlock_type_value:
								unlock_type_change=null;
								break;
							default:
								break;
						}
					}else{

/*
						byte[] param_data=new byte[2];
						param_data[0]=VS_UNLOCK_TYPE;
						param_data[1]=(byte)arg2;
						Command command=new Command();
						byte[] b=command.setS00SetValue(param_data);
						Intent i = new Intent();
						i.setClass(SetActivity0.this,DeviceService.class);
						i.setAction(send_setValue);
						i.putExtra(send_setValue,b);
						startService(i);
*/
					}
//[[CXQ
					//fun_SetUnlockType(VS_UNLOCK_TYPE,arg2);
					fun_SetValue(VS_UNLOCK_TYPE,arg2);
//]]

					Log.d("set_value","set_value " + param_data[0] +" "+ param_data[1] +" "+"editTextOffset "+editTextOffset.getText().toString() +"editTextSet "+ editTextSet.getText().toString());
					try {
						WLog.logFile("set_value " + param_data[0] +" "+ param_data[1] +" "+"editTextOffset "+editTextOffset.getText().toString() +"editTextSet "+ editTextSet.getText().toString());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub

				}
			});
		}
		saveButton=(Button) this.findViewById(R.id.save);
		cancelButton=(Button) this.findViewById(R.id.cancel);
		//saveButton监听事件
		saveButton.setOnClickListener(new OnClickListener() {
			int i_outside = 0;		//判断输入的字符有没有超过长度非0则超过

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
//				/*
				//判断偏移值方向是否被修改
				string=editTextOffset.getText().toString();
				int i=Integer.parseInt(string);
				if (directionChange.equals(directionValue)) {
					//确定偏移值方向未改变,继续判断偏移值是否被修改
					//validate
					if (-1<i&&i<101) {
						//					if (!string.equals(offsetValue))
						{
							if (directionChange.equals(positive)) {
//								sendChanged(VS_LOFFSET_GOT, i, SetAllActivity.OPCODE_SET_PARAM);
//[[cxq							设置偏移值
								fun_SetValue(VS_LOFFSET_GOT, i);
//]]
							} else {
								i=(i|(1<<7));
//								sendChanged(VS_LOFFSET_GOT, i, SetAllActivity.OPCODE_SET_PARAM);
//[[CXQ							设置偏移值
								fun_SetValue(VS_LOFFSET_GOT, i);
//
							}
						}
						i_outside = 0;
					} else {
						Toast.makeText(SetActivity0.this, "您输入的数字范围超出0-100之间,请重新输入!",Toast.LENGTH_LONG).show();
						i_outside = 1;
					}
				}else {
					//validate
					if (directionChange.equals(positive)) {
						i=Integer.parseInt(string);
						//send changed offset
//						sendChanged(VS_LOFFSET_GOT, i, SetAllActivity.OPCODE_SET_PARAM);
//[[cxq
						fun_SetValue(VS_LOFFSET_GOT, i);
//]]
					} else {
						i=Integer.parseInt(string);
						i=(i|(1<<7));
						//send changed offset
//						sendChanged(VS_LOFFSET_GOT, i, SetAllActivity.OPCODE_SET_PARAM);
//[[cxq
						fun_SetValue(VS_LOFFSET_GOT, i);
//]]
					}
				}
				//判断设置值是否被修改
				string=editTextSet.getText().toString();
				int i0=Integer.parseInt(string);
				//if (!string.equals(setValue))
				{
					if (-1<i0&&i0<31) {
						//send changed set
//						sendChanged(VS_ALL_GOT, i0, SetAllActivity.OPCODE_SET_PARAM);
//[[CXQ
						fun_SetValue(VS_ALL_GOT, i0);
//]]
						i_outside = 0;
					} else {
						Toast.makeText(SetActivity0.this, "您输入的数字范围超出0-30之间,请重新输入!",Toast.LENGTH_LONG).show();
						i_outside = 2;
					}
				}

				if (i_outside == 0) {		//超过长度不会更改，重新输入
//[[CXQ				//发送设置数据
					fun_SendValue();
					//]]

//					*/
					finish();

				}

			}
		});
		//cancelButton监听事件
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//关闭此界面
				finish();
			}
		});
	}

	@Override
	protected void onResume() {
		this.registerReceiver(this.broadcastReceiver, DeviceService.makeBroadcasstIntentFilter());
		super.onResume();
//        if (mBluetoothLeService != null) {
//            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
//            Log.w(TAG, "连接结果+Connect request result=" + result);
//        }
	}
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(broadcastReceiver);
		//must clear
		MainActivity.setS00=null;
	}
	//send changed value
	private void sendChanged(byte select,int i,byte state){
		param_data[0]=(byte)select;
		param_data[1]=(byte)i;
		CommandPacker packet = new CommandPacker();
		if(packet.setPacketParam(state, param_data, null)){
			byte[] bDataSend = packet.encodePacket(null);
			Intent intent = new Intent();
			intent.setClass(SetActivity0.this,DeviceService.class);
			intent.setAction(send_setValue);
			intent.putExtra(send_setValue, bDataSend);
			startService(intent);
		}
	}
	//
	void dialogPWD(final String state){
		LayoutInflater inflater = getLayoutInflater();
		final View layout = inflater.inflate(R.layout.password,(ViewGroup) findViewById(R.id.dialog));
		editText = (EditText)layout.findViewById(R.id.passwordValue);
		textView = (TextView) layout.findViewById(R.id.result);
		textView.setTextColor(getResources().getColor(R.color.red));
		Dialog dialog = new AlertDialog.Builder(SetActivity0.this)
				.setTitle(R.string.passwordIn)
				.setView(layout)
				.setPositiveButton("确定", new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						string= editText.getText().toString();
						if (string.equals("123456")) {
							if (state.equals(bluetoothNameValue)) {
								dialogBluetoothName();
							} else if (state.equals(S00SerialNumber)) {
								dialogS00SerialNumber();
							}
							DialogMy.dialogCloseReflect(dialog, DialogMy.close);
						}else if (string.equals("")) {
							textView.setText("密码不能为空!");
							DialogMy.dialogCloseReflect(dialog, DialogMy.unclose);
						} else {
							textView.setText("密码输入错误!");
							DialogMy.dialogCloseReflect(dialog, DialogMy.unclose);
						}
					}
				}).setNeutralButton("取消", new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int whichButton){
						DialogMy.dialogCloseReflect(dialog,DialogMy.close);
						dialog.cancel();
					}
				}).create();
		dialog.show();
	}
	//
	void dialogBluetoothName(){
		LayoutInflater inflater = getLayoutInflater();
		final View layout = inflater.inflate(R.layout.bluetooth_name,(ViewGroup) findViewById(R.id.dialog));
		editText = (EditText)layout.findViewById(R.id.bluetoothNameValue);
		editText.setText(bluetoothNameValue);
		textView = (TextView) layout.findViewById(R.id.result);
		textView.setTextColor(getResources().getColor(R.color.red));
		Dialog dialog = new AlertDialog.Builder(SetActivity0.this)
				.setTitle(R.string.bluetoothNameSet)
				.setView(layout)
				.setPositiveButton("确定", new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						string= editText.getText().toString();
						if (string.equals("")) {
							textView.setText(getResources().getString(R.string.bluetoothName)+"不能为空!");
							DialogMy.dialogCloseReflect(dialog, DialogMy.unclose);
						} else {
							if (!string.equals(bluetoothNameValue)) {
								User_Share.i_Change_Cmd = (byte)0xd9;
//						修改蓝牙广播名称
								if ((i_name_serial_lenght - string.length()) >0) {
									String str_data = "00000000000000000000";
									string = str_data.substring(0,i_name_serial_lenght-string.length()) + string;
								}
								Command command=new Command();
								byte[] by_Name=command.fun_SetS00Name(string.getBytes());


								Intent i = new Intent();
								i.setClass(SetActivity0.this,DeviceService.class);
								i.setAction(bluetoothName);
								i.putExtra(bluetoothName,by_Name);
								startService(i);

								textViewBluetoothName.setText(string);

								MainActivity.bluetoothNameSet=string;
							}
							DialogMy.dialogCloseReflect(dialog, DialogMy.close);
						}
					}
				}).setNeutralButton("取消", new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int whichButton){
						DialogMy.dialogCloseReflect(dialog,DialogMy.close);
						dialog.cancel();
					}
				}).create();
		dialog.show();
	}
	//
	void dialogS00SerialNumber(){
		LayoutInflater inflater = getLayoutInflater();
		final View layout = inflater.inflate(R.layout.s00_serial_number,(ViewGroup) findViewById(R.id.dialog));
		editTextS00SerialNumber = (EditText) layout.findViewById(R.id.S00SerialNumberValue);
		editTextS00SerialNumber.setText(S00SerialNumberValue);
		textView = (TextView) layout.findViewById(R.id.result);
		textView.setTextColor(getResources().getColor(R.color.red));
		Dialog dialog = new AlertDialog.Builder(SetActivity0.this)
				.setTitle(R.string.inS00SerialNumber)
				.setView(layout)
				.setPositiveButton("确定", new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						//判断 s00SerialNumber changed yes or no;
						string=editTextS00SerialNumber.getText().toString();
						if (string.equals("")) {
							textView.setText(getResources().getString(R.string.S00SerialNumber)+"不能为空!");
							DialogMy.dialogCloseReflect(dialog, DialogMy.unclose);
						}else{
							if (!string.equals(S00SerialNumberValue)) {
								//S00SerialNumberValue changed
								byteArray=string.getBytes();
								if (byteArray.length>20) {
							/*
							string="0";
							byteArray=string.getBytes();
							*/
									textView.setText(getResources().getString(R.string.S00SerialNumber)+"长度超出20byte,要求长度<20!");
									DialogMy.dialogCloseReflect(dialog, DialogMy.unclose);
								}

								if ((i_name_serial_lenght - string.length()) >0) {
									String str_data = "00000000000000000000";
									string = str_data.substring(0,i_name_serial_lenght-string.length()) + string;
								}

								byte[] byteArray0=new byte[byteArray.length+2];
								byteArray0[0]=VS_S00SerialNumber;
								byteArray0[1]=(byte) byteArray.length;
								for (int i = 0; i < byteArray.length; i++) {
									byteArray0[i+2]=byteArray[i];
								}
								CommandPacker packet = new CommandPacker();
								if(packet.setPacketParam(SetAllActivity.OPCODE_SET_PARAM, byteArray0, null)){
									byteArray = packet.encodePacket(null);
								}




								Intent i = new Intent();
								i.setClass(SetActivity0.this,DeviceService.class);
								i.setAction(send_setValue);
								i.putExtra(send_setValue, byteArray);
								startService(i);
							}
							DialogMy.dialogCloseReflect(dialog,DialogMy.close);
						}
					}
				}).setNeutralButton("取消", new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int whichButton){
						DialogMy.dialogCloseReflect(dialog,DialogMy.close);
						dialog.cancel();
					}
				}).create();
		dialog.show();
	}

	//[[CXQ  开锁类型
	public void fun_SetValue(byte by_Item,int i_Value) {
		// TODO Auto-generated method stub

		if (by_Item == VS_UNLOCK_TYPE) {
			by_SetValue[2] = by_Item;
			by_SetValue[5] = (byte)(i_Value & 0xff);
		}else if(by_Item == VS_ALL_GOT){
			by_SetValue[1] = by_Item;
			by_SetValue[4] = (byte)(i_Value & 0xff);
		}else if (by_Item == VS_LOFFSET_GOT) {
			by_SetValue[0] = by_Item;
			by_SetValue[3] = (byte)(i_Value & 0xff);
		}


	}

	//[[CXQ 自动设置开锁方式]]
	public  void  fun_SetType(byte item,int arg2) {
		fun_SetValue(item,arg2);
	}



	//[[CXQ 设置值
	public void fun_SendValue()
	{
//    	//写放文件开锁界面直接读取文件
//    	fun_WriteValue(new byte[]{by_SetValue[0],by_SetValue[3],by_SetValue[1],by_SetValue[4]});

		Command command=new Command();
		byte[] b=command.fun_setS00SetValue(by_SetValue);
		Intent i = new Intent();
		i.setClass(SetActivity0.this,DeviceService.class);
		i.setAction(send_setValue);
		i.putExtra(send_setValue,b);
		startService(i);
	}
//]]



	//[[CXQ		写入文件
	public void fun_WriteValue(byte[] data){
		FileStream fileStream = new FileStream();
		fileStream.fileStream(FileStream.config_value, FileStream.write, data);
	}

	//]]




}
