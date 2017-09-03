package com.fonsecakarsten.ahsl.SplashScreen;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.fonsecakarsten.ahsl.R;
import com.viewpagerindicator.CirclePageIndicator;


public class Splash_Screen_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_viewpager);

        MyPagerAdapter myPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager2);
        viewPager.setAdapter(myPagerAdapter);

        // Bind the title indicator to the adapter
        CirclePageIndicator mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
        mIndicator.setViewPager(viewPager);

        mIndicator.setRadius(10);
        mIndicator.setFillColor(getResources().getColor(R.color.SlateGray));
        mIndicator.setStrokeColor(getResources().getColor(R.color.Black));
        mIndicator.setBackgroundColor(getResources().getColor(R.color.LightSteelBlue));

    }
}

class MyPagerAdapter extends FragmentPagerAdapter {
    public MyPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = null;
        switch (i) {
            case 0:
                fragment = new Introduce_App_Fragment();
                break;
            case 1:
                fragment = new Credits_Fragment();
            default:
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }
}