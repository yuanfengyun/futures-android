package com.shinnytech.futures.controller.activity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.shinnytech.futures.controller.fragment.LazyLoadFragment;
import com.shinnytech.futures.R;
import com.shinnytech.futures.databinding.FragmentAutoGrideBinding;


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

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public AutoGrideFragment() {
        // Required empty public constructor
    }
    public void leave() {
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
/*
        LinearLayout layout = (LinearLayout)this.getActivity().findViewById(R.id.scroll_linear);
        LinearLayout line = new LinearLayout(this.getActivity());
        line.setOrientation(0);
        Button b1 = new Button(this.getActivity());
        b1.setText("撤销");
        ViewGroup.LayoutParams params = b1.getLayoutParams();
        params.width = 40;
        params.height = 30;
        b1.setLayoutParams(params);
        Button b2 = new Button(this.getActivity());
        b2.setText("撤销");
        Button b3 = new Button(this.getActivity());
        b3.setText("撤销");
        Button b4 = new Button(this.getActivity());
        b4.setText("撤销");
        line.addView(b1);
        line.addView(b2);
        line.addView(b3);
        line.addView(b4);
        layout.addView(line);*/
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
    }

    @Override
    public void refreshTD() {
    }
}
