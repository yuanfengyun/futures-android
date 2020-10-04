package com.shinnytech.futures.model.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.databinding.ItemFragmentQuoteBinding;
import com.shinnytech.futures.model.bean.CustomizeQuote;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.MathUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import static com.shinnytech.futures.constants.CommonConstants.DALIANZUHE;
import static com.shinnytech.futures.constants.CommonConstants.ZHENGZHOUZUHE;
import static com.shinnytech.futures.model.engine.LatestFileManager.getUpDown;
import static com.shinnytech.futures.model.engine.LatestFileManager.getUpDownRate;

public class CustomizeQuoteAdapter extends ArrayAdapter<CustomizeQuote> {
    private Context sContext;
    private String mTitle;
    private boolean mSwitchBid = false;
    private boolean mSwitchAsk = false;
    private boolean mSwitchChange = false;
    private boolean mSwitchVolume = false;
    private int resourceId;
    private int selectedItem = -1;

    public CustomizeQuoteAdapter(Context context, int resource, List<CustomizeQuote> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    public boolean check(String name){
        for(int i=0;i<getCount();i++){
            CustomizeQuote q = getItem(i);
            if(q.getName().equals(name)) return false;
        }
        return true;
    }

    public void setSelectedItem(int selectedItem)
    {
        this.selectedItem = selectedItem;
    }

    public int getSelectedItem(){
        return selectedItem;
    }

    public void removeSelectedItem(){
        if(-1==selectedItem) return;
        CustomizeQuote q = getItem(selectedItem);
        remove(q);
        selectedItem = -1;
    }

    // convertView 参数用于将之前加载好的布局进行缓存
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        CustomizeQuote quote=getItem(position); //获取当前项的Fruit实例

        // 加个判断，以免ListView每次滚动时都要重新加载布局，以提高运行效率
        View view;
        ViewHolder viewHolder;
        if (convertView==null){
            // 避免ListView每次滚动时都要重新加载布局，以提高运行效率
            view=LayoutInflater.from(getContext()).inflate(resourceId,parent,false);

            // 避免每次调用getView()时都要重新获取控件实例
            viewHolder = new ViewHolder();
            viewHolder.quoteName = view.findViewById(R.id.quote_name);
            viewHolder.quoteBidPrice = view.findViewById(R.id.quote_bid_price);
            viewHolder.quoteBidVolume = view.findViewById(R.id.quote_bid_volume);
            viewHolder.quoteAskPrice = view.findViewById(R.id.quote_ask_price);
            viewHolder.quoteAskVolume = view.findViewById(R.id.quote_ask_volume);

            // 将ViewHolder存储在View中（即将控件的实例存储在其中）
            view.setTag(viewHolder);
        } else{
            view=convertView;
            viewHolder=(ViewHolder) view.getTag();
        }

        // 获取控件实例，并调用set...方法使其显示出来
        viewHolder.quoteName.setText(quote.getName());
        viewHolder.quoteBidPrice.setText(quote.getBidPrice());
        viewHolder.quoteBidVolume.setText(quote.getBidVolume());
        viewHolder.quoteAskPrice.setText(quote.getAskPrice());
        viewHolder.quoteAskVolume.setText(quote.getAskVolume());

        if(position == selectedItem){
            view.setBackgroundColor(Color.parseColor("#880000"));
        }else{
            view.setBackgroundColor(Color.parseColor("#000000"));
        }

            return view;
    }

    // 定义一个内部类，用于对控件的实例进行缓存
    class ViewHolder{
        TextView quoteName;
        TextView quoteBidPrice;
        TextView quoteBidVolume;
        TextView quoteAskPrice;
        TextView quoteAskVolume;
    }
}
