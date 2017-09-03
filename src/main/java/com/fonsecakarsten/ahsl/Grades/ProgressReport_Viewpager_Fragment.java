package com.fonsecakarsten.ahsl.Grades;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fonsecakarsten.ahsl.Assignments.Assignments_Fragment;
import com.fonsecakarsten.ahsl.R;
import com.fonsecakarsten.ahsl.Tabs.SlidingTabLayout;

public class ProgressReport_Viewpager_Fragment extends Fragment {

    public ProgressReport_Viewpager_Fragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.viewpager_fragment, container, false);
        MyPagerAdapter3 mypageradapter = new MyPagerAdapter3(getChildFragmentManager());
        ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.pager);
        viewPager.setAdapter(mypageradapter);

        //Bind the title indicator to the adapter
        SlidingTabLayout mTabs = (SlidingTabLayout) rootView.findViewById(R.id.tabs);
        mTabs.setSelectedIndicatorColors(getResources().getColor(R.color.White));
        mTabs.setViewPager(viewPager);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
}

class MyPagerAdapter3 extends FragmentPagerAdapter {
    private final String[] Titles = {"Grades", "Assignments"};

    public MyPagerAdapter3(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new Grades_Fragment();
                break;
            case 1:
                fragment = new Assignments_Fragment();
            default:
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return Titles[position];
    }
}