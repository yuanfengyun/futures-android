package com.shinnytech.futures.controller.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import androidx.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.sfit.ctp.info.DeviceInfoManager;
import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.constants.SettingConstants;
import com.shinnytech.futures.databinding.ActivityLoginBinding;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.Base64;
import com.shinnytech.futures.utils.NetworkUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.utils.SystemUtils;
import com.shinnytech.futures.utils.TimeUtils;
import com.shinnytech.futures.utils.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Random;

import static android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS;
import static com.shinnytech.futures.application.BaseApplication.TD_BROADCAST_ACTION;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_LOGIN_TYPE;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_LOGIN_TYPE_VALUE_AUTO;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_LOGIN_TYPE_VALUE_LOGIN;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_LOGIN_TYPE_VALUE_VISIT;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PAGE_ID;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PAGE_ID_VALUE_LOGIN;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_SOURCE;
import static com.shinnytech.futures.constants.AmpConstants.AMP_LOGIN;
import static com.shinnytech.futures.constants.AmpConstants.AMP_SHOW_PAGE;
import static com.shinnytech.futures.constants.AmpConstants.AMP_USER_LOGIN_TIME_FIRST;
import static com.shinnytech.futures.constants.BroadcastConstants.TD_MESSAGE_LOGIN_TIMEOUT;
import static com.shinnytech.futures.constants.CommonConstants.BROKER_ID_SIMNOW;
import static com.shinnytech.futures.constants.CommonConstants.BROKER_ID_SIMULATION;
import static com.shinnytech.futures.constants.CommonConstants.BROKER_ID_VISITOR;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_ACCOUNT;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_BROKER;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_INIT_TIME;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_LOGIN_DATE;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_PASSWORD;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_SYSTEM_INFO;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_VERSION_CODE;
import static com.shinnytech.futures.constants.CommonConstants.LOGIN_ACTIVITY_TO_BROKER_LIST_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.LOGIN_ACTIVITY_TO_CHANGE_PASSWORD_ACTIVITY;
import static com.shinnytech.futures.constants.BroadcastConstants.TD_MESSAGE_BROKER_INFO;
import static com.shinnytech.futures.constants.BroadcastConstants.TD_MESSAGE_LOGIN_FAIL;
import static com.shinnytech.futures.constants.BroadcastConstants.TD_MESSAGE_LOGIN_SUCCEED;
import static com.shinnytech.futures.constants.BroadcastConstants.TD_MESSAGE_WEAK_PASSWORD;
import static com.shinnytech.futures.utils.ScreenUtils.getStatusBarHeight;

public class LoginActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST = 1;
    private static final int LOGIN_SUCCESS = 2;
    private static final int LOGIN_FAIL = 3;
    private static final int LOGIN_TO_CHANGE_PASSWORD = 4;
    private static final int LOGIN_TIMEOUT = 5;
    private static final int EXIT_APP = 6;
    private static final int MY_PERMISSIONS_REQUEST_DENIED = 7;
    protected Context sContext;
    protected DataManager sDataManager;
    /**
     * date: 7/7/17
     * description: 用户登录监听广播
     */
    private BroadcastReceiver mReceiverLogin;
    private String mBrokerName;
    private String mPhoneNumber;
    private Handler mHandler;
    private ActivityLoginBinding mBinding;
    private String mPassword;
    private long mExitTime;
    private boolean mIsFirm;
    private boolean mIsLoginEnable;
    private boolean mIsVisitEnable;
    private boolean mIsShowPassword;
    private Dialog mLoginDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        initData();
        initEvent();
        checkResponsibility();
        checkPermissions();
        initBrokerAccount();
        registerBroaderCast();
    }

    private void initData() {
        sContext = BaseApplication.getContext();
        sDataManager = DataManager.getInstance();
        mHandler = new MyHandler(this);
        mIsLoginEnable = true;
        mIsVisitEnable = true;
        mExitTime = 0;
        mIsShowPassword = false;
        sDataManager.LOGIN_TYPE = AMP_EVENT_LOGIN_TYPE_VALUE_AUTO;

        mLoginDialog = new Dialog(this, R.style.Theme_Light_Dialog);
        View dialogView = View.inflate(this, R.layout.view_dialog_init_optional, null);
        Window dialogWindow = mLoginDialog.getWindow();
        if (dialogWindow != null) {
            dialogWindow.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            dialogWindow.setGravity(Gravity.CENTER);
            lp.width = (int) getResources().getDimension(R.dimen.optional_dialog_width);
            lp.height = (int) getResources().getDimension(R.dimen.optional_dialog_height);
            dialogWindow.setAttributes(lp);
        }
        mLoginDialog.setContentView(dialogView);
        mLoginDialog.setCancelable(false);
        TextView hint = dialogView.findViewById(R.id.dialog_hint);
        hint.setText("登录进行中，请稍等片刻......");
    }

    private void initBrokerAccount() {
        switchFirm();
    }

    private void initEvent() {
        mBinding.visitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideHint();
                Intent intent1 = new Intent(LoginActivity.this, MainActivity.class);
                LoginActivity.this.startActivity(intent1);
                LoginActivity.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                LoginActivity.this.finish();
              }
        });

        mBinding.broker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideHint();
                try {
                    String broker = mBinding.broker.getText().toString();
                    Intent intentBroker = new Intent(LoginActivity.this, BrokerListActivity.class);
                    intentBroker.putExtra("broker", broker);
                    startActivityForResult(intentBroker, LOGIN_ACTIVITY_TO_BROKER_LIST_ACTIVITY);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mBinding.selectBroker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideHint();
                try {
                    String broker = mBinding.broker.getText().toString();
                    Intent intentBroker = new Intent(LoginActivity.this, BrokerListActivity.class);
                    intentBroker.putExtra("broker", broker);
                    startActivityForResult(intentBroker, LOGIN_ACTIVITY_TO_BROKER_LIST_ACTIVITY);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mBinding.deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideHint();
                mBinding.account.getEditableText().clear();
            }
        });

        mBinding.showPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideHint();
                if (!mIsShowPassword) {
                    mBinding.password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    mIsShowPassword = true;
                    mBinding.showPassword.setImageDrawable(getResources().getDrawable(R.mipmap.ic_visibility_white_18dp));
                }else {
                    mBinding.password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    mIsShowPassword = false;
                    mBinding.showPassword.setImageDrawable(getResources().getDrawable(R.mipmap.ic_visibility_off_white_18dp));
                }
                mBinding.password.setSelection(mBinding.password.getText().toString().length());
            }
        });

        mBinding.account.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    mBinding.deleteAccount.setVisibility(View.INVISIBLE);
                } else {
                    mBinding.deleteAccount.setVisibility(View.VISIBLE);
                }

            }
        });

        mBinding.account.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mBinding.llAccount.setBackgroundResource(R.drawable.activity_login_rectangle_border_focused);
                } else {
                    mBinding.llAccount.setBackgroundResource(R.drawable.activity_login_rectangle_border);
                }
            }
        });

        mBinding.password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mBinding.llPassword.setBackgroundResource(R.drawable.activity_login_rectangle_border_focused);
                } else {
                    mBinding.llPassword.setBackgroundResource(R.drawable.activity_login_rectangle_border);
                }
            }
        });

        //点击登录
        mBinding.buttonIdLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideHint();
                attemptLogin();
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                ToastUtils.showToast(BaseApplication.getContext(), getString(R.string.main_activity_exit));
                mExitTime = System.currentTimeMillis();
            } else {
                mHandler.sendEmptyMessage(EXIT_APP);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNetwork();
        sDataManager.SOURCE = AMP_EVENT_PAGE_ID_VALUE_LOGIN;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverLogin != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverLogin);
    }

    private void attemptLogin() {

        mBinding.password.setError(null);
        mBinding.account.setError(null);

        if (mIsFirm) mBrokerName = mBinding.broker.getText().toString();
        else mBrokerName = BROKER_ID_SIMULATION;
        mPhoneNumber = mBinding.account.getText().toString();
        mPassword = mBinding.password.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(mPassword)) {
            mBinding.password.setError(getString(R.string.login_activity_error_invalid_password));
            focusView = mBinding.password;
            cancel = true;
        }

        // Check for a valid phone number.
        if (TextUtils.isEmpty(mPhoneNumber)) {
            mBinding.account.setError(getString(R.string.login_activity_error_field_required));
            focusView = mBinding.account;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt fragment_home and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            if(!BaseApplication.getmTDWebSocket().isOpen()){
                ToastUtils.showToast(sContext, "登陆服务器连接中，请稍后");
                return;
            }
            if (!mIsLoginEnable){
                ToastUtils.showToast(sContext, "登录请求已发送，请稍候");
                return;
            }
            mIsLoginEnable = false;
            sDataManager.BROKER_ID = mBrokerName;
            sDataManager.USER_ID = mPhoneNumber;
            sDataManager.LOGIN_TYPE = AMP_EVENT_LOGIN_TYPE_VALUE_LOGIN;
            //直接保存，解决实盘<->模拟登录不上的问题

            SPUtils.putAndApply(sContext, CONFIG_BROKER, mBrokerName);

            SPUtils.putAndApply(sContext, CONFIG_LOGIN_DATE, "");
            SPUtils.putAndApply(sContext, CONFIG_ACCOUNT, mPhoneNumber);
            SPUtils.putAndApply(sContext, CONFIG_PASSWORD, mPassword);
            BaseApplication.getmTDWebSocket().sendReqLogin(mBrokerName, mPhoneNumber, mPassword);


            if (!mLoginDialog.isShowing())mLoginDialog.show();

            //关闭键盘
            View view = getWindow().getCurrentFocus();
            if (view != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null)
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), HIDE_NOT_ALWAYS);
            }
        }
    }

    private void registerBroaderCast() {
        mReceiverLogin = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String msg = intent.getStringExtra("msg");
                switch (msg) {
                    case TD_MESSAGE_LOGIN_SUCCEED:
                        //登录成功
                        mHandler.sendEmptyMessage(LOGIN_SUCCESS);
                        break;
                    case TD_MESSAGE_WEAK_PASSWORD:
                        //弱密码
                        mHandler.sendEmptyMessage(LOGIN_TO_CHANGE_PASSWORD);
                        break;
                    case TD_MESSAGE_BROKER_INFO:
                        if (mBinding.broker.getText().toString().isEmpty()
                                && mBinding.account.getText().toString().isEmpty())
                            initBrokerAccount();
                        //先登录实盘，登录失败，再点"进入行情"，断线重连
                        mIsVisitEnable = true;
                        mIsLoginEnable = true;
                        break;
                    case TD_MESSAGE_LOGIN_FAIL:
                        //登录失败
                        mHandler.sendEmptyMessage(LOGIN_FAIL);
                        break;
                    case TD_MESSAGE_LOGIN_TIMEOUT:
                        //登录失败
                        mHandler.sendEmptyMessage(LOGIN_TIMEOUT);
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiverLogin, new IntentFilter(TD_BROADCAST_ACTION));
    }

     //合约详情页返回,发送原来订阅合约
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case LOGIN_ACTIVITY_TO_BROKER_LIST_ACTIVITY:
                    String broker = data.getStringExtra("broker");
                    mBinding.broker.setText(broker);
                    break;
                case LOGIN_ACTIVITY_TO_CHANGE_PASSWORD_ACTIVITY:
                    mBinding.password.setText("");
                    break;
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

     // description: 检查是否第一次启动APP,弹出免责条款框
    public void checkResponsibility() {
        try {
            final float nowVersionCode = DataManager.getInstance().APP_CODE;
            float versionCode = (float) SPUtils.get(sContext, CONFIG_VERSION_CODE, 0.0f);
            if (nowVersionCode > versionCode) {
                final Dialog dialog = new Dialog(this, R.style.AppTheme);
                View view = View.inflate(this, R.layout.view_dialog_responsibility, null);
                dialog.setContentView(view);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                dialog.show();
                view.findViewById(R.id.agree).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SPUtils.putAndApply(LoginActivity.this, CONFIG_VERSION_CODE, nowVersionCode);
                        dialog.dismiss();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkNetwork() {
        if (!NetworkUtils.isNetworkConnected(sContext)) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("登录结果");
            dialog.setMessage("网络故障，无法连接到服务器");
            dialog.setCancelable(false);
            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mHandler.sendEmptyMessageDelayed(EXIT_APP, 500);
                }
            });
            dialog.show();
        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 2
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    getSystemInfo();
                }else {
                    mHandler.sendEmptyMessageDelayed(MY_PERMISSIONS_REQUEST_DENIED, 1000);
                }
                break;
            default:
                break;

        }
    }

    private void getSystemInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] info = DeviceInfoManager.getCollectInfo(LoginActivity.this);
                    String encodeInfo = Base64.encode(info);
                    SPUtils.putAndApply(sContext, CONFIG_SYSTEM_INFO, encodeInfo);
                } catch (Exception e) {
                    SPUtils.putAndApply(sContext, CONFIG_SYSTEM_INFO, "");
                }
            }
        }).start();
    }

     private void switchFirm() {
        mBinding.tvBroker.setVisibility(View.VISIBLE);
        mBinding.llBroker.setVisibility(View.VISIBLE);
        mBinding.tvAccount.setText("资金账号");

        changeStatusBarColor();
        mIsFirm = true;

        mBinding.account.getEditableText().clear();
        mBinding.account.requestFocus();
        mBinding.password.getEditableText().clear();
        String brokerName = (String) SPUtils.get(sContext, CONFIG_BROKER, "X先锋期货");
        String account = (String) SPUtils.get(sContext, CONFIG_ACCOUNT, "");
        String passwd = (String) SPUtils.get(sContext, CONFIG_PASSWORD, "");
        mBinding.broker.setText(brokerName);
        mBinding.account.setText(account);
        mBinding.account.setSelection(account.length());
        mBinding.password.setText(passwd);
        if (!account.isEmpty()){
            mBinding.deleteAccount.setVisibility(View.VISIBLE);
            mBinding.password.requestFocus();
        }
    }

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            int statusBarHeight = getStatusBarHeight(sContext);

            View view = new View(this);
            view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            view.getLayoutParams().height = statusBarHeight;
            ((ViewGroup) w.getDecorView()).addView(view);
            view.setBackground(getResources().getDrawable(R.color.colorPrimaryDark));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
    }

    private void showFailHint(){
        mBinding.loginHint1.setVisibility(View.VISIBLE);
        mBinding.loginHint2.setVisibility(View.VISIBLE);
        mBinding.loginHint3.setVisibility(View.VISIBLE);
        mBinding.loginHint4.setVisibility(View.GONE);
    }

    private void hideHint(){
        mBinding.loginHint1.setVisibility(View.GONE);
        mBinding.loginHint2.setVisibility(View.GONE);
        mBinding.loginHint3.setVisibility(View.GONE);
        mBinding.loginHint4.setVisibility(View.GONE);
    }

    private void showTimeoutHint() {
        mBinding.loginHint1.setVisibility(View.GONE);
        mBinding.loginHint2.setVisibility(View.GONE);
        mBinding.loginHint3.setVisibility(View.GONE);
        mBinding.loginHint4.setVisibility(View.VISIBLE);
    }

    static class MyHandler extends Handler {
        WeakReference<LoginActivity> mActivityReference;

        MyHandler(LoginActivity activity) {
            mActivityReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final LoginActivity activity = mActivityReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case LOGIN_SUCCESS:
                        if (activity.sDataManager.LOGIN_TYPE.equals(AMP_EVENT_LOGIN_TYPE_VALUE_LOGIN))
                            activity.hideHint();
                        if (activity.mLoginDialog.isShowing())activity.mLoginDialog.dismiss();
                        //重新写一遍账号信息，防止因过慢网速导致的超时触发密码置空
                        activity.mIsVisitEnable = true;
                        activity.mIsLoginEnable = true;
                        SPUtils.putAndApply(activity.sContext, CONFIG_LOGIN_DATE, TimeUtils.getNowTime());
                        //关闭键盘
                        View view = activity.getWindow().getCurrentFocus();
                        if (view != null) {
                            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (inputMethodManager != null)
                                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), HIDE_NOT_ALWAYS);
                        }
                        Intent intent1 = new Intent(activity, MainActivity.class);
                        activity.startActivity(intent1);
                        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        activity.finish();
                        break;
                    case LOGIN_FAIL:
                        if (activity.sDataManager.LOGIN_TYPE.equals(AMP_EVENT_LOGIN_TYPE_VALUE_LOGIN))
                            activity.showFailHint();
                        if (activity.mLoginDialog.isShowing())activity.mLoginDialog.dismiss();
                        activity.mIsVisitEnable = true;
                        activity.mIsLoginEnable = true;
                        break;
                    case LOGIN_TIMEOUT:
                        if (activity.sDataManager.LOGIN_TYPE.equals(AMP_EVENT_LOGIN_TYPE_VALUE_LOGIN))
                            activity.showTimeoutHint();
                        if (activity.mLoginDialog.isShowing())activity.mLoginDialog.dismiss();
                        activity.mIsVisitEnable = true;
                        activity.mIsLoginEnable = true;
                        break;
                    case LOGIN_TO_CHANGE_PASSWORD:
                        Intent intent = new Intent(activity, ChangePasswordActivity.class);
                        activity.startActivityForResult(intent, LOGIN_ACTIVITY_TO_CHANGE_PASSWORD_ACTIVITY);
                        break;
                    case EXIT_APP:
                        SystemUtils.exitApp(activity);
                        break;
                    case MY_PERMISSIONS_REQUEST_DENIED:
                        activity.checkPermissions();
                        break;
                    default:
                        break;
                }
            }
        }
    }

}

