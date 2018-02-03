package com.saintsung.saintpmc.orderdatabase;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by YinTxLz on 2017/5/31.
 *
 */
@ModelContainer
@Table(databaseName = DBFlowDatabase.NAME)
public class WorkOrderBean extends BaseModel {
    //自增ID
    @Column
    @PrimaryKey
    public Long id;
    //工单编号
    @Column
    public String workOrderNumber;
    //开始时间
    @Column
    public String starTime;
    //结束时间
    @Column
    public String endTime;
    //工单状态
    @Column
    public String workOrderState;
}
