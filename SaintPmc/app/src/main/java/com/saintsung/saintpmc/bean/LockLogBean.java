package com.saintsung.saintpmc.bean;

/**
 * Created by EvanShu on 2018/2/1.
 */

public class LockLogBean {
    private String lockNumber;
    private String resultId;
    private String dateTime;

    public String getLockNumber() {
        return lockNumber;
    }

    public void setLockNumber(String lockNumber) {
        this.lockNumber = lockNumber;
    }
    public String getResultId() {
        return resultId;
    }

    public void setResultId(String resultId) {
        this.resultId = resultId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
