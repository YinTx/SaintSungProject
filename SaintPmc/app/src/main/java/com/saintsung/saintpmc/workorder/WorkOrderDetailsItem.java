package com.saintsung.saintpmc.workorder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.saintsung.saintpmc.R;
import com.saintsung.saintpmc.configuration.BaseActivity;
import com.saintsung.saintpmc.location.BasicNaviActivity;
import com.saintsung.saintpmc.location.CheckPermissionsActivity;
import com.saintsung.saintpmc.orderdatabase.LockInformation;
import com.saintsung.saintpmc.orderdatabase.LockInformation$Table;
import com.saintsung.saintpmc.orderdatabase.WorkOrderDetailsBean;
import com.saintsung.saintpmc.orderdatabase.WorkOrderDetailsBean$Table;


import java.util.List;

import static com.saintsung.saintpmc.MainActivity.LatAndlon;

/**
 * Created by YinTxLz on 2017/5/24.
 */

public class WorkOrderDetailsItem extends CheckPermissionsActivity {
    private ImageButton myBluck;
    private ListView myList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workorderdetails);
        myList = (ListView) findViewById(R.id.workorderdetails);
        String str = getIntent().getStringExtra("workOrder");
        final List<LockInformation> peoples = new Select().from(LockInformation.class).where(Condition.column(LockInformation$Table.WORKORDERNUMBER).is(str)).queryList();
        mDetailsAdapterItem2 adapter = new mDetailsAdapterItem2(this, peoples);
        myList.setAdapter(adapter);
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                double[] Point = {LatAndlon.latitude, LatAndlon.longitude};
                String latitude = peoples.get(position).pointX;
                String longitude = peoples.get(position).pointY;
                if (latitude.equals("0.000000000000") || longitude.equals("0.000000000000")) {
                    Toast.makeText(WorkOrderDetailsItem.this, "该设备没有坐标信息", Toast.LENGTH_SHORT).show();
                } else {
                    double[] end = {Double.parseDouble(longitude), Double.parseDouble(latitude)};
//                    double[] end={31.236326260971527,121.48226139034274};
                    Intent intent = new Intent(WorkOrderDetailsItem.this, BasicNaviActivity.class);
                    intent.putExtra("Point", Point);
                    intent.putExtra("end", end);
                    startActivity(intent);
                }
            }
        });
    }

}
