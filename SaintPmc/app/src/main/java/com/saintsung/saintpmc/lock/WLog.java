package com.saintsung.saintpmc.lock;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;



public class WLog {

	static SimpleDateFormat sLogDateFmt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	public static void logFile(String msg) throws IOException{
		Date d = new Date();
		SimpleDateFormat logFileNameFmt = new SimpleDateFormat("yyyyMMdd");
		String logFileName = logFileNameFmt.format(d);
		msg = String.format("%s:%s\n",sLogDateFmt.format(d), msg);
		RandomAccessFile raf = null;
		try{
			String path = new FileStream().catalog;
			logFileName = String.format("%s/%s.txt", path,logFileName);
			raf = new RandomAccessFile(logFileName,"rw");
			raf.seek(raf.length());
			//change before
//			raf.write((msg).getBytes());
			//[[wk change
			raf.write((msg+"\r\n").getBytes());
			//]]
		}finally{
			if(raf != null){
				raf.close();
			}
		}
	}
//	static WThread_OLD sThread = null;
//	static void init()throws Exception{
//		sLogUrl = new URL("http://www.weforpay.com/rlog");
//		sThread = new WThread_OLD(null);
//		sThread.start();		
//	}
//	
//	static URL sLogUrl = null;
//	public static void rlog(final String msg){
//		sThread.Post(new Runnable(){
//
//			@Override
//			public void run() {
//				try {
//					HttpURLConnection conn = (HttpURLConnection) sLogUrl.openConnection();
//					conn.setRequestMethod("POST");// 提交模式
//			        conn.setDoOutput(true);// 是否输入参数
//			        byte[] bypes = msg.toString().getBytes();
//			        OutputStream o = conn.getOutputStream();
//			        o.write(bypes);// 输入参数		
//			        o.flush();
//			        conn.disconnect();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			
//		},0);
//	}
}
