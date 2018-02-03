package com.saintsung.saintpmc.lock;


import com.saintsung.saintpmc.MainActivity;
import com.saintsung.saintpmc.lock.DeviceService.OnReadKey;
import com.saintsung.saintpmc.lock.DeviceService.RecvAction;

public class UnlockUser extends ChainBase {

	boolean mOpen = false;
	String lockNumber;
	UnlockUser(DeviceService service) {
		super(service);
		service.setBigOpen(0);
		//响应开关锁
		this.mActionList.add(new RecvAction(){

			@Override
			public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {


			}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

			}


/*
			@Override
			public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {
				switch(mcuCmd.mCmd){
				case MCUCommand.OPCODE_WAIT_FALL_IN_UNLOCK:{
					//[[wk
					//must clear
					MainActivity.state_sleep=null;
					//]]
					if(mcuCmd.mErrCode != 0){
						mIndex = 0;
						//must clear
						MainActivity.handLockNumber=null;
						if (mcuCmd.mAttechData[0]!=0) {
							mService.addLog(mcuCmd.mCmd,MCUCommand.warn_charge);
						}
						throw new Exception("开锁落锁失败");
					}
					//
					mOpen = true;
					mIndex ++;
					//first
					mService.addLog(mcuCmd.mCmd,"开锁落锁成功");
					if (mcuCmd.mAttechData[0]!=0) {
						mService.addLog(mcuCmd.mCmd,MCUCommand.warn_charge);
					}
					if (MainActivity.handLockNumber!=null) {
						mService.addLog(mcuCmd.mCmd,"输入锁号:"+MainActivity.handLockNumber);
						mService.readKey(MainActivity.handLockNumber, new OnReadKey(){

							@Override
							public void On(final String key, Exception e) {
								if(e == null){
									byte cmd = 0;
									if(key.length() == 15){
										cmd = 'o';
									}else{
										cmd = 'O';
									}
									byte[] cmdBytes = Command.getInstance().AppSendLockerKey(cmd,MainActivity.handLockNumber,key,0,DeviceService.mNormalLock);
									mService.mDevice.Write(FileStream.write,cmdBytes);
									lockNumber=MainActivity.handLockNumber;
								}else{
									mIndex=0;
								}
							}
						});
					} else {
						//readLockNumber
						byte[] cmdBytes = Command.getInstance().AppReadLockerNunber();
						mService.mDevice.Write(FileStream.write,cmdBytes);
					}
				}break;
				case MCUCommand.OPCODE_WAIT_FALL_IN_LOCK:{
					//[[wk
					//must clear
					MainActivity.state_sleep=null;
					//]]
					if(mcuCmd.mErrCode != 0){
						mIndex = 0;
						if (mcuCmd.mAttechData[0]!=0) {
							mService.addLog(mcuCmd.mCmd,MCUCommand.warn_charge);
						}
						throw new Exception("关锁落锁失败");
					}

					if(User_Share.i_contact_lock == 1)		//接触式开锁
					{
						user_Share.fun_SetValue(User_Share.i_UnOpenLock);

						byte[] cmdBytes = Command.getInstance().AppReadLockerNunber();
						mService.mDevice.Write(FileStream.write,cmdBytes);
					}else{
						//byte[] cmdBytes = Command.getInstance().AppSendClose(MCUCommand.OPCODE_CLOSE_LOCK);
						byte[] cmdBytes = Command.getInstance().AppReadLockerNunber();
						mService.mDevice.Write(FileStream.write,cmdBytes);
					}


					mService.addLog(mcuCmd.mCmd,"关锁落锁成功");
					if (mcuCmd.mAttechData[0]!=0) {
						mService.addLog(mcuCmd.mCmd,MCUCommand.warn_charge);
					}
//					byte[] cmdBytes = Command.getInstance().AppReadLockerNunber();
//					mService.mDevice.Write(FileStream.write,cmdBytes);
					mOpen = false;
					mIndex ++;
				}break;
				case MCUCommand.opcode_sleep:{
					if(mcuCmd.mErrCode != 0){
						mIndex = 0;
						throw new Exception("掌机休眠失败!");
					}
					MainActivity.state_sleep=MainActivity.SLEEP;
					mService.addLog(mcuCmd.mCmd,"掌机进入休眠状态,按下开/关锁键可唤醒掌机后再进行其他操作!");
					mIndex=0;
				}break;
				default:{
					MainActivity.state_sleep=null;
					mIndex = 0;
				}
				}
			}
*/

		});
		//响应读锁号
		this.mActionList.add(new RecvAction(){

			@Override
			public void Do(final MCUCommand mcuCmd, byte[] data) throws Exception {
				switch(mcuCmd.mCmd){
					case 'S':{
						if(!mcuCmd.mErrCode.equals("00")){
							mIndex = 0;
							throw new Exception("读锁号失败");
						}
						mService.addLog(mcuCmd.mCmd,"读锁号:"+mcuCmd.mLockerNumber+"成功");
						if(mOpen){
							String str_key = mService.readKey(mcuCmd.mLockerNumber, new OnReadKey(){
								@Override
								public void On(final String key,final String lockType , Exception e) {
									if(e == null){
										byte cmd = 0;
										if(key.length() == 15){
											cmd = 'o';
										}else{
											cmd = 'O';
										}
										byte[] cmdBytes = Command.getInstance().AppSendLockerKey(cmd,mcuCmd.mLockerNumber,key,lockType,0);
										mService.mDevice.Write(FileStream.write,cmdBytes);
										if (key.equals(mService.str_ErrorKey)) {
											mIndex = 0;
											//	lockNumber=mcuCmd.mLockerNumber;
										}else{
											mIndex ++;
											lockNumber=mcuCmd.mLockerNumber;
										}

									}else{

										mService.addLog(mcuCmd.mCmd,e.getMessage());
										mIndex=0;
									}
								}

							});



						}else{
							byte[] cmdBytes = Command.getInstance().AppSendClose(MCUCommand.OPCODE_CLOSE_LOCK);
							mService.mDevice.Write(FileStream.write,cmdBytes);
							lockNumber=mcuCmd.mLockerNumber;
							mIndex ++;
						}
					}break;
					case 'o':
					case 'O':{
						if(!mcuCmd.mErrCode.equals("00")){
							mIndex = 0;
							//must clear
							MainActivity.handLockNumber=null;
							throw new Exception("手动开锁失败");
						}
						mService.addLog(mcuCmd.mCmd,"手动开锁成功");
						mIndex =0;
						//must clear
						MainActivity.handLockNumber=null;
						//上传手动开锁记录到服务器
						mService.uploadLog(MCUCommand.operatestate_unlock,lockNumber,null);
					}break;
					default:{
						mIndex = 0;
					}break;
				}
			}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

			}
		});
		//响应开关锁
		this.mActionList.add(new RecvAction(){

			@Override
			public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {
				switch(mcuCmd.mCmd){
					case 'o':
					case 'O':{
						if(!mcuCmd.mErrCode.equals("00")){
							mIndex = 0;
							throw new Exception("开锁失败");
						}
						mService.addLog(mcuCmd.mCmd,"开锁成功");
						mIndex =0;
						//上传开锁记录到服务器
						mService.uploadLog(MCUCommand.operatestate_unlock,lockNumber,null);
					}break;
					case 'C':{
						if(!mcuCmd.mErrCode.equals("00")){
							mIndex = 0;
							throw new Exception("关锁失败");
						}
						mService.addLog(mcuCmd.mCmd,"关锁成功");
						mIndex =0;
						//上传关锁记录到服务器
						mService.uploadLog(MCUCommand.operatestate_lock,lockNumber,null);
					}break;
					default:{
						mIndex = 0;
					}
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
