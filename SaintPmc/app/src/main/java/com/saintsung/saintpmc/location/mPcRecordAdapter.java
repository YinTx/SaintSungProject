package com.saintsung.saintpmc.location;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.saintsung.saintpmc.R;
import com.saintsung.saintpmc.bean.WorkOrderBean;
import com.saintsung.saintpmc.bean.WorkOrderDataItemBean;


import java.util.List;


public class mPcRecordAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	public WorkOrderBean list;
	public mPcRecordAdapter(Context context,WorkOrderBean list) {
		this.mInflater = LayoutInflater.from(context);
		this.list=list;
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
		view=mInflater.inflate(R.layout.pc_record_item, null);
		TextView work_orderNumbe= (TextView) view.findViewById(R.id.work_orderNumber);
		work_orderNumbe.setText(list.getData().get(arg0).getWorkOrderNo());
		String state=list.getData().get(arg0).getWorkState();
		if(state.equals("2") || state.equals("8")){
			work_orderNumbe.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		}
		return view;
	}
}
