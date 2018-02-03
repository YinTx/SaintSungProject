package com.saintsung.saintpmc.lock;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.saintsung.saintpmc.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class NetworkConnect extends Activity {
	//
	public static boolean checkNet(Context context) {
		try {
			ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivityManager != null) {
				NetworkInfo info = connectivityManager.getActiveNetworkInfo();
				if (info != null && info.isConnected()) {
					if (info.getState() == NetworkInfo.State.CONNECTED) {
						if (ping()) {
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	/**
	 * 检查网络是否可用
	 *
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {

		ConnectivityManager manager = (ConnectivityManager) context
				.getApplicationContext().getSystemService(
						Context.CONNECTIVITY_SERVICE);

		if (manager == null) {
			return false;
		}

		NetworkInfo networkinfo = manager.getActiveNetworkInfo();

		if (networkinfo == null || !networkinfo.isAvailable()) {
			return false;
		}

		return true;
	}

	/*
	 * 使用ping命令，判断当前网络是否可用
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	public static final boolean ping() {
		String result = null;
		try {
			String ip = "www.baidu.com"; //ping 地址，可以换成任何一种可靠的外网。
			Process process = null;
			try {
				process = Runtime.getRuntime().exec("ping -c 3 " + ip); //外网IP	//-n 3 -w 100
			} catch (IOException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			//读取ping 内容，可以不加
			InputStream input = process.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(input));
			StringBuffer stringBuffer = new StringBuffer();
			String content = "";
			while ((content = in.readLine()) != null) {
				stringBuffer.append(content);
			}
			Log.d("------ping -----", "result content: " + stringBuffer.toString());
			//ping 状态
			int status = process.waitFor();
			if (status == 0) {
				result = "success";
				return true;
			} else {
				result = "failed";
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		return false;

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		//检测网络连接
		boolean flag = false;
		//获取应用上下文
		Context context = getApplicationContext();
		//获取实例
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		//获取代表联网状态的NetWorkInfo对象
		NetworkInfo netinfo = connectivityManager.getActiveNetworkInfo();
		//		if (networkInfos != null) {
		//		for (int i = 0; i < networkInfos.length; i++) {
		//		if (networkInfos[i].getState() == NetworkInfo.State.CONNECTED) {
		//		}
		//		}
		//		}
		if (netinfo != null) {
			// 检测是否联网
			flag = netinfo.isAvailable();
			Toast.makeText(NetworkConnect.this, "登陆中...", Toast.LENGTH_LONG).show();
		}
		//若没有网络,提示是否开启网络
		if (!flag) {
			//给用户提示网络状态
			Toast.makeText(getApplicationContext(), "当前网络" + add(netinfo.isAvailable()) + "," + "网络" + app(netinfo.isConnected()) + "," + "网络连接" + adp(netinfo.isConnected()), Toast.LENGTH_LONG).show();
			Builder b = new Builder(NetworkConnect.this).setTitle("网络连接状态:").setMessage("当前网络" + add(netinfo.isAvailable()) + "," + "网络" + app(netinfo.isConnected()) + "," + "网络连接" + adp(netinfo.isConnected()));
			b.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Intent mIntent = new Intent("/");
					ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
					mIntent.setComponent(comp);
					mIntent.setAction("android.intent.action.VIEW");
					getApplicationContext().startActivity(mIntent);
				}
			}).setNeutralButton("取消", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.cancel();
				}
			}).create();
			b.show();
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

	// 检测是否连接网络的checkNetwork方法
	private void checkNetwork(final Context context) {
		boolean flag = false;
		// 获取实例
		ConnectivityManager cwjManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
		if (cwjManager.getActiveNetworkInfo() != null) {
			// 检测是否联网
			flag = cwjManager.getActiveNetworkInfo().isAvailable();
		}
		//若没有网络,提示是否开启网络
		if (!flag) {
			Builder b = new Builder(context).setTitle("没有可用的网络").setMessage("");
			b.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Intent mIntent = new Intent("/");
					ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
					mIntent.setComponent(comp);
					mIntent.setAction("android.intent.action.VIEW");
					context.startActivity(mIntent);
				}
			}).setNeutralButton("取消", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.cancel();
				}
			}).create();
			b.show();
		}
	}

}
