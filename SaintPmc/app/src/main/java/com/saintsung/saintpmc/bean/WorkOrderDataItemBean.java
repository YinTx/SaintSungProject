package com.saintsung.saintpmc.bean;

/**
 * Created by XLzY on 2018/1/18.
 */

public class WorkOrderDataItemBean {
    private String lockNo;//锁号
    private String assetNo;//资产编号
    private String optPwd;
    private String pointX;
    private String pointY;
    private String type;


    public String getLockNo() {
        return lockNo;
    }

    public void setLockNo(String lockNo) {
        this.lockNo = lockNo;
    }

    public String getAssetNo() {
        return assetNo;
    }

    public void setAssetNo(String assetNo) {
        this.assetNo = assetNo;
    }

    public String getOptPwd() {
        return optPwd;
    }

    public void setOptPwd(String optPwd) {
        this.optPwd = optPwd;
    }

    public String getPointX() {
        return pointX;
    }

    public void setPointX(String pointX) {
        this.pointX = pointX;
    }

    public String getPointY() {
        return pointY;
    }

    public void setPointY(String pointY) {
        this.pointY = pointY;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
