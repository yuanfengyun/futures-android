package com.shinnytech.futures.model.adapter;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.databinding.ItemFragmentQuoteBinding;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.MathUtils;

import java.util.ArrayList;
import java.util.List;

import static com.shinnytech.futures.constants.CommonConstants.DALIANZUHE;
import static com.shinnytech.futures.constants.CommonConstants.ZHENGZHOUZUHE;
import static com.shinnytech.futures.model.engine.LatestFileManager.getUpDown;
import static com.shinnytech.futures.model.engine.LatestFileManager.getUpDownRate;


/**
 * date: 7/9/17
 * author: chenli
 * description: 行情页适配器
 * version:
 * state: done
 */
public class QuoteAdapter extends RecyclerView.Adapter<QuoteAdapter.ItemViewHolder> {
    private Context sContext;
    private List<QuoteEntity> mData;
    private String mTitle;
    private boolean mSwitchBid = false;
    private boolean mSwitchAsk = false;
    private boolean mSwitchChange = false;
    private boolean mSwitchVolume = false;
    private List<String> highlightList = new ArrayList<>();

    public QuoteAdapter(Context context, List<QuoteEntity> data, String title) {
        this.sContext = context;
        this.mData = data;
        this.mTitle = title;
    }

    public List<QuoteEntity> getData() {
        return mData;
    }

    public void setData(List<QuoteEntity> data) {
        this.mData = data;
    }

    public void updateHighlightList(List<String> highlightList) {
        if (!this.highlightList.equals(highlightList)) {
            this.highlightList.clear();
            this.highlightList.addAll(highlightList);
            notifyDataSetChanged();
        }
    }

    public void switchBidView() {
        mSwitchBid = !mSwitchBid;
        notifyDataSetChanged();
    }

    public void switchAskView() {
        mSwitchAsk = !mSwitchAsk;
        notifyDataSetChanged();
    }

    public void switchChangeView() {
        mSwitchChange = !mSwitchChange;
        notifyDataSetChanged();
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 持仓量/成交量切换
     */
    public void switchVolView() {
        mSwitchVolume = !mSwitchVolume;
        notifyDataSetChanged();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemFragmentQuoteBinding binding = DataBindingUtil.inflate(LayoutInflater
                .from(sContext), R.layout.item_fragment_quote, parent, false);
        ItemViewHolder holder = new ItemViewHolder(binding.getRoot());
        holder.setBinding(binding);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder itemViewHolder, int position) {
        itemViewHolder.update();
    }

    @Override
    public void onBindViewHolder(ItemViewHolder itemViewHolder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            itemViewHolder.update();
        } else {
            Bundle bundle = (Bundle) payloads.get(0);
            itemViewHolder.updatePart(bundle);
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    /**
     * date: 2019/4/22
     * author: chenli
     * description: 正常合约布局
     * version:
     * state:
     */
    class ItemViewHolder extends RecyclerView.ViewHolder {

        private ItemFragmentQuoteBinding mBinding;

        public ItemViewHolder(View itemView) {
            super(itemView);
        }

        public ItemFragmentQuoteBinding getBinding() {
            return this.mBinding;
        }

        public void setBinding(ItemFragmentQuoteBinding binding) {
            this.mBinding = binding;
        }

        public void update() {
            if (mData == null || mData.size() == 0) return;
            QuoteEntity quoteEntity = mData.get(getLayoutPosition());
            if (quoteEntity == null) return;
            try {

                String instrumentId = quoteEntity.getInstrument_id();
                if (instrumentId == null) return;

                //合约高亮
                if (highlightList.contains(instrumentId))
                    mBinding.llQuote.setBackground(sContext.getResources().getDrawable(R.drawable.fragment_item_highlight_touch_bg));
                else
                    mBinding.llQuote.setBackground(sContext.getResources().getDrawable(R.drawable.fragment_item_touch_bg));

                String instrumentName = instrumentId;
                SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(instrumentId);
                if (searchEntity != null) instrumentName = searchEntity.getInstrumentName();
                if (instrumentName.contains("&")) mBinding.quoteName.setTextSize(10);
                else mBinding.quoteName.setTextSize(15);
                mBinding.quoteName.setText(instrumentName);

                String pre_settlement = LatestFileManager.saveScaleByPtick(quoteEntity.getPre_settlement(), instrumentId);
                String latest = LatestFileManager.saveScaleByPtick(quoteEntity.getLast_price(), instrumentId);
                String changePercent = MathUtils.round(
                        getUpDownRate(quoteEntity.getLast_price(), quoteEntity.getPre_settlement()), 2);
                String change = LatestFileManager.saveScaleByPtick(
                        getUpDown(quoteEntity.getLast_price(), quoteEntity.getPre_settlement()), instrumentId);
                String askPrice1 = LatestFileManager.saveScaleByPtick(quoteEntity.getAsk_price1(), instrumentId);
                String bidPrice1 = LatestFileManager.saveScaleByPtick(quoteEntity.getBid_price1(), instrumentId);

                setTextColor(mBinding.quoteLatest, latest, pre_settlement);
                if (mSwitchBid) {
                    mBinding.quoteBid.setText(quoteEntity.getBid_volume1());
                    mBinding.quoteBid.setTextColor(ContextCompat.getColor(sContext, R.color.text_white));
                } else {
                    setTextColor(mBinding.quoteBid, bidPrice1, pre_settlement);
                }
                if (mSwitchAsk) {
                    mBinding.quoteAsk.setText(quoteEntity.getAsk_volume1());
                    mBinding.quoteAsk.setTextColor(ContextCompat.getColor(sContext, R.color.text_white));
                } else {
                    setTextColor(mBinding.quoteAsk, askPrice1, pre_settlement);
                }
                if (mSwitchChange) {
                    setChangeTextColor(mBinding.quoteChangePercent, change);
                } else {
                    setChangeTextColor(mBinding.quoteChangePercent, changePercent);
                }
                if (mSwitchVolume) {
                    mBinding.quoteOpenInterest.setText(quoteEntity.getVolume());
                } else {
                    mBinding.quoteOpenInterest.setText(quoteEntity.getOpen_interest());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }

        private void updatePart(Bundle bundle) {
            for (String key :
                    bundle.keySet()) {
                String value = bundle.getString(key);
                switch (key) {
                    case "latest":
                        setTextColor(mBinding.quoteLatest, value, bundle.getString("pre_settlement"));
                        break;
                    case "change":
                        if (!(DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle)) && mSwitchChange) {
                            setChangeTextColor(mBinding.quoteChangePercent, value);
                        }
                        break;
                    case "change_percent":
                        if (!(DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle)) && !mSwitchChange) {
                            setChangeTextColor(mBinding.quoteChangePercent, value);
                        }
                        break;
                    case "volume":
                        if (!(DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle)) && mSwitchVolume) {
                            mBinding.quoteOpenInterest.setText(value);
                        }
                        break;
                    case "open_interest":
                        if (!(DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle)) && !mSwitchVolume) {
                            mBinding.quoteOpenInterest.setText(value);
                        }
                        break;
                    case "bid_price1":
                        if (!mSwitchBid) {
                            setTextColor(mBinding.quoteBid, value, bundle.getString("pre_settlement"));
                        }
                        break;
                    case "bid_volume1":
                        if (mSwitchBid) {
                            mBinding.quoteBid.setText(value);
                            mBinding.quoteBid.setTextColor(ContextCompat.getColor(sContext, R.color.text_white));
                        }
                        break;
                    case "ask_price1":
                        if (!mSwitchAsk) {
                            setTextColor(mBinding.quoteAsk, value, bundle.getString("pre_settlement"));
                        }
                        break;
                    case "ask_volume1":
                        if (mSwitchVolume) {
                            mBinding.quoteAsk.setText(value);
                            mBinding.quoteAsk.setTextColor(ContextCompat.getColor(sContext, R.color.text_white));
                        }
                        break;
                    default:
                        break;

                }
            }
        }

        /**
         * date: 7/9/17
         * author: chenli
         * description: 设置涨跌幅文字颜色
         */
        public void setChangeTextColor(TextView textView, String data) {
            textView.setText(data);
            if (data == null || data.equals("-")) {
                textView.setTextColor(ContextCompat.getColor(sContext, R.color.text_white));
                return;
            }
            try {
                float value = new Float(data);
                if (value < 0)
                    textView.setTextColor(ContextCompat.getColor(sContext, R.color.text_green));
                else if (value > 0)
                    textView.setTextColor(ContextCompat.getColor(sContext, R.color.text_red));
                else textView.setTextColor(ContextCompat.getColor(sContext, R.color.text_white));
            } catch (Exception e) {
                e.printStackTrace();

            }
        }

        /**
         * date: 2018/12/4
         * author: chenli
         * description: 设置最新价文字颜色
         */
        public void setTextColor(TextView textView, String latest, String pre_settlement) {
            textView.setText(latest);
            if (latest == null || latest.equals("-") ||
                    pre_settlement == null || pre_settlement.equals("-")) {
                textView.setTextColor(ContextCompat.getColor(sContext, R.color.text_white));
                return;
            }
            try {
                float value = Float.parseFloat(latest) - Float.parseFloat(pre_settlement);
                if (value < 0)
                    textView.setTextColor(ContextCompat.getColor(sContext, R.color.text_green));
                else if (value > 0)
                    textView.setTextColor(ContextCompat.getColor(sContext, R.color.text_red));
                else textView.setTextColor(ContextCompat.getColor(sContext, R.color.text_white));
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }
}
