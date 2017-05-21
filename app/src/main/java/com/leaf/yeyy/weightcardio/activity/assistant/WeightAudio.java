package com.leaf.yeyy.weightcardio.activity.assistant;

import android.util.Log;

import com.leaf.yeyy.weightcardio.activity.callback.IActionCallback;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 支持0.00~199.99的体重数值发声辅助类
 */
public class WeightAudio {
    private static final String URL_AUDIO_PREFIX = "http://auth.api.cfcmu.cn/music/";
    private static final String dot = "\\.";
    private static String TAG = WeightAudio.class.getSimpleName();
    private static double weight;
    private static int zhengShu;
    private static int baiWei;
    private static int shiWei;
    private static int geWei;
    private static double xiaoShu;
    private static int xiaoShu1;
    private static int xiaoShu2;
    private static String strWeight;

    private WeightAudio() {
    }

    public static void play(double fWeight, IActionCallback callback) {
        if (verifyAndParseWeight(fWeight)) {
            AudioPlayer audioPlayer = new AudioPlayer(null);
            try {
                audioPlayer.playUrlList(getWeightAudioList(), callback);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private static boolean verifyAndParseWeight(double fWeight) {
        Log.d(TAG, "fWeight" + fWeight);
        if (fWeight < 0) {
            Log.e(TAG, "one wrong");
            return false;
        }

        if (fWeight > 999.99) {
            Log.e(TAG, "two wrong");
            return false;
        }
//        NumberFormat numberFormat = NumberFormat.getNumberInstance();
//        numberFormat.setMaximumFractionDigits(2);
        //Log.d(TAG, "numberFormat" + numberFormat.format(Double.valueOf("")));
        DecimalFormat decimalFormat = new DecimalFormat("0.00");//构造方法的字符格式这里如果小数不足1位,会以0补足.
        strWeight = decimalFormat.format(fWeight);//format 返回的是字符串
        Log.d(TAG, "strWeight" + strWeight);
        weight = Double.parseDouble(strWeight);
        Log.d(TAG, "weight" + weight);
        String[] fNum = strWeight.split(dot);
        Log.d(TAG, "len1" + fNum.length);
        zhengShu = Integer.parseInt(fNum[0]);
        Log.d(TAG, "zhengShu" + zhengShu);
        xiaoShu = Double.parseDouble(("0." + fNum[1]));
        Log.d(TAG, "xiaoShu" + xiaoShu);

        try {
            baiWei = zhengShu / 100;
            shiWei = (zhengShu % 100) / 10;
            geWei = zhengShu % 10;
            Log.d(TAG, "baiWei" + baiWei);
            Log.d(TAG, "shiWei" + shiWei);
            Log.d(TAG, "geWei" + geWei);
        } catch (NumberFormatException e) {
            Log.e(TAG, "three wrong");
            return false;
        }

        try {
            int temp = (int) (xiaoShu * 100);
            xiaoShu1 = temp / 10;
            xiaoShu2 = temp % 10;
            Log.d(TAG, "xiaoShu1" + xiaoShu1);
            Log.d(TAG, "xiaoShu2" + xiaoShu2);
        } catch (NumberFormatException e) {
            Log.e(TAG, "four wrong");
            return false;
        }

        return true;
    }

    private static List<String> getWeightAudioList() {
        List<String> urlList = new ArrayList<>();

        if (baiWei > 0) {
            urlList.add(URL_AUDIO_PREFIX + baiWei + "00.m4a");
        }
        if (shiWei > 1) {
            if (baiWei > 0) urlList.add(URL_AUDIO_PREFIX + "and.m4a");
            urlList.add(URL_AUDIO_PREFIX + "0" + shiWei + "0.m4a");
            if (geWei > 0) urlList.add(URL_AUDIO_PREFIX + "00" + geWei + ".m4a");
        } else if (shiWei == 1) {
            if (baiWei > 0) urlList.add(URL_AUDIO_PREFIX + "and.m4a");
            urlList.add(URL_AUDIO_PREFIX + "0" + shiWei + geWei + ".m4a");
        } else { // shiWei == 0
            if (geWei != 0) {
                if (baiWei > 0) urlList.add(URL_AUDIO_PREFIX + "and.m4a");
                urlList.add(URL_AUDIO_PREFIX + "00" + geWei + ".m4a");
            } else {
                if (baiWei == 0) urlList.add(URL_AUDIO_PREFIX + "000.m4a"); // 0.XX
            }
        }
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        if (xiaoShu > 0) {
            urlList.add(URL_AUDIO_PREFIX + "point.m4a");
            urlList.add(URL_AUDIO_PREFIX + "00" + xiaoShu1 + ".m4a");
            if (xiaoShu2 > 0) urlList.add(URL_AUDIO_PREFIX + "00" + xiaoShu2 + ".m4a");
        }
        urlList.add(URL_AUDIO_PREFIX + "kg.m4a");
        Iterator<String> iterator = urlList.iterator();
        while (iterator.hasNext()) {
            Log.d(TAG, "URL:" + iterator.next());
        }
        return urlList;
    }

}
