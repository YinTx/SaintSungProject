package com.saintsung.saintpmc.orderdatabase;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by XLzY on 2018/1/6.
 */
@ModelContainer
@Table(databaseName = DBFlowDatabase.NAME)
public class DicLockSiteBean extends BaseModel {
    @PrimaryKey
    @Column
    public String id;
    @Column
    public String siteName;
    @Column
    public String siteCode;

}
