package com.saintsung.saintpmc.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by EvanShu on 2018/3/9.
 */

public class LockPwdServiceBean {
    private String optCode;
    private String optUserNumber;
    private String sign;
    private LockNumberBean data;
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

    public LockNumberBean getData() {
        return data;
    }

    public void setData(LockNumberBean data) {
        this.data = data;
    }
}
