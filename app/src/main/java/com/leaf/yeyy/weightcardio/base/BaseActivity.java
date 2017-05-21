package com.leaf.yeyy.weightcardio.base;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * 基类就只做基类的事情,不要把业务层面的代码写到这里来
 */
public class BaseActivity extends AppCompatActivity {
    private static final String TAG = BaseActivity.class.getSimpleName();

    /*
     * Activity的跳转
	 */
    public void startActivity(Class<?> cla) {
        Log.d(TAG, "Start Activity:" + cla.getSimpleName());
        Intent intent = new Intent();
        intent.setClass(this, cla);
        startActivity(intent);
        //overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

}
