package com.saintsung.saintpmc.lock;


import com.saintsung.saintpmc.lock.ChainBase;
import com.saintsung.saintpmc.lock.DeviceService;
import com.saintsung.saintpmc.lock.MCUCommand;
import com.saintsung.saintpmc.lock.DeviceService.RecvAction;

import android.util.Log;

public class Set extends ChainBase {
	Set(DeviceService service) {			//i_type 指定是哪个页面发过来的
		super(service);
		service.setBigOpen(0);
		//响应掌机序列号
		Log.d("mActionList", "mActionList"+mActionList.toString());
		this.mActionList.add(new RecvAction(){
			@Override
			public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {

				if (mcuCmd.mCmd == SetAllActivity.OPCODE_SET_PARAM &&  User_Share.i_Change_Cmd == (byte)0xd9) {
					mService.set(SetActivity0.VS_S00Name,null,new String("close"));

					return;
				}

				Log.d("mActionList","mActionList" + mcuCmd.mCmd);
				switch(mcuCmd.mCmd){
					case SetAllActivity.OPCODE_GET_PARAM:{
						if(!mcuCmd.mErrCode.equals("00")){
							mIndex = 0;
							throw new Exception("获取掌机序列号失败!");
						}
						//send get
						byte[] cmdBytes = Command.getInstance().getS00SetValue(SetActivity0.VS_FW51_VER_GOT);
						mService.mDevice.Write(FileStream.write,cmdBytes);
						mService.set(SetActivity0.VS_S00SerialNumber,null,new String(mcuCmd.mAttechData));
						mIndex ++;
					}break;
					case SetAllActivity.OPCODE_SET_PARAM:{
						Log.d("Set", "Set" + "Set");

						if(!mcuCmd.mErrCode.equals("00")){
							mIndex = 0;
							throw new Exception("修改/设置掌机序列号失败!");
						}
						//send get S00SerialNumber to update setValue(all)
						byte[] cmdBytes = Command.getInstance().getS00SetValue(SetActivity0.VS_S00SerialNumber);
						if (mService.mDevice != null ) {
							mService.mDevice.Write(FileStream.write,cmdBytes);
							mIndex=0;
						}

					}break;
					case MCUCommand.opcode_sleep:{
						if(!mcuCmd.mErrCode.equals("00")){
							mIndex = 0;
							throw new Exception("掌机休眠失败!");
						}
						//send MCUCommand.opcode_sleep to close setActivity
						mService.set(MCUCommand.opcode_sleep,null,MCUCommand.string_sleep);
						mIndex=0;
					}break;
					default:{
						mIndex = 0;
					}
				}
			}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

			}

		});
		//响应S00硬件版本
		this.mActionList.add(new RecvAction(){

			@Override
			public void Do(final MCUCommand mcuCmd, byte[] data) throws Exception {
				Log.d("mActionList","mActionList"+"响应S00硬件版本");
				switch(mcuCmd.mCmd){
					case SetAllActivity.OPCODE_GET_PARAM:{
						if(!mcuCmd.mErrCode.equals("00")){
							mIndex = 0;
							throw new Exception("获取S00硬件版本失败");
						}
						//send set
						byte[] cmdBytes = Command.getInstance().getS00SetValue(SetActivity0.VS_HW_GOT);
						if (mService.mDevice != null) {
							mService.mDevice.Write(FileStream.write,cmdBytes);
							mIndex ++;
						}

						mService.set(SetActivity0.VS_FW51_VER_GOT,null,TypeConvert.byte2hex(mcuCmd.mAttechData[1]).toUpperCase());
					}break;
					default:{
						mIndex = 0;
					}break;
				}
			}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

			}

		});
		//响应S00固件版本
		this.mActionList.add(new RecvAction(){

			@Override
			public void Do(final MCUCommand mcuCmd, byte[] data) throws Exception {
				Log.d("mActionList","mActionList"+"响应S00固件版本");
				switch(mcuCmd.mCmd){
					case SetAllActivity.OPCODE_GET_PARAM:{
						if(!mcuCmd.mErrCode.equals("00")){
							mIndex = 0;
							throw new Exception("获取S00固件版本失败");
						}
						//send set //发送命令获取偏移值
						byte[] cmdBytes = Command.getInstance().getS00SetValue(SetActivity0.VS_LOFFSET_GOT);
						if (mService.mDevice != null) {
							mService.mDevice.Write(FileStream.write,cmdBytes);
							mIndex ++;
						}

						mService.set(SetActivity0.VS_HW_GOT,null,TypeConvert.byte2hex(mcuCmd.mAttechData[1]).toUpperCase());
					}break;
					default:{
						mIndex = 0;
					}break;
				}
			}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

			}

		});
		//响应S00偏移值
		this.mActionList.add(new RecvAction(){

			@Override
			public void Do(final MCUCommand mcuCmd, byte[] data) throws Exception {
				Log.d("mActionList","mActionList"+"响应S00偏移值");
				switch(mcuCmd.mCmd){
					case SetAllActivity.OPCODE_GET_PARAM:{
						if(!mcuCmd.mErrCode.equals("00")){
							mIndex = 0;
							throw new Exception("获取S00偏移值失败");
						}
						//send set
						byte[] cmdBytes = Command.getInstance().getS00SetValue(SetActivity0.VS_ALL_GOT);
						if (mService.mDevice != null) {
							mService.mDevice.Write(FileStream.write,cmdBytes);
							mIndex ++;
						}

						int i;
						String direction;
						if (mcuCmd.mAttechData[1]<0) {
							i= mcuCmd.mAttechData[1]&0x7f;
							direction=SetActivity0.negative;
						} else {
							i= mcuCmd.mAttechData[1];
							direction=SetActivity0.positive;
						}
						mService.set(SetActivity0.VS_LOFFSET_GOT,direction,Integer.toString(i));
					}break;
					default:{
						mIndex = 0;
					}break;
				}
			}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

			}

		});

		//响应S00设置值
		this.mActionList.add(new RecvAction(){

			@Override
			public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {
				Log.d("mActionList","mActionList"+"响应S00设置值");
				switch(mcuCmd.mCmd){
					case SetAllActivity.OPCODE_GET_PARAM:{
						if(!mcuCmd.mErrCode.equals("00")){
							mIndex = 0;
							throw new Exception("获取S00设置值失败");
						}
						//send unlock type
						byte[] cmdBytes = Command.getInstance().getS00SetValue(SetActivity0.VS_UNLOCK_TYPE);
						if(mService.mDevice != null){
							mService.mDevice.Write(FileStream.write,cmdBytes);
							mService.set(SetActivity0.VS_ALL_GOT,null,String.valueOf(mcuCmd.mAttechData[1]));
							mIndex++;
						}



//					mService.set(SetActivity0.VS_ALL_GOT,null,TypeConvert.byte2hex(mcuCmd.mAttechData[1]));
//[[cxq

//]]

						//发送获取电池电量值的命令
//					byte[] cmdBytes = Command.getInstance().getS00SetValue(SetActivity0.OPCODE_GET_BATTERY);
//					mService.mDevice.Write(FileStream.write,cmdBytes);
//[[CXQ
//					mService.set(SetActivity0.VS_ALL_GOT,null,String.valueOf(mcuCmd.mAttechData[1]));
//]]
//					mService.set(SetActivity0.VS_ALL_GOT,null,TypeConvert.byte2hex(mcuCmd.mAttechData[1]));

					}break;
					default:{
						mIndex = 0;
					}
				}
			}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

			}

		});
/*

		//获取电量值
				this.mActionList.add(new RecvAction(){

					@Override
					public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {
						switch(mcuCmd.mCmd){
						case SetAllActivity.OPCODE_GET_BATTERY:{
							if(mcuCmd.mErrCode != 0){
								mIndex = 0;
								throw new Exception("获取电池电量值失败");
							}
							//send unlock type
							byte[] cmdBytes = Command.getInstance().getS00SetValue(SetActivity0.VS_UNLOCK_TYPE);
							mService.mDevice.Write(FileStream.write,cmdBytes);
		//[[CXQ
//							mService.set(SetActivity0.OPCODE_GET_BATTERY,null,String.valueOf(mcuCmd.mAttechData[1]));
		//]]
							mService.set(SetActivity0.VS_ALL_GOT,null,TypeConvert.byte2hex(mcuCmd.mAttechData[1]));
							mIndex++;
						}break;
						default:{
							mIndex = 0;
						}
						}
					}

				});
*/

		//响应S00开锁方式
		this.mActionList.add(new RecvAction(){

			@Override
			public void Do(MCUCommand mcuCmd, byte[] data) throws Exception {
				Log.d("mActionList","mActionList"+"响应S00开锁方式");
				switch(mcuCmd.mCmd){
					case SetAllActivity.OPCODE_GET_PARAM:{
						if(!mcuCmd.mErrCode.equals("00")){
							mIndex = 0;
							throw new Exception("获取S00开锁方式失败");
						}
						mService.set(SetActivity0.VS_UNLOCK_TYPE,null,TypeConvert.byte2hex(mcuCmd.mAttechData[1]));
						mIndex =0;
					}break;
					default:{
						mIndex = 0;
					}
				}
			}

			@Override
			public void Do(MCUCommand mcuCmd, String data) throws Exception {
				// TODO Auto-generated method stub

			}

		});
	}
}
