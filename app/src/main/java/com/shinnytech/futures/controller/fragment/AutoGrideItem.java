    package com.shinnytech.futures.controller.fragment;

    import android.os.Bundle;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.app.Fragment;
    import com.shinnytech.futures.R;


    public class AutoGrideItem extends Fragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.item_fragment_auto_gride,container,false);
        }
    }
