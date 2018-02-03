package com.saintsung.saintpmc.bean;

import java.util.List;

/**
 * Created by XLzY on 2018/1/18.
 */

public class WorkOrderDataBean {
    private String workOrderNo;//工单编号
    private List<WorkOrderDataItemBean> lockInfos;
    private String workType; //类型
    private String workNote; //内容
    private String workState;//状态
    private String startTime;
    private String endTime;

    public String getWorkOrderNo() {
        return workOrderNo;
    }

    public void setWorkOrderNo(String workOrderNo) {
        this.workOrderNo = workOrderNo;
    }

    public List<WorkOrderDataItemBean> getLockInfos() {
        return lockInfos;
    }

    public void setLockInfos(List<WorkOrderDataItemBean> lockInfos) {
        this.lockInfos = lockInfos;
    }

    public String getWorkType() {
        return workType;
    }

    public void setWorkType(String workType) {
        this.workType = workType;
    }

    public String getWorkNote() {
        return workNote;
    }

    public void setWorkNote(String workNote) {
        this.workNote = workNote;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getWorkState() {
        return workState;
    }

    public void setWorkState(String workState) {
        this.workState = workState;
    }


    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
