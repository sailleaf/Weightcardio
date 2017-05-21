package com.leaf.yeyy.weightcardio.activity.assistant;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.leaf.yeyy.weightcardio.R;
import com.leaf.yeyy.weightcardio.activity.callback.ISignCallback;
import com.leaf.yeyy.weightcardio.bean.SignInBean;
import com.leaf.yeyy.weightcardio.bean.SignInInfoBean;
import com.leaf.yeyy.weightcardio.bean.SignUpBean;
import com.leaf.yeyy.weightcardio.bean.SignUpInfoBean;
import com.leaf.yeyy.weightcardio.global.AppConstants;
import com.leaf.yeyy.weightcardio.http.HttpRequestUtil;
import com.leaf.yeyy.weightcardio.preferences.SPKey;
import com.leaf.yeyy.weightcardio.preferences.SharedPreferencesDao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Created by WIN10 on 2017/5/10.
 */

public class UserSignInAsyncTask extends AsyncTask<SignInBean, Void, String> {
    private static final String TAG = UserSignInAsyncTask.class.getSimpleName();
    private Context mContext;
    private ISignCallback mSignInCallback;
    private ProgressDialog mProgressDialog;
    private SignInBean mSignInBeen;

    public UserSignInAsyncTask(Context context, ISignCallback signInCallback) {
        this.mContext = context;
        this.mSignInCallback = signInCallback;
    }

    @Override
    protected String doInBackground(SignInBean... signInBeen) {
        mSignInBeen = signInBeen[0];
        String param = "userid=" + mSignInBeen.deviceID + mSignInBeen.userID + "&code=" + mSignInBeen.code;
        Log.d(TAG, param);
        try {
            //Thread.sleep(4000);
            return HttpRequestUtil.sendGet(AppConstants.URL_SIGN_IN, param);
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
        mProgressDialog.setMessage(mContext.getString(R.string.prompt_signing_in));
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
            SignInInfoBean signInInfoBean = new Gson().fromJson(jsonStringResult, SignInInfoBean.class);
            Log.d(TAG, "ret: " + signInInfoBean.ret + "result:" + signInInfoBean.data.result);
            if (signInInfoBean.data.result) {
                SharedPreferencesDao.getInstance().saveData(SPKey.KEY_LAST_ACCOUNT, mSignInBeen.userID);
                SharedPreferencesDao.getInstance().saveData(SPKey.KEY_LAST_DEVICE_ID, mSignInBeen.deviceID);
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                SharedPreferencesDao.getInstance().saveData(SPKey.KEY_LAST_SIGN_IN_DATE, df.format(new Date()));
                SharedPreferencesDao.getInstance().saveData(SPKey.KEY_ACCESS_TOKEN, "access token value");
                Log.d(TAG, "Sign In Success:" + df.format(new Date()));
                mSignInCallback.onSuccess(AppConstants.ACTION_SUCCESS);
            } else {
                SharedPreferencesDao.getInstance().saveData(SPKey.KEY_ACCESS_TOKEN, ""); // 清除Token信息
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                Log.d(TAG, "Sign In Failure:" + df.format(new Date()));
                mSignInCallback.onFailure(AppConstants.ACTION_FAILURE);
            }
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "JsonSyntaxException:" + e.getMessage());
            SharedPreferencesDao.getInstance().saveData(SPKey.KEY_ACCESS_TOKEN, ""); // 清除Token信息
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            Log.d(TAG, "Sign In Failure:" + df.format(new Date()));
            mSignInCallback.onFailure(AppConstants.ACTION_FAILURE);
        } catch (Exception e) {
            Log.e(TAG, "Exception:" + e.getMessage());
            SharedPreferencesDao.getInstance().saveData(SPKey.KEY_ACCESS_TOKEN, ""); // 清除Token信息
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            Log.d(TAG, "Sign In Failure:" + df.format(new Date()));
            mSignInCallback.onFailure(AppConstants.ACTION_FAILURE);
        }

    }

    @Override
    protected void onCancelled() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        mSignInCallback.onFailure(AppConstants.ACTION_FAILURE);
    }
}
