package com.saintsung.saintpmc.lock;

import com.saintsung.saintpmc.MainActivity;
import com.saintsung.saintpmc.lock.DeviceService.RecvAction;

public class ReadLockNumber extends ChainBase {

	boolean mOpen = false;
	ReadLockNumber(DeviceService service) {
		super(service);
		service.setBigOpen(0);
		//响应开关锁
		this.mActionList.add(new RecvAction(){

			@Override
			public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {
				switch(mcuCmd.mCmd){
					case MCUCommand.OPCODE_WAIT_FALL_IN_UNLOCK:{
						//[[wk
						//must clear
						MainActivity.state_sleep=null;
						//]]
						if(!mcuCmd.mErrCode.equals("00")){
							mIndex = 0;
							if (mcuCmd.mAttechData[0]!=0) {
								mService.addLog(mcuCmd.mCmd,MCUCommand.warn_charge);
							}
							throw new Exception("开锁落锁失败");
						}
						mOpen = true;
						mService.addLog(mcuCmd.mCmd,"开锁落锁成功");
						if (mcuCmd.mAttechData[0]!=0) {
							mService.addLog(mcuCmd.mCmd,MCUCommand.warn_charge);
						}
						byte[] cmdBytes = Command.getInstance().AppReadLockerNunber();
						mService.mDevice.Write(FileStream.write,cmdBytes);
						mIndex ++;
					}break;
					case MCUCommand.OPCODE_WAIT_FALL_IN_LOCK:{
						//[[wk
						//must clear
						MainActivity.state_sleep=null;
						//]]
						if(!mcuCmd.mErrCode.equals("00")){
							mIndex = 0;
							if (mcuCmd.mAttechData[0]!=0) {
								mService.addLog(mcuCmd.mCmd,MCUCommand.warn_charge);
							}
							throw new Exception("关锁落锁失败");
						}
						mService.addLog(mcuCmd.mCmd,"关锁落锁成功");
						if (mcuCmd.mAttechData[0]!=0) {
							mService.addLog(mcuCmd.mCmd,MCUCommand.warn_charge);
						}
						byte[] cmdBytes = Command.getInstance().AppSendClose(MCUCommand.OPCODE_CLOSE_LOCK);
						mService.mDevice.Write(FileStream.write,cmdBytes);
						mIndex ++;
					}break;
					case MCUCommand.opcode_sleep:{
						if(!mcuCmd.mErrCode.equals("00")){
							mIndex = 0;
							throw new Exception("掌机休眠失败!");
						}
						MainActivity.state_sleep=MainActivity.SLEEP;
						mService.addLog(mcuCmd.mCmd,"掌机进入休眠状态,按下开/关锁键可唤醒掌机后再进行其他操作!");
						mIndex=0;
					}break;
					default:{
						mIndex = 0;
					}
				}
			}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

			}

		});
		//响应读锁号
		this.mActionList.add(new RecvAction(){

			@Override
			public void Do(final MCUCommand mcuCmd, byte[] data) throws Exception {
				switch(mcuCmd.mCmd){
					case 'C':{
						if(!mcuCmd.mErrCode.equals("00")){
							mIndex = 0;
							throw new Exception("关锁失败");
						}
						mService.addLog(mcuCmd.mCmd,"关锁成功");
						mIndex =0;
					}break;
					case 'S':{
						if(!mcuCmd.mErrCode.equals("00")){
							mIndex = 0;
							throw new Exception("读锁号失败");
						}
						mService.addLog(mcuCmd.mCmd,"读锁号:"+mcuCmd.mLockerNumber+"成功");
						mIndex = 0;
					}break;
					default:{
						mIndex = 0;
					}break;
				}
				mService.IncTestTime(true);
			}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

			}
		});
	}

}
