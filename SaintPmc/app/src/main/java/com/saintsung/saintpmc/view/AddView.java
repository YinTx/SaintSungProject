package com.saintsung.saintpmc.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.saintsung.saintpmc.R;
import com.saintsung.saintpmc.orderdatabase.DicLockSiteBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by XLzY on 2018/1/6.
 */

public class AddView extends ListView {
    public List<View> saveViewList=new ArrayList<>();
    public AddView(Context context, List<DicLockSiteBean> dicLockSiteBean,LinearLayout mLl_parent) {
        super(context);
        for(int i=0;i<dicLockSiteBean.size();i++){
            View view=addLayout(dicLockSiteBean.get(i),context);
            saveViewList.add(view);
            mLl_parent.addView(view);
        }
    }
    private View addLayout(DicLockSiteBean dicLockSiteBean,Context context){
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LayoutInflater inflater3 = LayoutInflater.from(context);
        View view = inflater3.inflate(R.layout.linearlayout_scan_edittext, null);
        EditText text= (EditText) view.findViewById(R.id.number);
        text.setHint("请输入或扫描"+dicLockSiteBean.siteName+"铅封编号");
        view.setLayoutParams(lp);
        setViewIsFalse(view);
        view.setTag(dicLockSiteBean);
        return view;
    }
    private void setViewIsFalse(View view){
        view.findViewById(R.id.number).setEnabled(false);
        view.findViewById(R.id.scan).setEnabled(false);
    }
    private void setViewIsTrue(View view){
        view.findViewById(R.id.number).setEnabled(true);
        view.findViewById(R.id.scan).setEnabled(true);
    }
}
