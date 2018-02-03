package com.saintsung.saintpmc.lock;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.util.Log;

public class User_Share {
	public String MY_PREFS = "SAVEINFO";
	public String MY_CLASS = "MY_CLASS";

	public String class_LockerProcessAtivity = "LockerProcessAtivity";
	public String class_SetAllActivity = "SetAllActivity";
	public String class_SetAllActivity0 = "SetAllActivity0";
	//	public String def_DeviceName = "DeviceName";
	public String def_DeviceAddress = "DeviceAddress";
	public String def_AutoConnect = "AutoConnect";

	public int i_Auto = 0;

	public static final int i_OpenLock = 1;
	public static final int i_UnOpenLock = 0;
	public static int i_contact_lock = 0;			//0表示正常锁，1表示接触式开锁
	public  int m_OpenLock = 1;						//0表示不开锁，1开锁
	boolean mOpen = false;


	//薷Command cmd
	public static int i_Change_Cmd = 0;
	public User_Share()
	{

	}

	/**
	 * 将字符串转成MD5值
	 *
	 * @param string
	 * @return
	 */
	public  String byteToMD5(String data) {
		byte[] hash;
		int i ;
		byte[] by_data = 	data.getBytes();

		StringBuffer buf = new StringBuffer();
		try {
			MessageDigest bmd5 = MessageDigest.getInstance("MD5");
			bmd5.update(by_data);
			hash = bmd5.digest();
			for(int offset = 0;offset<hash.length;offset++)
			{
				i = hash[offset];
				if(i<0)
				{
					i+=256;
				}
				if (i<16) {
					buf.append("0");
				}
				buf.append(Integer.toHexString(i));
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}

		StringBuilder hex = new StringBuilder(hash.length * 2);
		for (byte b : hash) {
			if ((b & 0xFF) < 0x10)
				hex.append("0");
			hex.append(Integer.toHexString(b & 0xFF));
		}
		Log.d("md5", "md5"+hex.toString());

		return buf.toString();
	}




	/**
	 * 将字符串转成MD5值
	 *
	 * @param string
	 * @return
	 */
	public  String byteToMD5(byte[] by_data) {
		byte[] hash;
		int i ;
		StringBuffer buf = new StringBuffer();
		try {
			MessageDigest bmd5 = MessageDigest.getInstance("MD5");
			bmd5.update(by_data);
			hash = bmd5.digest();
			for(int offset = 0;offset<hash.length;offset++)
			{
				i = hash[offset];
				if(i<0)
				{
					i+=256;
				}
				if (i<16) {
					buf.append("0");
				}
				buf.append(Integer.toHexString(i));
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}

		StringBuilder hex = new StringBuilder(hash.length * 2);
		for (byte b : hash) {
			if ((b & 0xFF) < 0x10)
				hex.append("0");
			hex.append(Integer.toHexString(b & 0xFF));
		}
		Log.d("md5", "md5"+hex.toString());

		return buf.toString();
	}

}
