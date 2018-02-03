package com.saintsung.saintpmc;

import android.app.Application;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.saintsung.saintpmc.bean.WorkOrderBean;

public class MyApplication extends Application {
    private static MyApplication instance;
    private static String userName;
    private static String userId;
    private static String url;

    public static String getUrl() {
        return url;
    }

    public static void setUrl(String url) {
        MyApplication.url = url;
    }

    private static WorkOrderBean workOrderBean=new WorkOrderBean();

    public static WorkOrderBean getWorkOrderBean() {
        return workOrderBean;
    }

    public static void setWorkOrderBean(WorkOrderBean workOrderBean) {
        MyApplication.workOrderBean = workOrderBean;
    }

    public static String getUserName() {
        return userName;
    }

    public static String getUserId() {
        return userId;
    }

    public static void setUserId(String userId) {
        MyApplication.userId = userId;
    }

    public static void setUserName(String userName) {
        MyApplication.userName = userName;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(getApplicationContext());
        FlowManager.init(getApplicationContext());
    }

    public static MyApplication getInstance() {
        if (instance == null) {
            instance = new MyApplication();
        }
        return instance;
    }
}
