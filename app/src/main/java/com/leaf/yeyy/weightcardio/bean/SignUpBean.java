package com.leaf.yeyy.weightcardio.bean;

public class SignUpBean {
    public String userID;
    public String deviceID;
    public String code;
    public String height;
    public String age;
    public String sex;

    public  SignUpBean(String userID, String deviceID, String code, String height, String age, String sex){
        this.userID = userID;
        this.code = code;
        this.height = height;
        this.deviceID = deviceID;
        this.age = age;
        this.sex = sex;
    }

}
