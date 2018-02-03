package com.saintsung.saintpmc.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.saintsung.saintpmc.R;
import com.saintsung.saintpmc.orderdatabase.DicLockSiteBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by XLzY on 2018/1/6.
 */

public class SiteDataAdapter extends BaseAdapter implements View.OnClickListener {
    private Context context;
    private List<DicLockSiteBean> dicLockSiteBean;
    private MyClickListener mListener;
    public List<View> saveView = new ArrayList<>();

    public SiteDataAdapter(Context context, List<DicLockSiteBean> list, MyClickListener listener) {
        this.context = context;
        this.dicLockSiteBean = list;
        this.mListener = listener;
    }

    public void setViewEnabled(boolean flag) {
        for (int i = 0; i < saveView.size(); i++) {
            saveView.get(i).findViewById(R.id.number).setEnabled(flag);
            saveView.get(i).findViewById(R.id.scan).setEnabled(flag);
        }
    }

    ;

    public void cloneTextStr() {
        for (int i = 0; i < saveView.size(); i++) {
            EditText text = (EditText) saveView.get(i).findViewById(R.id.number);
            text.setText("");
        }
    }

    @Override
    public int getCount() {
        return dicLockSiteBean.size();
    }

    @Override
    public Object getItem(int i) {
        Log.e("TAG","I="+i);
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        ViewHolder viewHolder = null;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = layoutInflater.inflate(R.layout.linearlayout_scan_edittext, null);
            viewHolder.numberText = (EditText) view.findViewById(R.id.number);
            viewHolder.imgScan = (ImageButton) view.findViewById(R.id.scan);
            viewHolder.hs=dicLockSiteBean.get(i).siteCode;
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.numberText.setHint("请输入或扫描" + dicLockSiteBean.get(i).siteName + "铅封编号");
        viewHolder.numberText.setEnabled(false);
        viewHolder.imgScan.setOnClickListener(this);
        viewHolder.imgScan.setEnabled(false);
//        saveView.add(view);
        saveView.add(i,view);
        return view;
    }

    @Override
    public void onClick(View view) {
        mListener.clickListener(view);

    }

    public interface MyClickListener {
        public void clickListener(View v);
    }

    public final class ViewHolder {
        public EditText numberText;
        public ImageButton imgScan;
        public String hs;

    }
}
