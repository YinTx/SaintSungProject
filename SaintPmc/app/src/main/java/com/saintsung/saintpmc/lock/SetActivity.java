package com.saintsung.saintpmc.lock;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
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

import com.saintsung.saintpmc.MainActivity;
import com.saintsung.saintpmc.R;


public class SetActivity extends Activity{
	public static final byte VS_FW51_VER_GOT=1;
	public static final byte VS_HW_GOT = 2;
	//    public static final byte VS_LOFFSET_GOT = 3;
//    public static final byte VS_ALL_GOT = 4;
	public static final byte VS_LOFFSET_GOT = (byte) 0xd5;
	public static final byte VS_ALL_GOT = (byte) 0xd6;
	public static final byte VS_S00SerialNumber = 12;
	public static final byte VS_S00Name = 13;
	public static final byte VS_UNLOCK_TYPE = 14;
	public static final byte OPCODE_SET_PARAM = (byte)'D';//set param
	public static final byte OPCODE_GET_PARAM = (byte)'G';//get param
	//[[CXQ
	public static final byte OPCODE_SET_NAME = (byte)0xd9;//get param
	//]]
	private byte[] byteArray;
	public static byte[] param_data = new byte[2];
	byte[] byteArray20=new byte[20];
	private PackageInfo packageInfo;
	private TextView textView,dateValue,timeValue,textViewBluetoothName,textViewS00SerialNumber,textViewS00SoftwareVersion,textViewS00HardwareVersion,textViewBaudRate;
	private Spinner spinner;
	private EditText editText,editTextBluetoothName,editTextS00SerialNumber,editTextOffset,editTextSet;
	private Button saveButton,cancelButton;
	public final static String bluetoothName="bluetoothName";
	public final static String S00SerialNumber="S00SerialNumber";
	public final static String S00SoftwareVersion="S00SoftwareVersion";
	public final static String S00HardwareVersion="S00HardwareVersion";
	public final static String direction="direction";
	public final static String positive="+";
	public final static String negative="-";
	public static String directionValue=positive;
	private static final String[] stringArray={positive,negative};
	private ArrayAdapter<String> arrayAdapter;
	public final static String offset="offset";
	public final static String set="set";
	public final static String error="error";
	public final static String baudRateDefaultValue="9600";
	private String string,offsetValue,setValue,bluetoothNameValue,S00SerialNumberValue,directionChange,baudRateValue;
	private int length,year,month,day,hour,minute,second;
	boolean b;

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
			if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
				//must refresh DeviceScanActivity
				DeviceScanActivity.intState=BluetoothLeService.STATE_DISCONNECTED;
				//close DeviceControlActivity
				finish();
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				S00SerialNumberValue= intent.getStringExtra(S00SerialNumber);
				textViewS00SerialNumber.setText(S00SerialNumberValue);
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
				string=intent.getStringExtra(BluetoothLeService.WRITE_DATA);
				if (BluetoothLeService.string_sleep.equals(string)) {
					MainActivity.back=MainActivity.string_back;
					finish();
				}else if (BluetoothLeService.string_close.equals(string)) {
					//refresh DeviceScanActivity
					DeviceScanActivity.intState=BluetoothLeService.STATE_DISCONNECTED;
					finish();
				}
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//去除标题
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.set);
		//get
		Intent intent=getIntent();
		//获取S00数据信息
		string=intent.getStringExtra(error);
		//判断执行
		if (string!=null) {
			Toast.makeText(this, string, Toast.LENGTH_LONG).show();
		} else {
			textViewBluetoothName=(TextView) this.findViewById(R.id.bluetoothNameValue);
			if (MainActivity.bluetoothNameSet!=null) {
				bluetoothNameValue=MainActivity.bluetoothNameSet;
			} else {
				bluetoothNameValue=MainActivity.bluetoothLeService.bluetoothDevice.getName();
			}
			textViewBluetoothName.setText(bluetoothNameValue);
			textViewBluetoothName.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					dialogPWD(bluetoothName);
				}
			});
			textViewS00SerialNumber=(TextView) this.findViewById(R.id.S00SerialNumberValue);
			S00SerialNumberValue= intent.getStringExtra(S00SerialNumber);
			textViewS00SerialNumber.setText(S00SerialNumberValue);
			textViewS00SerialNumber.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					dialogPWD(S00SerialNumber);
				}
			});
			textViewS00SoftwareVersion=(TextView) this.findViewById(R.id.S00SoftwareVersionValue);
			string= intent.getStringExtra(S00SoftwareVersion);
			textViewS00SoftwareVersion.setText(string);
			textViewS00HardwareVersion=(TextView) this.findViewById(R.id.S00HardwareVersionValue);
			string= intent.getStringExtra(S00HardwareVersion);
			textViewS00HardwareVersion.setText(string);
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
			textViewBaudRate=(TextView) this.findViewById(R.id.baudRateValue);
			baudRateValue=MainActivity.bluetoothLeService.getBaudRate();
			textViewBaudRate.setText(baudRateValue);
			textViewBaudRate.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					if (!baudRateValue.equals(baudRateDefaultValue)) {
						b=MainActivity.bluetoothLeService.setBaudRate();
						if (b) {
							textViewBaudRate.setText(string);
						}
					}
				}
			});
		}
		saveButton=(Button) this.findViewById(R.id.save);
		cancelButton=(Button) this.findViewById(R.id.cancel);
		//saveButton监听事件
		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//判断偏移值方向是否被修改
				string=editTextOffset.getText().toString();
				if (directionChange.equals(directionValue)) {
					//确定偏移值方向未改变,继续判断偏移值是否被修改
					int i=Integer.parseInt(string);
					//validate
					if (-1<i&&i<101) {
						if (string.equals(offsetValue)) {
							//offset is not changed
						} else {
							//send changed offset
							sendChanged(VS_LOFFSET_GOT, i, OPCODE_SET_PARAM);
						}
					} else {
						Toast.makeText(SetActivity.this, "您输入的数字范围超出0-100之间,请重新输入!",Toast.LENGTH_LONG).show();
					}
				}else {
					//validate
					if (directionChange.equals(positive)) {
						int i=Integer.parseInt(string);
						//send changed offset
						sendChanged(VS_LOFFSET_GOT, i, OPCODE_SET_PARAM);
					} else {
						int i=Integer.parseInt(string);
						i=(i|(1<<7));
						//send changed offset
						sendChanged(VS_LOFFSET_GOT, i, OPCODE_SET_PARAM);
					}
				}
				//判断设置值是否被修改
				string=editTextSet.getText().toString();
				if (string.equals(setValue)) {
					//set is not changed
				} else {
					//validate
					int i=Integer.parseInt(string);
					if (-1<i&&i<101) {
						//send changed set
						sendChanged(VS_ALL_GOT, i, OPCODE_SET_PARAM);
					} else {
						Toast.makeText(SetActivity.this, "您输入的数字范围超出0-100之间,请重新输入!",Toast.LENGTH_LONG).show();
					}
				}
				//关闭此界面
				finish();
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
		super.onResume();
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
//        if (mBluetoothLeService != null) {
//            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
//            Log.w(TAG, "连接结果+Connect request result=" + result);
//        }
	}
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mGattUpdateReceiver);
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
			MainActivity.bluetoothLeService.writeLlsAlertLevel(null,bDataSend);
		}
	}
	//send changed stringValue
	private void sendChangedString(byte[] byteArray,byte state){
		CommandPacker packet = new CommandPacker();
		if(packet.setPacketParam(state, byteArray, null)){
			byteArray = packet.encodePacket(null);
			length=byteArray.length;
			if (length<21) {
				MainActivity.bluetoothLeService.writeLlsAlertLevel(null,byteArray);
			} else {
				//拆分发送
				System.arraycopy(byteArray, 0, byteArray20, 0, 20);
				MainActivity.bluetoothLeService.writeLlsAlertLevel(null,byteArray20);
				length-=20;
				int i=0;
				while((length-20)>1) {
					i++;
					length-=20;
					System.arraycopy(byteArray, 20*i, byteArray20, 0, 20);
					MainActivity.bluetoothLeService.writeLlsAlertLevel(BluetoothLeService.split,byteArray20);
				}
				byte[] byteArrayEnd=new byte[length];
				System.arraycopy(byteArray, 20*(i+1), byteArrayEnd, 0, length);
				MainActivity.bluetoothLeService.writeLlsAlertLevel(BluetoothLeService.split,byteArrayEnd);
			}
		}
	}
	//
	void dialogPWD(final String state){
		LayoutInflater inflater = getLayoutInflater();
		final View layout = inflater.inflate(R.layout.password,(ViewGroup) findViewById(R.id.dialog));
		editText = (EditText)layout.findViewById(R.id.passwordValue);
		textView = (TextView) layout.findViewById(R.id.result);
		textView.setTextColor(getResources().getColor(R.color.red));
		Dialog dialog = new AlertDialog.Builder(SetActivity.this)
				.setTitle(R.string.password)
				.setView(layout)
				.setPositiveButton("确定", new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						string= editText.getText().toString();
						if (string.equals("123456")) {
							if (state.equals(bluetoothName)) {
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
		Dialog dialog = new AlertDialog.Builder(SetActivity.this)
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
								b=MainActivity.bluetoothLeService.bluetoothNameSet(string.getBytes());
								if (b) {
									textViewBluetoothName.setText(string);
									MainActivity.bluetoothNameSet=string;
								}
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
		Dialog dialog = new AlertDialog.Builder(SetActivity.this)
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
									string="0";
									byteArray=string.getBytes();
								}
								byte[] byteArray0=new byte[byteArray.length+2];
								byteArray0[0]=VS_S00SerialNumber;
								byteArray0[1]=(byte) byteArray.length;
								for (int i = 0; i < byteArray.length; i++) {
									byteArray0[i+2]=byteArray[i];
								}
								sendChangedString(byteArray0, OPCODE_SET_PARAM);
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
	//
	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}



}
