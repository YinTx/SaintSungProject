package com.saintsung.saintpmc.lock;

import android.text.format.Time;
import android.util.Log;

import com.saintsung.saintpmc.HexUtil;
import com.saintsung.saintpmc.lock.MCUCommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class Command {
	private static Command sCmd = null;
	private static final byte by_HEAD = (byte) 0xa0;
	private static final byte by_NEAR = (byte) 0xF0;
	private static final byte by_GET_SET_VALUE = 'R';
	private static final byte by_SET_SET_VALUE = 'P';
	private final int cmd_index = 4;
	User_Share user_Share = new User_Share();
	FileStream fileStream = new FileStream();

	public static Command getInstance() {
		if (sCmd == null) {
			return new Command();
		}
		return sCmd;
	}

	// 新协议
	public MCUCommand new_Parse(String str_unlock, String data) {
		MCUCommand cmd = null;
		String str_status = "";
		int length = 0;
		if (data != null) {
			length = data.length();
		}
		if (length < 2) {
			return null;
		}
		cmd = new MCUCommand();

		if (data.length() > 0) {

			switch (data.charAt(cmd_index)) {
				case MCUCommand.OPCODE_CONNECTED:
					String str_DeviceID = data.substring(cmd_index + 3, data.length() - 32);
					String temp = str_DeviceID.trim();
					if (!temp.equals("default machine")) {
						fileStream.fileStream(FileStream.deviceFile, FileStream.write, str_DeviceID.getBytes());
					}
					cmd.mAttechString = str_DeviceID;
					cmd.mCmd = MCUCommand.OPCODE_CONNECTED;
					break;
				case MCUCommand.EREASE_LOCK_KEY: // 擦除锁
					str_status = data.substring(cmd_index + 1, cmd_index + 3);
					// str_status = data.substring(3, 5);
					cmd.mCmd = MCUCommand.DownloadKeyAndLock;
					cmd.mErrCode = str_status;
					break;
				case MCUCommand.OPCODE_OPEN_LOCK: // 开锁
					Log.d("unlockopen", "unlockopen" + data);
					cmd.mCmd = MCUCommand.OPCODE_OPEN_LOCK;
					break;
				case MCUCommand.DownloadKeyAndLock: // 下载开锁码
					str_status = data.substring(cmd_index + 1, cmd_index + 3);
					// str_status = data.substring(3, 5);
					cmd.mCmd = MCUCommand.DownloadKeyAndLock;
					cmd.mErrCode = str_status;
					break;

				case MCUCommand.AppUploadHistory: // 上传历史记录
					String str_lenth = data.substring(0, 2);
					// if (str_lenth.equals("1A"))
				{
					str_status = data.substring(cmd_index + 1, cmd_index + 3);
					// str_status = data.substring(3, 5);
					if (str_status.equals(MCUCommand.ERRCODE_SUCC)) {
						cmd.mCmd = MCUCommand.AppUploadHistory;
						cmd.mErrCode = str_status;
						cmd.mAttechString = data;
					}
				}
				break;
				case MCUCommand.App_Lock:
					str_status = data.substring(cmd_index + 1, cmd_index + 3);
					// str_status = data.substring(3, 5);
					break;

				default:
					break;
			}

		}

		return cmd;
	}

	// 旧协议 解释
	public MCUCommand Parse(byte[] data, int offset, int len) {
		MCUCommand cmd = null;
		if (len < 2) {
			return null;
		}
		if (data[offset] != 0x02) {
			return null;
		}
		if (data[offset + len - 1] != 0x03) {
			return null;
		}
		byte[] crcTmp = this.crc(data, offset, len - 3);
		//[[wk length check
		//		if(crcTmp[0] != data[offset+len-3] ||
		//			crcTmp[1] != data[offset+len-2] ){
		//			//crc error
		//			return null;
		//		}
		//]]
		cmd = new MCUCommand();
		if (cmd == null) {
			Log.d("command", "command" + "111");
		}
		// save crc value
		TypeConvert.A2HCvtRtnT val = TypeConvert.AsciiToHex(crcTmp[0], crcTmp[1]);
		if (val.rlt == false) {
			return null;
		}
		cmd.mCRC = val.val;

		//附件数据
		val = TypeConvert.AsciiToHex(data[offset + 1], data[offset + 2]);
		if (val.rlt == false) {
			return null;
		}
		int attachLen = val.val;
		if (attachLen > 0) {
			if (attachLen % 2 != 0) {
				//附加数据长度不是2的倍数
				return null;
			}
			cmd.mAttechData = new byte[attachLen / 2];
			for (int i = 0, j = 7; i < attachLen / 2; i++, j += 2) {
				val = TypeConvert.AsciiToHex(data[offset + j], data[offset + j + 1]);
				if (val.rlt == false) {
					return null;
				}
				cmd.mAttechData[i] = val.val;
			}
		}
		//mcu 命令
		val = TypeConvert.AsciiToHex(data[offset + 3], data[offset + 4]);
		if (val.rlt == false) {
			return null;
		}
		cmd.mCmd = val.val;
		switch (cmd.mCmd) {
			case 'S': {
				cmd.mLockerNumber = Integer.toString(TypeConvert.bigEndian_byte2int(cmd.mAttechData, 0));
			}
			break;
			// [[wk
			// case SetActivity.OPCODE_GET_PARAM:{
			// //具体指令
			// val = TypeConvert.AsciiToHex(data[offset+7], data[offset+8]);
			// if (val.val==SetActivity.VS_S00SerialNumber) {
			// cmd.mLockerNumber = new String(cmd.mAttechData);
			// } else {
			// cmd.mLockerNumber =
			// Integer.toString(TypeConvert.bigEndian_byte2int(cmd.mAttechData,0));
			// }
			// }break;
			// ]]
		}
		//errcode
		val = TypeConvert.AsciiToHex(data[offset + 5], data[offset + 6]);
		if (val.rlt == false) {
			return null;
		}
		//cmd.mErrCode = val.val;
		return cmd;
	}

	//检查
	public int check(int i, byte[] encodebData) {
		int crc_1byte = 0;
		if (encodebData[i] < 0) {
			int negative = encodebData[i] & 0x7f | (1 << 7);
			crc_1byte = crc_1byte ^ negative;
		} else {
			crc_1byte = crc_1byte ^ encodebData[i];
		}
		for (int j = 0; j < 8; j++) {
			if ((crc_1byte & 0x01) == 1) {
				//odd
				crc_1byte = crc_1byte ^ 0x31;
			}
			crc_1byte >>= 1;
		}
		return crc_1byte;
	}

	//CRC校验
	private byte[] crc(byte[] data, int offset, int len) {
		byte[] crcBytes = null;
		int byteFinish = 0;
		for (int i = offset; i < (offset + len); i++) {
			byteFinish = check(i, data) ^ byteFinish;
		}
		crcBytes = TypeConvert.HexToAscii((byte) (byteFinish));
		return crcBytes;
	}

	//生成读锁号命令
	public byte[] AppReadLockerNunber() {
		byte[] data = new byte[8];
		byte[] tmp = null;
		data[0] = 0x02;
		//附加数据长度
		tmp = TypeConvert.HexToAscii((byte) (0));
		data[1] = tmp[0];
		data[2] = tmp[1];
		//命令
		tmp = TypeConvert.HexToAscii((byte) ('S'));
		data[3] = tmp[0];
		data[4] = tmp[1];
		//crc
		tmp = crc(data, 0, 5);
		data[5] = tmp[0];
		data[6] = tmp[1];

		data[7] = 0x03;
		return data;
	}

	public byte[] AppLockerReady() {
		byte[] data = new byte[8];
		byte[] tmp = null;
		data[0] = 0x02;
		//附加数据长度
		tmp = TypeConvert.HexToAscii((byte) (0));
		data[1] = tmp[0];
		data[2] = tmp[1];
		//命令
		tmp = TypeConvert.HexToAscii((byte) ('F'));
		data[3] = tmp[0];
		data[4] = tmp[1];
		//crc
		tmp = crc(data, 0, 5);
		data[5] = tmp[0];
		data[6] = tmp[1];

		data[7] = 0x03;
		return data;
	}

	public byte[] AppResponseKeyReady() {
		byte[] data = new byte[8];
		byte[] tmp = null;
		data[0] = 0x02;
		//附加数据长度
		tmp = TypeConvert.HexToAscii((byte) (0));
		data[1] = tmp[0];
		data[2] = tmp[1];
		//命令
		tmp = TypeConvert.HexToAscii((byte) ('B'));
		data[3] = tmp[0];
		data[4] = tmp[1];
		//crc
		tmp = crc(data, 0, 5);
		data[5] = tmp[0];
		data[6] = tmp[1];

		data[7] = 0x03;
		return data;
	}

	public static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	//蓝牙连接成功后发送'l',同步时间
	public byte[] AppConnectedLED(String date) {
		// 长度于0xff 扩充到0xffff
		byte[] data = new byte[5 + 14 + 2];
		byte[] tmp = null;
		int length = 0;
		int offset = 0;
		data[length++] = by_HEAD;
		// 附加数据长度

		tmp = TypeConvert.HexToAscii((byte) ((14 & 0xff00) >> 8));
		data[length++] = tmp[0];
		data[length++] = tmp[1];
		tmp = TypeConvert.HexToAscii((byte) ((14 & 0x00ff)));
		data[length++] = tmp[0];
		data[length++] = tmp[1];

		// tmp = TypeConvert.HexToAscii((byte) (14));
		// data[length++] = tmp[0];
		// data[length++] = tmp[1];
		// 命令
		data[length++] = MCUCommand.OPCODE_CONNECTED;
		//		Time time = new Time();
		//		time.setToNow();
		//		String str_time = String.format("%02d", time.year) + String.format("%02d", time.month + 1) + String.format("%02d", time.monthDay) + String.format("%02d", time.hour) + String.format("%02d", time.minute) + String.format("%02d", time.second + 3);
		String str_time = date;

		System.arraycopy(str_time.getBytes(), 0, data, length, str_time.length());
		length += str_time.length();
		//MD5 32
		/*
		tmp = new byte[3];
		System.arraycopy(data, offset+1, tmp, 0, tmp.length);
		byte[] by_md5 = user_Share.byteToMD5(tmp).getBytes();
		System.arraycopy(by_md5, 0, data, length, by_md5.length);
		length += by_md5.length;
		*/

		data[length++] = by_NEAR;
		return data;
	}

	// [[新协议发送'E'擦除记录
	public byte[] EreaseLockKey() {
		byte[] data = new byte[37 + 2];
		byte[] tmp = null;
		int length = 0;
		int offset = 0;
		data[length++] = by_HEAD;
		// 附加数据长度
		tmp = TypeConvert.HexToAscii((byte) ((0 & 0xff00) >> 8));
		data[length++] = tmp[0];
		data[length++] = tmp[1];

		tmp = TypeConvert.HexToAscii((byte) ((0 & 0x00ff)));
		data[length++] = tmp[0];
		data[length++] = tmp[1];
		// tmp = TypeConvert.HexToAscii((byte) (0));
		// data[length++] = tmp[0];
		// data[length++] = tmp[1];
		data[length++] = MCUCommand.EREASE_LOCK_KEY; // 'E'擦除锁号
		// md5
		byte[] temp = new byte[5];
		System.arraycopy(data, offset + 1, temp, 0, temp.length);
		byte[] by_md5 = user_Share.byteToMD5(temp).getBytes();
		System.arraycopy(by_md5, 0, data, length, by_md5.length);
		length += by_md5.length;
		data[length++] = by_NEAR;
		return data;
	}

	/*
	 * 老协议发送'l'
	 */
	public byte[] old_AppConnectedLED() {
		byte[] data = new byte[8];
		byte[] tmp = null;
		data[0] = 0x02;
		//附加数据长度
		tmp = TypeConvert.HexToAscii((byte) (0));
		data[1] = tmp[0];
		data[2] = tmp[1];
		// 命令
		tmp = TypeConvert.HexToAscii((MCUCommand.OPCODE_CONNECTED));
		data[3] = tmp[0];
		data[4] = tmp[1];
		//crc
		tmp = crc(data, 0, 5);
		data[5] = tmp[0];
		data[6] = tmp[1];

		data[7] = 0x03;
		return data;
	}

	// 发送开锁码
	// 原值，正负2开锁
	// bigOpen:0 normal 1 bigopen 2 smallOpen
	public byte[] new_AppSendLockerKey(byte cmd, String number, String key, String lockType, int bigOpen, int unlockType) {
		// String lock_type = key.substring(key.length()-1,key.length());
		// String str_temp = key.substring(0, key.length()-2);
		// key = str_temp;

		// 锁类型4个字节 56+4
		// 9+15+4
		// 扩充2个字节增加长度
		byte[] data = new byte[60 + 2];
		byte[] tmp = null;
		byte[] by_data = new byte[100]; // 大小值
		int length = 0;
		int offset = 0;
		// 头
		data[length++] = by_HEAD;

		tmp = TypeConvert.HexToAscii((byte) ((0x17 & 0xff00) >> 8));
		data[length++] = tmp[0];
		data[length++] = tmp[1];

		tmp = TypeConvert.HexToAscii((byte) ((0x17 & 0x00ff)));
		data[length++] = tmp[0];
		data[length++] = tmp[1];

		// tmp = TypeConvert.HexToAscii((byte) 0x17);
		// data[length++] = tmp[0];
		// data[length++] = tmp[1];

		// 开锁命令
		data[length++] = MCUCommand.OPCODE_OPEN_LOCK;

		// 开锁码
		if (key.length() == 15) {

			if (key.length() == 15) {
				byte b = 0;
				int ii = 0;
				String sub_key = "";
				if (unlockType > 0) {
					for (int j = 0; j < 5; j++) {
						int v = Integer.parseInt(key.substring(j * 3, j * 3 + 3));
						if (j == 0) {
							switch (bigOpen) {
								case 1: {
									v += unlockType * 6;
								}
								break;
								case 2: {
									v -= unlockType * 6;
								}
								break;
								default:
									break;
							}
							sub_key = String.format("%03d", v);
						}

						key = sub_key + key.substring(3, key.length());
					}
					//System.arraycopy(by_data, 0, data, length, by_data.length);
				}
				byte[] by_key = key.getBytes();

				System.arraycopy(by_key, 0, data, length, by_key.length);
			}

		}
		length += key.length();
		// 旋转控制字
		byte[] by_Cmd = new byte[2];
		if (lockType.equals("0001")) {
			by_Cmd[0] = (byte) 0x10;
			by_Cmd[1] = (byte) 0x00;
		} else if (lockType.equals("0002")) {
			by_Cmd[0] = (byte) 0x00;
			by_Cmd[1] = (byte) 0x00;
		}
		tmp = TypeConvert.HexToAscii(by_Cmd[0]);
		data[length++] = tmp[0];
		data[length++] = tmp[1];
		tmp = TypeConvert.HexToAscii(by_Cmd[1]);
		data[length++] = tmp[0];
		data[length++] = tmp[1];

		// 锁类型
		if (lockType != null && lockType.length() > 0) {
			tmp = lockType.getBytes();
			System.arraycopy(tmp, 0, data, length, tmp.length);
		}
		length += lockType.length();

		// MD5
		byte[] temp = new byte[28];
		System.arraycopy(data, offset + 1, temp, 0, temp.length);
		byte[] by_md5 = user_Share.byteToMD5(temp).getBytes();
		//StringBuffer stringBuffer = new StringBuffer();
		// for (int i = 0; i < temp.length; i++) {
		// stringBuffer.append(String.format("%c", temp[i]));
		// }
		// Log.d("pre_md5","pre_md5"+stringBuffer.toString());
		System.arraycopy(by_md5, 0, data, length, by_md5.length);
		length += by_md5.length;
		data[length++] = by_NEAR;

		String str_data = new String(data);
		return data;
	}

	// 发送开锁码
	// 原值，正负2开锁
	// bigOpen:0 normal 1 bigopen 2 smallOpen
	public byte[] AppSendLockerKey(byte cmd, String number, String key, String lockType, int bigOpen) {
		int len = 0;
		if (key.length() == 15) {
			len = 36;
		} else {
			len = 26;
		}
		byte[] data = new byte[len];
		byte[] tmp = null;
		int ii = 0;
		data[ii++] = 0x02;
		//附加数据长度
		if (key.length() == 15) {
			tmp = TypeConvert.HexToAscii((byte) (28));
		} else {
			tmp = TypeConvert.HexToAscii((byte) (18));
		}
		data[ii++] = tmp[0];
		data[ii++] = tmp[1];
		//命令
		tmp = TypeConvert.HexToAscii(cmd);
		data[ii++] = tmp[0];
		data[ii++] = tmp[1];
		// data
		if (key.length() == 15) {
			byte b = 0;
			for (int j = 0; j < 5; j++) {
				int v = Integer.parseInt(key.substring(j * 3, j * 3 + 3));
				if (j == 0) {
					switch (bigOpen) {
						case 1: {
							v += 12;
						}
						break;
						case 2: {
							v -= 12;
						}
						break;
					}
				}
				b = (byte) ((v >> 8) & 0x000000FF);
				tmp = TypeConvert.HexToAscii(b);
				data[ii++] = tmp[0];
				data[ii++] = tmp[1];
				b = (byte) (v & 0x000000FF);
				tmp = TypeConvert.HexToAscii(b);
				data[ii++] = tmp[0];
				data[ii++] = tmp[1];
			}
			// �߳�����//附加锁号码
			byte[] lock_no_bytes = TypeConvert.bigEndian_int2byte(Integer.parseInt(number));
			for (int i = 0; i < 4; i++) {
				b = lock_no_bytes[i];
				tmp = TypeConvert.HexToAscii(b);
				data[ii++] = tmp[0];
				data[ii++] = tmp[1];
			}
		} else if (key.length() == 10) {

			byte b = 0;
			for (int j = 0; j < 5; j++) {
				b = (byte) (Integer.parseInt(key.substring(j * 2, j * 2 + 2)));
				if (j == 0) {
					switch (bigOpen) {
						case 1: {
							b += 2;
						}
						break;
						case 2: {
							b -= 2;
						}
						break;
					}
				}
				tmp = TypeConvert.HexToAscii(b);
				data[ii++] = tmp[0];
				data[ii++] = tmp[1];
			}
			byte[] lock_no_bytes = TypeConvert.bigEndian_int2byte(Integer.parseInt(number));
			for (int i = 0; i < 4; i++) {
				b = lock_no_bytes[i];
				tmp = TypeConvert.HexToAscii(b);
				data[ii++] = tmp[0];
				data[ii++] = tmp[1];
			}
		}
		//crc
		tmp = crc(data, 0, ii);
		data[ii++] = tmp[0];
		data[ii++] = tmp[1];
		data[ii++] = 0x03;
		return data;
	}

	// [[cxq
	// 正负3，正负4开锁

	// bigOpen:0 normal 1 bigopen 2 smallOpen
	public byte[] AppSendLockerKey(byte cmd, String number, String key, int bigOpen, int i_LockType) {
		int len = 0;
		if (key.length() == 15) {
			len = 36;
		} else {
			len = 26;
		}
		byte[] data = new byte[len];
		byte[] tmp = null;
		int ii = 0;
		data[ii++] = 0x02;
		// 附加数据长度
		if (key.length() == 15) {
			tmp = TypeConvert.HexToAscii((byte) (28));
		} else {
			tmp = TypeConvert.HexToAscii((byte) (18));
		}
		data[ii++] = tmp[0];
		data[ii++] = tmp[1];
		// 命令
		tmp = TypeConvert.HexToAscii(cmd);
		data[ii++] = tmp[0];
		data[ii++] = tmp[1];
		// data
		if (key.length() == 15) {
			byte b = 0;
			for (int j = 0; j < 5; j++) {
				int v = Integer.parseInt(key.substring(j * 3, j * 3 + 3));
				if (j == 0) {
					switch (bigOpen) {
						case 1: {
							if (i_LockType == 3) {
								v += 18;
							} else if (i_LockType == 4) {
								v += 24;
							}

						}
						break;
						case 2: {
							if (i_LockType == 3) {
								v -= 18;
							} else if (i_LockType == 4) {
								v -= 24;
							}
						}
						break;
					}
				}
				b = (byte) ((v >> 8) & 0x000000FF);
				tmp = TypeConvert.HexToAscii(b);
				data[ii++] = tmp[0];
				data[ii++] = tmp[1];
				b = (byte) (v & 0x000000FF);
				tmp = TypeConvert.HexToAscii(b);
				data[ii++] = tmp[0];
				data[ii++] = tmp[1];
			}
			// �߳�����
			byte[] lock_no_bytes = TypeConvert.bigEndian_int2byte(Integer.parseInt(number));
			for (int i = 0; i < 4; i++) {
				b = lock_no_bytes[i];
				tmp = TypeConvert.HexToAscii(b);
				data[ii++] = tmp[0];
				data[ii++] = tmp[1];
			}
		} else if (key.length() == 10) {

			byte b = 0;
			for (int j = 0; j < 5; j++) {
				b = (byte) (Integer.parseInt(key.substring(j * 2, j * 2 + 2)));
				if (j == 0) {
					switch (bigOpen) {
						case 1: {
							if (i_LockType == 3) {
								b += 2;
							} else if (i_LockType == 4) {
								b += 3;
							}
						}
						break;
						case 2: {
							if (i_LockType == 3) {
								b -= 2;
							} else if (i_LockType == 4) {
								b -= 3;
							}
						}
						break;
					}
				}
				tmp = TypeConvert.HexToAscii(b);
				data[ii++] = tmp[0];
				data[ii++] = tmp[1];
			}
			byte[] lock_no_bytes = TypeConvert.bigEndian_int2byte(Integer.parseInt(number));
			for (int i = 0; i < 4; i++) {
				b = lock_no_bytes[i];
				tmp = TypeConvert.HexToAscii(b);
				data[ii++] = tmp[0];
				data[ii++] = tmp[1];
			}
		}
		// crc
		tmp = crc(data, 0, ii);
		data[ii++] = tmp[0];
		data[ii++] = tmp[1];
		data[ii++] = 0x03;
		return data;
	}

	// ]]
	// 新协议发送关锁指令
	public byte[] AppSendClose() {
		// 扩充2个字节增加长度
		byte[] data = new byte[41 + 2];
		byte[] tmp = null;
		int length = 0;
		int offset = 0;
		// 头
		data[length++] = by_HEAD;
		// 长度
		tmp = TypeConvert.HexToAscii((byte) ((0x04 & 0xff00) >> 8));
		data[length++] = tmp[0];
		data[length++] = tmp[1];

		tmp = TypeConvert.HexToAscii((byte) ((0x04 & 0x00ff)));
		data[length++] = tmp[0];
		data[length++] = tmp[1];
		// tmp = TypeConvert.HexToAscii((byte) 0x04);
		// data[length++] = tmp[0];
		// data[length++] = tmp[1];
		// 命令字
		data[length++] = MCUCommand.App_Lock;
		//旋转控件字
		tmp = TypeConvert.HexToAscii((byte) 0x10);
		data[length++] = tmp[0];
		data[length++] = tmp[1];
		tmp = TypeConvert.HexToAscii((byte) 0x00);
		data[length++] = tmp[0];
		data[length++] = tmp[1];
		//md5
		byte[] md5 = new byte[length - 1];
		System.arraycopy(data, 1, md5, 0, length - 1);
		byte[] md5_2 = user_Share.byteToMD5(md5).getBytes();
		System.arraycopy(md5_2, 0, data, length, md5_2.length);
		data[length + md5_2.length] = by_NEAR;
		return data;
	}

	//旧协议
	public byte[] AppSendClose(byte b) {
		// change before
		// return AppSendLockerKey(b,"123456789","1234567890",1);
		// [[wk change 2016/03/25
		byte[] data = new byte[8];
		byte[] tmp = null;
		data[0] = 0x02;
		//附加数据长度
		tmp = TypeConvert.HexToAscii((byte) (0));
		data[1] = tmp[0];
		data[2] = tmp[1];
		// 命令
		tmp = TypeConvert.HexToAscii(b);
		data[3] = tmp[0];
		data[4] = tmp[1];
		//crc
		tmp = crc(data, 0, 5);
		data[5] = tmp[0];
		data[6] = tmp[1];

		data[7] = 0x03;
		return data;
		//]]
	}

	// [[CXQ
	// 修改广播名称
	public byte[] fun_SetS00Name(byte[] by_data) {
		byte[] data = new byte[10 + (by_data.length)];
		byte[] tmp = null;
		// 头
		data[0] = 0x02;
		// 附加数据长度
		tmp = TypeConvert.HexToAscii((byte) (by_data.length + 2));
		data[1] = tmp[0];
		data[2] = tmp[1];
		// 命令
		tmp = TypeConvert.HexToAscii(SetActivity.OPCODE_SET_PARAM);
		data[3] = tmp[0];
		data[4] = tmp[1];

		// data
		data[5] = SetActivity.OPCODE_SET_NAME;
		data[6] = (byte) (by_data.length);

		// data
		int i = 0;
		for (; i < by_data.length; i++) {
			data[i + 7] = by_data[i];
		}
		// crc
		tmp = crc(data, 0, i + 7);
		data[i + 7] = tmp[0];
		data[i + 8] = tmp[1];
		// 03
		data[i + 9] = 0x03;
		return data;
	}

	// ]]

	/*
	 * 新协议获取设备设置信息
	 */
	public byte[] new_getS00SetValue() {
		// 扩充2个字节增加长度
		byte[] data = new byte[37 + 2];
		byte[] tmp = null;
		int length = 0;
		data[length++] = by_HEAD;
		// 附加数据长度
		tmp = TypeConvert.HexToAscii((byte) ((0 & 0xff00) >> 8));
		data[length++] = tmp[0];
		data[length++] = tmp[1];

		tmp = TypeConvert.HexToAscii((byte) ((0 & 0x00ff)));
		data[length++] = tmp[0];
		data[length++] = tmp[1];

		// 命令字符
		data[length++] = by_GET_SET_VALUE;

		// 计算MD5
		byte[] temp = new byte[32];
		byte[] by_data = new byte[3 + 2];
		for (int k = 0; k < by_data.length; k++) {
			by_data[k] = data[k + 1];
		}
		temp = user_Share.byteToMD5(by_data).getBytes();

		for (int i = 0; i < temp.length; i++) {
			data[length++] = temp[i];
		}
		data[length++] = by_NEAR;

		return data;
	}

	// [[CXQ 新协议
	public byte[] setS00SetValue(byte[] date, byte[] name, byte[] serial, byte[] offset, byte[] set) {
		// 扩充2个字节，增加长度
		byte[] data = new byte[95 + 2];
		Arrays.fill(data, (byte) 0xff);
		byte[] tmp = null;
		int length = 0;
		// 头开始
		data[length++] = by_HEAD;
		// 附加数据长度
		tmp = TypeConvert.HexToAscii((byte) ((58 & 0xff00) >> 8));
		data[length++] = tmp[0];
		data[length++] = tmp[1];

		tmp = TypeConvert.HexToAscii((byte) ((58 & 0x00ff)));
		data[length++] = tmp[0];
		data[length++] = tmp[1];
		// 命令字符
		data[length++] = by_SET_SET_VALUE;
		//时间
		Time time = new Time();
		time.setToNow();
		String str_time = String.format("%02d", time.year) + String.format("%02d", time.month + 1) + String.format("%02d", time.monthDay) + String.format("%02d", time.hour) + String.format("%02d", time.minute) + String.format("%02d", time.second);
		byte[] by_time = str_time.getBytes();
		System.arraycopy(by_time, 0, data, length, by_time.length);
		//序列号
		length += by_time.length;
		System.arraycopy(serial, 0, data, length, serial.length);
		//广播名
		length += serial.length;
		System.arraycopy(name, 0, data, length, name.length);

		length += name.length;

		data[length++] = offset[0];
		data[length++] = offset[1];
		data[length++] = set[0];
		data[length++] = set[1];
		byte[] md5 = new byte[length - 1];
		System.arraycopy(data, 1, md5, 0, length - 1);
		byte[] md5_2 = user_Share.byteToMD5(md5).getBytes();
		System.arraycopy(md5_2, 0, data, length, md5_2.length);
		data[length + md5_2.length] = by_NEAR;

		return data;
	}
	//]]

	// [[wk 旧协议
	public byte[] getS00SetValue(byte b) {
		byte[] data = new byte[10];
		byte[] tmp = null;
		//
		data[0] = 0x02;
		//附加数据长度
		tmp = TypeConvert.HexToAscii((byte) (2));
		data[1] = tmp[0];
		data[2] = tmp[1];
		//命令
		tmp = TypeConvert.HexToAscii(SetActivity.OPCODE_GET_PARAM);
		data[3] = tmp[0];
		data[4] = tmp[1];
		//data
		data[5] = b;
		data[6] = (byte) 0;
		//crc
		tmp = crc(data, 0, 7);
		data[7] = tmp[0];
		data[8] = tmp[1];
		//
		data[9] = 0x03;
		return data;
	}

	public byte[] setS00SetValue(byte[] b) {
		Log.d("Set", "Set" + "setS00SetValue");
		byte[] data = new byte[10];
		byte[] tmp = null;
		//
		data[0] = 0x02;
		//附加数据长度
		tmp = TypeConvert.HexToAscii((byte) (2));
		data[1] = tmp[0];
		data[2] = tmp[1];
		//命令
		tmp = TypeConvert.HexToAscii(SetAllActivity.OPCODE_SET_PARAM);
		data[3] = tmp[0];
		data[4] = tmp[1];
		//data
		data[5] = b[0];
		data[6] = b[1];
		//crc
		tmp = crc(data, 0, 7);
		data[7] = tmp[0];
		data[8] = tmp[1];
		//
		data[9] = 0x03;
		return data;
	}

	//设置设置值偏移值
	public byte[] fun_setS00SetValue(byte[] b) {
		Log.d("Set", "Set" + "setS00SetValue");
		byte[] data = new byte[14];
		byte[] tmp = null;
		//
		data[0] = 0x02;
		//附加数据长度
		tmp = TypeConvert.HexToAscii((byte) (6));
		data[1] = tmp[0];
		data[2] = tmp[1];
		//命令
		tmp = TypeConvert.HexToAscii(SetAllActivity.OPCODE_SET_PARAM);
		data[3] = tmp[0];
		data[4] = tmp[1];
		//data
		data[5] = b[0];
		data[6] = b[1];
		data[7] = b[2];
		data[8] = b[3];
		data[9] = b[4];
		data[10] = b[5];
		//crc
		tmp = crc(data, 0, 11);
		data[11] = tmp[0];
		data[12] = tmp[1];
		//
		data[13] = 0x03;
		return data;
	}

	/*
	 * 下装开锁码命令 域 长度 值 备注 开始字节 1byte 0x02 数据长度 2bytes 命令字 2bytes 0x34,0x34 D 数据内容
	 * 数据 MD5校验 32bytes 数据长度+命令字+数据内容的MD5校验码 结束字节 1byte 0x03
	 * 
	 * 开始字节 数据长度 命令类型 数据内容 MD5校准值 结束字节 1Bytes 2Bytes 2Bytes 2*数据长度Bytes 32Bytes
	 * 1Bytes
	 * 
	 * LockNumber 4个字节 key 10个字节 1+2+2+14+32+1 =52个字节
	 */
	private final static byte by_start = 0x02;
	private final static byte by_end = 0x03;
	private final static byte by_cmd = (byte) 'D';

	// 新协议离线下载开锁码
	public byte[] new_sendSheet(String sheet) {
		// 新协议先发送‘E’擦除记录
		// 加入锁号与开锁码
		if (sheet == null) {

			byte[] data = Command.getInstance().EreaseLockKey(); // 先擦除

			return data;

		} else {
			// 加入锁类型长67+4个字符
			// LockNumber
			// 64+4+16
			// 16表示工单号码
			// 4锁类型
			// 16时间+有效期
			// 时间超过45天
			StringBuilder stringBuilder1 = new StringBuilder();
			// 起始标记0xa0 ,4数据长度，sheet.length()数据长度,32位MD5长度，1结束
			// 20161205 +4 有效期扩展到4个字节，数据长度扩展到4个FFFF
			// 头+长度+命令+数据+md5+尾
			byte[] data = new byte[1 + 4 + 1 + sheet.length() + 32 + 1];
			byte[] tmp = null;
			int length = 0;
			int offset = 0;
			// 头
			data[length++] = by_HEAD;
			// stringBuilder1.append((char) by_HEAD);
			// 长度
			tmp = TypeConvert.HexToAscii((byte) ((sheet.length() & 0xff00) >> 8));
			stringBuilder1.append((char) tmp[0]);
			stringBuilder1.append((char) tmp[1]);

			tmp = TypeConvert.HexToAscii((byte) ((sheet.length() & 0x00ff)));
			stringBuilder1.append((char) tmp[0]);
			stringBuilder1.append((char) tmp[1]);
			// 操作码
			stringBuilder1.append((char) MCUCommand.DownloadKeyAndLock);
			// 数据部分
			stringBuilder1.append(sheet);
			// MD5
			String str_md5 = user_Share.byteToMD5(stringBuilder1.toString().getBytes());
			stringBuilder1.append(str_md5);

			byte[] by_data = stringBuilder1.toString().getBytes();
			System.arraycopy(by_data, 0, data, length, by_data.length);
			length += by_data.length;
			// 尾部
			data[length++] = by_NEAR;
			//			StringBuilder stringBuilder = new StringBuilder();
			//			for(int k= 0;k<by_temp.length;k++)
			//			{
			//				stringBuilder.append(String.format("%c", by_temp[k]));
			//			}
			//			
			//			Log.d("check","check--"+stringBuilder.toString());
			Log.e("TAG","data="+ HexUtil.encodeHexStr(data));
			return data;
		}

	}

	public Long dateDiff(String startTime, String endTime, String format, String str) {
		// 按照传入的格式生成一个simpledateformate对象
		SimpleDateFormat sd = new SimpleDateFormat(format);
		long nd = 24 * 60 * 60;// 一天的毫秒数
		long nh = 60 * 60;// 一小时的毫秒数
		long nm = 60;// 一分钟的毫秒数
		long ns = 1000;// 一秒钟的毫秒数
		long diff;
		long day = 0;
		long hour = 0;
		long min = 0;
		long sec = 0;
		// 获得两个时间的毫秒时间差异
		try {
			diff = (sd.parse(endTime).getTime() - sd.parse(startTime).getTime()) / 1000;
			day = diff / nd;// 计算差多少天
			hour = diff % nd / nh + day * 24;// 计算差多少小时
			min = diff % nd % nh / nm + day * 24 * 60;// 计算差多少分钟
			sec = diff;// 计算差多少秒
			// 输出结果
			System.out.println("时间相差：" + day + "天" + (hour - day * 24) + "小时" + (min - day * 24 * 60) + "分钟" + sec + "秒。");
			System.out.println("hour=" + hour + ",min=" + min);
			if (str.equalsIgnoreCase("h")) {
				return hour;
			} else if (str.equalsIgnoreCase("m") || str.equalsIgnoreCase("min")) {
				return min;
			} else if (str.equalsIgnoreCase("s") || str.equalsIgnoreCase("sec")) {
				return sec;
			}

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (str.equalsIgnoreCase("h")) {
			return hour;
		} else {
			return min;
		}
	}

	// 新协议离线下载开锁码
	public byte[] new_sendLockNumberAndPassword(String number, String key) {

		//新协议先发送‘E’擦除记录
		// 加入锁号与开锁码
		if (key == null) {
			byte[] data = Command.getInstance().EreaseLockKey(); //先擦除

			return data;

		} else {
			//加入锁类型长67+4个字符
			// LockNumber
			byte[] data = new byte[67];
			byte[] tmp = null;
			int length = 0;
			int offset = 0;
			//头
			data[length++] = by_HEAD;
			// 长度
			// 扩充2个字节增加长度
			tmp = TypeConvert.HexToAscii((byte) ((0x1e & 0xff00) >> 8));
			data[length++] = tmp[0];
			data[length++] = tmp[1];

			tmp = TypeConvert.HexToAscii((byte) ((0x1e & 0x00ff)));
			data[length++] = tmp[0];
			data[length++] = tmp[1];
			// tmp = TypeConvert.HexToAscii((byte) 0x1e);
			// data[length++] = tmp[0];
			// data[length++] = tmp[1];
			// 操作码
			data[length++] = MCUCommand.DownloadKeyAndLock;
			//工单号
			tmp = TypeConvert.HexToAscii((byte) 0x01);
			data[length++] = tmp[0];
			data[length++] = tmp[1];
			//锁号
			byte[] by_lock = number.getBytes();
			System.arraycopy(by_lock, 0, data, length, by_lock.length);
			length += by_lock.length;
			//开锁码
			byte[] by_key = key.getBytes();
			System.arraycopy(by_key, 0, data, length, by_key.length);
			length += by_key.length;
			//旋转控制字
			tmp = TypeConvert.HexToAscii((byte) 0x10);
			data[length++] = tmp[0];
			data[length++] = tmp[1];
			tmp = TypeConvert.HexToAscii((byte) 0x00);
			data[length++] = tmp[0];
			data[length++] = tmp[1];
			//MD5
			byte[] by_temp = new byte[33];
			System.arraycopy(data, offset + 1, by_temp, 0, by_temp.length);
			byte[] by_md5 = user_Share.byteToMD5(by_temp).getBytes();
			System.arraycopy(by_md5, 0, data, length, by_md5.length);
			length += by_md5.length;

			//尾部
			data[length++] = by_NEAR;
			StringBuilder stringBuilder = new StringBuilder();
			for (int k = 0; k < by_temp.length; k++) {
				stringBuilder.append(String.format("%c", by_temp[k]));
			}

			Log.d("check", "check--" + stringBuilder.toString());
			return data;
		}

	}

	// send lockNumber and pwd //发送锁号与开锁码
	public byte[] __sendLockNumberAndPassword(String number, String key) {
		byte[] tmp = null;
		byte[] data = new byte[20];
		int ii = 0;
		//set prefix
		data[ii++] = MCUCommand.s_prefix_send_user_download;
		//set data length
		tmp = TypeConvert.HexToAscii((byte) (14));
		data[ii++] = tmp[0];
		data[ii++] = tmp[1];
		if (key == null) {
			String string;
			if (number.equals(MCUCommand.send_lock_start)) {
				//send locknostart000(length=14)
				string = MCUCommand.send_lock_start;
			} else {
				string = MCUCommand.send_lock_end;
			}
			tmp = string.getBytes();
			for (byte b : tmp) {
				data[ii++] = b;
			}
		} else {
			// LockNumber
			byte[] lock_no_bytes = new byte[4];
			if (key.length() == 15) {
				lock_no_bytes = TypeConvert.bigEndian_int2byte(Integer.parseInt(number));
			} else {
				// 密码长度=10的锁号最高位置1
				lock_no_bytes = TypeConvert.bigEndian_int2byte(Integer.parseInt(number) | 1 << 31);
			}
			for (int i = 0; i < 4; i++) {
				data[ii++] = lock_no_bytes[i];
			}
			int v;
			byte b = 0;
			for (int j = 0; j < 5; j++) {
				v = Integer.parseInt(key.substring(j * key.length() / 5, j * key.length() / 5 + key.length() / 5));
				b = (byte) ((v >> 8) & 0x000000FF);
				data[ii++] = b;
				b = (byte) (v & 0x000000FF);
				data[ii++] = b;
			}
		}
		//crc
		tmp = crc(data, 0, 17);
		data[ii++] = tmp[0];
		data[ii++] = tmp[1];
		//set suffix
		data[ii++] = MCUCommand.s_suffix_send_user_download;
		return data;
	}

	// 上传记录
	public byte[] AppUploadHistory() {
		// 扩充两个字节增加长度
		byte[] data = new byte[37 + 2];
		byte[] tmp = null;
		int length = 0;
		int offset = 0;
		// 头部
		data[length++] = by_HEAD;
		// 长度
		tmp = TypeConvert.HexToAscii((byte) ((0 & 0xff00) >> 8));
		data[length++] = tmp[0];
		data[length++] = tmp[1];

		tmp = TypeConvert.HexToAscii((byte) ((0 & 0x00ff)));
		data[length++] = tmp[0];
		data[length++] = tmp[1];
		// tmp = TypeConvert.HexToAscii((byte) 0x00);
		// data[length++] = tmp[0];
		// data[length++] = tmp[1];
		// 命令字
		data[length++] = MCUCommand.AppUploadHistory;
		// 数据无
		// md5校验
		byte[] temp = new byte[3 + 2];
		System.arraycopy(data, offset + 1, temp, 0, temp.length);
		byte[] by_md5 = user_Share.byteToMD5(temp).getBytes();
		System.arraycopy(by_md5, 0, data, length, by_md5.length);
		length += by_md5.length;
		data[length++] = by_NEAR;

		return data;
	}

	public byte[] setUpdateTime(byte[] b) {
		Log.d("Set", "Set" + "setUpdateTime");
		int length = 0;
		byte[] data = new byte[23];
		byte[] tmp = null;
		//set prefix
		data[length++] = MCUCommand.s_prefix_send_user_download;
		//附加数据长度
		tmp = TypeConvert.HexToAscii((byte) (14));
		data[length++] = tmp[0];
		data[length++] = tmp[1];
		//命令
		tmp = TypeConvert.HexToAscii(SetAllActivity.OPCODE_SET_PARAM);
		data[length++] = tmp[0];
		data[length++] = tmp[1];
		//data
		data[length++] = SetAllActivity.VS_update_time;
		for (byte c : b) {
			data[length++] = c;
		}
		//crc
		tmp = crc(data, 0, 7);
		data[length++] = tmp[0];
		data[length++] = tmp[1];
		//set suffix
		data[length++] = MCUCommand.s_suffix_send_user_download;
		return data;
	}
	//]]
	//[[CXQ
	//检测到丢包情况重接重发

	public byte[] fun_Request_Package(byte by_command) {
		byte[] data = new byte[10];
		byte[] tmp = null;
		data[0] = 0x02;
		//附加数据长度
		tmp = TypeConvert.HexToAscii((byte) (0));
		data[1] = tmp[0];
		data[2] = tmp[1];
		// 命令
		tmp = TypeConvert.HexToAscii(by_command);
		data[3] = tmp[0];
		data[4] = tmp[1];
		//结果码
		tmp = TypeConvert.HexToAscii((byte) (0));
		data[5] = tmp[0];
		data[6] = tmp[1];
		//crc
		tmp = crc(data, 0, 7);
		data[7] = tmp[0];
		data[8] = tmp[1];

		data[9] = 0x03;
		return data;
	}
	//	]]

}
