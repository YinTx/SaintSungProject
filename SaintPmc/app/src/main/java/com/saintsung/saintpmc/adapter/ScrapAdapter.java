package com.saintsung.saintpmc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.saintsung.saintpmc.R;
import com.saintsung.saintpmc.bean.ScrapItemBean;

import java.util.List;

/**
 * Created by XLzY on 2018/1/11.
 */

public class ScrapAdapter extends BaseAdapter {
    private Context context;
    private List<ScrapItemBean> list;
    public ScrapAdapter(Context context, List<ScrapItemBean> list){
        this.context=context;
        this.list=list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(context).inflate(R.layout.scrap_adapter_item,null);
        TextView textView= (TextView) view.findViewById(R.id.scrap_item_text);
        textView.setText(list.get(i).getLno());
        view.findViewById(R.id.scrap_item_sub).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list.remove(i);
                ScrapAdapter.super.notifyDataSetChanged();
            }
        });
        return view;
    }
}
