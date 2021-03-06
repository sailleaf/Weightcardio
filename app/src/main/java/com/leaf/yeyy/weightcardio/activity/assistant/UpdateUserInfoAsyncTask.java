package com.leaf.yeyy.weightcardio.activity.assistant;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.leaf.yeyy.weightcardio.R;
import com.leaf.yeyy.weightcardio.activity.callback.ICommonCallback;
import com.leaf.yeyy.weightcardio.bean.SignUpBean;
import com.leaf.yeyy.weightcardio.bean.SignUpInfoBean;
import com.leaf.yeyy.weightcardio.global.AppConstants;
import com.leaf.yeyy.weightcardio.http.HttpRequestUtil;
import com.leaf.yeyy.weightcardio.preferences.SPKey;
import com.leaf.yeyy.weightcardio.preferences.SharedPreferencesDao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UpdateUserInfoAsyncTask extends AsyncTask<SignUpBean, Void, String> {
    private static final String TAG = UpdateUserInfoAsyncTask.class.getSimpleName();
    private Context mContext;
    private ICommonCallback mUpdeUserInfoCallback;
    private ProgressDialog mProgressDialog;
    private SignUpBean mSignUpBeen;

    public UpdateUserInfoAsyncTask(Context context, ICommonCallback updateUserInfoCallback) {
        this.mContext = context;
        this.mUpdeUserInfoCallback = updateUserInfoCallback;
    }

    @Override
    protected String doInBackground(SignUpBean... signUpBeen) {
        mSignUpBeen = signUpBeen[0];
        String param = "userid=" + mSignUpBeen.deviceID + mSignUpBeen.userID
                + "&code=" + mSignUpBeen.code
                + "&deviceid=" + mSignUpBeen.deviceID
                + "&height=" + mSignUpBeen.height
                + "&age=" + mSignUpBeen.age
                + "&sex=" + mSignUpBeen.sex;
        try {
            // Log.d(TAG, "param : " + param);
            return HttpRequestUtil.sendGet(AppConstants.URL_UPDATE_SIGN_UP_INFO, param);
        } catch (Exception e) {
            Log.e(TAG, "Exception:" + e.getMessage());
            return "exception " + e.getMessage();
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mContext, R.style.AppTheme_Dark_Dialog);
        }
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(mContext.getString(R.string.btn_update_user_info));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    protected void onPostExecute(final String jsonStringResult) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        Log.d(TAG, "Result: " + jsonStringResult);

        try {
            SignUpInfoBean signUpInfoBean = new Gson().fromJson(jsonStringResult, SignUpInfoBean.class);
            Log.d(TAG, "ret: " + signUpInfoBean.ret + "result:" + signUpInfoBean.data.result);
            if (signUpInfoBean.data.result) {
                SharedPreferencesDao.getInstance().saveData(SPKey.KEY_LAST_ACCOUNT, mSignUpBeen.userID);
                SharedPreferencesDao.getInstance().saveData(SPKey.KEY_LAST_DEVICE_ID, mSignUpBeen.deviceID);
                SharedPreferencesDao.getInstance().saveData(SPKey.KEY_LAST_HEIGHT, mSignUpBeen.height);
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                SharedPreferencesDao.getInstance().saveData(SPKey.KEY_LAST_SIGN_IN_DATE, df.format(new Date()));
                SharedPreferencesDao.getInstance().saveData(SPKey.KEY_ACCESS_TOKEN, "access token value");
                Log.d(TAG, "Update Success:" + df.format(new Date()));
                mUpdeUserInfoCallback.onSuccess(AppConstants.ACTION_SUCCESS);
            } else {
                SharedPreferencesDao.getInstance().saveData(SPKey.KEY_ACCESS_TOKEN, ""); // 清除Token信息
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                Log.d(TAG, "Update Failure:" + df.format(new Date()));
                mUpdeUserInfoCallback.onFailure(AppConstants.ACTION_FAILURE);
            }
        } catch (JsonSyntaxException e) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            Log.d(TAG, "Update Failure:" + df.format(new Date()));
            Log.e(TAG, "JsonSyntaxException:" + e.getMessage());
            SharedPreferencesDao.getInstance().saveData(SPKey.KEY_ACCESS_TOKEN, ""); // 清除Token信息
            mUpdeUserInfoCallback.onFailure(AppConstants.ACTION_FAILURE);
        }
    }

    @Override
    protected void onCancelled() {
        Log.d(TAG, "Update cancel");
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        mUpdeUserInfoCallback.onFailure(AppConstants.ACTION_FAILURE);
    }

}
