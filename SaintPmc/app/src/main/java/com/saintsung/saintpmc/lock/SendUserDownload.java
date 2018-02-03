package com.saintsung.saintpmc.lock;

import java.util.ArrayList;
import java.util.Arrays;

import com.saintsung.saintpmc.lock.DeviceService.RecvAction;

import android.util.Log;

public class SendUserDownload extends ChainBase {

	boolean mOpen = false;

	//	private String str_temp = new String();
	//	private byte[] by_temp = new byte[]{};
	//	private int i_index = -1;
	float i_HadComplete = (float) 0.0;
	//

	FileStream fileStream = new FileStream();
	Command command = new Command();
	byte[] byteArray = fileStream.fileStream(FileStream.sendLockNumberAndPassword, FileStream.read, null);
	String string = new String(byteArray);
	// sendLockNumberAndPassword.txt
	String[] stringArray = string.split("\r\n");
	// 下载离线文件
	int offline_pack_count = 20;
	int offline_pack_total = 0;
	ArrayList<String> str_offline_data = new ArrayList<String>();
	final String str_DownloadStart = "download start      ";
	final String str_DownalodOver = "download over       ";

	//
	SendUserDownload(DeviceService service) {
		super(service);
		service.setBigOpen(0);
		for (int i = 0; i <= stringArray.length; i++) {
			//		for(int i=0;i<1001;i++) {
			// 响应开关锁
			this.mActionList.add(new RecvAction() {

				@Override
				public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {
					if (str_offline_data != null && str_offline_data.size() <= 0) {
						fun_Ready_Offline(); // 整理离线数据
					}
					switch (mcuCmd.mCmd) {
						case MCUCommand.DownloadKeyAndLock: {
							if (!mcuCmd.mErrCode.equals("00")) {
								mIndex = 0;
								i_HadComplete = (float) 0.0;
								throw new Exception(MCUCommand.send_lock_false);
							}
							if (mIndex < str_offline_data.size() - 1) {
								Log.d("mIndex", "mIndex" + mIndex);
								// 20161207修复下发工单
								byte[] b = command.new_sendSheet(str_offline_data.get(mIndex));
								switch (mIndex) {
									case 0: {
										mService.mDevice.Write(FileStream.write, b);
										mService.addLog(mcuCmd.mCmd, "用户离线关联锁具信息开始下传,请稍后...");
										mIndex++;
										DeviceService.sheet_interrupt = "ok";
									}
									break;

									default: {
										Thread.sleep(50);
										mService.mDevice.Write(FileStream.write, b);
										Log.w("已发送", mIndex + "条锁具信息");
										// mService.addLog(mcuCmd.mCmd,"用户离线关联锁具信息开始下传,请稍后...");
										//[[cxq
										double db_complete = (mIndex * 1.0 / (offline_pack_total - 2)) * 100;
										if (i_HadComplete != (float) ((int) (db_complete * 10) / 10.0)) {
											mService.addLog(mcuCmd.mCmd, "已完成 :" + String.format("%.1f", (i_HadComplete)) + "%");
											i_HadComplete = (float) ((int) (db_complete * 10) / 10.0);
										}
										//]]
										mIndex++;
										DeviceService.sheet_interrupt = "ok";
									}
									break;
								}
							} else {
								//				mService.addLog(mcuCmd.mCmd,"用户离线关联锁具信息下传"+mIndex+"条已达上限!");
								// 发送最后一个包
								byte[] b = command.new_sendSheet(str_offline_data.get(mIndex));
								mService.mDevice.Write(FileStream.write, b);

								mService.addLog(mcuCmd.mCmd, "已完成 :" + "100%");
								mService.addLog(mcuCmd.mCmd, "用户离线关联" + stringArray.length + MCUCommand.send_lock_success);
								DeviceService.sheet_interrupt = "success";
							}
						}
						break;
						case MCUCommand.OPCODE_SENDLOCKNUMBERANDPASSWORD_PREFIX: {
							if (!mcuCmd.mErrCode.equals("00")) {
								mIndex = 0;
								i_HadComplete = (float) 0.0;
								throw new Exception(MCUCommand.send_lock_false);
							}
						}
						break;
						default: {
							mIndex = 0;
							i_HadComplete = (float) 0.0;
						}
						mIndex = 0;
						i_HadComplete = (float) 0.0;
					}

				}

				@Override
				public void Do(MCUCommand mcuCmd, String data) throws Exception {
					// TODO Auto-generated method stub

				}

			});
		}
	}

	public String[] fun_ReadLockAndKey() {
		String[] stringArray0 = stringArray[mIndex].split(":");
		//		while (stringArray0[1].length() != 15) {					//碰到10位开锁码直接跳过，继续往下读
		//			mIndex++;
		//			stringArray0=stringArray[mIndex].split(":");
		//		}
		return stringArray0;
	}

	public String[] fun_ReadLockAndKey(String strings) {
		String[] stringArray0 = null;
		try {
			stringArray0 = strings.split(":");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stringArray0;
	}

	// 整理数据
	public void fun_Ready_Offline() {
		String offline_head = "";
		offline_pack_total = (stringArray.length / offline_pack_count) + ((stringArray.length % offline_pack_count == 0) ? 0 : 1) + 2; // 增加一个头一个尾
		// 递归过滤10位开锁码
		String[] stringArray0 = fun_ReadLockAndKey();

		// 整理添加到字符串数组中
		// download start （20）+ 工单号（16）+开始时间（12）+有效期（8） //开始
		// （56）
		String str_avalidata = "ffffffff";
		offline_head = DeviceService.sheet_offline + "200001010000" + str_avalidata;
		String[] lockinfo = new String[stringArray.length];
		for (int k = 0; k < stringArray.length; k++) {
			String[] str_tmp = fun_ReadLockAndKey(stringArray[k]);
			if (str_tmp[2].equals("0001")) {
				lockinfo[k] = str_tmp[0] + str_tmp[1] + "1000" + str_tmp[2];
			} else {
				lockinfo[k] = str_tmp[0] + str_tmp[1] + "0000" + str_tmp[2];
			}
		}
		for (int i = 0; i < offline_pack_total; i++) {
			if (i == 0) {
				str_offline_data.add(str_DownloadStart + offline_head);
			} else if (i == (offline_pack_total - 1)) {
				str_offline_data.add(str_DownalodOver);
			} else {
				String str_lock_info = "";
				String[] lock = Arrays.copyOfRange(lockinfo, (i - 1) * 20, (i) * 20);
				for (int i_lock = 0; i_lock < lock.length; i_lock++) {
					if (lock[i_lock] != null) {
						str_lock_info += lock[i_lock];
					}
				}
				str_offline_data.add(str_lock_info);
			}
		}
	}

}
