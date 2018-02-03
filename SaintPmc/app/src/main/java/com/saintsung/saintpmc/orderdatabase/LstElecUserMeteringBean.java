package com.saintsung.saintpmc.orderdatabase;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by YinTxLz on 2017/7/11.
 */
@ModelContainer
@Table(databaseName = DBFlowDatabase.NAME)
public class LstElecUserMeteringBean extends BaseModel {
    @Column
    @PrimaryKey
    public Long ID;
    @Column
    public String ELEC_USER_ID;

    public Long getID() {
        return ID;
    }

    @Column
    public String METERING_NO;


}
