package com.saintsung.saintpmc.orderdatabase;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by EvanShu on 2018/2/3.
 */
@ModelContainer
@Table(databaseName = DBFlowDatabase.NAME)
public class WorkOrderControData extends BaseModel {
    @Column
    @PrimaryKey
    public String workOrderNumber;
    @Column
    public String workOrderState;
    @Column
    public String startTime;
    @Column
    public String endTime;
    @Column
    public String workTime;
}
