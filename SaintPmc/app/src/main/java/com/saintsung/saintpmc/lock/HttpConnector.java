package com.saintsung.saintpmc.lock;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpConnector implements IHttpConnection {
	private URL url;
	private HttpURLConnection conn;
	private int state;
	private int result;
	private Thread readerThread;
	private ReadThread reader;
	private IListener listener;
	private InputStream input;
	private byte[] buffer;
	private static final int STATE_IDLE = 0;
	private static final int STATE_SETUP = 1;
	private static final int STATE_CONNECTED = 2;
	private static final int STATE_CLOSED = 3;
	private static final int STATE_PAUSED = 4;

	private static final int RESULT_UNKOWN = 0;
	private static int HttpConnect_i = 0;
	public static boolean HttpConnect_timeout = false;

	public HttpConnector() {
		this.url = null;
		reader = null;
		readerThread = null;
		listener = null;
		buffer = new byte[512];
		init();
	}

	public void init() {
		state = STATE_PAUSED;
		result = RESULT_UNKOWN;
	}

	public void send(byte[] bData) {
		// TODO Auto-generated method stub
	}

	public void send(byte[] bData, int off, int len) {
		// TODO Auto-generated method stub

	}

	public int read(byte[] bData) {
		// TODO Auto-generated method stub

		return 0;
	}

	public int read(byte[] bData, int off, int len) {
		// TODO Auto-generated method stub

		return 0;
	}

	public void open() {
		// TODO Auto-generated method stub
		try {
			conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("GET");
		} catch (Exception e) {
		}
	}

	public void start() {
		// TODO Auto-generated method stub
		state = STATE_SETUP;
		reader = new ReadThread();
		readerThread = new Thread(reader);
		readerThread.start();
	}

	public void stop() {
		// TODO Auto-generated method stub
		state = STATE_IDLE;
	}

	public void pause() {
		// TODO Auto-generated method stub
		state = STATE_PAUSED;
	}

	public void resume() {
		// TODO Auto-generated method stub
		state = STATE_SETUP;
	}
	public void close() {
		// TODO Auto-generated method stub
		try {
			state = STATE_CLOSED;
		} catch (Exception e) {
		}
	}
	//
	public void Register(IListener listener) {
		if (listener == null) {
			this.listener = null;
			return;
		} else if (listener instanceof IListener) {
			this.listener = listener;
		}
	}
	//
	public void setUrl(String url) {
		try {
			this.url = new URL(url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private class ReadThread implements Runnable {
		public synchronized void run() {
			while (true) {
				try {
					//update screw_log
					FileStream fileStream=new FileStream();
					if (state == STATE_SETUP) {
						conn.connect();
						conn = (HttpURLConnection ) url.openConnection();
						conn.setRequestMethod("GET");
						fileStream.fileStream(FileStream.screw_log, FileStream.write, ("HttpConnector+ReadThread+HttpURLConnection:"+url+"\r\n").getBytes());
						fileStream.fileStream(FileStream.screw_log, FileStream.write, ("HttpConnector+ReadThread+HttpURLConnection.getResponseCode():"+conn.getResponseCode()+"\r\n").getBytes());
						fileStream.fileStream(FileStream.screw_log, FileStream.write, ("HttpConnector+ReadThread+HttpURLConnection.getHeaderField():"+conn.getHeaderField("Content-Type").indexOf("text/vnd.wap.wml")+"\r\n").getBytes());


						if (conn.getResponseCode() == HttpURLConnection.HTTP_OK /*&& conn.getHeaderField("Content-Type").indexOf("text/vnd.wap.wml") == -1*/) {
							state = STATE_CONNECTED;
							fileStream.fileStream(FileStream.screw_log, FileStream.write, ("HttpConnector+ReadThread:"+state+"\r\n").getBytes());
						} else {
							//Thread.sleep(500);
							Thread.sleep(Integer.parseInt(new String(CommonResources.getParam("progress_interval"))));
							HttpConnect_i++;
							// 三秒后提示链接失败
							if (HttpConnect_i == 6){
								HttpConnect_i = 0;
								state = STATE_CLOSED;
								HttpConnect_timeout = true;
								fileStream.fileStream(FileStream.screw_log, FileStream.write, ("HttpConnector+ReadThread:"+HttpConnect_i+"\r\n").getBytes());
								fileStream.fileStream(FileStream.screw_log, FileStream.write, ("HttpConnector+ReadThread:"+state+"\r\n").getBytes());
							}
						}
					} else if (state == STATE_CONNECTED) {
						input = conn.getInputStream();
						int len = (int) conn.getContentLength();
						if (len > 0) {
							int actual = 0;
							int bytesread = 0;
							byte[] data = new byte[len];
							while ((bytesread != len) && (actual != -1)) {
								actual = input.read(buffer, bytesread, len - bytesread);
								bytesread += actual;
							}
							listener.dataComing(buffer);
							state = STATE_CLOSED;
						} else {
							int actual = 0;
							int ch = 0;
							while ((ch = input.read()) != -1) {
								buffer[actual++] = (byte) ch;
							}
							// input.close();
							// conn.close();
							// Thread.sleep(Integer.parseInt(new
							// String(CommonResources.getParam("progress_interval"))));
							state = STATE_CLOSED;
							listener.dataComing(buffer);
							break;
						}
						fileStream.fileStream(FileStream.screw_log, FileStream.write, ("HttpConnector+ReadThread:"+state+"\r\n").getBytes());
					} else if (state == STATE_CLOSED) {
						input.close();
						conn.disconnect();
						fileStream.fileStream(FileStream.screw_log, FileStream.write, ("HttpConnector+ReadThread:"+state+"\r\n").getBytes());
						break;
					} else if (state == STATE_PAUSED) {
						fileStream.fileStream(FileStream.screw_log, FileStream.write, ("HttpConnector+ReadThread:"+state+"\r\n").getBytes());
						//Thread.sleep(500);
						Thread.sleep(Integer.parseInt(new String(CommonResources.getParam("progress_interval"))));
					}
				} catch (Exception e) {
					continue;
				}
			}
		}
	}

//	private void echo(int bx, int by, int x, int y, String str) {
//		int fHeight = m_g.getFont().getHeight();
//		// m_g.copyArea(bx, by, 240, fHeight, x, y, Graphics.LEFT|Graphics.TOP);
//		m_g.drawString(str, x, y, Graphics.HCENTER | Graphics.TOP);
//		GUIController.m_mainMenu.flushGraphics();
//	}
}
