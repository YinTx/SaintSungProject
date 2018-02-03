package com.saintsung.saintpmc.lock;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import com.saintsung.saintpmc.HexUtil;
import com.saintsung.saintpmc.MainActivity;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.Semaphore;




public class BleDeviceMgr implements LeScanCallback{
	private final String TAG = "BleDeviceMgr";
	public interface DeviceClient{
		void onConnected();
		void onDisconnected();
		void onError(Exception e);
		void onRecv(byte[] data);
		void onRecv(String data);
	}
	public interface ScanCallback{
		void onScan(String address, String name);
	}


	public class Device extends BluetoothGattCallback{

		String mAddress = "";
		String mName = "";
		DeviceClient mDeviceClient = null;
		BluetoothGatt mBluetoothGatt = null;
		Thread mThread = null;
		Handler mHandler = null;
		boolean mDisconnecting = false;
		boolean mWriteTimeout = false;
		final int MAX_TRY = 3;//最重试次数
		//CXQ
//	    /*
		public final UUID uuid_service_read = UUID.fromString("00001910-0000-1000-8000-00805f9b34fb");
		public final UUID uuid_service_write = UUID.fromString("00001910-0000-1000-8000-00805f9b34fb");
		public final UUID uuid_characteristic_read = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb");
		public final UUID uuid_characteristic_write = UUID.fromString("0000FFF2-0000-1000-8000-00805f9b34fb");
		public final UUID uuid_CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
		//	    */
		BluetoothGattCharacteristic mCharacteristicRead = null,mCharacteristicWrite = null,bluetoothGattCharacteristicSet,bluetoothGattCharacteristicBaudRate;

		// [[ CXQ
		StringBuffer stringBuffer = new StringBuffer();
		ArrayList<Byte> m_list = new ArrayList<>();

		byte by_cmd = 0;
		int m_Total = -1;
		int m_index = -1;
		int i_start = -1;
		int i_end = -1;
		private boolean mConnected = false;
		// 收到单片机要求重发。
		private final byte[] by_Recv_Request_Data = new byte[] { 0x02, 0x30, 0x30, 0x35, 0x32, 0x46, 0x46, 0x31, 0x31, 0x03 };
		// 重发数据的字符串
		private String str_Resend_String = new String();
		// 重发的数据
		private byte[] by_Resend_data = new byte[] {};

		private final byte[] by_Recv_Data = new byte[] {};

		private Timer m_timer;


		//]]

		ArrayList<byte[]> mPendingDataList = new ArrayList<byte[]>();
		//最底层20个字节为一组的缓存缓冲，即拆包缓冲
		ArrayList<byte[]> mSendPending = new ArrayList<byte[]>();
		//已经重试次数
		int mSendTrying = 0;
		//最底层20个字节为一级的接收缓冲，组包缓冲
		ArrayList<byte[]> mRecvPending = new ArrayList<byte[]>();
		int mRecvLeftLen = 0;//剩余数据长度
		int mRecvTotall = 0;//大包的总长度
		public void SetClient(DeviceClient client){
			synchronized(this){
				mDeviceClient = client;
			}
		}
		public boolean Connect(){
			synchronized(this){
				if(this.mAddress.length() == 0){
					return false;
				}
				if(mBluetoothGatt != null){
					return true;
				}
			}
			final Semaphore semp = new Semaphore(1);
			try {
				semp.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.mThread = new Thread(new Runnable(){

				@Override
				public void run() {
					Looper.prepare();
					Device.this.mHandler = new Handler();
					semp.release();
					Looper.loop();
				}
			});
			if(this.mThread == null){
				return false;
			}
			this.mThread.setName(this.mAddress);
			this.mThread.setDaemon(true);
			this.mThread.start();
			try {
				semp.acquire();
				semp.release();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.mHandler.post(new Runnable(){

				@Override
				public void run() {
					Device.this.connectDevice();
				}
			});
			return true;
		}
		//内部使用，连接设备
		void connectDevice(){
//			/*
			//[[wk
			//onClientConnectionState() - status=133 clientIf=5
			if (mBluetoothGatt != null) {
				Disconnect();
				/*
				mBluetoothGatt.disconnect();
				mBluetoothGatt.close();
				mBluetoothGatt = null;
				*/
			}
			//]]
//			*/
			BluetoothManager bluetoothManager=(BluetoothManager)BleDeviceMgr.this.mContext.getSystemService(Context.BLUETOOTH_SERVICE);
			BluetoothAdapter adapter = bluetoothManager.getAdapter();
			BluetoothDevice  device = adapter.getRemoteDevice(Device.this.mAddress);

			BluetoothGatt gatt = device.connectGatt(BleDeviceMgr.this.mContext, false, Device.this);
			synchronized(this){
				mBluetoothGatt = gatt;
			}
		}
		public void Disconnect(){
			if(this.mHandler == null){
				return ;
			}
			this.mHandler.post(new Runnable(){
				@Override
				public void run() {
					if (mBluetoothGatt == null) {
						return;
					}
					mDisconnecting = true;
					try {
						mBluetoothGatt.disconnect();
						//	mBluetoothGatt.close();
						if(mBluetoothAdapter.isEnabled())
						{
							//		 mBluetoothAdapter.disable();		//关闭蓝牙
						}
					} catch (Exception e) {
					}

					// 3秒没有断开强行断开
					Device.this.PostRun(new Runnable() {
						@Override
						public void run() {
							DeviceClient client = null;
							synchronized(this){
								client = mDeviceClient;
							}
							if(client != null){
								client.onDisconnected();
								synchronized(this){
									mDeviceClient = null;
								}
								quitThread();
							}
						}
					}, 2000);

				}
			});

		}

		//发送数据
		//data length can >= 20 bytes
		public boolean Write(String string,byte[] data){
			BluetoothGatt gatt = null;
			BluetoothGattCharacteristic charWrite = null;

			synchronized(this){
				gatt = mBluetoothGatt;
				charWrite = this.mCharacteristicWrite;
			}
			if(gatt == null){
				return false;
			}
			if(charWrite == null){
				return false;
			}
			if (!mConnected) {
				return false;
			}
//[[保存副本，以便重发
			//加上头尾检测避免影响下载JSON文件时出错。
			str_Resend_String = string;
			by_Resend_data = data;
			if ((data[0]== 0x02 && data[data.length-1]== 0x03 && data[3] == 0x36 && data[4] == 0x46) ||(data[0]== 0x02 && data[data.length-1]== 0x03 && data[3] == 0x35 && data[4] == 0x31) /*O 0x6F o 0x51*/) {
				//分两条发
				fun_OpenChain(string,data);

				return true;
			}

			// ]]

			synchronized (this) {
				if(mSendPending.size() > 0){
					mSendPending.clear();
				}


				if (mSendPending.size() > 0) {
					// 上一个包还没有发完,放入队尾等待发送
					mPendingDataList.add(data);
				} else {
					// 上一个包已经发送完成，可以发送
					// 先放入队尾，再拆成20个字节的子包发送
					this.mPendingDataList.add(data);
					Device.this.ContinueSendList(string);
				}
			}
			return true;
		}
		//开锁命令发两次发送
		public void fun_OpenChain(String string,byte[] data)
		{
			BluetoothGatt gatt = null;
			BluetoothGattCharacteristic cWrite = null;

			boolean success_first = false;
			//		 boolean success_second = false;

			gatt = this.mBluetoothGatt;
			cWrite = this.mCharacteristicWrite;


			if(gatt == null){
				//[[wk add
				mDeviceClient.onError(new Exception("gatt="+gatt));
				try {
					string+="---gatt="+gatt;
					WLog.logFile(string);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//]]

			}

			StringBuilder stringBuilder = new StringBuilder();

//[[CXQ
			byte[]first = new byte[20];
			System.arraycopy(data, 0, first, 0, 20);
			if (mConnected) {
				cWrite.setValue(first);
				success_first = gatt.writeCharacteristic(cWrite);

				for(int i = 0;i<first.length;i++)
				{
					stringBuilder.append(String.format("%02x ", first[i]));
				}
//	    	Log.d("first", "first"+stringBuilder.toString()+" "+stringBuilder.length());

				try {
					WLog.logFile("first "+ String.valueOf(success_first) +stringBuilder.toString()+" "+stringBuilder.length());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}else{
				try {
					WLog.logFile("Sending"+"蓝牙已经断开发送失败");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (!success_first) {		//如果失败了，重新发
				cWrite.setValue(first);
				success_first = gatt.writeCharacteristic(cWrite);
			}else
			{
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				stringBuilder.delete(0, stringBuilder.length()) ;

				int i_second_lenght = data.length-20;
				byte[]second = new byte[i_second_lenght];
				System.arraycopy(data, 20, second, 0, i_second_lenght);
				if (mConnected) {
					cWrite.setValue(second);
					boolean success_second = 	gatt.writeCharacteristic(cWrite);

					for(int i = 0;i<second.length;i++)
					{
						stringBuilder.append(String.format("%02x ", second[i]));
					}
//	    		Log.d("first", "first 222"+stringBuilder.toString()+" "+stringBuilder.length());
					try {
						WLog.logFile("second "+ success_second +stringBuilder.toString()+" "+stringBuilder.length());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (!success_second) {
						cWrite.setValue(second);
						success_second = gatt.writeCharacteristic(cWrite);
					}


				}else{
					try {
						WLog.logFile("Sending"+"蓝牙已经断开发送失败");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			//保存以便下次重发
			str_Resend_String = string;
			by_Resend_data = data;

		}

		public void ClearSendList(){
			synchronized(this){
				this.mPendingDataList.clear();
			}
		}
		//继续发送队列里的数据
		public void ContinueSendList(final String string){
			this.mHandler.post(new Runnable(){
				@Override
				public void run() {
					Device.this.splitPacket();
					Device.this.sendSubPacket(string,1,0);
				}
			});
		}
		public String GetAddress(){
			String address = "";
			synchronized(this){
				address = this.mAddress;
			}
			return address;
		}
		public String GetName(){
			String name;
			synchronized(this){
				name = this.mName;
			}
			return name;
		}
		public boolean PostRun(Runnable runner){
			return PostRun(runner,0);
		}
		public boolean PostRun(Runnable runner,long ms){
			synchronized(this){
				if(this.mHandler != null){
					this.mHandler.postDelayed(runner, ms);
					return true;
				}
			}
			return false;
		}
		public boolean RemoveRunner(Runnable runner){
			synchronized(this){
				if(this.mHandler != null){
					this.mHandler.removeCallbacks(runner);
					return true;
				}
			}
			return false;
		}
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt,int status,int newState) {
			Log.d("BluetoothGatt","BluetoothGatt "+"onConnectionStateChange");
			if (newState == BluetoothProfile.STATE_CONNECTED) {
//[[cxq
				mConnected = true;
				MainActivity.state_sleep=null;
//]]
				this.PostRun(new Runnable(){
					@Override
					public void run() {
						Device.this.mBluetoothGatt.discoverServices();
					}
				});
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

				//	refreshDeviceCache();
//[[cxq
				mConnected = false;

				Disconnect();
/*
				mBluetoothGatt.disconnect();
				DeviceClient client = null;
				synchronized(this) {
					client = mDeviceClient;
				}
					if(client != null) {
						client.onDisconnected();
						//[[wk add
						try {
							WLog.logFile("接收到蓝牙模组返回的断开连接状态");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				synchronized(this){
					mDeviceClient = null;
				}
*/

/*		20170913
//]]
            	//如果不是调用disconnect引起的，则自动重启系统蓝牙并重新连接
            	this.mHandler.post(new Runnable(){
					@Override
					public void run() {
						DeviceClient client = null;
						synchronized(this){
							client = mDeviceClient;
						}
						if(client != null){
							client.onDisconnected();
							//[[wk add
							try {
								WLog.logFile("接收到蓝牙模组返回的断开连接状态");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							//]]
    						synchronized(this){
    							mDeviceClient = null;
    						}
						}
						quitThread();
					}
				});
            }

			//[[wk add
            else if (newState==133) {
				try {
					WLog.logFile("onConnectionStateChange newState="+newState);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	Disconnect();
		        BluetoothManager bluetoothManager = (BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
		        final BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
		        bluetoothAdapter.disable();
		        try {
					Thread.sleep(1000);
					bluetoothAdapter.enable();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/
			}

			//]]
/* 20170914
		//	super.onConnectionStateChange(gatt, status, newState);
*/
		}
		void quitThread(){
			Device.this.mCharacteristicRead = null;
			Device.this.mCharacteristicWrite = null;
//			/*
			//[[wk
			Device.this.bluetoothGattCharacteristicSet = null;
			Device.this.bluetoothGattCharacteristicBaudRate = null;
			MainActivity.connect_state=null;
			//]]
//        	*/
			Device.this.mPendingDataList.clear();
			Device.this.mSendPending.clear();
			Device.this.mSendTrying = 0;
			Device.this.mRecvLeftLen = 0;
			Device.this.mRecvPending.clear();
			Device.this.mRecvTotall = 0;
			Device.this.mBluetoothGatt.close();
			Device.this.mBluetoothGatt = null;
			mDisconnecting = false;
			synchronized(this){
				if(Device.this.mHandler != null){
					Device.this.mHandler.getLooper().quit();
				}
				Device.this.mHandler = null;
			}
		}
		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {

			if (status == BluetoothGatt.GATT_SUCCESS) {
				this.mHandler.post(new Runnable(){
					@Override
					public void run() {
						DeviceClient client = Device.this.mDeviceClient;
						BluetoothGatt gatt = Device.this.mBluetoothGatt;
						BluetoothGattService service = gatt.getService(uuid_service_read);
						if(service == null){
							Device.this.mDeviceClient.onError(new Exception("clinet not set"));
							return;
						}
						BluetoothGattCharacteristic cRead = service.getCharacteristic(uuid_characteristic_read);
						if(cRead == null){
							client.onError(new Exception("not read characteristic"));
							return;
						}
						int characteristicProperties = cRead.getProperties();

						if ((characteristicProperties | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
							gatt.setCharacteristicNotification(cRead, true);
//CXQ

							//This is config notification.
							if (uuid_characteristic_read.equals(cRead.getUuid())) {
								BluetoothGattDescriptor descriptor = cRead.getDescriptor(uuid_CLIENT_CHARACTERISTIC_CONFIG);
								descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
								mBluetoothGatt.writeDescriptor(descriptor);
							}
							//]]
//
						}
						service = gatt.getService(uuid_service_write);
						if(service == null){
							client.onError(new Exception("clinet not set"));
							return;
						}
						BluetoothGattCharacteristic cWrite = service.getCharacteristic(uuid_characteristic_write);
						if(cWrite == null){
							client.onError(new Exception("not write characteristic"));
							return;
						}
//		                cWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
						cWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);

						Device.this.mCharacteristicRead = cRead;
						Device.this.mCharacteristicWrite = cWrite;

						mDeviceClient.onConnected();

					}
				});
			}

			super.onServicesDiscovered(gatt, status);
		}
		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic, int status) {
			if(characteristic.getUuid().equals(this.uuid_service_read)){
				this.mHandler.post(new Runnable(){
					@Override
					public void run() {
						recvData(Device.this.mCharacteristicRead.getValue());
					}
				});
			}
			super.onCharacteristicRead(gatt, characteristic, status);
		}
		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic, int status) {
			Log.d("发送状态",String.format("%d", status));
			onWriteResponse(status);
			super.onCharacteristicWrite(gatt, characteristic, status);
		}
		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic) {
			//接收到子包
			if(characteristic.getUuid().equals(this.uuid_characteristic_read)){
				this.mHandler.post(new Runnable(){
					@Override
					public void run() {
						recvData(Device.this.mCharacteristicRead.getValue());
					}
				});
			}
			super.onCharacteristicChanged(gatt, characteristic);
		}

		/**
		 * Clears the internal cache and forces a refresh of the services from the
		 * remote device.
		 */
		public boolean refreshDeviceCache() {
			if (mBluetoothGatt != null) {
				try {
					BluetoothGatt localBluetoothGatt = mBluetoothGatt;
					Method localMethod = localBluetoothGatt.getClass().getMethod(
							"refresh", new Class[0]);
					if (localMethod != null) {
						boolean bool = ((Boolean) localMethod.invoke(
								localBluetoothGatt, new Object[0])).booleanValue();
						return bool;
					}
				} catch (Exception localException) {
					Log.i(TAG, "An exception occured while refreshing device");
				}
			}
			return false;
		}

		void onWriteResponse(int status){
			if (status == BluetoothGatt.GATT_SUCCESS) {
				this.mHandler.post(new Runnable(){
					@Override
					public void run() {

						//上1个子包发送成功,发送下1个子包,如果子包全发完,则发下1个包
						Device.this.mSendTrying = 0;
						if(Device.this.mSendPending.size() > 0 ){
							byte[] head = Device.this.mSendPending.get(0);
							byte[] tmp = Device.this.mCharacteristicWrite.getValue();
							if (head.equals(tmp)) {
								//	Device.this.mSendPending.remove(0);
								Log.d(TAG, "sent a sub packet");
							}
						}
						if (Device.this.mSendPending.size() > 0) {
							// 当前包还没有发达
//							Device.this.sendSubPacket("", -1);
						} else {
							Log.d(TAG, "sent a packet");
							//发送子包后连续发包
							//	Device.this.ContinueSendList("");
						}
					}

				});
			}else{
				//[[wk add
				try {
					WLog.logFile("onWriteResponse status="+status);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//]]
				if(status == 133){
					//发送超时
					synchronized(this){
						mWriteTimeout = true;
					}
					Log.e(TAG, "write timeout:133");
				}else{
					this.mHandler.post(new Runnable(){
						@Override
						public void run() {
							DeviceClient client = Device.this.mDeviceClient;
							// 如果发送失败，重试三次
							if (mSendTrying < MAX_TRY) {
								// 重试当前包的当前子包
								if (!sendSubPacket("", -1,1)) {
									if (client != null) {
										client.onError(new Exception("send sub packet failed"));
									}
								}
							}else{
								if(client != null){
									client.onError(new Exception("send failed after try MAX_TRY"));
								}
								Device.this.mSendTrying= 0;
								Device.this.mSendPending.clear();
							}
						}
					});
				}
			}
		}

		/****************************************数据部分***********************************************/

		// 发送子包
		private boolean sendSubPacket(String string, int index,int _status) {
			BluetoothGatt gatt = null;
			BluetoothGattCharacteristic cWrite = null;
			synchronized (this) {
				gatt = this.mBluetoothGatt;
				switch (string) {
					case SetActivity0.bluetoothName:

						cWrite = this.mCharacteristicWrite;
						break;
					default:
						cWrite = this.mCharacteristicWrite;
						break;
				}
			}
			if(gatt == null){
				//[[wk add
				mDeviceClient.onError(new Exception("gatt="+gatt));
				try {
					string+="---gatt="+gatt;
					WLog.logFile(string);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//]]
				return false;
			}
			if (this.mSendPending.size() > 0) {
				// try {
				// Thread.sleep(200);
				// } catch (InterruptedException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
				if ((index-1) < mSendPending.size() && (index-1) >= 0) {
					byte[] first = mSendPending.get(index -1);

					// [[CXQ
					if (mConnected && mBluetoothGatt != null && mCharacteristicWrite != null) {
						cWrite.setValue(first);
						gatt.writeCharacteristic(cWrite);
						StringBuilder sBuilder = new StringBuilder();
						for (int i = 0; i < first.length; i++) {
							sBuilder.append(String.format("%02x ", first[i]));
						}
						Log.d(TAG, "send data " + sBuilder.toString());
						try {
							WLog.logFile("Send Data 普通数据:" + sBuilder.toString());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					} else {
						try {
							WLog.logFile("Sending" + "蓝牙已经断开发送失败");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}else{
					mSendPending.clear();
				}

				// ]]


				mSendTrying++;
			} else {
				// [[wk add
				// mDeviceClient.onError(new
				// Exception("发送数据长度="+this.mSendPending.size()));
				// ]]
				return false;
			}
			return true;
		}

		public boolean fun_Response(byte[] data) {
			// [[CXQ
			BluetoothGatt gatt = null;
			BluetoothGattCharacteristic cWrite = null;
			boolean result = false;
			synchronized (this) {
				gatt = this.mBluetoothGatt;
				cWrite = this.mCharacteristicWrite;
			}
			if (gatt == null) {
				// [[wk add
				mDeviceClient.onError(new Exception("gatt=" + gatt));
				return false;
			} else {
				if (mConnected && mBluetoothGatt != null && mCharacteristicWrite != null) {
					cWrite.setValue(data);
					boolean r = gatt.writeCharacteristic(cWrite);
					result = r;
				}

			}
			return result;
		}

		// 直接发送数据比如'l'
		public void fun_Write(byte[] data) {
			BluetoothGatt gatt = null;
			BluetoothGattCharacteristic cWrite = null;
			if (this.mBluetoothGatt != null && this.mCharacteristicWrite != null) {
				gatt = this.mBluetoothGatt;
				cWrite = this.mCharacteristicWrite;
			}
			if (mConnected && mBluetoothGatt != null && mCharacteristicWrite != null) {
				cWrite.setValue(data);
				gatt.writeCharacteristic(cWrite);
			}
		}

		// 旧协议
		private final int _SUB_PKT_SIZE = 20;

		private boolean _splitPacket() {
			synchronized (this) {
				if (this.mPendingDataList.size() == 0) {
					return false;
				}
				byte[] data = this.mPendingDataList.get(0);
				int added = 0;
				for (int i = 0; i < data.length; i += _SUB_PKT_SIZE) {
					int l = data.length - added > _SUB_PKT_SIZE ? _SUB_PKT_SIZE : data.length - added;
					byte[] buf = new byte[l];
					int j = 0;
					for (; j < _SUB_PKT_SIZE; j++) {
						if ((i + j) < data.length) {
							buf[j] = data[i + j];
						} else {
							break;
						}
					}
					this.mSendPending.add(buf);
					added += j;
				}
				this.mPendingDataList.remove(0);
			}
			return true;
		}

		// 新协议
		private final int SUB_PKT_SIZE = 16;
		private final int PACKAGE_LENGTH = 20;

		private boolean splitPacket() {
			synchronized (this) {
				if (this.mPendingDataList.size() == 0) {
					return false;
				}
				byte[] data = this.mPendingDataList.get(0);

				int i_index = 1;
				int added = 0;
				// 计算总包数
				int Total = (data.length / SUB_PKT_SIZE) + ((data.length % SUB_PKT_SIZE) > 0 ? 1 : 0);
				for (int i = 0; i < data.length; i += SUB_PKT_SIZE) {

					int l = data.length - added > SUB_PKT_SIZE ? SUB_PKT_SIZE : data.length - added;

					byte[] buf = new byte[PACKAGE_LENGTH];
					Arrays.fill(buf, (byte) 0xff);
					buf[0] = (byte) 0xA1; // CXQ
					buf[PACKAGE_LENGTH - 1] = (byte) (0xF1); // cxq
					buf[1] = (byte) Total;
					buf[2] = (byte) i_index++;

					int j = 0;
					for (; j < l; j++) {
						if ((i + j) < data.length) {
							// buf[j] = data[i+j]; //CXQ
							buf[j + 3] = data[i + j];
						} else {
							break;
						}
					}
					// buf[j+2+1] = (byte)(0x23);
					this.mSendPending.add(buf);
					added += j;

				}
				this.mPendingDataList.remove(0);
			}
			return true;
		}

		int recv_index = 0;

		// 接收子包，判断是不是最一后，所有子包接收完后，要组一个大包给Client
		private void recvData(byte[] data) {
			// onWriteResponse(0);

			StringBuffer stringBuffer = new StringBuffer();
			for (int i = 0; i < data.length; i++) {
				stringBuffer.append(String.format("%02x ", data[i]));
			}

			Log.d(TAG, "recvData" + stringBuffer + " " + data.length);

			try {
				WLog.logFile("recvData" + stringBuffer + " " + data.length);

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			stringBuffer.delete(0, stringBuffer.length());
			// [[cxq

			// 如果收到要求重发的包就重发一条数据
			// 修下重发功能
			if (Arrays.equals(data, by_Recv_Request_Data)) {
				boolean success_send = Write(str_Resend_String, by_Resend_data);
				Log.d("resend", "resend" + success_send);
				return;
			}

			byte[] by_ack = new byte[2];
			if (data[0] == (byte) 0xa1 && data[data.length - 1] == (byte) 0xf1) {
				//收到正常的包发送总包数，当前包数
				// 总包数
				by_ack[0] = data[1];
				// 当前包序号
				by_ack[1] = data[2];

				if (data.length == 20) {
					fun_Response(by_ack);
				}
				fun_Deal_Data(data);
			} else {
				// 0xa1		//不正常数据包
				by_ack[0] = (byte) 0xff;
				// 0xf1		//不正常的数据包
				by_ack[1] = (byte) 0xff;
				// 收到不正确的包
				fun_Response(by_ack);
				try {
					WLog.logFile("错误数据");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

		//[[CXQ
		public void fun_Deal_Data(byte[]data){

			DeviceClient client = null;
			synchronized (this) {
				client = this.mDeviceClient;
			}
			if (client == null) {
				return;
			}

			int i_tmp = -1;
			if (data.length >=20) {
				if (data[0] == (byte)0xa1 ) {

					if(m_index == 8) {
						Log.d("88888","88888 "+String.valueOf(m_index)+" 1");
					}
					m_Total = data[1];
					i_tmp = data[2];
					if (m_index == -1 || i_tmp == m_index) {
						synchronized (this) {
							m_index = i_tmp;
						}

					}else{
						return;
					}


					for (int i = 3; i < data.length; i++) {
						if (data[i] != (byte)0xf1 && data[i] != (byte)0xff && data[i] != (byte)0xa0 && data[i] != (byte)0xf0) {
							try {
								stringBuffer.append(String.format("%c",data[i]));
							} catch (Exception e) {
								// TODO: handle exception
								Log.d("format",String.valueOf(data[i]));
							}


						}
					}

					if (m_index == m_Total) {
						synchronized (this) {
							m_index = -1;
							m_Total = -1;
						}

						String str_data = stringBuffer.toString();
						stringBuffer.delete(0, stringBuffer.length());
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						client.onRecv(str_data);

					}else {
						synchronized (this) {
							m_index++;
						}

					}

				}
			}

			//数据的返回
			if (data.length == 5) {
				if (data[0] == (byte) 0xa1 && data[data.length-1] == (byte) 0xf1) {
					int i_index = data[2] + 1;
					if (i_index <= data[1]) {
						sendSubPacket(FileStream.write,i_index,data[3]);
						//m_list.clear();
					}
				}
			}

		}

		//组包
		private byte[] groupPacket(){
			byte[] data = null;
			int s = mRecvPending.size();
			if(s <= 0 && this.mRecvTotall > 0 ){
				return null;
			}
			data = new byte[mRecvTotall];
			for(int i = 0,ii = 0;i<s;i++){
				byte[] d = mRecvPending.get(i);
				//[[wk change i to j
//				for(int j = 0;i<d.length;j++){
				for(int j = 0;j<d.length;j++){
					//]]
					data[ii++] = d[j];
				}
			}
			//[[wk add must clear
			Device.this.mRecvPending.clear();
			//]]
			return data;
		}

	}

	private BluetoothAdapter mBluetoothAdapter = null;
	private Boolean mScanning = false;
	private final Map<String, Device> mDeviceMap = new HashMap<String, Device>();
	private ScanCallback mScanCallback = null;
	private Context mContext = null;
	private Handler mHandler = null;

	public BleDeviceMgr(Context c){
		this.mContext = c;
		BluetoothManager bluetoothManager =(BluetoothManager) c.getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		this.mHandler = new Handler(this.mContext.getMainLooper());
	}
	public Context getApplicationContext() {
		// TODO Auto-generated method stub
		return null;
	}
	public void PostUIRun(Runnable runner,int ms){
		this.mHandler.postDelayed(runner,ms);
	}
	public void RemoveUIRun(Runnable runner){
		this.mHandler.removeCallbacks(runner);
	}
	private static BleDeviceMgr sMgr = null;
	public static BleDeviceMgr CreateMgr(Context c){
		synchronized(BleDeviceMgr.class){
			if(sMgr == null){
				sMgr = new BleDeviceMgr(c);
			}
		}
		return sMgr;
	}
	public static BleDeviceMgr getMgr(){
		BleDeviceMgr mgr = null;
		synchronized(BleDeviceMgr.class){
			mgr = sMgr;
		}
		return mgr;
	}
	public void StartScan(ScanCallback scanCallback){
		synchronized(mScanning){
			if(mScanning){
				return ;
			}
		}
		mScanning = true;
		mBluetoothAdapter.startLeScan(this);
		synchronized(this){
			mScanCallback = scanCallback;
		}
	}
	public void StopScan(){
		synchronized(mScanning){
			if(!mScanning){
				return ;
			}
			mScanning = false;
		}
		mBluetoothAdapter.stopLeScan(this);
	}
	public Device GetDevice(String address){
		Device device = null;
		synchronized(mDeviceMap){
			device = this.mDeviceMap.get(address);
		}
		return device;
	}
	@Override
	public void onLeScan(BluetoothDevice paramBluetoothDevice, int paramInt,byte[] paramArrayOfByte) {
		String address = paramBluetoothDevice.getAddress();
		String name = paramBluetoothDevice.getName();
		synchronized(mDeviceMap){
			Device device = new Device();
			device.mAddress = address /*+ " RSSI: "+String.valueOf(paramInt)*/;
			device.mName = name;

			mDeviceMap.put(address, device);
//			 Log.d("name000", "name000"+name);
		}
		ScanCallback callback = null;
		synchronized(this){
			callback = mScanCallback;
		}
		if(callback != null){
			callback.onScan(address,name);
		}
	}
}
