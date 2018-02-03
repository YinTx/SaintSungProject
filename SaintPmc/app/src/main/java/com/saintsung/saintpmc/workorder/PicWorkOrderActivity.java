package com.saintsung.saintpmc.workorder;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import com.example.qr_codescan.MipcaActivityCapture;

import com.google.gson.Gson;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.saintsung.saintpmc.MyApplication;
import com.saintsung.saintpmc.R;
import com.saintsung.saintpmc.adapter.SiteDataAdapter;
import com.saintsung.saintpmc.asynctask.BaseResponse;
import com.saintsung.saintpmc.asynctask.ModelFilteredFactory;
import com.saintsung.saintpmc.asynctask.RetrofitRxAndroidHttp;
import com.saintsung.saintpmc.bean.QueryBureauNumberBean;
import com.saintsung.saintpmc.bean.QueryBureauNumberBean2;
import com.saintsung.saintpmc.configuration.configuration;
import com.saintsung.saintpmc.lock.MD5;
import com.saintsung.saintpmc.lock.NetworkConnect;
import com.saintsung.saintpmc.myinterface.BlogService;
import com.saintsung.saintpmc.orderdatabase.DicLockSiteBean;
import com.saintsung.saintpmc.orderdatabase.DoorAndMeterDataBase;
import com.saintsung.saintpmc.tool.DataProcess;
import com.saintsung.saintpmc.tool.SharedPreferencesUtil;
import com.saintsung.saintpmc.tool.ToastUtil;
import com.saintsung.saintpmc.view.AddView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.functions.Action1;

import static com.saintsung.saintpmc.MainActivity.LatAndlon;

/**
 * Created by XLzY on 2017/7/25.
 */

public class PicWorkOrderActivity extends AppCompatActivity implements View.OnClickListener, SiteDataAdapter.MyClickListener {
    private final int SCANNIN_GREQUEST_CODE = 1;//局号
    private final int SCANNIN_GREQUEST_CODE1 = 3;
    private static final int TAKE_PHOTO_REQUEST_CODE = 2;//权限返回code
    private AutoCompleteTextView editBureauNo;//局号
    private ImageButton scanBureauNo;//扫描局号
    private Button submit;//提交按钮
    private TextView resultMessage;//局号提示Message
    private ListView siteTypeList;
    private List<DicLockSiteBean> dicLockSiteBean;
    private View saveResult;
    private SiteDataAdapter siteDataAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.installation_order);
        takePhoto(this);
        initView();
    }

    private void initView() {
//        ModelFilteredFactory.compose(new Observable<BaseResponse<ResponseBody>> n);
        siteTypeList = (ListView) findViewById(R.id.siteTypeList);
        TextView latitude = (TextView) findViewById(R.id.latitude);
        TextView longitude = (TextView) findViewById(R.id.longitude);
        if (LatAndlon == null) {
        } else {
            latitude.setText("纬度：" + LatAndlon.latitude + "");
            longitude.setText("经度：" + LatAndlon.longitude + "");
        }
        editBureauNo = (AutoCompleteTextView) findViewById(R.id.bureauNo);
        scanBureauNo = (ImageButton) findViewById(R.id.scanBureauNo);
        resultMessage = (TextView) findViewById(R.id.resultMessage);
        submit = (Button) findViewById(R.id.sub_userDevice);
        scanBureauNo.setOnClickListener(this);
        submit.setOnClickListener(this);
        submit.setEnabled(false);
        editBureauNo.addTextChangedListener(textWather);
        dicLockSiteBean = new Select().from(DicLockSiteBean.class).queryList();
        siteDataAdapter = new SiteDataAdapter(this, dicLockSiteBean, this);
        siteTypeList.setAdapter(siteDataAdapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sub_userDevice:
                Map map = new HashMap();
                for (int i = 0; i < siteDataAdapter.saveView.size(); i++) {
                    View v = siteDataAdapter.saveView.get(i);
                    EditText te = (EditText) v.findViewById(R.id.number);
                    String textStr = te.getText().toString();
                    if (!textStr.equals("")) {
                        SiteDataAdapter.ViewHolder tag = (SiteDataAdapter.ViewHolder) siteDataAdapter.saveView.get(i).getTag();
                        map.put(tag.hs, textStr);
                    }
                }
                checkBureauNumber(editBureauNo.getText().toString(),map);
                editBureauNo.setText("");
                siteDataAdapter.cloneTextStr();
                siteDataAdapter.setViewEnabled(false);
                submit.setEnabled(false);
                break;
            case R.id.scanBureauNo:
                starInent(SCANNIN_GREQUEST_CODE);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SCANNIN_GREQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null){
                        String result = bundle.getString("result");
                        editBureauNo.setText(result);}
                }
                break;
            case SCANNIN_GREQUEST_CODE1:
                if(resultCode==RESULT_OK){
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    String result = bundle.getString("result");
                    LinearLayout linearLayout = (LinearLayout) saveResult.getParent();
                    EditText editText = (EditText) linearLayout.findViewById(R.id.number);
                    editText.setText(result);
                }
                }
                break;
            default:
                break;
        }
    }

    public static void takePhoto(Context context) {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.CAMERA},
                    TAKE_PHOTO_REQUEST_CODE);
        }

    }
    private void checkBureauNumber(final String result, Map map) {
        Gson gson = new Gson();
        List<Map> list = new ArrayList<>();
        QueryBureauNumberBean2 queryBureauNumber = new QueryBureauNumberBean2();
        queryBureauNumber.setOptCode("ElecUserLockInfo");
        queryBureauNumber.setUserNumber(result);
        queryBureauNumber.setOptUserNumber(MyApplication.getUserId());
        if (map != null)
            if (map.size() > 0)
                list.add(map);
        queryBureauNumber.setData(list);
        String sign = queryBureauNumber.getOptCode() + queryBureauNumber.getOptUserNumber() + queryBureauNumber.getUserNumber() + gson.toJson(queryBureauNumber.getData());
        sign = MD5.toMD5(sign);
        queryBureauNumber.setSign(sign);
        final String str = gson.toJson(queryBureauNumber);
        if (NetworkConnect.isNetworkAvailable(this)) {
            RetrofitRxAndroidHttp retrofitRxAndroidHttp = new RetrofitRxAndroidHttp();
            retrofitRxAndroidHttp.serviceConnect(MyApplication.getUrl(), str, action1);
        } else {
            if (map == null)
                return;
            if(map.size()<=0)
                return;
            DoorAndMeterDataBase doorAndMeterDataBase = new DoorAndMeterDataBase();
            doorAndMeterDataBase.jsonStrInService = str;
            doorAndMeterDataBase.strType = 0;
            doorAndMeterDataBase.editBureauNo=editBureauNo.getText().toString();
            doorAndMeterDataBase.insert();
            editBureauNo.setText("");
            siteDataAdapter.cloneTextStr();
            ToastUtil.showDialog("您的网络存在问题，我已为您保存在手机上，下次启动程序将为您上传数据！", this);
        }
    }

    //rtnErrcd=0000  表示操作成功
    //rtnErrcd=E001  表示对应的用电户信息不存在
    //rtnErrcd=E002  表示数据库异常，
    //rtnErrcd=E003  表示提供的用电户关联数据格式有误，
    //rtnErrcd=E004  表示签名sign错误，
    private void dataProcessing(String data) {
        Log.e("TAG", "============" + data);
        Gson gson = new Gson();
        try {
            QueryBureauNumberBean queryBureauNumber = gson.fromJson(data, QueryBureauNumberBean.class);
            String result = queryBureauNumber.getResult();
            if (result.equals("0000")) {
                resultMessage.setText(queryBureauNumber.getResultMessage());
                List<Map> listMap = queryBureauNumber.getData();
                for (int i = 0; i < listMap.size(); i++) {
                    Map map = (Map) listMap.get(i);
                    Set set = map.keySet();
                    String s = set.toString().replace("]", "").replace("[", "");
                    for (int j = 0; j < siteDataAdapter.saveView.size(); j++) {
                        SiteDataAdapter.ViewHolder tag = (SiteDataAdapter.ViewHolder) siteDataAdapter.saveView.get(j).getTag();
                        if (tag.hs.equals(s)) {
                            Log.e("TAG", map.get(s) + "");
                            tag.numberText.setText((String) map.get(s));
                        }
                    }
                }


            } else if (result.equals("E001")) {
                resultMessage.setText(queryBureauNumber.getResultMessage());
            } else if (result.equals("E002")) {
                resultMessage.setText(queryBureauNumber.getResultMessage());
            } else if (result.equals("E003")) {
                resultMessage.setText(queryBureauNumber.getResultMessage());
            } else if (result.equals("E004")) {
                resultMessage.setText(queryBureauNumber.getResultMessage());
            } else {
                resultMessage.setText(queryBureauNumber.getResultMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", "错误=" + e);
        }


    }

    private void starInent(int code) {
        Intent intent = new Intent();
        intent.setClass(PicWorkOrderActivity.this, MipcaActivityCapture.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, code);
    }

    private TextWatcher textWather = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            siteDataAdapter.cloneTextStr();
            submit.setEnabled(false);
            resultMessage.setText("");
            if (editable.length() == 22) {
                submit.setEnabled(true);
                siteDataAdapter.setViewEnabled(true);
                checkBureauNumber(editable.toString(),null);
            }
        }
    };
    private Action1<ResponseBody> action1=new Action1<ResponseBody>() {

        @Override
        public void call(ResponseBody responseBody) {
            try {
                dataProcessing(responseBody.string());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void clickListener(View v) {
        saveResult = v;
        starInent(SCANNIN_GREQUEST_CODE1);
    }
}