package com.saintsung.saintpmc.workorder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.saintsung.saintpmc.R;
import com.saintsung.saintpmc.orderdatabase.LstLookBean;

import com.saintsung.saintpmc.orderdatabase.LstLookBean$Table;
import com.saintsung.saintpmc.orderdatabase.WorkOrderBean;

import com.saintsung.saintpmc.orderdatabase.WorkOrderBean$Table;
import com.saintsung.saintpmc.orderdatabase.WorkOrderDetailsBean;

import java.util.List;

public class mDetailsAdapterItem2 extends BaseAdapter {
	private LayoutInflater mInflater;
	private List mArrayAdapter;
	private List<WorkOrderDetailsBean> list;
    WorkOrderBean peoples;
	mDetailsAdapterItem2(Context context, List<WorkOrderDetailsBean> list) {
		this.mInflater = LayoutInflater.from(context);
		this.list=list;
		if(list.size()>0){
			peoples = new Select().from(WorkOrderBean.class).where(Condition.column(WorkOrderBean$Table.WORKORDERNUMBER).is(list.get(0).workOrderNumber)).querySingle();}
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
		WorkOrderDetailsBean workOrderDetailsBean=list.get(arg0);
		view=mInflater.inflate(R.layout.orderdetailsitem,null);
		LstLookBean lstLookBean=new Select().from(LstLookBean.class).where(Condition.column(LstLookBean$Table.L_NO).is(list.get(arg0).lookNumber)).querySingle();
		TextView myOrderNo= (TextView) view.findViewById(R.id.orderNo);
		TextView myStarTime= (TextView) view.findViewById(R.id.starTime);
		TextView myEndTime= (TextView) view.findViewById(R.id.endTime);
		TextView myType= (TextView) view.findViewById(R.id.type);
		TextView myLookNo= (TextView) view.findViewById(R.id.lookNo);
		myOrderNo.setText(workOrderDetailsBean.workOrderNumber);
		myStarTime.setText("开始:"+peoples.starTime);
		myEndTime.setText("结束:"+peoples.endTime);
		myLookNo.setText("资产号:"+lstLookBean.ASSET_NO);
		if(workOrderDetailsBean.deviceType.equals("0001")){
			myType.setText("设备类型：挂锁");
		}else if(workOrderDetailsBean.deviceType.equals("0002")) {
			myType.setText("设备类型：螺丝");
		}else if(workOrderDetailsBean.deviceType.equals("0003")){
			myType.setText("设备类型：井盖");
		}else if(workOrderDetailsBean.deviceType.equals("0004")){
			myType.setText("设备类型：阀门");
		}else if(workOrderDetailsBean.deviceType.equals("0005")){
			myType.setText("设备类型：报警器");
		}else if(workOrderDetailsBean.deviceType.equals("0006")){
			myType.setText("设备类型：铅封");
		}
		return view;
	}
}
