package com.saintsung.saintpmc.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by XLzY on 2018/1/18.
 */

public class WorkOrderBean {
    private String optCode;
    private String optUserNumber;
    private String sign;
    private List<WorkOrderDataBean> data=new ArrayList<>();
    private String result;
    private String resultMessage;

    public String getOptCode() {
        return optCode;
    }

    public void setOptCode(String optCode) {
        this.optCode = optCode;
    }

    public String getOptUserNumber() {
        return optUserNumber;
    }

    public void setOptUserNumber(String optUserNumber) {
        this.optUserNumber = optUserNumber;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public List<WorkOrderDataBean> getData() {
        return data;
    }

    public void setData(List<WorkOrderDataBean> data) {
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
}
