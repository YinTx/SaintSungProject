package com.saintsung.saintpmc.lock;


import com.saintsung.saintpmc.MainActivity;
import com.saintsung.saintpmc.lock.DeviceService.RecvAction;

import android.util.Log;

public class UnlockValve extends ChainBase {

	boolean mOpen = false;
	String lockNumber="100000004";
	String lockKey = "000236288238200";

	public byte[] byteArray;


	public FileStream fileStream = new FileStream();

	//从文件中读取开锁码
	public String[] fun_SearchKey() {
		byteArray =	fileStream.fileStream(FileStream.valve_key, FileStream.read, null);

		if (byteArray != null && byteArray.length > 0) {
			String string = new String(byteArray);
			String[] str_array = string.split(":");

			return str_array;
		}
		return null;
	}


	UnlockValve(DeviceService service) {
		super(service);
		service.setBigOpen(0);

		//响应开/关阀门
		this.mActionList.add(new RecvAction(){
			@Override
			public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {
				//从文件中读取开锁码
				String[] str_Array = fun_SearchKey();
				if (str_Array != null && str_Array.length > 0) {
					lockNumber = str_Array[0];
					lockKey = str_Array[1];
				}else {
					//mService.addLog(mcuCmd.mCmd,"此阀门没有关联，请检查文件");
					lockNumber="100000004";
					lockKey = "000236288238200";

				}

				switch(mcuCmd.mCmd){
					case SetAllActivity.OPCODE_SET_PARAM:{
						Log.d("Set", "Set" + "UnlockValue");
						//must clear
						MainActivity.handLockNumber=null;
						if(!mcuCmd.mErrCode.equals("00")){
							mIndex = 0;
//						throw new Exception("掌机切换为开阀门模式失败!");
						}
//					mService.addLog(mcuCmd.mCmd,"掌机切换为开阀门模式成功!");
						mIndex=0;
					}break;
					case MCUCommand.OPCODE_OPEN_SCREW_BEFORE:{
						//wake up
						MainActivity.state_sleep=null;
						if(!mcuCmd.mErrCode.equals("00")){
							mIndex = 0;
							if (mcuCmd.mAttechData[0]!=0) {
								mService.addLog(mcuCmd.mCmd,MCUCommand.warn_charge);
							}
							throw new Exception(MCUCommand.open_valve_prepare_false);
						}else{
							mService.addLog(mcuCmd.mCmd,"阀门号:"+lockNumber);
							if (mcuCmd.mAttechData[0]!=0) {
								mService.addLog(mcuCmd.mCmd,MCUCommand.warn_charge);
							}
							byte[] cmdBytes = Command.getInstance().AppSendLockerKey(MCUCommand.opcode_open_screw,lockNumber,lockKey,0,DeviceService.mOtherLock);
							mService.mDevice.Write(FileStream.write,cmdBytes);
							mIndex++;
						}
					}break;
					case MCUCommand.OPCODE_CLOSE_SCREW_BEFORE:{
						//wake up
						MainActivity.state_sleep=null;
						if(!mcuCmd.mErrCode.equals("00")){
							mIndex = 0;
							if (mcuCmd.mAttechData[0]!=0) {
								mService.addLog(mcuCmd.mCmd,MCUCommand.warn_charge);
							}
							throw new Exception(MCUCommand.close_valve_prepare_false);
						}else{
							//send close valve
							byte[] cmdBytes = Command.getInstance().AppSendClose(MCUCommand.opcode_close_screw);
							if (mcuCmd.mAttechData[0]!=0) {
								mService.addLog(mcuCmd.mCmd,MCUCommand.warn_charge);
							}
							mService.mDevice.Write(FileStream.write,cmdBytes);
							mService.addLog(mcuCmd.mCmd,"阀门号:"+lockNumber);
							mIndex++;
						}
					}break;
					case MCUCommand.OPCODE_WAIT_FALL_IN_UNLOCK:{
						//wake up
						MainActivity.state_sleep=null;
						//must clear
						MainActivity.handLockNumber=null;
						mIndex = 0;
						if(!mcuCmd.mErrCode.equals("00")){
							if (mcuCmd.mAttechData[0]!=0) {
								mService.addLog(mcuCmd.mCmd,MCUCommand.warn_charge);
							}
//						throw new Exception("落螺丝失败(开)!");
							throw new Exception("落锁失败(开),掌机固件未升级!");
						}else{
							mService.addLog(mcuCmd.mCmd,"落锁成功(开),掌机固件未升级!");
							if (mcuCmd.mAttechData[0]!=0) {
								mService.addLog(mcuCmd.mCmd,MCUCommand.warn_charge);
							}
						}
					}break;
					case MCUCommand.OPCODE_WAIT_FALL_IN_LOCK:{
						//wake up
						MainActivity.state_sleep=null;
						//must clear
						MainActivity.handLockNumber=null;
						mIndex = 0;
						if(!mcuCmd.mErrCode.equals("00")){
							if (mcuCmd.mAttechData[0]!=0) {
								mService.addLog(mcuCmd.mCmd,MCUCommand.warn_charge);
							}
//						throw new Exception("落螺丝失败(关)!");
							throw new Exception("落锁失败(关),掌机固件未升级!");
						}
						//
						mService.addLog(mcuCmd.mCmd,"落锁成功(关),掌机固件未升级!");
						if (mcuCmd.mAttechData[0]!=0) {
							mService.addLog(mcuCmd.mCmd,MCUCommand.warn_charge);
						}
						mIndex =0;
					}break;
					case MCUCommand.opcode_sleep:{
						if(!mcuCmd.mErrCode.equals("00")){
							mIndex = 0;
							throw new Exception("掌机休眠失败!");
						}
						MainActivity.state_sleep=MainActivity.SLEEP;
						mService.addLog(mcuCmd.mCmd,"掌机进入休眠状态,按开/关键唤醒掌机后再进行其他操作!");
						mIndex=0;
					}break;
					default:{
						//wake up
						MainActivity.state_sleep=null;
						//must clear
						MainActivity.handLockNumber=null;
//					mService.addLog(mcuCmd.mCmd,mcuCmd.mCmd+"待分析指令:"+new String(data));
//					mService.addLog(mcuCmd.mCmd,"中断操作,选择<手动开锁>后,可重新开始!");
						mIndex = 0;
					}
				}
			}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

			}

		});
		//响应打开/关闭阀门
		this.mActionList.add(new RecvAction(){
			@Override
			public void Do(final MCUCommand mcuCmd, byte[] data) throws Exception {
				switch(mcuCmd.mCmd){
					case MCUCommand.opcode_open_screw:
					case MCUCommand.OPCODE_OPEN_LOCK:
					case MCUCommand.OPCODE_OPEN_LOCK15:{
						mIndex = 0;
						if(!mcuCmd.mErrCode.equals("00")){
//						throw new Exception(MCUCommand.open_valve_false);
							throw new Exception(MCUCommand.open_valve_success);
						}else{
							mService.addLog(mcuCmd.mCmd,MCUCommand.open_valve_success);
						}
						//上传开阀门记录到服务器
						mService.uploadLog(MCUCommand.operatestate_unlock,lockNumber,null);
					}break;
					case MCUCommand.opcode_close_screw:
					case MCUCommand.OPCODE_CLOSE_LOCK:{
						mIndex = 0;
						if(!mcuCmd.mErrCode.equals("00")){
//						throw new Exception(MCUCommand.close_valve_false);
							throw new Exception(MCUCommand.close_valve_success);
						}else{
							mService.addLog(mcuCmd.mCmd,MCUCommand.close_valve_success);
						}
						//上传关阀门记录到服务器
						mService.uploadLog(MCUCommand.operatestate_lock,lockNumber,null);
					}break;
					default:{
						//must clear
						MainActivity.state_sleep=null;
//					mService.addLog(mcuCmd.mCmd,"中断操作,选择<手动开锁>后,可重新开始!");
//					mService.addLog(mcuCmd.mCmd,mcuCmd.mCmd+"待分析指令:"+new String(data));
						mService.addLog(mcuCmd.mCmd,"之前输入阀门号有误,请重新操作!");
						mIndex = 0;
					}break;
				}
			}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

			}
		});
	}
}
