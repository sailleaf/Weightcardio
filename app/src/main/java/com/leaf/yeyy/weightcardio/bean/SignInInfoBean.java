package com.leaf.yeyy.weightcardio.bean;

/**
 * Created by Administrator on 2017/4/26.
 {
 "ret":"0",
 "errMsg":null,
 "errCode":null,
 "data":{"msg":"code is wrong!","result":false},
 "tips":{"content":null,"title":null,"button":null,"type":"default"}
 }
 */

public class SignInInfoBean {
    public String ret;
    public String errMsg;
    public String errCode;
    public Data data;
    public Tips tips;

    public class Data {
        public String msg;
        public boolean result;
    }

    public class Tips {
        public String content;
        public String title;
        public String button;
        public String type;
    }

}
