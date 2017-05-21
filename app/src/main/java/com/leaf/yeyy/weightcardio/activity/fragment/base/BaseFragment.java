package com.leaf.yeyy.weightcardio.activity.fragment.base;

import android.support.annotation.CallSuper;
import android.support.v4.app.Fragment;
import android.view.View;

import com.leaf.yeyy.weightcardio.activity.assistant.HealthData;

/**
 * Created by WIN10 on 2017/5/12.
 */

public abstract class BaseFragment extends Fragment {
    protected static final double INIT_VALUE = -1.1d;
    protected static final int REFRESH_COMPLETE = 0x0000;
    protected static final int SUCCESS_COMPLETE = 0x1111;
    protected static final int FAILURE_COMPLETE = 0x2222;
    protected static final int PLAY_WEIGHT_SOUND = 0x3333;
    private static final String TAG = BaseFragment.class.getSimpleName();
    //保证Fragment即使在onDetach后，仍持有Activity的引用（有引起内存泄露的风险，但是相比空指针闪退，这种做法“安全”些）
    protected int visibleTime = 0;         //Fragment 可见的次数，只需要区分 0,1，>1次就行。
    protected boolean isViewsInit = false; //标示View 是否初始化完毕了
    protected HealthData mHealthData = HealthData.getInstance();

    /**
     * 会先于onCreate 和onCreateView 执行
     *
     * @param isVisibleToUser
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) { //如果是可见
            visibleTime++;
            onVisible();
        } else {
            onInvisible();
        }
    }

    /**
     * 一定要super，放在最后面的一行代码来Super!
     */
    @CallSuper
    protected void initViews(View rootView) {
        isViewsInit = true;
    }

    /**
     * 选择性的实现懒加载方案，不是所有的Fragment 都需要懒加载的
     */
    protected abstract void lazyLoadData(boolean isForceLoad);

    /**
     * Fragment 可见的时候调用尝试调用加载数据，
     */
    protected void onVisible() {
        lazyLoadData(false);
    }

    /**
     * Fragment 不可见的时候调用，选择性的使用，可以基本不用
     */
    protected void onInvisible() {
    }

}
