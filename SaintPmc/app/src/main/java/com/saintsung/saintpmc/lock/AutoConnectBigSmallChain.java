package com.saintsung.saintpmc.lock;

import java.util.Date;

import android.content.Intent;

import com.saintsung.saintpmc.lock.BigSmallChain;
import com.saintsung.saintpmc.lock.DeviceService;
import com.saintsung.saintpmc.lock.MCUCommand;
import com.saintsung.saintpmc.lock.DeviceService.RecvAction;

public class AutoConnectBigSmallChain extends BigSmallChain {

	boolean mAutoReconnect = false;
	int mMaxTimes = 2;
	int mTestTimes = 0;
	int mTotallTestTimes = 0;
	long mFirstTime =  new Date().getTime();
	AutoConnectBigSmallChain(DeviceService service) {
		super(service);
		//替换最后一个RecvAction
		final RecvAction acton = mActionList.get(mActionList.size()-1);
		mActionList.set(mActionList.size()-1, new RecvAction(){

			@Override
			public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {

				mTestTimes++;
				if(mTestTimes >= mMaxTimes){
					mTotallTestTimes ++;
					mAutoReconnect = true;
					mService.addLog(mcuCmd.mCmd,"蓝牙断开中...");
					mService.mDevice.Disconnect();
				}else{
					acton.Do(mcuCmd, data);
				}
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
					AutoConnectBigSmallChain.this.simulatorOpenReady();
				}

			},3000);
		}else{
			mIndex = 0;
		}
	}
}
