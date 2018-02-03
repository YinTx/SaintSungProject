package com.saintsung.saintpmc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.saintsung.saintpmc.orderdatabase.WorkOrderDetailsBean;

import java.util.List;

public class mListAdapter2 extends BaseAdapter {
	private LayoutInflater mInflater;
	private List mArrayAdapter;
	private List<WorkOrderDetailsBean> list;
	mListAdapter2(Context context, List<WorkOrderDetailsBean> list) {
		this.mInflater = LayoutInflater.from(context);
		this.list=list;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();

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
		view = mInflater.inflate(R.layout.orderdetailsitem, null);
		TextView myOrderNo= (TextView) view.findViewById(R.id.orderNo);
		TextView myStarTime= (TextView) view.findViewById(R.id.starTime);
		TextView myEndTime= (TextView) view.findViewById(R.id.endTime);
		TextView myType= (TextView) view.findViewById(R.id.type);
		TextView myLookNo= (TextView) view.findViewById(R.id.lookNo);
		myOrderNo.setText("开锁码："+list.get(arg0).lookNumber);
		return view;
	}
}
