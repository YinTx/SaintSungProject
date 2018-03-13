package com.saintsung.saintpmc.bean;

/**
 * Created by EvanShu on 2018/3/9.
 */

public class LockNumberBean {
    private String lockNo;
    private String optPwd;
    private String lockType;
    public String getLockNo() {
        return lockNo;
    }

    public void setLockNo(String lockNo) {
        this.lockNo = lockNo;
    }

    public String getOptPwd() {
        return optPwd;
    }

    public String getLockType() {
        return lockType;
    }

    public void setLockType(String lockType) {
        this.lockType = lockType;
    }

    public void setOptPwd(String optPwd) {
        this.optPwd = optPwd;
    }
}
