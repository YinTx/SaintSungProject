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
 * 用户表实体类
 */
@ModelContainer
@Table(databaseName = DBFlowDatabase.NAME)
public class LstElecUserBean extends BaseModel{
    @Column
    @PrimaryKey
    public Long ID;
    @Column
    public String ELEC_USER_TYPE;
    @Column
    public String ELEC_USER_STATUS;
    @Column
    public String ELEC_USER_NAME;
    @Column
    public String ELEC_LEVER;
    @Column
    public String ELEC_USER_MOBILE;
    @Column
    public String IS_DELETE;
    @Column
    public String METER_POINT_ID;
    @Column
    public String ELEC_USER_NO;
    @Column
    public String OFFICE_NO;
}
