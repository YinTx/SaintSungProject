package com.saintsung.saintpmc.lock;

import android.util.Log;
import com.saintsung.saintpmc.lock.DeviceService.RecvAction;
import java.util.ArrayList;

public class ChainBase implements DeviceService.ActionChain{
	DeviceService mService = null;
	int mIndex = 0;
	User_Share user_Share = new User_Share();


	class UnProcException extends Exception{

		UnProcException(MCUCommand mcuCmd){
			super(String.format("unproc cmd:%c",mcuCmd.mCmd));
		}
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

	}

	ArrayList<RecvAction> mActionList = new ArrayList<RecvAction>();
	ChainBase(DeviceService service){
		this.mService = service;
	}
	@Override
	public RecvAction Next() {
		if(mIndex >= (this.mActionList.size())){
			return null;
		}
		RecvAction action = this.mActionList.get(mIndex);
		Log.d("mIndex", "mIndex"+"RecvAction Next()"+mIndex);
		return action;


	}
	@Override
	public void onConnect() {
		this.mIndex = 0;
		Log.d("mIndex=0", "mIndex=0"+"onConnect");
	}
	@Override
	public void onDisconnect() {
		// TODO Auto-generated method stub

	}
	@Override
	public void reset() {
		mIndex = 0;
		Log.d("mIndex=0", "mIndex=0"+"reset");
	}
}
