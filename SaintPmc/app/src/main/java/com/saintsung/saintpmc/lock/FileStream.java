package com.saintsung.saintpmc.lock;

import android.os.Environment;
import android.util.Log;


import com.saintsung.saintpmc.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileStream {
	private File file;
	public final static String write="write";
	public final static String read="read";
	public final static String delete="delete";
	public final static String connectRecord="connectRecord";
	public final static String userLogin="userLogin";
	public final static String userDownload="userDownload";
	public final static String sendLockNumberAndPassword="sendLockNumberAndPassword";
	public final static String jsonFile="jsonFile";
	public final static String deviceFile ="deviceFile";
	public final static String log="state_log";
	public final static String rewriteLog="rewriteLog";
	public final static String unlockType="unlockType";
	public final static String socket="socket";
	public final static String S00Up="S00Up";
	public final static String screw_log="screw_log";
	public final static String valve_key="valve_key";
	public final static String config_value = "config_value";
	public final static String config_serial_number ="config_serial_number";
	public final static String sheetFile = "sheetFile";

	public String catalog = (Environment.getExternalStorageDirectory()).toString()+"/APP";
	public String connectRecordFileName = catalog+"/connectRecord.txt";
	public String userLoginFileName = catalog+"/userLogin.txt";
	public String userDownloadFileName = catalog+"/userDownload.txt";
	public String sendLockNumberAndPasswordFileName = catalog+"/sendLockNumberAndPassword.txt";
	public String jsonFileName = catalog+"/lock.json";
	public String logFileName = catalog+"/log.txt";
	public String unlockTypeFileName = catalog+"/unlockType.txt";
	public String socketFileName = catalog+"/socket.txt";
	public String S00UpFileName = catalog+"/STM32F103CB_BT.bin";
	public String screw_logFileName = catalog+"/screw_log.txt";
	public String ValveFileName = catalog+"/valve_key.txt";
	public String ConfigValueFileName = catalog+"/config_value.bat";
	public String ConfigSerialNumberFileName = catalog+"/config_serial_Number.txt";
	public String DeviceInfoFile = catalog+"/DeviceInfo.txt";
	public String SheetFile = catalog + "/SheetFile.txt";
	String string;
	private int pwdOffsetTwo10=2;
	private int pwdOffsetTwo15=12;
	private int pwdOffsetThree10=3;
	private int pwdOffsetThree15=18;
	private int pwdOffsetFour10=4;
	private int pwdOffsetFour15=24;
	private FileOutputStream fileOutputStream;
	private byte[] byteArray,bDataSend;



	//check file(文件夹)/目录(catalog)/路径(path)是否存在
	public String checkCatalog(){
		//定义文件夹()/目录(catalog)/路径(path)
		file=new File(catalog);
		if (!file.exists()) {
			try {
				//创建文件夹()/目录(catalog)/路径(path)
				file.mkdirs();
				return file+"";
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("创建文件", "exception:"+e);
				return e.toString();
			}
		}else{
			return file+"";
		}

	}
	//检查文件是否存在
	public String checkFile(String state){
		//判断执行
		switch (state) {
			case connectRecord:
				file=new File(connectRecordFileName);
				break;
			case userLogin:
				file=new File(userLoginFileName);
				break;
			case unlockType:
				file=new File(unlockTypeFileName);
				break;
			case jsonFile:
				file=new File(jsonFileName);
				break;
			case userDownload:
				file=new File(userDownloadFileName);
				break;
			case sendLockNumberAndPassword:
				file=new File(sendLockNumberAndPasswordFileName);
				break;
			case log:
				file=new File(logFileName);
				break;
			case socket:
				file=new File(socketFileName);
				break;
			case S00Up:
				file=new File(S00UpFileName);
				break;
			case screw_log:
				file=new File(screw_logFileName);
				break;
			case valve_key:
				file = new File(ValveFileName);
				break;
			case config_value:
				file = new File(ConfigValueFileName);
				break;
			case config_serial_number:
				file = new File(ConfigSerialNumberFileName);
				break;
			case deviceFile:
				file=new File(DeviceInfoFile);
				break;
			case sheetFile:
				file = new File(SheetFile);
				break;

			default:
				break;
		}
		if (!file.exists()) {
			try {
				//在指定的文件夹中创建文件
				file.createNewFile();
				return file+"";
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("创建文件", "exception:"+e);
				return e.toString();
			}
		}else{
			return file+"";
		}
	}
	//写入数据
	public byte[] writeFile(String state,byte [] byteArray){
		try {
			//判断执行
			if(state.equals(log)||state.equals(userDownload)||state.equals(sendLockNumberAndPassword)||state.equals(screw_log)){
				//write continue(续写)
				fileOutputStream=new FileOutputStream(file,true);
			}else{
				//重写
				fileOutputStream=new FileOutputStream(file);
			}
			//判断写入数据
			if(byteArray!=null){
				//写入数据
				fileOutputStream.write(byteArray);
			} else {
				//清空/重写
				byteArray=new byte[0];
				fileOutputStream.write(byteArray);
			}

//[[CXQ
			//用户登录信息
			if (state.equals(userLogin) && byteArray != null) {
				//写入数据 对数据进行加密
				//重写
				fileOutputStream=new FileOutputStream(file);
				fileOutputStream.write(fun_ReversalLoginInfo(byteArray));
			}
//]]

			fileOutputStream.close();
			return byteArray;
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("写入数据", "exception:"+e);
			return e.toString().getBytes();
		}
	}
	//读取数据
	public byte[] readFile(String state){
		try {
			// 新建一个FileInputStream对象
			FileInputStream  fileInputStream=new FileInputStream (file);
			// 新建一个字节数组
			byteArray = new byte[fileInputStream.available()];
			// 将文件中的内容读取到字节数组中
			fileInputStream.read(byteArray);

//[[CXQ
			if(state.equals(userLogin))
			{
				//用户登录信息
				if (byteArray != null) {
					//写入数据 对数据进行加密
					byteArray = fun_ReversalLoginInfo(byteArray);
				}
			}

//]]

			fileInputStream.close();
			return byteArray;
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("写入数据", "exception:"+e);
			return e.toString().getBytes();
		}
	}
	//
	public byte[] fileStream(String state,String fileState,byte [] byteArray){
		checkCatalog();
		checkFile(state);
		switch (fileState) {
			case FileStream.write:
				//write file
				writeFile(state,byteArray);
				break;
			case FileStream.read:
				//read file
				byteArray=readFile(state);

				break;
			case FileStream.delete:
				//delete file
				file.delete();
				break;
			default:
				break;
		}
		return byteArray;
	}
	//
	public boolean PWD(){
		boolean flag=false;
		byte[] byteArray=fileStream(jsonFile, FileStream.read, null);
		if (byteArray==null) {
			//提示信息
			BluetoothLeService.intent.putExtra(BluetoothLeService.WRITE_DATA, "请手动导入lock.json文件到手机的APP文件夹下!" + "\n");
			BluetoothLeService.intent0.putExtra(BluetoothLeService.WRITE_DATA, "请手动导入lock.json文件到手机的APP文件夹下!" + "\n");
		}else if (byteArray.equals("")) {
			//提示信息
			BluetoothLeService.intent.putExtra(BluetoothLeService.WRITE_DATA, "APP文件夹里的lock.json为空!" + "\n");
			BluetoothLeService.intent0.putExtra(BluetoothLeService.WRITE_DATA, "APP文件夹里的lock.json为空!" + "\n");
		}else {
			try {
				JSONObject jsonObject = new JSONObject(new String(byteArray));
				if (!jsonObject.has(BluetoothLeService.lock_no)) {
					//提示信息
					BluetoothLeService.intent.putExtra(BluetoothLeService.WRITE_DATA, "本地Json文件,不支持开此锁!" + "\n");
					BluetoothLeService.intent0.putExtra(BluetoothLeService.WRITE_DATA, "本地Json文件,不支持开此锁!" + "\n");
				} else {
					BluetoothLeService.pwd = jsonObject.getString(BluetoothLeService.lock_no);
					//打包发送开锁密码
					decodeOpt_pwd();
					flag=true;
				}
			} catch (JSONException e) {
				e.printStackTrace();
				BluetoothLeService.intent.putExtra(BluetoothLeService.WRITE_DATA, e + "\n");
				BluetoothLeService.intent0.putExtra(BluetoothLeService.WRITE_DATA, e + "\n");
			}
		}
		return flag;
	}
	//打包发送开锁密码
	public void decodeOpt_pwd(){
		int sepIdx,temp=0;
		String temp_pwd="";
		CommandPacker packet = new CommandPacker();
		if(BluetoothLeService.pwd!=null && (BluetoothLeService.pwd.length()==10||BluetoothLeService.pwd.length()==15)){
			BluetoothLeService.state=CommandPacker.encode_openLock;
			if(BluetoothLeService.pwd.length()==10){
				//判断执行
				if (LockSetActivity.unlockType.equals(LockSetActivity.unlockTwo)||LockSetActivity.unlockType.equals(LockSetActivity.unlockThree)||LockSetActivity.unlockType.equals(LockSetActivity.unlockFour)) {
					if (BluetoothLeService.unlockCount>=2) {
						BluetoothLeService.unlockCount=0;
					}
					BluetoothLeService.unlockCount++;
					for(sepIdx=0;sepIdx<5;sepIdx++){
						if(sepIdx==0){
							if(BluetoothLeService.unlockCount==1){
								if (LockSetActivity.unlockType.equals(LockSetActivity.unlockTwo)) {
									temp=((Integer.parseInt(BluetoothLeService.pwd.substring(sepIdx*2, sepIdx*2+2)))+pwdOffsetTwo10);
								} else if (LockSetActivity.unlockType.equals(LockSetActivity.unlockThree)) {
									temp=((Integer.parseInt(BluetoothLeService.pwd.substring(sepIdx*2, sepIdx*2+2)))+pwdOffsetThree10);
								} else if (LockSetActivity.unlockType.equals(LockSetActivity.unlockFour)) {
									temp=((Integer.parseInt(BluetoothLeService.pwd.substring(sepIdx*2, sepIdx*2+2)))+pwdOffsetFour10);
								}else {

								}
							}else if(BluetoothLeService.unlockCount==2){
								if (LockSetActivity.unlockType.equals(LockSetActivity.unlockTwo)) {
									temp=((Integer.parseInt(BluetoothLeService.pwd.substring(sepIdx*2, sepIdx*2+2)))-pwdOffsetTwo10);
								} else if (LockSetActivity.unlockType.equals(LockSetActivity.unlockThree)) {
									temp=((Integer.parseInt(BluetoothLeService.pwd.substring(sepIdx*2, sepIdx*2+2)))-pwdOffsetThree10);
								} else if (LockSetActivity.unlockType.equals(LockSetActivity.unlockFour)) {
									temp=((Integer.parseInt(BluetoothLeService.pwd.substring(sepIdx*2, sepIdx*2+2)))-pwdOffsetFour10);
								}else {

								}
							}
						}else{
							temp=(Integer.parseInt(BluetoothLeService.pwd.substring(sepIdx*2, sepIdx*2+2)));
						}
						if (temp<10) {
							temp_pwd+="0"+temp;
						}else{
							temp_pwd+=temp;
						}
					}
					MainActivity.bluetoothLeService.showData();
				}else{
					//其他开锁方式
					temp_pwd=BluetoothLeService.pwd;
				}
				byte[] open_lst = new byte[9];
				for(sepIdx=0;sepIdx<5;sepIdx++){
					open_lst[sepIdx]=(byte)(Integer.parseInt(temp_pwd.substring(sepIdx*2, sepIdx*2+2)));
				}
				byte[] lock_no_bytes = TypeConvert.bigEndian_int2byte(Integer.parseInt(BluetoothLeService.lock_no));
				for(int i=5;i<9;i++){
					open_lst[i]=lock_no_bytes[i-5];
				}
				if(packet.setPacketParam(CommandPacker.OPCODE_OPEN_LOCK, open_lst,BluetoothLeService.state)){
					bDataSend = packet.encodePacket(BluetoothLeService.state);
//				send(bDataSend,0,bDataSend.length);
				}
			}else if((BluetoothLeService.pwd.length()==15)){
				//判断执行
				if (LockSetActivity.unlockType.equals(LockSetActivity.unlockTwo)||LockSetActivity.unlockType.equals(LockSetActivity.unlockThree)||LockSetActivity.unlockType.equals(LockSetActivity.unlockFour)) {
					if (BluetoothLeService.unlockCount>=2) {
						BluetoothLeService.unlockCount=0;
					}
					BluetoothLeService.unlockCount++;
					for(sepIdx=0;sepIdx<5;sepIdx++){
						if(sepIdx==0){
							if(BluetoothLeService.unlockCount==1){
								if (LockSetActivity.unlockType.equals(LockSetActivity.unlockTwo)) {
									temp=((Integer.parseInt(BluetoothLeService.pwd.substring(sepIdx*3, sepIdx*3+3)))+pwdOffsetTwo15);
								} else if (LockSetActivity.unlockType.equals(LockSetActivity.unlockThree)) {
									temp=((Integer.parseInt(BluetoothLeService.pwd.substring(sepIdx*3, sepIdx*3+3)))+pwdOffsetThree15);
								} else if (LockSetActivity.unlockType.equals(LockSetActivity.unlockFour)) {
									temp=((Integer.parseInt(BluetoothLeService.pwd.substring(sepIdx*3, sepIdx*3+3)))+pwdOffsetFour15);
								}
							}else if(BluetoothLeService.unlockCount==2){
								if (LockSetActivity.unlockType.equals(LockSetActivity.unlockTwo)) {
									temp=((Integer.parseInt(BluetoothLeService.pwd.substring(sepIdx*3, sepIdx*3+3)))-pwdOffsetTwo15);
								} else if (LockSetActivity.unlockType.equals(LockSetActivity.unlockThree)) {
									temp=((Integer.parseInt(BluetoothLeService.pwd.substring(sepIdx*3, sepIdx*3+3)))-pwdOffsetThree15);
								} else if (LockSetActivity.unlockType.equals(LockSetActivity.unlockFour)) {
									temp=((Integer.parseInt(BluetoothLeService.pwd.substring(sepIdx*3, sepIdx*3+3)))-pwdOffsetFour15);
								}
							}
						}else{
							temp=(Integer.parseInt(BluetoothLeService.pwd.substring(sepIdx*3, sepIdx*3+3)));
						}
						if (temp<10) {
							temp_pwd+="00"+temp;
						}else if((temp>9)&&(temp<100)){
							temp_pwd+="0"+temp;
						}else{
							temp_pwd+=temp;
						}
					}
					MainActivity.bluetoothLeService.showData();
				} else {
					//其他开锁方式
					temp_pwd=BluetoothLeService.pwd;
				}
				byte[] open_lst = new byte[14];
				sepIdx=0;
				for(int j=0;j<5;j++){
					open_lst[sepIdx]=(byte)((Integer.parseInt(temp_pwd.substring(j*3, j*3+3))>>8)&0x000000FF);
					sepIdx++;
					open_lst[sepIdx]=(byte)(Integer.parseInt(temp_pwd.substring(j*3, j*3+3))&0x000000FF);
					sepIdx++;
				}
				//线程问题
				byte[] lock_no_bytes = TypeConvert.bigEndian_int2byte(Integer.parseInt(BluetoothLeService.lock_no));
				for(int i=10;i<14;i++){
					open_lst[i]=lock_no_bytes[i-10];
				}
				if(packet.setPacketParam(CommandPacker.OPCODE_OPEN_LOCK15, open_lst,BluetoothLeService.state)){
					bDataSend = packet.encodePacket(BluetoothLeService.state);
				}
			}
//        	MainActivity.bluetoothLeService.writeLlsAlertLevel(null,bDataSend);
			int Dlength=bDataSend.length;
			if (Dlength<21) {
				MainActivity.bluetoothLeService.writeLlsAlertLevel(null,bDataSend);
			} else {
				//拆分发送
				byte[] bDataSend0=new byte[20];
				System.arraycopy(bDataSend, 0, bDataSend0, 0, 20);
				MainActivity.bluetoothLeService.writeLlsAlertLevel(null,bDataSend0);
				Dlength-=20;
				int i=0;
				if((Dlength-20)>20) {
					i++;
					Dlength-=20;
					MainActivity.bluetoothLeService.bDataSend1=new byte[20];
					System.arraycopy(bDataSend, 20*i, MainActivity.bluetoothLeService.bDataSend1, 0, 20);
//					MainActivity.bluetoothLeService.writeLlsAlertLevel(null,MainActivity.bluetoothLeService.bDataSend1);
					MainActivity.bluetoothLeService.writeLlsAlertLevel(BluetoothLeService.split,MainActivity.bluetoothLeService.bDataSend1);
				}else {
					MainActivity.bluetoothLeService.bDataSend1=new byte[Dlength];
					System.arraycopy(bDataSend, 20*(i+1), MainActivity.bluetoothLeService.bDataSend1, 0, Dlength);
//					MainActivity.bluetoothLeService.writeLlsAlertLevel(null,MainActivity.bluetoothLeService.bDataSend1);
					MainActivity.bluetoothLeService.writeLlsAlertLevel(BluetoothLeService.split,MainActivity.bluetoothLeService.bDataSend1);
				}
			}
			BluetoothLeService.intent.putExtra(BluetoothLeService.WRITE_DATA, "开锁中,固定锁不要离开..." + "\n");
			BluetoothLeService.intent0.putExtra(BluetoothLeService.WRITE_DATA, "开锁中,固定锁不要离开..." + "\n");
		}else if (BluetoothLeService.pwd==null||BluetoothLeService.pwd.contains("")) {
			BluetoothLeService.intent.putExtra(BluetoothLeService.WRITE_DATA, "开锁密码不能为空!" + "\n");
			BluetoothLeService.intent0.putExtra(BluetoothLeService.WRITE_DATA, "开锁密码不能为空!" + "\n");
		}else{
			BluetoothLeService.intent.putExtra(BluetoothLeService.WRITE_DATA, "输入开锁密码长度有误,目前只支持10或15位长度的开锁密码!" + "\n");
			BluetoothLeService.intent0.putExtra(BluetoothLeService.WRITE_DATA, "输入开锁密码长度有误,目前只支持10或15位长度的开锁密码!" + "\n");
		}
		MainActivity.handLockNumber=null;
	}

	//	//异或加密解密
	public  byte[] fun_ReversalLoginInfo(byte[] by_LoginInfo) {
		byte[] by_data = by_LoginInfo;
		byte by_key = 7;
//		byte by_temp = 0;
		for (int i = 0; i < by_data.length ; i++) {
			by_data[i] = (byte) (by_data[i] ^ by_key);
		}
		return by_data;
	}
}
