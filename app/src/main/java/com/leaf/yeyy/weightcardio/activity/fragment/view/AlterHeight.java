package com.leaf.yeyy.weightcardio.activity.fragment.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.leaf.yeyy.weightcardio.R;
import com.leaf.yeyy.weightcardio.global.AppConstants;
import com.leaf.yeyy.weightcardio.http.HttpRequestUtil;
import com.leaf.yeyy.weightcardio.preferences.SPKey;
import com.leaf.yeyy.weightcardio.preferences.SharedPreferencesDao;

public class AlterHeight {
    private static final int SUCCESS_COMPLETE = 0x1111;
    private static final int FAILURE_COMPLETE = 0x2222;
    private static final String TAG = AlterHeight.class.getSimpleName();
    private EditText etAlterHeight;
    private Context mContext;
    private ProgressDialog mProgressDialog;
    private Handler mUIHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            String uiResult = msg.obj.toString();
            switch (msg.what) {
                case SUCCESS_COMPLETE:
                    showUIResult(uiResult);
                    break;
                case FAILURE_COMPLETE:
                    showUIResult(uiResult);
                    break;
            }
        }
    };

    public AlterHeight() {
    }

    public AlertDialog.Builder getDialog(final Activity activity) {
        //其实对话框里面很多东西都是可以自定义的
        //  1.  图标  2.  整个布局
        mContext = activity;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);  //先得到构造器
        builder.setTitle("Update Height");             //设置标题
        builder.setIcon(R.mipmap.ic_launcher);//设置图标，图片id即可

        //  载入布局
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_alert_height, null);
        builder.setView(layout);

        //  对布局中的控件监听
        etAlterHeight = (EditText) layout.findViewById(R.id.input_alter_height);

        //  确认按钮
        builder.setPositiveButton("Commit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "Commit");
                onPreExecute();
                updateRegisterInfo();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onPostExecute();
                dialog.dismiss();
            }
        });

        builder.setCancelable(false);
        //  显示 builder.create().show();
        return builder;
    }

    private void showUIResult(String msg) {
        Toast.makeText(mContext, "" + msg, Toast.LENGTH_LONG).show();
        onPostExecute();
    }

    private void onPreExecute() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mContext, R.style.AppTheme_Dark_Dialog);
        }
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(mContext.getString(R.string.prompt_signing_in));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    private void onPostExecute() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }


    private void updateRegisterInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String account = SharedPreferencesDao.getInstance().getData(SPKey.KEY_LAST_ACCOUNT, "", String.class);
                    String lastDeviceId = SharedPreferencesDao.getInstance().getData(SPKey.KEY_LAST_DEVICE_ID, "", String.class);
                    String param = "userid=" + lastDeviceId + account + "&height="
                            + etAlterHeight.getText().toString();

                    Log.e(TAG, "" + param);
                    String rs = HttpRequestUtil.sendGet(AppConstants.URL_UPDATE_SIGN_UP_INFO, param);
                    Log.e(TAG, "" + rs);
                    mUIHandler.obtainMessage(SUCCESS_COMPLETE, "Alter Height Successfully").sendToTarget();
                } catch (Exception ex) {
                    Log.e(TAG, "" + ex.getMessage());
                    mUIHandler.obtainMessage(FAILURE_COMPLETE, ex.getMessage()).sendToTarget();
                }
            }
        }).start();
    }

}
