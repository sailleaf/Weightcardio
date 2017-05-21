package com.leaf.yeyy.weightcardio.activity.assistant;

import android.view.View;
import android.view.animation.AlphaAnimation;

/**
 * Created by WIN10 on 2017/5/10.
 */

public class ActivityHelper {
    public static void changeAlpha(View view, boolean alpha) {
        if (alpha) {
            AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0.2f);//初始化操作，参数传入0和1，即由透明度0变化到透明度为1
            view.startAnimation(alphaAnimation);//开始动画
            alphaAnimation.setFillAfter(true);//动画结束后保持状态
            alphaAnimation.setDuration(300);//动画持续时间，单位为毫秒
        } else {
            AlphaAnimation alphaAnimation = new AlphaAnimation(0.2f, 1f);//初始化操作，参数传入1和0，即由透明度1变化到透明度为0
            view.startAnimation(alphaAnimation);//开始动画
            alphaAnimation.setFillAfter(true);//动画结束后保持状态
            alphaAnimation.setDuration(300);//动画持续时间
        }
    }
}
