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
public class LstLookBean extends BaseModel {
    //{"POINT_Y":"30.638686","STATE_ID":"3","TYPE_ID":"1","POINT_X":"119.692427","CATE_ID":"5","L_NO":"285436506","ADDRESS":"待更新。","USER_ID":"3","ID":"10","OPT_PWD":"123123213213213","IS_DELETE":"0"}
    @Column
    @PrimaryKey
    public Long ID;
    @Column
    public String POINT_Y;
    @Column
    public String STATE_ID;
    @Column
    public String TYPE_ID;
    @Column
    public String POINT_X;
    @Column
    public String CATE_ID;
    @Column
    public String L_NO;
    @Column
    public String ADDRESS;
    @Column
    public String USER_ID;
    @Column
    public String OPT_PWD;
    @Column
    public String IS_DELETE;
    @Column
    public String ASSET_NO;
}
