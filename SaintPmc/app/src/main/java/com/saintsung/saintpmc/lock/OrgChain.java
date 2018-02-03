package com.saintsung.saintpmc.lock;

import android.util.Log;

import com.saintsung.saintpmc.MainActivity;
import com.saintsung.saintpmc.lock.ChainBase;
import com.saintsung.saintpmc.lock.DeviceService.OnReadKey;
import com.saintsung.saintpmc.lock.DeviceService.RecvAction;

public class OrgChain extends ChainBase {

	boolean mOpen = true;

	OrgChain(DeviceService service) {
		super(service);
		service.setBigOpen(0);
		// 响应开关锁
		this.mActionList.add(new RecvAction() {

			@Override
			public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {
			}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

				switch (mcuCmd.mCmd) {
					case MCUCommand.OPCODE_WAIT_FALL_IN_UNLOCK: {
						// [[wk
						// must clear
						MainActivity.state_sleep = null;
						// ]]
						if (!mcuCmd.mErrCode.equals("00")) {
							mIndex = 1;
							if (mcuCmd.mAttechData[0] != 0) {
								mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
							}
							throw new Exception("开锁落锁失败");
						}
						byte[] cmdBytes = Command.getInstance().AppReadLockerNunber();
						mService.mDevice.Write(FileStream.write, cmdBytes);
						mOpen = true;
						mService.addLog(mcuCmd.mCmd, "开锁落锁成功");
						if (mcuCmd.mAttechData[0] != 0) {
							mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
						}
						mIndex++;
						// Handler handler=new Handler();
						// handler.postDelayed(null, 200);
					}
					break;
					case MCUCommand.OPCODE_WAIT_FALL_IN_LOCK: {
						// [[wk
						// must clear
						MainActivity.state_sleep = null;
						// ]]
						if (!mcuCmd.mErrCode.equals("00")) {
							mIndex = 1;
							if (mcuCmd.mAttechData[0] != 0) {
								mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
							}
							throw new Exception("关锁落锁失败");
						}
						byte[] cmdBytes = Command.getInstance().AppSendClose(MCUCommand.OPCODE_CLOSE_LOCK);
						mService.mDevice.Write(FileStream.write, cmdBytes);
						mService.addLog(mcuCmd.mCmd, "关锁落锁成功");
						if (mcuCmd.mAttechData[0] != 0) {
							mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
						}
						mIndex++;
					}
					break;
					case MCUCommand.opcode_sleep: {
						if (!mcuCmd.mErrCode.equals("00")) {
							mIndex = 1;
							throw new Exception("掌机休眠失败!");
						}
						MainActivity.state_sleep = MainActivity.SLEEP;
						mService.addLog(mcuCmd.mCmd, "掌机进入休眠状态,按下开/关锁键可唤醒掌机后再进行其他操作!");
						mIndex = 1;
					}
					break;
					default: {
						mIndex = 1;
					}
				}

			}

		});
		// 响应读锁号
		this.mActionList.add(new RecvAction() {

			@Override
			public void Do(final MCUCommand mcuCmd, byte[] data) throws Exception {
				Log.d("fasdfa", "fasdfa");
			}

			@Override
			public void Do(final MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

				switch (mcuCmd.mCmd) {
					case 'C': {
						if (!mcuCmd.mErrCode.equals("00")) {
							mIndex = 1;
							throw new Exception("关锁失败");
						}

						mService.addLog(mcuCmd.mCmd, "关锁落锁成功");
						// 发关锁命令
						//byte[] cmdBytes = Command.getInstance().AppSendClose(MCUCommand.OPCODE_CLOSE_LOCK);
						byte[] cmdBytes = Command.getInstance().AppSendClose();
						mService.mDevice.Write(FileStream.write, cmdBytes);
						//mIndex = 1;
					}
					break;
					case 'O': {
						if (!mcuCmd.mErrCode.equals("00")) {
							mIndex = 1;
							throw new Exception("读锁号失败");
						}
						int offset = 0;
						int openlock_type = 0; // 正负开锁,0原值,1正负,2正负,3正负,4正负
						final String lockNo = data.substring(offset + 5, offset + 14);
						mcuCmd.mLockerNumber = lockNo;
						mService.addLog((byte)0, "读锁号:" + mcuCmd.mLockerNumber + "成功");
						if (mOpen) {
							mService.readKey(mcuCmd.mLockerNumber, new OnReadKey() {

								@Override
								public void On(final String key, final String lockType, Exception e) {
									if (e == null) {
										byte cmd = 0;
										if (key.length() == 15) {
											cmd = 'o';
										} else {
											cmd = 'O';
										}
										// byte[] cmdBytes =
										// Command.getInstance().AppSendLockerKey(cmd,
										// mcuCmd.mLockerNumber, key, null, 0);
										byte[] cmdBytes = Command.getInstance().new_AppSendLockerKey(cmd, mcuCmd.mLockerNumber, key, lockType, 0, 0);
										mService.mDevice.Write(FileStream.write, cmdBytes);
										// mIndex++;
									} else {
										// 异常处理
										mService.addLog((byte) 0, e.getMessage());
										mIndex = 1;
									}
								}

							});
						}
					}
					case 'S':
						if (mcuCmd.mCmd == 'S' || mcuCmd.mCmd == 's') {
							if (!mcuCmd.mErrCode.equals("00")) {
								mIndex = 1;// 从头开始执行
								throw new Exception("开锁失败");
							}
							// 开锁成功
							mService.addLog(mcuCmd.mCmd, "开锁成功");
						}

						break;
					case 'T':
						if (mcuCmd.mCmd == 'T') {
							if (!mcuCmd.mErrCode.equals("00")) {
								mIndex = 1;// 从头开始执行
								mService.IncTestTime(false);
								throw new Exception("关锁失败");
							}
							mService.addLog(mcuCmd.mCmd, "关锁成功");
						}
						break;
					default: {
						//	mIndex = 1;
					}
					break;
				}

			}

		});
		// 响应开关锁
		this.mActionList.add(new RecvAction() {

			@Override
			public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {
			}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

				switch (mcuCmd.mCmd) {
					case 'o':
					case 'O': {
						if (!mcuCmd.mErrCode.equals("00")) {
							mIndex = 1;
							throw new Exception("开锁失败");
						}
						mService.addLog(mcuCmd.mCmd, "开锁成功");
						mIndex = 1;
					}
					break;
					default: {
						mIndex = 0;
					}
				}
				mService.IncTestTime(true);

			}

		});
	}

}
