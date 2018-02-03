package com.saintsung.saintpmc.lock;

import android.util.Log;

import com.saintsung.saintpmc.lock.DeviceService.RecvAction;

public class UploadHistory  extends ChainBase{

	UploadHistory(DeviceService service) {
		super(service);
		// TODO Auto-generated constructor stub

		// 响应开关锁
		this.mActionList.add(new RecvAction() {
			@Override
			public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {
				Log.d("UploadHistory", "UploadHistory");
			}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub
				Log.d("UploadHistory", "UploadHistory " + data);
				switch (mcuCmd.mCmd) {
					case 'U':
						int offset = 0;
						// upload start 123456789012345678  FC06030588945807:27602596420161208142133!27602596420161208142748!:upload
						// over
						if (data.contains("upload start") && data.contains("upload over")) {
							// 3C U 00 F927470900752311 927470900752312 762248846
							// c24ae141f7bab318c8ee57d6b79f69ea
							// F927470900752311 2016092816411 9276224884!
							// 123456789012345678 faaf2e542be10711772927fb721b65f1
							String[] str_record_data = data.split(":");
							String str_log = str_record_data[0];
							//						String str_log = data.substring(offset + 5, data.length() - 32);
							String str_sheet_no = str_log.substring(str_log.length() - 16, str_log.length()); // 工单
							DeviceService.recv_record_head = str_sheet_no;
							String str_DeviceID = str_log.substring(str_log.length() - 36, str_log.length() - 36 + 18); // 设备号
							//	str_log = str_record_data[1]; //锁记录

							String lock_record = str_record_data[1];
							lock_record = lock_record.replace("!", MCUCommand.operatelock_succ);
							lock_record = lock_record.replace("A", MCUCommand.operateunlock_succ);
							lock_record = lock_record.replace("B", MCUCommand.operateunlock_fail);
							lock_record = lock_record.replace("C", MCUCommand.operateunlock_fail);
							lock_record = lock_record.replace("D", MCUCommand.operateunlock_fail);
							lock_record = lock_record.replace("#", MCUCommand.operatelock_fail);
							lock_record = lock_record.replace("$", MCUCommand.operatelock_fail);
							lock_record = lock_record.replace("E", MCUCommand.operatereset_succ);
							mService.list_LockRecord.clear();
							mService.list_LockRecord.add(str_DeviceID + lock_record);
						}
						//	else if (str_data_length.equals("00"))
					{
						if (mService.list_LockRecord.size() > 0) {
							// 上传手动开锁记录到服务器
							mService.offline_uploadLog(mService.list_LockRecord); // 前面不为空表示发送手机里的数据
						} else {
							// 读取是否手机中有记录
							mService.offline_uploadLog(mService.list_LockRecord);

						}
					}
					break;
					default:
						break;
				}
			}

		});
	}

}
