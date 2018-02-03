package com.saintsung.saintpmc.lock;

public class CommonResources {
	public static final int requestTopLen=6;
	public static final int rtn_ser_id_len=4;
	public static final int rtn_flag_len=1;
	public static final int rtn_errcd_len=4;
	public static final int rtn_uid_len=16;
	public static final int rtn_time_len=14;
	public static final int rtn_sx_times_len=4;
	public static final int rtn_zq_times_len=4;
	public static final int rtn_lock_inrcode_len=9;
	public static final int rtn_lock_forcode_len=12;
	public static final int rtn_lock_key_len=15;
	public static final int rtn_ett_date_len=6;
	public static final int rtn_msg_len=3;
	public static final int rtn_lck_chg_flag_len =1;
	public static final int rtn_key_chg_flag_len =1;
	public static final int rtn_man_chg_flag_len =1;
	public static final String lck_chg_flag_true="2";//授权有变动，需要提示下载
	public static final String lck_chg_flag_false="1";//授权无变动，
	public static final String key_chg_flag_true="2";//有效
	public static final String key_chg_flag_false="1";//无效
	public static final String man_chg_flag_true="2";//有效
	public static final String man_chg_flag_false="1";//无效
	//in ms
	private static final int DEFAULT_NETWORK_PROGRESS_INTERVAL = 500;
	//[[wk before
//	private static final String DEFAULT_NETWORK_LOCK_AUTH_URL = "http://210.22.164.146/auth.php?";
//	private static final String DEFAULT_NETWORK_LOCK_AUTH_URL = "http://210.22.164.146/authAuthUser.php?";
//	private static final String DEFAULT_NETWORK_LOCK_AUTH_URL = "http://101.200.122.169/authAuthUser?";
//	private static final String DEFAULT_NETWORK_LOCK_AUTH_URL = "http://101.200.122.169/test/test.jsp?";
//	private static final String DEFAULT_NETWORK_LOCK_AUTH_URL = "http://192.168.1.118:8080/Test/test.jsp?";

	private static final String DEFAULT_NETWORK_LOCK_AUTH_URL = "http://210.22.164.146/authInfo.php?";	//设置类型




	// 规范请求数据:数据长度+数据+MD5检验码
	public static String createRequestPacket(String res) {
		String request = res;
		String md5Crc = MD5.toMD5(request);
		request += md5Crc;
		int len = request.length();
		String lenStr = len + "";
		//数据报文前6位为长度位,指明其后数据内容的长度.
		lenStr = getleftFill0Str(lenStr, requestTopLen);
		request = lenStr + request;
		return request;
	}

	/**
	 * 长度位数不足,左补0
	 */
	public static String getleftFill0Str(String rst, int len) {
		String rtn = rst;
		int rtnLen = rtn.length();
		String rrnLenStrLenLeft0 = "";
		for (int i = 0; i < len - rtnLen; i++) {
			rrnLenStrLenLeft0 += "0";
		}
		rst = rrnLenStrLenLeft0 + rst;
		return rst;
	}

	/**
	 * 用户名/密码长度不够10位,左补空格
	 */
	public static String getleftFillSpaceStr(String rst, int len) {
		String rtn = rst;
		for (int i = 0; i < len - rtn.length(); i++) {
			rst = " " + rst;
		}
		rtn = rst;
		return rtn;
	}

	/**
	 * 截取RTN_FLAG,返回标志,VARCHAR2(1),1表示成功,0表示失败;
	 */
	public static String getPacketRtnFlagByResult(String data) {
		return data.substring(requestTopLen+rtn_ser_id_len,requestTopLen+rtn_ser_id_len + rtn_flag_len);
		//return data.substring(6+4, 6+4+1);
	}

	/**
	 * 截取RTN_UID,成功登陆的用户ID,VARCHAR2(16),由服务器返还的用户ID,供业务操作验证使用;
	 */
	public static String getPacketRtnUidByResult(String data){
		return data.substring(requestTopLen+rtn_ser_id_len+rtn_flag_len+rtn_errcd_len, requestTopLen+rtn_ser_id_len+rtn_flag_len+rtn_errcd_len+rtn_uid_len);
		//return data.substring(6+4+1+4, 6+4+1+4+16);
	}
	/**
	 * 截取LOGIN_TIME,登录时间,VARCHAR2(14),成功则返回登录服务器时间YYYYMMDDhhmmss,24小时格式;
	 */
	public static String getPacketRtnLoginTimeByResult(String data){
		return data.substring(requestTopLen+rtn_ser_id_len+rtn_flag_len+rtn_errcd_len+rtn_uid_len, requestTopLen+rtn_ser_id_len+rtn_flag_len+rtn_errcd_len+rtn_uid_len+rtn_time_len);
	}
	/**
	 * 截取SX_TIMES,无信号自动失效时间分钟,VARCHAR2(4),数字(默认60=15*4分钟);
	 */
	public static String getPacketRtnSXTimesByResult(String data){
		try{
			return data.substring(requestTopLen+rtn_ser_id_len+rtn_flag_len+rtn_errcd_len+rtn_uid_len+rtn_time_len,requestTopLen+rtn_ser_id_len+rtn_flag_len+rtn_errcd_len+rtn_uid_len+rtn_time_len+rtn_sx_times_len);
		}catch (Exception e){
			return null;
		}
	}
	/**
	 * 截取ZQ_TIMES,周期检查时间分钟,VARCHAR2(4),数字(默认15分钟);
	 */
	public static String getPacketRtnZQTimesByResult(String data){
		try{
			return data.substring(requestTopLen+rtn_ser_id_len+rtn_flag_len+rtn_errcd_len+rtn_uid_len+rtn_time_len+rtn_sx_times_len,requestTopLen+rtn_ser_id_len+rtn_flag_len+rtn_errcd_len+rtn_uid_len+rtn_time_len+rtn_sx_times_len+rtn_zq_times_len);
		}catch (Exception e){
			return null;
		}
	}
	//LCK_CHG	锁具信息是否失效	VARCHAR2（1）	1为授权无变动，2授权有变动
	public static String getPacketRtnLckChgByResult(String data){
		//without MSG_CONT	消息	VARCHAR2(512)	以“|“进行分隔。		6+4+1+4+14+3 6+4+1+14+3+1
		return data.substring(requestTopLen+rtn_ser_id_len+rtn_flag_len+rtn_errcd_len+rtn_time_len+rtn_msg_len,requestTopLen+rtn_ser_id_len+rtn_flag_len+rtn_errcd_len+rtn_time_len+rtn_msg_len+rtn_lck_chg_flag_len);
	}
	//KEY _CHG	掌机是否失效	VARCHAR2（1）	1授权无效，2授权有效			6+4+1+4+14+3+1 	6+4+1+4+14+3+1+1
	public static String getPacketRtnKeyChgByResult(String data){
		return data.substring(requestTopLen+rtn_ser_id_len+rtn_flag_len+rtn_errcd_len+rtn_time_len+rtn_msg_len+rtn_lck_chg_flag_len,requestTopLen+rtn_ser_id_len+rtn_flag_len+rtn_errcd_len+rtn_time_len+rtn_msg_len+rtn_lck_chg_flag_len+rtn_key_chg_flag_len);
	}
	//MAN_CHG	人员是否失效	VARCHAR2（1）	1授权无效，2授权有效
	public static String getPacketRtnManChgByResult(String data){
		return data.substring(requestTopLen+rtn_ser_id_len+rtn_flag_len+rtn_errcd_len+rtn_time_len+rtn_msg_len+rtn_lck_chg_flag_len+rtn_key_chg_flag_len,requestTopLen+rtn_ser_id_len+rtn_flag_len+rtn_errcd_len+rtn_time_len+rtn_msg_len+rtn_lck_chg_flag_len+rtn_key_chg_flag_len+rtn_man_chg_flag_len);
	}
	//
	public static byte[] getParam(String param_name) {

		byte[] value = null;
		try {
			if (param_name.equals("progress_interval")) {
				value = Integer.toString(DEFAULT_NETWORK_PROGRESS_INTERVAL).getBytes();
			} else if(param_name.equals("lock_auth_url")){
				value = DEFAULT_NETWORK_LOCK_AUTH_URL.getBytes();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}







	/**
	 * 截取RTN_ERRCD,错误代号,VARCHAR2(4),验证错误的错误代号,字母/数字;
	 */
	public static String getPacketRtnErrcdByResult(String data){
		return data.substring(requestTopLen+rtn_ser_id_len+rtn_flag_len, requestTopLen+rtn_ser_id_len+rtn_flag_len+rtn_errcd_len);
		//return data.substring(6+4+1, 6+4+1+4);
	}

	/**
	 E001 服务器接收到的报文为空
	 E002 报文头标识长度与报文实际长度不一致
	 E003 用户身份验证不通过
	 E004 接口编码不正确
	 E005 MD5校验不通过
	 E006 这个先不管
	 E007 其他
	 */
	public static String getErrContByErrcd(String errcd) {
		String res="";
		if (errcd.equals("E001")) {
			res = "服务器接收到的报文为空";
		} else if (errcd.equals("E002")) {
			res = "报文头标识长度与报文实际长度不一致";
		} else if (errcd.equals("E003")) {
			res = "用户身份验证不通过";
		} else if (errcd.equals("E004")) {
			res = "接口编码不正确";
		} else if (errcd.equals("E005")) {
			res = "MD5校验不通过";
		} else if (errcd.equals("E006")) {
			res = "这个先不管";
		} else if (errcd.equals("E007")) {
			res = "其他";
		}
		return res;
	}



}
