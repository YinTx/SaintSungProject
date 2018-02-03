package com.saintsung.saintpmc.msgintercept;

/**
 * Created by EvanShu on 2018/2/1.
 */

public class UserService implements IUserService {
    @Override
    public String search(int hashCode) {
        return "User:"+hashCode;
    }
}
