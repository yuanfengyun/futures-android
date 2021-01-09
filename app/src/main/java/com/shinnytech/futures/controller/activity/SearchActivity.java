package com.shinnytech.futures.controller.activity;

import android.content.Context;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.SearchView;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.databinding.ActivitySearchBinding;
import com.shinnytech.futures.model.adapter.SearchAdapter;
import com.shinnytech.futures.model.bean.eventbusbean.SwitchInsEvent;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.DividerItemDecorationUtils;
import com.shinnytech.futures.utils.NetworkUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import static android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_OPTIONAL_DIRECTION;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_OPTIONAL_DIRECTION_VALUE_ADD;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_OPTIONAL_DIRECTION_VALUE_DELETE;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_OPTIONAL_INSTRUMENT_ID;
import static com.shinnytech.futures.constants.AmpConstants.AMP_OPTIONAL_SEARCH;
import static com.shinnytech.futures.constants.CommonConstants.SOURCE_ACTIVITY_AUTO_GRIDE;
import static com.shinnytech.futures.constants.CommonConstants.SOURCE_ACTIVITY_CUSTOMIZE;
import static com.shinnytech.futures.constants.CommonConstants.INS_BETWEEN_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.SOURCE_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.SOURCE_ACTIVITY_CONDITION_ORDER;
import static com.shinnytech.futures.constants.CommonConstants.SOURCE_ACTIVITY_FUTURE_INFO;
import static com.shinnytech.futures.constants.CommonConstants.SOURCE_ACTIVITY_MAIN;
import static com.shinnytech.futures.utils.ScreenUtils.getStatusBarHeight;

/**
 * date: 6/21/17
 * author: chenli
 * description: 搜索合约页
 * version:
 * state: basically done
 */
public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private SearchAdapter mSearchAdapter;
    private Context sContext;
    private ActivitySearchBinding mBinding;
    private String mSourceActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        initData();
        initEvent();
    }

    private void initData() {
        mSourceActivity = getIntent().getStringExtra(SOURCE_ACTIVITY);
        sContext = BaseApplication.getContext();
        mBinding.toolbarSearch.setTitle("");
        setSupportActionBar(mBinding.toolbarSearch);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        changeStatusBarColor();

        mBinding.rvSearchQuoteList.setLayoutManager(new LinearLayoutManager(this));
        mBinding.rvSearchQuoteList.addItemDecoration(
                new DividerItemDecorationUtils(this, DividerItemDecorationUtils.VERTICAL_LIST));
        mBinding.rvSearchQuoteList.setItemAnimator(new DefaultItemAnimator());
        mSearchAdapter = new SearchAdapter(this);
        mBinding.rvSearchQuoteList.setAdapter(mSearchAdapter);
    }

    private void initEvent() {
        mSearchAdapter.setOnItemClickListener(new SearchAdapter.OnItemClickListener() {
            //跳转到合约详情页
            @Override
            public void OnItemJump(SearchEntity searchEntity, String instrument_id) {
                //如果用户点击了搜索到的合约信息，就把此条合约保存到搜索历史中
                if (instrument_id != null && !instrument_id.isEmpty()) {
                    LatestFileManager.getSearchEntitiesHistory().put(instrument_id, searchEntity);
                    switch (mSourceActivity){
                        case SOURCE_ACTIVITY_MAIN:
                            Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra(INS_BETWEEN_ACTIVITY, instrument_id);
                            startActivity(intent);
                            break;
                        case SOURCE_ACTIVITY_FUTURE_INFO:
                            SwitchInsEvent switchInsEvent = new SwitchInsEvent();
                            switchInsEvent.setInstrument_id(instrument_id);
                            EventBus.getDefault().post(switchInsEvent);
                            break;
                        case SOURCE_ACTIVITY_CONDITION_ORDER:
                            Intent intent1 = new Intent();
                            intent1.putExtra(INS_BETWEEN_ACTIVITY, instrument_id);
                            setResult(RESULT_OK, intent1);
                            break;
                        case SOURCE_ACTIVITY_AUTO_GRIDE:
                            Intent intent2= new Intent();
                            intent2.putExtra(INS_BETWEEN_ACTIVITY, instrument_id);
                            setResult(RESULT_OK, intent2);
                        case SOURCE_ACTIVITY_CUSTOMIZE:
                            Intent intent3= new Intent();
                            intent3.putExtra(INS_BETWEEN_ACTIVITY, instrument_id);
                            setResult(RESULT_OK, intent3);
                        default:
                            break;
                    }
                    //关闭键盘后销毁
                    View view = getCurrentFocus();
                    if (view != null) {
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (inputMethodManager != null)
                            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), HIDE_NOT_ALWAYS);
                    }
                    finish();
                }
            }

            //收藏或移除该合约到自选合约列表
            @Override
            public void OnItemCollect(View view, String instrument_id) {
                if (instrument_id == null || "".equals(instrument_id)) return;
                Map<String, QuoteEntity> insList = LatestFileManager.getOptionalInsList();
                try {
                    String name = instrument_id;
                    SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(instrument_id);
                    if (searchEntity != null) name = searchEntity.getInstrumentName();
                    if (insList.containsKey(instrument_id)) {
                        insList.remove(instrument_id);
                        LatestFileManager.saveInsListToFile(new ArrayList<>(insList.keySet()));
                        ToastUtils.showToast(BaseApplication.getContext(), name + "合约已移除");
                        ((ImageView) view).setImageResource(R.mipmap.ic_favorite_border_white_24dp);
                    } else {
                        QuoteEntity quoteEntity = new QuoteEntity();
                        quoteEntity.setInstrument_id(instrument_id);
                        insList.put(instrument_id, quoteEntity);
                        LatestFileManager.saveInsListToFile(new ArrayList<>(insList.keySet()));
                        ToastUtils.showToast(BaseApplication.getContext(), name + "合约已添加");
                        ((ImageView) view).setImageResource(R.mipmap.ic_favorite_white_24dp);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //添加搜索监听
        mBinding.searchEditFrame.setOnQueryTextListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateToolbarFromNetwork();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 返回行情页时，如果是自选合约详情页则刷新之
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                //关闭键盘后销毁
                back();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 处理返回键逻辑或者使用onBackPressed()
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) back();
        return super.onKeyDown(keyCode, event);
    }

    /**
     * date: 2019/3/30
     * author: chenli
     * description: 返回上一页
     */
    private void back() {
        //关闭键盘后销毁
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null)
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), HIDE_NOT_ALWAYS);
        }
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 添加搜索过滤
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        mSearchAdapter.filter(newText);
        return true;
    }


    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            int statusBarHeight = getStatusBarHeight(BaseApplication.getContext());

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


    /**
     * date: 7/9/17
     * author: chenli
     * description: 检查网络状态，更新toolbar的显示
     */
    private void updateToolbarFromNetwork() {
        if (NetworkUtils.isNetworkConnected(sContext)) {
            mBinding.toolbarSearch.setBackgroundColor(ContextCompat.getColor(sContext, R.color.toolbar));
        } else {
            mBinding.toolbarSearch.setBackgroundColor(ContextCompat.getColor(sContext, R.color.login_simulation_hint));
        }
    }

}
