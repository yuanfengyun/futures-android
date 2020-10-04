package com.shinnytech.futures.controller.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.R;
import com.shinnytech.futures.model.adapter.CustomizeQuoteAdapter;
import com.shinnytech.futures.model.bean.CustomizeQuote;
import com.shinnytech.futures.databinding.FragmentCustomizeBinding;

import java.util.ArrayList;
import java.util.List;

import static com.shinnytech.futures.constants.BroadcastConstants.NORMAL_MESSAGE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AutoGrideFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AutoGrideFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CustomizeFragment extends LazyLoadFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private FragmentCustomizeBinding mBinding;
    private View mView;
    private ListView listView;

    // TODO: Rename and change types of parameters
    private OnFragmentInteractionListener mListener;
    private ViewGroup.LayoutParams pa;
    private boolean first_ins = true;
    private List<CustomizeQuote> quoteList = new ArrayList<CustomizeQuote>();
    private Context context;
    private String quote1Name="";
    private String quote2Name="";

    public CustomizeFragment() {
        // Required empty public constructor
    }
    public void leave() {
    }

    @Override
    public void show() {
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        CustomizeQuoteAdapter adapter = new CustomizeQuoteAdapter(context,
                R.layout.item_fragment_customize_quote, quoteList);
        listView = (ListView) mBinding.quoteList;
        listView.setAdapter(adapter);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                adapter.setSelectedItem(arg2);
                adapter.notifyDataSetInvalidated();
                CustomizeQuote q = adapter.getItem(arg2);
                mBinding.tradeQuote.setText(q.getName());
            }
        });

        mBinding.openCloseSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mBinding.openCloseSwitch.setText("开");
                }else{
                    mBinding.openCloseSwitch.setText("平");
                }
            }
        });
        mBinding.selectQuote1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                first_ins = true;
                BaseApplication.sendMessage(NORMAL_MESSAGE,BaseApplication.NORMAL_BROADCAST);
            }
        });

        mBinding.selectQuote2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                first_ins = false;
                BaseApplication.sendMessage(NORMAL_MESSAGE,BaseApplication.NORMAL_BROADCAST);
            }
        });

        mBinding.addCustomizeQuote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(quote1Name.equals(quote2Name)) return;
                if(quote1Name.equals("") || quote2Name.equals("")) return;
                if(adapter.check(CustomizeQuote.getName(quote1Name,quote2Name))) {
                    adapter.add(new CustomizeQuote(quote1Name, quote2Name));
                }
            }
        });

        mBinding.delCustomizeQuote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.removeSelectedItem();
            }
        });
    }

    public static CustomizeFragment newInstance(String param1, String param2) {
        CustomizeFragment fragment = new CustomizeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_customize, container, false);
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
        this.context = context;
        super.onAttach(context);
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

    public void updateInstrumentid(String id){

        BaseApplication.getmMDWebSocket().sendSubscribeQuote(id);
        if(first_ins){
            quote1Name = id;
            mBinding.selectedQuote1.setText(id.substring(id.indexOf(".")+1));
        }else{
            quote2Name = id;
            mBinding.selectedQuote2.setText(id.substring(id.indexOf(".")+1));
        }
    }
    @Override
    public void refreshMD() {
    }

    @Override
    public void refreshTD() {
    }
}
