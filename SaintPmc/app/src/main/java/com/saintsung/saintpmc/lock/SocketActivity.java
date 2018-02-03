package com.saintsung.saintpmc.lock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.saintsung.saintpmc.R;

public class SocketActivity extends Activity {
	byte[] byteArray;
	String[] stringArray;
	public static String string, string0, socketAddressValue, socketPortValue, stringFileStream;
	EditText editTextSocketAddress, editTextSocketPort;
	Button button;
	TextView textViewTile, textViewSocketAddress, textViewSocketPort;
	Intent intent = new Intent();
	FileStream fileStream = new FileStream();

	String str_service = "";
	String loginType = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// 去除标题
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.socket_url);
		intent = getIntent();
		textViewTile = (TextView) this.findViewById(R.id.title);
		string = intent.getStringExtra(SocketConnect.socketError);
		textViewTile.setText(string);
		textViewTile.setTextColor(getResources().getColor(R.color.red));
		editTextSocketAddress = (EditText) this.findViewById(R.id.socketAddressValue);
		socketAddressValue = intent.getStringExtra(SocketConnect.socketAddress);
		editTextSocketAddress.setText(socketAddressValue);
		textViewSocketAddress = (TextView) this.findViewById(R.id.resultSocketAddress);
		textViewSocketAddress.setTextColor(getResources().getColor(R.color.red));
		editTextSocketPort = (EditText) this.findViewById(R.id.socketPortValue);
		socketPortValue = intent.getStringExtra(SocketConnect.socketPort);
		editTextSocketPort.setText(socketPortValue);
		loginType = intent.getStringExtra("LoginType");
		str_service = new String(fileStream.fileStream(FileStream.socket, FileStream.read, null));
		if (str_service != null && str_service.length() > 4 && str_service.contains(";")) {

			String[] service = str_service.split(";");
			editTextSocketAddress.setText(service[0]);
			editTextSocketPort.setText(service[1]);

			if (loginType == null) {
				//有socket 信息则直接跳转到登录界面
				//				finish();
				Intent intent = new Intent(SocketActivity.this, LoginActivity.class);
				startActivity(intent);
			} else if (loginType != null && loginType.equals("SP")) {

			}

		}

		textViewSocketPort = (TextView) this.findViewById(R.id.resultSocketPort);
		textViewSocketPort.setTextColor(getResources().getColor(R.color.red));
		button = (Button) this.findViewById(R.id.save);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				socket();
			}
		});
	}

	//
	//[[固定IP,固定端口号]]
	void socket() {
		string = editTextSocketAddress.getText().toString();
		string0 = editTextSocketPort.getText().toString();
		//		string = "210.22.164.146";
		//		string0 = "7013";
		if (string.length() <= 0 && string0.length() <= 0) {
			string = getString(R.string.ip);
			string0 = getString(R.string.port);
		}

		if (string.equals("") || string0.equals("")) {
			if (string.equals("")) {
				textViewSocketAddress.setText("socket地址不能为空!");
			} else if (string0.equals("")) {
				textViewSocketPort.setText("socket端口不能为空!");
			}
		} else {
			string = string + ";" + string0;
			//write socketAddress
			fileStream.fileStream(FileStream.socket, FileStream.write, string.getBytes());
			finish();
			Intent intent = new Intent(SocketActivity.this, LoginActivity.class);
			startActivity(intent);

		}
	}

}
