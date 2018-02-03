package com.saintsung.saintpmc.lock;

import android.util.Log;

import com.saintsung.saintpmc.lock.DeviceService.RecvAction;

import java.util.ArrayList;
import java.util.Arrays;

public class SendSheet extends ChainBase {
	boolean mOpen = false;

	//	private String str_temp = new String();
	//	private byte[] by_temp = new byte[]{};
	//	private int i_index = -1;
	float i_HadComplete = (float) 0.0;
	FileStream fileStream = new FileStream();
	Command command = new Command();
	byte[] byteArray = fileStream.fileStream(FileStream.sheetFile, FileStream.read, null);
	String string = new String(byteArray);
	// sendLockNumberAndPassword.txt
	String[] stringArray = string.split("\r\n");
	int sheet_pack_count = 20;
	int sheet_pack_total = 0;
	ArrayList<String> str_sheet_data = new ArrayList<String>();

	final String str_DownloadStart = "download start      ";
	final String str_DownalodOver = "download over       ";

	//
	SendSheet(DeviceService service) {

		super(service);
		service.setBigOpen(0);

		if (str_sheet_data != null && str_sheet_data.size() <= 0) {
			fun_Ready_Sheet(); // 整理工单
		}
		//	for (int i = 0; i <= stringArray.length; i++) {
		for(int i =0;i<str_sheet_data.size();i++){
			//		for(int i=0;i<1001;i++) {

			// 响应开关锁
			this.mActionList.add(new RecvAction() {
				@Override
				public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {
					Log.d("SendSheet", "SendSheet");
				}

				public void Do(MCUCommand mcuCmd, String data) throws Exception {

					switch (mcuCmd.mCmd) {
						case MCUCommand.DownloadKeyAndLock: {
							if (!mcuCmd.mErrCode.equals("00")) {
								mIndex = 0;
								i_HadComplete = (float) 0.0;
								throw new Exception(MCUCommand.send_lock_false);
							}

							if (mIndex < str_sheet_data.size() - 1) { // 开始下载工单
								Log.d("mIndex", "mIndex" + mIndex);
								// 20161206修改下发工单
								byte[] b = command.new_sendSheet(str_sheet_data.get(mIndex));
								switch (mIndex) {
									case 0: {
										mService.mDevice.Write(FileStream.write, b);
										mService.addLog(mcuCmd.mCmd, "工单开始下传,请稍后...");
										mIndex++;
										DeviceService.sheet_interrupt = "ok";
									}
									break;

									default: {
										Thread.sleep(50);
										mService.mDevice.Write(FileStream.write, b);
										Log.w("已发送", mIndex + "条锁具信息");
										// [[cxq

										double db_complete = (mIndex * 1.0 / (sheet_pack_total - 2)) * 100;
										if (i_HadComplete != (float) ((int) (db_complete * 10) / 10.0)) {
											mService.addLog(mcuCmd.mCmd, "已完成 :" + String.format("%.1f", (i_HadComplete)) + "%");
											i_HadComplete = (float) ((int) (db_complete * 10) / 10.0);
										}

										// ]]
										mIndex++;
										DeviceService.sheet_interrupt = "ok";
									}
									break;
								}
							} else {
								// mService.addLog(mcuCmd.mCmd,"用户离线关联锁具信息下传"+mIndex+"条已达上限!");
								// 发一个结束包
								byte[] b = command.new_sendSheet(str_sheet_data.get(mIndex));
								mService.mDevice.Write(FileStream.write, b);

								mService.addLog(mcuCmd.mCmd, "已完成 :" + "100%");
								mService.addLog(mcuCmd.mCmd, "工单" + stringArray.length + MCUCommand.send_lock_success);
								DeviceService.sheet_interrupt = "success";
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
			});
		}

	}

	public String[] fun_ReadLockAndKey() {

		String[] stringArray0 = null;
		try {
			stringArray0 = stringArray[mIndex].split(":");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		/*
		 * while (stringArray0[1].length() != 15) { //碰到10位开锁码直接跳过，继续往下读
		 * mIndex++; stringArray0=stringArray[mIndex].split(":"); }
		 */
		return stringArray0;
	}

	public String[] fun_ReadLockAndKey(String strings) {

		String[] stringArray0 = null;
		try {
			stringArray0 = strings.split(":");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		/*
		 * while (stringArray0[1].length() != 15) { //碰到10位开锁码直接跳过，继续往下读
		 * mIndex++; stringArray0=stringArray[mIndex].split(":"); }
		 */
		return stringArray0;
	}

	// 整理数据
	public void fun_Ready_Sheet() {
		String[] sheet_head = new String[1];
		sheet_pack_total = (stringArray.length / sheet_pack_count) + ((stringArray.length % sheet_pack_count == 0) ? 0 : 1) + 2; // 增加一个头，一个尾
		// 递归过滤10位开锁码
		String[] stringArray0 = fun_ReadLockAndKey();
		//// 保存工单号
		sheet_head[0] = stringArray0[0];
		// 整理添加到字符串数组中
		// download start （20）+ 工单号（16）+开始时间（12）+有效期（8） //开始
		// （56）
		String str_avalidata = String.format("%08x", new Command().dateDiff(stringArray0[1], stringArray0[2], "yyyyMMddhhmm", "sec"));
		String[] lockinfo = new String[stringArray.length];
		for (int k = 0; k < stringArray.length; k++) {
			String[] str_tmp = fun_ReadLockAndKey(stringArray[k]);
			//铅封不转 ||str_tmp[5].equals("0006")
			if (str_tmp[5].equals("0001")) {
				lockinfo[k] = str_tmp[3] + str_tmp[4] + "1000" + str_tmp[5];
			} else {
				lockinfo[k] = str_tmp[3] + str_tmp[4] + "0000" + str_tmp[5];
			}
		}
		for (int i = 0; i < sheet_pack_total; i++) {
			if (i == 0) {
				str_sheet_data.add(str_DownloadStart + stringArray0[0] + stringArray0[1] + str_avalidata);
			} else if (i == (sheet_pack_total - 1)) {
				str_sheet_data.add(str_DownalodOver);
			} else {
				String str_lock_info = "";
				String[] lock = Arrays.copyOfRange(lockinfo, (i - 1) * 20, (i) * 20);
				for (int i_lock = 0; i_lock < lock.length; i_lock++) {
					if (lock[i_lock] != null) {
						str_lock_info += lock[i_lock];
					}
				}
				str_sheet_data.add(str_lock_info);
			}
		}
	}
}
