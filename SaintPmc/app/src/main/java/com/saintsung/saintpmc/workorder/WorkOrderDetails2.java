package com.saintsung.saintpmc.workorder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.saintsung.saintpmc.R;
import com.saintsung.saintpmc.configuration.BaseActivity;
import com.saintsung.saintpmc.orderdatabase.WorkOrderBean;
import com.saintsung.saintpmc.orderdatabase.WorkOrderBean$Table;


import java.util.ArrayList;
import java.util.List;

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
        myBluck= (ImageButton) findViewById(R.id.myBluck);
        myListView= (ListView) findViewById(R.id.workorderdetails);
        myBluck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        final List<WorkOrderBean> peoples = new Select().from(WorkOrderBean.class).where(Condition.column(WorkOrderBean$Table.WORKORDERSTATE).is("1")).queryList();
            for(int i=0;i<peoples.size();i++){
                String orderNo=peoples.get(i).workOrderNumber;
                listAdapter.add(orderNo);
        }
        mDetailsAdapter adapter=new mDetailsAdapter(this,listAdapter);
        myListView.setAdapter(adapter);
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(WorkOrderDetails2.this,WorkOrderDetailsItem.class);
                intent.putExtra("workOrder",peoples.get(position).workOrderNumber);
                startActivity(intent);
            }
        });
    }
}
