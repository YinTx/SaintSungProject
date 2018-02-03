package com.saintsung.saintpmc.lock;

public class TypeConvert {

	public static class A2HCvtRtnT {
		public boolean rlt;
		public byte val;
	}

	//
	public static A2HCvtRtnT AsciiToHex(byte bDataHigh, byte bDataLow) {
		A2HCvtRtnT rtn = new A2HCvtRtnT();
		int iValue = 0;

		if ((bDataHigh >= 0x30) && (bDataHigh <= 0x39)) {

			iValue = bDataHigh - 0x30;
		} else if ((bDataHigh >= 'A') && (bDataHigh <= 'F')) {
			iValue = bDataHigh - 'A' + 10;
		} else {
			rtn.rlt = false;
			return rtn;
		}
		iValue = iValue & 0x0F;
		iValue = iValue << 4;
		if ((bDataLow >= 0x30) && (bDataLow <= 0x39)) {
			iValue += bDataLow - 0x30;
		} else if ((bDataLow >= 'A') && (bDataLow <= 'F')) {
			iValue += bDataLow - 'A' + 10;
		} else {
			rtn.rlt = false;
			return rtn;
		}
		rtn.rlt = true;
		rtn.val = (byte) iValue;
		return rtn;
	}

	//
	public static byte[] HexToAscii(byte bValue) {
		byte[] bData = new byte[2];
		int iValue = bValue & 0xFF;
		bData[0] = (byte) (iValue / 16);
		bData[1] = (byte) (iValue % 16);
		if (bData[0] < 10) {
			bData[0] = (byte) (bData[0] + 0x30);
		} else {
			bData[0] = (byte) (bData[0] + 'A' - 10);
		}
		if (bData[1] < 10) {
			bData[1] = (byte) (bData[1] + 0x30);
		} else {
			bData[1] = (byte) (bData[1] + 'A' - 10);
		}
		return bData;
	}

	//
	public static int bigEndian_byte2int(byte[] bbyte, int index) {
		int iValue = 0;
		iValue = ((bbyte[index] << 24) & 0xFF000000)
				| ((bbyte[index + 1] << 16) & 0x00FF0000)
				| ((bbyte[index + 2] << 8) & 0x0000FF00)
				| ((bbyte[index + 3]) & 0x000000FF);
		return iValue;
	}

	//
	public static byte[] bigEndian_int2byte(int n) {
		byte[] bArray = new byte[4];
		bArray[0] = (byte) (n >> 24);
		bArray[1] = (byte) (n >> 16);
		bArray[2] = (byte) (n >> 8);
		bArray[3] = (byte) n;
		return bArray;
	}
	//
	public static byte[] lockNumber(int n) {
		byte[] bArray = new byte[4];
		bArray[0] = (byte) (n >> 24);
		bArray[1] = (byte) (n >> 16);
		bArray[2] = (byte) (n >> 8);
		bArray[3] = (byte) (n &0x000000FF);
		return bArray;
	}
	//
	public static String byte2hex(byte b){
		String hexStr = Integer.toHexString(b&0x000000FF);
		if(hexStr.length()==1)
			return "0"+hexStr;
		else
			return hexStr;
	}



}
