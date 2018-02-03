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
public class DicCategoryBean extends BaseModel {
    @Column
    @PrimaryKey
    public Long ID;
    @Column
    public String POINT_Y;
    @Column
    public String POINT_X;
    @Column
    public String CATE_NAME;
    @Column
    public String IS_DELETE;
    @Column
    public String P_ID;
}
