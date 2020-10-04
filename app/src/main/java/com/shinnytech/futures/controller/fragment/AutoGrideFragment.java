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

import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.autogride.AutoGrideEngine;
import com.shinnytech.futures.R;
import com.shinnytech.futures.databinding.FragmentAutoGrideBinding;
import com.shinnytech.futures.model.bean.accountinfobean.OrderEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.TDUtils;

import java.util.HashMap;

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
    private DataManager sDataManager = DataManager.getInstance();

    private HashMap<Float,Button> price2cancel = new HashMap<Float,Button>();
    private HashMap<Float,Button> price2buy = new HashMap<Float,Button>();
    private HashMap<Float,Button> price2sell = new HashMap<Float,Button>();
    private HashMap<Float,Integer> price2order = new HashMap<Float,Integer>();

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

    public void updateInstrumentid(String id){
        if(mInstrumentId==id) return;

        QuoteEntity quote = DataManager.getInstance().getRtnData().getQuote(id);
        if(quote==null){
            BaseApplication.getmMDWebSocket().sendSubscribeQuote(id);
            return;
        }
        mInstrumentId = id;
        upper_limit = Float.parseFloat(quote.getUpper_limit());
        lower_limit = Float.parseFloat(quote.getLower_limit());
        mBinding.quoteName.setText(id);
        initGride();
    }

    private void initGride(){
        LinearLayout layout = (LinearLayout)this.getActivity().findViewById(R.id.scroll_linear);
        for(float i=upper_limit;i>lower_limit;i--){
            LinearLayout line = new LinearLayout(this.getActivity());
            line.setOrientation(0);
            Button b = getButton("","#21303d","cancel",i);
            line.addView(b);
            price2cancel.put(i,b);
            b = getButton("","#700707","buy",i);
            line.addView(b);
            price2buy.put(i,b);
            b = getButton("","#57993d","sell",i);
            line.addView(b);
            price2sell.put(i,b);
            b = getButton(Float.toString(i),"#324e76","",i);
            line.addView(b);
            layout.addView(line);
        }
    }

    private Button getButton(String text,String color,String type,float price){
        Button b = new Button(this.getActivity());
        b.setBackgroundColor(Color.parseColor(color));
        Button aa = (Button)this.getActivity().findViewById(R.id.button_cancel);

        ViewGroup.LayoutParams pa = aa.getLayoutParams();
        b.setLayoutParams(pa);
        b.setTextSize(14);
        b.setTextColor(Color.parseColor("#ffffff"));
        b.setText(text);
        b.setPadding(0,0,0,0);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LogUtils.e("click button "+type+" "+Float.toString(price), true);
                if(type=="cancel") {
                    TDUtils.cancelOrderByInstrumentAndPrice("", "", price);
                } else if(type=="buy"){
                    TDUtils.tryBuy("", "", price,1);
                } else if(type=="sell"){
                    TDUtils.trySell("", "", price,1);
                }
            }
        });
        return b;
    }

    public void show(){
        mBinding.grideSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
                AutoGrideEngine.getInstance().updateSchedule(mInstrumentId,Float.parseFloat(mBinding.gridePoint.getText().toString()),mBinding.grideSwitch.isChecked());
            }
        });
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
        QuoteEntity quoteEntity = sDataManager.getRtnData().getQuotes().get(mInstrumentId);
        if (quoteEntity == null) return;

        float askprice = Float.parseFloat(quoteEntity.getAsk_price1());
        float bidprice = Float.parseFloat(quoteEntity.getBid_price1());
        int askvolume = Integer.parseInt(quoteEntity.getAsk_volume1());
        int bidvolume = Integer.parseInt(quoteEntity.getBid_volume1());

        if(ask_volume>0 && (askprice!=ask_price || ask_volume != askvolume)){
            price2sell.get(ask_price).setText("");
        }

        if(askvolume>0 && (askprice!=ask_price || ask_volume != askvolume)){
            price2sell.get(askprice).setText(quoteEntity.getAsk_volume1());
            ask_price = askprice;
            ask_volume = askvolume;
        }

        if(bid_volume>0 && (bidprice!=bid_price || bid_volume != bidvolume)){
            price2buy.get(bid_price).setText("");
        }

        if(bidvolume>0 && (bidprice!=bid_price || bid_volume != bidvolume)){
            price2buy.get(bidprice).setText(quoteEntity.getBid_volume1());
            bid_price = bidprice;
            bid_volume = bidvolume;
        }
    }

    @Override
    public void refreshTD() {
        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
        if (userEntity == null) return;
        HashMap<Float, Integer> m = new HashMap<Float, Integer>();
        for (OrderEntity orderEntity :
                userEntity.getOrders().values()) {
            if (orderEntity != null &&
                    (orderEntity.getExchange_id() + "." + orderEntity.getInstrument_id())
                            .equals(mInstrumentId)) {
                if (STATUS_ALIVE.equals(orderEntity.getStatus())) {
                    float p = Float.parseFloat(orderEntity.getLimit_price());
                    Integer n = Integer.parseInt(orderEntity.getVolume_orign());
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
                price2cancel.get(k).setText(Integer.toString(v));
            }
        }
        for (HashMap.Entry<Float, Integer> entity : price2order.entrySet()) {
            float k = entity.getKey();
            Integer v = entity.getValue();
            if (m.containsKey(k) == false) {
                price2cancel.get(k).setText("");
            }
        }
        price2order = m;
    }
}
