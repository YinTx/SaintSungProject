package com.saintsung.saintpmc.location;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.saintsung.saintpmc.R;
import com.saintsung.saintpmc.bean.WorkOrderBean;
import com.saintsung.saintpmc.orderdatabase.WorkOrderControData;
import com.saintsung.saintpmc.orderdatabase.WorkOrderControData$Table;

import java.util.ArrayList;
import java.util.List;


public class mPcRecordAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    public WorkOrderBean list;
    public List<View> saveView = new ArrayList<>();
    private Context context;
    public mPcRecordAdapter(Context context, WorkOrderBean list) {
        this.mInflater = LayoutInflater.from(context);
        this.context=context;
        this.list = list;
    }

    public void clearView(int i) {
        for (int j = 0; j < saveView.size(); j++) {
            if (j != i) {
                TextView textView = (TextView) saveView.get(j).findViewById(R.id.newWorkOrder);
                if (!textView.getText().equals("最新工单")) {
                    textView.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.getData().size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    @Override
    public View getView(int arg0, View view, ViewGroup arg2) {
        view = mInflater.inflate(R.layout.pc_record_item, null);
        TextView work_orderNumbe = (TextView) view.findViewById(R.id.work_orderNumber);
        TextView stateTes = (TextView) view.findViewById(R.id.newWorkOrder);
        String workOrderNumber = list.getData().get(arg0).getWorkOrderNo();
        work_orderNumbe.setText(workOrderNumber);
        WorkOrderControData workOrderControData = new Select().from(WorkOrderControData.class).where(Condition.column(WorkOrderControData$Table.WORKORDERNUMBER).is(workOrderNumber)).querySingle();
        String state = workOrderControData.workOrderState;
        if (state.equals("2") || state.equals("8")) {
            //未反馈 与 重新派工
            stateTes.setText("最新工单");
            stateTes.setVisibility(View.VISIBLE);
        } else {
            stateTes.setVisibility(View.INVISIBLE);
        }
        saveView.add(arg0, view);
        return view;
    }
}
