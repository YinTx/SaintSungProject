package com.saintsung.saintpmc;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;

import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.model.NaviLatLng;

import com.google.gson.Gson;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.runtime.transaction.process.SaveModelTransaction;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.saintsung.saintpmc.asynctask.RetrofitRxAndroidHttp;

import com.saintsung.saintpmc.bean.QueryBureauNumberBean2;
import com.saintsung.saintpmc.bean.WorkOrderDataItemBean;

import com.saintsung.saintpmc.loading.LoginActivity;

import com.saintsung.saintpmc.loading.User;
import com.saintsung.saintpmc.loading.Utils;
import com.saintsung.saintpmc.location.BasicNaviActivity;
import com.saintsung.saintpmc.location.CheckPermissionsActivity;
import com.saintsung.saintpmc.lock.BluetoothLeService;
import com.saintsung.saintpmc.lock.CommandPacker;
import com.saintsung.saintpmc.lock.CommonResources;
import com.saintsung.saintpmc.lock.DeviceScanActivity0;
import com.saintsung.saintpmc.lock.DeviceService;
import com.saintsung.saintpmc.lock.FileStream;
import com.saintsung.saintpmc.lock.JSONArray;
import com.saintsung.saintpmc.lock.JSONException;
import com.saintsung.saintpmc.lock.JSONObject;
import com.saintsung.saintpmc.lock.LockSetActivity;
import com.saintsung.saintpmc.lock.NetworkConnect;
import com.saintsung.saintpmc.lock.SetActivity0;
import com.saintsung.saintpmc.lock.SetAllActivity;
import com.saintsung.saintpmc.lock.SocketActivity;
import com.saintsung.saintpmc.lock.SocketConnect;
import com.saintsung.saintpmc.lock.User_Share;
import com.saintsung.saintpmc.msgintercept.TextActivity;
import com.saintsung.saintpmc.orderdatabase.DicCategoryBean;
import com.saintsung.saintpmc.orderdatabase.DicLockSiteBean;

import com.saintsung.saintpmc.orderdatabase.DoorAndMeterDataBase;
import com.saintsung.saintpmc.orderdatabase.DoorAndMeterDataBase$Table;
import com.saintsung.saintpmc.orderdatabase.LockInformation;
import com.saintsung.saintpmc.orderdatabase.LstElecDeviceBean;
import com.saintsung.saintpmc.orderdatabase.LstElecUserBean;

import com.saintsung.saintpmc.orderdatabase.LstElecUserLockBean;

import com.saintsung.saintpmc.orderdatabase.LstElecUserMeteringBean;

import com.saintsung.saintpmc.orderdatabase.LstLookBean;

import com.saintsung.saintpmc.orderdatabase.WorkOrderBean;
import com.saintsung.saintpmc.orderdatabase.WorkOrderDetailsBean;
import com.saintsung.saintpmc.tool.DataProcess;
import com.saintsung.saintpmc.tool.ToastUtil;

import com.saintsung.saintpmc.workorder.PicWorkOrderActivity;
import com.saintsung.saintpmc.workorder.ScrapActivity;
import com.saintsung.saintpmc.workorder.WorkOrderDetailsPic;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import rx.functions.Action1;

import static com.saintsung.saintpmc.tool.DataProcess.*;

/**
 * ************************************************************************
 * **                              _oo0oo_                               **
 * **                             o8888888o                              **
 * **                             88" . "88                              **
 * **                             (| -_- |)                              **
 * **                             0\  =  /0                              **
 * **                           ___/'---'\___                            **
 * **                        .' \\\|     |// '.                          **
 * **                       / \\\|||  :  |||// \\                        **
 * **                      / _ ||||| -:- |||||- \\                       **
 * **                      | |  \\\\  -  /// |   |                       **
 * **                      | \_|  ''\---/''  |_/ |                       **
 * **                      \  .-\__  '-'  __/-.  /                       **
 * **                    ___'. .'  /--.--\  '. .'___                     **
 * **                 ."" '<  '.___\_<|>_/___.' >'  "".                  **
 * **                | | : '-  \'.;'\ _ /';.'/ - ' : | |                 **
 * **                \  \ '_.   \_ __\ /__ _/   .-' /  /                 **
 * **            ====='-.____'.___ \_____/___.-'____.-'=====             **
 * **                              '=---='                               **
 * ************************************************************************
 * **                        佛祖保佑      镇类之宝                         **
 * ************************************************************************
 */

public class MainActivity extends CheckPermissionsActivity
        implements NavigationView.OnNavigationItemSelectedListener, AMap.CancelableCallback, LocationSource, AMapLocationListener, ActivityCompat.OnRequestPermissionsResultCallback {
    public static Intent intentBluetoothLeService;
    public static BluetoothLeService bluetoothLeService;
    public static ServiceConnection mServiceConnection;
    private boolean flag;
    public static boolean flag_open;
    public static final int sendCount = 4;
    public static final int rtn_uid_len = 16;
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    int count = 0, length, countLength, countSX, count_false, SX_TIMES, ZQ_TIMES;
    public String string, userNameVlue, userPWDValue, lock, pwd, lockType, request, requestPacket, progress, responsePacket;
    public static String userId, DeviceID, IMEI, handLockNumber, handLockPassword, setS00, back, connect_state, bluetoothNameSet, downloadUpdate, state_sleep;
    public static final String MainActivity_ID = "MainActivity_ID";
    public String str_unlocktype = "";
    public static int step = 0;
    public static final String download_paceket_num_serviceId = "L005";
    //	public static final String download_by_sub_packet_serviceId ="L006";
    public static final String download_by_sub_packet_serviceId = "L011";
    public static final String check_software_version_serviceId = "L007";
    private SharedPreferences myPortSharedPreferences;
    public static final String string_back = "string_back";
    public static final String LOGINOUT = "";
    public static final String CONNECTED = "CONNECTED";
    public static final String SLEEP = "SLEEP";
    public static final String WAKEUP = "wakeup";
    private byte[] byteArray, receivedArray;
    private String[] stringArray;
    EditText editText, userName, userPWD;
    TextView textView;
    ProgressDialog mProgressDialog;
    byte[] lockArray = new byte[9];
    byte[] pwdArray = new byte[15];
    byte[] lockTypeArray = new byte[4];
    FileStream fileStream = new FileStream();
    CommandPacker commandPacker = new CommandPacker();
    SharedPreferences.Editor editor;
    User_Share user_Share = new User_Share();
    //-----------------------------------------------------------这是一个分割线----------------------------------------------------------------------
    private OnLocationChangedListener mListener;
    private final static int SCANNIN_GREQUEST_CODE = 1;
    private final static int SCANNIN_GREQUEST_CODE2 = 2;
    private ImageButton btnLocation;
    List<String[]> list = new LinkedList<>();
    private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
    SharedPreferences mySharedPreferences;
    private MapView mMap;
    private AMap myAmap;
    private boolean isLocation = true;
    private Button upLock;
    public static boolean isDown;
    //存储经纬度的数据类型
    public static LatLng LatAndlon;
    public AMapLocationClient mLocationClient = null;
    // 声明mLocationOption对象
    public AMapLocationClientOption mLocationOption = null;
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation amapLocation) {
            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
                    // 定位成功回调信息，设置相关消息
                    LatAndlon = new LatLng(amapLocation.getLatitude(),
                            amapLocation.getLongitude());
                } else {
//                     显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError", "location Error, ErrCode:"
                            + amapLocation.getErrorCode() + ", errInfo:"
                            + amapLocation.getErrorInfo());
                }
            }
        }
    };
    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        initView();
        initMap(savedInstanceState);
        //深圳蓝牙模块初始化
        initBlue();
        List<DicLockSiteBean> dicLockSiteBeen = new Select().from(DicLockSiteBean.class).queryList();
        for (DicLockSiteBean student : dicLockSiteBeen) {
            student.delete();
        }
        List<LockInformation> lockInformations = new Select().from(LockInformation.class).queryList();
        for (LockInformation student : lockInformations) {
            student.delete();
        }
        MyThread4("DIC_LOCK_SITE", "0001");
//        sHA1(this);
    }


    class MyTask_Download extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... arg0) {
            // TODO Auto-generated method stub
            // 连接服务器,发送开/关锁记录信息.
            SocketConnect socketConnect = new SocketConnect();
            responsePacket = socketConnect.sendDate(arg0[0]);
            return responsePacket;
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if (result.substring(10, 11).equals("0")) {
                Toast.makeText(MainActivity.this, "获取工单失败", Toast.LENGTH_LONG).show();
            } else {
                if (result.length() > 47) {
                    String workNo = result.substring(15, 31);
                    String str_start_time = result.substring(31, 43);
                    String str_end_time = result.substring(43, 55);
                    MyThread2(workNo, result.substring(55, 59), str_start_time, str_end_time);
                } else {
                    Toast.makeText(MainActivity.this, "工单中没有任务", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private WorkOrderDetailsBean workOrderForSqlite(String result, String workNo, String starTime, String endTime, WorkOrderDetailsBean workOrderDetailsBean) {
        if (result.substring(10, 11).equals("1")) {
            result = result.substring(35, result.length() - 32);

            for (int i = 0; i < result.length() / 56; i++) {
                workOrderDetailsBean.workOrderNumber = workNo;
                workOrderDetailsBean.lookNumber = result.substring(i * 56, i * 56 + 9);
                workOrderDetailsBean.openLookNumber = result.substring(i * 56 + 9, i * 56 + 24);
                workOrderDetailsBean.deviceType = result.substring(i * 56 + 24, i * 56 + 28);
                workOrderDetailsBean.longitude = result.substring(i * 56 + 28, i * 56 + 42);
                workOrderDetailsBean.latitude = result.substring(i * 56 + 42, i * 56 + 56);
                workOrderDetailsBean.insert();
            }
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
            String systemTime = formatter.format(curDate);
            MyThread3(workNo, systemTime, "0001");
        } else {
            Toast.makeText(MainActivity.this, "获取工单失败", Toast.LENGTH_LONG).show();
        }
        final List<WorkOrderDetailsBean> peoples = new Select().from(WorkOrderDetailsBean.class).queryList();
        return workOrderDetailsBean;
    }


    private void MyThread5(final String str, int i) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                com.saintsung.saintpmc.asynctask.SocketConnect socketConnect = new com.saintsung.saintpmc.asynctask.SocketConnect();
                String str1 = CommonResources.createRequestPacket(str);
                String result = socketConnect.sendDate(str1);
            }
        }).start();
    }

    private void MyThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                com.saintsung.saintpmc.asynctask.SocketConnect socketConnect = new com.saintsung.saintpmc.asynctask.SocketConnect();
                String str = "L008" + userId + MainActivity.IMEI;
                str = CommonResources.createRequestPacket(str);
                String result = socketConnect.sendDate(str);
            }
        }).start();
    }

    private void MyThread2(final String workNo, final String res, final String starTime, final String endTime) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                WorkOrderDetailsBean workOrdeDetailsrBean = new WorkOrderDetailsBean();
                WorkOrderBean workOrderBean = new WorkOrderBean();
                workOrderBean.workOrderNumber = workNo;
                workOrderBean.workOrderState = "0";
                workOrderBean.starTime = starTime;
                workOrderBean.endTime = endTime;
                workOrderBean.insert();
                for (int i = 1; i <= Integer.parseInt(res); i++) {
                    String str = "L013" + userId + MainActivity.IMEI + workNo + DataProcess.ComplementZeor(i + "", 4);
                    str = CommonResources.createRequestPacket(str);
                    com.saintsung.saintpmc.asynctask.SocketConnect socketConnect = new com.saintsung.saintpmc.asynctask.SocketConnect();
                    String result = socketConnect.sendDate(str);
                    workOrdeDetailsrBean = workOrderForSqlite(result, workNo, starTime, endTime, workOrdeDetailsrBean);
                }
            }
        }).start();
    }

    private void MyThread3(final String workNo, final String time, final String no) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String str = "L012" + MainActivity.IMEI + workNo + time + no;
                str = CommonResources.createRequestPacket(str);
                com.saintsung.saintpmc.asynctask.SocketConnect socketConnect = new com.saintsung.saintpmc.asynctask.SocketConnect();
                String result = socketConnect.sendDate(str);
            }
        }).start();
    }

    private void MyThread4(final String dbtable, final String packet) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String str = "L014" + DataProcess.ComplementZeor(userId, 16) + MainActivity.IMEI + ComplementSpace2(dbtable, 50) + packet;
                str = CommonResources.createRequestPacket(str);
                com.saintsung.saintpmc.asynctask.SocketConnect socketConnect = new com.saintsung.saintpmc.asynctask.SocketConnect();
                String result = socketConnect.sendDate(str);
                if (result.length() < 10) {
                    Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
                    return;
                }
                if (dbtable.equals("DIC_LOCK_SITE") && result.substring(10, 11).equals("1")) {
                    String result1 = result.substring(73, result.length() - 32);
                    try {
                        JSONArray jsonArray = new JSONArray(result1);
                        List<DicLockSiteBean> peoples = new Select().from(DicLockSiteBean.class).queryList();
//                        peoples.r
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            boolean flag3 = true;
                            for (int k = 0; k < peoples.size(); k++) {
                                String flag = peoples.get(k).id.toString();
                                String flag2 = jsonObject.getString("ID");
                                if (flag.equals(flag2)) {
                                    flag3 = false;
                                    break;
                                }
                            }
                            if (flag3) {
                                dicLockSiteBean = new DicLockSiteBean();
                                if (!jsonObject.isNull("ID")) {
                                    dicLockSiteBean.id = jsonObject.getString("ID");
                                }
                                if (!jsonObject.isNull("SITE_NAME")) {
                                    dicLockSiteBean.siteName = jsonObject.getString("SITE_NAME");
                                }
                                if (!jsonObject.isNull("SITE_CODE")) {
                                    dicLockSiteBean.siteCode = jsonObject.getString("SITE_CODE");
                                }
                                dicLockSiteBean.insert();
                            }
                        }
                        if (Integer.parseInt(packet) < Integer.parseInt(result.substring(65, 69))) {
                            int pac = Integer.parseInt(packet);
                            pac++;
                            MyThread4(dbtable, ComplementZeor(pac + "", 4));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                if (dbtable.equals("LST_ELEC_USER_LOCK") && result.substring(10, 11).equals("1")) {
                    String result1 = result.substring(73, result.length() - 32);
                    try {
                        JSONArray jsonArray = new JSONArray(result1);
                        List<LstElecUserLockBean> peoples = new Select().from(LstElecUserLockBean.class).queryList();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            boolean flag3 = true;
                            for (int k = 0; k < peoples.size(); k++) {
                                String flag = peoples.get(k).id.toString();
                                String flag2 = jsonObject.getString("ID");
                                if (flag.equals(flag2)) {
                                    flag3 = false;
                                    break;
                                }
                            }
                            if (flag3) {
                                lstElecUserLockBean = new LstElecUserLockBean();
                                if (!jsonObject.isNull("ID")) {
                                    lstElecUserLockBean.id = Long.valueOf(jsonObject.getString("ID"));
                                }
                                if (!jsonObject.isNull("SITE_ID")) {
                                    lstElecUserLockBean.SITE_ID = jsonObject.getString("SITE_ID");
                                }
                                if (!jsonObject.isNull("ELEC_USER_ID")) {
                                    lstElecUserLockBean.ELEC_USER_ID = jsonObject.getString("ELEC_USER_ID");
                                }
                                if (!jsonObject.isNull("IS_DELETE")) {
                                    lstElecUserLockBean.IS_DELETE = jsonObject.getString("IS_DELETE");
                                }

                                if (!jsonObject.isNull("L_ID")) {
                                    lstElecUserLockBean.L_ID = jsonObject.getString("L_ID");
                                }
                                lstElecUserLockBean.insert();
                            }
                        }
                        if (Integer.parseInt(packet) < Integer.parseInt(result.substring(65, 69))) {
                            int pac = Integer.parseInt(packet);
                            pac++;
                            MyThread4(dbtable, ComplementZeor(pac + "", 4));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                if (dbtable.equals("LST_ELEC_USER") && result.substring(10, 11).equals("1")) {
                    String result1 = result.substring(73, result.length() - 32);
                    try {
                        JSONArray jsonArray = new JSONArray(result1);
                        List<LstElecUserBean> peoples = new Select().from(LstElecUserBean.class).queryList();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            boolean flag3 = true;
                            for (int k = 0; k < peoples.size(); k++) {
                                String flag = peoples.get(k).ID.toString();
                                String flag2 = jsonObject.getString("ID");
                                if (flag.equals(flag2)) {
                                    flag3 = false;
                                    break;
                                }
                            }
                            if (flag3) {
                                lstElecUserBean = new LstElecUserBean();
                                if (!jsonObject.isNull("ID")) {
                                    lstElecUserBean.ID = Long.valueOf(jsonObject.getString("ID"));
                                }
                                if (!jsonObject.isNull("ELEC_LEVER")) {
                                    lstElecUserBean.ELEC_LEVER = jsonObject.getString("ELEC_LEVER");
                                }
                                if (!jsonObject.isNull("ELEC_USER_MOBILE")) {
                                    lstElecUserBean.ELEC_USER_MOBILE = jsonObject.getString("ELEC_USER_MOBILE");
                                }
                                if (!jsonObject.isNull("ELEC_USER_NAME")) {
                                    lstElecUserBean.ELEC_USER_NAME = jsonObject.getString("ELEC_USER_NAME");
                                }
                                if (!jsonObject.isNull("ELEC_USER_STATUS")) {
                                    lstElecUserBean.ELEC_USER_STATUS = jsonObject.getString("ELEC_USER_STATUS");
                                }
                                if (!jsonObject.isNull("ELEC_USER_TYPE")) {
                                    lstElecUserBean.ELEC_USER_TYPE = jsonObject.getString("ELEC_USER_TYPE");
                                }
                                if (!jsonObject.isNull("IS_DELETE")) {
                                    lstElecUserBean.IS_DELETE = jsonObject.getString("IS_DELETE");
                                }
                                if (!jsonObject.isNull("METER_POINT_ID")) {
                                    lstElecUserBean.METER_POINT_ID = jsonObject.getString("METER_POINT_ID");
                                }
                                if (!jsonObject.isNull("ELEC_USER_NO")) {
                                    lstElecUserBean.ELEC_USER_NO = jsonObject.getString("ELEC_USER_NO");
                                }
                                if (!jsonObject.isNull("OFFICE_NO")) {
                                    lstElecUserBean.OFFICE_NO = jsonObject.getString("OFFICE_NO");
                                }
                                lstElecUserBean.insert();
                            }
                        }
                        if (Integer.parseInt(packet) < Integer.parseInt(result.substring(65, 69))) {
                            int pac = Integer.parseInt(packet);
                            pac++;
                            MyThread4(dbtable, ComplementZeor(pac + "", 4));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                if (dbtable.equals("LST_ELEC_DEVICE") && result.substring(10, 11).equals("1")) {
                    String result1 = result.substring(73, result.length() - 32);
                    try {
                        JSONArray jsonArray = new JSONArray(result1);
                        List<LstElecDeviceBean> peoples = new Select().from(LstElecDeviceBean.class).queryList();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            boolean flag3 = true;
                            for (int k = 0; k < peoples.size(); k++) {
                                String flag = peoples.get(k).ID.toString();
                                String flag2 = jsonObject.getString("ID");
                                if (flag.equals(flag2)) {
                                    flag3 = false;
                                    break;
                                }
                            }

                            if (flag3) {
                                lstElecDeviceBean = new LstElecDeviceBean();
                                if (!jsonObject.isNull("ID")) {
                                    lstElecDeviceBean.ID = Long.valueOf(jsonObject.getString("ID"));
                                }
                                if (!jsonObject.isNull("ELEC_DEVICE_NO")) {
                                    lstElecDeviceBean.ELEC_DEVICE_NO = jsonObject.getString("ELEC_DEVICE_NO");
                                }
                                if (!jsonObject.isNull("ELEC_DEVICE_STATUS")) {
                                    lstElecDeviceBean.ELEC_DEVICE_STATUS = jsonObject.getString("ELEC_DEVICE_STATUS");
                                }
                                if (!jsonObject.isNull("ELEC_DEVICE_TYPE")) {
                                    lstElecDeviceBean.ELEC_DEVICE_TYPE = jsonObject.getString("ELEC_DEVICE_TYPE");
                                }
                                if (!jsonObject.isNull("ELEC_DEVICE_VENDER")) {
                                    lstElecDeviceBean.ELEC_DEVICE_VENDER = jsonObject.getString("ELEC_DEVICE_VENDER");
                                }
                                if (!jsonObject.isNull("IS_DELETE")) {
                                    lstElecDeviceBean.IS_DELETE = jsonObject.getString("IS_DELETE");
                                }
                                if (!jsonObject.isNull("METER_POINT_ID")) {
                                    lstElecDeviceBean.METER_POINT_ID = jsonObject.getString("METER_POINT_ID");
                                }
                                lstElecDeviceBean.insert();
                            }
                        }
                        if (Integer.parseInt(packet) < Integer.parseInt(result.substring(65, 69))) {
                            int pac = Integer.parseInt(packet);
                            pac++;
                            MyThread4(dbtable, ComplementZeor(pac + "", 4));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                if (dbtable.equals("DIC_CATEGORY") && result.substring(10, 11).equals("1")) {
                    String result1 = result.substring(73, result.length() - 32);

                    try {
                        JSONArray jsonArray = new JSONArray(result1);
                        List<DicCategoryBean> peoples = new Select().from(DicCategoryBean.class).queryList();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            boolean flag3 = true;
                            for (int k = 0; k < peoples.size(); k++) {
                                String flag = peoples.get(k).ID.toString();
                                String flag2 = jsonObject.getString("ID");
                                if (flag.equals(flag2)) {
                                    flag3 = false;
                                    break;
                                }
                            }
                            if (flag3) {
                                dicCategoryBean = new DicCategoryBean();
                                if (!jsonObject.isNull("ID")) {
                                    dicCategoryBean.ID = Long.valueOf(jsonObject.getString("ID"));
                                }
                                if (!jsonObject.isNull("CATE_NAME")) {
                                    dicCategoryBean.CATE_NAME = jsonObject.getString("CATE_NAME");
                                }
                                if (!jsonObject.isNull("IS_DELETE")) {
                                    dicCategoryBean.IS_DELETE = jsonObject.getString("IS_DELETE");
                                }
                                if (!jsonObject.isNull("P_ID")) {
                                    dicCategoryBean.P_ID = jsonObject.getString("P_ID");
                                }
                                if (!jsonObject.isNull("POINT_X")) {
                                    dicCategoryBean.POINT_X = jsonObject.getString("POINT_X");
                                }
                                if (!jsonObject.isNull("POINT_Y")) {
                                    dicCategoryBean.POINT_Y = jsonObject.getString("POINT_Y");
                                }
                                dicCategoryBean.insert();
                            }
                        }
                        if (Integer.parseInt(packet) < Integer.parseInt(result.substring(65, 69))) {
                            int pac = Integer.parseInt(packet);
                            pac++;
                            MyThread4(dbtable, ComplementZeor(pac + "", 4));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                if (dbtable.equals("LST_LOCK") && result.substring(10, 11).equals("1")) {
                    String result1 = result.substring(73, result.length() - 32);

                    try {
                        JSONArray jsonArray = new JSONArray(result1);
                        List<LstLookBean> peoples = new Select().from(LstLookBean.class).queryList();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            boolean flag3 = true;
                            for (int k = 0; k < peoples.size(); k++) {
                                String flag = peoples.get(k).ID.toString();
                                String flag2 = jsonObject.getString("ID");
                                if (flag.equals(flag2)) {
                                    flag3 = false;
                                    break;
                                }
                            }
                            if (flag3) {
                                lstLookBean = new LstLookBean();
                                if (!jsonObject.isNull("ID")) {
                                    lstLookBean.ID = Long.valueOf(jsonObject.getString("ID"));
                                }
                                if (!jsonObject.isNull("ADDRESS")) {
                                    lstLookBean.ADDRESS = jsonObject.getString("ADDRESS");
                                }
                                if (!jsonObject.isNull("CATE_ID")) {
                                    lstLookBean.CATE_ID = jsonObject.getString("CATE_ID");
                                }
                                if (!jsonObject.isNull("IS_DELETE")) {
                                    lstLookBean.IS_DELETE = jsonObject.getString("IS_DELETE");
                                }
                                if (!jsonObject.isNull("L_NO")) {
                                    lstLookBean.L_NO = jsonObject.getString("L_NO");
                                }
                                if (!jsonObject.isNull("OPT_PWD")) {
                                    lstLookBean.OPT_PWD = jsonObject.getString("OPT_PWD");
                                }
                                if (!jsonObject.isNull("POINT_X")) {
                                    lstLookBean.POINT_X = jsonObject.getString("POINT_X");
                                }
                                if (!jsonObject.isNull("POINT_Y")) {
                                    lstLookBean.POINT_Y = jsonObject.getString("POINT_Y");
                                }
                                if (!jsonObject.isNull("STATE_ID")) {
                                    lstLookBean.STATE_ID = jsonObject.getString("STATE_ID");
                                }
                                if (!jsonObject.isNull("TYPE_ID")) {
                                    lstLookBean.TYPE_ID = jsonObject.getString("TYPE_ID");
                                }
                                if (!jsonObject.isNull("USER_ID")) {
                                    lstLookBean.USER_ID = jsonObject.getString("USER_ID");
                                }
                                if (!jsonObject.isNull("ASSET_NO")) {
                                    lstLookBean.ASSET_NO = jsonObject.getString("ASSET_NO");
                                }
                                lstLookBean.insert();

                                new SaveModelTransaction<>(ProcessModelInfo.withModels(lstLookBean)).onExecute();
                            }
                        }
                        if (Integer.parseInt(packet) < Integer.parseInt(result.substring(65, 69))) {
                            int pac = Integer.parseInt(packet);
                            pac++;
                            MyThread4(dbtable, ComplementZeor(pac + "", 4));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                if (dbtable.equals("LST_ELEC_USER_METERING") && result.substring(10, 11).equals("1")) {
                    String result1 = result.substring(73, result.length() - 32);
                    try {
                        JSONArray jsonArray = new JSONArray(result1);
                        List<LstElecUserMeteringBean> peoples = new Select().from(LstElecUserMeteringBean.class).queryList();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            boolean flag3 = true;
                            for (int k = 0; k < peoples.size(); k++) {
                                String flag = peoples.get(k).ID.toString();
                                String flag2 = jsonObject.getString("ID");
                                if (flag.equals(flag2)) {
                                    flag3 = false;
                                    break;
                                }
                            }
                            if (flag3) {
                                lstElecUserMeteringBean = new LstElecUserMeteringBean();
                                if (!jsonObject.isNull("ID")) {
                                    lstElecUserMeteringBean.ID = Long.valueOf(jsonObject.getString("ID"));
                                }
                                if (!jsonObject.isNull("ELEC_USER_ID")) {
                                    lstElecUserMeteringBean.ELEC_USER_ID = jsonObject.getString("ELEC_USER_ID");
                                }
                                if (!jsonObject.isNull("METERING_NO")) {
                                    lstElecUserMeteringBean.METERING_NO = jsonObject.getString("METERING_NO");
                                }
                                lstElecUserMeteringBean.insert();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();

    }

    /**
     * 查看app sha1 码
     *
     * @param context
     * @return
     */
    @Nullable
    public static String sHA1(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            byte[] cert = info.signatures[0].toByteArray();
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(cert);
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < publicKey.length; i++) {
                String appendString = Integer.toHexString(0xFF & publicKey[i])
                        .toUpperCase(Locale.US);
                if (appendString.length() == 1)
                    hexString.append("0");
                hexString.append(appendString);
                hexString.append(":");
            }
            String result = hexString.toString();
            return result.substring(0, result.length() - 1);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initBlue() {
        // 文件保存信息
        user_Share = new User_Share();
        mySharedPreferences = getSharedPreferences(user_Share.MY_PREFS, MODE_PRIVATE);
        editor = mySharedPreferences.edit();
        LockSetActivity.unlockType = LockSetActivity.unlockUser;
        //写入到文件中
        fileStream.fileStream(FileStream.unlockType, FileStream.write, LockSetActivity.unlockType.getBytes());
        //
        flag_open = true;
        //去除标题
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //初始化(默认)开锁方式
        readStream(FileStream.unlockType);
        str_unlocktype = new String(byteArray);
        //IMEI（International Mobile Equipment Identity）是移动设备国际身份码的缩写，IMEI由15位数字组成。
        IMEI = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
        //规范IMEI长度
        do {
            //长度不足左补0
            IMEI = "0" + IMEI;
        } while (IMEI.length() < 18);
        //socket Validate
        if (downloadUpdate == null || downloadUpdate.equals("")) {
            downloadUpdate = SetAllActivity.disenabled;
        }
        byteArray = fileStream.fileStream(FileStream.userLogin, FileStream.read, null);
        string = new String(byteArray);
        stringArray = string.split(",");
        userId = stringArray[2];
    }

    //----------------------------------------------------这里是深圳的方法--------------------------------------
    //获取开锁方式从本机lockType.txt文件里
    private void readStream(String state) {
        FileStream fileStream = new FileStream();
        byteArray = fileStream.fileStream(state, FileStream.read, null);
        if (byteArray.length == 0) {
            //默认设置为用户离线开锁

            Toast.makeText(MainActivity.this, "用户离线开锁(默认设置)!", Toast.LENGTH_SHORT).show();
        } else {
            LockSetActivity.unlockType = new String(byteArray);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS: //we set this to 0
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("下载进度...");
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setMax(100);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }

    void dialogWakeUp() {
        //对话框   Builder是AlertDialog的静态内部类
        Dialog dialog = new AlertDialog.Builder(this)
                //设置对话框的标题
                .setTitle("操作提示:")
                //设置对话框要显示的消息
                .setMessage(getString(R.string.wakeUp))
                .create();//创建按钮
        dialog.show();//显示一把
    }

    void checkDownoad() {
        //get userId
        request = download_paceket_num_serviceId + userId + MainActivity.IMEI;
        //打包请求
        string = CommonResources.createRequestPacket(request);
        dialogDownload2();
    }

    void dialogDownload2() {
        //对话框   Builder是AlertDialog的静态内部类
        Dialog dialog = new AlertDialog.Builder(this)
                //设置对话框的标题
                .setTitle("用户下载离线关联锁具信息提示:")
                //设置对话框要显示的消息
                .setMessage("确定要下载用户关联锁具信息(离线)么?")
                //给对话框来个按钮 叫“确定” ，并且设置监听器 这种写法也真是有些BT
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    //点击 "确定"按钮之后要执行的操作就写在这里
                    public void onClick(DialogInterface dialog, int which) {
                        //验证网络连接
                        flag = NetworkConnect.checkNet(getApplicationContext());
                        if (flag) {
                            new AsyncTaskUserDownload().execute("");
                        } else {
                            Toast.makeText(getBaseContext(), "网络断开连接,联网后再进行其他操作!", Toast.LENGTH_LONG).show();
                        }
                    }
                }).setNeutralButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                }).create();//创建按钮
        dialog.show();//显示一把
    }

    //UI子线程
    class AsyncTaskUserDownload extends AsyncTask<String, String, String> {
        @SuppressWarnings("deprecation")
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //open the dialog before the file was downloaded(点击之后,下载执行之前,设置进度条可见)
            showDialog(DIALOG_DOWNLOAD_PROGRESS);
        }

        @Override
        protected String doInBackground(String... params) {
            //delete before userDownload.txt
            fileStream.fileStream(FileStream.userDownload, FileStream.delete, null);
            //将拆分后的服务器数据直接写入本地文件
            SocketConnect socketConnect = new SocketConnect();
            responsePacket = socketConnect.sendDate(string);                        //请求包的个数
            if (responsePacket.startsWith(download_paceket_num_serviceId, 6)) {    //L005
                commandPacker.decodeResultFlag(responsePacket);
                //initialize count
                if (CommandPacker.succ_flag) {
                    step = 1;
                    count = Integer.parseInt(responsePacket.substring(15, 19));
                    for (int countPackage = 1; countPackage <= count; countPackage++) {

                        request = download_by_sub_packet_serviceId + userId + MainActivity.IMEI + CommonResources.getleftFill0Str(String.valueOf(countPackage), sendCount);        //L006
                        //打包请求
                        string = CommonResources.createRequestPacket(request);
                        //连接服务器,发送下载请求.
                        responsePacket = socketConnect.sendDate(string);
                        Log.d("responsePacket", "responsePacket" + responsePacket);
                        //判断执行
                        if (responsePacket.startsWith(download_by_sub_packet_serviceId, 6) && responsePacket.length() > 51) {    //请求数据包
                            //拆分服务器返回数据
                            commandPacker.decodeResultFlag(responsePacket);
                            receivedArray = responsePacket.getBytes();
                            length = receivedArray.length - 51;
                            byteArray = new byte[length];
                            System.arraycopy(receivedArray, 19, byteArray, 0, length);
                            //新协议加锁类型由36变成40一个锁信息
                            //"004045 L011 1000 0000 1 000000006000000000000000000000000000 0001
                            int lock_info_length = 36 + 4;
                            //拆出锁号
                            for (int i = 0; i < byteArray.length / lock_info_length; i++) {
                                System.arraycopy(byteArray, i * lock_info_length, lockArray, 0, lockArray.length);
                                lock = new String(lockArray);
                                System.arraycopy(byteArray, i * lock_info_length + 21, pwdArray, 0, pwdArray.length);
                                pwd = new String(pwdArray);
                                System.arraycopy(byteArray, i * lock_info_length + 36, lockTypeArray, 0, lockTypeArray.length);
                                lockType = new String(lockTypeArray);
                                string = lock + ":" + pwd + "|" + lockType + "\r\n";

                                //将拆分后的服务器数据直接写入本地文件
                                fileStream.fileStream(FileStream.userDownload, FileStream.write, string.getBytes());
                            }
                            countLength += byteArray.length / lock_info_length;
                        }
                        publishProgress("" + (int) ((countPackage * 100) / count));
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void onPostExecute(String result) {
            //dismiss/close the dialog after the file was downloaded
            dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
            //等待通讯成功
            if (CommandPacker.succ_flag) {
                CommandPacker.succ_flag = false;
                if (count == 0) {
                    Toast.makeText(getBaseContext(), "此用户尚未关联任何1把锁具信息!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getBaseContext(), "用户下载离线关联锁具信息下载结果:" + count + "个包,共计" + countLength + "条离线锁具信息成功完成下载!", Toast.LENGTH_LONG).show();
                    countLength = 0;
                    //must changed ServiceActivity.disenabled
                    downloadUpdate = SetAllActivity.disenabled;
                }
            } else if (responsePacket.startsWith("java.net.UnknownHostException:")) {
                byteArray = fileStream.fileStream(FileStream.socket, FileStream.read, null);
                string = new String(byteArray);
                stringArray = string.split(";");
                Intent intent = new Intent();
                intent.setClass(getBaseContext(), SocketActivity.class);
                intent.putExtra(SocketConnect.socketError, "socket通信地址输入有误,确认修改正确后,再尝试重新下载!");
                intent.putExtra(SocketConnect.socketAddress, stringArray[0]);
                intent.putExtra(SocketConnect.socketPort, stringArray[1]);
                startActivity(intent);
            } else if (responsePacket.startsWith("java.lang.IllegalArgumentException: Port out of range:")) {
                byteArray = fileStream.fileStream(FileStream.socket, FileStream.read, null);
                string = new String(byteArray);
                stringArray = string.split(";");
                Intent intent = new Intent();
                intent.setClass(getBaseContext(), SocketActivity.class);
                intent.putExtra(SocketConnect.socketError, "socket通信端口输入有误,确认修改正确后,再尝试重新下载!");
                intent.putExtra(SocketConnect.socketAddress, stringArray[0]);
                intent.putExtra(SocketConnect.socketPort, stringArray[1]);
                startActivity(intent);
            } else if (responsePacket.startsWith("java.net.ConnectException")) {
                Toast.makeText(getBaseContext(), "网络断开连接,导致访问服务器登录超时,确定网络正常后再尝试重新下载!", Toast.LENGTH_LONG).show();
            } else if (responsePacket.contains("SocketTimeoutException")) {
                Toast.makeText(getBaseContext(), "服务器登录超时,确认服务器能正常访问后再尝试重新下载!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getBaseContext(), "下载失败,请重新下载!", Toast.LENGTH_LONG).show();
            }
            //must clear
            flag = false;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    private String getUpLook() {
        byte[] byteLog = fileStream.fileStream(FileStream.log, FileStream.read, null);
        String res = "L004" + MainActivity.IMEI + userId + "285436506" + "20170526082418" + "1";
        res = CommonResources.createRequestPacket(res);
        return res;
    }

    class MyLookUp extends AsyncTask<String, String, String> {
        @SuppressWarnings("deprecation")
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //open the dialog before the file was downloaded(点击之后,下载执行之前,设置进度条可见)
            showDialog(DIALOG_DOWNLOAD_PROGRESS);
        }

        @Override
        protected String doInBackground(String... params) {
            com.saintsung.saintpmc.asynctask.SocketConnect socketConnect = new com.saintsung.saintpmc.asynctask.SocketConnect();
            return socketConnect.sendDate(getUpLook());
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void onPostExecute(String result) {
            dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    //---------------------------------------------------- 这是一个分割线-----------------------------
    private void msgString(String str) {
        if (str.equals("") || str == null) {
            return;
        }

        String[] rr = null;
        String[] rt = str.split("\\(");
        String[] time = rt[0].split("\\|");//必有4个数据
        String address;
        String[] user2 = null;
        String strs = "";
        for (int i = 1; i < rt.length; i++) {
            user2 = rt[i].split("\\[");
            for (int k = 1; k < user2.length; k++) {
                address = user2[0];
                String equipment = user2[k];
                address = address + "|" + equipment;
                rr = address.split("\\]");
                address = rr[0];
                rr = address.split("\\|");
                rr = getUs(time, rr);
                Date curDate = new Date(System.currentTimeMillis());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
                String strDate = sdf.format(curDate);
                if (Long.parseLong(strReplace(rr[1])) < Long.parseLong(strDate) && Long.parseLong(strDate) < Long.parseLong(strReplace(rr[2]))) {
                    list.add(rr);
                    strs += rr[0] + ":" + strReplace(rr[1]) + ":" + strReplace(rr[2]) + ":" + rr[7] + ":" + rr[8] + ":" + "000" + rr[9] + ":" + getZerofill(rr[5]) + ":" + getZerofill(rr[6]) + "\r\n";
                }
            }
        }
        fileStream.fileStream(FileStream.sheetFile, FileStream.write, strs.getBytes());
    }

    private String getZerofill(String str) {
        int str1 = str.length();
        for (int i = 0; i < 14 - str1; i++) {
            str = str + "0";
        }
        return str;
    }

    private String strReplace(String str) {
        str = str.replace(" ", "");
        str = str.replace("-", "");
        str = str.replace(":", "");
        return str;
    }

    private static String[] getUs(String[] user1, String[] user2) {
        String[] rtUser = new String[user1.length + user2.length];
        for (int i = 0; i < user1.length; i++) {
            rtUser[i] = user1[i];
        }
        for (int i = user1.length; i < user1.length + user2.length; i++) {
            rtUser[i] = user2[i - user1.length];
        }
        return rtUser;
    }

    TextView mYText;
    Button wordOdown2;
    LstLookBean lstLookBean;
    LstElecUserLockBean lstElecUserLockBean;
    LstElecUserBean lstElecUserBean;
    LstElecDeviceBean lstElecDeviceBean;
    DicCategoryBean dicCategoryBean;
    LstElecUserMeteringBean lstElecUserMeteringBean;
    DicLockSiteBean dicLockSiteBean = new DicLockSiteBean();
    int con = 0;
    private void initView() {
        RetrofitRxAndroidHttp retrofitRxAndroidHttp = new RetrofitRxAndroidHttp();
        List<DoorAndMeterDataBase> doorAndMeterDataBase = new Select().from(DoorAndMeterDataBase.class).queryList();
        for (DoorAndMeterDataBase doorAndMeterDataBase1 : doorAndMeterDataBase) {
            retrofitRxAndroidHttp.serviceConnect(MyApplication.getUrl(), doorAndMeterDataBase1.jsonStrInService, action2);
        }
        String userName = MyApplication.getUserName();
        retrofitRxAndroidHttp.serviceConnect(MyApplication.getUrl(), getGsonStr(), action1);
        dicCategoryBean = new DicCategoryBean();
        lstElecDeviceBean = new LstElecDeviceBean();
        lstElecUserBean = new LstElecUserBean();
        lstElecUserLockBean = new LstElecUserLockBean();
        lstLookBean = new LstLookBean();
        lstElecUserMeteringBean = new LstElecUserMeteringBean();
        LockSetActivity.unlockType = LockSetActivity.unlockUser;
        //写入到文件中
        fileStream.fileStream(FileStream.unlockType, FileStream.write, LockSetActivity.unlockType.getBytes());
        //
        flag_open = true;
        myPortSharedPreferences = this.getSharedPreferences("port", Context.MODE_PRIVATE);
        mySharedPreferences = getSharedPreferences("orderInfo",
                Activity.MODE_PRIVATE);
        mMap = (MapView) findViewById(R.id.mMap);
        btnLocation = (ImageButton) findViewById(R.id.mbtn_d);

        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (con == 5) {
                    startActivity(new Intent(MainActivity.this, TextActivity.class));
                }else{
                    con++;
                }
                if (LatAndlon == null) {
                    new ToastUtil().getToast(MainActivity.this, "正在获取地理位置,请稍后...");
                } else {
                    Toast.makeText(MainActivity.this, "当前经纬度" + LatAndlon.latitude + "    -" + LatAndlon.longitude, Toast.LENGTH_LONG).show();
                    changeCamera(CameraUpdateFactory
                            .newCameraPosition(new CameraPosition(LatAndlon, 18,
                                    30, 0)), MainActivity.this);
                }
            }
        });
        wordOdown = (Button) findViewById(R.id.wordOdown);
        wordOdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyThread();
            }
        });
        mYText = (TextView) findViewById(R.id.myText);
        upLock = (Button) findViewById(R.id.up_lock);
        upLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] bytes = fileStream.fileStream(FileStream.log, FileStream.read, null);
            }
        });
    }

    private Action1<ResponseBody> action2 = new Action1<ResponseBody>() {

        @Override
        public void call(ResponseBody responseBody) {
            try {
                dataProcess(responseBody.string());
//                Log.e("TAG","====="+);
//                dataProcessing(responseBody.string());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
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

    private void dataProcess(String string) {
        Log.e("TAG", "" + string);
        Gson gson = new Gson();
        QueryBureauNumberBean2 queryBureauNumberBean2 = gson.fromJson(string,QueryBureauNumberBean2.class);
        if (queryBureauNumberBean2.getResult().equals("0000")) {
            DoorAndMeterDataBase doorAndMeterDataBase = new Select().from(DoorAndMeterDataBase.class).where(Condition.column(DoorAndMeterDataBase$Table.EDITBUREAUNO).is(queryBureauNumberBean2.getUserNumber())).querySingle();
            doorAndMeterDataBase.delete();
        }
    }

    private void dataProcessing(String string) {
        Log.e("TAG", "" + string);
        Gson gson = new Gson();
        com.saintsung.saintpmc.bean.WorkOrderBean workOrderBean = gson.fromJson(string, com.saintsung.saintpmc.bean.WorkOrderBean.class);
        if (workOrderBean.getResult().equals("0000"))
            MyApplication.setWorkOrderBean(workOrderBean);
        LockInformation lockInformation = new LockInformation();
        for (int i = 0; i < workOrderBean.getData().size(); i++) {
            List<WorkOrderDataItemBean> workOrderDataItemBeanList = workOrderBean.getData().get(i).getLockInfos();
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

    private String getGsonStr() {
        Gson gson = new Gson();
        String sign = "";
        com.saintsung.saintpmc.bean.WorkOrderBean workOrderBean = new com.saintsung.saintpmc.bean.WorkOrderBean();
        workOrderBean.setOptCode("GetWorkOrderInfos");
        workOrderBean.setOptUserNumber(MyApplication.getUserId());
        sign = com.saintsung.saintpmc.lock.MD5.toMD5(workOrderBean.getOptCode() + workOrderBean.getOptUserNumber() + workOrderBean.getData());
        workOrderBean.setSign(sign);
        return gson.toJson(workOrderBean);
    }

    public static Date getNextDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        date = calendar.getTime();
        return date;
    }

    Button wordOdown;

    private void initMap(Bundle savedInstanceState) {
        mMap.onCreate(savedInstanceState);
        myAmap = mMap.getMap();
        UiSettings mUiSettings = myAmap.getUiSettings();
        mUiSettings.setScaleControlsEnabled(true);
        myAmap.setLocationSource(this);
        mUiSettings.setZoomControlsEnabled(false);
        myAmap.moveCamera(CameraUpdateFactory.zoomTo(18));
//        mUiSettings.setMyLocationButtonEnabled(true);
        myAmap.setMyLocationEnabled(true);
        // 初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        // 设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        // 初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        // 设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        // 启动定位
        mLocationClient.startLocation();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private SimpleDateFormat simpleDateFormat;
    private Date date_back;

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_camera) {
            startActivity(new Intent(MainActivity.this, WorkOrderDetailsPic.class));
        } else if (id == R.id.nav_slideshow) {
            runToActivity(new Intent(this, PicWorkOrderActivity.class));
        } else if (id == R.id.nav_share) {
            if (LockSetActivity.unlock_screw.equals(LockSetActivity.unlockType) || LockSetActivity.unlock_valve.equals(LockSetActivity.unlockType)) {
                //change unlockType
                LockSetActivity.unlockType = LockSetActivity.unlockUser;
            }
            //写入到文件中
            fileStream.fileStream(FileStream.unlockType, FileStream.write, LockSetActivity.unlockType.getBytes());
            if (CONNECTED.equals(connect_state)) {
                if (SLEEP.equals(state_sleep)) {
                    dialogWakeUp();
                } else {
                    if (user_Share.i_Auto == 1) {
                        //自动连接
                        editor.putString(user_Share.def_AutoConnect, "1");
                        editor.commit();
                    }

                    //连接后发送了更改开锁类型0x44

                    Intent intent = new Intent(getBaseContext(), DeviceService.class);
                    intent.setAction(SetActivity0.unlock_bluetooth);
                    //				Intent intent = new Intent(getApplicationContext(),LockerProcessAtivity.class);
                    startService(intent);
                }
            } else {
                if (user_Share.i_Auto == 1) {
                    //自动连接
                    editor.putString(user_Share.def_AutoConnect, "1");
                    editor.commit();
                }
//			intent.setClass(getBaseContext(),DeviceScanActivity.class);
                Intent intent = new Intent(getApplicationContext(), DeviceScanActivity0.class);
                startActivity(intent);
            }
        } else if (id == R.id.nav_send) {
            SharedPreferences myUserSharedPreferences;
            myUserSharedPreferences = this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
            myUserSharedPreferences.edit().clear().commit();
            fileStream.fileStream(FileStream.userLogin, FileStream.delete, null);
            ArrayList<User> mUsers = Utils.getUserList(MainActivity.this);
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            mUsers.clear();
            try {
                Utils.saveUserList(MainActivity.this, mUsers);
            } catch (Exception e) {
                e.printStackTrace();
            }
            finish();
        } else if (id == R.id.nav_scrap) {
            startActivity(new Intent(MainActivity.this, ScrapActivity.class));
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void runToActivity(final Intent intent) {
        if (intent == null)
            return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(600);
                    startActivity(intent);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 根据动画按钮状态，调用函数animateCamera或moveCamera来改变可视区域
     */
    private void changeCamera(CameraUpdate update, AMap.CancelableCallback callback) {
        boolean animated = true;
        if (animated) {
            myAmap.animateCamera(update, 1000, callback);
        } else {
            myAmap.moveCamera(update);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocationClient.startLocation();
    }


    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mMap.onResume();

    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mMap.onPause();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mMap.onDestroy();
    }

    //定位完成回调
    @Override
    public void onFinish() {
    }

    @Override
    public void onCancel() {
    }


    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mLocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mLocationClient.startLocation();
        }

    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null) {
            mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
        }
    }

    void dialogDownload(final NaviLatLng latLng, final String type) {
        //对话框   Builder是AlertDialog的静态内部类
        Dialog dialog = new AlertDialog.Builder(this)
                //设置对话框的标题
                .setTitle("提示！")
                //给对话框来个按钮 叫“确定” ，并且设置监听器 这种写法也真是有些BT
                .setPositiveButton("进行导航", new DialogInterface.OnClickListener() {
                    //点击 "确定"按钮之后要执行的操作就写在这里
                    public void onClick(DialogInterface dialog, int which) {
                        if (LatAndlon == null) {
                            Toast.makeText(MainActivity.this, "正在获取当前位置，请稍后....", Toast.LENGTH_LONG).show();
                        } else {
                            double[] Point = {LatAndlon.latitude, LatAndlon.longitude};
                            double[] end = {latLng.getLongitude(), latLng.getLatitude()};
                            Intent intent = new Intent(MainActivity.this, BasicNaviActivity.class);
                            intent.putExtra("Point", Point);
                            intent.putExtra("end", end);
                            startActivity(intent);

                        }
                    }
                })
                .create();//创建按钮
        dialog.show();
    }

    private Uri SMS_INBOX = Uri.parse("content://sms/");

//    public String getSmsFromPhone() {
//        final String SMS_URI_ALL = "content://sms/";
//        final String SMS_URI_INBOX = "content://sms/inbox";
//        final String SMS_URI_SEND = "content://sms/sent";
//        final String SMS_URI_DRAFT = "content://sms/draft";
//        StringBuilder smsBuilder = new StringBuilder();
//        try {
//            ContentResolver cr = getContentResolver();
//            String[] projection = new String[]{"_id", "address", "person",
//                    "body", "date", "type"};
//            Uri uri = Uri.parse(SMS_URI_INBOX);
//            Cursor cur = cr.query(uri, projection, "address=?", new String[]{"+8613161502939"}, "date desc");
//            if (cur.moveToFirst()) {
//                String name;
//                String phoneNumber;
//                String smsbody;
//                String date;
//                String type;
//                int nameColumn = cur.getColumnIndex("person");
//                int phoneNumberColumn = cur.getColumnIndex("address");
//                int smsbodyColumn = cur.getColumnIndex("body");
//                int dateColumn = cur.getColumnIndex("date");
//                int typeColumn = cur.getColumnIndex("type");
//                //ewf129916b
//                do {
//                    name = cur.getString(nameColumn);
//                    phoneNumber = cur.getString(phoneNumberColumn);
//                    smsbody = cur.getString(smsbodyColumn);
//
//                    SimpleDateFormat dateFormat = new SimpleDateFormat(
//                            "yyyy-MM-dd hh:mm:ss");
//                    Date d = new Date(Long.parseLong(cur.getString(dateColumn)));
//                    date = dateFormat.format(d);
//
//                    int typeId = cur.getInt(typeColumn);
//                    if (typeId == 1) {
//                        type = "接收";
//                    } else if (typeId == 2) {
//                        type = "发送";
//                    } else {
//                        type = "";
//                    }
//                    smsBuilder.append(name + ",");
//                    smsBuilder.append(phoneNumber + ",");
//                    smsBuilder.append(smsbody + ",");
//                    smsBuilder.append(date + ",");
//                    smsBuilder.append(type);
//                    smsBuilder.append(";");
//                    if (smsbody == null) smsbody = "";
//                } while (cur.moveToNext());
//            } else {
//                smsBuilder.append("");
//            }
//        } catch (SQLiteException ex) {
//            Log.d("SQLiteException in getSmsIn" +
//                    "" +
//                    "" +
//                    "" +
//                    "Phone", ex.getMessage());
//        }
//        return smsBuilder.toString();
//    }
}

