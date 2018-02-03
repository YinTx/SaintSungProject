package com.saintsung.saintpmc.orderdatabase;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by YinTxLz on 2017/6/13.
 */
@ModelContainer
@Table(databaseName = DBFlowDatabase.NAME)
public class LstElecDeviceBean extends BaseModel {
    @Column
    public String ELEC_DEVICE_NO;
    @Column
    public String ELEC_DEVICE_VENDER;
    @Column
    @PrimaryKey
    public Long ID;
    @Column
    public String ELEC_DEVICE_TYPE;
    @Column
    public String ELEC_DEVICE_STATUS;
    @Column
    public String IS_DELETE;
    @Column
    public String METER_POINT_ID;
}
