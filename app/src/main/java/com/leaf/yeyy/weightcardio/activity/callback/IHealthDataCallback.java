package com.leaf.yeyy.weightcardio.activity.callback;

import com.leaf.yeyy.weightcardio.bean.HealthDataBean;

/**
 * Created by WIN10 on 2017/5/14.
 */

public interface IHealthDataCallback {
    void onSuccess(HealthDataBean healthDataBean);
    void onFailure(String errMsg);
}
