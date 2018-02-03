package com.saintsung.saintpmc.orderdatabase;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by YinTxLz on 2017/5/31.
 */
@ModelContainer
@Table(databaseName = DBFlowDatabase.NAME)
public class WorkOrderDetailsBean extends BaseModel {
    @Column
    @PrimaryKey
    public Long id;
    //工单编号
    @Column
    public String workOrderNumber;
    //锁号
    @Column
    public String lookNumber;
    //开锁码
    @Column
    public String openLookNumber;
    //设备类型
    @Column
    public String deviceType;
    //经度
    @Column
    public String longitude;
    //纬度
    @Column
    public String latitude;
}
