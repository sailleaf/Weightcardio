package com.leaf.yeyy.weightcardio.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.acker.simplezxing.activity.CaptureActivity;
import com.leaf.yeyy.weightcardio.R;
import com.leaf.yeyy.weightcardio.activity.assistant.ActivityHelper;
import com.leaf.yeyy.weightcardio.activity.assistant.UserSignInAsyncTask;
import com.leaf.yeyy.weightcardio.activity.assistant.UserSignUpAsyncTask;
import com.leaf.yeyy.weightcardio.activity.callback.ICommonCallback;
import com.leaf.yeyy.weightcardio.base.BaseActivity;
import com.leaf.yeyy.weightcardio.bean.SignInBean;
import com.leaf.yeyy.weightcardio.bean.SignUpBean;
import com.leaf.yeyy.weightcardio.global.AppConstants;
import com.leaf.yeyy.weightcardio.preferences.SPKey;
import com.leaf.yeyy.weightcardio.preferences.SharedPreferencesDao;
import com.leaf.yeyy.weightcardio.utils.SHA1Utils;
import com.leaf.yeyy.weightcardio.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignDoorActivity extends BaseActivity implements ICommonCallback {
    private static final String TAG = SignDoorActivity.class.getSimpleName();
    private static final int REQ_CODE_CAMERA_PERMISSION = 0x1111;
    private static final int REQ_CODE_SIGN_IN = 0x2222;
    private static final int REQ_CODE_SIGN_UP = 0x3333;
    private static int REQ_SCAN_TYPE;

    // 建立数据源
    private final String[] mSpinnerItems = {"Guest", "User1", "User2", "User3", "User4"};
    private final String[] mSpinnerValue = {"00", "01", "02", "03", "04"};
    private final String[] mSpinnerSexItems = {"Male", "Female"};

    @BindView(R.id.include_sign_door_content)
    View vSignDoor;
    @BindView(R.id.include_sign_in_content)
    View vSignIn;
    @BindView(R.id.include_sign_up_content)
    View vSignUp;
    @BindView(R.id.image_bg)
    View vBlackground;
    //注册控件
    @BindView(R.id.input_sign_up_device_id)
    EditText mInputSignUpDeviceID;
    @BindView(R.id.input_sign_up_password)
    EditText mInputSignUpPassword;
    @BindView(R.id.input_sign_up_confirm_password)
    EditText mInputConfirmPassword;
    @BindView(R.id.input_sign_up_height)
    EditText mInputHeight;
    @BindView(R.id.input_sign_up_age)
    EditText mInputAge;
    @BindView(R.id.btn_sign_up)
    AppCompatButton mBtnSignUp;
    @BindView(R.id.link_sign_up)
    TextView mLinkSignUp;
    @BindView(R.id.spinner_sign_up_user)
    Spinner mSpinnerSignUp;
    @BindView(R.id.spinner_sign_up_sex)
    Spinner mSpinnerSignUpSex;

    //登录控件
    @BindView(R.id.input_sign_in_device_id)
    EditText mInputSignInDeviceID;
    @BindView(R.id.input_sign_in_password)
    EditText mInputSignInPassword;
    @BindView(R.id.btn_sign_in)
    AppCompatButton mBtnSignIn;
    @BindView(R.id.spinner_sign_in_user)
    Spinner mSpinnerSignIn;
    ArrayAdapter<String> spinnerAdapter;
    ArrayAdapter<String> spinnerSexAdapter;

    private String mTargetUser;
    AdapterView.OnItemSelectedListener onUserItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            mTargetUser = mSpinnerValue[pos];
            //Toast.makeText(SignDoorActivity.this, "你点击的是:" + mTargetUser, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };
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


    private UserSignInAsyncTask mUserSignInAsyncTask;
    private UserSignUpAsyncTask mUserSignUpAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }
        setContentView(R.layout.activity_sign_door);
        ButterKnife.bind(this);
        // 建立Adapter并且绑定数据源
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mSpinnerItems);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerSexAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mSpinnerSexItems);
        spinnerSexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    }

    @OnClick(R.id.btn_sign_in)
    public void signIn() {
        Log.d(TAG, "Sign In...");
        if (mUserSignInAsyncTask != null) {
            return; //Task is running
        }
        if (validateSignIn()) {
            mBtnSignIn.setEnabled(false);
            SignInBean signInBean = new SignInBean(mTargetUser, mInputSignInDeviceID.getText().toString(), SHA1Utils.SHA256(mInputSignInPassword.getText().toString()));
            mUserSignInAsyncTask = new UserSignInAsyncTask(this, this);
            mUserSignInAsyncTask.execute(signInBean);
        }
    }

    /**
     * 验证登录字段是否合法
     *
     * @return
     */
    public boolean validateSignIn() {

        boolean valid = true;
        View focusView = null;
        // Reset errors.
        mInputSignInDeviceID.setError(null);
        mInputSignInPassword.setError(null);

        // Store values at the time of the login attempt.
        String deviceID = mInputSignInDeviceID.getText().toString();
        String password = mInputSignInPassword.getText().toString();

        // Check for a valid deviceID.
        if (TextUtils.isEmpty(deviceID)) {
            mInputSignInDeviceID.setError(getString(R.string.error_field_required));
            focusView = mInputSignInDeviceID;
            valid = false;
        } else if (deviceID.length() < 6) {
            mInputSignInDeviceID.setError(getString(R.string.error_invalid_device_id));
            focusView = mInputSignInDeviceID;
            valid = false;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mInputSignInPassword.setError(getString(R.string.error_field_required));
            focusView = mInputSignInPassword;
            valid = false;
        } else if (password.length() < 6) {
            mInputSignInPassword.setError(getString(R.string.error_invalid_password));
            focusView = mInputSignInPassword;
            valid = false;
        }
        if (!valid) {
            focusView.requestFocus();
        }
        return valid;
    }

    @OnClick(R.id.btn_sign_up)
    public void signUp() {
        Log.d(TAG, "Sign Up...");
        if (mUserSignUpAsyncTask != null) {
            return; //Task is running
        }
        if (validateSignUp()) {
            mBtnSignUp.setEnabled(false);
            SignUpBean signUpBean = new SignUpBean(mTargetUser,
                    mInputSignUpDeviceID.getText().toString(),
                    SHA1Utils.SHA256(mInputSignInPassword.getText().toString()),
                    mInputHeight.getText().toString(),
                    mInputAge.getText().toString(),
                    mTargetSex);
            mUserSignUpAsyncTask = new UserSignUpAsyncTask(this, this);
            mUserSignUpAsyncTask.execute(signUpBean);
        }

    }

    /**
     * 验证注册字段是否合法
     *
     * @return
     */
    public boolean validateSignUp() {
        boolean valid = true;
        View focusView = null;
        // Reset errors.
        mInputSignUpDeviceID.setError(null);
        mInputSignUpPassword.setError(null);
        mInputConfirmPassword.setError(null);
        mInputHeight.setError(null);

        // Store values at the time of the login attempt.
        String deviceID = mInputSignUpDeviceID.getText().toString();
        String password = mInputSignUpPassword.getText().toString();
        String confirmPassword = mInputConfirmPassword.getText().toString();
        String height = mInputHeight.getText().toString();

        // Check for a valid deviceID.
        if (TextUtils.isEmpty(deviceID)) {
            mInputSignUpDeviceID.setError(getString(R.string.error_field_required));
            focusView = mInputSignUpDeviceID;
            valid = false;
        } else if (deviceID.length() < 6) {
            mInputSignUpDeviceID.setError(getString(R.string.error_invalid_device_id));
            focusView = mInputSignUpDeviceID;
            valid = false;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mInputSignUpPassword.setError(getString(R.string.error_field_required));
            focusView = mInputSignUpPassword;
            valid = false;
        } else if (password.length() < 6) {
            mInputSignUpPassword.setError(getString(R.string.error_invalid_password));
            focusView = mInputSignUpPassword;
            valid = false;
        }

        if (confirmPassword.isEmpty() || confirmPassword.length() < 6 || !(confirmPassword.equals(password))) {
            mInputConfirmPassword.setError(getString(R.string.error_confirm_password));
            focusView = mInputSignUpPassword;
            valid = false;
        }

        if (height.isEmpty()) {
            mInputHeight.setError(getString(R.string.error_invalid_height));
            focusView = mInputHeight;
            valid = false;
        }

        try {
            double dheight = Double.parseDouble(height);
            if (dheight < 10 || dheight > 300) {
                mInputHeight.setError(getString(R.string.error_invalid_height));
                focusView = mInputHeight;
                valid = false;
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "" + e.getMessage());
            mInputHeight.setError(getString(R.string.error_invalid_height));
            focusView = mInputHeight;
            valid = false;
        }

        if (!valid && focusView != null) {
            focusView.requestFocus();
        }
        return valid;
    }


    @OnClick({R.id.btn_goto_sign_in, R.id.btn_goto_sign_up, R.id.link_sign_in, R.id.link_sign_up})
    public void gotoTargetView(View view) {
        View vBg = findViewById(R.id.image_bg);//找到你要设透明背景的layout 的id
        ActivityHelper.changeAlpha(vBg, true);
        if (view.getId() == R.id.btn_goto_sign_in) {
            switchTo("sign in");
        } else if (view.getId() == R.id.btn_goto_sign_up) {
            switchTo("sign up");
        } else if (view.getId() == R.id.link_sign_in) {
            switchTo("sign in");
        } else if (view.getId() == R.id.link_sign_up) {
            switchTo("sign up");
        }
    }

    /**
     * 控件组跳转
     *
     * @param target
     */
    private void switchTo(String target) {
        if ("sign in".equals(target)) {
            vSignDoor.setVisibility(View.GONE);
            vSignIn.setVisibility(View.VISIBLE);
            vSignUp.setVisibility(View.GONE);
            if (!SharedPreferencesDao.getInstance().getData(SPKey.KEY_LAST_DEVICE_ID, "", String.class).isEmpty()) {
                mInputSignInDeviceID.setText(SharedPreferencesDao.getInstance().getData(SPKey.KEY_LAST_DEVICE_ID, "", String.class));
            }
            mSpinnerSignIn.setAdapter(spinnerAdapter);
            mSpinnerSignIn.setOnItemSelectedListener(onUserItemSelectedListener);
            mSpinnerSignIn.setSelection(0, true);//放在setOnItemSelectedListener()方法后
        } else if ("sign up".equals(target)) {
            vSignDoor.setVisibility(View.GONE);
            vSignIn.setVisibility(View.GONE);
            vSignUp.setVisibility(View.VISIBLE);
            mSpinnerSignUp.setAdapter(spinnerAdapter);
            mSpinnerSignUp.setOnItemSelectedListener(onUserItemSelectedListener);
            mSpinnerSignUp.setSelection(0, true);//放在setOnItemSelectedListener()方法后

            mSpinnerSignUpSex.setAdapter(spinnerSexAdapter);
            mSpinnerSignUpSex.setOnItemSelectedListener(onSexItemSelectedListener);
            mSpinnerSignUpSex.setSelection(0, true);//放在setOnItemSelectedListener()方法后

        } else {
            vSignDoor.setVisibility(View.VISIBLE);
            vSignIn.setVisibility(View.GONE);
            vSignUp.setVisibility(View.GONE);
        }
    }

    /**
     * 监听Back键按下事件方法
     * 注意:返回值表示是否能完全处理该事件,在此处返回false,会继续传播该事件.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Log.d(TAG, "onKeyDown()");
            ActivityHelper.changeAlpha(vBlackground, false);
            if (vSignIn.getVisibility() == View.VISIBLE || vSignUp.getVisibility() == View.VISIBLE) {
                switchTo("sign door");
            } else {
                showExitAlert();
                return false;
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }

    private void showExitAlert() {
        new AlertDialog.Builder(this)
                .setMessage("Confirm to exit weightcardio?")
                .setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                finish();
                            }
                        })
                .show();
    }

    @OnClick({R.id.btn_goto_sign_in_scan, R.id.btn_goto_sign_up_scan})
    public void gotoScan(View scanButton) {
        if (scanButton.getId() == R.id.btn_goto_sign_in_scan) {
            REQ_SCAN_TYPE = REQ_CODE_SIGN_IN;
        } else {
            REQ_SCAN_TYPE = REQ_CODE_SIGN_UP;
        }
        // Open Scan Activity
        if (ContextCompat.checkSelfPermission(SignDoorActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Do not have the permission of camera, request it.
            ActivityCompat.requestPermissions(SignDoorActivity.this, new String[]{Manifest.permission.CAMERA}, REQ_CODE_CAMERA_PERMISSION);
        } else {
            // Have gotten the permission
            startCaptureActivityForResult();
        }
    }

    private void startCaptureActivityForResult() {
        Intent intent = new Intent(SignDoorActivity.this, CaptureActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean(CaptureActivity.KEY_NEED_BEEP, CaptureActivity.VALUE_BEEP);
        bundle.putBoolean(CaptureActivity.KEY_NEED_VIBRATION, CaptureActivity.VALUE_VIBRATION);
        bundle.putBoolean(CaptureActivity.KEY_NEED_EXPOSURE, CaptureActivity.VALUE_NO_EXPOSURE);
        bundle.putByte(CaptureActivity.KEY_FLASHLIGHT_MODE, CaptureActivity.VALUE_FLASHLIGHT_OFF);
        bundle.putByte(CaptureActivity.KEY_ORIENTATION_MODE, CaptureActivity.VALUE_ORIENTATION_AUTO);
        intent.putExtra(CaptureActivity.EXTRA_SETTING_BUNDLE, bundle);
        startActivityForResult(intent, CaptureActivity.REQ_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_CODE_CAMERA_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // User agree the permission
                    startCaptureActivityForResult();
                } else {
                    // User disagree the permission
                    Toast.makeText(this, "You must agree the camera permission request before you use the code scan function", Toast.LENGTH_LONG).show();
                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CaptureActivity.REQ_CODE:
                switch (resultCode) {
                    case RESULT_OK:
                        if (REQ_SCAN_TYPE == REQ_CODE_SIGN_IN) {
                            mInputSignInDeviceID.setText(data.getStringExtra(CaptureActivity.EXTRA_SCAN_RESULT));
                        } else {
                            mInputSignUpDeviceID.setText(data.getStringExtra(CaptureActivity.EXTRA_SCAN_RESULT));
                        }
                        Log.e(TAG, data.getStringExtra(CaptureActivity.EXTRA_SCAN_RESULT));
                        break;
                    case RESULT_CANCELED:
                        if (data != null) {
                            // for some reason camera is not working correctly
                            Log.e(TAG, data.getStringExtra(CaptureActivity.EXTRA_SCAN_RESULT));
                        }
                        break;
                }
                break;
        }
    }

    @Override
    public void onSuccess(String msg) {
        // Close Task
        if (mUserSignInAsyncTask != null) {
            mUserSignInAsyncTask = null;
        }
        if (mUserSignUpAsyncTask != null) {
            mUserSignUpAsyncTask = null;
        }
        if (!mBtnSignIn.isEnabled()) {
            mBtnSignIn.setEnabled(true);
        }
        if (!mBtnSignUp.isEnabled()) {
            mBtnSignUp.setEnabled(true);
        }
        if (AppConstants.ACTION_SUCCESS.equals(msg)) {
            startActivity(MainActivity.class);
            finish();
        }
    }

    @Override
    public void onFailure(String errMsg) {
        // Close Task
        if (mUserSignInAsyncTask != null) {
            mUserSignInAsyncTask = null;
        }
        if (mUserSignUpAsyncTask != null) {
            mUserSignUpAsyncTask = null;
        }
        if (!mBtnSignIn.isEnabled()) {
            mBtnSignIn.setEnabled(true);
        }
        if (!mBtnSignUp.isEnabled()) {
            mBtnSignUp.setEnabled(true);
        }
        if (AppConstants.ACTION_FAILURE.equals(errMsg)) {
            ToastUtil.showToast(getApplicationContext(), "failure");
        }
    }
}