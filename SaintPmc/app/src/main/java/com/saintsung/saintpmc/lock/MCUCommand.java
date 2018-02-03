package com.saintsung.saintpmc.lock;

import android.os.Parcel;
import android.os.Parcelable;

//表示解包后mcu的命令
public class MCUCommand implements Parcelable {
	// [[wk
	public static final byte OPCODE_CONNECTED = (byte) 'l';
	// open_screw
	public static final byte opcode_open_screw = (byte) 'L';
	// wait key head fall into the unlock
	public static final byte OPCODE_WAIT_FALL_IN_UNLOCK = (byte) 'F';
	// wait key head fall into the lock
	public static final byte OPCODE_WAIT_FALL_IN_LOCK = (byte) 'f';
	// read lock sn
	public static final byte OPCODE_LOCKSN = (byte) 'S';
	// motor open lock
	public static final byte OPCODE_OPEN_LOCK = (byte) 'O';
	// motor open lock of 15bit
	public static final byte OPCODE_OPEN_LOCK15 = (byte) 'o';
	// motor close lock
	public static final byte OPCODE_CLOSE_LOCK = (byte) 'C';
	// close_screw
	public static final byte opcode_close_screw = (byte) 'c';
	public static final byte opcode_sleep = (byte) 'K';
	public static final byte opcode_close = (byte) 'k';
	public static final byte OPCODE_SENDLOCKNUMBERANDPASSWORD_PREFIX = (byte) 'X';
	public static final byte OPCODE_SENDLOCKNUMBERANDPASSWORD_SUFFIX = (byte) 'x';
	public static final byte OPCODE_OPEN_SCREW_BEFORE = (byte) 'E';
	public static final byte OPCODE_CLOSE_SCREW_BEFORE = (byte) 'e';
	public static final byte opcode_user_download = (byte) 'w';
	public static final byte s_prefix_send_user_download = (byte) 0x22;
	public static final byte s_suffix_send_user_download = (byte) 0x23;
	public static final String string_sleep = "string_sleep";
	public static final String string_close = "string_close";
	public static final String operatestate_unlock = "operatestate_unlock";
	public static final String operatestate_lock = "operatestate_lock";
	public static final String operatelock_succ = "0"; // 关锁成功
	public static final String operateunlock_succ = "1"; // 开锁成功
	public static final String operateunlock_fail = "2"; // 开锁失败
	public static final String operatelock_fail = "3"; // 关锁失败
	public static final String operatereset_succ="4"; //复位成功
	public static final String upload_log_serviceId = "L004";
	public static final String send_lock_start = "locknostart000";
	public static final String send_lock_end = "locknoend00000";
	public static final String send_lock_false = "用户离线关联锁具信息下传失败";
	public static final String send_lock_success = "条锁具信息下传成功结束!";
	public static final String reconnecting = "蓝牙自动断开,检查掌机是否关机";
	public static final String unlock_disabled = "您的权限不支持开此锁!";
	public static final String open_screw_prepare_false = "准备开启螺丝失败!";
	public static final String open_screw_prepare = "准备开启螺丝...";
	public static final String root_screw_disabled = "该用户未实时关联此螺丝!";
	public static final String open_screw_false = "手动开螺丝失败!";
	public static final String open_screw_success = "手动开螺丝成功!";
	public static final String close_screw_prepare_false = "准备关闭螺丝失败!";
	public static final String close_screw_prepare = "准备关闭螺丝...";
	public static final String close_screw_false = "关螺丝失败!";
	public static final String close_screw_success = "关螺丝成功!";
	public static final String open_valve_prepare_false = "准备开启阀门失败!";
	public static final String open_valve_prepare = "准备开启阀门...";
	public static final String open_valve_false = "开启阀门失败!";
	public static final String open_valve_success = "开启阀门成功!";
	public static final String close_valve_prepare_false = "准备关闭阀门失败!";
	public static final String close_valve_prepare = "准备关闭阀门...";
	public static final String close_valve_false = "关闭阀门失败!";
	public static final String close_valve_success = "关闭阀门成功!";
	public static final String warn_charge = "掌机电量不足,方便后续使用请及时充电!";
	public static final String close_lock_ok = "落锁成功: ";
	public static final String close_lock_fail = "落锁失败: ";
	public static final String close_lock_timout = "落锁超时: ";
	public static final String closelock_ok = "关锁成功: ";
	public static final String closelock_fail = "关锁失败: ";
	public static final String closelock_timout = "关锁超时: ";

	public static final String lock_type_online = "lock_type_online";
	public static final String lock_type_offline = "lock_type_offline";
	public static final String lock_type_local = "lock_type_local";
	public static final String lock_type_uploadSheet = "lock_type_uploadSheet";
	public static final String lock_type_screw = "lock_type_screw";

	// 新协议
	public static final byte App_Connect = (byte) 'l';
	// 擦除flash
	public static final byte EREASE_LOCK_KEY = (byte) 'E';
	// 开锁
	public static final byte App_Unlock = (byte) 'O';
	// 开锁状态
	public static final byte App_Unlock_Status = (byte) 'S';
	// 关锁
	public static final byte App_Lock = (byte) 'C';
	// 下载开锁码
	public static final byte DownloadKeyAndLock = (byte) 'D';
	// 上传开锁记录从掌机到手机端
	public static final byte AppUploadHistory = (byte) 'U';
	// 低电量
	public static final byte AppLowPower = (byte) 'M';
	// 休眠
	public static final byte AppSleep = (byte) 'K';
	// 唤醒
	public static final byte AppWakeUp = (byte) 'N';
	// 关锁成功
	public static final byte AppLock = (byte) 'T';
	//成功
	public static final byte AppReset=(byte)'Q';

	public static final String ERRCODE_UNKNOWN = "99";
	public static final String ERRCODE_SUCC = "00";
	public static final String ERRCODE_WrongDataLen = "07";
	public static final String ERRCODE_IncompletedPack = "08";
	public static final String ERRCODE_UnknownCmd = "09";
	public static final String ERRCODE_MotorTrap = "10";
	public static final String ERRCODE_MotorTimeout = "14";
	public static final String ERRCODE_MotorLose = "15";
	public static final String ERRCODE_MotorRunFail = "16";
	public static final String ERRCODE_OpenLockFail = "17";
	public static final String ERRCODE_ReadLockSnFail = "18";
	public static final String ERRCODE_MotorNotInited = "19";
	public static final String ERRCODE_MotorResetFail = "20";
	public static final String ERRCODE_Reset = "21";
	public static final String ERRCODE_DownNot = "22";
	public static final String ERRCODE_UpNot = "23";

	// ]]
	byte mCmd = 0;
	String mErrCode = "";// 0成功
	byte[] mAttechData = new byte[0];// 附加数据
	String mAttechString = ""; // 附加数据字符串
	int mUnlockType = 0; // 0表示正常开锁
	byte mCRC = 0;
	String mLockerNumber = "";// 解码后的锁号

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		arg0.writeByte(mCmd);
		arg0.writeString(this.mErrCode);
		arg0.writeByteArray(this.mAttechData);
		arg0.writeByte(this.mCRC);
		arg0.writeString(this.mLockerNumber);
	}

	public MCUCommand() {

	}

	private MCUCommand(Parcel in) {
		this.mCmd = in.readByte();
		this.mErrCode = in.readString();
		this.mAttechData = in.createByteArray();
		this.mCRC = in.readByte();
		this.mLockerNumber = in.readString();
	}

	public static final Creator<MCUCommand> CREATOR = new Creator<MCUCommand>() {
		public MCUCommand createFromParcel(Parcel in) {
			return new MCUCommand(in);
		}

		public MCUCommand[] newArray(int size) {
			return new MCUCommand[size];
		}
	};

}