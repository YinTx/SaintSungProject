package com.saintsung.saintpmc.lock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.saintsung.saintpmc.MainActivity;
import com.saintsung.saintpmc.R;

public class LoginActivity extends Activity {
	private SharedPreferences sp;
	private EditText userName, password;
	private CheckBox rem_pw, auto_login;
	private Button loginButton;
	//static的作用
	private String passwordValue, string, requestPacket, loginOne, progress;
	private static final String login_serviceId = "L001";
	private static final String login_state = "登陆中,请稍后...";
	private static final String name = "S";
	private static final String pwd = "P";
	public static String responsePacket;
	public static final int requestUsernameOrPasswordLength = 10;
	public static String userId, userNameValue;
	static String state;
	byte[] byteArray;
	String[] stringArray;
	FileStream fileStream = new FileStream();

	ToastShow toastShow = new ToastShow(this);

	//定时器
	private int i_times = 0;

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 1) {

				if (i_times < 15) {
					toastShow.toastShow("登录中，请稍后..." + String.valueOf(i_times++) + "s");
				} else if ((i_times > 15) && (i_times < 20)) {
					i_times++;
					toastShow.toastShow("登录失败请检测输入");
				} else {
					i_times++;
					toastShow.toastShut();
				}

			}
			super.handleMessage(msg);
		}

	};

	Runnable runnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			handler.postDelayed(this, 1000);
			Message message = new Message();
			message.what = 1;
			handler.sendMessage(message);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//去除标题
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login);
		//写入ip地址，端口号
		//        socket();
		//获得实例对象
		sp = this.getSharedPreferences("userInfo", Context.MODE_WORLD_READABLE);
		userName = (EditText) findViewById(R.id.userName);
		password = (EditText) findViewById(R.id.password);
		//        rem_pw = (CheckBox) findViewById(R.id.cb_mima);
		//        auto_login = (CheckBox) findViewById(R.id.cb_auto);
		loginButton = (Button) findViewById(R.id.loginButton);

		byteArray = fileStream.fileStream(FileStream.userLogin, FileStream.read, null);
		string = new String(byteArray);

		stringArray = string.split(",");
		//fake connected bluetooth
		//		MainActivity.connect_state=MainActivity.CONNECTED;
		//未注销用户直接登录
		if (stringArray.length == 5) {

			Intent intent = new Intent(getApplicationContext(), MainActivity.class);
			startActivity(intent);
			finish();
		}
		loginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				userNameValue = userName.getText().toString();
				passwordValue = password.getText().toString();
				if (userNameValue.length() == 0 || userNameValue.equals("") || passwordValue.length() == 0 || passwordValue.equals("")) {
					Toast.makeText(LoginActivity.this, "用户名或密码不能为空!", Toast.LENGTH_LONG).show();
				} else if (userNameValue.equals(stringArray[0]) && passwordValue.equals(stringArray[1])) {
					//rewrite userLogin
					string = stringArray[0] + "," + stringArray[1] + "," + stringArray[2] + "," + stringArray[3] + "," + stringArray[4];
					fileStream.fileStream(FileStream.userLogin, FileStream.write, string.getBytes());
					//注销用户后,同1用户重新登录,跳过访问服务器这步;
					Intent intent = new Intent(getApplicationContext(), MainActivity.class);
					startActivity(intent);
					finish();
				} else if (name.equals(userNameValue) && pwd.equals(passwordValue)) {
					byteArray = fileStream.fileStream(FileStream.socket, FileStream.read, null);
					string = new String(byteArray);
					if (string.length() == 0) {
						Intent intent = new Intent(getBaseContext(), SocketActivity.class);
						intent.putExtra(SocketConnect.socketError, "新增/修改您要访问的服务器的socket通信地址和端口后,再尝试重新登录!");
						startActivity(intent);
					} else if (string.length() > 0) {
						stringArray = string.split(";");
						Intent intent = new Intent(getBaseContext(), SocketActivity.class);
						intent.putExtra(SocketConnect.socketError, "新增/修改您要访问的服务器的socket通信地址和端口后,再尝试重新登录!");
						intent.putExtra(SocketConnect.socketAddress, stringArray[0]);
						intent.putExtra(SocketConnect.socketPort, stringArray[1]);
						intent.putExtra("LoginType", "SP");
						startActivity(intent);
					}

				} else {
					//验证网络连接
					boolean flagWlan = NetworkConnect.checkNet(getApplicationContext());
					if (flagWlan) {
						handler.removeCallbacks(runnable);
						i_times = 0;
						handler.postDelayed(runnable, 1000);
						login();
						if (login_state.equals(loginOne)) {
							//click loginButton wait for result
							Toast.makeText(LoginActivity.this, login_state, Toast.LENGTH_LONG).show();
						} else {
							//only one
							loginOne = login_state;
							login();
						}
					} else {
						Toast.makeText(LoginActivity.this, "连接网络后,再进行其他操作!", Toast.LENGTH_LONG).show();
					}
				}
			}

			//
			void login() {
				String namePacket = CommonResources.getleftFillSpaceStr(userNameValue, requestUsernameOrPasswordLength);
				String passPadket = CommonResources.getleftFillSpaceStr(passwordValue, requestUsernameOrPasswordLength);
				//IMEI（International Mobile Equipment Identity）是移动设备国际身份码的缩写，IMEI由15位数字组成。
				string = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
				//规范IMEI长度
				do {
					//长度不足左补0
					string = "0" + string;
				} while (string.length() < 18);
				string = login_serviceId + namePacket + passPadket + string;
				//请求打包
				requestPacket = CommonResources.createRequestPacket(string);
				//验证网络连接
				boolean flagWlan = NetworkConnect.checkNet(getApplicationContext());
				if (flagWlan) {

					new MyTask().execute(requestPacket, progress, responsePacket);
				} else {
					Toast.makeText(LoginActivity.this, "连接网络后,再进行其他操作!", Toast.LENGTH_LONG).show();
				}
			}
		});

		/**
		 //监听记住密码多选框按钮事件
		 rem_pw.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		 public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
		 if (rem_pw.isChecked()) {
		 //记住密码已选中;
		 sp.edit().putBoolean("ISCHECK", true).commit();
		 }else {
		 //记住密码没有选中;
		 sp.edit().putBoolean("ISCHECK", false).commit();
		 }
		 }
		 });

		 //监听自动登录多选框事件
		 auto_login.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		 public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
		 if (auto_login.isChecked()) {
		 //自动登录已选中;
		 sp.edit().putBoolean("AUTO_ISCHECK", true).commit();
		 } else {
		 //自动登录没有选中;
		 sp.edit().putBoolean("AUTO_ISCHECK", false).commit();
		 }
		 }
		 });
		 */

	}

	//UI子线程
	class MyTask extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {

			//连接服务器,发送登陆信息.
			SocketConnect socketConnect = new SocketConnect();
			responsePacket = socketConnect.sendDate(requestPacket);
			return null;
		}

		@Override
		protected void onProgressUpdate(String... progress) {
			Toast.makeText(getApplicationContext(), "登录中，请稍候...", Toast.LENGTH_LONG).show();

		}

		@Override
		protected void onPostExecute(String result) {
			handler.removeCallbacks(runnable);

			if (responsePacket == null) {
				Intent intent = new Intent(getBaseContext(), SocketActivity.class);
				intent.putExtra(SocketConnect.socketError, "socket通信地址和端口不能为空,填写正确后重新登录!");
				startActivity(intent);
			} else {
				if (responsePacket.startsWith("java.net.UnknownHostException:")) {
					byteArray = fileStream.fileStream(FileStream.socket, FileStream.read, null);
					string = new String(byteArray);
					stringArray = string.split(";");
					Intent intent = new Intent(getBaseContext(), SocketActivity.class);
					intent.putExtra(SocketConnect.socketError, "socket通信地址输入有误,确认修改正确后,再尝试重新登录!");
					intent.putExtra(SocketConnect.socketAddress, stringArray[0]);
					intent.putExtra(SocketConnect.socketPort, stringArray[1]);
					startActivity(intent);
				} else if (responsePacket.startsWith("java.lang.IllegalArgumentException: Port out of range:")) {
					byteArray = fileStream.fileStream(FileStream.socket, FileStream.read, null);
					string = new String(byteArray);
					stringArray = string.split(";");
					Intent intent = new Intent(getBaseContext(), SocketActivity.class);
					intent.putExtra(SocketConnect.socketError, "socket通信端口输入有误,确认修改正确后,再尝试重新登录!");
					intent.putExtra(SocketConnect.socketAddress, stringArray[0]);
					intent.putExtra(SocketConnect.socketPort, stringArray[1]);
					startActivity(intent);
				} else if (responsePacket.startsWith("java.net.ConnectException")) {
					Toast.makeText(getBaseContext(), "网络断开连接,导致访问服务器登录超时,确定网络正常后再尝试重新登录!", Toast.LENGTH_LONG).show();
				} else if (responsePacket.contains("SocketTimeoutException")) {
					Toast.makeText(getBaseContext(), "服务器登录超时,确认服务器能正常访问后再尝试重新下载!", Toast.LENGTH_LONG).show();
				} else if (responsePacket.startsWith(login_serviceId, 6)) {
					toastShow.toastShut();
					handler.removeCallbacks(runnable);
					//拆分服务器返回数据
					CommandPacker commandPacker = new CommandPacker();
					commandPacker.decodeResultFlag(responsePacket);
					// 等待通讯成功
					if (CommandPacker.succ_flag) {
						CommandPacker.succ_flag = false;
						Toast.makeText(LoginActivity.this, "登录成功!", Toast.LENGTH_SHORT).show();
						userId = CommonResources.getPacketRtnUidByResult(responsePacket);
						String SXTimes = CommonResources.getPacketRtnSXTimesByResult(responsePacket);
						String ZQTimes = CommonResources.getPacketRtnZQTimesByResult(responsePacket);
						string = userNameValue + "," + passwordValue + "," + userId + "," + SXTimes + "," + ZQTimes;
						fileStream.fileStream(FileStream.userLogin, FileStream.write, string.getBytes());
						fileStream.fileStream(FileStream.userDownload, FileStream.delete, null);
						Intent intent = new Intent(getApplicationContext(), MainActivity.class);
						startActivity(intent);
						finish();
					} else {
						Toast.makeText(LoginActivity.this, "用户名或密码有误,请重新登录!", Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(getBaseContext(), "服务器登录超时,确认服务器能正常访问后再尝试重新登录!", Toast.LENGTH_LONG).show();
				}
			}
			//must
			loginOne = null;
		}
	}

	String add(Boolean bl) {
		String s = "不可用";
		if (bl == true) {
			s = "可用";
		}
		return s;
	}

	String app(Boolean bl) {
		String s = "未连接";
		if (bl == true) {
			s = "已连接";
		}
		return s;
	}

	String adp(Boolean bl) {
		String s = "不存在！";
		if (bl == true) {
			s = "存在！";
		}
		return s;
	}

	public byte[] fun_ReversalLoginInfo(String str_LoginInfo) {
		byte[] by_data = str_LoginInfo.getBytes();
		//		byte by_temp = 0;
		for (int i = 0; i < by_data.length - 1; i++) {
			by_data[i] = (byte) (by_data[i] ^ by_data[i + 1]);
		}

		return by_data;
	}

	//[[固定IP,固定端口号]]
	public void socket() {
		String string = "210.22.164.146";
		String string0 = "7013";
		if (string.equals("") || string0.equals("")) {

		} else {
			string = string + ";" + string0;
			//write socketAddress
			fileStream.fileStream(FileStream.socket, FileStream.write, string.getBytes());

		}
	}

}
