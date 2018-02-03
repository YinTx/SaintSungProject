package com.saintsung.saintpmc.lock;

import com.saintsung.saintpmc.MainActivity;
import com.saintsung.saintpmc.lock.DeviceService.OnReadKey;
import com.saintsung.saintpmc.lock.DeviceService.RecvAction;

import android.text.format.Time;
import android.util.Log;

public class UnlockScrew extends ChainBase {

	boolean mOpen = false;
	private final int cmd_index = 4;

	UnlockScrew(DeviceService service) {
		super(service);
		service.setBigOpen(0);

		// 响应开/关螺丝
		this.mActionList.add(new RecvAction() {
			@Override
			public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {
				switch (mcuCmd.mCmd) {
					case SetAllActivity.OPCODE_SET_PARAM: {
						Log.d("Set", "Set" + "UnlockScrew");
						// must clear
						MainActivity.handLockNumber = null;
						if (!mcuCmd.mErrCode.equals("00")) {
							mIndex = 0;
							// throw new Exception("掌机切换为开螺丝模式失败!");
						}
						// mService.addLog(mcuCmd.mCmd,"掌机切换为开螺丝模式成功!");
						mIndex = 0;
					}
					break;
					case MCUCommand.OPCODE_OPEN_SCREW_BEFORE: {
						// wake up
						MainActivity.state_sleep = null;
						if (!mcuCmd.mErrCode.equals("00")) {
							mIndex = 0;
							if (mcuCmd.mAttechData[0] != 0) {
								mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
							}
							throw new Exception(MCUCommand.open_screw_prepare_false);
						} else {
							if (MainActivity.handLockNumber != null) {
								// mService.addLog((byte)0, "正在获取开螺丝密码,请稍后...");
								// get lock password
								mService.readKey(MainActivity.handLockNumber, new OnReadKey() {
									@Override
									public void On(final String key, final String lockType, Exception e) {
										if (e == null) {
											byte cmd = 0;
											if (key.length() == 15) {
												cmd = MCUCommand.opcode_open_screw;
											} else {
												cmd = MCUCommand.OPCODE_OPEN_LOCK;
											}
											byte[] cmdBytes = Command.getInstance().AppSendLockerKey(cmd, MainActivity.handLockNumber, key, 0, DeviceService.mScrewLock);
											mService.mDevice.Write(FileStream.write, cmdBytes);
										}
									}
								});
							} else {
								mService.addLog(mcuCmd.mCmd, MCUCommand.open_screw_prepare);
								if (mcuCmd.mAttechData[0] != 0) {
									mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
								}
							}
							if (mIndex == 1) {
								mIndex = 0;
							} else {
								mIndex++;
							}
						}
					}
					break;
					case MCUCommand.OPCODE_CLOSE_SCREW_BEFORE: {
						// wake up
						MainActivity.state_sleep = null;
						if (!mcuCmd.mErrCode.equals("00")) {
							mIndex = 0;
							if (mcuCmd.mAttechData[0] != 0) {
								mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
							}
							throw new Exception(MCUCommand.close_screw_prepare_false);
						} else {
							if (MainActivity.handLockNumber != null) {
								// get lock password
								mService.readKey(MainActivity.handLockNumber, new OnReadKey() {
									@Override
									public void On(final String key, final String lockType, Exception e) {
										if (e == null) {
											// send close screw
											byte[] cmdBytes = Command.getInstance().AppSendClose(MCUCommand.opcode_close_screw);
											mService.mDevice.Write(FileStream.write, cmdBytes);
										}
									}
								});
							} else {
								mService.addLog(mcuCmd.mCmd, MCUCommand.close_screw_prepare);
								if (mcuCmd.mAttechData[0] != 0) {
									mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
								}
							}
							if (mIndex == 1) {
								mIndex = 0;
							} else {
								mIndex++;
							}
						}
					}
					break;
					case MCUCommand.OPCODE_WAIT_FALL_IN_UNLOCK: {
						// must clear
						MainActivity.handLockNumber = null;
						// wake up
						MainActivity.state_sleep = null;
						if (!mcuCmd.mErrCode.equals("00")) {
							mIndex = 0;
							if (mcuCmd.mAttechData[0] != 0) {
								mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
							}
							// throw new Exception("落螺丝失败(开)!");
							throw new Exception("落锁失败(开),掌机固件未升级!");
						}
						mIndex = 0;
						mService.addLog(mcuCmd.mCmd, "落锁成功(开),掌机固件未升级!");
						if (mcuCmd.mAttechData[0] != 0) {
							mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
						}
					}
					break;
					case MCUCommand.OPCODE_WAIT_FALL_IN_LOCK: {
						// wake up
						MainActivity.state_sleep = null;
						if (!mcuCmd.mErrCode.equals("00")) {
							mIndex = 0;
							if (mcuCmd.mAttechData[0] != 0) {
								mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
							}
							// throw new Exception("落螺丝失败(关)!");
							throw new Exception("落锁失败(关),掌机固件未升级!");
						}
						mIndex = 0;
						if (mcuCmd.mAttechData[0] != 0) {
							mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
						}
						mService.addLog(mcuCmd.mCmd, "落锁成功(关),掌机固件未升级!");
					}
					break;
					case MCUCommand.opcode_sleep: {
						if (!mcuCmd.mErrCode.equals("00")) {
							mIndex = 0;
							throw new Exception("掌机休眠失败!");
						}
						MainActivity.state_sleep = MainActivity.SLEEP;
						mService.addLog(mcuCmd.mCmd, "掌机进入休眠状态,按开/关键唤醒掌机后再进行其他操作!");
						mIndex = 0;
					}
					break;
					default: {
						// wake up
						MainActivity.state_sleep = null;
						MainActivity.handLockNumber = null;
						// mService.addLog(mcuCmd.mCmd,mcuCmd.mCmd+"待分析指令:"+new
						// String(data));
						// mService.addLog(mcuCmd.mCmd,"中断操作,选择<手动开锁>后,可重新开始!");
						mIndex = 0;
					}
				}
			}

			// 处理开关螺丝
			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub
				Log.d("screw", "screw11111");
				// 文件
				FileStream fileStream = null;
				fileStream = new FileStream();
				// 记录
				String str_log = "";
				String str_UserInfo = "";
				Time time = new Time();
				time.setToNow();
				String str_time = String.format("%02d", time.year) + String.format("%02d", time.month + 1) + String.format("%02d", time.monthDay) + String.format("%02d", time.hour) + String.format("%02d", time.minute) + String.format("%02d", time.second);
				String str_DeviceID = new String(fileStream.fileStream(FileStream.deviceFile, FileStream.read, null));
				str_DeviceID = str_DeviceID.substring(0, 18);

				byte[] byteArray = fileStream.fileStream(FileStream.userLogin, FileStream.read, null);
				str_UserInfo = new String(byteArray);
				String[] stringArray = str_UserInfo.split(",");
				str_UserInfo = stringArray[2];

				//				String cmd = data.substring(cmd_index, cmd_index + 1);
				String state = "";

				switch (data.charAt(cmd_index)) {
					case MCUCommand.App_Unlock_Status:
						state = data.substring(cmd_index + 1, cmd_index + 3);
						if (state != null && state.length() > 0 && state.equals(MCUCommand.ERRCODE_SUCC)) {
							mService.addLog((byte) 0x00, "开启成功: ");
							DeviceService.unlockScrew = "Close";
							mIndex++;

							str_log = str_DeviceID + str_UserInfo + DeviceService.ScrewNum + str_time + 1 + "\r\n";
							if (str_log != null && str_log.length() == 60) {
								fileStream.fileStream(FileStream.screw_log, FileStream.write, str_log.getBytes());
							}

							// 上传开启成功记录到服务器
							mService.uploadLog(MCUCommand.operateunlock_succ, MainActivity.handLockNumber, null);

						} else {
							mService.addLog((byte) 0x00, "开启失败: ");
							str_log = str_DeviceID + str_UserInfo + DeviceService.ScrewNum + str_time + 2 + "\r\n";
							if (str_log != null && str_log.length() == 60) {
								fileStream.fileStream(FileStream.screw_log, FileStream.write, str_log.getBytes());
							}
							// 上传开启成功记录到服务器
							mService.uploadLog(MCUCommand.operateunlock_fail, MainActivity.handLockNumber, null);
						}

						break;
					case MCUCommand.AppLock:
						state = data.substring(cmd_index + 1, cmd_index + 3);
						if (state != null && state.length() > 0 && state.equals(MCUCommand.ERRCODE_SUCC)) {
							mService.addLog((byte) 0x00, "关闭成功: ");
							DeviceService.unlockScrew = "Open";
							mIndex++;
							str_log = str_DeviceID + str_UserInfo + DeviceService.ScrewNum + str_time + 0 + "\r\n";
							if (str_log != null && str_log.length() == 60) {
								fileStream.fileStream(FileStream.screw_log, FileStream.write, str_log.getBytes());
							}
							// 上传关闭成功记录到服务器
							mService.uploadLog(MCUCommand.operatelock_succ, MainActivity.handLockNumber, null);
						} else {
							mService.addLog((byte) 0x00, "关闭失败: ");
							str_log = str_DeviceID + str_UserInfo + DeviceService.ScrewNum + str_time + 3 + "\r\n";
							if (str_log != null && str_log.length() == 60) {
								fileStream.fileStream(FileStream.screw_log, FileStream.write, str_log.getBytes());
							}
							// 上传关闭成功记录到服务器
							mService.uploadLog(MCUCommand.operatelock_fail, MainActivity.handLockNumber, null);
						}

						break;
					default:
						mIndex = 0;
						break;
				}

			}
		});
		// 响应开/关螺丝
		this.mActionList.add(new RecvAction() {
			@Override
			public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {
				switch (mcuCmd.mCmd) {
					case MCUCommand.OPCODE_OPEN_SCREW_BEFORE: {
						// wake up
						MainActivity.state_sleep = null;
						if (!mcuCmd.mErrCode.equals("00")) {
							mIndex = 0;
							if (mcuCmd.mAttechData[0] != 0) {
								mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
							}
							throw new Exception(MCUCommand.open_screw_prepare_false);
						} else {
							if (MainActivity.handLockNumber != null) {
								// get lock password
								mService.readKey(MainActivity.handLockNumber, new OnReadKey() {
									@Override
									public void On(final String key, final String lockType, Exception e) {
										if (e == null) {
											byte cmd = 0;
											if (key.length() == 15) {
												cmd = MCUCommand.opcode_open_screw;
											} else {
												cmd = MCUCommand.OPCODE_OPEN_LOCK;
											}
											byte[] cmdBytes = Command.getInstance().AppSendLockerKey(cmd, MainActivity.handLockNumber, key, 0, DeviceService.mScrewLock);
											mService.mDevice.Write(FileStream.write, cmdBytes);
										}
									}
								});
							} else {
								mService.addLog(mcuCmd.mCmd, MCUCommand.open_screw_prepare);
								if (mcuCmd.mAttechData[0] != 0) {
									mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
								}
							}
							if (mIndex == 1) {
								mIndex = 0;
							} else {
								mIndex++;
							}
						}
					}
					break;
					case MCUCommand.OPCODE_CLOSE_SCREW_BEFORE: {
						// wake up
						MainActivity.state_sleep = null;
						if (!mcuCmd.mErrCode.equals("00")) {
							mIndex = 0;
							if (mcuCmd.mAttechData[0] != 0) {
								mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
							}
							throw new Exception(MCUCommand.close_screw_prepare_false);
						} else {
							if (MainActivity.handLockNumber != null) {
								// get lock password
								mService.readKey(MainActivity.handLockNumber, new OnReadKey() {
									@Override
									public void On(final String key, final String lockType, Exception e) {
										if (e == null) {
											// send close screw
											byte[] cmdBytes = Command.getInstance().AppSendClose(MCUCommand.opcode_close_screw);
											mService.mDevice.Write(FileStream.write, cmdBytes);
										}
									}
								});
							} else {
								mService.addLog(mcuCmd.mCmd, MCUCommand.close_screw_prepare);
								if (mcuCmd.mAttechData[0] != 0) {
									mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
								}
							}
							if (mIndex == 1) {
								mIndex = 0;
							} else {
								mIndex++;
							}
						}
					}
					break;
					case MCUCommand.OPCODE_WAIT_FALL_IN_UNLOCK: {
						// must clear
						MainActivity.handLockNumber = null;
						// wake up
						MainActivity.state_sleep = null;
						if (!mcuCmd.mErrCode.equals("00")) {
							mIndex = 0;
							if (mcuCmd.mAttechData[0] != 0) {
								mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
							}
							// throw new Exception("落螺丝失败(开)!");
							throw new Exception("落锁失败(开),掌机固件未升级!");
						}
						mIndex = 0;
						if (mcuCmd.mAttechData[0] != 0) {
							mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
						}
						mService.addLog(mcuCmd.mCmd, "落锁成功(开),掌机固件未升级!");
					}
					break;
					case MCUCommand.OPCODE_WAIT_FALL_IN_LOCK: {
						// wake up
						MainActivity.state_sleep = null;
						if (!mcuCmd.mErrCode.equals("00")) {
							mIndex = 0;
							if (mcuCmd.mAttechData[0] != 0) {
								mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
							}
							// throw new Exception("落螺丝失败(关)!");
							throw new Exception("落锁失败(关),掌机固件未升级!");
						}
						//
						mIndex = 0;
						if (mcuCmd.mAttechData[0] != 0) {
							mService.addLog(mcuCmd.mCmd, MCUCommand.warn_charge);
						}
						mService.addLog(mcuCmd.mCmd, "落锁成功(关),掌机固件未升级!");
					}
					break;
					case MCUCommand.opcode_open_screw:
					case MCUCommand.OPCODE_OPEN_LOCK:
					case MCUCommand.OPCODE_OPEN_LOCK15: {
						mIndex = 0;
						if (!mcuCmd.mErrCode.equals("00")) {
							// must clear
							MainActivity.handLockNumber = null;
							throw new Exception(MCUCommand.open_screw_false);
						} else {
							mService.addLog(mcuCmd.mCmd, MCUCommand.open_screw_success);
							// 上传手动开螺丝记录到服务器
							mService.uploadLog(MCUCommand.operatestate_unlock, MainActivity.handLockNumber, null);
							// after upload operate log must clear handLockNumber
							MainActivity.handLockNumber = null;
						}
					}
					break;
					case MCUCommand.opcode_close_screw:
					case MCUCommand.OPCODE_CLOSE_LOCK: {
						mIndex = 0;
						if (!mcuCmd.mErrCode.equals("00")) {
							throw new Exception(MCUCommand.close_screw_false);
						} else {
							mService.addLog(mcuCmd.mCmd, MCUCommand.close_screw_success);
							// 上传关螺丝记录到服务器
							mService.uploadLog(MCUCommand.operatestate_lock, MainActivity.handLockNumber, null);
							// after upload operate log must clear handLockNumber
							MainActivity.handLockNumber = null;
						}
					}
					break;
					case MCUCommand.opcode_sleep: {
						if (!mcuCmd.mErrCode.equals("00")) {
							mIndex = 0;
							throw new Exception("掌机休眠失败!");
						}
						MainActivity.state_sleep = MainActivity.SLEEP;
						mService.addLog(mcuCmd.mCmd, "掌机进入休眠状态,按开/关键唤醒掌机后再进行其他操作!");
						mIndex = 0;
					}
					break;
					default: {
						// wake up
						MainActivity.state_sleep = null;
						MainActivity.handLockNumber = null;
						// mService.addLog(mcuCmd.mCmd,mcuCmd.mCmd+"待分析指令:"+new
						// String(data));
						// mService.addLog(mcuCmd.mCmd,"中断操作,选择<手动开锁>后,可重新开始!");
						mIndex = 0;
					}
				}
			}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub
				Log.d("screw", "screw22222");
			}
		});
	}
}
