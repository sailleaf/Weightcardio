package com.leaf.yeyy.weightcardio.activity.assistant;

import android.util.Log;

import com.google.gson.Gson;
import com.leaf.yeyy.weightcardio.activity.callback.IHealthDataCallback;
import com.leaf.yeyy.weightcardio.bean.HealthDataBean;
import com.leaf.yeyy.weightcardio.global.AppConstants;
import com.leaf.yeyy.weightcardio.http.HttpRequestUtil;
import com.leaf.yeyy.weightcardio.preferences.SPKey;
import com.leaf.yeyy.weightcardio.preferences.SharedPreferencesDao;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by WIN10 on 2017/5/14.
 */

public class HealthData {
    private static final String TAG = HealthData.class.getSimpleName();
    private static volatile HealthData mInstance;
    private static HealthDataBean mHealthDataBean;

    private HealthData() {
    }

    public static HealthData getInstance() {
        if (mInstance == null) {
            synchronized (HealthData.class) {
                if (mInstance == null) {
                    mInstance = new HealthData();
                }
            }
        }
        return mInstance;
    }

    public synchronized void getHealthData(final IHealthDataCallback callback) {
        if (mHealthDataBean != null) {
            callback.onSuccess(mHealthDataBean);
        } else {
            queryUserInfo(callback);
        }
    }

    public synchronized void getHealthDataFromNet(final IHealthDataCallback callback) {
        queryUserInfo(callback);
    }

    public void queryUserInfo(final IHealthDataCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String account = SharedPreferencesDao.getInstance().getData(SPKey.KEY_LAST_ACCOUNT, "", String.class);
                String lastDeviceId = SharedPreferencesDao.getInstance().getData(SPKey.KEY_LAST_DEVICE_ID, "", String.class);
                String param = "userid=" + lastDeviceId + account;
                String result = "";
                try {
                    result = HttpRequestUtil.sendGet(AppConstants.URL_QUERY_USER_INFO, param);
                    Log.d(TAG, result);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onFailure("" + e.getMessage());
                    }
                }

                try {
                    JSONObject jsonObject = new JSONObject(result);
                    mHealthDataBean = new Gson().fromJson(jsonObject.getJSONObject("data").getJSONObject("data").toString(), HealthDataBean.class);

                    Log.d(TAG, "weight1: " + mHealthDataBean.getWeight());
                    Log.d(TAG, "height1: " + mHealthDataBean.getHeight());

                    if ("null".equals(mHealthDataBean.getWeight())) {
                        mHealthDataBean.setWeight("66.6");
                    }
                    if ("null".equals(mHealthDataBean.getHeight())) {
                        mHealthDataBean.setHeight("172.5");
                    }

                    double dWeight = Double.parseDouble(mHealthDataBean.getWeight());
                    double dHeight = Double.parseDouble(mHealthDataBean.getHeight());
                    Log.d(TAG, "weight2: " + mHealthDataBean.getWeight());
                    Log.d(TAG, "height2: " + mHealthDataBean.getHeight());

                    if (dWeight == 0d) {
                        dWeight = 66.5d;
                    }
                    if (dHeight == 0d) {
                        dHeight = 170.5d;
                    }
                    Log.d(TAG, "Weight3: " + dWeight);
                    Log.d(TAG, "Height3: " + dHeight);

                    initHealthData();
                    callback.onSuccess(mHealthDataBean);
                } catch (JSONException e) {
                    Log.e(TAG, "" + e.getMessage());
                    e.printStackTrace();
                    callback.onFailure(e.getMessage());
                } catch (NumberFormatException e) {
                    Log.e(TAG, "" + e.getMessage());
                    callback.onFailure(e.getMessage());
                } catch (Exception e) {
                    Log.e(TAG, "" + e.getMessage());
                    callback.onFailure(e.getMessage());
                }
            }
        }).start();
    }

    private void initHealthData() {
        double dWeight = Double.parseDouble(mHealthDataBean.getWeight());
        double dHeight = Double.parseDouble(mHealthDataBean.getHeight());

        double dBMI = dWeight / ((dHeight / 100) * (dHeight / 100));
        mHealthDataBean.setBMI(String.format(Locale.CHINA, "%.0f", dBMI));
        double dBust = dHeight * 0.51f * dBMI / 20f;
        mHealthDataBean.setBust(String.format(Locale.CHINA, "%.0f", dBust));
        double dWaistline = dHeight * 0.34f * dBMI / 20f;
        mHealthDataBean.setWaistline(String.format(Locale.CHINA, "%.0f", dWaistline));
        double dHipline = dHeight * 0.542f * dBMI / 20f;
        mHealthDataBean.setHipline(String.format(Locale.CHINA, "%.0f", dHipline));

    }

    private float randFloat(float min, float max, float weight) {
        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        //Random rand = new Random();
        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        //int randomNum = rand.nextInt((max - min) + 1) + min;
        return (min + (max - min) * weight / 100f);
    }

}
