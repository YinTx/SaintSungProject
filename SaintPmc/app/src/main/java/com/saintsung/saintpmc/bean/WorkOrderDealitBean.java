package com.saintsung.saintpmc.bean;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by EvanShu on 2018/
 * 3/8.
 */

public class WorkOrderDealitBean {
    private String optCode;
    private String workOrderNo;
    private String nums;
    private String cntNum;
    private String sign;
    private String lockNum;
    private List<WorkOrderDataItemBean> data=new ArrayList<>();
    private String result;
    private String resultMessage;
    private String startTime;
    private String endTime;

    public String getLockNum() {
        return lockNum;
    }

    public void setLockNum(String lockNum) {
        this.lockNum = lockNum;
    }

    public String getOptCode() {
        return optCode;
    }

    public void setOptCode(String optCode) {
        this.optCode = optCode;
    }

    public String getWorkOrderNo() {
        return workOrderNo;
    }

    public void setWorkOrderNo(String workOrderNo) {
        this.workOrderNo = workOrderNo;
    }

    public String getNums() {
        return nums;
    }

    public void setNums(String nums) {
        this.nums = nums;
    }

    public String getCntNum() {
        return cntNum;
    }

    public void setCntNum(String cntNum) {
        this.cntNum = cntNum;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public List<WorkOrderDataItemBean> getData() {
        return data;
    }

    public void setData(List<WorkOrderDataItemBean> data) {
        this.data = data;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
