package com.saintsung.saintpmc.orderdatabase;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by XLzY on 2018/1/5.
 */
@ModelContainer
@Table(databaseName=DBFlowDatabase.NAME)
public class DoorAndMeterDataBase extends BaseModel{
    @Column
    @PrimaryKey(autoincrement = true)
    public int id;
    @Column
    public String jsonStrInService;
    @Column
    public int strType;//表示该Json字符串的类型
    @Column
    public String editBureauNo;
}
