package com.saintsung.saintpmc.lock;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;


import com.google.gson.Gson;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.saintsung.saintpmc.MainActivity;
import com.saintsung.saintpmc.MyApplication;
import com.saintsung.saintpmc.R;

import com.saintsung.saintpmc.asynctask.QueryAddressTask;
import com.saintsung.saintpmc.asynctask.RetrofitRxAndroidHttp;
import com.saintsung.saintpmc.bean.LockLogBean;
import com.saintsung.saintpmc.bean.LockLogUpServiceBean;
import com.saintsung.saintpmc.bean.WorkOrderBean;
import com.saintsung.saintpmc.orderdatabase.LockInformation;
import com.saintsung.saintpmc.orderdatabase.LockInformation$Table;
import com.saintsung.saintpmc.tool.DataProcess;
import com.saintsung.saintpmc.tool.ToastUtil;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.saintsung.saintpmc.lock.BleDeviceMgr.DeviceClient;
import com.saintsung.saintpmc.lock.ChainBase.UnProcException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import okhttp3.ResponseBody;
import rx.functions.Action;
import rx.functions.Action1;

@SuppressLint("SimpleDateFormat")
public class DeviceService extends Service implements DeviceClient, CommomInterface {
    // 连结设备
    // extra:
    // address:设备地址
    public static final String ACTION_CONNECT = "com.saintsung.saintpmc.lock.connect";
    // 断开设备
    // extra:
    // address:设备地址
    public static final String ACTION_DISCONNECT = "com.saintsung.saintpmc.lock.disconnect";
    // 得到已经连接的设备address
    // 因为已经连接的设备扫描不到
    public static final String ACTION_GETDEVICE = "com.saintsung.saintpmc.lock.getdevice";
    // 接收设备状态 由DeviceService发出的广播
    // extra:
    // status:DeviceStatus
    // mcuCmd:设备发过的命令
    // errMsg:错误描述串
    // errCode:错误码
    public static final String ACTION_DEVICE_STATUS = "com.saintsung.saintpmc.lock.device_status";
    public static final String ACTION_DEVICE_SLEEP_WAKEIP = "com.saintsung.saintpmc.lock.device_sleep_wake";
    // 设置自动测试类型 0:关闭自动测试,1:自动一次大小值测试,2自动循环大小值测试
    public static final String ACTION_AUTO_TEST_TYPE = "com.saintsung.saintpmc.lock.auto_test_type";
    // 设置开锁码的类型,0：原值开所,1:大小值开所,2:小值开所
    public static final String ACTION_SET_KEY_VALUE_TYPE = "com.saintsung.saintpmc.lock.set_key_value_type";
    public static final String ACTION_GET_VALUES = "com.saintsung.saintpmc.lock.get_values";
    public static final String ACTION_AUTORECONNECT = "com.saintsung.saintpmc.lock.auto_reconnect";
    public static final String EXTRA_STATUS = "com.saintsung.saintpmc.lock.extra_status";
    // 设备地址
    public static final String EXTRA_ADDRESS = "com.saintsung.saintpmc.lock.extra_address";
    // 设备名称
    public static final String EXTRA_NAME = "com.saintsung.saintpmc.lock.extra_name";
    public static final String EXTRA_MCU_CMD = "com.saintsung.saintpmc.lock.extra_mcu_cmd";
    public static final String EXTRA_ERR_MSG = "com.saintsung.saintpmc.lock.extra_err_msg";
    public static final String EXTRA_ERR_CODE = "com.saintsung.saintpmc.lock.extra_err_code";
    public static final String EXTRA_LOG = "com.saintsung.saintpmc.lock.extra_log";
    public static final String EXTRA_TEST_TIMES = "com.saintsung.saintpmc.lock.extra_test_times";
    public static final String EXTRA_OK_TIMES = "com.saintsung.saintpmc.lock.extra_ok_times";
    public static final String EXTRA_FAIL_TIMES = "com.saintsung.saintpmc.lock.extra_fail_times";
    public static final String EXTRA_START_TIME = "com.saintsung.saintpmc.lock.extra_start_time";
    public static final String EXTRA_KEY_VALUE_TYPE = "com.saintsung.saintpmc.lock.key_value_type";
    public static final String EXTRA_AUTO_TEST_TYPE = "com.saintsung.saintpmc.lock.extra_auto_test_type";
    public static final String EXTRA_MANUAL = "com.saintsung.saintpmc.lock.extra_manual";

    public static final String ACTION_DELETE = "action_delete";
    // [[wk
    public static final String activity_main = "MainActivity";
    public static final String activity_DeviceScan0 = "com.saintsung.saintpmc.lock.DeviceScanActivity0";
    public static final String activity_LockerProcess = "com.saintsung.saintpmc.lock.LockerProcessAtivity";
    public static final String activity_set = "SetActivity0";
    public static final String activity_setAll = "SetAllActivity";
    public static final String extra_screw_state = "extra_screw_state";
    public static String standardDate = "";

    // ]]
    // [[CXQ
    // 2016-4-7
    public static final String activity_GetConfig = "Main_GetConfig";
    public static final String activity_SetType = "Main_SetType";
    public static final String EXTRA_VALUE = "extra.value";
    public static final String EXTRA_FLAG = "extra.flag";
    public static final String EXTRA_SERIAL_NUMBER = "extra.serial";
    public static final String action_upload = "action_upload";
    public static final int mNormalLock = 0; // 普通锁
    public static final int mScrewLock = 1; // 螺丝锁
    public static final int mOtherLock = 2; // 其它锁
    public static final String download_sheet = "download_sheet";
    public static final String upload_history = "upload_history";
    public static final String download_offline = "download_offline";
    public static String sheet_interrupt = "";
    public static String action_down_upload = ""; // 如果就下载工单先上传再下载，如果只上传再不下载
    public String sheet_remark = ""; // 工单备注
    public final int cmd_index = 4; // 命令码的位置
    public static final String sheet_offline = "FCXXXXXXXXXXXXXX";
    public static String recv_record_head = "";

    public static String ScrewOperType = "ScrewOperType";
    public static String unlockScrew = "";
    public static String ScrewNum = "";

    // [[cxq
    public static String STR_CMD = "";
    private final static int i_md5_length = 32;
    private String str_lock = "";
    //锁类型的总长度
    private final int keyTotalLength = 4;

    int dw_flag = 0;


    // 新协议上传历史记录的条数
    private int i_uploadItem = 1;
    ArrayList<String> list_LockRecord = new ArrayList<String>();
    ArrayList<String> list_SheetRecord = new ArrayList<String>();

    ArrayList<String> list_Sheet = new ArrayList<String>();
    ArrayList<String> array_record = new ArrayList<String>();

    // ]]

    SharedPreferences mySharedPreferences;
    SharedPreferences.Editor editor;
    User_Share user_Share = new User_Share();

    public String str_ErrorKey = "FFFFFFFFFFFFFFF|FFFF";
    // 锁类型
    private final String[] array_LockType = {"0001", "0002", "0003", "0004"};
    byte[] by_Set_Value = new byte[]{0, 0, 0, 0};
    public Interface_BLE interface_BLE;
    int i_lockViewGetParam = 0;

    // ]]

    public static final int DS_UNKNOWN = 0;
    // 已经连接
    public static final int DS_CONNECTED = 1;
    // 已经断开连接
    public static final int DS_DISCONNECTED = 2;
    // 错误
    public static final int DS_ERROR = 3;
    public static final int DS_LOG = 4;
    String rtnFlag;
    String rtnLockSn;
    String rtnPwd;

    enum ErrCode {
        OK, NORMAL,
    }

    Intent intent;

    public class LocalBinder extends Binder {
        DeviceService getService() {
            return DeviceService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    //
    private final IBinder mBinder = new LocalBinder();

    public static IntentFilter makeBroadcasstIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DeviceService.ACTION_DEVICE_STATUS);
        intentFilter.addAction(DeviceService.ACTION_DEVICE_SLEEP_WAKEIP);
        return intentFilter;
    }

    public static IntentFilter makeBroadcastIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DeviceService.activity_GetConfig);
        return intentFilter;
    }

    BleDeviceMgr.Device mDevice = null;
    String mLastAddress = null;
    String mLastDeviceName = null;
    String mAddres = null;
    Handler mHandler = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mHandler == null) {
            mHandler = new Handler();
        }
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }
        // 文件保存信息
        user_Share = new User_Share();
        mySharedPreferences = getSharedPreferences(user_Share.MY_PREFS, MODE_PRIVATE);
        editor = mySharedPreferences.edit();

        Bundle b = intent.getExtras();

        switch (intent.getAction()) {
            case ACTION_CONNECT: {
                onConnect(intent, flags, startId);
            }
            break;
            case ACTION_DISCONNECT: {            //断开
                onDisconnect(intent, flags, startId);
            }
            break;
            case ACTION_GETDEVICE: {
                if (this.mLastAddress != null && this.mLastAddress.length() > 0) {
                    if (this.mDevice != null) {
                        broadcastStatus(DeviceService.DS_CONNECTED);
                    } else {
                        broadcastStatus(DeviceService.DS_DISCONNECTED);
                    }
                }
            }
            break;
            case ACTION_AUTO_TEST_TYPE: {
                String type = b.getString(EXTRA_AUTO_TEST_TYPE);
                this.mAutoTest = type;
                CreateActionChain();
                this.broadcastStatus(DS_UNKNOWN);
            }
            break;
            case ACTION_SET_KEY_VALUE_TYPE: {
                this.mBigOpen = b.getInt(EXTRA_KEY_VALUE_TYPE);
                this.mAutoTest = LockSetActivity.unlockUser;
                CreateActionChain();
                this.broadcastStatus(DS_UNKNOWN);
            }
            break;
            case ACTION_GET_VALUES: {
                this.broadcastStatus(DS_UNKNOWN);
            }
            break;
            // [[wk
            case LockerProcessAtivity.HAND_SET_LOCKPASSWORD: {
                // handLockPassword
                byte cmd = 0;
                if (MainActivity.handLockPassword.length() == 15) {
                    cmd = 'o';
                } else {
                    cmd = 'O';
                }
                byte[] cmdBytes = Command.getInstance().AppSendLockerKey(cmd, MainActivity.handLockNumber, MainActivity.handLockPassword, null, 0);
                mDevice.Write(FileStream.write, cmdBytes);
                MainActivity.handLockPassword = null;
            }
            break;
            case SetActivity0.get_setValue: {
                synchronized (DeviceService.this) {
                    String str_SetType = b.getString(DeviceService.activity_SetType);
                    if (this.mCurrentActionChain == null || !this.mCurrentActionChain.getClass().getCanonicalName().equals("com.saintsung.saintpmc.lock.Set")) {
                        this.mCurrentActionChain = new Set(this); // 指定哪个页面获取
                    } else {
                        this.mCurrentActionChain.onConnect();
                        Log.d("onConnect()", "onConnect()" + "SetActivity0.get_setValue");
                    }
                /*
                 * 新协议
				 */
                    // send set
                    STR_CMD = "R";
                    byte[] cmdBytes = Command.getInstance().new_getS00SetValue();
                    mDevice.Write(FileStream.write, cmdBytes);
                }
            }
            break;

            case SetActivity0.send_setValue: {
                synchronized (DeviceService.this) {
                    String str_SetType = b.getString(DeviceService.activity_SetType);
                    if (this.mCurrentActionChain == null || !this.mCurrentActionChain.getClass().getCanonicalName().equals("com.saintsung.saintpmc.lock.Set")) {
                        this.mCurrentActionChain = new Set(this);
                    } else {
                        this.mCurrentActionChain.onConnect();
                        Log.d("onConnect()", "onConnect()" + "SetActivity0.send_setValue");
                    }
                /*
                 * 新协议
				 */
                    // send send_setValue
                    STR_CMD = "P";
                    if (mDevice != null) {
                        byte[] byte_data = intent.getByteArrayExtra(SetAllActivity.send_setValue);
                        mDevice.Write(FileStream.write, byte_data);

                        StringBuilder stringBuilder = new StringBuilder();
                        for (byte by_data : byte_data)
                            stringBuilder.append(String.format("%02x ", by_data));
                        Log.d("SetValue", "SetValue" + stringBuilder.toString());
                    }

                }
            }
            break;

            case SetAllActivity.update_time: {
                synchronized (DeviceService.this) {
                    // send send_setValue
                    mDevice.Write(FileStream.write, b.getByteArray(SetAllActivity.update_time));

                }
            }
            break;
            case SetActivity0.bluetoothName: {
                synchronized (DeviceService.this) {
                    // send setBluetoothName
                    mDevice.Write(SetActivity0.bluetoothName, b.getByteArray(SetActivity0.bluetoothName));

                }
            }
            break;
            case LockerProcessAtivity.send_userDownload: {
                synchronized (DeviceService.this) {
                    action_down_upload = intent.getStringExtra("menu_action");
                    fun_action_upload(); // 上传历史记录
                    // 先下载工单,离线开锁部分不再下载
                    //				sortLockNumber_Offline();
                }

            }
            break;
            case LockerProcessAtivity.send_sheet: {
                synchronized (DeviceService.this) {
                    // uploadHistory();
                    // 先下载工单,离线开锁部分不再下载
                    // sortLockNumber_Sheet();
                    // 先上传历史记录，然后再检测工单
                    action_down_upload = intent.getStringExtra("menu_action");
                    fun_action_upload(); // 上传历史记录
                }

            }
            break;
            case LockSetActivity.unlock_screw: {
                synchronized (DeviceService.this) {
                    if (b != null) {
                        // send open screw
                        if (MainActivity.handLockNumber != null) {
                        /*
						 * //send test byte[] cmdBytes =
						 * Command.getInstance().AppSendLockerKey
						 * (MCUCommand.opcode_open_screw
						 * ,MainActivity.handLockNumber,"000359168168000",0);
						 * mDevice.Write(FileStream.write,cmdBytes);
						 */
                            unlockScrew = b.getString(ScrewOperType);
                            ScrewNum = MainActivity.handLockNumber;
                            if (unlockScrew != null && unlockScrew.length() > 0 && unlockScrew.equals("Open")) {
                                addLog((byte) 0, "准备打开螺丝");
                                final String lockNum = b.getString("LockNum");
                                ScrewNum = lockNum;
                                readKey(lockNum, new OnReadKey() {

                                    @Override
                                    public void On(String key, String lockType, Exception e) {
                                        // TODO Auto-generated method stub
                                        Log.d("key", "key" + key);
                                        if (e == null) {
                                            byte cmd = 0;
                                            if (key.length() == 15) {
                                                cmd = 'o';
                                            } else {
                                                cmd = 'O';
                                            }

                                            byte[] cmdBytes = Command.getInstance().new_AppSendLockerKey(cmd, str_lock, key, lockType, 0, 0);
                                            mDevice.Write(FileStream.write, cmdBytes);

                                        } else {
                                            addLog((byte) 0, "找不到开锁码");
                                        }
                                    }
                                });
                            } else if (unlockScrew != null && unlockScrew.length() > 0 && unlockScrew.equals("Close")) {
                                addLog((byte) 0, "准备关闭螺丝");
                                byte[] cmdBytes = Command.getInstance().AppSendClose();
                                mDevice.Write(FileStream.write, cmdBytes);
                            }

						/*
						 * //在线开锁方式 boolean wlanFlag =
						 * NetworkConnect.checkNet(getApplicationContext()); if
						 * (wlanFlag) {
						 * download_single_lock_sn(MainActivity.handLockNumber,
						 * new IListener() {
						 *
						 * @Override public void dataComing(byte[] bData) { //
						 * TODO Auto-generated method stub addLog((byte) 0,
						 * "dataComing:" + new String(bData)); int startI = -1;
						 * int endI = -1; String recv = new String(bData); if
						 * ((startI = recv.indexOf("true")) != -1) { endI =
						 * recv.indexOf("endop", startI); String[] key_lockType
						 * = recv.split("\\|"); final String pwd =
						 * key_lockType[0]; final String lockType =
						 * key_lockType[1]; addLog((byte) 0, "dataComing+pwd:" +
						 * new String(bData)); byte[] cmdBytes =
						 * Command.getInstance().AppSendLockerKey(MCUCommand.
						 * opcode_open_screw, MainActivity.handLockNumber, pwd,
						 * lockType, 0); addLog((byte) 0, "dataComing+send:" +
						 * new String(cmdBytes));
						 * mDevice.Write(FileStream.write, cmdBytes); } else {
						 * // [[wk switch (mAutoTest) { case
						 * LockSetActivity.unlockLogin: { addLog((byte) 0,
						 * "该用户未实时关联此锁!");
						 *
						 * } break; case LockSetActivity.unlock_screw: {
						 * addLog((byte) 0, "该用户未实时关联此螺丝!"); } break; case
						 * LockSetActivity.unlock_well: { addLog((byte) 0,
						 * "该用户未实时关联此井盖!"); } break; default: { addLog((byte) 0,
						 * "未定义操作方式!"); } break; } // ]] } } }); } else {
						 * addLog((byte) 0, "请连接网络后,再进行其他操作!"); }
						 */
						/*
						 * // readKey(MainActivity.handLockNumber, new
						 * OnReadKey() {
						 *
						 * @Override public void On(String key, Exception e) {
						 * // TODO Auto-generated method stub if(e == null){
						 * byte cmd = 0; if(key.length() == 15){ cmd =
						 * MCUCommand.opcode_open_screw; }else{ cmd =
						 * MCUCommand.OPCODE_OPEN_LOCK; } byte[] cmdBytes =
						 * Command
						 * .getInstance().AppSendLockerKey(cmd,MainActivity
						 * .handLockNumber,key,0);
						 * mDevice.Write(FileStream.write,cmdBytes); } } });
						 */
                        }
                    }
                }
            }
            break;
            case SetActivity0.unlock_bluetooth: {
                synchronized (DeviceService.this) {
                    // change unlock_bluetooth
                    changeUnlockType(0);
                }
            }
            break;
            case SetActivity0.unlock_screw: {
                synchronized (DeviceService.this) {
                    // change unlock_screw
                    changeUnlockType(2);
                }
            }
            break;
            case SetActivity0.unlock_only: {
                synchronized (DeviceService.this) {
                    // change unlock_type to 掌机独立开锁
                    changeUnlockType(1);
                }
            }
            break;
            case action_upload:
                synchronized (DeviceService.this) {
                    // 只上传历史记录
                    action_down_upload = intent.getStringExtra("menu_action");
                    if (DeviceService.this.mCurrentActionChain != null && this.mCurrentActionChain.getClass().getCanonicalName().equals("com.saintsung.saintpmc.lock.UnlockScrew")) {
                        Log.d("dsfadsf", "fasdfasd");
                        ArrayList<String> record_data = new ArrayList<String>();
                        offline_uploadLog(record_data); // 上传螺丝记录
                    } else {
                        fun_action_upload(); // 上传历史记录
                    }
                }
                break;
            case ACTION_DELETE:
                synchronized (DeviceService.this) {
                    fun_delete_record();
                }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void fun_delete_record() {
        byte[] by_data = Command.getInstance().new_sendSheet(null);
        mDevice.Write(FileStream.write, by_data);
        //清除手机中的开关锁记录
        FileStream fileStream = new FileStream();
        fileStream.fileStream(FileStream.log, FileStream.delete, null);
        fileStream.fileStream(FileStream.sheetFile, FileStream.delete, null);
    }

    //// 上传历史记录
    public void fun_action_upload() {
        if (DeviceService.this.mCurrentActionChain == null || !DeviceService.this.mCurrentActionChain.getClass().getCanonicalName().equals("com.saintsung.saintpmc.lock.UploadHistory")) {
            // 上传记录计数器
            i_uploadItem = 1;
            DeviceService.this.mCurrentActionChain = new UploadHistory(DeviceService.this);
        } else {
            DeviceService.this.mCurrentActionChain.onConnect();

            Log.d("onConnect()", "onConnect()" + "sortLockNumber()");

        }
        // 发送上传命令
        // action_down_upload = upload_history;
        byte[] cmdBytes = Command.getInstance().AppUploadHistory();
        mDevice.Write(FileStream.write, cmdBytes);
    }

    // 下载工单
    public void fun_action_download_sheet() {
        sortLockNumber_Sheet();
    }

    // 生成锁
    void CreateActionChain() {
        switch (this.mAutoTest) {
            case LockSetActivity.unlockOriginal: { // 原值开锁
                this.mBigOpen = 0;
                if (this.mCurrentActionChain == null || !this.mCurrentActionChain.getClass().getCanonicalName().equals("com.saintsung.saintpmc.lock.OrgChain")) {
                    this.mCurrentActionChain = new OrgChain(this);
                } else {
                    this.mCurrentActionChain.onConnect();
                    Log.d("onConnect()", "onConnect()" + "LockSetActivity.unlockOriginal");
                }
            }
            break;
            case LockSetActivity.unlockTwo: { // 正负2开锁
                this.mBigOpen = 1;// 从大值开始测试
                synchronized (DeviceService.this) {

                    if (this.mCurrentActionChain == null || !this.mCurrentActionChain.getClass().getCanonicalName().equals("com.saintsung.saintpmc.lock.BigSmallChain")) {
                        this.mCurrentActionChain = new BigSmallChain(this);
                    } else {
                        this.mCurrentActionChain.onConnect();
                        Log.d("onConnect()", "onConnect()" + "LockSetActivity.unlockTwo");
                    }
                }

            }
            break;

            case LockSetActivity.unlockThree: { // 正负3还没完成
                this.mBigOpen = 1; // 从大值开始测试
                synchronized (DeviceService.this) {

                    if (this.mCurrentActionChain == null || !this.mCurrentActionChain.getClass().getCanonicalName().equals("com.saintsung.saintpmc.lock.BigSmallChain")) {
                        this.mCurrentActionChain = new BigSmallChain(this, LockSetActivity.unlockThree);
                    } else {
                        this.mCurrentActionChain.onConnect();
                        Log.d("onConnect() ", "onConnect()" + LockSetActivity.unlockThree);
                    }
                }

            }
            break;
            case LockSetActivity.unlockFour: { // 正负4还没完成
                this.mBigOpen = 1; // 从大值开始测试
                synchronized (DeviceService.this) {

                    if (this.mCurrentActionChain == null || !this.mCurrentActionChain.getClass().getCanonicalName().equals("com.saintsung.saintpmc.lock.BigSmallChain")) {
                        this.mCurrentActionChain = new BigSmallChain(this, LockSetActivity.unlockFour);
                    } else {
                        this.mCurrentActionChain.onConnect();
                        Log.d("onConnect() ", "onConnect()" + LockSetActivity.unlockFour);
                    }
                }

            }
            break;

            case LockSetActivity.unlockContinue: { // 连接开锁
                this.mBigOpen = 1;// 从大值开始测试
                synchronized (DeviceService.this) {

                    if (this.mCurrentActionChain == null || !this.mCurrentActionChain.getClass().getCanonicalName().equals("com.saintsung.saintpmc.lock.BigSmallChain")) {
                        this.mCurrentActionChain = new BigSmallChain(this);
                        // [[wk
                        // this.mCurrentActionChain = new OrgChain(this);
                        // ]]
                    } else {
                        this.mCurrentActionChain.onConnect();
                        Log.d("onConnect()", "onConnect()" + " LockSetActivity.unlockContinue");
                    }

                }
            }
            break;
            case LockSetActivity.unlockAutoReconnectTwo: {
                synchronized (DeviceService.this) {

                    if (this.mCurrentActionChain == null || !this.mCurrentActionChain.getClass().getCanonicalName().equals("com.saintsung.saintpmc.lock.AutoConnectBigSmallChain")) {
                        this.mCurrentActionChain = new AutoConnectBigSmallChain(this);
                    } else {
                        this.mCurrentActionChain.onConnect();
                        Log.d("onConnect()", "onConnect()" + " LockSetActivity.unlockAutoReconnectTwo");
                    }

                }
            }
            break;
            case LockSetActivity.unlockAutoReconnect: {
                synchronized (DeviceService.this) {

                    if (this.mCurrentActionChain == null || !this.mCurrentActionChain.getClass().getCanonicalName().equals("com.saintsung.saintpmc.lock.AutoReconnectChain")) {
                        this.mCurrentActionChain = new AutoReconnectChain(this);
                    } else {
                        this.mCurrentActionChain.onConnect();
                        Log.d("onConnect()", "onConnect()" + " LockSetActivity.unlockAutoReconnect");
                    }

                }
            }
            break;
            // [[wk
            case LockSetActivity.readLockNumber: {
                synchronized (DeviceService.this) {
                    if (this.mCurrentActionChain == null || !this.mCurrentActionChain.getClass().getCanonicalName().equals("com.saintsung.saintpmc.lock.ReadLockNumber")) {
                        this.mCurrentActionChain = new ReadLockNumber(this);
                    } else {
                        this.mCurrentActionChain.onConnect();
                        Log.d("onConnect()", "onConnect()" + "LockSetActivity.readLockNumber");
                    }
                }
            }
            break;
            case LockSetActivity.unlockLogin:
            case LockSetActivity.unlockUser: {
                synchronized (DeviceService.this) {
                    if (this.mCurrentActionChain == null || !this.mCurrentActionChain.getClass().getCanonicalName().equals("com.saintsung.saintpmc.lock.UnlockUser")) {
                        this.mCurrentActionChain = new UnlockUser(this);
                        // change unlock_bluetooth
                        // changeUnlockType(0);
                    } else {
                        this.mCurrentActionChain.onConnect();
                        Log.d("onConnect()", "onConnect()" + "LockSetActivity.unlockUser");
                        // begin handLockNumberUnlock
                        if (MainActivity.handLockNumber != null) {
                            // send fallDownLock
                            byte[] cmdBytes = Command.getInstance().AppLockerReady();
                            mDevice.Write(FileStream.write, cmdBytes);
                        }
                    }
                }
            }
            break;
            case LockSetActivity.unlock_screw: {
                synchronized (DeviceService.this) {
                    if (this.mCurrentActionChain == null || !this.mCurrentActionChain.getClass().getCanonicalName().equals("com.saintsung.saintpmc.lock.UnlockScrew")) {
                        this.mCurrentActionChain = new UnlockScrew(this);
                        // change unlock_screw
                        // changeUnlockType(2);
                    } else {
                        this.mCurrentActionChain.onConnect();
                        Log.d("onConnect()", "onConnect()" + "LockSetActivity.unlock_screw");
                    }
                }
            }
            break;
            case LockSetActivity.unlock_valve: {
                synchronized (DeviceService.this) {
                    if (this.mCurrentActionChain == null || !this.mCurrentActionChain.getClass().getCanonicalName().equals("com.saintsung.saintpmc.lock.UnlockValve")) {
                        this.mCurrentActionChain = new UnlockValve(this);
                        // change unlock_screw
                        // changeUnlockType(2);
                    } else {
                        this.mCurrentActionChain.onConnect();
                        Log.d("onConnect()", "onConnect()" + "LockSetActivity.unlock_valve");
                    }
                }
            }
            break;
            // ]]
        }
    }

    Runnable mAutoReconnectRunnable = new Runnable() {
        @Override
        public void run() {
/*		20170913
		//	DeviceService.this.autoReconnect();
*/
        }
    };

    void onConnect(Intent intent, int flags, int startId) {
        if (this.mDevice != null) {
            return;
        }
        this.mTestTimes = 0;
        this.mStartTime = new Date().getTime();
        this.mOkTimes = 0;
        this.mFailTimes = 0;
        // [[wk
        if (MainActivity.bluetoothNameSet != null) {
            mLastDeviceName = MainActivity.bluetoothNameSet;
            mAddres = intent.getExtras().getString(EXTRA_ADDRESS);
        } else {
            mLastDeviceName = intent.getExtras().getString(EXTRA_NAME);
            mAddres = intent.getExtras().getString(EXTRA_ADDRESS);
        }
        // ]]
        mLastAddress = intent.getExtras().getString(EXTRA_ADDRESS);
        this.addLog((byte) 0, getString(R.string.connecting));
        this.mDevice = BleDeviceMgr.getMgr().GetDevice(mLastAddress);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                /**
                 *要执行的操作
                 */
                onConnectBle();
            }
        }, 3000);//3秒后执行Runnable中的run方法
/*
		if (this.mDevice != null) {
			this.mDevice.SetClient(DeviceService.this);
			this.mDevice.Connect();
		}
*/
/*
		String url = "http://cgi.im.qq.com/cgi-bin/cgi_svrtime";
		fun_getStandardDate(url);
*/
    }

    //连接蓝牙
    public void onConnectBle() {
        if (this.mDevice != null) {
            this.mDevice.SetClient(DeviceService.this);
            this.mDevice.Connect();
        }
    }

    void onDisconnect(Intent intent, int flags, int startId) {
        MainActivity.isDown = false;
        if (this.mDevice == null) {
            return;
        }
        String address = intent.getExtras().getString(EXTRA_ADDRESS);
        if (this.mDevice.GetAddress().equals(address) == false) {
            return;
        }
        if (intent.getBooleanExtra(EXTRA_MANUAL, false)) {
            mHandler.removeCallbacks(mAutoReconnectRunnable);
            this.mDevice.SetClient(new DeviceClient() {
                @Override
                public void onConnected() {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onDisconnected() {
                    //断开蓝牙
                    DeviceService.this.broadcastStatus(DS_DISCONNECTED);
                    if (DeviceService.this.mCurrentActionChain != null) {
                        DeviceService.this.mCurrentActionChain.onDisconnect();
                    }
                    DeviceService.this.mDevice = null;
                    DeviceService.this.mCurrentActionChain = null;
                    // [[wk add
                    // common disconnect delete connectRecord
                    FileStream fileStream = new FileStream();
                    fileStream.fileStream(FileStream.connectRecord, FileStream.delete, null);
                    try {
                        WLog.logFile("手动操作断开连接");
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    // ]]
                }

                @Override
                public void onError(Exception e) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onRecv(byte[] data) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onRecv(String data) {
                    // TODO Auto-generated method stub

                }
            });
        }
        this.mDevice.Disconnect();
    }

    //
    void broadcastStatus(int status) {
        Intent i = new Intent();
        if (this.mLastAddress != null) {
            i.putExtra(EXTRA_ADDRESS, this.mLastAddress);
        }
        if (this.mLastDeviceName != null) {
            i.putExtra(EXTRA_NAME, this.mLastDeviceName);
        }
        i.setAction(ACTION_DEVICE_STATUS);
        i.putExtra(EXTRA_TEST_TIMES, this.mTestTimes);
        i.putExtra(EXTRA_OK_TIMES, this.mOkTimes);
        i.putExtra(EXTRA_FAIL_TIMES, this.mFailTimes);
        i.putExtra(EXTRA_START_TIME, this.mStartTime);
        i.putExtra(EXTRA_KEY_VALUE_TYPE, this.mBigOpen);
        i.putExtra(EXTRA_AUTO_TEST_TYPE, this.mAutoTest);
        i.putExtra(DeviceService.EXTRA_STATUS, status);
        this.sendBroadcast(i);
    }

    void broadcastStatus(int status, MCUCommand cmd) {
        Intent i = new Intent();
        i.setAction(ACTION_DEVICE_STATUS);
        // [[wk
        i.putExtra(EXTRA_NAME, this.mLastDeviceName);
        // ]]
        i.putExtra(EXTRA_ADDRESS, this.mLastAddress);
        i.putExtra(EXTRA_TEST_TIMES, this.mTestTimes);
        i.putExtra(EXTRA_OK_TIMES, this.mOkTimes);
        i.putExtra(EXTRA_FAIL_TIMES, this.mFailTimes);
        i.putExtra(EXTRA_START_TIME, this.mStartTime);
        i.putExtra(EXTRA_KEY_VALUE_TYPE, this.mBigOpen);
        i.putExtra(EXTRA_AUTO_TEST_TYPE, this.mAutoTest);
        i.putExtra(DeviceService.EXTRA_STATUS, status);
        this.sendBroadcast(i);
    }

    void broadcastStatus(int status, ErrCode errCode, String errMsg) {
        Intent i = new Intent();
        i.setAction(ACTION_DEVICE_STATUS);
        i.putExtra(EXTRA_ERR_CODE, errCode);
        i.putExtra(EXTRA_ERR_MSG, errMsg);
        // [[wk
        i.putExtra(EXTRA_NAME, this.mLastDeviceName);
        // ]]
        i.putExtra(EXTRA_ADDRESS, this.mLastAddress);
        i.putExtra(EXTRA_TEST_TIMES, this.mTestTimes);
        i.putExtra(EXTRA_OK_TIMES, this.mOkTimes);
        i.putExtra(EXTRA_FAIL_TIMES, this.mFailTimes);
        i.putExtra(EXTRA_START_TIME, this.mStartTime);
        i.putExtra(EXTRA_KEY_VALUE_TYPE, this.mBigOpen);
        i.putExtra(EXTRA_AUTO_TEST_TYPE, this.mAutoTest);
        i.putExtra(DeviceService.EXTRA_STATUS, status);
        this.sendBroadcast(i);
    }

    int count = 0;
    byte[] update_time = null;

    Runnable runnable_l = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
            standardDate = formatter.format(curDate);
            if (standardDate != null && standardDate.length() < 0) {
                //getDate();
                //standardDate = Command.getInstance().AppGetStandardDate(getApplicationContext(), url);
            }
            if (standardDate != null && standardDate.length() > 0) {
                update_time = Command.getInstance().AppConnectedLED(standardDate);
                mDevice.Write(FileStream.write, update_time);
                addLog((byte) 0, "当前时间 " + standardDate);
                count++;
                MainActivity.isDown = true;
            }

            if (count < 2) {
                mHandler.postDelayed(runnable_l, 1000);
            } else {
                mHandler.removeCallbacks(runnable_l);
            }

        }
    };

    public void getDate() {
        String url = "http://cgi.im.qq.com/cgi-bin/cgi_svrtime";
        OkHttpClient client = new OkHttpClient();
        //创建一个request
        final Request request = new Request.Builder().url(url).build();
        // new call
        Call call = client.newCall(request);
        //请求加入调度
        call.enqueue(new Callback() {

            @Override
            public void onResponse(Response arg0) throws IOException {
                // TODO Auto-generated method stub
                if (arg0.body().string() != null && arg0.body().string().length() > 0) {
                    standardDate = arg0.body().string();
                }
            }

            @Override
            public void onFailure(Request arg0, IOException arg1) {
                // TODO Auto-generated method stub

            }
        });
    }

    @Override
    public void onConnected() {
        // [[wk annotation/note
        this.mLastDeviceName = this.mDevice.GetName();
        MainActivity.connect_state = MainActivity.CONNECTED;
        // add connectRecord
        FileStream fileStream = new FileStream();
        fileStream.fileStream(FileStream.connectRecord, FileStream.write, mLastAddress.getBytes());
        // ]]
        this.mHandler.removeCallbacks(mAutoConnectDeviceRunnable);
        this.mHandler.removeCallbacks(mAutoReconnectRunnable);
        this.broadcastStatus(DS_CONNECTED);
        CreateActionChain();

        switch (LockSetActivity.unlockType) {
            case LockSetActivity.unlock_screw:
            case LockSetActivity.unlock_valve: {
                Intent intent = new Intent(getBaseContext(), DeviceService.class);
                intent.setAction(SetActivity0.unlock_screw);
                startService(intent);
            }
            break;
            default: {


            }
            break;
        }
        String url = "http://cgi.im.qq.com/cgi-bin/cgi_svrtime";
        fun_getStandardDate(url);

        if (mHandler != null) {
            mHandler.removeCallbacks(runnable_l);
            mHandler.postDelayed(runnable_l, 2000);
        }

    }

    final int mbtEabledCheckingTimer = 3000;
    final int mConnectedCheckingTimer = 20000;
    final int mDisableBtTimer = 30000;
    Runnable mAutoConnectDeviceRunnable = new Runnable() {
        @Override
        public void run() {
            DeviceService that = DeviceService.this;
            if (that.mDevice != null) {
                that.mDevice.Disconnect();
                // 自动引发autoReconnect
                return;
            }
            BluetoothManager bluetoothManager = (BluetoothManager) that.getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter.isEnabled()) {
                Intent i = new Intent();
                i.setClass(that, DeviceService.class);
                i.setAction(ACTION_CONNECT);
                // [[wk
                i.putExtra(DeviceService.EXTRA_NAME, mLastDeviceName);
                MainActivity.connect_state = getString(R.string.reconnecting);
                // ]]
                i.putExtra(DeviceService.EXTRA_ADDRESS, that.mLastAddress);
                that.startService(i);
                that.addLog((byte) 0, getString(R.string.reconnecting));
                // 拉长时间，以检测连接不通的情况
                that.mHandler.removeCallbacks(mAutoReconnectRunnable);
                that.mHandler.postDelayed(that.mAutoConnectDeviceRunnable, mConnectedCheckingTimer);
            } else {
                that.mHandler.postDelayed(that.mAutoConnectDeviceRunnable, mbtEabledCheckingTimer);
            }
        }
    };

    void autoReconnect() {
        this.addLog((byte) 0, MCUCommand.reconnecting);
        // 非手动断开，重启系统蓝牙模块，重连
        this.mHandler.removeCallbacks(mAutoConnectDeviceRunnable);
        this.mHandler.removeCallbacks(mAutoReconnectRunnable);
        this.mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                Context c = DeviceService.this.getApplicationContext();
                DeviceService that = DeviceService.this;
                BluetoothManager bluetoothManager = (BluetoothManager) c.getSystemService(Context.BLUETOOTH_SERVICE);
                final BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
                bluetoothAdapter.disable();
                try {
                    Thread.sleep(1000);
                    bluetoothAdapter.enable();
                    that.mHandler.postDelayed(that.mAutoConnectDeviceRunnable, mbtEabledCheckingTimer);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // 20秒后重试,一直重试直到手动断开
                that.mHandler.postDelayed(that.mAutoReconnectRunnable, mDisableBtTimer);
            }

        }, 1000);
    }

    @Override
    public void onDisconnected() {

        // if(user_Share.i_Auto == 0)
        {
            MainActivity.isDown = false;
            // change before
            this.addLog((byte) 0, "蓝牙已断开！");
            {
                //断开蓝牙
                DeviceService.this.broadcastStatus(DS_DISCONNECTED);

                this.mDevice.Disconnect();

                if (DeviceService.this.mCurrentActionChain != null) {
                    DeviceService.this.mCurrentActionChain.onDisconnect();
                }
                DeviceService.this.mDevice = null;
                DeviceService.this.mCurrentActionChain = null;
                // [[wk add
                // common disconnect delete connectRecord
                FileStream fileStream = new FileStream();
                fileStream.fileStream(FileStream.connectRecord, FileStream.delete, null);
                try {
                    WLog.logFile("手动操作断开连接");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // ]]
            }

/*	20170913 取消自动连
		//	this.mDevice = null;
		//	this.autoReconnect();
*/
        }


    }

    @Override
    public void onError(Exception e) {
        this.broadcastStatus(DS_ERROR, ErrCode.NORMAL, e.getMessage());
    }

    int mOkTimes = 0;
    int mFailTimes = 0;
    int mTestTimes = 0;
    long mStartTime = new Date().getTime();

    void IncTestTime(boolean ok) {
        synchronized (this) {
            mTestTimes++;
            if (ok) {
                mOkTimes++;
            } else {
                mFailTimes++;
            }
        }
		/*
		 * //[[wk FileStream fileStream=new FileStream();
		 * fileStream.fileStream(FileStream.screw_log, FileStream.write,
		 * ("开/关锁次数:"+mTestTimes+"\r\n").getBytes()); //]]
		 */
    }

    String mLogs = "";
    int mLogLine = 0;

    void addLog(final byte b, final String logMsg) {
        Intent i = new Intent();
        i.setAction(ACTION_DEVICE_STATUS);
        i.putExtra(DeviceService.EXTRA_STATUS, DS_LOG);
        i.putExtra(EXTRA_LOG, logMsg);
        // [[wk add
        i.putExtra(EXTRA_NAME, DeviceService.this.mLastDeviceName);
        // ]]
        i.putExtra(EXTRA_ADDRESS, DeviceService.this.mLastAddress);
        i.putExtra(EXTRA_TEST_TIMES, DeviceService.this.mTestTimes);
        i.putExtra(EXTRA_OK_TIMES, DeviceService.this.mOkTimes);
        i.putExtra(EXTRA_FAIL_TIMES, DeviceService.this.mFailTimes);
        i.putExtra(EXTRA_START_TIME, DeviceService.this.mStartTime);
        i.putExtra(EXTRA_KEY_VALUE_TYPE, DeviceService.this.mBigOpen);
        i.putExtra(EXTRA_AUTO_TEST_TYPE, DeviceService.this.mAutoTest);
        DeviceService.this.sendBroadcast(i);

        Date now = new Date();
        long diff = now.getTime() - mStartTime;
        long days = diff / (1000 * 60 * 60 * 24);
        long hours = (diff - days * (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (diff - days * (1000 * 60 * 60 * 24) - hours * (1000 * 60 * 60)) / (1000 * 60);
        String timestr = "" + days + "天" + hours + "小时" + minutes + "分";
        String passTime = String.format("Totall：%d，ok:%d,fail:%d,Time:%s", mTestTimes, mOkTimes, mFailTimes, timestr);
        String string = String.format("%s:%s", logMsg, passTime);
        Log.d("addLog", string);
        try {
            WLog.logFile(string);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    interface OnReadKey {
        void On(String key, String lockType, Exception e);
    }

    String readKey(final String number, final OnReadKey on) {
        String key = "FFFFFFFFFFFFFFF|FFFF";
        final LockInformation lockInformation = new Select().from(LockInformation.class).where(Condition.column(LockInformation$Table.LOCKNO).is(number)).querySingle();
        if (lockInformation == null) {
            on.On(null, null, new Exception("没有权限开此设备!"));
        } else {
            Date day = new Date();
            if (DataProcess.isInDate(day, lockInformation.starTime, lockInformation.endTime)) {
                mDevice.PostRun(new Runnable() {
                    @Override
                    public void run() {
                        on.On(lockInformation.optPwd, lockInformation.type, null);
                    }
                });
            } else {
                on.On(null, null, new Exception("工单已过期!"));
            }
        }
        switch (mAutoTest) {
            case LockSetActivity.unlockAutoReconnectTwo:
            case LockSetActivity.unlockContinue:
            case LockSetActivity.unlockTwo:
            case LockSetActivity.unlockThree:
            case LockSetActivity.unlockFour:
            case LockSetActivity.unlockOriginal: {
                try {
                    FileStream fileStream = new FileStream();
                    byte[] byteArray = fileStream.fileStream(FileStream.jsonFile, FileStream.read, null);
                    if (byteArray.length == 0) {
                        on.On(null, null, new Exception("手动导入lock.json文件到手机本地APP文件夹后,再尝试本地开锁测试!"));
                    } else {
                        JSONObject mKeyJson = new JSONObject(new String(byteArray));
                        key = mKeyJson.get(number).toString();
                        if (key.length() > 0) { // lock.json中找到的
                            String lockType = "";
                            String[] tmp = key.split("\\|");
                            key = tmp[0];
                            lockType = tmp[1];
                            on.On(key, lockType, null); // 原值默认用"锁"
                        }

                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    on.On(null, null, new Exception("本地json文件不支持打开此锁!"));
                }
            }
            break;
            // [[wk
            case LockSetActivity.unlock_screw:
            case LockSetActivity.unlockUser: {
                // 开锁(用户离线关联):
//                userDownload(number, on);
                // key = "";
            }
            break;
            default: {
                on.On(null, null, new Exception("未定义操作方式!"));
            }
            break;
            // ]]
        }

        if (key.length() <= 0) {
            // 读锁号无回应，刚重新开始
            mCurrentActionChain.reset();
        }
        // ]]
        return key;
    }

    //锁类型怎么补充前面的0
    public String fillWithPre(int total, String data) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < total - data.length(); i++) {
            stringBuilder.append("0");
        }
        stringBuilder.append(data);
        return stringBuilder.toString();
    }


    // 开锁，开螺丝没有找到开锁码
    public void fun_NoKey() {

        mDevice.PostRun(new Runnable() {
            @Override
            public void run() {
                // [[wk
                switch (mAutoTest) {
                    case LockSetActivity.unlockLogin: {
                        addLog((byte) 0, "该用户未实时关联此锁!");
                        mCurrentActionChain.reset();
                    }
                    break;
                    case LockSetActivity.unlock_screw: {
                        addLog((byte) 0, MCUCommand.root_screw_disabled);
                    }
                    break;
                    case LockSetActivity.unlock_well: {
                        addLog((byte) 0, "该用户未实时关联此井盖!");
                    }
                    break;
                    default: {
                        addLog((byte) 0, "未定义操作方式!");
                    }
                    break;
                }
                // must clear
                MainActivity.handLockNumber = null;
                // ]]
            }
        });

    }


    public void download_single_lock_sn(String number, QueryAddressTask.resultService listener) {
        FileStream fileStream = new FileStream();
        // get userNameValue
        byte[] byt = fileStream.fileStream(FileStream.socket, FileStream.read, null);
        String str = new String(byt);
        String[] strBy = str.split(":");
        QueryAddressTask queryAddressTask = new QueryAddressTask();
        queryAddressTask.execute("GET_LOCK_INFO", strBy[0], number, "1");
        queryAddressTask.setResultStr(listener);
    }

    boolean mOpenAction = false;
    boolean mSimulateCloseReady = false;// 模拟关锁落成功
    boolean mPressCloseKey = false;
    boolean mOnlineKey = false;
    // >0 自动测试， == 1则在开锁成功后自动发锁落锁指令完成自动尖锁,完成大小值自动测试，不循环,==2循环大小值测试,== 3 联网下载密码测试
    // == 4 10次大不值后自动重连牙测试
    // [[wk change int to string
    // int mAutoTest = 1;
    String mAutoTest = LockSetActivity.unlockUser;
    // ]]
    int mBigOpen = 1;// 0 正常，1大值，2小值

    String getAutoTest() {
        synchronized (this) {
            return this.mAutoTest;
        }
    }

    void setBigOpen(int open) {
        synchronized (this) {
            this.mBigOpen = open;
        }
    }

    int getBigOpen() {
        int v = 0;
        synchronized (this) {
            v = this.mBigOpen;
        }
        return v;
    }

    boolean flag = false;

    // 新协议分发数据
    public void Dispatch_Data(String v_data) {
        String m_data = "";
        String str_data = "";
        int offset = 0;
        m_data = v_data;
        int i_offset = 0;
        String sub_status = "";
        // TODO Auto-generated method stub
        switch (m_data.charAt(cmd_index)) {
            case MCUCommand.OPCODE_CONNECTED:
                sub_status = m_data.substring(cmd_index + 1, cmd_index + 3);
                if (sub_status.equals(MCUCommand.ERRCODE_SUCC)) {
                    this.mHandler.post(new ActionRunnable(m_data));
                }
                break;
            case MCUCommand.AppReset:
                sub_status = m_data.substring(cmd_index + 1, cmd_index + 3);
                switch (sub_status) {
                    case MCUCommand.ERRCODE_SUCC:
                        addLog((byte) 0x00, "复位成功");
                        break;
                }

                break;
            case 'P':
                // addLog((byte)0, "设置成功!");
                // Toast.makeText(this, "设置成功", Toast.LENGTH_SHORT).show();
            case 'R':
                intent = new Intent();
                intent.setClass(this, SetAllActivity.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
                i_offset += 2;
                // 时间
                str_data = m_data.substring(i_offset + 5, i_offset + 5 + 14);
                intent.putExtra(SetAllActivity.update_time, str_data);
                // 序列号
                i_offset += 19;
                str_data = m_data.substring(i_offset, i_offset + 20);
                intent.putExtra(SetAllActivity.S00SerialNumber, str_data);
                // 设备名
                i_offset += 20;
                str_data = m_data.substring(i_offset, i_offset + 20);
                intent.putExtra(SetAllActivity.bluetoothName, str_data);
                // 偏移值
                i_offset += 20;
                str_data = m_data.substring(i_offset, i_offset + 2);
                intent.putExtra(SetAllActivity.offset, str_data);
                // 设置值
                i_offset += 2;
                str_data = m_data.substring(i_offset, i_offset + 2);
                intent.putExtra(SetAllActivity.set, str_data);
                // 固件号
                i_offset += 2;
                str_data = m_data.substring(i_offset, i_offset + 10);
                intent.putExtra(SetAllActivity.S00SoftwareVersion, str_data);
                // 硬件号
                i_offset += 10;
                str_data = m_data.substring(i_offset, i_offset + 10);
                intent.putExtra(SetAllActivity.S00HardwareVersion, str_data);
                startActivity(intent);
                break;

            case MCUCommand.App_Unlock: // 开锁
                sub_status = m_data.substring(cmd_index + 1, cmd_index + 3);
                switch (sub_status) {
                    case MCUCommand.ERRCODE_SUCC:
                        str_data = m_data.substring(cmd_index + 3, cmd_index + 3 + 9);
                        addLog((byte) 0x00, "开锁操作 锁号 :" + str_data);
                        fun_SendKeyCode(str_data);
                        str_lock = str_data;
//						if(!flag) {
//							flag=true;
//							FileStream fileStream = new FileStream();
//							byte[] byt = fileStream.fileStream(FileStream.socket, FileStream.read, null);
//							String str = new String(byt);
//							String[] strBy = str.split(":");
//							QueryAddressTask queryAddressTask = new QueryAddressTask();
//							queryAddressTask.execute("GET_LOCK_INFO", strBy[0], str_data, "1");
//							queryAddressTask.setResultStr(this);
//						}
                        break;

                    case MCUCommand.ERRCODE_WrongDataLen:
                        addLog((byte) 0x00, "开锁操作 数据长度错误！");
                        break;
                    case MCUCommand.ERRCODE_IncompletedPack:
                        addLog((byte) 0x00, "开锁操作 数据包不完整！");
                        break;
                    case MCUCommand.ERRCODE_UnknownCmd:
                        addLog((byte) 0x00, "开锁操作 未知命令！");
                        break;
                    case MCUCommand.ERRCODE_MotorTrap:
                        addLog((byte) 0x00, "开锁操作 电机阻转！");
                        break;
                    case MCUCommand.ERRCODE_MotorTimeout:
                        addLog((byte) 0x00, "开锁操作 读锁号超时！");
                        break;
                    case MCUCommand.ERRCODE_MotorLose:
                        addLog((byte) 0x00, "开锁操作 无电机！");
                        break;
                    case MCUCommand.ERRCODE_MotorRunFail:
                        addLog((byte) 0x00, "开锁操作 电机运转失败！");
                        break;
                    case MCUCommand.ERRCODE_OpenLockFail:
                        addLog((byte) 0x00, "开锁操作 开锁失败！");
                        break;
                    case MCUCommand.ERRCODE_ReadLockSnFail:
                        addLog((byte) 0x00, "开锁操作 读锁号失败！");
                        break;
                    case MCUCommand.ERRCODE_MotorNotInited:
                        addLog((byte) 0x00, "开锁操作 电机未初始化！");
                        break;
                    case MCUCommand.ERRCODE_MotorResetFail:
                        addLog((byte) 0x00, "开锁操作 电机初始化失败！");
                        break;
                    case MCUCommand.ERRCODE_Reset:
                        addLog((byte) 0x00, "开锁操作 重置！");
                        break;
                    case MCUCommand.ERRCODE_DownNot:
                        addLog((byte) 0x00, "开锁操作 不能按下！");
                        break;
                    case MCUCommand.ERRCODE_UpNot:
                        addLog((byte) 0x00, "开锁操作 不能弹起！");
                        break;
                    default:
                        break;
                }
                // break;
                // }

                break;
            case MCUCommand.App_Unlock_Status:
                Log.d("unlock555", "unlock555" + m_data);
                sub_status = m_data.substring(cmd_index + 1, cmd_index + 3);
                switch (this.mAutoTest) {
                    case LockSetActivity.unlock_screw: {
                        //if (sub_status.equals(MCUCommand.ERRCODE_SUCC))
                        {
                            this.mHandler.post(new ActionRunnable(m_data));
                        }
                    }
                    break;

                    default:
                        if (sub_status.equals(MCUCommand.ERRCODE_SUCC)) {
                            addLog((byte) 0x00, "开锁成功: " + str_lock);
                            // 上传关锁记录到服务器
                            uploadLog(MCUCommand.operateunlock_succ, str_lock, null);
                        } else if (sub_status.equals(MCUCommand.ERRCODE_OpenLockFail)) {
                            addLog((byte) 0x00, "开锁失败: " + str_lock);
                            // 上传关锁记录到服务器
                            uploadLog(MCUCommand.operateunlock_fail, str_lock, null);
                        }

                        break;
                }

                break;
            case MCUCommand.App_Lock:
                sub_status = m_data.substring(cmd_index + 1, cmd_index + 3);
                str_lock = m_data.substring(cmd_index + 3, cmd_index + 3 + 9);
                if (false) {
                    if (sub_status.equals(MCUCommand.ERRCODE_SUCC)) {

                        addLog((byte) 0x00, MCUCommand.close_lock_ok + str_lock);
                        // 上传关锁记录到服务器
                        // uploadLog(MCUCommand.operateunlock_succ, str_lock,null);
                        byte[] cmdBytes = Command.getInstance().AppSendClose();
                        mDevice.Write(FileStream.write, cmdBytes);

                    } else if (sub_status.equals(MCUCommand.ERRCODE_OpenLockFail)) {
                        addLog((byte) 0x00, MCUCommand.close_lock_fail + str_lock);
                    } else if (sub_status.equals(MCUCommand.ERRCODE_MotorTimeout)) {
                        // addLog((byte) 0x00, MCUCommand.close_lock_timout +
                        // str_lock);
                        addLog((byte) 0x00, MCUCommand.close_lock_timout + "请检查锁与掌机连接");
                    }
                }
                switch (sub_status) {
                    case MCUCommand.ERRCODE_SUCC:
                        addLog((byte) 0x00, "关锁操作 " + MCUCommand.close_lock_ok + str_lock);
                        // 上传关锁记录到服务器
                        // uploadLog(MCUCommand.operateunlock_succ, str_lock,null);
                        byte[] cmdBytes = Command.getInstance().AppSendClose();
                        mDevice.Write(FileStream.write, cmdBytes);
                        break;
                    case MCUCommand.ERRCODE_WrongDataLen:
                        addLog((byte) 0x00, "关锁操作 数据长度错误！");
                        break;
                    case MCUCommand.ERRCODE_IncompletedPack:
                        addLog((byte) 0x00, "关锁操作 数据包不完整！");
                        break;
                    case MCUCommand.ERRCODE_UnknownCmd:
                        addLog((byte) 0x00, "关锁操作 未知命令！");
                        break;
                    case MCUCommand.ERRCODE_MotorTrap:
                        addLog((byte) 0x00, "关锁操作 电机阻转！");
                        break;
                    case MCUCommand.ERRCODE_MotorTimeout:
                        addLog((byte) 0x00, "关锁操作 读锁号超时！");
                        break;
                    case MCUCommand.ERRCODE_MotorLose:
                        addLog((byte) 0x00, "关锁操作 无电机！");
                        break;
                    case MCUCommand.ERRCODE_MotorRunFail:
                        addLog((byte) 0x00, "关锁操作 电机运转失败！");
                        break;
                    case MCUCommand.ERRCODE_OpenLockFail:
                        addLog((byte) 0x00, "关锁操作 开锁失败！");
                        break;
                    case MCUCommand.ERRCODE_ReadLockSnFail:
                        addLog((byte) 0x00, "关锁操作 读锁号失败！");
                        break;
                    case MCUCommand.ERRCODE_MotorNotInited:
                        addLog((byte) 0x00, "关锁操作 电机未初始化！");
                        break;
                    case MCUCommand.ERRCODE_MotorResetFail:
                        addLog((byte) 0x00, "关锁操作 电机初始化失败！");
                        break;
                    case MCUCommand.ERRCODE_Reset:
                        addLog((byte) 0x00, "关锁操作 重置！");
                        break;
                    case MCUCommand.ERRCODE_DownNot:
                        addLog((byte) 0x00, "关锁操作 不能按下！");
                        break;
                    case MCUCommand.ERRCODE_UpNot:
                        addLog((byte) 0x00, "关锁操作 不能弹起！");
                        break;
                    default:
                        break;
                }


                break;
            case MCUCommand.EREASE_LOCK_KEY:
                Log.d("erease", "erease" + str_data);
                dw_flag = 1;
                sub_status = m_data.substring(cmd_index + 1, cmd_index + 3);

                if (sub_status.equals(MCUCommand.ERRCODE_SUCC)) {
                    this.mHandler.post(new ActionRunnable(m_data));
                    addLog((byte) 0, "存储记录已清除!");
                }
                break;

            case MCUCommand.DownloadKeyAndLock:
                sub_status = m_data.substring(cmd_index + 1, cmd_index + 3);
                if (sub_status.equals(MCUCommand.ERRCODE_SUCC)) {
                    this.mHandler.post(new ActionRunnable(m_data));
                }
                break;
            case MCUCommand.AppUploadHistory:
                String str_lenth = m_data.substring(0, 4);

                if (!str_lenth.equals("0000")) {
                    sub_status = m_data.substring(cmd_index + 1, cmd_index + 3);
                    if (sub_status.equals(MCUCommand.ERRCODE_SUCC)) {
                        this.mHandler.post(new ActionRunnable(m_data));
                    }
                } else {
                    if (i_uploadItem > 1) {
                        addLog((byte) 0, "开关锁记录读完！");
                    } else {
                        addLog((byte) 0, "开关锁记录为空！");
                        if (action_down_upload.equals(download_sheet)) { // 工单菜单，检测有无工单
                            // 下载工单
                            sortLockNumber_Sheet();
                        } else if (action_down_upload.equals(download_offline)) { //离线菜单，检测有无记录
                            sortLockNumber_Offline();
                        }
                    }
                }

                break;
            case MCUCommand.AppLock:
                sub_status = m_data.substring(cmd_index + 1, cmd_index + 3);
                //	if (sub_status.equals(MCUCommand.ERRCODE_SUCC))
            {
                switch (this.mAutoTest) {
                    case LockSetActivity.unlock_screw: {
                        //	if (sub_status.equals(MCUCommand.ERRCODE_SUCC))
                        {
                            this.mHandler.post(new ActionRunnable(m_data));
                        }
                    }
                    break;
                    default:
                        addLog((byte) 0x00, "关锁成功");
                        // 上传关锁记录到服务器
                        uploadLog(MCUCommand.operatelock_succ, str_lock, null);
                        break;
                }

            }

            break;
            default:
                break;
        }

    }

    // 在线开锁发送开锁码
    public void fun_SendKeyCode(final String str_lock) {
        MainActivity.handLockNumber = str_lock;
        mAutoTest = LockSetActivity.unlockType;
        readKey(MainActivity.handLockNumber, new OnReadKey() {
            @Override
            public void On(final String key, final String lockType, Exception e) {
                if (e == null) {
                    byte cmd = 0;
                    if (key.length() == 15) {
                        cmd = 'o';
                    } else {
                        cmd = 'O';
                    }

                    byte[] cmdBytes = Command.getInstance().new_AppSendLockerKey(cmd, str_lock, key, lockType, 0, 0);
                    mDevice.Write(FileStream.write, cmdBytes);

                } else {
                    addLog((byte) 0, e.getMessage());
                }
            }
        });
    }

    int i_record_data_count = 0;
    String str_record_data_total = "";

    ActionChain mCurrentActionChain = null;

    class ActionRunnable implements Runnable {
        byte[] mData = null;
        MCUCommand mMcuCmd = null;

        // 新协议
        String str_Data = null;

        ActionRunnable(String data) {
            // TODO Auto-generated constructor stub
            this.str_Data = data;
        }

        ActionRunnable(byte[] data) {
            this.mData = data;

        }

        @Override
        public void run() {
            RecvAction action = null;
            synchronized (DeviceService.this) {
                if (mCurrentActionChain == null) {
                    return;
                }
                action = mCurrentActionChain.Next();        //一个大坑
                if (action == null) {
                    return;
                }
            }
            Command command = Command.getInstance();

            // 旧协议注释
            // mMcuCmd = command.Parse(mData, 0, mData.length);

            // 新协议
            mMcuCmd = command.new_Parse(mAutoTest, str_Data);

            // mMcuCmd= Command.getInstance().Parse(mData, 0, mData.length);
            if (command == null || mMcuCmd == null) {
                Log.d("command", "command");
            }
            switch (mMcuCmd.mCmd) {
                case 'f':
                case 'F': {
                    byte[] cmdBytes = Command.getInstance().AppResponseKeyReady();
                    DeviceService.this.mDevice.Write(FileStream.write, cmdBytes);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
                break;
                case MCUCommand.EREASE_LOCK_KEY: {
                    //记录已经清除了

                }
                break;
                case MCUCommand.OPCODE_CONNECTED:
                    String temp = mMcuCmd.mAttechString;
                    temp = temp.trim();
                    String tmp = getString(R.string.default_device_id);
                    if (temp.contains(tmp)) {
                        DeviceService.this.addLog((byte) 0, getString(R.string.set_device_id)); //收到'l'命令
                    } else {
                        MainActivity.DeviceID = temp;
                        //第一没有休眠，第二网络可以连接，第三蓝牙连接
                        // 验证网络连接
//						boolean wlanFlag = NetworkConnect.checkNet(getApplicationContext());
//						if (wlanFlag) {
//							//发送上传命令
//							if (count >= 2) {
//								fun_action_upload();
//							}
//						} else {
//							addLog((byte) 0, "请打开网络");
//						}
                    }
                    break;

                case MCUCommand.opcode_sleep: {
                    MainActivity.state_sleep = MainActivity.SLEEP;
                }

                default:
                    break;
            }
            try {
                String string = mCurrentActionChain.getClass().getCanonicalName();
                byte by_sigle = str_Data.substring(4, 5).getBytes()[0];
                switch (by_sigle) {
                    case MCUCommand.AppUploadHistory:
                        if (string.equals("com.saintsung.saintpmc.lock.UploadHistory")) {
                            if (str_Data.length() <= 7) { //屏蔽掉e，再次上传。
                                return;
                            }
                            if (str_Data.substring(4, 5).equals("U")) {
                                if (str_Data.contains("upload start")) {
                                    //					addLog((byte) 0, "读取到" + String.valueOf(i_uploadItem++) + "条记录");
                                    i_record_data_count = 0; //读取到
                                    str_record_data_total = String.valueOf(Integer.parseInt(str_Data.substring(str_Data.length() - 32 - 36 - 8, str_Data.length() - 32 - 36))); //总条数
                                    array_record.add(str_Data.substring(str_Data.length() - 32 - 56, str_Data.length() - 32));
                                    addLog((byte) 0, "开关锁记录读取中");
                                } else if (str_Data.contains("upload over")) {
                                    array_record.add(str_Data.substring(str_Data.length() - 32 - 20, str_Data.length() - 32));
                                    if (array_record.size() > 2) {
                                        String str_record_result = array_record.get(0) + ":";
                                        for (int i = 1; i < array_record.size() - 1; i++) {
                                            str_record_result += array_record.get(i);
                                        }
                                        str_record_result += ":" + array_record.get(array_record.size() - 1);
                                        array_record.clear(); //先不下载工单
                                        addLog((byte) 0, "开关锁记录读取完");
                                        action.Do(mMcuCmd, str_record_result);
                                    } else {
                                        addLog((byte) 0, "开关锁记录为空！");
                                    }

                                } else {
                                    String str_lock_record = str_Data.substring(7, str_Data.length() - 32);
                                    array_record.add(str_lock_record);
                                    i_record_data_count += str_lock_record.length() / 24;
                                    addLog((byte) 0, "读取中" + i_record_data_count + "/" + str_record_data_total);
                                }
                            }

                        }
                        break;
                    default:
                        break;
                }

                //用户授权，
                if (string.equals("com.saintsung.saintpmc.lock.SendUserDownload")) {
                    if (mData != null && mData.length > 0) {
                        action.Do(mMcuCmd, mData);
                    }
                }
                //工单授权
                if (string.equals("com.saintsung.saintpmc.lock.SendSheet")) {
                    if (str_Data != null && str_Data.length() > 0) {
                        action.Do(mMcuCmd, str_Data);
                    }
                }

                if (string.equals("com.saintsung.saintpmc.lock.UnlockScrew")) {
                    action.Do(mMcuCmd, str_Data);
                }

            } catch (UnProcException e) {
                mCurrentActionChain.reset();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                DeviceService.this.IncTestTime(false);
                DeviceService.this.addLog((byte) 0, e.getMessage());
            }
        }
    }

    @Override
    public void onRecv(String data) {
        // MainActivity.state_sleep = null;

        try {
            WLog.logFile("收到的数据：" + data);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        Intent intent = new Intent();
        String str_state = "";
        if (data == null || data.length() <= 0) {
            return;
        }
        // 下面语句可以合为一条
        switch (data.charAt(cmd_index)) {
            case MCUCommand.OPCODE_CONNECTED:
                CheckMD5(data); // 校验MD5
                break;
            case MCUCommand.AppReset:
                CheckMD5(data);    //校验MD5
                break;
            case 'R':
            case 'P':
                CheckMD5(data); // 校验MD5
                break;
            case 'D':
                // Log.d("lll", "lll"+data); //下载开锁码
                CheckMD5(data); // 校验MD5
                break;
            case MCUCommand.App_Unlock: // 开锁
                CheckMD5(data); // 校验MD5
                break;
            case MCUCommand.App_Unlock_Status:
                CheckMD5(data); // 校验MD5
                break;
            case MCUCommand.App_Lock: // 关锁
                CheckMD5(data); // 校验MD5
                break;
            case MCUCommand.AppLock: // 关锁状态
                CheckMD5(data); // 校验MD5
                break;
            case MCUCommand.EREASE_LOCK_KEY: // 擦除flash
                CheckMD5(data);
                break;
            case MCUCommand.AppUploadHistory: // 上传开关锁记录
                CheckMD5(data);
                break;
            case MCUCommand.AppLowPower: // 低电量
                // this.mHandler.post(new ActionRunnable(data));
                addLog((byte) 0, MCUCommand.warn_charge);
                break;
            case MCUCommand.AppSleep: // 休眠电量
                MainActivity.state_sleep = MainActivity.SLEEP;
                addLog((byte) 0, getString(R.string.App_Sleep));

                intent.setAction(ACTION_DEVICE_SLEEP_WAKEIP);
                intent.putExtra(MainActivity.state_sleep, MainActivity.SLEEP);
                sendBroadcast(intent);// 传递过去
                break;
            case MCUCommand.AppWakeUp: // 唤醒
                MainActivity.state_sleep = MainActivity.WAKEUP;
                addLog((byte) 0, getString(R.string.App_WakeUp));

                intent.setAction(ACTION_DEVICE_SLEEP_WAKEIP);
                intent.putExtra(MainActivity.state_sleep, MainActivity.WAKEUP);
                sendBroadcast(intent);// 传递过去
                break;

            default:
                break;
        }

    }

    // 验证MD5是否正确
    public void CheckMD5(String data) {
        if (data.length() < 32) {
            Dispatch_Data(data);
            return;
        }
        String check_code = data.substring(data.length() - i_md5_length, data.length());
        String st = data.substring(0, data.length() - i_md5_length);
        String str_md5 = user_Share.byteToMD5(data.substring(0, data.length() - i_md5_length));
        if (check_code.equals(str_md5)) {
            Dispatch_Data(data);
            // 清除标记
            // STR_CMD = "";
        }
    }

    @Override
    public void onRecv(byte[] data) {
        // TODO Auto-generated method stub

        this.mHandler.post(new ActionRunnable(data));
    }

    public interface RecvAction {
        // 收到mcu的数据后的执行操作,
        void Do(MCUCommand mcuCmd, byte[] data) throws Exception;

        void Do(MCUCommand mcuCmd, String data) throws Exception;
    }

    public interface ActionChain {
        RecvAction Next();

        void onConnect();

        void onDisconnect();

        void reset();
    }

    // [[wk
    void userDownload(String number, final OnReadKey on) {
        FileStream fileStream = new FileStream();
        byte[] byteArray = fileStream.fileStream(FileStream.userDownload, FileStream.read, null);
        if (byteArray == null || byteArray.length == 0) {
            on.On(null, null, new Exception("请先下载您关联的锁具信息后,再操作开锁!"));
        } else {
            String string = new String(byteArray);

            String[] stringArray = string.split("\r\n");
            int length = stringArray.length;
            boolean b = false;
            for (int i = 0; i < length; i++) {
                string = stringArray[i];
                final String[] stringArray0 = string.split(":");
                final String str_key = stringArray0[1];
                final String str_lockType = stringArray0[2];
                string = stringArray0[0];
                // 离线开锁
                if (string.equals(number)) {
                    b = true;
                    // 打包发送开锁密码
                    mDevice.PostRun(new Runnable() {
                        @Override
                        public void run() {
                            // on.On(stringArray0[1], null);
                            on.On(str_key, str_lockType, null);
                        }
                    });
                    break;
                }
                if (i == (length - 1) && b == false) {
                    // 打包发送开锁密码
                    mDevice.PostRun(new Runnable() {

                        @Override
                        public void run() {
                            String str_lockType = new String();
                            stringArray0[1] = "FFFFFFFFFFFFFFF";
                            str_lockType = "FFFF";
                            on.On(stringArray0[1], str_lockType, null);
                        }

                    });
                    break;
                }

            }
            // lockPwd not exist
            if (!b) {
                on.On(null, null, new Exception(MCUCommand.unlock_disabled));
            }
        }
    }

    //
    void set(byte name, String direction, String string) {
        String activityName = getActivityManager();
        switch (activityName) {
            // 主界面
            case activity_main: {
                switch (name) {
                    case SetActivity0.VS_S00SerialNumber:
                        // service里跳转页面前提
                        // before sendBroadcast must new Intent();
                        intent = new Intent();
                        intent.setClass(this, SetAllActivity.class);
                        intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(SetActivity0.bluetoothName, this.mLastDeviceName);
                        // [[CXQ 掌机序列号，如果收到20个FF就用MAC地址代替
                        intent.putExtra(SetActivity0.bluetoothAddress, this.mAddres);
                        // ]]
                        intent.putExtra(SetActivity0.S00SerialNumber, string);
                        break;
                    // 固件版本
                    case SetActivity0.VS_FW51_VER_GOT:
                        intent.putExtra(SetActivity0.S00SoftwareVersion, string);
                        break;
                    // 硬件版本
                    case SetActivity0.VS_HW_GOT:
                        intent.putExtra(SetActivity0.S00HardwareVersion, string);
                        break;
                    // 偏移值
                    case SetActivity0.VS_LOFFSET_GOT:
                        intent.putExtra(SetActivity0.direction, direction);
                        intent.putExtra(SetActivity0.offset, string);
                        break;
                    // 设置值
                    case SetActivity0.VS_ALL_GOT:
                        intent.putExtra(SetActivity0.set, string);
                        break;
                    // [[cxq
			/*
			 * case SetActivity0.OPCODE_GET_BATTERY:
			 * intent.putExtra(SetActivity0.battery, string); break;
			 *
			 * //]]
			 */
                    // 开锁方式
                    case SetActivity0.VS_UNLOCK_TYPE:
                        intent.putExtra(SetActivity0.unlock_type, string);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
            }
            break;
            // //锁处理界面
            case activity_DeviceScan0:
            case activity_LockerProcess: {
                switch (name) {
                    case SetActivity0.VS_S00SerialNumber:
                        // service里跳转页面前提
                        // before sendBroadcast must new Intent();
                        intent = new Intent();
                        intent.setClass(this, SetActivity0.class);
                        intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(SetActivity0.bluetoothName, this.mLastDeviceName);
                        // [[CXQ 掌机序列号，如果收到20个FF就用MAC地址代替
                        intent.putExtra(SetActivity0.bluetoothAddress, this.mAddres);
                        // ]]
                        intent.putExtra(SetActivity0.S00SerialNumber, string);
                        break;
                    case SetActivity0.VS_FW51_VER_GOT:
                        intent.putExtra(SetActivity0.S00SoftwareVersion, string);
                        break;
                    case SetActivity0.VS_HW_GOT:
                        intent.putExtra(SetActivity0.S00HardwareVersion, string);
                        break;
                    case SetActivity0.VS_LOFFSET_GOT:
                        intent.putExtra(SetActivity0.direction, direction);
                        intent.putExtra(SetActivity0.offset, string);
                        break;
                    case SetActivity0.VS_ALL_GOT:
                        intent.putExtra(SetActivity0.set, string);
                        break;
                    case SetActivity0.VS_UNLOCK_TYPE:
                        intent.putExtra(SetActivity0.unlock_type, string);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
            }
            break;
            // 设置界面进入
            case activity_set: {
                switch (name) {
                    case SetActivity0.VS_S00SerialNumber:
                        // before sendBroadcast must new Intent();
                        intent = new Intent();
                        // before sendBroadcast must setAction();
                        intent.setAction(ACTION_DEVICE_STATUS);
                        intent.putExtra(SetActivity0.bluetoothName, this.mLastDeviceName);
                        intent.putExtra(SetActivity0.S00SerialNumber, string);
                        break;
                    case SetActivity0.VS_FW51_VER_GOT:
                        intent.putExtra(SetActivity0.S00SoftwareVersion, string);
                        break;
                    case SetActivity0.VS_HW_GOT:
                        intent.putExtra(SetActivity0.S00HardwareVersion, string);
                        break;
                    case SetActivity0.VS_LOFFSET_GOT:
                        intent.putExtra(SetActivity0.direction, direction);
                        intent.putExtra(SetActivity0.offset, string);
                        break;
                    case SetActivity0.VS_ALL_GOT:
                        intent.putExtra(SetActivity0.set, string);
                        break;
                    case SetActivity0.VS_S00Name:
                        Intent intent_name = new Intent();
                        intent_name = new Intent();
                        intent_name.putExtra(SetAllActivity.bluetoothName, string);
                        intent_name.setAction(ACTION_DEVICE_STATUS);
                        this.sendBroadcast(intent_name);
                        break;
                    case SetActivity0.VS_UNLOCK_TYPE:
                        intent.putExtra(SetActivity0.unlock_type, string);
                        // sendBroadcast update setValue(all)
                        this.sendBroadcast(intent);
                        break;
                    case MCUCommand.opcode_sleep:
                        // before sendBroadcast must new Intent();
                        intent = new Intent();
                        // before sendBroadcast must setAction();
                        intent.setAction(ACTION_DEVICE_STATUS);
                        intent.putExtra(MCUCommand.string_sleep, string);
                        // sendBroadcast update setValue(all)
                        this.sendBroadcast(intent);
                        break;

                    default:
                        break;
                }
            }
            break;
            // 设置所有
            case activity_setAll: {
                switch (name) {
                    case SetActivity0.VS_S00SerialNumber:
                        // before sendBroadcast must new Intent();
                        intent = new Intent();
                        // before sendBroadcast must setAction();
                        intent.setAction(ACTION_DEVICE_STATUS);
                        intent.putExtra(SetActivity0.bluetoothName, this.mLastDeviceName);
                        intent.putExtra(SetActivity0.S00SerialNumber, string);
                        break;
                    case SetActivity0.VS_FW51_VER_GOT:
                        intent.putExtra(SetActivity0.S00SoftwareVersion, string);
                        break;
                    case SetActivity0.VS_HW_GOT:
                        intent.putExtra(SetActivity0.S00HardwareVersion, string);
                        break;
                    case SetActivity0.VS_LOFFSET_GOT:
                        intent.putExtra(SetActivity0.direction, direction);
                        intent.putExtra(SetActivity0.offset, string);
                        break;
                    case SetActivity0.VS_ALL_GOT:
                        intent.putExtra(SetActivity0.set, string);
                        break;
                    case SetActivity0.VS_S00Name:
                        Intent intent_name = new Intent();
                        intent_name = new Intent();
                        intent_name.putExtra(SetAllActivity.bluetoothName, string);
                        intent_name.setAction(ACTION_DEVICE_STATUS);
                        this.sendBroadcast(intent_name);
                        break;
                    case SetActivity0.VS_UNLOCK_TYPE:
                        intent.putExtra(SetActivity0.unlock_type, string);
                        // sendBroadcast update setValue(all)
                        this.sendBroadcast(intent);
                        break;
                    case MCUCommand.opcode_sleep:
                        // before sendBroadcast must new Intent();
                        intent = new Intent();
                        // before sendBroadcast must setAction();
                        intent.setAction(ACTION_DEVICE_STATUS);
                        intent.putExtra(MCUCommand.string_sleep, string);
                        // sendBroadcast update setValue(all)
                        this.sendBroadcast(intent);
                        break;

                    default:
                        break;
                }
            }
            break;
            default:
                break;
        }
    }

    //
    String getActivityManager() {
        // get ActivityManager
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        // one
        String string = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
        return string;
    }

    // 上传工单
    public void sheet_uploadLog(ArrayList<String> record_data) {

        String string = "";
        String logState = "";
        String lockNumber = "";
        String systemTime = "";
        String str_UserInfo = "";
        FileStream fileStream = new FileStream();
        if (str_UserInfo.length() <= 0) {
            // get userId
            // FileStream fileStream=new FileStream();
            byte[] byteArray = fileStream.fileStream(FileStream.userLogin, FileStream.read, null);
            str_UserInfo = new String(byteArray);
            String[] stringArray = str_UserInfo.split(",");
            str_UserInfo = stringArray[2];
        }

        if (record_data.size() <= 0) {
            byte[] by_old_record = fileStream.fileStream(FileStream.log, FileStream.read, null);
            if (by_old_record != null && by_old_record.length > 0) {
                String old_record = new String(by_old_record);
                if (old_record != null && old_record.length() > 0) {
                    String[] array_data = old_record.split("\r\n");
                    for (int i = 0; i < array_data.length; i++) {
                        record_data.add(array_data[i]);
                    }
                    int offset = 0;
                    for (int i = 0; i < record_data.size(); i++) {
                        String data = record_data.get(i);
                        logState = data.substring(offset, offset + 1);
                        lockNumber = data.substring(offset + 1, offset + 1 + 9);
                        systemTime = data.substring(offset + 10, offset + 10 + 14);
                        // 开关锁记录
                        string += MCUCommand.upload_log_serviceId + MainActivity.IMEI + str_UserInfo + lockNumber + systemTime + logState;
                    }
                    // 请求打包
                    requestPacket = string; // 先不打包
                    uploadType = MCUCommand.lock_type_local; // 上传保存到手机里的记录
                }
            } else {
                return;
            }
        } else {
            int offset = 0;
            for (int i = 0; i < record_data.size(); i++) {
                String data = record_data.get(i);
                logState = data.substring(offset, offset + 1);
                lockNumber = data.substring(offset + 1, offset + 1 + 9);
                systemTime = data.substring(offset + 10, offset + 10 + 14);
                // 开关锁记录
                string += MCUCommand.upload_log_serviceId + MainActivity.IMEI + str_UserInfo + lockNumber + systemTime + logState;
            }
            // 请求打包
            uploadType = MCUCommand.lock_type_offline; // 离线开锁
        }

        // 验证网络连接
        boolean wlanFlag = NetworkConnect.checkNet(getApplicationContext());
        if (wlanFlag) {
            // 请求打包
            requestPacket = CommonResources.createRequestPacket(string);
            new MyTask().execute(string);
        } else {
            // 连接网络后,再进行其他操作!
            // writeStream(requestPacket);
            // 没有网络先保存到手机中
            addLog((byte) 0, "网络有问题请检查网络!");
            if (list_LockRecord.size() > 0) {
                for (int i = 0; i < list_LockRecord.size(); i++) {
                    fileStream.fileStream(FileStream.log, FileStream.write, (list_LockRecord.get(i) + "\r\n").getBytes());
                }
                list_LockRecord.clear();
            }

            if (list_SheetRecord.size() > 0) {
                for (int i = 0; i < list_SheetRecord.size(); i++) {
                    fileStream.fileStream(FileStream.log, FileStream.write, (list_SheetRecord.get(i) + "\r\n").getBytes());
                }
                list_SheetRecord.clear();
            }
        }
        // must clear
        lockNumber = null;

    }

    // 新协议 上传开锁记录到服务器
    public void offline_uploadLog(ArrayList<String> record_data) {
        String string = "";
        String logState = "";
        String lockNumber = "";
        String systemTime = "";
        String str_UserInfo = "";
        String str_DeviceID = "";
        FileStream fileStream = new FileStream();
        List<LockLogBean> lockLogBeanList=new ArrayList<>();
        if (str_UserInfo.length() <= 0) {
            // get userId
            // FileStream fileStream=new FileStream();
            byte[] byteArray = fileStream.fileStream(FileStream.userLogin, FileStream.read, null);
            str_UserInfo = new String(byteArray);
            String[] stringArray = str_UserInfo.split(",");
            str_UserInfo = stringArray[2];
        }

        if (record_data.size() <= 0) {
            byte[] by_old_record = null;
            if (DeviceService.this.mCurrentActionChain != null && DeviceService.this.mCurrentActionChain.getClass().getCanonicalName().equals("com.saintsung.saintpmc.lock.UnlockScrew")) {
                by_old_record = fileStream.fileStream(FileStream.screw_log, FileStream.read, null);
                uploadType = MCUCommand.lock_type_screw;
            } else {
                by_old_record = fileStream.fileStream(FileStream.log, FileStream.read, null);
            }

            if (by_old_record != null && by_old_record.length > 0) {
                String old_record = new String(by_old_record);
                if (old_record != null && old_record.length() > 0) {
                    String[] array_data = old_record.split("\r\n");
                    for (int i = 0; i < array_data.length; i++) {
                        record_data.add(array_data[i]);
                    }
                    int offset = 0;
                    for (int i = 0; i < record_data.size(); i++) {
                        String data = record_data.get(i);
                        str_DeviceID = data.substring(offset, offset + 18); // 掌机编号
                        lockNumber = data.substring(offset + 18, offset + 18 + 9); // 锁号
                        systemTime = data.substring(offset + 27, offset + 27 + 14); // 时间锁号
                        logState = data.substring(offset + 41, offset + 41 + 1); // 结果
                        // 开关锁记录
                        string += str_UserInfo + lockNumber + systemTime + logState;
                    }
                    string = str_DeviceID + string;
                    // 请求打包
                    //requestPacket = "L004" + string; // 先不打包
                    uploadType = MCUCommand.lock_type_local; // 上传保存到手机里的记录
                }
            } else {
                addLog((byte) 0, "没有记录!");
                return;
            }
        } else { // 直接打包
            int offset = 0;
            str_DeviceID = record_data.get(0).substring(offset, offset + 18); // 掌机编号
            String data = record_data.get(0).substring(offset + 18, record_data.get(0).length());
            for (int i = 0; i < (data.length()) / 24; i++) {
                String temp = data.substring(i * 24, (i + 1) * 24);
                lockNumber = temp.substring(0, 9); // 锁号
                systemTime = temp.substring(9, 9 + 14); // 时间
                logState = temp.substring(23, 23 + 1); // 结果
                // 开关锁记录
                LockLogBean lockLogBean=getLockInfo(logState,systemTime,lockNumber);
                lockLogBeanList.add(lockLogBean);

            }

            // 请求打包
            uploadType = MCUCommand.lock_type_offline; // 离线开锁
        }

        // 验证网络连接
        boolean wlanFlag = NetworkConnect.checkNet(getApplicationContext());
        if (wlanFlag) {
            // 请求打包
            if(lockLogBeanList.size()<1)
                return;
            upLoadOpenLockInfo(getStr(lockLogBeanList));
        } else {
            // 连接网络后,再进行其他操作!
            // writeStream(requestPacket);
            // 没有网络先保存到手机中
            addLog((byte) 0, "网络有问题请检查网络!");
            if (list_LockRecord.size() > 0) {
                for (int i = 0; i < list_LockRecord.size(); i++) {
                    fileStream.fileStream(FileStream.log, FileStream.write, (list_LockRecord.get(i) + "\r\n").getBytes());
                }
                list_LockRecord.clear();
            }
        }
        // must clear
        lockNumber = null;
    }

    // 上传开锁记录到服务器
    public void uploadLog(String logState, String lockNumber, String str_OpertTime) {
        if (lockNumber == null || lockNumber.equals("") || logState == null || logState.equals("")) {
            return;
        }
        //		0-关闭成功
        //		1-开启成功
        //		2-开启失败
        //		3-关闭失败★必须传入
        FileStream fileStream = new FileStream();
        // 验证网络连接
        if (ToastUtil.isNetworkAvailable(getApplicationContext())) {
            // 请求打包
            LockLogBean lockLogBean = getLockInfo(logState, str_OpertTime, lockNumber);
            List<LockLogBean> lockLogBeanList = new ArrayList<>();
            lockLogBeanList.add(lockLogBean);
            upLoadOpenLockInfo(getStr(lockLogBeanList));
//            requestPacket = CommonResources.createRequestPacket(string);
//            uploadType = MCUCommand.lock_type_online; // 在线开关锁
//            new MyTask().execute();
        } else {
            // 连接网络后,再进行其他操作!
            addLog((byte) 0, "请检查网络状态");
            fileStream.fileStream(FileStream.log, FileStream.write, requestPacket.getBytes()); // 记录保存在本地
        }
        // must clear
        lockNumber = null;
    }

    String requestPacket = "";
    String responsePacket;
    String uploadType = "";

    // UI子线程
    class MyTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... arg0) {
            // TODO Auto-generated method stub
            // 连接服务器,发送开/关锁记录信息.
            SocketConnect socketConnect = new SocketConnect();
            responsePacket = socketConnect.sendDate(requestPacket);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            // 拆分服务器返回数据
            CommandPacker commandPacker = new CommandPacker();
            commandPacker.decodeResultFlag(responsePacket);
            try {
                WLog.logFile("上传记录请求: " + requestPacket);
                WLog.logFile("上传记录返回: " + responsePacket);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // 等待通讯成功
            if (CommandPacker.succ_flag) {
                FileStream fileStream = new FileStream();
                if (uploadType.equals(MCUCommand.lock_type_screw)) {
                    // 螺丝记录上传成功
                    addLog((byte) 0, "开关螺丝记录上传成功！");
                    fileStream.fileStream(FileStream.screw_log, FileStream.delete, null);
                }
                if (uploadType.equals(MCUCommand.lock_type_online)) {
                    // 开/关锁记录上传成功
                    addLog((byte) 0, "记录上传成功!");

                } else if (uploadType.equals(MCUCommand.lock_type_offline)) {
                    if (list_LockRecord.size() > 0) {
                        // 开/关锁记录上传成功
                        addLog((byte) 0, String.valueOf((list_LockRecord.get(0).length() - 18) / 24) + "条记录上传成功!");
                        if (recv_record_head.equals(sheet_offline)) {
                            addLog((byte) 0, "记录成功上传");
                            //上传完后，下载用户关联到掌机中
                            if (action_down_upload.equals(download_offline)) {
                                sortLockNumber_Offline();
                            }
                        } else {
                            // 上传工单
                            uploadType = MCUCommand.lock_type_uploadSheet;
                            fun_UploadSheet(list_LockRecord);
                        }
                        //之前离线开锁，现在想下工单
                        if (action_down_upload.equals(download_sheet)) {
                            //						fun_action_download_sheet(); // 下载工单
                        }
                        // list_LockRecord.clear();
                        i_uploadItem = 1;
                    }
                } else if (uploadType.equals(MCUCommand.lock_type_local)) {
                    // 开/关锁记录上传成功
                    addLog((byte) 0, String.valueOf(list_LockRecord.size()) + "条手机中记录上传成功!");
                    if (list_LockRecord.size() > 0) {
                        list_LockRecord.clear();
                    }

                    fileStream.fileStream(FileStream.log, FileStream.delete, null);
                    // 本地记录上传完，也上传工单
                    // 上传工单
                    uploadType = MCUCommand.lock_type_uploadSheet;
                    fun_UploadSheet(list_LockRecord);
                    i_uploadItem = 1;

                } else if (uploadType.equals(MCUCommand.lock_type_uploadSheet)) {
                    addLog((byte) 0, "工单上传成功!");
                    // 此时清除掉手机中缓存的记录
                    if (list_LockRecord != null && list_LockRecord.size() > 0) {
                        list_LockRecord.clear();
                    }
                    // 上传完清楚掌机记录，手机记录
                    fun_delete_record();
                    //	fileStream.fileStream(FileStream.log, FileStream.delete, null);
                    // 上传完不删除工单，下载时先删除。
                    // fileStream.fileStream(FileStream.sheetFile,
                    // FileStream.delete, null);

                    if (list_LockRecord != null && list_LockRecord.size() > 0) {
                        list_LockRecord.clear();
                    }

                    // 下载工单
                    if (action_down_upload.equals(download_sheet)) { //不自动下载工单
                        //			fun_action_download_sheet();
                    }

                }

            } else {
                addLog((byte) 0, "上传失败请检查服务器连接");
                // 开/关锁记录上传失败
                FileStream fileStream = new FileStream();
                String log = requestPacket;
                if (uploadType.equals(MCUCommand.lock_type_offline)) // 只要保存离线的
                {
                    if (list_LockRecord.size() > 0) {
                        for (int i = 0; i < list_LockRecord.size(); i++) {
                            fileStream.fileStream(FileStream.log, FileStream.write, (list_LockRecord.get(i) + "\r\n").getBytes());
                        }
                        list_LockRecord.clear();
                    }
                }
            }
        }
    }

    //上传工单
    public void fun_UploadSheet(ArrayList<String> list_LockRecord) {

        FileStream fileStream = new FileStream();
        byte[] by_Sheet = fileStream.fileStream(FileStream.sheetFile, FileStream.read, null);
        String str_Sheet = new String(by_Sheet);
        if (str_Sheet.length() <= 0) {
            addLog((byte) 0, "手机没有下载工单,请先下载工单!");
            if (list_LockRecord != null && list_LockRecord.size() > 0) {
                list_LockRecord.clear();
            }
            if (action_down_upload.equals(download_sheet)) {
                fun_action_download_sheet(); // 下载工单
            }

            return;
        }
        String[] str_array_sheet = str_Sheet.split("\r\n");
        String[] str_array_lockNo = new String[str_array_sheet.length];

        int result = 1; // 包含
        // 获取工单中的锁号
        for (int i = 0; i < str_array_sheet.length; i++) {
            str_array_lockNo[i] = str_array_sheet[i].split(":")[3];
        }
        // 获取记录中的锁
        String[] str_list_record_lockNo = new String[list_LockRecord.size()];
        String str_record_lockNo = "";
        for (int i = 0; i < list_LockRecord.size(); i++) {
            // str_list_record_lockNo[i] = list_LockRecord.get(i).substring(0,
            // 9);
            str_record_lockNo += list_LockRecord.get(i).substring(18, 18 + 9);
        }

		/*
		 * //对比记录中有无工碟中的锁号 for(int i=0;i<str_array_lockNo.length;i++) { if
		 * (!str_record_lockNo.contains(str_array_lockNo[i])) { result = 0;
		 * //记录中没有工单中的锁 break; }
		 *
		 * ///* result = 0; for(int j=0;j<list_LockRecord.size();j++) {
		 * if(str_array_lockNo[i].equals(str_list_record_lockNo[i])){ //不包含
		 * result = 1; continue; }else{ result = 0; break; } } break; //
		 */

        // }

        Time time = new Time();
        time.setToNow();
        String str_time = String.format("%02d", time.year) + String.format("%02d", time.month + 1) + String.format("%02d", time.monthDay) + String.format("%02d", time.hour) + String.format("%02d", time.minute) + String.format("%02d", time.second);
        // if (result == 0)
        { // 有没有完成的工单
            // fun_Sheet_Send(str_array_sheet[0],str_time,"0"); //完成
        }
        // else
        {

            fun_Sheet_Send(str_array_sheet[0], str_time, "1");
        }

    }

    public void fun_Sheet_Send(String sheet_no, String str_time, String str_result) {

        try {
            try {

                String str_DataID = "L012";
                String str_Sheet_ID = sheet_no.split(":")[0];
                String str_Opter_Time = str_time;
                String str_Opter_Resutl = str_result;
                String string = "";

                FileStream fileStream = new FileStream();
                String str_DeviceID = new String(fileStream.fileStream(FileStream.deviceFile, FileStream.read, null));
                if (str_DeviceID == null || str_DeviceID.length() <= 0) {
                    addLog((byte) 0, "没有设置序列号，不上传工单");
                    return;
                }
                if (MainActivity.DeviceID == null || MainActivity.DeviceID.length() <= 0) {
                    addLog((byte) 0, "没有设置序列号，不上传工单");
                    return;
                }
                // 打包请求
                string = str_DataID + MainActivity.DeviceID + str_Sheet_ID + str_Opter_Time + str_Opter_Resutl;
                // 连接服务器,发送下载请求.
                // 验证网络连接
                boolean wlanFlag = NetworkConnect.checkNet(getApplicationContext());
                if (wlanFlag) {
                    // 请求打包
                    requestPacket = CommonResources.createRequestPacket(string);
//					new MyTask().execute(string);
                } else {
                    // 连接网络后,再进行其他操作!
                    // writeStream(requestPacket);
                    addLog((byte) 0, "请检查网络状态");

                }
                // System.out.println("send:"+"over");
            } finally {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // UI子线程
    class GetLockPassword extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... arg0) {
            // TODO Auto-generated method stub
			/*
						OkHttpClient okHttpClient = new OkHttpClient();
						FileStream fileStream = new FileStream();
						//get userNameValue
						byte[] byteArray = fileStream.fileStream(FileStream.userLogin, FileStream.read, null);
						String[] stringArray = newString(byteArray).split(",");
						String url = "http://210.22.164.146/authAuthUser.php?u=" + stringArray[0] + "&l=" + MainActivity.handLockNumber + "&k=" + MainActivity.IMEI + "&a=2";
						String url = new String(CommonResources.getParam("lock_auth_url")) + "u=" + stringArray[0] + "&l=" + MainActivity.handLockNumber + "&k=" + MainActivity.IMEI + "&a=2";
						Request request = new Request.Builder().url(url).build();
						try {
							Response response = okHttpClient.newCall(request).execute();
							if (response.isSuccessful()) {
								Headers responseHeaders = response.headers();
								for (int i = 0; i < responseHeaders.size(); i++) {
									System.out.println(responseHeaders.name(i) + ":" + responseHeaders.value(i));
								}
								System.out.println(response.body().string());
							}
						} catch (IOException e) { // TODO Auto-generated catch block
							e.printStackTrace();
						}
			*/
            return null;

        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
        }
    }

    // openLockerProcessAtivity
    void openLockerProcessAtivity() {
        String activityName = getActivityManager();
        switch (activityName) {
            case activity_LockerProcess: {
                //			intent = new Intent();
                //			intent.setClass(DeviceService.this, LockerProcessAtivity.class);
                //			intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
                //			startActivity(intent);
            }
            break;
            case activity_set: {
            }
            break;

            default: {
                // service里跳转页面前提
                intent = new Intent();
                intent.setClass(DeviceService.this, LockerProcessAtivity.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            break;
        }
    }

    // change unlock_bluetooth
    void changeUnlockType(int i) {
        Log.d("Set", "Set " + "changeUnlockType" + i);
        // must first open LockerProcessAtivity
        openLockerProcessAtivity();
    }

    // 发送上传命令给掌机
    private void uploadHistory() {

        byte[] by_data = Command.getInstance().AppUploadHistory();

    }

    private String userId = "";

    private void sortLockNumber_Sheet() {
        if (sheet_interrupt.equals("ok")) {
            addLog((byte) 0, getString(R.string.old_sheet) + sheet_remark);
            // 先不下载工单
            if (DeviceService.this.mCurrentActionChain == null || !DeviceService.this.mCurrentActionChain.getClass().getCanonicalName().equals("com.saintsung.saintpmc.lock.SendUserDownload")) {
                DeviceService.this.mCurrentActionChain = new SendSheet(DeviceService.this);
            } else {
                DeviceService.this.mCurrentActionChain.onConnect();
                Log.d("onConnect()", "onConnect()" + "sortLockNumber()");
            }
            if (mHandler != null) {
                dw_flag = 0;
                if (dw_flag == 0) {
                    mHandler.removeCallbacks(runnable_e);
                    mHandler.postDelayed(runnable_e, 500);
                } else {
                    mHandler.removeCallbacks(runnable_e);
                }
            }
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // 读取掌机序号
                    FileStream fileStream = new FileStream();
                    byte[] by_DeviceId = fileStream.fileStream(FileStream.deviceFile, FileStream.read, null);
                    byte[] by_UserID = fileStream.fileStream(FileStream.userLogin, FileStream.read, null);
                    String[] array_UserID = new String(by_UserID).split(",");
                    userId = array_UserID[2];
                    // TODO Auto-generated method stub
                    String str_DataID = "L008";
                    String str_UserID = array_UserID[2];
                    String str_DeviceID = new String(by_DeviceId);
                    str_DeviceID = str_DeviceID.substring(0, 18);
                    str_DeviceID = str_DeviceID.trim();
                    if (str_DeviceID.contains("default machine") || str_DeviceID == null || str_DeviceID.length() <= 0) {
                        addLog((byte) 0, "请先设置掌机序号,否则不能下载工单");
                        return;
                    }
                    addLog((byte) 0, "正在下发工单！");
                    fun_send_sheet();
                }

            });

        }

    }

    private void sortLockNumber_Offline() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {

				/*
				 * 离线下载这里不用
				 */

                // start sort
                FileStream fileStream = new FileStream();
                // sort lock.json file
                // byte[] byteArray=fileStream.fileStream(FileStream.jsonFile,
                // FileStream.read, null);
                // sort userDownload.txt file
                byte[] byteArray = fileStream.fileStream(FileStream.userDownload, FileStream.read, null);
                String string = new String(byteArray);
                //
                // get lock.json file data
                // delete ""(空格符)
                string = string.replaceAll(" ", "");
                // delete "{" and "}"
                // string=string.substring(1, string.length()-1); //读离线文件时不需要这条
                // replace "\n(回车符)" to ""(空字符串)
                string = string.replaceAll("\n", "");
                // replace ""(双引号)" to ""(空字符串)
                string = string.replaceAll("\"", "");
                // replace "," to ""(空字符串)
                string = string.replaceAll(",", "\r\n");
                //
                // check LockNumber count
                String[] stringArray = string.split("\r"); // 读离线文件时\r,lock.json时\r\n
                if (stringArray.length == 0) {
                    // addLog((byte)0,"下传锁具信息个数为0,请先手动导入lock.json文件到手机APP文件夹里!");
                    addLog((byte) 0, "下传锁具信息个数为0,请先确认该用户没有任何1把关联锁具信息或者有关联锁具信息未下载到本地先!");
                } else if (stringArray.length > 2000) {
                    addLog((byte) 0, "下传锁具信息个数超过2000,下传失败!");
                } else if (stringArray.length <= 2000) {

                    // after sort
                    String string4 = "";
                    for (String string3 : stringArray) {
                        string4 = string4 + string3 + "\r\n";
                    }
                    fileStream.fileStream(FileStream.sendLockNumberAndPassword, FileStream.delete, null);
                    fileStream.fileStream(FileStream.sendLockNumberAndPassword, FileStream.write, string4.getBytes());
                    addLog((byte) 0, "本地锁号排序成功结束!");
                    // flag_sort=true;
                    // /*
                    if (DeviceService.this.mCurrentActionChain == null || !DeviceService.this.mCurrentActionChain.getClass().getCanonicalName().equals("com.saintsung.saintpmc.lock.SendUserDownload")) {
                        DeviceService.this.mCurrentActionChain = new SendUserDownload(DeviceService.this);
                    } else {
                        DeviceService.this.mCurrentActionChain.onConnect();

                        Log.d("onConnect()", "onConnect()" + "sortLockNumber()");

                    }
                    // send locknostart000(length=14)
                    Command command = new Command();
                    byte[] byteArray0 = command.new_sendLockNumberAndPassword(MCUCommand.send_lock_start, null);
                    mDevice.Write(FileStream.write, byteArray0);
                    //
                } else {
                    addLog((byte) 0, "本地锁号排序中断失败!");
                }

            }
        });
    }

    // swap position/location/place/seat;
    void swap(String[] stringArray, int i, int j) {
        // 选择排序
        String string = stringArray[i];
        stringArray[i] = stringArray[j];
        stringArray[j] = string;
    }

    // UI子线程
    class MyTask_Download extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... arg0) {
            // TODO Auto-generated method stub
            // 连接服务器,发送开/关锁记录信息.
            SocketConnect socketConnect = new SocketConnect();
            responsePacket = socketConnect.sendDate(requestPacket);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            // 拆分服务器返回数据
            CommandPacker commandPacker = new CommandPacker();
            commandPacker.decodeResultFlag(responsePacket);
            try {
                WLog.logFile("工单数据" + responsePacket);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            // 等待通讯成功
            if (CommandPacker.succ_flag) {
                // 000597L00810000 F927470900752311
                // 2016092711312016120100000000test
                // B2F565CE5241E0533169C2326A71A2C3
                // 长度 服务ID
                // 15+512+32
                if (list_Sheet.size() > 0) {
                    list_Sheet.clear();
                }
                String error_code = responsePacket.substring(11, 11 + 4);
                int length = 0;
                try {
                    length = Integer.parseInt(responsePacket.substring(0, 6));
                } catch (Exception e) {
                    // TODO: handle exception
                    addLog((byte) 0, "工单有误");
                    return;
                }

                if (length <= 597) {
                    addLog((byte) 0, getString(R.string.sheet_no_task));
                } else if (error_code.equals("0000")) {
                    String data = responsePacket.substring(15, responsePacket.length() - 32);
                    String device_id = data.substring(0, 16);
                    String str_start_time = data.substring(16, 16 + 12);
                    String str_end_time = data.substring(28, 28 + 12);
                    String str_item = data.substring(40, 40 + 4);
                    String str_lock_info = data.substring(44, data.length() - 512);
                    String str_remarksString = data.substring(data.length() - 512, data.length()).trim();
                    Log.d("lockInfo", "lockInfo" + str_lock_info);
                    String str_Lng = ""; // 经度
                    String str_lat = ""; // 纬度
                    int i_LockInfoLength = 56;
                    sheet_remark = str_remarksString; // 保留工单备注；
                    SimpleDateFormat sd = new SimpleDateFormat("yyyyMMddhhmm");
                    long diff = -1;
                    try {
                        diff = sd.parse(str_end_time).getTime() - sd.parse(str_start_time).getTime();
                    } catch (Exception e) {
                        // TODO: handle exception
                        addLog((byte) 0, "工单有误");
                        return;
                    }
                    if (diff < 0) {
                        addLog((byte) 0, "工单有误");
                        return;
                    }

                    for (int i = 0; i < Integer.parseInt(str_item); i++) {
                        str_Lng = str_lock_info.substring(i * i_LockInfoLength + 28, i * i_LockInfoLength + 42); // 经度
                        str_lat = str_lock_info.substring(i * i_LockInfoLength + 42, i * i_LockInfoLength + 56); // 纬度
                        list_Sheet.add(device_id + ":" + str_start_time + ":" + str_end_time + ":" + str_lock_info.substring(i * i_LockInfoLength, i * i_LockInfoLength + 9) + ":" + str_lock_info.substring(i * i_LockInfoLength + 9, i * i_LockInfoLength + 24) + ":" + str_lock_info.substring(i * i_LockInfoLength + 24, i * i_LockInfoLength + 28) + ":" + str_Lng + ":" + str_lat + "\r\n");
                    }

                    FileStream fileStream = new FileStream();
                    String str_data = "";
                    for (int i = 0; i < list_Sheet.size(); i++) {
                        str_data += list_Sheet.get(i);
                    }
                    // 先删除，再创建写入文件
                    fileStream.fileStream(FileStream.sheetFile, FileStream.delete, null);
                    fileStream.fileStream(FileStream.sheetFile, FileStream.write, str_data.getBytes());
                    // addLog((byte) 0, getString(R.string.download_ready));

                    addLog((byte) 0, getString(R.string.new_sheet) + str_remarksString);
                    // 下载工单
                    fun_send_sheet();

                }

            } else {
                addLog((byte) 0, getString(R.string.download_error));

            }
        }
    }

    public void fun_send_sheet() {
        // 先不下载工单
        if (DeviceService.this.mCurrentActionChain == null || !DeviceService.this.mCurrentActionChain.getClass().getCanonicalName().equals("com.saintsung.saintpmc.lock.SendUserDownload")) {
            DeviceService.this.mCurrentActionChain = new SendSheet(DeviceService.this);
        } else {
            DeviceService.this.mCurrentActionChain.onConnect();

            Log.d("onConnect()", "onConnect()" + "sortLockNumber()");
        }
        dw_flag = 0;

        if (mHandler != null) {
            if (dw_flag == 0) {
                mHandler.removeCallbacks(runnable_e);
                mHandler.postDelayed(runnable_e, 500);
            } else {
                mHandler.removeCallbacks(runnable_e);
            }
        }
    }

    Runnable runnable_e = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (dw_flag == 0) {
                // 先E命令上传记录，再下载工单
                Command command = new Command();
                byte[] byteArray0 = command.new_sendLockNumberAndPassword(MCUCommand.send_lock_start, null);
                // byte[] byteArray0 = command.AppUploadHistory();
                mDevice.Write(FileStream.write, byteArray0);
            }

            if (mHandler != null) {
                if (dw_flag == 0) {
                    mHandler.postDelayed(runnable_e, 500);
                } else {
                    mHandler.removeCallbacks(runnable_e);
                }
            }
        }
    };

    public Long dateDiff(String startTime, String endTime, String format, String str) {
        // 按照传入的格式生成一个simpledateformate对象
        SimpleDateFormat sd = new SimpleDateFormat(format);
        long nd = 1000 * 24 * 60 * 60;// 一天的毫秒数
        long nh = 1000 * 60 * 60;// 一小时的毫秒数
        long nm = 1000 * 60;// 一分钟的毫秒数
        long ns = 1000;// 一秒钟的毫秒数
        long diff;
        long day = 0;
        long hour = 0;
        long min = 0;
        long sec = 0;
        // 获得两个时间的毫秒时间差异
        try {
            diff = sd.parse(endTime).getTime() - sd.parse(startTime).getTime();
            day = diff / nd;// 计算差多少天
            hour = diff % nd / nh + day * 24;// 计算差多少小时
            min = diff % nd % nh / nm + day * 24 * 60;// 计算差多少分钟
            sec = diff % nd % nh % nm / ns;// 计算差多少秒
            // 输出结果
            System.out.println("时间相差：" + day + "天" + (hour - day * 24) + "小时" + (min - day * 24 * 60) + "分钟" + sec + "秒。");
            System.out.println("hour=" + hour + ",min=" + min);
            if (str.equalsIgnoreCase("h")) {
                return hour;
            } else {
                return min;
            }

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (str.equalsIgnoreCase("h")) {
            return hour;
        } else {
            return min;
        }
    }

    // ]]


    // 蓝牙断开返回界面
    public void fun_Close_View() {
        Intent intent = new Intent();
        startActivity(intent);

    }

    // 蓝牙断开时断开所有连接
    public void fun_Stop_Link() {

        // [[wk change 2016-03-24
        if (this.mDevice == null) {
            return;
        } else {
            DeviceService.this.mDevice = null;
        }
        if (DeviceService.this.mCurrentActionChain != null) {
            DeviceService.this.mCurrentActionChain.onDisconnect();
        } else {
            DeviceService.this.mCurrentActionChain = null;
        }
        if (mAutoConnectDeviceRunnable != null) {
            this.mHandler.removeCallbacks(mAutoConnectDeviceRunnable);
        }
        if (mAutoReconnectRunnable != null) {
            this.mHandler.removeCallbacks(mAutoReconnectRunnable);
        }

        DeviceService.this.broadcastStatus(DS_DISCONNECTED);
        // ]]

    }

    // 判断传入的字符串是不是数字
	/*
	 * 判断是否为整数
	 *
	 * @param str 传入的字符串
	 *
	 * @return 是整数返回true,否则返回false
	 */

    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    public void fun_getStandardDate(String url) {
        boolean wlanFlag = NetworkConnect.checkNet(getApplicationContext());
        if (wlanFlag) {
            new MyTask_GetDate().execute(url);
        }
    }

    class MyTask_GetDate extends AsyncTask<String, String, String> {
        String standardDate = "";

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            final CommomInterface commomInterface = new DeviceService();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
            standardDate = formatter.format(curDate);
            commomInterface.getDate(standardDate);
            return standardDate;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

    }

    @Override
    public void getDate(String date) {
        // TODO Auto-generated method stub
        Log.d("data", "getDate" + date);
        if (date != null && date.length() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmm");
            Date date_util = null;
            try {
                date_util = sdf.parse(date);
                date = sdf2.format(date_util);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } //转换为util.date
            DeviceService.standardDate = date;
        }
    }

    void upLoadOpenLockInfo(String jsonStr) {
        RetrofitRxAndroidHttp retrofitRxAndroidHttp = new RetrofitRxAndroidHttp();
        retrofitRxAndroidHttp.serviceConnect(MyApplication.getUrl(), jsonStr, action1);
    }

    private Action1<ResponseBody> action1 = new Action1<ResponseBody>() {

        @Override
        public void call(ResponseBody responseBody) {
            try {
                dataProcessing(responseBody.string());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    private LockLogBean getLockInfo(String type, String time, String number) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");
        String data = null;
        if (time == null) {
            Date date = new Date();
            data = sdf.format(date);
        } else{
            try {
                Date strr=sdf2.parse(time);
                data=sdf.format(strr);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        LockLogBean lockLogBean = new LockLogBean();
        lockLogBean.setResultId(type);
        lockLogBean.setDateTime(data);
        lockLogBean.setLockNumber(number);
        lockLogBean.setOptUserNumber(MyApplication.getUserId());
        lockLogBean.setKeyNumber(mAddres);
        return lockLogBean;
    }

    private String getStr(List<LockLogBean> lockLogBeans) {
        Gson gson = new Gson();
        LockLogUpServiceBean lockLogUpServiceBean = new LockLogUpServiceBean();
        lockLogUpServiceBean.setOptCode("LockLogUpload");
        lockLogUpServiceBean.setData(lockLogBeans);
        String dataStr=gson.toJson(lockLogUpServiceBean.getData());
        lockLogUpServiceBean.setSign(MD5.toMD5(lockLogUpServiceBean.getOptCode()+ dataStr));
        return gson.toJson(lockLogUpServiceBean);
    }

    private void dataProcessing(String string) {
        Log.e("TAG","Result:"+string);
        Gson gson=new Gson();
        LockLogUpServiceBean lockLogUpServiceBean=gson.fromJson(string,LockLogUpServiceBean.class);
        addLog((byte) 0, lockLogUpServiceBean.getResultMessage());
    }
}
