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

public class MeasureFragment extends BaseFragment implements IHealthDataCallback {
    private static final String TAG = MeasureFragment.class.getSimpleName();
    private static final String ARG_PARAM1 = "param1";
    private String mParam1;
    private HealthDataBean mHealthDataBean;

    private TextView tvBust, tvWaistline, tvHipline;

    private Handler mUIHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            String rs = msg.obj.toString();
            switch (msg.what) {
                case SUCCESS_COMPLETE:
                    showUIResult(rs);
                    break;
                case FAILURE_COMPLETE:
                    showUIResult(rs);
                    break;
            }
        }
    };

    public MeasureFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment AreUSleepFragment.
     */
    public static MeasureFragment newInstance(String param1) {
        MeasureFragment fragment = new MeasureFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_measure, container, false);
        viewsInit(rootView);
        return rootView;
    }

    /**
     * init views
     **/
    private void viewsInit(View rootView) {
        tvBust = (TextView) rootView.findViewById(R.id.tvBust);
        tvWaistline = (TextView) rootView.findViewById(R.id.tvWaistline);
        tvHipline = (TextView) rootView.findViewById(R.id.tvHipline);
        //mUsername = SharedPreferencesDao.getInstance().getData(SPKey.KEY_LAST_ACCOUNT, "", String.class);
        super.initViews(rootView);  //一定放在最后面来调用
    }


    @Override
    protected void onVisible() {
        super.onVisible();
        mHealthData.getHealthDataFromNet(this);
    }

    @Override
    protected void lazyLoadData(boolean isForceLoad) {
    }

    private void showUIResult(String msg) {
        if ("succ".equals(msg)) {
            tvBust.setText(getActivity().getString(R.string.main_nav_bust_text) + mHealthDataBean.getBust());
            tvWaistline.setText(getActivity().getString(R.string.main_nav_waistline_text) + mHealthDataBean.getWaistline());
            tvHipline.setText(getActivity().getString(R.string.main_nav_hipline_text) + mHealthDataBean.getHipline());
        } else {
            Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSuccess(HealthDataBean healthDataBean) {
        Log.d(TAG, "Measure onSuccess");
        mHealthDataBean = healthDataBean;
        mUIHandler.obtainMessage(SUCCESS_COMPLETE, "succ").sendToTarget();
    }

    @Override
    public void onFailure(String errMsg) {
        Log.d(TAG, "Measure onFailure" + errMsg);
        mUIHandler.obtainMessage(FAILURE_COMPLETE, errMsg).sendToTarget();
    }
}
