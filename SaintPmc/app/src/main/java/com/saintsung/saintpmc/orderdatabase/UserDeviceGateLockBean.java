package com.saintsung.saintpmc.orderdatabase;

/**
 * Created by YinTxLz on 2017/6/13.
 */

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * 用户关联表实体类（此表为未修改表，暂时停用）
 */
@ModelContainer
@Table(databaseName = DBFlowDatabase.NAME)
public class UserDeviceGateLockBean extends BaseModel {
    //{"ELEC_USER_ID":"1","ELEC_DEVICE_ID_TYPE":"2","ELEC_DEVICE_ID":"3","ID":"3","L_ID":"10","IS_DELETE":"1"}
    @Column
    @PrimaryKey
    public Long id;
    @Column
    public String ELEC_USER_ID;
    @Column
    public String ELEC_DEVICE_ID;
    @Column
    public String ELEC_DEVICE_ID_TYPE;
    @Column
    public String L_ID;
    @Column
    public String IS_DELETE;
}
