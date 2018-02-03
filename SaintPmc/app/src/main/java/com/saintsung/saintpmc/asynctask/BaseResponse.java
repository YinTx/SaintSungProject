package com.saintsung.saintpmc.asynctask;

/**
 * Created by EvanShu on 2018/1/30.
 */

public class BaseResponse<T> {
    private boolean success;//请求是否成功
    private int resultCode;//状态吗
    private String msg;//返回的提示消息
    private T data;//主要内容,因为不知道返回的会是什么类型,所以用泛型来表示
    //get set方法就不贴了.

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
