package com.saintsung.saintpmc.orderdatabase;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by YinTxLz on 2017/6/21.
 * 工单关系表（新）
 */
@ModelContainer
@Table(databaseName = DBFlowDatabase.NAME)
public class LstElecUserLockBean extends BaseModel {
    @Column
    @PrimaryKey
    public Long id;
    @Column
    public String ELEC_USER_ID;
    @Column
    public String L_ID;
    @Column
    public String SITE_ID;
    @Column
    public String IS_DELETE;

    public Long getId() {
        return id;
    }
}
