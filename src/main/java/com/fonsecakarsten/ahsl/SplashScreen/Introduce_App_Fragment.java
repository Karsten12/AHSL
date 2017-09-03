package com.fonsecakarsten.ahsl.SplashScreen;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fonsecakarsten.ahsl.R;


public class Introduce_App_Fragment extends Fragment {

    public Introduce_App_Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.introduce_fragment, container, false);
    }

}
