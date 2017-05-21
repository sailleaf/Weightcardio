package com.leaf.yeyy.weightcardio.bean;

public class SignInBean {
    public String userID;
    public String deviceID;
    public String code;

    public SignInBean(String userID, String deviceID, String code) {
        this.userID = userID;
        this.deviceID = deviceID;
        this.code = code;
    }

}
