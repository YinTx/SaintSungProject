package com.saintsung.saintpmc.bean;

import java.util.List;

/**
 * Created by EvanShu on 2018/2/1.
 */

public class LockLogUpServiceBean {
    private String optCode;
    private String optUserNumber;
    private List<LockLogBean> data;
    private String sign;
    private String version;
    private String result;
    private String resultMessage;

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

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

    public List<LockLogBean> getData() {
        return data;
    }

    public void setData(List<LockLogBean> data) {
        this.data = data;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
