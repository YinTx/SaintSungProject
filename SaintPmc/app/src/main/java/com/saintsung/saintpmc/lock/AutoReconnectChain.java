package com.saintsung.saintpmc.lock;

import java.util.Date;

import android.content.Intent;

import com.saintsung.saintpmc.lock.BigSmallChain;
import com.saintsung.saintpmc.lock.DeviceService.RecvAction;

public class AutoReconnectChain extends BigSmallChain {
	boolean mAutoReconnect = false;
	int mMaxTimes = 1;
	int mTestTimes = 0;
	int mTotallTestTimes = 0;
	long mFirstTime =  new Date().getTime();

	AutoReconnectChain(DeviceService service) {
		super(service);
		mActionList.clear();
		mActionList.add(new RecvAction(){

			@Override
			public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {
				mIndex = 0;
				mService.mDevice.PostRun(new Runnable(){

					@Override
					public void run() {
						mTotallTestTimes ++;
						mAutoReconnect = true;
						mService.addLog((byte)0,"蓝牙断开中...");
						mService.mDevice.Disconnect();
					}

				},1000);
			}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

			}

		});
	}
	void connectDevice(String address){
		Intent i = new Intent();
		i.setClass(mService,DeviceService.class);
		i.putExtra(DeviceService.EXTRA_ADDRESS, address);
		i.setAction(DeviceService.ACTION_CONNECT);
		mService.startService(i);
	}
	@Override
	public void onDisconnect() {
		if(mAutoReconnect){
			mService.addLog((byte)0,"蓝牙已经断开");
			String address = mService.mDevice.mAddress;
			connectDevice(address);
		}else{
			mService.mTestTimes = 0;
			mFirstTime = new Date().getTime();
		}
	}
	@Override
	public void onConnect() {
		if(mAutoReconnect){
			mService.mTestTimes = mTotallTestTimes;
			mService.mStartTime = mFirstTime;
			mService.addLog((byte)0,"蓝牙已经连接");
			mAutoReconnect = false;
			mService.mDevice.PostRun(new Runnable(){

				@Override
				public void run() {
					mIndex = 0;
					mTotallTestTimes ++;
					mAutoReconnect = true;
					mService.addLog((byte)0,"蓝牙断开中...");
					mService.mDevice.Disconnect();
				}

			},3000);
		}else{
			mIndex = 0;
		}
	}

}
