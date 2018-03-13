package com.saintsung.saintpmc.workorder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.saintsung.saintpmc.MainActivity;
import com.saintsung.saintpmc.MyApplication;
import com.saintsung.saintpmc.R;
import com.saintsung.saintpmc.asynctask.RetrofitRxAndroidHttp;
import com.saintsung.saintpmc.bean.WorkOrderDataItemBean;
import com.saintsung.saintpmc.bean.WorkOrderDealitBean;
import com.saintsung.saintpmc.configuration.BaseActivity;
import com.saintsung.saintpmc.lock.FileStream;
import com.saintsung.saintpmc.lock.LockerProcessAtivity;
import com.saintsung.saintpmc.lock.MD5;
import com.saintsung.saintpmc.orderdatabase.LockInformation;
import com.saintsung.saintpmc.orderdatabase.WorkOrderBean;
import com.saintsung.saintpmc.orderdatabase.WorkOrderBean$Table;
import com.saintsung.saintpmc.orderdatabase.WorkOrderControData;
import com.saintsung.saintpmc.orderdatabase.WorkOrderControData$Table;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import rx.functions.Action1;

/**
 * Created by YinTxLz on 2017/5/24.
 */

public class WorkOrderDetails2 extends BaseActivity {
    private ImageButton myBluck;
    private List<String> listAdapter=new ArrayList<>();
    SharedPreferences mySharedPreferences;
    private ListView myListView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workorderdetails);
        myListView= (ListView) findViewById(R.id.workorderdetails);
        final List<WorkOrderControData> peoples = new Select().from(WorkOrderControData.class).queryList();
            for(int i=0;i<peoples.size();i++){
                String orderNo=peoples.get(i).workOrderNumber;
                listAdapter.add(orderNo);
        }
        mDetailsAdapter adapter=new mDetailsAdapter(this,listAdapter);
        myListView.setAdapter(adapter);
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RetrofitRxAndroidHttp retrofitRxAndroidHttp = new RetrofitRxAndroidHttp();
                retrofitRxAndroidHttp.serviceConnect(MyApplication.getUrl(), getWorkOrderDealitStr(peoples.get(position).workOrderNumber), action3);
            }
        });
    }
    private String getWorkOrderDealitStr(String number) {
        Gson gson = new Gson();
        String sign = "";
        WorkOrderDealitBean workOrderDealitBean = new WorkOrderDealitBean();
        workOrderDealitBean.setOptCode("GetWorkOrderDetailInfos");
        workOrderDealitBean.setWorkOrderNo(number);
        workOrderDealitBean.setNums("0");
        workOrderDealitBean.setLockNum("2000");
        workOrderDealitBean.setCntNum("1");
        workOrderDealitBean.setSign(MD5.toMD5(workOrderDealitBean.getOptCode() + workOrderDealitBean.getWorkOrderNo() + workOrderDealitBean.getNums() + workOrderDealitBean.getCntNum() + workOrderDealitBean.getLockNum() + workOrderDealitBean.getData()));
        return gson.toJson(workOrderDealitBean);
    }
    private Action1<ResponseBody> action3 = new Action1<ResponseBody>() {

        @Override
        public void call(ResponseBody responseBody) {
            try {
                dataProces(responseBody.string());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    private void dataProces(String string) {
        Log.e("TAG","sss:"+string);
        Gson gson = new Gson();
        WorkOrderDealitBean workOrderDealitBean = gson.fromJson(string, WorkOrderDealitBean.class);
        if (workOrderDealitBean.getResult().equals("0000")) {
            WorkOrderControData workOrderControData1 = new Select().from(WorkOrderControData.class).where(Condition.column(WorkOrderControData$Table.WORKORDERNUMBER).is(workOrderDealitBean.getWorkOrderNo())).querySingle();
            List<WorkOrderDataItemBean> workOrderDataItemBeanList = workOrderDealitBean.getData();
            String res = "";
            for (int j = 0; j < workOrderDealitBean.getData().size(); j++) {
                LockInformation lockInformation = new LockInformation();
                lockInformation.lockNo = workOrderDataItemBeanList.get(j).getLockNo();
                lockInformation.assetno = workOrderDataItemBeanList.get(j).getAssetNo();
                lockInformation.optPwd = workOrderDataItemBeanList.get(j).getOptPwd();
                lockInformation.pointX = workOrderDataItemBeanList.get(j).getPointX();
                lockInformation.pointY = workOrderDataItemBeanList.get(j).getPointY();
                lockInformation.type = workOrderDataItemBeanList.get(j).getType();
                lockInformation.starTime = workOrderControData1.startTime;
                lockInformation.endTime = workOrderControData1.endTime;
                lockInformation.insert();
            }
            Intent intent=new Intent(WorkOrderDetails2.this,WorkOrderDetailsItem.class);
            intent.putExtra("workOrder",workOrderControData1.workOrderNumber);
            startActivity(intent);
        }
    }
}
