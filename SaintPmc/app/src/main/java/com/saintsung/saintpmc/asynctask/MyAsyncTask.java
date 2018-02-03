package com.saintsung.saintpmc.asynctask;

import android.os.AsyncTask;
/**
 * Created by YinTxLz on 2017/6/5.
 */
public class MyAsyncTask extends AsyncTask<String,String,String>{
    @Override
    protected String doInBackground(String... params) {
        SocketConnect socketConnect=new SocketConnect();
        return socketConnect.sendDate(params[0]);
    }
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

    }
    @Override
    protected void onPreExecute() {

        super.onPreExecute();
    }
}
