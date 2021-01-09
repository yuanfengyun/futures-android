package com.shinnytech.futures.controller.fragment;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.autogride.AutoGrideEngine;
import com.shinnytech.futures.R;
import com.shinnytech.futures.databinding.FragmentAutoGrideBinding;
import com.shinnytech.futures.model.bean.accountinfobean.OrderEntity;
import com.shinnytech.futures.model.bean.accountinfobean.PositionEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.MathUtils;
import com.shinnytech.futures.utils.TDUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import static android.view.View.TEXT_ALIGNMENT_CENTER;
import static com.shinnytech.futures.constants.TradeConstants.DIRECTION_SELL;
import static com.shinnytech.futures.constants.TradeConstants.OFFSET_OPEN;
import static com.shinnytech.futures.constants.TradeConstants.STATUS_ALIVE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AutoGrideFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AutoGrideFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AutoGrideFragment extends LazyLoadFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private FragmentAutoGrideBinding mBinding;
    private View mView;
    private float upper_limit = 0;
    private float lower_limit = 0;
    private float ask_price = 0;
    private float bid_price = 0;
    private int ask_volume = 0;
    private int bid_volume = 0;

    private float a = 0;
    private float high = 4050;
    private float low = 4000;
    private float step = 1;
    private String mInstrumentId = "";
    private String mLeftInstrumentId = "";
    private String mRightInstrumentId = "";
    private String mLoadingInstrumentId = "";
    private DataManager sDataManager = DataManager.getInstance();

    private HashMap<Float,Button> price2cancel = new HashMap<Float,Button>();
    private HashMap<Float,Button> price2buy = new HashMap<Float,Button>();
    private HashMap<Float,Button> price2sell = new HashMap<Float,Button>();
    private HashMap<Float,Integer> price2order = new HashMap<Float,Integer>();
    private ArrayList<ArrayList<Button>> buttonCache = new ArrayList<ArrayList<Button>>();
    private ArrayList<LinearLayout> lines = new ArrayList<LinearLayout>();
    private ArrayList<OrderEntity> orderList = new ArrayList<OrderEntity>();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private ViewGroup.LayoutParams pa;

    public AutoGrideFragment() {
        // Required empty public constructor
    }
    public void leave() {
    }

    private void initButtonCache(){
        LinearLayout layout = (LinearLayout)this.getActivity().findViewById(R.id.scroll_linear);
    }

    private void createButtons(int n){
        Button c0 = (Button)this.getActivity().findViewById(R.id.button_cancel0);
        Button b0 = (Button)this.getActivity().findViewById(R.id.button_buy0);
        Button s0 = (Button)this.getActivity().findViewById(R.id.button_sell0);
        Button p0 = (Button)this.getActivity().findViewById(R.id.button_price0);
        if(buttonCache.isEmpty()){
            addButtonClickListener(c0,"cancel",0);
            addButtonClickListener(b0,"buy",0);
            addButtonClickListener(s0,"sell",0);
            buttonCache.add(new ArrayList<Button>(){{add(c0);add(b0);add(s0);add(p0);}});
            lines.add((LinearLayout) this.getActivity().findViewById(R.id.scroll_linear0));
        }
        LinearLayout layout = (LinearLayout)this.getActivity().findViewById(R.id.scroll_linear);
        for(int i=1;i<n;i++){
            if(buttonCache.size()<=i) {
                LinearLayout line = new LinearLayout(this.getActivity());
                line.setOrientation(0);
                Button c = getButton("#21303d", "cancel", i);
                line.addView(c);
                Button b = getButton("#700707", "buy", i);
                line.addView(b);
                Button s = getButton("#57993d", "sell", i);
                line.addView(s);
                Button p = getButton("#324e76", "", i);
                line.addView(p);
                layout.addView(line);
                buttonCache.add(new ArrayList<Button>() {{
                    add(c);
                    add(b);
                    add(s);
                    add(p);
                }});
                lines.add(line);
            }
        }

        for(int i=lines.size()-1;i>=n;i--){
            layout.removeView(lines.get(i));
            lines.remove(i);
            buttonCache.remove(i);
        }
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        createButtons(30);
        mBinding.radioButton5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mInstrumentId==""){
                    return;
                }

                AutoGrideEngine.getInstance().updateSchedule(mInstrumentId,Float.parseFloat(mBinding.gridePoint.getText().toString()),!isChecked);
            }
        });
        mBinding.radioButton6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mInstrumentId==""){
                    return;
                }
                AutoGrideEngine.getInstance().updateSchedule(mInstrumentId,Float.parseFloat(mBinding.gridePoint.getText().toString()),isChecked);
            }
        });
        mBinding.gridePoint.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = mBinding.gridePoint.getText().toString();
                if(str.equals("")) return;
                AutoGrideEngine.getInstance().updateSchedule(mInstrumentId,Float.parseFloat(str),mBinding.radioButton6.isChecked());
            }
        });
    }

    public void updateInstrumentid(String id){
        if(mInstrumentId==id) return;

        QuoteEntity quote = DataManager.getInstance().getRtnData().getQuote(id);
        if (quote == null) {
            mLoadingInstrumentId = id;
            sendSubscribeQuote(id);
            return;
        }

        if(TDUtils.isCombine(id)) {
            String leftLeg = TDUtils.getLeftLeg(id);
            String rightLeg = TDUtils.getRightLeg(id);
            QuoteEntity leftQuote = DataManager.getInstance().getRtnData().getQuote(leftLeg);
            QuoteEntity rightQuote = DataManager.getInstance().getRtnData().getQuote(rightLeg);
            if(leftQuote==null || rightQuote==null){
                mLoadingInstrumentId = id;
                sendSubscribeQuote(id);
                return;
            }

            upper_limit = Float.parseFloat(leftQuote.getUpper_limit()) - Float.parseFloat(rightQuote.getLower_limit());
            lower_limit = Float.parseFloat(leftQuote.getLower_limit()) - Float.parseFloat(rightQuote.getUpper_limit());
        }
        else {
            upper_limit = Float.parseFloat(quote.getUpper_limit());
            lower_limit = Float.parseFloat(quote.getLower_limit());
        }

        if (mBinding == null || mBinding.quoteName != null) {
            mBinding.quoteName.setText(id.substring(id.indexOf(".") + 1));
        }

        mBinding.radioButton5.setChecked(true);

        mInstrumentId = id;
        mLoadingInstrumentId = "";

        orderList.clear();
        if(mBinding!=null && mBinding.scrollTrade!=null) {
            for (int i = mBinding.scrollTrade.getChildCount()-1; i >0 ;i--){
                mBinding.scrollTrade.removeViewAt(i);
            }
        }

        bid_volume = 0;
        ask_volume = 0;
        int n = (int)(upper_limit-lower_limit)+1;
        createButtons(n);
        for(int i=0;i<n;i++){
            ArrayList<Button> array = buttonCache.get(i);
            array.get(0).setText("");
            array.get(1).setText("");
            array.get(2).setText("");
            array.get(3).setText(MathUtils.subZeroAndDot(Float.toString(upper_limit-i)));
        }

        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
        if (userEntity != null) {
            PositionEntity pe = userEntity.getPosition(mInstrumentId);
            if (pe != null && mBinding != null) {
                mBinding.longPos.setText(pe.getVolume_long() + "手");
                mBinding.shortPos.setText(pe.getVolume_short() + "手");
            } else {
                mBinding.longPos.setText("0手");
                mBinding.shortPos.setText("0手");
            }
        }

        showTrade();
    }

    private Button getButton(String color,String t,int i){
        Button b = new Button(this.getActivity());
        b.setBackgroundColor(Color.parseColor(color));
        Button aa = (Button)this.getActivity().findViewById(R.id.button_cancel0);
        ViewGroup.LayoutParams pa = aa.getLayoutParams();
        b.setLayoutParams(pa);
        b.setTextSize(14);
        b.setTextColor(Color.parseColor("#ffffff"));
        b.setText("");
        b.setPadding(0,0,0,0);
        if(t!="") {
            addButtonClickListener(b, t, i);
        }
        return b;
    }

    private void addButtonClickListener(Button b,String t,int i){
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                float price = upper_limit - i;
                LogUtils.e("click button "+t+" "+ Float.toString(i), true);
                if(t=="cancel") {
                    TDUtils.cancelOrderByInstrumentAndPrice(mInstrumentId, price);
                } else if(t=="buy"){
                    TDUtils.tryBuy("", mInstrumentId, price,1);
                } else if(t=="sell"){
                    TDUtils.trySell("", mInstrumentId, price,1);
                }
            }
        });
    }

    public void show(){

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AutoGrideFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AutoGrideFragment newInstance(String param1, String param2) {
        AutoGrideFragment fragment = new AutoGrideFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_auto_gride, container, false);
        mView = mBinding.getRoot();
        return mView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
/*        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void refreshMD() {
        if(mLoadingInstrumentId!="") {
            QuoteEntity quoteEntity = sDataManager.getRtnData().getQuotes().get(mLoadingInstrumentId);
            if (quoteEntity == null){
                return;
            }
            if(TDUtils.isCombine(mLoadingInstrumentId)){
                String leftLeg = TDUtils.getLeftLeg(mLoadingInstrumentId);
                String rightLeg = TDUtils.getRightLeg(mLoadingInstrumentId);
                QuoteEntity leftQuote = DataManager.getInstance().getRtnData().getQuote(leftLeg);
                QuoteEntity rightQuote = DataManager.getInstance().getRtnData().getQuote(rightLeg);
                if(leftQuote==null || rightQuote==null){
                    return;
                }
            }
            updateInstrumentid(mLoadingInstrumentId);
        }

        QuoteEntity quoteEntity = sDataManager.getRtnData().getQuotes().get(mInstrumentId);
        if (quoteEntity == null){
            return;
        }

        float askprice = MathUtils.toFloat(quoteEntity.getAsk_price1(),0);
        float bidprice = MathUtils.toFloat(quoteEntity.getBid_price1(),0);
        int askvolume = MathUtils.toInt(quoteEntity.getAsk_volume1(),0);
        int bidvolume = MathUtils.toInt(quoteEntity.getBid_volume1(),0);

        if(ask_volume>0 && (askprice!=ask_price || ask_volume != askvolume)){
            ArrayList<Button> array = buttonCache.get((int)(upper_limit-ask_price));
            if(array !=null) {
                array.get(2).setText("");
            }
        }

        if(askvolume>0 && (askprice!=ask_price || ask_volume != askvolume)){
            ArrayList<Button> array = buttonCache.get((int)(upper_limit-askprice));
            if(array !=null) {
                array.get(2).setText(quoteEntity.getAsk_volume1());
            }
            ask_price = askprice;
            ask_volume = askvolume;
        }

        if(bid_volume>0 && (bidprice!=bid_price || bid_volume != bidvolume)){
            ArrayList<Button> array = buttonCache.get((int)(upper_limit-bid_price));
            if(array !=null) {
                array.get(1).setText("");
            }
        }

        if(bidvolume>0 && (bidprice!=bid_price || bid_volume != bidvolume)){
            ArrayList<Button> array = buttonCache.get((int)(upper_limit-bidprice));
            if(array !=null) {
                array.get(1).setText(quoteEntity.getBid_volume1());
            }
            bid_price = bidprice;
            bid_volume = bidvolume;
        }
    }

    @Override
    public void refreshTD() {
        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
        if (userEntity == null) return;

        if(mInstrumentId.equals("")) return;
        HashMap<Float, Integer> m = new HashMap<Float, Integer>();
        for (OrderEntity orderEntity :
                userEntity.getOrders().values()) {
            if (orderEntity != null &&
                    (orderEntity.getExchange_id() + "." + orderEntity.getInstrument_id())
                            .equals(mInstrumentId)) {
                if (STATUS_ALIVE.equals(orderEntity.getStatus())) {
                    float p = Float.parseFloat(orderEntity.getLimit_price());
                    Integer n = Integer.parseInt(orderEntity.getVolume_orign());
                    if(DIRECTION_SELL.equals(orderEntity.getDirection())){
                        n = -n;
                    }
                    if (m.containsKey(p)) {
                        m.put(p, m.get(p) + n);
                    } else {
                        m.put(p, n);
                    }
                }
            }
        }
        for (HashMap.Entry<Float, Integer> entity : m.entrySet()) {
            float k = entity.getKey();
            Integer v = entity.getValue();
            if (price2order.containsKey(k) == false || price2order.get(k) != v) {
                ArrayList<Button> array = buttonCache.get((int)(upper_limit-k));
                if(array !=null) {
                    array.get(0).setText(Integer.toString(v));
                }
            }
        }
        for (HashMap.Entry<Float, Integer> entity : price2order.entrySet()) {
            float k = entity.getKey();
            Integer v = entity.getValue();
            if (m.containsKey(k) == false) {
                ArrayList<Button> array = buttonCache.get((int)(upper_limit-k));
                if(array !=null) {
                    array.get(0).setText("");
                }
            }
        }
        price2order = m;

        PositionEntity pe = userEntity.getPosition(mInstrumentId);
        if(pe!=null && mBinding!= null){
            mBinding.longPos.setText(pe.getVolume_long()+"手");
            mBinding.shortPos.setText(pe.getVolume_short()+"手");
        }else{
            mBinding.longPos.setText("0");
            mBinding.shortPos.setText("0");
        }

        showTrade();
    }

    private void showTrade(){
        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
        if (userEntity == null) return;


        if(mBinding==null) return;
        TextView textDirection = mBinding.textDirection;
        TextView textPrice = mBinding.textPrice;
        TextView textVolume = mBinding.textVolume;
        TextView textStatus = mBinding.textStatus;
        TextView textTime = mBinding.textTime;

        TreeMap<Double, OrderEntity> treeMap = new TreeMap<Double, OrderEntity>();
        LinearLayout scrollTrade = mBinding.scrollTrade;
        for (OrderEntity orderEntity : userEntity.getOrders().values()) {
            if (orderEntity != null && (orderEntity.getExchange_id() + "." + orderEntity.getInstrument_id()).equals(mInstrumentId)) {
                treeMap.put(Double.parseDouble(orderEntity.getInsert_date_time())/1000,orderEntity);
            }
        }
        Iterator iter = treeMap.keySet().iterator();
        while (iter.hasNext()){
            Double key = (Double)iter.next();
            OrderEntity orderEntity = treeMap.get(key);
            boolean find = false;
            int index = 0;
            for(OrderEntity o:orderList) {
                if(o.getOrder_id()==orderEntity.getOrder_id()){
                    find = true;
                }
                if(Double.parseDouble(o.getInsert_date_time()) > Double.parseDouble(orderEntity.getInsert_date_time()))
                {
                    index++;
                }
            }
            if(find){
                LinearLayout layout = (LinearLayout) scrollTrade.getChildAt(index+1);
                TextView volumeText = (TextView) layout.getChildAt(+2);
                Integer voluemeLeft = Integer.parseInt(orderEntity.getVolume_left());
                String volumeStr = Integer.toString(Integer.parseInt(orderEntity.getVolume_orign()) - voluemeLeft) + "/" + orderEntity.getVolume_orign();
                volumeText.setText(volumeStr);

                TextView statusText = (TextView) layout.getChildAt(3);
                if(orderEntity.getStatus().length()>5){
                    statusText.setText(orderEntity.getStatus().substring(0,6));
                }else {
                    statusText.setText(orderEntity.getStatus());
                }
            }else{
                orderList.add(index,orderEntity);
                String directionStr = "买";
                if (DIRECTION_SELL.equals(orderEntity.getDirection())) {
                    directionStr = "卖";
                }
                String offsetStr = "开";
                if (!OFFSET_OPEN.equals(orderEntity.getOffset())) {
                    offsetStr = "平";
                }

                String price = MathUtils.subZeroAndDot(orderEntity.getLimit_price());

                Integer voluemeLeft = Integer.parseInt(orderEntity.getVolume_left());
                String volumeStr = Integer.toString(Integer.parseInt(orderEntity.getVolume_orign()) - voluemeLeft) + "/" + orderEntity.getVolume_orign();

                SimpleDateFormat format =  new SimpleDateFormat("HH:mm:ss"); //设置格式
                String timeText = format.format(key/1000);

                LinearLayout line = new LinearLayout(this.getActivity());
                line.setOrientation(0);
                TextView text = new TextView(this.getActivity());
                text.setText(directionStr + offsetStr);
                text.setTextSize(10);
                text.setLayoutParams(textDirection.getLayoutParams());
                line.addView(text);
                text = new TextView(this.getActivity());
                text.setText(price);
                text.setTextSize(10);
                text.setLayoutParams(textPrice.getLayoutParams());
                text.setTextAlignment(TEXT_ALIGNMENT_CENTER);
                line.addView(text);
                text = new TextView(this.getActivity());
                text.setText(volumeStr);
                text.setTextSize(10);
                text.setLayoutParams(textVolume.getLayoutParams());
                text.setTextAlignment(TEXT_ALIGNMENT_CENTER);
                line.addView(text);
                text = new TextView(this.getActivity());
                if(orderEntity.getStatus().length()>5){
                    text.setText(orderEntity.getStatus().substring(0,6));
                }else {
                    text.setText(orderEntity.getStatus());
                }
                text.setTextSize(10);
                text.setLayoutParams(textStatus.getLayoutParams());
                text.setTextAlignment(TEXT_ALIGNMENT_CENTER);
                line.addView(text);
                text = new TextView(this.getActivity());
                text.setText(timeText);
                text.setTextSize(10);
                text.setLayoutParams(textTime.getLayoutParams());
                text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                line.addView(text);
                scrollTrade.addView(line,index+1);
            }
        }
    }
}
