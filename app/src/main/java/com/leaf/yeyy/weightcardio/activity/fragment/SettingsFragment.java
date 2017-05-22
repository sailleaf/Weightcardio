package com.leaf.yeyy.weightcardio.activity.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.leaf.yeyy.viewlib.SettingView;
import com.leaf.yeyy.viewlib.entity.SettingData;
import com.leaf.yeyy.viewlib.entity.SettingViewItemData;
import com.leaf.yeyy.viewlib.item.BasicItemViewH;
import com.leaf.yeyy.viewlib.item.SwitchItemView;
import com.leaf.yeyy.weightcardio.R;
import com.leaf.yeyy.weightcardio.activity.SignDoorActivity;
import com.leaf.yeyy.weightcardio.activity.assistant.AudioPlayer;
import com.leaf.yeyy.weightcardio.activity.fragment.base.BaseFragment;
import com.leaf.yeyy.weightcardio.bean.HealthDataBean;
import com.leaf.yeyy.weightcardio.global.AppConstants;
import com.leaf.yeyy.weightcardio.http.HttpRequestUtil;
import com.leaf.yeyy.weightcardio.preferences.SPKey;
import com.leaf.yeyy.weightcardio.preferences.SharedPreferencesDao;
import com.leaf.yeyy.weightcardio.utils.SoundPlayUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 懒加载的实验Fragment，将会作为github me Profile 的Fragment 复用
 * <p>
 * https://github.com/SpikeKing/TestCoordinatorLayout  ：个人简介使用这种布局样式，交互样式非常的好
 *
 * @author anylife.zlb@gmail.com
 */
public class SettingsFragment extends BaseFragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String TAG = SettingsFragment.class.getSimpleName();
    private SettingView mSettingView = null;
    private SettingData mItemData = null;
    private SettingViewItemData mItemViewData = null;
    private List<SettingViewItemData> mListData = new ArrayList<>();
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private AudioPlayer mAudioPlayer;
    private SeekBar seekBarAudio;
    private HealthDataBean mHealthDataBean;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BlankFragment.
     */
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * 使用这种方式来生成的Fragment 在内存不足的时候重启后会
     * 一定会在任何情况都能恢复到离开前的页面，并且保证数据的完整性。
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, mParam1 + "   onCreate  !!!!! " + savedInstanceState);
        if (getArguments() != null) {  //在  static BlankFragment newInstance 中实例化的
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    /**
     * 当视图可见的时候就会被调用，当然在onCreateView 也会调用一次，
     */
    @Override
    protected void lazyLoadData(boolean isForceLoad) {
        if (isViewsInit && visibleTime < 1) {
            Log.e(TAG, "视图已经初始化完毕了，虽然不去加载网络数据，但是可以加载一下本地持久化的缓存数据啊！");
        }

        if (!isViewsInit || visibleTime < 1) {  //假如views 没有初始化或者Fragment不可见，那就不要尝试加载数据
            return;
        } else {
            if (isForceLoad) {
                Log.e(TAG, "前面的支付页面支付9.9，那么这里显示的剩余金额必然变动了，敏感数据，要实时刷新");
            }
            if (visibleTime == 1) { //这里也不是每次可见的时候都能刷新，只有第一次可见的时候或者数据加载从来没有成功 才调用刷新
                disposeHttpResult();
                //Toast.makeText(mActivity, "第一次可见", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG, mParam1 + "   onCreateView  " + savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        initViews(rootView);
        lazyLoadData(true);
        return rootView;
    }

    /**
     * 初始化所有的视图
     *
     * @param rootView 根视图
     */
    protected void initViews(View rootView) {
        SoundPlayUtils.init(getActivity());
        LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.me_layout);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //BaseActivity baseActivity = (BaseActivity) getActivity();
                //baseActivity.goWebView("file:///android_asset/index.html");
            }
        });
        TextView tvUserType = (TextView) rootView.findViewById(R.id.tv_head_user_type);
        TextView tvDeviceID = (TextView) rootView.findViewById(R.id.tv_head_device_id);
        tvUserType.setText(getString(R.string.head_user_type) + SharedPreferencesDao.getInstance().getData(SPKey.KEY_LAST_ACCOUNT, "", String.class));
        tvDeviceID.setText(getString(R.string.head_device_id) + SharedPreferencesDao.getInstance().getData(SPKey.KEY_LAST_DEVICE_ID, "", String.class));

        seekBarAudio = (SeekBar) rootView.findViewById(R.id.seekbar_audio);
        seekBarAudio.setOnSeekBarChangeListener(new SeekBarChangeEvent());

        Button btnExit = (Button) rootView.findViewById(R.id.btn_exit);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exit(view);
            }
        });

        mSettingView = (SettingView) rootView.findViewById(R.id.ios_style_setting_view_01);
        mSettingView.setOnSettingViewItemClickListener(new SettingView.onSettingViewItemClickListener() {
            @Override
            public void onItemClick(int index) {
                Toast.makeText(getActivity(), "#" + index + " is been clicked", Toast.LENGTH_SHORT).show();
                if (index == 2) {
                    mSettingView.modifySubTitle("hello", index);
                }
            }
        });

        mSettingView.setOnSettingViewItemSwitchListener(new SettingView.onSettingViewItemSwitchListener() {
            @Override
            public void onSwitchChanged(int index, boolean isChecked) {
                if (index == 1) {
                    if (isChecked) {
                        Toast.makeText(getActivity(), "#" + index + " open", Toast.LENGTH_SHORT).show();
                        test();
                    } else {
                        Toast.makeText(getActivity(), "#" + index + " close", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        //=======================    FBI WARMMING !    这样子是很差劲的，说明没有写好=================
        super.initViews(rootView);  //一定放在最后面来调用
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(TAG, "onStop()");
        if (mAudioPlayer != null) {
            mAudioPlayer.stop();
            mAudioPlayer = null;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * 在这里实现Fragment数据的缓加载.
     *
     * @param isVisibleToUser
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.e(TAG, "setUserVisibleHint called,------isVisibleToUser:" + isVisibleToUser);
    }


    private void disposeHttpResult() {
        /* ==========================SettingView1========================== */
        mSettingView.removeAllViews();
        mListData.clear();

        mItemViewData = new SettingViewItemData();
        mItemData = new SettingData();
        mItemData.setTitle("Alter Height");
        mItemData.setSubTitle("");
        mItemViewData.setData(mItemData);
        mItemViewData.setItemView(new BasicItemViewH(getActivity()));
        mListData.add(mItemViewData);

        mItemViewData = new SettingViewItemData();
        mItemData = new SettingData();
        mItemData.setTitle("Alter Age");
        mItemData.setSubTitle("");
        mItemViewData.setData(mItemData);
        mItemViewData.setItemView(new BasicItemViewH(getActivity()));
        mListData.add(mItemViewData);

        mItemViewData = new SettingViewItemData();
        mItemData = new SettingData();
        mItemData.setTitle("Alter Sex");
        mItemData.setSubTitle("");
        mItemViewData.setData(mItemData);
        mItemViewData.setItemView(new BasicItemViewH(getActivity()));
        mListData.add(mItemViewData);

//        mItemViewData = new SettingViewItemData();
//        mItemData = new SettingData();
//        mItemData.setTitle("Item");
//        mItemViewData.setData(mItemData);
//        mItemViewData.setItemView(new SwitchItemView(getActivity()));
//        mListData.add(mItemViewData);

        mItemViewData = new SettingViewItemData();
        mItemData = new SettingData();
        mItemData.setTitle("Hello Test");
        mItemData.setSubTitle("Go");
        //mItemData.setDrawable(getResources().getDrawable(R.drawable.main_footer_discovery_selected));
        mItemViewData.setData(mItemData);
        mItemViewData.setItemView(new BasicItemViewH(getActivity()));
        mListData.add(mItemViewData);

        mSettingView.setAdapter(mListData);
        /* ==========================SettingView1========================== */
    }

    public void exit(View view) {
        SharedPreferencesDao.getInstance().saveData(SPKey.KEY_ACCESS_TOKEN, "");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        Log.d(TAG, "exit time: " + df.format(new Date()));

        Intent intent = new Intent();
        intent.setClass(getActivity(), SignDoorActivity.class);
        getActivity().startActivity(intent);

        getActivity().overridePendingTransition(R.animator.push_left_in, R.animator.push_left_out);
        getActivity().finish();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    String url = "http://auth.api.cfcmu.cn/test/logout.json";
//                    String param = "userid=" + mUsername + mLastDeviceId;
//                    String result = HttpRequestUtil.sendGet(url, param);
//                    Log.d(TAG, result);
//                } catch (Exception ex) {
//                    Log.e(TAG, "" + ex.getMessage());
//                }
//            }
//        }).start();
    }


    public void test() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.e(TAG, "" + HttpRequestUtil.sendGet(AppConstants.URL_PLAY_SOUND));
                } catch (Exception ex) {
                    Log.e(TAG, "" + ex.getMessage());
                }
            }
        }).start();

    }

    class SeekBarChangeEvent implements SeekBar.OnSeekBarChangeListener {
        int progress;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            // 原本是(progress/seekBar.getMax())*audioPlayer.mediaPlayer.getDuration()
            this.progress = progress * mAudioPlayer.mediaPlayer.getDuration()
                    / seekBar.getMax();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // seekTo()的参数是相对与影片时间的数字，而不是与seekBar.getMax()相对的数字
            mAudioPlayer.mediaPlayer.seekTo(progress);
        }

    }

}
