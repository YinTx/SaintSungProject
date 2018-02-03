package com.saintsung.saintpmc.lock;


import com.saintsung.saintpmc.MainActivity;
import com.saintsung.saintpmc.R;
import com.saintsung.saintpmc.lock.ChainBase;
import com.saintsung.saintpmc.lock.DeviceService;
import com.saintsung.saintpmc.lock.MCUCommand;
import com.saintsung.saintpmc.lock.DeviceService.OnReadKey;
import com.saintsung.saintpmc.lock.DeviceService.RecvAction;

public class BigSmallChain extends ChainBase {

	boolean mSimulatorOpenReady = false;
	boolean mContinueTest = true;

	// [[CXQ
	// private FileStream fileStream = new FileStream();

	// ]]

	// 默认的
	BigSmallChain(DeviceService service) {
		super(service);
		this.mService = service;
		// 响应开锁落锁成功
		this.mActionList.add(new RecvAction() {

			@Override
			public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

				mService.setBigOpen(1);
				switch (mcuCmd.mCmd) {
					// [[wk
					case MCUCommand.opcode_sleep: {
						if (!mcuCmd.mErrCode.equals("00")) {
							mIndex = 1;
							throw new Exception("掌机休眠失败!");
						}
						MainActivity.state_sleep = MainActivity.SLEEP;
						mService.addLog(mcuCmd.mCmd,
								"掌机进入休眠状态,按下开/关锁键可唤醒掌机后再进行其他操作!");
						mIndex = 0;
					}
					break;
					// ]]
					default: {
						mIndex = 0;
						throw new UnProcException(mcuCmd);
					}
				}


			}
		});
		// 响应锁号
		this.mActionList.add(new RecvAction() {

			@Override
			public void Do(final MCUCommand mcuCmd, byte[] data) throws Exception {}

			@Override
			public void Do(final MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

				if (mcuCmd.mCmd == 'o'||mcuCmd.mCmd=='O') {
					if (!mcuCmd.mErrCode.equals("00")) {
						mIndex = 1;// 从头开始执行
						throw new Exception("(大值)读锁号失败");
					}
					int offset=0;
					int openlock_type=0;		//正负开锁,0原值,1正负,2正负,3正负,4正负
					final String lockNo =  data.substring(offset + 5, offset + 14);
					mcuCmd.mLockerNumber = lockNo;
					mService.addLog(mcuCmd.mCmd, "(大值)读锁号成功" + mcuCmd.mLockerNumber);

					mService.readKey(mcuCmd.mLockerNumber, new OnReadKey() {

						@Override
						public void On(final String key, final String lockType, Exception e) {
							if (e == null) {
								mService.mDevice.PostRun(new Runnable() {

									@Override
									public void run() {
										byte cmd = 0;
										if (key.length() == 15) {
											cmd = 'o';
										} else {
											cmd = 'O';
										}

										//byte[] cmdBytes = Command.getInstance().AppSendLockerKey(cmd, mcuCmd.mLockerNumber, key, 1, DeviceService.mNormalLock);
										byte[] cmdBytes = Command.getInstance().new_AppSendLockerKey(cmd, lockNo, key, lockType, 1,mcuCmd.mUnlockType);	//
										mService.mDevice.Write(FileStream.write, cmdBytes);
										mIndex++;
									}
								});
							} else {
								// 异常处理
								mService.addLog((byte) 0, e.getMessage());
								mIndex = 0;
							}
						}
					});
				} else {
					throw new UnProcException(mcuCmd);
				}


			}
		});
		// 响应大值开锁结果
		this.mActionList.add(new RecvAction() {

			@Override
			public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

				if (mcuCmd.mCmd == 'S' || mcuCmd.mCmd == 's') {
					if (!mcuCmd.mErrCode.equals("00")) {
						mIndex = 0;// 从头开始执行
						throw new Exception("(大值)开锁失败");
					}
					// 开锁成功
					mService.addLog(mcuCmd.mCmd, "(大值)开锁成功");
					mService.mDevice.PostRun(new Runnable() {

						@Override
						public void run() {
							// 发送落锁命令
							//	byte[] cmdBytes = Command.getInstance().AppLockerReady();
							//	mService.mDevice.Write(FileStream.write, cmdBytes);
							mIndex++;
						}
					}, 2000);

				} else {
					throw new UnProcException(mcuCmd);
				}


			}
		});
		// 响应落锁结果
		this.mActionList.add(new RecvAction() {

			@Override
			public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

				if (mcuCmd.mCmd == 'C'||mcuCmd.mCmd =='c') {
					if (!mcuCmd.mErrCode.equals("00")) {
						mIndex = 0;// 从头开始执行
						if (mcuCmd.mAttechData[0] != 0) {
							mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
						}
						throw new Exception("(大值)关锁落锁失败");
					}
					mService.addLog(mcuCmd.mCmd, "(大值)关锁落锁成功");
//					if (mcuCmd.mAttechData[0] != 0) {
//						mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
//					}
					// 发关锁命令
					//byte[] cmdBytes = Command.getInstance().AppSendClose(MCUCommand.OPCODE_CLOSE_LOCK);
					byte[] cmdBytes = Command.getInstance().AppSendClose();
					mService.mDevice.Write(FileStream.write, cmdBytes);
					mIndex++;
					// 由于落锁指令只有一个'F',没有办法区分是关锁还是开锁，所以只能收到后这里修改，后台显示状态才正确
					//mcuCmd.mCmd = 'f';
				} else {
					throw new UnProcException(mcuCmd);
				}

			}
		});
		// 响应关锁
		this.mActionList.add(new RecvAction() {

			@Override
			public void Do(final MCUCommand mcuCmd, byte[] data) throws Exception {}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

				if (mcuCmd.mCmd == 'T') {
					if (!mcuCmd.mErrCode.equals("00")) {
						mIndex = 0;// 从头开始执行
						mService.IncTestTime(false);
						throw new Exception("(大值)关锁失败");
					}
					mService.addLog(mcuCmd.mCmd, "(大值)关锁成功");
					mService.setBigOpen(2);
					// 发送落锁命令
					//simulatorOpenReady();
					mService.addLog((byte)0, mService.getString(R.string.bigsmall_unlock));

					mIndex++;
					mIndex++;
					mService.IncTestTime(true);
				} else {
					throw new UnProcException(mcuCmd);
				}

			}
		});
		// 响应小值落锁结果
		this.mActionList.add(new RecvAction() {

			@Override
			public void Do(final MCUCommand mcuCmd, byte[] data) throws Exception {}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

				if (mcuCmd.mCmd == 'F') {
					if (!mcuCmd.mErrCode.equals("00")) {
						mIndex = 0;// 从头开始执行
						if (mcuCmd.mAttechData[0] != 0) {
							mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
						}
						throw new Exception("(小值)开锁落锁失败");
					}
					mService.addLog(mcuCmd.mCmd, "(小值)开锁落锁成功");
					if (mcuCmd.mAttechData[0] != 0) {
						mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
					}
					byte[] cmdBytes = Command.getInstance().AppReadLockerNunber();
					mService.mDevice.Write(FileStream.write, cmdBytes);
					mIndex++;
				} else {
					throw new UnProcException(mcuCmd);
				}
				mIndex++;
			}
		});
		// 小值读锁号
		this.mActionList.add(new RecvAction() {
			@Override
			public void Do(final MCUCommand mcuCmd, byte[] data) throws Exception {}

			@Override
			public void Do(final MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

				if (mcuCmd.mCmd == 'O' || mcuCmd.mCmd == 'o') {
					if (!mcuCmd.mErrCode.equals("00")) {
						mIndex = 0;// 从头开始执行
						throw new Exception("(小值)读锁号失败");
					}
					int offset=0;
					int openlock_type=0;		//正负开锁,0原值,1正负,2正负,3正负,4正负
					final String lockNo =  data.substring(offset + 5, offset + 14);
					mcuCmd.mLockerNumber = lockNo;
					mService.addLog(mcuCmd.mCmd, "(小值)读锁号成功" + mcuCmd.mLockerNumber);
					// 小值开锁
					mService.readKey(mcuCmd.mLockerNumber, new OnReadKey() {

						@Override
						public void On(final String key,final String lockType, Exception e) {
							if (e == null) {
								mService.mDevice.PostRun(new Runnable() {

									@Override
									public void run() {
										byte cmd = 0;
										if (key.length() == 15) {
											cmd = 'o';
										} else {
											cmd = 'O';
										}
										//byte[] cmdBytes = Command.getInstance().AppSendLockerKey(cmd, mcuCmd.mLockerNumber, key, 2, DeviceService.mNormalLock);
										byte[] cmdBytes = Command.getInstance().new_AppSendLockerKey(cmd, lockNo, key, lockType, 2,mcuCmd.mUnlockType);
										mService.mDevice.Write(FileStream.write, cmdBytes);
										mIndex++;
									}
								});
							} else {
								mService.addLog(mcuCmd.mCmd, "(小值)读密码失败");
								mIndex = 0;
							}
						}
					});
				} else {
					throw new UnProcException(mcuCmd);
				}

			}

		});

		// 响应小值开锁结果
		this.mActionList.add(new RecvAction() {

			@Override
			public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

				if (mcuCmd.mCmd == 's' || mcuCmd.mCmd == 'S') {
					if (!mcuCmd.mErrCode.equals("00")) {
						mIndex = 0;// 从头开始执行
						throw new Exception("(小值)开锁失败");
					}
					// 开锁成功
					mService.addLog(mcuCmd.mCmd, "(小值)开锁成功");
					mService.mDevice.PostRun(new Runnable() {

						@Override
						public void run() {
							// 发送落锁命令
//							simulatorOpenReady();
							mIndex++;
						}
					}, 2000);
				} else {
					throw new UnProcException(mcuCmd);
				}

			}
		});
		// 响应落锁结果
		this.mActionList.add(new RecvAction() {

			@Override
			public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

				if (mcuCmd.mCmd == 'C'||mcuCmd.mCmd=='c') {
					if (!mcuCmd.mErrCode.equals("00")) {
						mIndex = 0;// 从头开始执行
//						if (mcuCmd.mAttechData[0] != 0) {
//							mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
//						}
						throw new Exception("(小值)关锁落锁失败");
					}
					mService.addLog(mcuCmd.mCmd, "(小值)关锁落锁成功");
//					if (mcuCmd.mAttechData[0] != 0) {
//						mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
//					}
					// 发关锁命令
					//byte[] cmdBytes = Command.getInstance().AppSendClose(MCUCommand.OPCODE_CLOSE_LOCK);
					byte[] cmdBytes = Command.getInstance().AppSendClose();
					mService.mDevice.Write(FileStream.write, cmdBytes);
					mIndex++;
					// 由于落锁指令只有一个'F',没有办法区分是关锁还是开锁，所以只能收到 后这里修改，后台显示状态才正确
					//mcuCmd.mCmd = 'f';
				} else {
					throw new UnProcException(mcuCmd);
				}

			}

		});
		// 响应关锁
		this.mActionList.add(new RecvAction() {

			@Override
			public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

				if (mcuCmd.mCmd == 'T'||mcuCmd.mCmd=='t') {
					if (!mcuCmd.mErrCode.equals("00")) {
						mIndex = 0;// 从头开始执行
						mService.IncTestTime(false);
						throw new Exception("(小值)关锁失败");
					}
					if (mService.getBigOpen() == 2) {
						mService.addLog(mcuCmd.mCmd, "(小值)关锁成功");
					} else {
						mService.addLog(mcuCmd.mCmd, "关锁成功");
					}
					mIndex = 0;
					mService.IncTestTime(true);
					switch (mService.getAutoTest()) {
						case LockSetActivity.unlockAutoReconnectTwo:
						case LockSetActivity.unlockContinue: {
							if (mContinueTest) {
								simulatorOpenReady();
							}
						}
						break;
					}
				} else {
					throw new UnProcException(mcuCmd);
				}

			}
		});
	}

	// [[cxq
	// 添加一个正负3 正负4开锁

	BigSmallChain(DeviceService service, final String str_LockType) {
		super(service);
		this.mService = service;
		// 响应开锁落锁成功
		this.mActionList.add(new RecvAction() {

			@Override
			public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

				mService.setBigOpen(1);
				switch (mcuCmd.mCmd) {
					case MCUCommand.OPCODE_WAIT_FALL_IN_UNLOCK: {
						// [[wk
						// must clear
						MainActivity.state_sleep = null;
						// ]]
						mContinueTest = true;
						if (!mcuCmd.mErrCode.equals("00")) {
							mIndex = 0;// 从头开始执行
							if (mcuCmd.mAttechData[0] != 0) {
								mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
							}
							throw new Exception("(大值)开锁落锁失败");
						}
						mService.addLog(mcuCmd.mCmd, "(大值)开锁落锁成功");
						if (mcuCmd.mAttechData[0] != 0) {
							mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
						}
						byte[] cmdBytes = Command.getInstance()
								.AppReadLockerNunber();
						mService.mDevice.Write(FileStream.write, cmdBytes);
						mIndex++;
					}
					break;
					case MCUCommand.OPCODE_WAIT_FALL_IN_LOCK: {
						// [[wk
						// must clear
						MainActivity.state_sleep = null;
						// ]]
						mContinueTest = false;
						if (!mcuCmd.mErrCode.equals("00")) {
							mIndex = 0;// 从头开始执行
							if (mcuCmd.mAttechData[0] != 0) {
								mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
							}
							throw new Exception("关锁锁落锁失败");
						}
						mService.addLog(mcuCmd.mCmd, "关锁落锁成功");
						if (mcuCmd.mAttechData[0] != 0) {
							mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
						}
						byte[] cmdBytes = Command.getInstance().AppSendClose(
								MCUCommand.OPCODE_CLOSE_LOCK);
						mService.mDevice.Write(FileStream.write, cmdBytes);
						mIndex = 9;
					}
					break;
					// [[wk
					case MCUCommand.opcode_sleep: {
						if (!mcuCmd.mErrCode.equals("00")) {
							mIndex = 0;
							throw new Exception("掌机休眠失败!");
						}
						MainActivity.state_sleep = MainActivity.SLEEP;
						mService.addLog(mcuCmd.mCmd,
								"掌机进入休眠状态,按下开/关锁键可唤醒掌机后再进行其他操作!");
						mIndex = 0;
					}
					break;
					// ]]
					default: {
						mIndex = 0;
						throw new UnProcException(mcuCmd);
					}
				}


			}
		});
		// 响应锁号
		this.mActionList.add(new RecvAction() {

			@Override
			public void Do(final MCUCommand mcuCmd, byte[] data) throws Exception {}

			@Override
			public void Do(final MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

				if (mcuCmd.mCmd == 'S') {
					if (!mcuCmd.mErrCode.equals("00")) {
						mIndex = 0;// 从头开始执行
						throw new Exception("(大值)读锁号失败");
					}
					mService.addLog(mcuCmd.mCmd, "(大值)读锁号成功" + mcuCmd.mLockerNumber);
					mService.readKey(mcuCmd.mLockerNumber, new OnReadKey() {

						@Override
						public void On(final String key,final String lockType, Exception e) {
							if (e == null) {
								mService.mDevice.PostRun(new Runnable() {

									@Override
									public void run() {
										byte cmd = 0;
										if (key.length() == 15) {
											cmd = 'o';
										} else {
											cmd = 'O';
										}
										// [[CXQ
										// byte[] by_Array =
										// fileStream.fileStream(FileStream.unlockType,
										// FileStream.read, null);
										// int i = by_Array.length;
										// ]]
										// 正负3，正负4开关锁
										if ((LockSetActivity.unlockThree)
												.equals(str_LockType)) {
											byte[] cmdBytes = Command
													.getInstance()
													.AppSendLockerKey(
															cmd,
															mcuCmd.mLockerNumber,
															key, 1, 3);
											mService.mDevice.Write(
													FileStream.write, cmdBytes);
										} else if ((LockSetActivity.unlockFour)
												.equals(str_LockType)) {
											byte[] cmdBytes = Command
													.getInstance()
													.AppSendLockerKey(
															cmd,
															mcuCmd.mLockerNumber,
															key, 1, 4);
											mService.mDevice.Write(
													FileStream.write, cmdBytes);
										}

										mIndex++;
									}
								});
							} else {
								// 异常处理
								mService.addLog((byte) 0, e.getMessage());
								mIndex = 0;
							}
						}
					});
				} else {
					throw new UnProcException(mcuCmd);
				}

			}
		});
		// 响应大值开锁结果
		this.mActionList.add(new RecvAction() {

			@Override
			public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

				if (mcuCmd.mCmd == 'o' || mcuCmd.mCmd == 'O') {
					if (!mcuCmd.mErrCode.equals("00")) {
						mIndex = 0;// 从头开始执行
						throw new Exception("(大值)开锁失败");
					}
					// 开锁成功
					mService.addLog(mcuCmd.mCmd, "(大值)开锁成功");
					mService.mDevice.PostRun(new Runnable() {

						@Override
						public void run() {
							// 发送落锁命令
							byte[] cmdBytes = Command.getInstance()
									.AppLockerReady();
							mService.mDevice.Write(FileStream.write, cmdBytes);
							mIndex++;
						}
					}, 2000);

				} else {
					throw new UnProcException(mcuCmd);
				}

			}
		});
		// 响应落锁结果
		this.mActionList.add(new RecvAction() {

			@Override
			public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

				if (mcuCmd.mCmd == 'F') {
					if (!mcuCmd.mErrCode.equals("00")) {
						mIndex = 0;// 从头开始执行
						if (mcuCmd.mAttechData[0] != 0) {
							mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
						}
						throw new Exception("(大值)关锁落锁失败");
					}
					mService.addLog(mcuCmd.mCmd, "(大值)关锁落锁成功");
					if (mcuCmd.mAttechData[0] != 0) {
						mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
					}
					// 发关锁命令
					byte[] cmdBytes = Command.getInstance().AppSendClose(
							MCUCommand.OPCODE_CLOSE_LOCK);
					mService.mDevice.Write(FileStream.write, cmdBytes);
					mIndex++;
					// 由于落锁指令只有一个'F',没有办法区分是关锁还是开锁，所以只能收到后这里修改，后台显示状态才正确
					mcuCmd.mCmd = 'f';
				} else {
					throw new UnProcException(mcuCmd);
				}

			}
		});
		// 响应关锁
		this.mActionList.add(new RecvAction() {

			@Override
			public void Do(final MCUCommand mcuCmd, byte[] data) throws Exception {}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

				if (mcuCmd.mCmd == 'C') {
					if (!mcuCmd.mErrCode.equals("00")) {
						mIndex = 0;// 从头开始执行
						mService.IncTestTime(false);
						throw new Exception("(大值)关锁失败");
					}
					mService.addLog(mcuCmd.mCmd, "(大值)关锁成功");
					mService.setBigOpen(2);
					// 发送落锁命令
					simulatorOpenReady();
					mIndex++;
					mService.IncTestTime(true);
				} else {
					throw new UnProcException(mcuCmd);
				}

			}
		});
		// 响应小值落锁结果
		this.mActionList.add(new RecvAction() {

			@Override
			public void Do(final MCUCommand mcuCmd, byte[] data) throws Exception {}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

				if (mcuCmd.mCmd == 'F') {
					if (!mcuCmd.mErrCode.equals("00")) {
						mIndex = 0;// 从头开始执行
						if (mcuCmd.mAttechData[0] != 0) {
							mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
						}
						throw new Exception("(小值)开锁落锁失败");
					}
					mService.addLog(mcuCmd.mCmd, "(小值)开锁落锁成功");
					if (mcuCmd.mAttechData[0] != 0) {
						mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
					}
					byte[] cmdBytes = Command.getInstance()
							.AppReadLockerNunber();
					mService.mDevice.Write(FileStream.write, cmdBytes);
					mIndex++;
				} else {
					throw new UnProcException(mcuCmd);
				}

			}
		});
		// 小值读锁号
		this.mActionList.add(new RecvAction() {
			@Override
			public void Do(final MCUCommand mcuCmd, byte[] data) throws Exception {}

			@Override
			public void Do(final MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

				if (mcuCmd.mCmd == 'S') {
					if (!mcuCmd.mErrCode.equals("00")) {
						mIndex = 0;// 从头开始执行
						throw new Exception("(小值)读锁号失败");
					}
					mService.addLog(mcuCmd.mCmd, "(小值)读锁号成功" + mcuCmd.mLockerNumber);
					// 小值开锁
					mService.readKey(mcuCmd.mLockerNumber, new OnReadKey() {

						@Override
						public void On(final String key, final String lockType, Exception e) {
							if (e == null) {
								mService.mDevice.PostRun(new Runnable() {

									@Override
									public void run() {
										byte cmd = 0;
										if (key.length() == 15) {
											cmd = 'o';
										} else {
											cmd = 'O';
										}

										// 正负3，正负4 小值开关锁
										if ((LockSetActivity.unlockThree)
												.equals(str_LockType)) {
											byte[] cmdBytes = Command
													.getInstance()
													.AppSendLockerKey(
															cmd,
															mcuCmd.mLockerNumber,
															key, 2, 3);
											mService.mDevice.Write(
													FileStream.write, cmdBytes);
										} else if ((LockSetActivity.unlockFour)
												.equals(str_LockType)) {
											byte[] cmdBytes = Command
													.getInstance()
													.AppSendLockerKey(
															cmd,
															mcuCmd.mLockerNumber,
															key, 2, 4);
											mService.mDevice.Write(
													FileStream.write, cmdBytes);
										}

										mIndex++;
									}
								});
							} else {
								mService.addLog(mcuCmd.mCmd, "(小值)读密码失败");
								mIndex = 0;
							}
						}
					});
				} else {
					throw new UnProcException(mcuCmd);
				}

			}
		});
		// 响应小值开锁结果
		this.mActionList.add(new RecvAction() {

			@Override
			public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

				if (mcuCmd.mCmd == 'o' || mcuCmd.mCmd == 'O') {
					if (!mcuCmd.mErrCode.equals("00")) {
						mIndex = 0;// 从头开始执行
						throw new Exception("(小值)开锁失败");
					}
					// 开锁成功
					mService.addLog(mcuCmd.mCmd, "(小值)开锁成功");
					mService.mDevice.PostRun(new Runnable() {

						@Override
						public void run() {
							// 发送落锁命令
							simulatorOpenReady();
							mIndex++;
						}
					}, 2000);
				} else {
					throw new UnProcException(mcuCmd);
				}

			}
		});
		// 响应落锁结果
		this.mActionList.add(new RecvAction() {

			@Override
			public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

				if (mcuCmd.mCmd == 'F') {
					if (!mcuCmd.mErrCode.equals("00")) {
						mIndex = 0;// 从头开始执行
						if (mcuCmd.mAttechData[0] != 0) {
							mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
						}
						throw new Exception("(小值)关锁落锁失败");
					}
					mService.addLog(mcuCmd.mCmd, "(小值)关锁落锁成功");
					if (mcuCmd.mAttechData[0] != 0) {
						mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
					}
					// 发关锁命令
					byte[] cmdBytes = Command.getInstance().AppSendClose(
							MCUCommand.OPCODE_CLOSE_LOCK);
					mService.mDevice.Write(FileStream.write, cmdBytes);
					mIndex++;
					// 由于落锁指令只有一个'F',没有办法区分是关锁还是开锁，所以只能收到 后这里修改，后台显示状态才正确
					mcuCmd.mCmd = 'f';
				} else {
					throw new UnProcException(mcuCmd);
				}

			}

		});
		// 响应关锁
		this.mActionList.add(new RecvAction() {

			@Override
			public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

				if (mcuCmd.mCmd == 'C') {
					if (!mcuCmd.mErrCode.equals("00")) {
						mIndex = 0;// 从头开始执行
						mService.IncTestTime(false);
						throw new Exception("(小值)关锁失败");
					}
					if (mService.getBigOpen() == 2) {
						mService.addLog(mcuCmd.mCmd, "(小值)关锁成功");
					} else {
						mService.addLog(mcuCmd.mCmd, "关锁成功");
					}
					mIndex = 0;
					mService.IncTestTime(true);
					switch (mService.getAutoTest()) {
						case LockSetActivity.unlockAutoReconnectTwo:
						case LockSetActivity.unlockContinue: {
							if (mContinueTest) {
								simulatorOpenReady();
							}
						}
						break;
					}
				} else {
					throw new UnProcException(mcuCmd);
				}

			}
		});
	}

	// ]]

	void autoTest() {
		switch (mService.getAutoTest()) {
			case LockSetActivity.unlockAutoReconnectTwo:
			case LockSetActivity.unlockContinue: {
				mIndex = 0;
				mService.mDevice.PostRun(new Runnable() {

					@Override
					public void run() {
						if (mContinueTest) {
							simulatorOpenReady();
						}
					}
				}, 1000);
			}
			break;
		}
	}

	void simulatorOpenReady() {
		mService.mDevice.PostRun(new Runnable() {

			@Override
			public void run() {
				byte[] cmdBytes = Command.getInstance().AppLockerReady();
				mService.mDevice.Write(FileStream.write, cmdBytes);
				mService.addLog((byte) 0, "simulatorOpenReady");
				mSimulatorOpenReady = true;
			}

		}, 2000);
	}

	@Override
	public void onConnect() {
		autoTest();
	}
}
