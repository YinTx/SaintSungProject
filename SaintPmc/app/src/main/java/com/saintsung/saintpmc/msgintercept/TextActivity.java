package com.saintsung.saintpmc.msgintercept;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.TextView;

import com.example.common.app.Activity;
import com.saintsung.saintpmc.R;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by YinTxLz on 2017/2/27.
 */

public class TextActivity extends Activity implements IView{
    @BindView(R.id.sear_str)
    TextView searStr;
    @BindView(R.id.input_text)
    EditText inputStr;
    private Presenter mPresenter;
    @Override
    protected int getContentLayoutId() {
        return R.layout.textactivity;
    }
    @Override
    protected void initData(){
        super.initData();
        mPresenter=new Presenter(this);
    }
    @OnClick(R.id.sumbit)
    public void search(){
        mPresenter.search();
    }

    @Override
    public String getSearchString() {
        return inputStr.getText().toString();
    }

    @Override
    public void setSearchString(String string) {
        searStr.setText(string);
    }
}
