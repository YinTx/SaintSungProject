package com.saintsung.saintpmc.workorder;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import com.saintsung.saintpmc.R;
import com.saintsung.saintpmc.configuration.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YinTxLz on 2017/5/24.
 */

public class WorkOrderDetailsItem2 extends BaseActivity {
    private ImageButton myBluck;
    private List<String> list=new ArrayList<>();
    private ListView myList;
    SharedPreferences mySharedPreferences;
     @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workorderdetails);
        myBluck= (ImageButton) findViewById(R.id.myBluck);
        myBluck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

//         if(!strSf.equals("")){
//             String []byteStrSf=strSf.split("\r\n");
//             for(int i=0;i<byteStrSf.length;i++){
//                 list.add(byteStrSf[i]);
//             }
//         }
//         mDetailsAdapterItem2 adapter=new mDetailsAdapterItem2(this,list);
//         myList.setAdapter(adapter);
    }
}
