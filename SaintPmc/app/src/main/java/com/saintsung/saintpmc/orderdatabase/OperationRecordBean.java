package com.saintsung.saintpmc.orderdatabase;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by YinTxLz on 2017/6/1.
 * 开关设备记录实体类
 */
@ModelContainer
@Table(databaseName = DBFlowDatabase.NAME)
public class OperationRecordBean extends BaseModel {
    //自增id
    @Column
    @PrimaryKey
    public Long id;
    //操作时间
    @Column
    public String operationTime;
    //设备编号
    @Column
    public String equipmentNumber;
   // 执行动作
    @Column
    public String executionAct;
   // 工单编号
    @Column
    public String workOrderNumber;
}
