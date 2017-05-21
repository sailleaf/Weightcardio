package com.leaf.yeyy.weightcardio.bean;

/**
 * {"ret":"0","errMsg":null,"errCode":null,"data":{"data":{"weight":"null","height":"172"},"result":true},"tips":{"content":null,"title":null,"button":null,"type":"default"}}
 */
public class UserInfoBean {
    public Data data;

    public class Data {
        public String weight;
        public String height;
    }

}
