package com.saintsung.saintpmc;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.saintsung.saintpmc.configuration.BaseActivity;
import com.saintsung.saintpmc.lock.FileStream;
import com.saintsung.saintpmc.orderdatabase.WorkOrderDetailsBean;
import com.saintsung.saintpmc.orderdatabase.WorkOrderDetailsBean$Table;


import java.util.List;

/**
 * Created by YinTxLz on 2017/6/14.
 */

public class newTextListView extends BaseActivity {
    private ListView mListView;
    List<WorkOrderDetailsBean> workOrderDetailsBeen;
    mListAdapter2 mListAdapter2;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adapter_time2);
        mListView= (ListView) findViewById(R.id.mListView);
        workOrderDetailsBeen = new Select().from(WorkOrderDetailsBean.class).queryList();
        mListAdapter2=new mListAdapter2(this,workOrderDetailsBeen);
        mListView.setAdapter(mListAdapter2);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showCustomizeDialog(position);
            }
        });
    }
    private void showCustomizeDialog(final int position) {
    /* @setView 装入自定义View ==> R.layout.dialog_customize
     * 由于dialog_customize.xml只放置了一个EditView，因此和图8一样
     * dialog_customize.xml可自定义更复杂的View
     */
        AlertDialog.Builder customizeDialog =
                new AlertDialog.Builder(newTextListView.this);
        final View dialogView = LayoutInflater.from(newTextListView.this)
                .inflate(R.layout.tuyjg,null);
        customizeDialog.setTitle("修改开锁码！");
        customizeDialog.setView(dialogView);
        customizeDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 获取EditView中的输入内容
                        EditText edit_text = (EditText) dialogView.findViewById(R.id.edit_text);

                        List<WorkOrderDetailsBean> lstElecUserBeen = new Select().from(WorkOrderDetailsBean.class).where(Condition.column(WorkOrderDetailsBean$Table.ID).is(workOrderDetailsBeen.get(position).id)).queryList();
                            if(lstElecUserBeen.size()==0){}else {
                                lstElecUserBeen.get(0).openLookNumber=edit_text.getText().toString();
                                lstElecUserBeen.get(0).update();
                            }
                        mListAdapter2.notifyDataSetChanged();
                        getUserDownFor();
                    }
                });
        customizeDialog.show();
    }
    FileStream fileStream=new FileStream();
    private void getUserDownFor(){
        fileStream.fileStream(FileStream.userDownload,FileStream.delete,null);
        List<WorkOrderDetailsBean> people = new Select().from(WorkOrderDetailsBean.class).queryList();
        String str="";
        for(int i=0;i<people.size();i++){
            str=str+people.get(i).lookNumber+":"+people.get(i).openLookNumber+":"+people.get(i).deviceType+"\r\n";
        }
        fileStream.fileStream(FileStream.userDownload,FileStream.write,str.getBytes());
    }
}
