package com.saintsung.saintpmc.configuration;

import android.support.v7.app.AppCompatActivity;

import com.saintsung.saintpmc.R;
import com.saintsung.saintpmc.tool.ToastUtil;

public class BaseActivity extends AppCompatActivity {
	@Override
	protected void onStart() {
		super.onStart();
		// 沉浸式状态栏
		ToastUtil.setColor(this, getResources().getColor(R.color.top_bg));
		// 公共侧边栏
	}

}
