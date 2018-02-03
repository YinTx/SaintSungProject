package com.saintsung.saintpmc.lock;


public class CommandPacker {
	private byte m_bLen;
	private byte m_bOpcode;
	private byte m_bErrorType;
	private byte[] m_bData;
	private byte[] encodebData;
	private byte[] bTmp;
	public static boolean succ_flag = false;
	public static final byte s_bPrefix = (byte) 0x02;
	public static final byte s_bSuffix = (byte) 0x03;
	public static final byte s_bPrefix0 = (byte) 0x23;
	public static final byte send_packet_fix_len0 = (byte) 2;
	public static final byte send_packet_fix_len1 = (byte) 5;
	public static final byte SEND_PACKET_FIX_LEN = (byte) 8;
	public static final byte RECV_PACKET_FIX_LEN = (byte) 10;
	// motor open lock
	public static final byte OPCODE_OPEN_LOCK = (byte) 'O';
	// motor open lock of 15bit
	public static final byte OPCODE_OPEN_LOCK15 = (byte) 'o';
	public static final byte opcode_requestPwd = (byte) 'v';
	public static final byte opcode_userRelationPrefix = (byte) 'W';
	public static final byte opcode_userRelationSuffix = (byte) 'p';
	public static final String encode_openLock="openLockPwd";
	public static final String encode_connectBluetoothPwd="connectBluetoothPwd";
	public static final String encode_userRelationFix="userRelationFix";
	public static final String encode_userRelation="userRelation";
	public static final String state_openLock="openLock";
	public static final String state_userRelation="userRelation";
	private int crc_1byte=0;
	private byte byteFinish;



	public int decodePacket(String state,byte[] bData, int iOffset) {
		//判断执行
		if (state.equals(state_openLock)) {
			TypeConvert.A2HCvtRtnT cvt;
			int iLen;
			if (bData == null) {
				return -1;
			}
			if (iOffset < 0) {
				return -2;
			}
			// check prefix
			if (bData[iOffset] != s_bPrefix) {
				return -3;
			}
			// check is completed
			cvt = TypeConvert.AsciiToHex(bData[iOffset + 1], bData[iOffset + 2]);
			if (cvt.rlt)
				iLen = cvt.val;
			else
				return -7;
			if (iLen % 2 != 0) {
				return -4;
			}
			if (iOffset + iLen + RECV_PACKET_FIX_LEN > bData.length) {
				return -5;
			}
			// check suffix
			if (bData[iOffset + RECV_PACKET_FIX_LEN + iLen - 1] != s_bSuffix) {
				return -6;
			}
			// decode data
			cvt = TypeConvert.AsciiToHex(bData[iOffset + 3], bData[iOffset + 4]);
			if (cvt.rlt)
				m_bOpcode = cvt.val;
			else
				return -7;
			cvt = TypeConvert.AsciiToHex(bData[iOffset + 5], bData[iOffset + 6]);
			if (cvt.rlt)
				m_bErrorType = cvt.val;
			else
				return -7;
			if (iLen != 0) {
				m_bData = new byte[iLen / 2];
				for (int i = 0; i < m_bData.length; i++) {
					cvt = TypeConvert.AsciiToHex(bData[iOffset + 7 + 2 * i],
							bData[iOffset + 8 + 2 * i]);
					if (cvt.rlt)
						m_bData[i] = cvt.val;
					else
						return -7;
				}
			} else
				m_bData = null;
			return iOffset + RECV_PACKET_FIX_LEN + iLen;
		} else if (state.equals(state_userRelation)) {
			//
			for (int i = 0; i < bData.length; i++) {
				if (i>18 && i<bData.length-32) {
					int j=0;
					bData[j]=bData[i];
				} else {

				}
			}
			return 0;
		} else {
			//
			return 0;
		}
	}

	//
	public boolean setPacketParam(byte bOpcode, byte[] bData, String state) {
		m_bOpcode = bOpcode;
		m_bData = bData;
		if (bData == null)
			m_bLen = 0;
		else
			//Validate
			if (state==null) {
				m_bLen = (byte)(bData.length);
			} else {
				if (state.equals(encode_openLock)) {
					//encode_openLock
					m_bLen = (byte) (bData.length * 2);
				}else {
					//
					m_bLen = (byte)(bData.length);
				}
			}
		return true;
	}

	/**
	 * EnPacket
	 *
	 * @return reference to EnPacket byte stream
	 */
	public byte[] encodePacket(String state) {
		int idx = 0;
		//validate state
		if (state==null) {
			state="";
		}
		//validate prefix and length
		if (state.equals(encode_userRelation)) {
			//"用户关联"
			encodebData = new byte[m_bLen + send_packet_fix_len1];
			encodebData[idx++] = s_bPrefix0;
		} else if (state.equals(UpS00Activity.prefixUpS00)) {
			//prefixUpS00
			encodebData = new byte[m_bLen];
		} else if (state.equals(UpS00Activity.dataUpS00)) {
			//dataUpS00
			encodebData = new byte[m_bLen+1];
		}  else {
			//
			encodebData = new byte[m_bLen + SEND_PACKET_FIX_LEN];
			encodebData[idx++] = s_bPrefix;
		}
		//validate lengthValue
		if (state.equals(UpS00Activity.prefixUpS00)) {
			//prefixUpS00
		} else if (state.equals(UpS00Activity.dataUpS00)) {
			//dataUpS00
			encodebData[idx++] = (byte)(m_bLen);
		} else {
			bTmp = TypeConvert.HexToAscii(m_bLen);
			for (int i = 0; i < 2; i++) {
				encodebData[idx++] = bTmp[i];
			}
		}
		//validate sign
		if (state.equals(encode_userRelation)||state.equals(UpS00Activity.prefixUpS00)||state.equals(UpS00Activity.dataUpS00)) {
			//encode_userRelation and
		} else {
			//
			bTmp = TypeConvert.HexToAscii(m_bOpcode);
			for (int i = 0; i < 2; i++)
				encodebData[idx++] = bTmp[i];
		}
		//validate sendData
		if (state.equals(encode_openLock)) {
			//开锁
			for (int i = 0; i < m_bLen / 2; i++) {
				bTmp = TypeConvert.HexToAscii(m_bData[i]);
				for (int j = 0; j < 2; j++)
					encodebData[idx++] = bTmp[j];
			}
		} else {
			//
			for (int i = 0; i < m_bLen; i++) {
				encodebData[idx++] = m_bData[i];
			}
		}
		//validate lengthValidateValue
		if (state.equals(UpS00Activity.prefixUpS00)||state.equals(UpS00Activity.dataUpS00)) {
			//upS00
		} else {
			//must clear cache
			byteFinish=0;
			for(int i=0;i<encodebData.length-3;i++){
				check(i, encodebData);
				byteFinish=(byte)(crc_1byte^byteFinish);
			}
			bTmp = TypeConvert.HexToAscii(byteFinish);
			for (int i = 0; i < 2; i++)
				encodebData[idx++] = bTmp[i];
		}
		//validate suffix
		if (state.equals(encode_userRelation)||state.equals(UpS00Activity.prefixUpS00)||state.equals(UpS00Activity.dataUpS00)) {
			//"用户关联"
		} else {
			//
			encodebData[idx++] = s_bSuffix;
		}
		return encodebData;
	}
	//createLengthValidateValue
	public int check(int i, byte[] encodebData) {
		crc_1byte=0;
		if (encodebData[i]<0) {
			int negative=encodebData[i]&0x7f|(1<<7);
			crc_1byte = crc_1byte ^ negative;
		} else {
			crc_1byte = crc_1byte ^ encodebData[i];
		}
		for(int j = 0; j < 8; j++){
			if((crc_1byte&0x01)==1){
				//odd
				crc_1byte = crc_1byte^0x31;
			}
			crc_1byte>>=1;
		}
		return crc_1byte;
	}
	//RTN_FLAG	返回标志	VARCHAR2 (1)	1表示成功，0表示失败
	//RTN_ERRCD	错误代号	VARCHAR2 (4)	验证错误的错误代号，字母/数字
	//RTN_UID	成功登陆的用户ID	VARCHAR2(16)	由服务器返还的用户ID，供业务操作验证使用
	//LOGIN_TIME	登录时间	VARCHAR2(14)	成功则返回登录服务器时间YYYYMMDDhhmmss,24小时格式
	//SX_TIMES	无信号自动失效时间分钟	VARCHAR2(4)	数字(默认60分钟)
	//ZQ_TIMES	周期检查时间分钟	VARCHAR2(4)	数字(默认15分钟)
	//数据长度+数据+MD5检验码
	public void decodeResultFlag(String response){
		String rtn_flag=CommonResources.getPacketRtnFlagByResult(response);
		if(rtn_flag.equals("0")){
			succ_flag=false;
		}else if(rtn_flag.equals("1")){
			succ_flag = true;
		} else {
			succ_flag = false; //不等于0,也不等于1
		}
	}
	/**
	 * Get Data
	 *
	 * @return Reference to byte stream
	 */
	public byte[] getData() {
		return m_bData;
	}

	public byte getCmdType() {
		return m_bOpcode;
	}

	public byte getErrorType() {
		return m_bErrorType;
	}

}
