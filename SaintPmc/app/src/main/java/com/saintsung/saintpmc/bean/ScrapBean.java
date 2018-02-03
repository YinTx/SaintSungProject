package com.saintsung.saintpmc.bean;


import java.util.List;


/**
 * Created by XLzY on 2018/1/4.
 */
public class ScrapBean {
    private String optCode="";//服务器地址
    private String optUserNumber=""; //用户编号
    private String userNumber="";//局号
    private String userName=""; //用电户名
    private List<ScrapItemBean> data; //数据
    private String result=""; //结果编号
    private String resultMessage="";//返回消息


    private String sign="";//

    public String getOptCode() {
        return optCode;
    }

    public void setOptCode(String optCode) {
        this.optCode = optCode;
    }

    public String getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(String userNumber) {
        this.userNumber = userNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<ScrapItemBean> getData() {
        return data;
    }

    public void setData(List<ScrapItemBean> data) {
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
}
