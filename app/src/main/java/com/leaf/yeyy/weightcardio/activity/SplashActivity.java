package com.leaf.yeyy.weightcardio.activity;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;

import com.leaf.yeyy.weightcardio.R;
import com.leaf.yeyy.weightcardio.base.BaseActivity;
import com.leaf.yeyy.weightcardio.global.AppConstants;
import com.leaf.yeyy.weightcardio.preferences.SPKey;
import com.leaf.yeyy.weightcardio.preferences.SharedPreferencesDao;


/**
 * 启动页面Activity
 */
public class SplashActivity extends BaseActivity {
    private static final String TAG = SplashActivity.class.getSimpleName();

    private Handler UiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    startActivity(SignDoorActivity.class);
                    overridePendingTransition(R.animator.push_left_in, R.animator.push_left_out);
                    SplashActivity.this.finish();
                    break;
                case 1:
                    startActivity(MainActivity.class);
                    overridePendingTransition(R.animator.push_left_in, R.animator.push_left_out);
                    SplashActivity.this.finish();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }
        // 判断是否是第一次开启应用
        String firstOpen = SharedPreferencesDao.getInstance().getData(AppConstants.FIRST_OPEN, "", String.class);
        //firstOpen = "";
        // 如果是第一次启动，则先进入功能引导页
        if (firstOpen.isEmpty()) {
            startActivity(WelcomeGuideActivity.class);
            overridePendingTransition(R.animator.push_left_in, R.animator.push_left_out);
            finish();
            return;
        }

        // 如果不是第一次启动app，则正常显示启动屏
        setContentView(R.layout.activity_splash);
        UiHandler.sendEmptyMessageDelayed(directToActivity(), 1000);
        Log.e(TAG, "Splash Start! ");
    }

    private int directToActivity() {
        int signal;
        String username = SharedPreferencesDao.getInstance().getData(SPKey.KEY_LAST_ACCOUNT, "", String.class);
        String lastSignInDate = SharedPreferencesDao.getInstance().getData(SPKey.KEY_LAST_SIGN_IN_DATE, "", String.class);
        String lastDeviceId = SharedPreferencesDao.getInstance().getData(SPKey.KEY_LAST_DEVICE_ID, "", String.class);
        String token = SharedPreferencesDao.getInstance().getData(SPKey.KEY_ACCESS_TOKEN, "", String.class);
        if (username.isEmpty() || lastSignInDate.isEmpty() || lastDeviceId.isEmpty() || token.isEmpty()) {
            signal = 0;
        } else {
            signal = 1;
        }
        return signal;
    }

}
