package com.leaf.yeyy.weightcardio.activity.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.leaf.yeyy.weightcardio.R;
import com.leaf.yeyy.weightcardio.activity.callback.IHealthDataCallback;
import com.leaf.yeyy.weightcardio.activity.fragment.base.BaseFragment;
import com.leaf.yeyy.weightcardio.bean.HealthDataBean;

public class ChartFragment extends BaseFragment implements IHealthDataCallback {
    private static final String TAG = ChartFragment.class.getSimpleName();

    private static final String ARG_PARAM1 = "param1";
    TextView tvWater;
    TextView tvMetabolism;
    TextView tvBone;
    TextView tvFat;
    TextView tvMuscle;
    TextView tvVfat;
    TextView tvBodyImpedance;

    private String mParam1;
    private HealthDataBean mHealthDataBean;
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
                case REFRESH_COMPLETE:
                    Toast.makeText(getActivity(), "" + uiResult, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    public ChartFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment AreUSleepFragment.
     */
    public static ChartFragment newInstance(String param1) {
        ChartFragment fragment = new ChartFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
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
        View rootView = inflater.inflate(R.layout.fragment_chart, container, false);
        viewsInit(rootView);
        return rootView;
    }

    private void viewsInit(View rootView) {
        tvWater = (TextView) rootView.findViewById(R.id.tv_water);
        tvMetabolism = (TextView) rootView.findViewById(R.id.tv_metabolism);
        tvBone = (TextView) rootView.findViewById(R.id.tv_bone);
        tvFat = (TextView) rootView.findViewById(R.id.tv_fat);
        tvMuscle = (TextView) rootView.findViewById(R.id.tv_muscle);
        tvVfat = (TextView) rootView.findViewById(R.id.tv_vfat);
        tvBodyImpedance = (TextView) rootView.findViewById(R.id.tv_bodyImpedance);
        super.initViews(rootView);  //一定放在最后面来调用
    }


    @Override
    protected void lazyLoadData(boolean isForceLoad) {
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        mHealthData.getHealthDataFromNet(this);
    }

    private void showUIResult(String errMsg) {
        if (errMsg == null || errMsg.isEmpty()) {
            //显示结果
            tvWater.setText(getString(R.string.tv_water) + " " + mHealthDataBean.getWater());
            tvMetabolism.setText(getString(R.string.tv_metabolism) + " " + mHealthDataBean.getMetabolism());
            tvBone.setText(getString(R.string.tv_bone) + " " + mHealthDataBean.getBone());
            tvFat.setText(getString(R.string.tv_fat) + " " + mHealthDataBean.getFat());
            tvMuscle.setText(getString(R.string.tv_muscle) + " " + mHealthDataBean.getMuscle());
            tvVfat.setText(getString(R.string.tv_vfat) + " " + mHealthDataBean.getvFat());
            tvBodyImpedance.setText(getString(R.string.tv_bodyImpedance) + " " + mHealthDataBean.getBodyImpedance());
        } else {
            //显示错误
            Toast.makeText(getActivity(), "" + errMsg, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onSuccess(HealthDataBean healthDataBean) {
        Log.d(TAG, "Chart onSuccess");
        mHealthDataBean = healthDataBean;
        mUIHandler.obtainMessage(SUCCESS_COMPLETE, "").sendToTarget();
    }

    @Override
    public void onFailure(String errMsg) {
        Log.d(TAG, "Chart onFailure" + errMsg);
        mUIHandler.obtainMessage(FAILURE_COMPLETE, errMsg).sendToTarget();
    }
}
