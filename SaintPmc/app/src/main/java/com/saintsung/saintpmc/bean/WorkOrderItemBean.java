package com.saintsung.saintpmc.bean;

/**
 * Created by EvanShu on 2018/2/28.
 */

public class WorkOrderItemBean {
    private String workOrderNo;
    private String optType;
    private String optTime;

    public String getWorkOrderNo() {
        return workOrderNo;
    }

    public void setWorkOrderNo(String workOrderNo) {
        this.workOrderNo = workOrderNo;
    }

    public String getOptType() {
        return optType;
    }

    public void setOptType(String optType) {
        this.optType = optType;
    }

    public String getOptTime() {
        return optTime;
    }

    public void setOptTime(String optTime) {
        this.optTime = optTime;
    }
}
