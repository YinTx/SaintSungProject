package com.saintsung.saintpmc.lock;


import com.saintsung.saintpmc.MainActivity;

public class Response {
	private IResponser m_responser;
	String string;


	public void dataComing(byte[] bData) {
		CommDataQueue s_recvQueue =new CommDataQueue();
		if (!s_recvQueue.isEmpty()) {
			int iLen = s_recvQueue.available();
			byte[] bDataTmp = new byte[iLen + bData.length];
			for (int i = 0; i < iLen; i++) {
				bDataTmp[i] = s_recvQueue.get();
			}
			for (int i = 0; i < bData.length; i++) {
				bDataTmp[i + iLen] = bData[i];
			}
			dataComing(bDataTmp);
		} else {
			int iOffset = 0;
			int iBegin = 0;
			int iEnd = 0;
			//
			while ((iBegin = scanData(bData, iOffset, CommandPacker.s_bPrefix)) != -1) {
				iOffset = iBegin;
				if ((iEnd = scanData(bData, iOffset + 1, CommandPacker.s_bSuffix)) != -1) {
					CommandPacker inData = new CommandPacker();
					//
					String state=CommandPacker.state_openLock;
					if (inData.decodePacket(state,bData, iBegin)> 0) {
						if (m_responser != null){
//							this.m_responser.notify(inData.getCmdType(),inData.getErrorType(), inData.getData());
							m_responser.notify(inData.getCmdType(),inData.getErrorType(), inData.getData());
						}
						iOffset = iEnd+1;

					} else {
						iOffset += 1;
					}

				} else {
					//WK add;
					TypeConvert.A2HCvtRtnT cvt = TypeConvert.AsciiToHex(bData[iOffset + 1], bData[iOffset + 2]);
					if (cvt.rlt){
						BluetoothLeService.receivedLength = cvt.val;
					}
					break;
				}
			}
			if(iBegin>=0)
				for (int i = iOffset; i < bData.length; i++)
					s_recvQueue.push(bData[i]);
			//WK add;
			if (BluetoothLeService.receivedLength>20) {
				string=new String(bData);
				MainActivity.bluetoothLeService.stringBuffer.append(string);
				if ((iEnd = scanData(bData, iOffset + 1, CommandPacker.s_bSuffix)) != -1) {
					bData=MainActivity.bluetoothLeService.stringBuffer.toString().getBytes();
					dataComing(bData);
//					MainActivity.mBluetoothLeService.stringBuffer.delete(0, bData.length-1);
//					MainActivity.mBluetoothLeService.stringBuffer.replace(0, bData.length-1, "");
//					MainActivity.mBluetoothLeService.stringBuffer.replace(0, MainActivity.mBluetoothLeService.stringBuffer.length()-1, "");
					MainActivity.bluetoothLeService.stringBuffer.setLength(0);
					BluetoothLeService.receivedLength=0;
				}
			}
		}
	}

	//
	class CommDataQueue {
		int m_iFront = 0;
		int m_iRear = 0;
		int QUEUE_LEN = 500;
		byte[] m_bQueue = new byte[QUEUE_LEN];
		//
		public synchronized void push(byte bData) {
			m_iRear = (m_iRear + 1) % QUEUE_LEN;
			m_bQueue[m_iRear] = bData;
		}
		//
		public synchronized byte get() {
			if (isEmpty()) {
				return (byte) 0xFF;
			}
			m_iFront = (m_iFront + 1) % QUEUE_LEN;
			return m_bQueue[m_iFront];
		}

		public synchronized int available() {
			return (m_iRear - m_iFront + QUEUE_LEN) % QUEUE_LEN;
		}
		//
		public boolean isFull() {
			return (m_iRear + 1) % QUEUE_LEN == m_iFront;
		}
		//
		public boolean isEmpty() {
			return m_iRear == m_iFront;
		}

		public void clean() {
			m_iFront = m_iRear = 0;
		}

		public byte[] getbQueue() {
			byte[] bData = new byte[available()];
			for (int i = 0; i < bData.length; i++) {
				bData[i] = m_bQueue[(i + m_iFront + 1) % QUEUE_LEN];
			}
			return bData;
		}

		public byte getTail() {
			return m_bQueue[m_iRear];
		}

		public byte getFront() {
			int iFront = (m_iFront + 1) % QUEUE_LEN;
			return m_bQueue[iFront];
		}
	}

	//
	private int scanData(byte[] bData, int iOffset, byte bPrefix) {
		if (bData == null) {
			return -1;
		}
		if (iOffset < 0) {
			return -1;
		}
		if (bData.length - iOffset < 1) {
			return -1;
		}

		for (int i = iOffset; i < bData.length; i++) {
			if (bData[i] == bPrefix) {
				return i;
			}
		}
		return -1;
	}

	//
	public void Register(IResponser response)  {
//		this.m_responser=response;
		if (response == null) {
			m_responser = null;
			return;
		}else if (response instanceof IResponser) {
			m_responser = response;
		}
	}







}
