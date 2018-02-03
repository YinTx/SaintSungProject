package com.saintsung.saintpmc.workorder;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.qr_codescan.MipcaActivityCapture;
import com.google.gson.Gson;
import com.saintsung.saintpmc.MyApplication;
import com.saintsung.saintpmc.R;
import com.saintsung.saintpmc.adapter.ScrapAdapter;
import com.saintsung.saintpmc.asynctask.RetrofitRxAndroidHttp;
import com.saintsung.saintpmc.bean.ScrapBean;
import com.saintsung.saintpmc.bean.ScrapItemBean;
import com.saintsung.saintpmc.configuration.MD5;
import com.saintsung.saintpmc.configuration.configuration;
import com.saintsung.saintpmc.lock.LockerProcessAtivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.functions.Action1;

import static com.saintsung.saintpmc.tool.ToastUtil.isNetworkAvailable;


/**
 * Created by XLzY on 2018/1/10.
 */

public class ScrapActivity extends AppCompatActivity implements View.OnClickListener {
    private ListView mListView;
    private ScrapAdapter adapter;
    private Button butScrap;
    private Button submit;
    private List<ScrapItemBean> list;
    private final int SCANNIN_GREQUEST_CODE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrap);
        initView();
//        sendScrapService("3305230001150");
    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.mListView);
        butScrap = (Button) findViewById(R.id.scrap_sub);
        butScrap.setOnClickListener(this);
        submit = (Button) findViewById(R.id.scrap_sumbit);
        submit.setOnClickListener(this);
        list = new ArrayList<>();
        adapter = new ScrapAdapter(this, list);
        mListView.setAdapter(adapter);
    }

    private void sendScrapService(List<ScrapItemBean> list) {
        ScrapBean scrapBean = new ScrapBean();
        Gson gson = new Gson();
        scrapBean.setOptCode("ScrapLockInfos");
        scrapBean.setOptUserNumber(MyApplication.getUserId());
        scrapBean.setData(list);
        String sign = scrapBean.getOptCode() + scrapBean.getOptUserNumber() + gson.toJson(scrapBean.getData());
        sign = MD5.toMD5(sign);
        scrapBean.setSign(sign);
        String str = gson.toJson(scrapBean);
        if (isNetworkAvailable(this)) {
            RetrofitRxAndroidHttp retrofitRxAndroidHttp = new RetrofitRxAndroidHttp();
            retrofitRxAndroidHttp.serviceConnect(MyApplication.getUrl(), str, action1);
        } else {
            Toast.makeText(this, "请连接网络再试！", Toast.LENGTH_LONG).show();
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SCANNIN_GREQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    //A扫描后返回的数据
                    if (bundle != null) {
                        final String result = bundle.getString("result");
                        final ScrapItemBean scrapItem = new ScrapItemBean();
                        scrapItem.setEid("1");
                        AlertDialog.Builder dialog = new AlertDialog.Builder(ScrapActivity.this);
                        dialog.setTitle("请选择报废类型！");
                        dialog.setSingleChoiceItems(new String[]{"设备不能开关", "设备损坏", "其他"}, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                i++;
                                scrapItem.setEid(i + "");
                            }
                        });
                        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                scrapItem.setLno(result);
                                for (int j = 0; j < list.size(); j++) {
                                    if (list.get(j).getLno().equals(scrapItem.getLno())) {
                                        list.remove(j);
                                        list.add(j, scrapItem);
                                        break;
                                    }
                                    if (j == list.size() - 1)
                                        list.add(scrapItem);
                                }
                                if (list.size() == 0)
                                    list.add(scrapItem);
                                adapter.notifyDataSetChanged();
                            }
                        });
                        dialog.setNegativeButton("取消", null);
                        dialog.show();
                    }
                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.scrap_sub:
                Intent intent = new Intent();
                intent.setClass(ScrapActivity.this, MipcaActivityCapture.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
                break;
            case R.id.scrap_sumbit:
                if (list.size() > 0) {
                    sendScrapService(list);
                } else {
                    Toast.makeText(this, "请添加报废设备再提交！", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    private void dataProcessing(String data) {
        Log.e("TAG", "=================" + data);
        Gson gson = new Gson();
        try {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("提示！");
            ScrapBean queryBureauNumber = gson.fromJson(data, ScrapBean.class);
            String result = queryBureauNumber.getResult();
            if (result.equals("0000")) {
                list.clear();
                adapter.notifyDataSetChanged();
                dialog.setMessage(queryBureauNumber.getResultMessage());
                dialog.show();
            } else if (result.equals("E001")) {
                dialog.setMessage(queryBureauNumber.getResultMessage());
                dialog.show();
            } else if (result.equals("E002")) {
                dialog.setMessage(queryBureauNumber.getResultMessage());
                dialog.show();
            } else if (result.equals("E003")) {
                dialog.setMessage(queryBureauNumber.getResultMessage());
                dialog.show();
            } else if (result.equals("E004")) {
                dialog.setMessage(queryBureauNumber.getResultMessage());
                dialog.show();
            } else {
                dialog.setMessage(queryBureauNumber.getResultMessage());
                dialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", "错误=" + e);
        }
    }
}
