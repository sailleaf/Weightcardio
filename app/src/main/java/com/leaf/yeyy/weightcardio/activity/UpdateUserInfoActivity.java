package com.leaf.yeyy.weightcardio.activity;

import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.leaf.yeyy.weightcardio.R;
import com.leaf.yeyy.weightcardio.activity.assistant.UpdateUserInfoAsyncTask;
import com.leaf.yeyy.weightcardio.activity.callback.ICommonCallback;
import com.leaf.yeyy.weightcardio.base.BaseActivity;
import com.leaf.yeyy.weightcardio.bean.SignUpBean;
import com.leaf.yeyy.weightcardio.global.AppConstants;
import com.leaf.yeyy.weightcardio.preferences.SPKey;
import com.leaf.yeyy.weightcardio.preferences.SharedPreferencesDao;
import com.leaf.yeyy.weightcardio.utils.SHA1Utils;
import com.leaf.yeyy.weightcardio.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UpdateUserInfoActivity extends BaseActivity implements ICommonCallback {
    private static final String TAG = UpdateUserInfoActivity.class.getSimpleName();

    // 建立数据源
    private final String[] mSpinnerSexItems = {"Male", "Female"};

    //注册控件
    @BindView(R.id.input_update_password)
    EditText mInputUpdatePassword;
    @BindView(R.id.input_update_confirm_password)
    EditText mInputUpdateConfirmPassword;
    @BindView(R.id.input_update_height)
    EditText mInputUpdateHeight;
    @BindView(R.id.input_update_age)
    EditText mInputUpdateAge;
    ArrayAdapter<String> spinnerSexAdapter;
    @BindView(R.id.btn_update_user_info)
    AppCompatButton btnUpdateUserInfo;
    @BindView(R.id.spinner_update_sex)
    Spinner mSpinnerUpdateSex;

    private String mTargetSex;
    AdapterView.OnItemSelectedListener onSexItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            mTargetSex = mSpinnerSexItems[pos];
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private UpdateUserInfoAsyncTask mUpdateUserInfoAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
//            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
//        }
        setContentView(R.layout.activity_update_user_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_update);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        toolbar.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); //返回事件
            }
        });

        ButterKnife.bind(this);
        // 建立Adapter并且绑定数据源
        spinnerSexAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mSpinnerSexItems);
        spinnerSexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerUpdateSex.setAdapter(spinnerSexAdapter);
        mSpinnerUpdateSex.setOnItemSelectedListener(onSexItemSelectedListener);
        mSpinnerUpdateSex.setSelection(0, true);//放在setOnItemSelectedListener()方法后

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "Test");
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.btn_update_user_info)
    public void UpdateUserInfo() {
        Log.d(TAG, "Update User Info...");
        if (mUpdateUserInfoAsyncTask != null) {
            return; //Task is running
        }
        if (validateUserInfo()) {
            btnUpdateUserInfo.setEnabled(false);
            String lastDeviceId = SharedPreferencesDao.getInstance().getData(SPKey.KEY_LAST_DEVICE_ID, "", String.class);
            String account = SharedPreferencesDao.getInstance().getData(SPKey.KEY_LAST_ACCOUNT, "", String.class);
            SignUpBean signUpBean = new SignUpBean(account,
                    lastDeviceId,
                    SHA1Utils.SHA256(mInputUpdatePassword.getText().toString()),
                    mInputUpdateHeight.getText().toString(),
                    mInputUpdateAge.getText().toString(),
                    mTargetSex);
            mUpdateUserInfoAsyncTask = new UpdateUserInfoAsyncTask(this, this);
            mUpdateUserInfoAsyncTask.execute(signUpBean);
        }
    }

    /**
     * 验证注册字段是否合法
     *
     * @return
     */
    public boolean validateUserInfo() {
        boolean valid = true;
        View focusView = null;
        // Reset errors.
        mInputUpdatePassword.setError(null);
        mInputUpdateConfirmPassword.setError(null);
        mInputUpdateHeight.setError(null);
        mInputUpdateAge.setError(null);

        // Store values at the time of the login attempt.
        String password = mInputUpdatePassword.getText().toString();
        String confirmPassword = mInputUpdateConfirmPassword.getText().toString();
        String height = mInputUpdateHeight.getText().toString();
        String age = mInputUpdateAge.getText().toString();

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mInputUpdatePassword.setError(getString(R.string.error_field_required));
            focusView = mInputUpdatePassword;
            valid = false;
        } else if (password.length() < 6) {
            mInputUpdatePassword.setError(getString(R.string.error_invalid_password));
            focusView = mInputUpdatePassword;
            valid = false;
        }

        if (confirmPassword.isEmpty() || confirmPassword.length() < 6 || !(confirmPassword.equals(password))) {
            mInputUpdateConfirmPassword.setError(getString(R.string.error_confirm_password));
            focusView = mInputUpdateConfirmPassword;
            valid = false;
        }

        if (height.isEmpty()) {
            mInputUpdateHeight.setError(getString(R.string.error_invalid_height));
            focusView = mInputUpdateHeight;
            valid = false;
        }

        try {
            double dheight = Double.parseDouble(height);
            if (dheight < 10 || dheight > 300) {
                mInputUpdateHeight.setError(getString(R.string.error_invalid_height));
                focusView = mInputUpdateHeight;
                valid = false;
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "" + e.getMessage());
            mInputUpdateHeight.setError(getString(R.string.error_invalid_height));
            focusView = mInputUpdateHeight;
            valid = false;
        }

        if (age.isEmpty()) {
            mInputUpdateAge.setError(getString(R.string.error_invalid_age));
            focusView = mInputUpdateAge;
            valid = false;
        }

        try {
            int iAge = Integer.parseInt(age);
            if (iAge < 0 || iAge > 130) {
                mInputUpdateAge.setError(getString(R.string.error_invalid_age));
                focusView = mInputUpdateAge;
                valid = false;
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "" + e.getMessage());
            mInputUpdateAge.setError(getString(R.string.error_invalid_age));
            focusView = mInputUpdateAge;
            valid = false;
        }

        if (!valid && focusView != null) {
            focusView.requestFocus();
        }
        return valid;
    }

    @Override
    public void onSuccess(String msg) {
        // Close Task
        if (mUpdateUserInfoAsyncTask != null) {
            mUpdateUserInfoAsyncTask = null;
        }
        if (!btnUpdateUserInfo.isEnabled()) {
            btnUpdateUserInfo.setEnabled(true);
        }
        if (AppConstants.ACTION_SUCCESS.equals(msg)) {
            finish();
        }
    }

    @Override
    public void onFailure(String errMsg) {
        // Close Task
        // Close Task
        if (mUpdateUserInfoAsyncTask != null) {
            mUpdateUserInfoAsyncTask = null;
        }
        if (!btnUpdateUserInfo.isEnabled()) {
            btnUpdateUserInfo.setEnabled(true);
        }
        if (AppConstants.ACTION_FAILURE.equals(errMsg)) {
            ToastUtil.showToast(getApplicationContext(), "failure");
        }
    }
}