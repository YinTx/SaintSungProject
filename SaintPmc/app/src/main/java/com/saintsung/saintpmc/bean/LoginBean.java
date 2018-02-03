package com.saintsung.saintpmc.bean;

/**
 * Created by EvanShu on 2018/1/24.
 */

public class LoginBean {
    private String optCode;
    private String sign;
    private LoginItemBean data;
    private String result;
    private String resultMessage;
   private String optUserNumber;
    public String getOptCode() {
        return optCode;
    }

    public String getOptUserNumber() {
        return optUserNumber;
    }

    public void setOptUserNumber(String optUserNumber) {
        this.optUserNumber = optUserNumber;
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

    public LoginItemBean getData() {
        return data;
    }

    public void setData(LoginItemBean data) {
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
