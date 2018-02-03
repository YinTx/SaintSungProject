package com.saintsung.saintpmc.workorder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.saintsung.saintpmc.R;

import java.util.List;

public class mDetailsAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List mArrayAdapter;
	private List<String> list;
	mDetailsAdapter(Context context, List<String> list) {
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
		view=mInflater.inflate(R.layout.orderdetails,null);
		TextView myOrderNo= (TextView) view.findViewById(R.id.myOrderNo);
		myOrderNo.setText(list.get(arg0));
		return view;
	}
}
