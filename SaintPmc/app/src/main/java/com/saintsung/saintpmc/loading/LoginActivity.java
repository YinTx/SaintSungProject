package com.saintsung.saintpmc.loading;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.saintsung.saintpmc.MainActivity;
import com.saintsung.saintpmc.MyApplication;
import com.saintsung.saintpmc.R;
import com.saintsung.saintpmc.asynctask.RetrofitRxAndroidHttp;
import com.saintsung.saintpmc.asynctask.SocketConnect;
import com.saintsung.saintpmc.bean.LoginBean;
import com.saintsung.saintpmc.bean.LoginItemBean;
import com.saintsung.saintpmc.configuration.configuration;
import com.saintsung.saintpmc.location.CheckPermissionsActivity;
import com.saintsung.saintpmc.lock.CommonResources;
import com.saintsung.saintpmc.lock.FileStream;
import com.saintsung.saintpmc.lock.LockerProcessAtivity;
import com.saintsung.saintpmc.lock.MD5;
import com.saintsung.saintpmc.tool.DiaLog;
import com.saintsung.saintpmc.tool.SharedPreferencesUtil;
import com.saintsung.saintpmc.tool.ToastUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.functions.Action1;

import static com.saintsung.saintpmc.lock.SocketActivity.string;
import static com.saintsung.saintpmc.tool.ToastUtil.isNetworkAvailable;

public class LoginActivity extends CheckPermissionsActivity implements OnClickListener,
        OnItemClickListener, OnDismissListener {
    protected static final String TAG = "LoginActivity";
    private LinearLayout mLoginLinearLayout;
    private LinearLayout mUserIdLinearLayout;
    private Animation mTranslate;
    private Dialog mLoginingDlg;
    private ImageView mMoreUser;
    private Button mLoginButton;
    private ImageView mLoginMoreUserView;
    private String nameValue;
    private String passValue;
    private ArrayList<User> mUsers;
    private ListView mUserIdListView;
    private MyAapter mAdapter;
    private PopupWindow mPop;

    private EditText userName, passWord;

    private String parameter;
    private String addressIP;
    private String port;
    //static的作用
    private String passwordValue, string, requestPacket, loginOne, progress;
    public static String responsePacket;
    public static final int requestUsernameOrPasswordLength = 10;
    public static String userId, userNameValue;
    private CheckBox automaticLogon, rememberPwd;//记住密码和自动登录
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 10;
    private TextView portLog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ToastUtil.setTranslucent(this);
        setListener();
        initView();
    }


    class MyAapter extends ArrayAdapter<User> {

        public MyAapter(ArrayList<User> users) {
            super(LoginActivity.this, 0, users);
        }

        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(
                        R.layout.listview_item, null);
            }

            TextView userIdText = (TextView) convertView
                    .findViewById(R.id.listview_userid);
            userIdText.setText(getItem(position).getId());

            ImageView deleteUser = (ImageView) convertView
                    .findViewById(R.id.login_delete_user);
            deleteUser.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (getItem(position).getId().equals(nameValue)) {
                        nameValue = "";
                        passValue = "";
                        userName.setText(nameValue);
                        passWord.setText(passValue);
                    }
                    mUsers.remove(getItem(position));
                    mAdapter.notifyDataSetChanged();
                }
            });
            return convertView;
        }

    }

    private void setListener() {
        userName = (EditText) findViewById(R.id.login_edtId);
        passWord = (EditText) findViewById(R.id.login_edtPwd);
        mMoreUser = (ImageView) findViewById(R.id.login_more_user);
        mLoginButton = (Button) findViewById(R.id.login_btnLogin);
        mLoginMoreUserView = (ImageView) findViewById(R.id.login_more_user);
        mLoginLinearLayout = (LinearLayout) findViewById(R.id.login_linearLayout);
        mUserIdLinearLayout = (LinearLayout) findViewById(R.id.userId_LinearLayout);
        mTranslate = AnimationUtils.loadAnimation(this, R.anim.my_translate);
        portLog = (TextView) findViewById(R.id.port_log);
        portLog.setOnClickListener(this);
        userName.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                nameValue = s.toString();
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        passWord.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                passValue = s.toString();
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        mLoginButton.setOnClickListener(this);
        mLoginMoreUserView.setOnClickListener(this);
        mLoginLinearLayout.startAnimation(mTranslate);
        mUsers = Utils.getUserList(LoginActivity.this);
        if (mUsers.size() > 0) {
            userName.setText(mUsers.get(0).getId());
            passWord.setText(mUsers.get(0).getPwd());
        }
        LinearLayout parent = (LinearLayout) getLayoutInflater().inflate(
                R.layout.userifo_listview, null);
        mUserIdListView = (ListView) parent.findViewById(android.R.id.list);
        parent.removeView(mUserIdListView);
        mUserIdListView.setOnItemClickListener(this);
        mAdapter = new MyAapter(mUsers);
        mUserIdListView.setAdapter(mAdapter);
    }

    private String isLogin;
    private String isPassword;

    private void initView() {
        isLogin = SharedPreferencesUtil.getSharedPreferences(this, "isLogin", "0");
        isPassword = SharedPreferencesUtil.getSharedPreferences(this, "isPassword", "0");
        automaticLogon = (CheckBox) findViewById(R.id.automaticLogon);
        automaticLogon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    rememberPwd.setChecked(true);
                }
            }
        });
        rememberPwd = (CheckBox) findViewById(R.id.rememberPwd);
        initLoginingDlg();
        if (isLogin.equals("1")) {
            automaticLogon.setChecked(true);
            isLogin();
        } else {

        }
        if (isPassword.equals("1")) {
            rememberPwd.setChecked(true);
        } else {
            rememberPwd.setChecked(false);
        }
    }

    public void initPop() {
        int width = mUserIdLinearLayout.getWidth() - 4;
        int height = LayoutParams.WRAP_CONTENT;
        mPop = new PopupWindow(mUserIdListView, width, height, true);
        mPop.setOnDismissListener(this);
        mPop.setBackgroundDrawable(new ColorDrawable(0xffffffff));

    }

    private void initLoginingDlg() {
        mLoginingDlg = new Dialog(this, R.style.loginingDlg);
        mLoginingDlg.setContentView(R.layout.logining_dlg);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btnLogin:
                isLogin();
                break;
            case R.id.login_more_user:
                if (mPop == null) {
                    initPop();
                }
                if (!mPop.isShowing() && mUsers.size() > 0) {
                    mMoreUser.setImageResource(R.drawable.login_more_down);
                    mPop.showAsDropDown(mUserIdLinearLayout, 2, 1);
                }
                break;
            case R.id.port_log:
                DiaLog diaLog = new DiaLog(LoginActivity.this);
                diaLog.setMsg("请输入端口号！");
                diaLog.show();
                setWidthAndH(diaLog);
                break;
            default:
                break;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        userName.setText(mUsers.get(position).getId());
        passWord.setText(mUsers.get(position).getPwd());
        mPop.dismiss();
    }

    @Override
    public void onDismiss() {
        mMoreUser.setImageResource(R.drawable.login_more_up);
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            Utils.saveUserList(LoginActivity.this, mUsers);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断登录
     */
    private void isLogin() {
        if (!isNetworkAvailable(this)) {
            Toast.makeText(this,"请检查网络后再试！",Toast.LENGTH_LONG).show();
        } else {
            if (userName.getText().toString().equals("") || passWord.getText().toString().equals("")) {
                Toast.makeText(this, "请输入用户名或密码！", Toast.LENGTH_LONG).show();
            } else {
                if (isPort()) {
                    mLoginingDlg.show();
                    String username = userName.getText().toString();
                    String password = passWord.getText().toString();
                    RetrofitRxAndroidHttp retrofitRxAndroidHttp = new RetrofitRxAndroidHttp();
                    MyApplication.setUrl(configuration.addressHttp + SharedPreferencesUtil.getSharedPreferences(this, "port", "") + configuration.serviceTail);
                    retrofitRxAndroidHttp.serviceConnect(MyApplication.getUrl(), getResult(username, password), action1);
                }

            }
        }
    }
    private String getResult(String username, String password) {
        Gson gson = new Gson();
        LoginBean loginBean = new LoginBean();
        LoginItemBean loginItemBean = new LoginItemBean();
        String sign = "";
        loginItemBean.setUserNo(username);
        loginItemBean.setUserPwd(MD5.toMD5(password));
        loginBean.setOptCode("OptUserLogin");
        loginBean.setData(loginItemBean);
        sign = MD5.toMD5(loginBean.getOptCode() + gson.toJson(loginBean.getData()));
        loginBean.setSign(sign);
        return gson.toJson(loginBean);
    }
    FileStream fileStream = new FileStream();
    /**
     * 保存用户名和密码
     */
    private void SavePassword() {
        SharedPreferencesUtil.putSharedPreferences(this, "isPassword", "1");
        boolean mIsSave = true;
        try {
            for (User user : mUsers) {
                if (user.getId().equals(nameValue)) {
                    mIsSave = false;
                    break;
                }
            }
            if (mIsSave) {
                User user = new User(nameValue, passValue);
                mUsers.add(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断是否有IP地址及端口号
     */
    private boolean isPort() {
        port = SharedPreferencesUtil.getSharedPreferences(this, "port", "");
        if (port.equals("")) {
            DiaLog diaLog = new DiaLog(LoginActivity.this);
            diaLog.setMsg("检测到您的通讯端口为空,请输入端口号！");
            diaLog.show();
            setWidthAndH(diaLog);
            return false;
        } else return true;
    }

    private void setWidthAndH(DiaLog diaLog) {
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = diaLog.getWindow().getAttributes();
        lp.width = (int) (display.getWidth()); //设置宽度
        diaLog.getWindow().setAttributes(lp);
    }

    private Action1<ResponseBody> action1 = new Action1<ResponseBody>() {

        @Override
        public void call(ResponseBody responseBody) {
            try {
                dataProcess(responseBody.string());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    };

    private void dataProcess(String result) {
        Log.e("TAG", "======" + result);
        Gson gson = new Gson();
        try {
            LoginBean loginBean = gson.fromJson(result, LoginBean.class);
            if (loginBean.getResult().equals("0000")) {
                if (automaticLogon.isChecked()) {
                    SharedPreferencesUtil.putSharedPreferences(this, "isLogin", "1");
                } else {
                    SharedPreferencesUtil.putSharedPreferences(this, "isLogin", "0");
                }
                if (rememberPwd.isChecked()) {
                    SavePassword();
                } else {
                    SharedPreferencesUtil.putSharedPreferences(this, "isPassword", "0");
                }
                MyApplication.setUserId(loginBean.getOptUserNumber());
                string = "111,111," + loginBean.getOptUserNumber() + "";
                fileStream.fileStream(FileStream.userLogin, FileStream.write, string.getBytes());
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                mLoginingDlg.dismiss();
                this.finish();
            } else {
                Toast.makeText(this, loginBean.getResultMessage(), Toast.LENGTH_LONG).show();
                mLoginingDlg.dismiss();
            }
        } catch (Exception e) {
            System.out.print(e.getStackTrace());
        }


    }
}
