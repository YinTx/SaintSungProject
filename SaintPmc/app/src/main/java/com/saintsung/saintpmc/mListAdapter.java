package com.saintsung.saintpmc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class mListAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List mArrayAdapter;
	private List<String[]> list;
	mListAdapter(Context context,List<String[]> list) {
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
		String[] str=list.get(arg0);
		if(arg0==0){
			view = mInflater.inflate(R.layout.adapter_time, null);
			TextView textView1= (TextView) view.findViewById(R.id.adapter_time);
			textView1.setText(str[1]+" - "+str[2]);
			TextView textView2= (TextView) view.findViewById(R.id.order_no);
			textView2.setText("工单号:"+str[0]);
			TextView textView3= (TextView) view.findViewById(R.id.id_no);
			textView3.setText("设备编号:"+str[7]);
			TextView textView4= (TextView) view.findViewById(R.id.equipment_type);
			if(str[9].equals("1")){
				textView4.setText("设备类型:"+"锁具");
			}else if(str[9].equals("2")){
				textView4.setText("设备类型:"+"螺丝");
			}else if(str[9].equals("3")){
				textView4.setText("设备类型:"+"井盖");
			}else if(str[9].equals("4")){
	            textView4.setText("设备类型:"+"阀门锁");
			}else if(str[9].equals("5")){
				textView4.setText("设备类型:"+"报警器");
			}else if(str[9].equals("6")){
				textView4.setText("设备类型:"+"采集终端");
			}
			TextView textView5= (TextView) view.findViewById(R.id.adapter_name);
			textView5.setText("测试人:"+str[3]);
			TextView textView6= (TextView) view.findViewById(R.id.adapter_address);
			textView6.setText("地址:"+str[4]);
		}else{
			String[] lastPost=list.get(arg0-1);
			if(str[1].equals(lastPost[1]) && str[2].equals(lastPost[2])){
				view = mInflater.inflate(R.layout.mlist_item, null);
				TextView textView2= (TextView) view.findViewById(R.id.order_no);
				textView2.setText("工单号:"+str[0]);
				TextView textView3= (TextView) view.findViewById(R.id.id_no);
				textView3.setText("设备编号:"+str[7]);
				TextView textView4= (TextView) view.findViewById(R.id.equipment_type);
				if(str[9].equals("1")){
					textView4.setText("设备类型:"+"锁具");
				}else if(str[9].equals("2")){
					textView4.setText("设备类型:"+"螺丝");
				}else if(str[9].equals("3")){
					textView4.setText("设备类型:"+"井盖");
				}else if(str[9].equals("4")){
					textView4.setText("设备类型:"+"阀门锁");
				}else if(str[9].equals("5")){
					textView4.setText("设备类型:"+"报警器");
				}else if(str[9].equals("6")){
					textView4.setText("设备类型:"+"采集终端");
				}
				TextView textView5= (TextView) view.findViewById(R.id.adapter_name);
				textView5.setText("测试人:"+str[3]);
				TextView textView6= (TextView) view.findViewById(R.id.adapter_address);
				textView6.setText("地址:"+str[4]);
			}else{
				view = mInflater.inflate(R.layout.adapter_time, null);
				TextView textView1= (TextView) view.findViewById(R.id.adapter_time);
				textView1.setText(str[1]+" - "+str[2]);
				TextView textView2= (TextView) view.findViewById(R.id.order_no);
				textView2.setText("工单号:"+str[0]);
				TextView textView3= (TextView) view.findViewById(R.id.id_no);
				textView3.setText("设备编号:"+str[7]);
				TextView textView4= (TextView) view.findViewById(R.id.equipment_type);
				if(str[9].equals("1")){
					textView4.setText("设备类型:"+"锁具");
				}else if(str[9].equals("2")){
					textView4.setText("设备类型:"+"螺丝");
				}else if(str[9].equals("3")){
					textView4.setText("设备类型:"+"井盖");
				}else if(str[9].equals("4")){
					textView4.setText("设备类型:"+"阀门锁");
				}else if(str[9].equals("5")){
					textView4.setText("设备类型:"+"报警器");
				}else if(str[9].equals("6")){
					textView4.setText("设备类型:"+"采集终端");
				}
				TextView textView5= (TextView) view.findViewById(R.id.adapter_name);
				textView5.setText("测试人:"+str[3]);
				TextView textView6= (TextView) view.findViewById(R.id.adapter_address);
				textView6.setText("地址:"+str[4]);
			}

		}

//		view = mInflater.inflate(R.layout.mlist_item, null);
//		if (arg0 == 19) {
//			view.findViewById(R.id.item_view).setVisibility(View.GONE);
//		}
//		if (arg0 == 8) {
//			view.findViewById(R.id.item_time_icon).setBackgroundResource(
//					R.mipmap.list_item_true);
//		}
//		if(arg0==5){
//			view.findViewById(R.id.item_time_icon2).setVisibility(View.VISIBLE);
//			view.findViewById(R.id.item_time_icon).setVisibility(View.GONE);
//		}
		// TextView t=(TextView) view.findViewById(R.id.mLanya);
		// t.setText(mArrayAdapter.get(arg0)+"");
		return view;
	}
}
