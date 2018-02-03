package com.saintsung.saintpmc.orderdatabase;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by EvanShu on 2018/1/24.
 */
@ModelContainer
@Table(databaseName=DBFlowDatabase.NAME)
public class LockInformation extends BaseModel {
    @Column
    @PrimaryKey(autoincrement = true)
    public int id;
    @Column
    public String lockNo;
    @Column
    public String assetno;
    @Column
    public String optPwd;
    @Column
    public String pointX;
    @Column
    public String pointY;
    @Column
    public String type;
    @Column
    public String starTime;
    @Column
    public String endTime;
}
