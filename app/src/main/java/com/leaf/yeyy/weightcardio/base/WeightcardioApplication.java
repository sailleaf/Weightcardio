package com.leaf.yeyy.weightcardio.base;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Log;

import com.leaf.yeyy.weightcardio.preferences.SharedPreferencesDao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;


public class WeightcardioApplication extends Application {
    public static final String TAG = WeightcardioApplication.class.getSimpleName();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private boolean isDebug = false;  //App 是否是调试模式

    public static String getSignature(Context context) {
        try {
            /** 通过包管理器获得指定包名包含签名的包信息 **/
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            /******* 通过返回的包信息获得签名数组 *******/
            Signature[] signatures = packageInfo.signatures;
            /******* 循环遍历签名数组拼接应用签名 *******/
            return signatures[0].toCharsString();
            /************** 得到应用签名 **************/
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public native String getAuthKey(Context context);

    @Override
    public void onCreate() {
        super.onCreate();
        String processName = getProcessName();
        Log.d(TAG, processName + "Application onCreate");

        // 很多的东西最好能放到一个IntentService 中去初始化
        // InitializeService.start(this);
        // isDebugCheck();
        initApplication();
        String authKey = getAuthKey(getApplicationContext());
        Log.e(TAG, stringFromJNI());
        //Log.e(TAG,getAuthKey(getApplicationContext()));
        if ("error".equalsIgnoreCase(authKey)) {
            Log.e(TAG, "Something wrong!");

        } else {
            Log.d(TAG, "Application OK");
        }
        //Log.e(TAG,getSignature(getApplicationContext()));
    }

    /**
     * 根据不同的进程来初始化不同的东西
     * 比如web进程就不需要初始化推送，也不需要图片加载等等
     * <p>
     * 发新版 或 测试版也有不同的初始化
     * 比如调试工具stetho 在debug 环境是要的，Release 是不需要的
     */
    private void initApplication() {
        //部分 初始化服务最好能新开一个IntentService 去处理,bugly 在两个进程都有初始化
        SharedPreferencesDao.initSharePrefenceDao(getApplicationContext());// 初始化SP

        String processName = getProcessName();
        switch (processName) {
            case "com.leaf.yeyy.weightcardio":
                break;
            case "com.leaf.yeyy.weightcardio:webprocess":
                break;
            default:
                Log.e(TAG, "what a fatal error!");
                break;
        }
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    /**
     * 检查APP 是不是调试模式
     *
     * @return
     */
    public boolean isDebug() {
        return isDebug;
    }

    /**
     * 检查是不是Debug 模式
     */
    private void isDebugCheck() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    this.getPackageName(), PackageManager.GET_META_DATA);
            isDebug = info.applicationInfo.metaData.getBoolean("APP_DEBUG");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取进程名字
     *
     * @return
     */
    public String getProcessName() {
        try {
            File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
            BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
            String processName = mBufferedReader.readLine().trim();
            mBufferedReader.close();
            return processName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
