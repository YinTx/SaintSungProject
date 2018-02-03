package com.saintsung.saintpmc.lock;

import android.content.Intent;

import com.saintsung.saintpmc.MainActivity;
import com.saintsung.saintpmc.lock.WLog;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class SocketConnect {
	public static String socketAddress = "socketAddress";
	public static String socketPort = "socketPort";
	public static String socketError = "socketError";
	String string, stringSocketAddress, responsePacket;
	int intSocketPort;
	byte[] byteArray;
	String[] stringArray;
	Intent intent = new Intent();
	FileStream fileStream = new FileStream();

	// 连接服务器,发送登陆/下载信息.
	public String sendDate(String requestPacket) {
		// get socket

		byteArray = fileStream.fileStream(FileStream.socket, FileStream.read, null);
		if (byteArray == null || byteArray.length == 0) {
			return null;
		} else {

			Socket socket = null;
			DataOutputStream out = null;
			DataInputStream in = null;
			string = new String(byteArray);
			stringArray = string.split(";");
			// stringSocketAddress="210.22.164.146";
			stringSocketAddress = stringArray[0];
			// intSocketPort=7013;
			intSocketPort = Integer.parseInt(stringArray[1]);
			try {
				// 应用Server的IP和端口建立Socket对象
				// socket = new Socket(stringSocketAddress,intSocketPort);
				// [[CXQ
				socket = new Socket();

				SocketAddress socketAddress = new InetSocketAddress(stringSocketAddress, intSocketPort);
				socket.connect(socketAddress, 10000);

				// ]]
				// 将信息通过这个对象来发送给Server
				out = new DataOutputStream(socket.getOutputStream());
				out.writeUTF(requestPacket);
				out.flush();
				// 读取服务器端数据
				in = new DataInputStream(socket.getInputStream());
				if (MainActivity.step == 1) {
					int length = in.available();
					byte[] dst = new byte[length];
					in.readFully(dst);

				}
				responsePacket = in.readUTF();
				return responsePacket;
			} catch (Exception e) {
				// DataInputStream/SocketTimeoutException/IOException
				e.printStackTrace();
				responsePacket = "服务器异常：" + e.toString();
				try {
					WLog.logFile(responsePacket);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				return responsePacket;
			} finally {
				// 因数据量可能偏大先保存到临时文件中
				try {
					if (in != null)
						in.close();
					if (out != null)
						out.close();
					if (socket != null)
						socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.gc();
			}
		}
	}
	/*
	 * //连接服务器,发送登陆信息. private void sendDate(){ try { //应用Server的IP和端口建立Socket对象
	 * socket = new Socket("210.22.164.146", 7013); //将信息通过这个对象来发送给Server out =
	 * new DataOutputStream(socket.getOutputStream());
	 * out.writeUTF(requestPacket); out.flush(); //读取服务器端数据 in = new
	 * DataInputStream(socket.getInputStream()); responsePacket=in.readUTF();
	 * //因数据量可能偏大先保存到临时文件中 } catch (UnknownHostException e) {
	 * e.printStackTrace(); } catch (SocketTimeoutException e) {
	 * e.printStackTrace(); } catch (IOException e) { e.printStackTrace();
	 * }catch(Exception e){ e.printStackTrace(); }finally{ try{ if(in!=null)
	 * in.close(); if(out!=null) out.close(); if(socket!=null) socket.close();
	 * System.gc(); }catch(Exception e){ //待解疑问:关闭输入输出流后还能捕获抛异常么
	 * e.printStackTrace(); } } }
	 */

}
