package com.saintsung.saintpmc.lock;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.widget.PullRefreshLayout;
import com.google.gson.Gson;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.saintsung.saintpmc.MainActivity;
import com.saintsung.saintpmc.MyApplication;
import com.saintsung.saintpmc.R;
import com.saintsung.saintpmc.asynctask.RetrofitRxAndroidHttp;
import com.saintsung.saintpmc.bean.WorkOrderDataBean;
import com.saintsung.saintpmc.bean.WorkOrderDataItemBean;
import com.saintsung.saintpmc.bean.WorkOrderItemBean;
import com.saintsung.saintpmc.bean.WorkOrderUpServiceBean;
import com.saintsung.saintpmc.configuration.configuration;
import com.saintsung.saintpmc.location.mPcRecordAdapter;
import com.saintsung.saintpmc.orderdatabase.LockInformation;
import com.saintsung.saintpmc.orderdatabase.WorkOrderBean;
import com.saintsung.saintpmc.orderdatabase.WorkOrderControData;
import com.saintsung.saintpmc.orderdatabase.WorkOrderControData$Table;
import com.saintsung.saintpmc.orderdatabase.WorkOrderDetailsBean;
import com.saintsung.saintpmc.orderdatabase.WorkOrderDetailsBean$Table;
import com.saintsung.saintpmc.tool.ToastUtil;


import okhttp3.ResponseBody;
import retrofit2.*;
import retrofit2.Response;
import rx.functions.Action1;

import static com.saintsung.saintpmc.lock.DeviceScanActivity.disconnect;
import static com.saintsung.saintpmc.MainActivity.connect_state;
import static com.saintsung.saintpmc.tool.DataProcess.getSystemTime;
import static com.saintsung.saintpmc.tool.ToastUtil.isNetworkAvailable;

public class LockerProcessAtivity extends AppCompatActivity {
    TextView deviceName = null, connectionState = null, unlockName = null, mTestInfo = null, mLogTextView = null;
    TextView FirmwareVesion = null; // 固件值
    TextView HardwareVesion = null; // 硬件值
    Button buttonBack;
    String mLogs = "", deviceAddress, mAutoTest;
    Handler mHandler = null;
    int i_delay = 1000;
    BleDeviceMgr.Device mDevice = null;
    int mOkTimes = 0, mFailTimes = 0, mTestTimes = 0, mBigOpen = 0, mLogLine = 0;
    JSONObject mKeyJson = null;

    // [[wk
    AlertDialog.Builder dialog = null;
    public static final String HAND_SET_LOCKPASSWORD = "HAND_SET_LOCKPASSWORD";
    public static final String send_userDownload = "send_userDownload";
    public static final String send_sheet = "send_sheet";
    private Dialog m_Dialog = null;
    // ]]

    // [[CXQ
    // 设备名称
    private String str_Device_Name = "智能掌机1";
    // 获取设置值不能重复发送;
    private int i_step = 0;
    SharedPreferences mySharedPreferences;
    SharedPreferences.Editor editor;
    User_Share user_Share = new User_Share();
    FileStream fileStream_log = new FileStream();
    private int i_GetVersion = 0; // 未连接前如果连接成功显示连接中,
    private int i_ConnState = 0; // 连接成功状态
    private ListView pc_record;
    PullRefreshLayout layout;
    private List<Map<String, String>> list = new ArrayList<>();
    mPcRecordAdapter mPcRecordAdapter;
    private com.saintsung.saintpmc.bean.WorkOrderBean workOrderBean;
    private TextView newWorkOrder;

    void registerBroadcast() {
        this.registerReceiver(this.mReceiver, DeviceService.makeBroadcasstIntentFilter());
        this.registerReceiver(this.mReceiverValue, DeviceService.makeBroadcastIntentFilter()); // 设置值
    }

    void unregisterBroadcast() {
        this.unregisterReceiver(mReceiver);
        this.unregisterReceiver(mReceiverValue);
    }

    FileStream fileStream = new FileStream();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        // [[CXQ
        // 禁止休眠
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // ]]
        this.setContentView(R.layout.locker_process);
        initLoginingDlg();

        // 文件保存信息
        user_Share = new User_Share();
        mySharedPreferences = getSharedPreferences(user_Share.MY_PREFS, MODE_PRIVATE);
        editor = mySharedPreferences.edit();

        this.mLogTextView = (TextView) this.findViewById(R.id.log);
        this.mLogTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
        mHandler = new Handler();
        deviceName = (TextView) this.findViewById(R.id.device_name);
        connectionState = (TextView) this.findViewById(R.id.connectionState);
        if (MainActivity.CONNECTED.equals(connect_state)) {
            i_ConnState = 1; // 连接成功
            // if (i_GetVersion == 0)
            {
                // connectionState.setText(R.string.connecting);
                // //在未获取固件值硬件值前显示连接中
            }
            {
                connectionState.setText(R.string.connected); // 在未获取固件值硬件值前显示连接中
            }

        } else {
            connectionState.setText(R.string.connecting);
        }

        unlockName = (TextView) this.findViewById(R.id.unlockName);
        // ]]
        mTestInfo = (TextView) this.findViewById(R.id.test_info);
        // [[wk
        buttonBack = (Button) this.findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                MainActivity.back = MainActivity.string_back;
                // /*
                // check MainActivity flag_open
                if (!MainActivity.flag_open) {
                    Intent intent = new Intent();
                    intent.setClass(getBaseContext(), MainActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        });
        if (MainActivity.CONNECTED.equals(connect_state) && LockSetActivity.unlock_screw.equals(LockSetActivity.unlockType)) {
            // handLockNumber
            dialogLockNumber();
        }
        FirmwareVesion = (TextView) findViewById(R.id.lp_offset);
        HardwareVesion = (TextView) findViewById(R.id.lp_setvalue);
        fun_Init();
        final List<WorkOrderBean> people = new Select().from(WorkOrderBean.class).queryList();
        pc_record = (ListView) findViewById(R.id.pc_record);
        workOrderBean = MyApplication.getWorkOrderBean();
        layout = (PullRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        layout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                layout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isNetworkAvailable(LockerProcessAtivity.this)) {
                            // 刷新1秒完成
                            RetrofitRxAndroidHttp retrofitRxAndroidHttp = new RetrofitRxAndroidHttp();
                            retrofitRxAndroidHttp.serviceConnect(MyApplication.getUrl(), getGsonStr(), action1);
                        } else {
                            layout.setRefreshing(false);
                            Toast.makeText(LockerProcessAtivity.this, "请连接网络后在试！", Toast.LENGTH_LONG).show();
                        }
                    }
                }, 1000);
            }
        });
        mPcRecordAdapter = new mPcRecordAdapter(this, workOrderBean);
        pc_record.setAdapter(mPcRecordAdapter);
        pc_record.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                fileStream.fileStream(FileStream.sheetFile, FileStream.delete, null);
                String res = "";
                newWorkOrder = (TextView) view.findViewById(R.id.newWorkOrder);
                newWorkOrder.setVisibility(View.VISIBLE);
                newWorkOrder.setText("进行工单");
                mPcRecordAdapter.clearView(position);
                TextView work_orderNumber = (TextView) view.findViewById(R.id.work_orderNumber);
                WorkOrderControData workOrderControData = new Select().from(WorkOrderControData.class).where(Condition.column(WorkOrderControData$Table.WORKORDERNUMBER).is(work_orderNumber.getText().toString())).querySingle();
                workOrderControData.workOrderState = "3";
                workOrderControData.update();
                //				List<WorkOrderDetailsBean> peoples = new Select	List<WorkOrderDetailsBean> peoples = new Select().from(WorkOrderDetailsBean.class).where(Condition.column(WorkOrderDetailsBean$Table.WORKORDERNUMBER).is(workOrder)).queryList();
                for (int i = 0; i < workOrderBean.getData().get(position).getLockInfos().size(); i++) {
                    WorkOrderDataBean workOrderDataBean = workOrderBean.getData().get(position);
                    res = res + workOrderDataBean.getWorkOrderNo() + ":" + workOrderDataBean.getStartTime() + ":" + workOrderDataBean.getEndTime() + ":" + workOrderDataBean.getLockInfos().get(i).getLockNo() + ":" + workOrderDataBean.getLockInfos().get(i).getOptPwd() + ":" + workOrderDataBean.getLockInfos().get(i).getType() + ":" + workOrderDataBean.getLockInfos().get(i).getPointX() + ":" + workOrderDataBean.getLockInfos().get(i).getPointY() + "\r\n";
                }
                fileStream.fileStream(FileStream.sheetFile, FileStream.write, res.getBytes());
                if (MainActivity.SLEEP.equals(MainActivity.state_sleep)) {
                    dialogWakeUp();
                    Toast.makeText(LockerProcessAtivity.this, "掌机未唤醒", Toast.LENGTH_LONG).show();
                } else {
                    if (MainActivity.isDown)
                        sendS00_sheet();
                    else
                        addLog("正在初始化小掌机时间，请稍后再试！");
                }
            }
        });
        mPcRecordAdapter.notifyDataSetChanged();
    }

    private String getGsonStr() {
        Gson gson = new Gson();
        String sign = "";
        com.saintsung.saintpmc.bean.WorkOrderBean workOrderBean = new com.saintsung.saintpmc.bean.WorkOrderBean();
        workOrderBean.setOptCode("GetWorkOrderInfos");
        workOrderBean.setOptUserNumber(MyApplication.getUserId());
        sign = MD5.toMD5(workOrderBean.getOptCode() + workOrderBean.getOptUserNumber() + workOrderBean.getData());
        workOrderBean.setSign(sign);
        return gson.toJson(workOrderBean);
    }
    private Action1<ResponseBody> action2 = new Action1<ResponseBody>() {

        @Override
        public void call(ResponseBody responseBody) {
            layout.setRefreshing(false);
            try {
                dataProcess(responseBody.string());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private void dataProcess(String string) {
        Log.e("TAG", "" + string);
    }

    private Action1<ResponseBody> action1 = new Action1<ResponseBody>() {

        @Override
        public void call(ResponseBody responseBody) {
            layout.setRefreshing(false);
            try {
                dataProcessing(responseBody.string());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    private void dataProcessing(String string) {
        Log.e("TAG", "" + string);
        Gson gson = new Gson();
        workOrderBean = gson.fromJson(string, com.saintsung.saintpmc.bean.WorkOrderBean.class);
        MyApplication.setWorkOrderBean(workOrderBean);
        mPcRecordAdapter.list = workOrderBean;
        RetrofitRxAndroidHttp retrofitRxAndroidHttp=new RetrofitRxAndroidHttp();
        retrofitRxAndroidHttp.serviceConnect(MyApplication.getUrl(),getUpService(workOrderBean),action2);
        mPcRecordAdapter.notifyDataSetChanged();
        List<LockInformation> lockInformations = new Select().from(LockInformation.class).queryList();
        for (LockInformation student : lockInformations) {
            student.delete();
        }
        LockInformation lockInformation = new LockInformation();
        for (int i = 0; i < workOrderBean.getData().size(); i++) {
            List<WorkOrderDataItemBean> workOrderDataItemBeanList = workOrderBean.getData().get(i).getLockInfos();
            WorkOrderControData workOrderControData = new WorkOrderControData();
            WorkOrderControData workOrderControData1 = new Select().from(WorkOrderControData.class).where(Condition.column(WorkOrderControData$Table.WORKORDERNUMBER).is(workOrderBean.getData().get(i).getWorkOrderNo())).querySingle();
            if (workOrderControData1 == null) {
                workOrderControData.workOrderNumber = workOrderBean.getData().get(i).getWorkOrderNo();
                workOrderControData.workOrderState = workOrderBean.getData().get(i).getWorkState();
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                workOrderControData.workTime = sdf.format(date);
                workOrderControData.insert();
            } else {
                workOrderControData.workOrderNumber = workOrderBean.getData().get(i).getWorkOrderNo();
                workOrderControData.workOrderState = workOrderBean.getData().get(i).getWorkState();
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                workOrderControData.workTime = sdf.format(date);
                workOrderControData.update();
            }

            for (int j = 0; j < workOrderDataItemBeanList.size(); j++) {
                lockInformation.lockNo = workOrderDataItemBeanList.get(j).getLockNo();
                lockInformation.assetno = workOrderDataItemBeanList.get(j).getAssetno();
                lockInformation.optPwd = workOrderDataItemBeanList.get(j).getOptPwd();
                lockInformation.pointX = workOrderDataItemBeanList.get(j).getPointX();
                lockInformation.pointY = workOrderDataItemBeanList.get(j).getPointY();
                lockInformation.type = workOrderDataItemBeanList.get(j).getType();
                lockInformation.starTime = workOrderBean.getData().get(i).getStartTime();
                lockInformation.endTime = workOrderBean.getData().get(i).getEndTime();
                lockInformation.insert();
            }
        }

    }

    private String getUpService(com.saintsung.saintpmc.bean.WorkOrderBean workOrderBean) {
        Gson gson=new Gson();
        WorkOrderUpServiceBean workOrderUpServiceBean=new WorkOrderUpServiceBean();
        List<WorkOrderItemBean> orderItemBeanList=new ArrayList<>();
        workOrderUpServiceBean.setOptCode("WorkOrderResult");
        for(WorkOrderDataBean workOrderBean1:workOrderBean.getData()){
            WorkOrderItemBean workOrderItemBean=new WorkOrderItemBean();
            workOrderItemBean.setWorkOrderNo(workOrderBean1.getWorkOrderNo());
            workOrderItemBean.setOptTime(getSystemTime());
            workOrderItemBean.setOptType("0001");
            orderItemBeanList.add(workOrderItemBean);
        }
        workOrderUpServiceBean.setData(orderItemBeanList);
        workOrderUpServiceBean.setSign(MD5.toMD5(workOrderUpServiceBean.getOptCode()+gson.toJson(workOrderUpServiceBean.getData())));
        return gson.toJson(workOrderUpServiceBean);
    }

    private Dialog mLoginingDlg;

    private void initLoginingDlg() {
        mLoginingDlg = new Dialog(this, R.style.loginingDlg);
        mLoginingDlg.setContentView(R.layout.logining_dlg2);
        Window window = mLoginingDlg.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int cxScreen = dm.widthPixels;
        int cyScreen = dm.heightPixels;
        int height = (int) getResources().getDimension(
                R.dimen.loginingdlg_height);
        int lrMargin = (int) getResources().getDimension(
                R.dimen.loginingdlg_lr_margin);
        int topMargin = (int) getResources().getDimension(
                R.dimen.loginingdlg_top_margin);
        params.y = (-(cyScreen - height) / 2) + topMargin; // -199
        params.width = cxScreen;
        params.height = height;
        mLoginingDlg.setCanceledOnTouchOutside(false);
        mLoginingDlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    private String getUserId() {
        FileStream fileStream = new FileStream();
        byte[] by_UserID = fileStream.fileStream(FileStream.userLogin, FileStream.read, null);
        String[] array_UserID = new String(by_UserID).split(",");
        if (array_UserID == null || array_UserID.length <= 0)
            return null;
        String str_UserID = array_UserID[2];
        str_UserID = str_UserID.replaceAll("^(0+)", "");
        Log.e("TAG", "str_UserID=" + str_UserID);
        return str_UserID;
    }

    void setState(int status) {
    }

    @Override
    protected void onResume() {
        this.registerBroadcast();
        if (MCUCommand.string_sleep.equals(MainActivity.back)) {
            MainActivity.back = null;
            addLog(getString(R.string.wakeUp));
        }
        if (MainActivity.back == MainActivity.string_back) {
            MainActivity.back = null;
            finish();
        }
        // ]]

        super.onResume();
    }

    public void fun_Init() {
        editor.putString(user_Share.MY_CLASS, user_Share.class_LockerProcessAtivity);
        // 提交保存的结果 将改变写到系统中
        editor.commit();

        Intent i = new Intent();
        i.setClass(this, DeviceService.class);
        i.setAction(DeviceService.ACTION_GET_VALUES);
        this.startService(i);
        //
        i = new Intent();
        i.setClass(this, DeviceService.class);
        i.setAction(DeviceService.ACTION_AUTO_TEST_TYPE);
        // [[wk
        if (LockSetActivity.unlockType == null || LockSetActivity.unlockType.equals("")) {
            LockSetActivity.unlockType = LockSetActivity.unlockUser;
        } else {
            i.putExtra(DeviceService.EXTRA_AUTO_TEST_TYPE, LockSetActivity.unlockType);
        }
        // ]]
        if (i != null) {
            this.startService(i);
        }
        // [[wk
        if (MainActivity.CONNECTED.equals(connect_state)) {

            connectionState.setText(R.string.connected);

        }
        if (MCUCommand.string_sleep.equals(MainActivity.back)) {
            // must clear
            MainActivity.back = null;
            // wake up S00
            addLog(getString(R.string.wakeUp));
        }
        if (MainActivity.back == MainActivity.string_back) {
            MainActivity.back = null;
            finish();
        }
    }

    @Override
    protected void onPause() {
        this.unregisterBroadcast();
        super.onPause();
    }

    void addLog(String msg) {
        // wg before
        // if(mLogs.length() > 10*1024){
        // [[wk
        if (mLogs.length() > 200) {
            mLogs = "";
        }
        mLogs = String.format("%06d:%s\n%s", this.mLogLine++, msg, mLogs);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLogTextView.setText(mLogs);
                int h = mLogTextView.getMeasuredHeight();
                // editor.putString(user_Share.def_DeviceName, str_Device_Name);
                editor.putString(user_Share.def_DeviceAddress, deviceAddress);
                editor.commit();
                // if(FirmwareVesion.getText().toString().length() <= 0)
                {
                    // 获取设置值偏移值
                    // fun_GetConfigVesion();
                }

            }
        });
    }

    Date mStartDate = new Date();

    void setTestInfo(long startTime) {
        Date now = new Date();
        long diff = now.getTime() - startTime;
        long days = diff / (1000 * 60 * 60 * 24);
        long hours = (diff - days * (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (diff - days * (1000 * 60 * 60 * 24) - hours * (1000 * 60 * 60)) / (1000 * 60);
        String timestr = "" + days + "天" + hours + "小时" + minutes + "分";
        this.mTestInfo.setText(String.format("总次数：%d，成功:%d,失败:%d,耗时:%s", mTestTimes, mOkTimes, mFailTimes, timestr));
    }

    BroadcastReceiver mReceiverValue = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Log.d("getAction", "getAction" + intent.getAction());
            FileStream fileStream = new FileStream(); // 保存到文件
            switch (intent.getAction()) {

                case DeviceService.activity_GetConfig: {
                    Bundle bundle = intent.getExtras();
                    String flag = bundle.getString(DeviceService.EXTRA_LOG);
                    if (flag.equals("FW_HW")) {
                        byte[] data = bundle.getByteArray(DeviceService.EXTRA_VALUE);
                        String str_Serial = bundle.getString(DeviceService.EXTRA_SERIAL_NUMBER, "");
                        if (data != null) {
                            if (data[0] == (byte) 0x01 && data[2] == (byte) 0x02) {
                                byte i_firmware = 0;
                                i_firmware = (byte) (data[1]);
                                String str_firmware = String.valueOf(String.format("%02X", i_firmware));
                                if (FirmwareVesion != null && HardwareVesion != null) {
                                    FirmwareVesion.setText(str_firmware);
                                    HardwareVesion.setText(String.valueOf(String.format("%02X", (byte) data[3])));
                                }
                            }
                            fileStream.fileStream(FileStream.config_value, FileStream.write, data);

                        }
                        if (str_Serial.length() > 0) {
                            // 保存序列号到文件中
                            fileStream.fileStream(FileStream.config_serial_number, FileStream.write, str_Serial.getBytes());
                        }

                    }
                    // connectionState.setVisibility(View.VISIBLE); //在未获取固件值硬件值前不显示
                    // mLogTextView.setVisibility(View.VISIBLE); //在未获取固件值硬件值前不显示
                    i_GetVersion = 1; // 已经获取到固件号硬件号
                    if (i_ConnState == 1) {
                    /*
					 * new Thread(new Runnable() {
					 *
					 * @Override public void run() { // TODO Auto-generated
					 * method stub connectionState.setText(R.string.connected);
					 * mHandler.postDelayed(this, 1000); } }).start();
					 */
                        if (mHandler != null) {
                            mHandler.removeCallbacks(runnable);
                            mHandler.postDelayed(runnable, i_delay);

                        }

                    }

                }

                break;
            }
        }

    };

    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            connectionState.setText(R.string.connected);
        }
    };

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        LockerProcessAtivity that = LockerProcessAtivity.this;

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case DeviceService.ACTION_DEVICE_STATUS: {
                    Bundle b = intent.getExtras();
                    // [[wk
                    deviceAddress = b.getString(DeviceService.EXTRA_ADDRESS);
                    if (MainActivity.bluetoothNameSet != null) {
                        // bluetoothName changed
                        that.deviceName.setText(MainActivity.bluetoothNameSet + "(" + deviceAddress + ")");
                    } else {
                        that.deviceName.setText(b.getString(DeviceService.EXTRA_NAME));
                        MainActivity.bluetoothNameSet = deviceName.getText().toString();
                    }
                    if (MainActivity.CONNECTED.equals(connect_state)) {
                        that.connectionState.setText(R.string.connected);
                        // [[CXQ
                        i_ConnState = 1; // 连接成功
                        connectionState.setText(R.string.connected); // 在未获取固件值硬件值前显示连接中

                    }
                    // ]]
                    that.mTestTimes = b.getInt(DeviceService.EXTRA_TEST_TIMES);
                    that.mOkTimes = b.getInt(DeviceService.EXTRA_OK_TIMES);
                    that.mFailTimes = b.getInt(DeviceService.EXTRA_FAIL_TIMES);
                    long startTime = b.getLong(DeviceService.EXTRA_START_TIME);
                    mBigOpen = b.getInt(DeviceService.EXTRA_KEY_VALUE_TYPE);
                    mAutoTest = b.getString(DeviceService.EXTRA_AUTO_TEST_TYPE);
                    that.setTestInfo(startTime);
                    that.unlockName.setText(LockSetActivity.getActionText(that));
                    int status = b.getInt(DeviceService.EXTRA_STATUS);

                    switch (status) {
                        case DeviceService.DS_CONNECTED: {
                            that.addLog(getString(R.string.connected));
                            that.connectionState.setText(R.string.connected);

                            connectionState.setText(R.string.connected); // 在未获取固件值硬件值前显示连接中


                            // [[wk add
                            if (LockSetActivity.unlock_screw.equals(LockSetActivity.unlockType)) {
                                // handLockNumber
                                Byte state = b.getByte(LockSetActivity.unlock_screw);
                                dialogLockNumber();
                            }
                            // ]]
                        }
                        break;
                        case DeviceService.DS_DISCONNECTED: {

                            that.addLog(getString(R.string.disconnect));
                            that.connectionState.setText(R.string.disconnected);
                            // [[wk
                            MainActivity.back = MainActivity.string_back;
                            MainActivity.connect_state = disconnect;
                            finish();
                            // ]]

                        }
                        break;

                        case DeviceService.DS_LOG: {
                            String msg = b.getString(DeviceService.EXTRA_LOG);

                            that.addLog(msg);
                            // [[wk
                            if (MCUCommand.reconnecting.equals(msg) || getString(R.string.connecting).equals(msg)) {
                                that.connectionState.setText(R.string.reconnecting);
                            }
                            switch (LockSetActivity.unlockType) {
                                case LockSetActivity.unlockUser: {
                                    if (MainActivity.handLockNumber != null && MCUCommand.unlock_disabled.equals(msg)) {
                                        // handLockPassword
                                        dialogLockPwd();
                                    } else if (msg != null && (msg.contains(MCUCommand.send_lock_success) || msg.contains(MCUCommand.send_lock_success))) {
                                        Intent intent0 = new Intent(LockerProcessAtivity.this, DeviceService.class);
                                        intent0.setAction(DeviceService.ACTION_AUTO_TEST_TYPE);
                                        if (LockSetActivity.unlockType == null || LockSetActivity.unlockType.equals("")) {
                                            LockSetActivity.unlockType = LockSetActivity.unlockUser;
                                        } else {
                                            intent0.putExtra(DeviceService.EXTRA_AUTO_TEST_TYPE, LockSetActivity.unlockType);
                                        }
                                        if (intent0 != null) {
                                            LockerProcessAtivity.this.startService(intent0);
                                        }
                                        // change unlock_type to 掌机独立开锁
                                        intent0.setAction(SetActivity0.unlock_only);
                                        startService(intent0);
                                    }
                                }
                                break;
                                case LockSetActivity.unlock_screw: {
                                    if (MainActivity.handLockNumber == null && (MCUCommand.open_screw_success.equals(msg) || MCUCommand.close_screw_success.equals(msg) || MCUCommand.root_screw_disabled.equals(msg))) {
                                        // handLockNumber
                                        dialogLockNumber();
                                    }
                                }
                                break;
                                default: {
                                    // do nothing
                                }
                                break;
                            }
                            // ]]
                        }
                        break;
                        case DeviceService.DS_ERROR: {
                            // [[CXQ //不显示重发次数
                            String msg = b.getString(DeviceService.EXTRA_ERR_MSG);
                            that.addLog("Err:" + msg);
                            // ]]
                        }
                        break;
                    }

                }
                break;
                case DeviceService.ACTION_DEVICE_SLEEP_WAKEIP: {
                    Bundle b = intent.getExtras();
                    // 唤醒
                    String WakeUp = b.getString(MainActivity.state_sleep);
                    if (WakeUp != null && WakeUp.length() > 0) {
                        dialogWakeUp();
                    }
                }
                break;

                default:
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        menu.findItem(R.id.menu_connect).setVisible(false);
        menu.findItem(R.id.menu_disconnect).setVisible(true);
        switch (LockSetActivity.unlockType) {
            case LockSetActivity.unlockUser: {
                menu.findItem(R.id.menu_hand).setVisible(false);
                menu.findItem(R.id.menu_sheet_send).setVisible(false);
                menu.findItem(R.id.menu_offline_send).setVisible(false);
                menu.findItem(R.id.menu_upHistory).setVisible(true);
                menu.findItem(R.id.menu_offline_location).setVisible(false);
                menu.findItem(R.id.menu_delete_record).setVisible(true);
            }
            break;
            case LockSetActivity.unlock_screw: {
                menu.findItem(R.id.menu_hand).setVisible(false);
                menu.findItem(R.id.menu_sheet_send).setVisible(false);
                menu.findItem(R.id.menu_offline_send).setVisible(false);
                menu.findItem(R.id.menu_upHistory).setVisible(true);
                menu.findItem(R.id.menu_offline_location).setVisible(false);
            }
            break;
            default:
                menu.findItem(R.id.menu_hand).setVisible(false);
                menu.findItem(R.id.menu_sheet_send).setVisible(false);
                menu.findItem(R.id.menu_offline_send).setVisible(false);
                menu.findItem(R.id.menu_upHistory).setVisible(false);
                menu.findItem(R.id.menu_delete_record).setVisible(true);
                break;
        }
        menu.findItem(R.id.menu_set).setVisible(true);
        menu.findItem(R.id.menu_back).setVisible(false);
        menu.findItem(R.id.menu_upS00).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_disconnect:
                editor.putString(user_Share.def_AutoConnect, "0");
                editor.commit();
                Intent i = new Intent();
                i.setClass(this, DeviceService.class);
                i.setAction(DeviceService.ACTION_DISCONNECT);
                i.putExtra(DeviceService.EXTRA_ADDRESS, deviceAddress);
                i.putExtra(DeviceService.EXTRA_MANUAL, true);
                this.startService(i);
                return true;
            // 这里的R为android.R,非R.
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_hand:
                if (MainActivity.SLEEP.equals(MainActivity.state_sleep)) {
                    dialogWakeUp();
                } else {
                    dialogLockNumber();
                }
                return true;
            case R.id.menu_set:
                if (MainActivity.SLEEP.equals(MainActivity.state_sleep)) {
                    dialogWakeUp();
                } else {
                    // 保存由菜单进入的设置
                    editor.putString(user_Share.MY_CLASS, user_Share.class_SetAllActivity0);
                    editor.commit();
                    showSet();
                }
                return true;
            // 掌机授权
            case R.id.menu_sheet_send:
                if (MainActivity.SLEEP.equals(MainActivity.state_sleep)) {
                    dialogWakeUp();
                } else {
//					boolean wlanFlag = NetworkConnect.checkNet(getApplicationContext());
//					if (wlanFlag) {
                    sendS00_sheet();
//					} else {
//						Toast.makeText(this, "请先确保网络正常!", Toast.LENGTH_SHORT).show();
//					}

                }
                return true;
            // 用户授权
            case R.id.menu_offline_send:
                if (MainActivity.SLEEP.equals(MainActivity.state_sleep)) {
                    dialogWakeUp();
                } else {
                    boolean wlanFlag = NetworkConnect.checkNet(getApplicationContext());
                    if (wlanFlag) {
                        sendS00_offline();
                    } else {
                        Toast.makeText(this, "请先确保网络正常!", Toast.LENGTH_SHORT).show();
                    }

                }
                return true;
            // 新协议上传历史记录
            case R.id.menu_upHistory:
                if (MainActivity.SLEEP.equals(MainActivity.state_sleep)) {
                    dialogWakeUp();
                } else {
                    // 验证网络连接
                    boolean wlanFlag = NetworkConnect.checkNet(getApplicationContext());
                    if (wlanFlag) {
                        uploadHistory();
                    } else {
                        Toast.makeText(this, "请先确保网络正常!", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.menu_offline_location:
//				startActivityForResult(new Intent(this, com.android.location.MainActivity.class), 200);
                break;
            case R.id.menu_delete_record:
                if (MainActivity.SLEEP.equals(MainActivity.state_sleep)) {
                    dialogWakeUp();
                } else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setTitle("输入密码确认");
                    dialog.setIcon(android.R.drawable.ic_dialog_alert);
                    final EditText editText = new EditText(this);
                    editText.setHint("清除掌机中的记录不可恢复");
                    dialog.setView(editText);
                    dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            if (editText.getText().toString().equals("123456")) {
                                dialog.dismiss(); //关闭dialog
                                Intent intent = new Intent(LockerProcessAtivity.this, DeviceService.class);
                                intent.setAction(DeviceService.ACTION_DELETE);
                                startService(intent);
                            }
                        }
                    });
                    dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            dialog.dismiss();

                        }
                    });
                    dialog.create().show();

                }
                break;

            case R.id.menu_back:
                MainActivity.back = MainActivity.string_back;
			/*
			 * //check MainActivity flag_open if (!MainActivity.flag_open) {
			 * Intent intent=new Intent(); intent.setClass(getBaseContext(),
			 * MainActivity.class); startActivity(intent); }
			 */
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //
    private void dialogLockNumber() {
        FileStream fileStream = new FileStream();
        String str_DeviceID = new String(fileStream.fileStream(FileStream.deviceFile, FileStream.read, null));
        str_DeviceID = str_DeviceID.substring(0, 18);
        if (str_DeviceID.contains("default machine")) {
            addLog("请先设置掌机序列号!");
            return;
        }

        // 对话框 Builder是AlertDialog的静态内部类
        if (dialog == null) {
            dialog = new AlertDialog.Builder(this);
        }

        LayoutInflater inflater = getLayoutInflater();
        final View layout = inflater.inflate(R.layout.lock_number_dialog, (ViewGroup) findViewById(R.id.dialog));
        final EditText lockNumber = (EditText) layout.findViewById(R.id.lockNumber_value);
        final TextView textView = (TextView) layout.findViewById(R.id.result);
        textView.setTextColor(getResources().getColor(R.color.red));
        //	lockNumber.setText("060000003");

        // 设置对话框的标题
        dialog.setTitle("请输入锁号:");
        // 设置对话框要显示的消息
        dialog.setView(layout);
        // 给对话框来个按钮 叫“确定” ，并且设置监听器 这种写法也真是有些BT
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            // 点击 "确定"按钮之后要执行的操作就写在这里
            public void onClick(DialogInterface dialog, int which) {

                // 根据ID获取对象
                RadioGroup radioGroup = (RadioGroup) layout.findViewById(R.id.radioGroup1);
                // RadioButton screw_open = (RadioButton)
                // layout.findViewById(R.id.radioOpen);
                // RadioButton screw_close = (RadioButton)
                // layout.findViewById(R.id.radioClose);
                String screw_OpertType = "";
                int id = radioGroup.getCheckedRadioButtonId();
                switch (id) {
                    case R.id.radioOpen:
                        Log.d("radio", "radio" + "open");
                        screw_OpertType = "Open";
                        break;
                    case R.id.radioClose:
                        Log.d("radio", "radio" + "close");
                        screw_OpertType = "Close";
                        break;
                    default:
                        break;
                }

                //
                MainActivity.handLockNumber = lockNumber.getText().toString();
                switch (MainActivity.handLockNumber.length()) {
                    case 0: {
                        textView.setText("要求必须输入锁号!");
                        DialogMy.dialogCloseReflect(dialog, DialogMy.unclose);
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                    break;
                    case 9: {
                        Intent i = new Intent();
                        i.setClass(LockerProcessAtivity.this, DeviceService.class);

                        i.setAction(DeviceService.ACTION_AUTO_TEST_TYPE);
                        if (LockSetActivity.unlockType == null || LockSetActivity.unlockType.equals("")) {
                            LockSetActivity.unlockType = LockSetActivity.unlockUser;
                        }
                        i.putExtra(DeviceService.EXTRA_AUTO_TEST_TYPE, LockSetActivity.unlockType);
                        LockerProcessAtivity.this.startService(i);
                        if (MainActivity.handLockNumber != null && LockSetActivity.unlock_screw.equals(LockSetActivity.unlockType)) {
                            // addLog("输入螺丝号:"+MainActivity.handLockNumber+",再次按下掌机的开/关按钮后立刻执行开启/关闭此螺丝!");
                            addLog("输入螺丝号:" + MainActivity.handLockNumber);
                            dialog.dismiss(); //关闭dialog
                            Intent intent = new Intent();
                            intent.setClass(LockerProcessAtivity.this, DeviceService.class);
                            intent.setAction(LockSetActivity.unlock_screw);
                            intent.putExtra("LockNum", MainActivity.handLockNumber);
                            intent.putExtra(DeviceService.ScrewOperType, screw_OpertType);
                            startService(intent);

                        }
                        dialog.dismiss();
                        dialog.cancel();
                        dialog = null;

                    }
                    break;
                    default: {
                        textView.setText("锁号长度有误,目前只支持9位长度的锁号!");
                        DialogMy.dialogCloseReflect(dialog, DialogMy.unclose);
                    }
                    break;
                }
            }
        }).setNeutralButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                DialogMy.dialogCloseReflect(dialog, DialogMy.close);
                if (dialog != null) {
                    dialog.dismiss();
                    dialog.cancel();
                }
                finish();

            }
        }).create();// 创建按钮

        // 禁止错误操作关闭 dialog.setCanceledOnTouchOutside(false);
        dialog.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) { // TODO Auto-generated method stub
                if (arg1 == KeyEvent.KEYCODE_BACK && arg2.getRepeatCount() == 0) {
                    //
                    MainActivity.back = MainActivity.string_back;
                    DialogMy.dialogCloseReflect(arg0, DialogMy.close);
                    finish();
                    return true;
                }
                return false;
            }
        });
        dialog.show();// 显示一把
    }

    //
    private void dialogLockPwd() {
        LayoutInflater inflater = getLayoutInflater();
        final View layout = inflater.inflate(R.layout.lock_pwd, (ViewGroup) findViewById(R.id.dialog));
        // 对话框 Builder是AlertDialog的静态内部类
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        // 设置对话框的标题
        dialog.setTitle("请输入开锁密码:");
        // 设置对话框要显示的消息
        dialog.setView(layout);
        // 给对话框来个按钮 叫“确定” ，并且设置监听器 这种写法也真是有些BT
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            // 点击 "确定"按钮之后要执行的操作就写在这里
            public void onClick(DialogInterface dialog, int which) {
                EditText lockPwd = (EditText) layout.findViewById(R.id.lockPwdValue);
                TextView textView = (TextView) layout.findViewById(R.id.result);
                textView.setTextColor(getResources().getColor(R.color.red));
                MainActivity.handLockPassword = lockPwd.getText().toString();
                switch (MainActivity.handLockPassword.length()) {
                    case 0: {
                        textView.setText("密码不能为空!");
                        DialogMy.dialogCloseReflect(dialog, DialogMy.unclose);
                    }
                    break;
                    case 10:
                    case 15: {
                        Intent i = new Intent();
                        i.setClass(LockerProcessAtivity.this, DeviceService.class);
                        i.setAction(HAND_SET_LOCKPASSWORD);
                        LockerProcessAtivity.this.startService(i);
                        DialogMy.dialogCloseReflect(dialog, DialogMy.close);
                    }
                    break;
                    default: {
                        textView.setText("密码长度有误,目前只支持10或15位长度的开锁密码!");
                        DialogMy.dialogCloseReflect(dialog, DialogMy.unclose);
                    }
                    break;
                }
            }
        }).setNeutralButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                DialogMy.dialogCloseReflect(dialog, DialogMy.close);
                dialog.cancel();
            }
        }).create();// 创建按钮
        dialog.show();// 显示一把
    }

    //
    void dialogWakeUp() {
        // 对话框 Builder是AlertDialog的静态内部类
        if (m_Dialog == null && MainActivity.state_sleep.equals(MainActivity.SLEEP)) {
            m_Dialog = new AlertDialog.Builder(this)
                    // 设置对话框的标题
                    .setTitle("操作提示:")
                    // 设置对话框要显示的消息
                    .setMessage(getString(R.string.wakeUp)).create();// 创建按钮
            m_Dialog.show();// 显示一把
        } else {
            if (m_Dialog != null) {
                m_Dialog.cancel();
                m_Dialog = null;
            }
        }
    }

    // 按菜单获取设置值偏移值
    private void showSet() {
        Intent i = new Intent();
        i.setClass(LockerProcessAtivity.this, DeviceService.class);
        i.putExtra(DeviceService.activity_SetType, "menu_set");
        i.setAction(SetActivity0.get_setValue);
        LockerProcessAtivity.this.startService(i);
    }

    private void fun_GetConfigVesion() {
        // 发送命令获取偏移值
        mDevice = BleDeviceMgr.getMgr().GetDevice(deviceAddress);
        // send set //发送命令获取偏移值
        switch (i_step) {
            case 0:
                // send get
                byte[] cmdBytes = Command.getInstance().getS00SetValue(SetActivity0.VS_FW51_VER_GOT); // FW固件版本号
                mDevice.Write(FileStream.write, cmdBytes);
                // byte[] cmdBytes =
                // Command.getInstance().getS00SetValue(SetActivity0.VS_HW_GOT);
                // //硬件版本号
                // mService.mDevice.Write(FileStream.write,cmdBytes);
                i_step++;
                break;
            case 1:
                break;
            default:
                break;
        }

    }

    void sendS00_sheet() {
        // addLog("本地锁号排序开始后至下传掌机结束成功之前,禁止关闭当前界面进行其他操作会导致下传失败,排序开始,请稍后...");
        // send userDownload
        Intent i = new Intent();
        i.setClass(LockerProcessAtivity.this, DeviceService.class);
        i.setAction(send_sheet);
        i.putExtra("menu_action", DeviceService.download_sheet);
        LockerProcessAtivity.this.startService(i);
    }

    void sendS00_offline() {
        addLog("本地锁号排序开始后至下传掌机结束成功之前,禁止关闭当前界面进行其他操作会导致下传失败,排序开始,请稍后...");
        // send userDownload
        Intent i = new Intent();
        i.setClass(LockerProcessAtivity.this, DeviceService.class);
        i.setAction(send_sheet);
        i.putExtra("menu_action", DeviceService.download_offline);
        i.setAction(send_userDownload);
        LockerProcessAtivity.this.startService(i);
    }

    // 新协议
    public void uploadHistory() {
        Intent i = new Intent();
        i.setClass(LockerProcessAtivity.this, DeviceService.class);
        i.setAction(DeviceService.action_upload);
        i.putExtra("menu_action", DeviceService.upload_history);
        LockerProcessAtivity.this.startService(i);

    }


}
