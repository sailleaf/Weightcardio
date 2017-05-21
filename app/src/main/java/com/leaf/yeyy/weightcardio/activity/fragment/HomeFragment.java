package com.leaf.yeyy.weightcardio.activity.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.leaf.yeyy.viewlib.ColorArcProgressBar;
import com.leaf.yeyy.weightcardio.R;
import com.leaf.yeyy.weightcardio.activity.assistant.WeightAudio;
import com.leaf.yeyy.weightcardio.activity.callback.IActionCallback;
import com.leaf.yeyy.weightcardio.activity.callback.IHealthDataCallback;
import com.leaf.yeyy.weightcardio.activity.fragment.base.BaseFragment;
import com.leaf.yeyy.weightcardio.bean.HealthDataBean;

public class HomeFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, IHealthDataCallback {
    private static final String TAG = HomeFragment.class.getSimpleName();
    private static final String ARG_PARAM1 = "param1";
    private String mParam1;
    private SwipeRefreshLayout mSwipeLayout;
    private ColorArcProgressBar mWeightBar;
    private ColorArcProgressBar mHeightBar;
    private double dWeight = INIT_VALUE;
    private double dHeight = INIT_VALUE;
    private Button btnPlaySound;
    private HealthDataBean mHealthDataBean;
    private Handler mUIHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            //String uiResult = msg.obj.toString();
            switch (msg.what) {
                case SUCCESS_COMPLETE:
                    showUIResult();
                    break;
                case FAILURE_COMPLETE:
                    showUIResult();
                    break;
                case PLAY_WEIGHT_SOUND:
                    playSound();
                    break;
            }
        }
    };

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment AreUSleepFragment.
     */
    public static HomeFragment newInstance(String param1) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    private void playSound() {
        if (mHealthDataBean != null) {
            Log.d(TAG, "has weight data");
            WeightAudio.play(Double.parseDouble(mHealthDataBean.getWeight()), new IActionCallback() {
                @Override
                public void onComplete() {
                    btnPlaySound.setEnabled(true);
                }
            });
        } else {
            Log.d(TAG, "no weight data");
            mHealthData.getHealthData(this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        viewsInit(rootView);
        return rootView;
    }

    /**
     * init views
     **/
    private void viewsInit(View rootView) {
        mSwipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.id_swipe_ly);
        mSwipeLayout.setOnRefreshListener(this);
        mWeightBar = (ColorArcProgressBar) rootView.findViewById(R.id.pbar_weight);
        mHeightBar = (ColorArcProgressBar) rootView.findViewById(R.id.pbar_height);
        btnPlaySound = (Button) rootView.findViewById(R.id.btn_weight_sound);
        btnPlaySound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPlaySound.setEnabled(false);
                playSound();
            }
        });
        super.initViews(rootView);  //一定放在最后面来调用
    }

    @Override
    protected void lazyLoadData(boolean isForceLoad) {
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        mHealthData.getHealthData(this);
    }

    @Override
    public void onRefresh() {
        Log.d(TAG, Thread.currentThread().getName());
        if (dWeight == INIT_VALUE || dHeight == INIT_VALUE) {
            mHealthData.getHealthData(this);
        } else {
            if (mSwipeLayout.isRefreshing()) {
                mSwipeLayout.setRefreshing(false);
            }
        }
    }

    private void showUIResult() {
        mWeightBar.setCurrentValues((float) dWeight);
        mHeightBar.setCurrentValues((float) dHeight);
        if (mSwipeLayout.isRefreshing()) {
            mSwipeLayout.setRefreshing(false);
        }
    }

    @Override
    public void onSuccess(HealthDataBean healthDataBean) {
        Log.d(TAG, "Home onSuccess");
        mHealthDataBean = healthDataBean;
        dWeight = Double.parseDouble(healthDataBean.getWeight());
        dHeight = Double.parseDouble(healthDataBean.getHeight());
        mUIHandler.obtainMessage(SUCCESS_COMPLETE).sendToTarget();
    }

    @Override
    public void onFailure(String errMsg) {
        Log.d(TAG, "Home onFailure" + errMsg);
        dWeight = 0d;
        dHeight = 0d;
        mUIHandler.obtainMessage(FAILURE_COMPLETE, errMsg).sendToTarget();
    }
}
