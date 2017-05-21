package com.leaf.yeyy.weightcardio.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.leaf.yeyy.weightcardio.R;
import com.leaf.yeyy.weightcardio.activity.adapter.ViewPagerAdapter;
import com.leaf.yeyy.weightcardio.activity.assistant.BottomNavigationViewHelper;
import com.leaf.yeyy.weightcardio.activity.fragment.ChartFragment;
import com.leaf.yeyy.weightcardio.activity.fragment.HomeFragment;
import com.leaf.yeyy.weightcardio.activity.fragment.MeasureFragment;
import com.leaf.yeyy.weightcardio.activity.fragment.SettingsFragment;
import com.leaf.yeyy.weightcardio.base.BaseActivity;
import com.leaf.yeyy.weightcardio.http.HttpRequestUtil;
import com.leaf.yeyy.weightcardio.preferences.SPKey;
import com.leaf.yeyy.weightcardio.preferences.SharedPreferencesDao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {
    private static String TAG = MainActivity.class.getSimpleName();
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private ViewPager viewPager;
    private MenuItem menuItem;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    viewPager.setCurrentItem(0);
                    return true;
                case R.id.navigation_measure:
                    viewPager.setCurrentItem(1);
                    return true;
                case R.id.navigation_chart:
                    viewPager.setCurrentItem(2);
                    return true;
                case R.id.navigation_settings:
                    viewPager.setCurrentItem(3);
                    return true;

            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_navi);
        ButterKnife.bind(this);
        initViews();
    }

    public void initViews() {
        toolbar.setTitleTextColor(getResources().getColor(R.color.white)); //标题栏白色
        toolbar.setTitle(getString(R.string.app_name));
        final BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(navigation);

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setAccessibilityLiveRegion(BottomNavigationView.ACCESSIBILITY_LIVE_REGION_ASSERTIVE);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Log.d(TAG, "onPageScrolled =" + position);
            }

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected =" + position);
                //setTitle(position),
                if (menuItem != null) {
                    menuItem.setChecked(false);
                } else {
                    navigation.getMenu().getItem(0).setChecked(false);
                }
                menuItem = navigation.getMenu().getItem(position);
                menuItem.setChecked(true);

                switch (position) {
                    case 0:
                        viewPager.setCurrentItem(0);
                        toolbar.setTitle(getString(R.string.app_name));
                        break;
                    case 1:
                        viewPager.setCurrentItem(1);
                        toolbar.setTitle(getString(R.string.menu_measure));
                        break;
                    case 2:
                        viewPager.setCurrentItem(2);
                        toolbar.setTitle(getString(R.string.menu_chart));
                        break;
                    case 3:
                        viewPager.setCurrentItem(3);
                        toolbar.setTitle(getString(R.string.menu_settings));
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d(TAG, "onPageScrollStateChanged =" + state);
            }
        });

        //禁止ViewPager滑动
//        viewPager.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (viewPager.getCurrentItem() == 1) {
//                    return true;
//                } else {
//                    return false;
//                }
//            }
//        });

        setupViewPager(viewPager);
        viewPager.setOffscreenPageLimit(4); //123456789--97534567
    }

    private ChartFragment mChartFragment;

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(HomeFragment.newInstance("home"));
        adapter.addFragment(MeasureFragment.newInstance("measure"));
        mChartFragment = ChartFragment.newInstance("chart");
        adapter.addFragment(mChartFragment);
        adapter.addFragment(SettingsFragment.newInstance("settings", "444444"));
        viewPager.setAdapter(adapter);
    }

    public void exit(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = "http://auth.api.cfcmu.cn/test/logout.json";
                    String lastDeviceId = SharedPreferencesDao.getInstance().getData(SPKey.KEY_LAST_DEVICE_ID, "", String.class);
                    String account = SharedPreferencesDao.getInstance().getData(SPKey.KEY_LAST_ACCOUNT, "", String.class);

                    String param = "userid=" + lastDeviceId + account;
                    String result = HttpRequestUtil.sendGet(url, param);
                    Log.d(TAG, result);
                } catch (Exception ex) {
                    Log.e(TAG, "" + ex.getMessage());
                }
            }
        }).start();

        SharedPreferencesDao.getInstance().saveData(SPKey.KEY_ACCESS_TOKEN, "");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        Log.d(TAG, "exit time: " + df.format(new Date()));
        startActivity(SignDoorActivity.class);
        overridePendingTransition(R.animator.push_left_in, R.animator.push_left_out);
        finish();
    }

}
