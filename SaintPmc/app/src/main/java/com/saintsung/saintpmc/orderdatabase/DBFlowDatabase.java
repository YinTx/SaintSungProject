package com.saintsung.saintpmc.orderdatabase;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Created by YinTxLz on 2017/5/31.
 */
@Database(name = DBFlowDatabase.NAME,version = DBFlowDatabase.VERSION)
public class DBFlowDatabase{
    //数据库名称
    public static final String NAME = "WorkOrderManage";
    //数据库版本号
    public static final int VERSION = 2;
}
