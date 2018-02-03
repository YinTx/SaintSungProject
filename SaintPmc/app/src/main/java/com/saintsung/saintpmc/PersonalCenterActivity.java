package com.saintsung.saintpmc;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.raizlabs.android.dbflow.sql.language.Select;
import com.saintsung.saintpmc.configuration.BaseActivity;
import com.saintsung.saintpmc.orderdatabase.LockInformation;
import com.saintsung.saintpmc.workorder.ScrapActivity;
import com.saintsung.saintpmc.workorder.WorkOrderDetails;
import com.saintsung.saintpmc.workorder.WorkOrderDetails2;

import java.util.List;


public class PersonalCenterActivity extends BaseActivity {
    private LinearLayout myOrderNo;
    private ImageButton myBluck;
    private LinearLayout completedWorkOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_center);
        initView();

    }

    private void initView() {
        List<LockInformation> lockInformation=new Select().from(LockInformation.class).queryList();
        myOrderNo = (LinearLayout) findViewById(R.id.completionWorkOrder);
        myOrderNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PersonalCenterActivity.this, WorkOrderDetails.class));
            }
        });
        completedWorkOrder = (LinearLayout) findViewById(R.id.completedWorkOrder);
        completedWorkOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PersonalCenterActivity.this, WorkOrderDetails2.class));
            }
        });
        myBluck = (ImageButton) findViewById(R.id.myBluck);
        myBluck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
//        Scrap = (LinearLayout) findViewById(R.id.scrap);
//        Scrap.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
    }






}
