package com.saintsung.saintpmc.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by EvanShu on 2018/2/28.
 */

public class WorkOrderUpServiceBean {
    private String optCode;
    private String sign;
    private List<WorkOrderItemBean> data=new ArrayList<>();

    public String getOptCode() {
        return optCode;
    }

    public void setOptCode(String optCode) {
        this.optCode = optCode;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public List<WorkOrderItemBean> getData() {
        return data;
    }

    public void setData(List<WorkOrderItemBean> data) {
        this.data = data;
    }
}
