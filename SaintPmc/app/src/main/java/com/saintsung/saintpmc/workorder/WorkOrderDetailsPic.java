package com.saintsung.saintpmc.workorder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import com.raizlabs.android.dbflow.sql.language.Select;
import com.saintsung.saintpmc.MyApplication;
import com.saintsung.saintpmc.R;
import com.saintsung.saintpmc.configuration.BaseActivity;
import com.saintsung.saintpmc.location.mPcRecordAdapter;
import com.saintsung.saintpmc.lock.FileStream;
import com.saintsung.saintpmc.orderdatabase.WorkOrderBean;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by YinTxLz on 2017/5/23.
 * 未完成工单页面
 */

public class WorkOrderDetailsPic extends AppCompatActivity{
    private ListView myListView;
    FileStream fileStream=new FileStream();
    private com.saintsung.saintpmc.bean.WorkOrderBean listAdapter;
    private ImageButton myBluck;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workorderdetails);
        myListView= (ListView) findViewById(R.id.workorderdetails);
       listAdapter= MyApplication.getWorkOrderBean();
        mPcRecordAdapter adapter=new mPcRecordAdapter(this,listAdapter);
        myListView.setAdapter(adapter);
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(WorkOrderDetailsPic.this,PicUpServiceActivity.class);
                intent.putExtra("workOrder",listAdapter.getData().get(position).getWorkOrderNo());
                startActivity(intent);
            }
        });
    }


}
