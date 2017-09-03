package com.fonsecakarsten.ahsl;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.fonsecakarsten.ahsl.Calendar.Calendar_Day_Fragment;
import com.fonsecakarsten.ahsl.Grades.ProgressReport_Viewpager_Fragment;
import com.fonsecakarsten.ahsl.Locker.Locker_Fragment;
import com.fonsecakarsten.ahsl.LoopMail.Loop_Mail_Viewpager_Fragment;
import com.fonsecakarsten.ahsl.Misc.API;
import com.fonsecakarsten.ahsl.Misc.Utils;
import com.fonsecakarsten.ahsl.News.News_Fragment;
import com.fonsecakarsten.ahsl.ReportCard.ReportCard_Fragment;


public class MainActivity extends AppCompatActivity {
    private String[] mNavigationDrawerItemTitles;
    private DrawerLayout mDrawerLayout;
    private RelativeLayout mDrawerLinear;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private int SelectedPosition = 0;
    private static final String PRIVATE_PREF = "myapp";
    private static final String VERSION_KEY = "version_number";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        mTitle = mDrawerTitle = getTitle();
        mNavigationDrawerItemTitles = getResources().getStringArray(R.array.navigation_drawer_items_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLinear = (RelativeLayout) findViewById(R.id.left_drawer);
        mDrawerList = (ListView) findViewById(R.id.left_drawer_child);

        // Define objects in drawer
        ObjectDrawerItem[] drawerItem = new ObjectDrawerItem[7];
        drawerItem[0] = new ObjectDrawerItem(R.drawable.ic_account, (API.get().getPortalTitle()));
        drawerItem[1] = new ObjectDrawerItem(R.drawable.ic_progress, "Progress Report");
        drawerItem[2] = new ObjectDrawerItem(R.drawable.ic_mail, "Loop Mail");
        drawerItem[3] = new ObjectDrawerItem(R.drawable.ic_calendar, "Calendar");
        drawerItem[4] = new ObjectDrawerItem(R.drawable.ic_locker, "Locker");
        drawerItem[5] = new ObjectDrawerItem(R.drawable.ic_news, "News");
        drawerItem[6] = new ObjectDrawerItem(R.drawable.ic_info, "About");

        // Set the adapter for the list view
        DrawerItemCustomAdapter adapter = new DrawerItemCustomAdapter(this, drawerItem);
        mDrawerList.setAdapter(adapter);

        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar icon to behave as action to toggle navigation drawer
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mTitle);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(mDrawerTitle);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // Load Progress Fragment after LogIn intent
        if (savedInstanceState == null) {
            selectItem(1);
        }

        Intent KEEP_ALIVE_TASK = new Intent(this, KeepAliveService.class);
        startService(KEEP_ALIVE_TASK);

    }

    /*
    // SHOW UPDATE DIALOG
    private void init() {
        SharedPreferences sharedPref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
        int currentVersionNumber = 0;

        int savedVersionNumber = sharedPref.getInt(VERSION_KEY, 0);
        try {
            PackageInfo pi =
                    getPackageManager().getPackageInfo(getPackageName(), 0);
            currentVersionNumber = pi.versionCode;
        } catch (Exception e) {
        }
        if (currentVersionNumber > savedVersionNumber) {
            //    showWhatsNewDialog();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(VERSION_KEY, currentVersionNumber);
            editor.commit();
        }
    }

    private void showsWhatsNewDialog() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_alert)
                .setTitle("Changelog")
                .setMessage("v0.8")
                .setPositiveButton("continue",null)
                .show();
    }
    */

    // change title when opening new fragment
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(mTitle);
    }

    @SuppressWarnings("UnusedParameters")
    public void LogOut_button(View view) {
        Utils.logOut(this);
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
    ..    MenuInflater inflater = getMenuInflater();
    //    inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }
    */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //	switch(item.getItemId()) {
        //		case R.id.action_new_mail:

        // 	}
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        return mDrawerToggle.onOptionsItemSelected(item);

    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mDrawerLinear)) {
            mDrawerLayout.closeDrawer(mDrawerLinear);
        } else {
            if (SelectedPosition != 1) {
                selectItem(1);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);

        }

    }

    void selectItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new ReportCard_Fragment();
                break;
            case 1:
                fragment = new ProgressReport_Viewpager_Fragment();
                break;
            case 2:
                fragment = new Loop_Mail_Viewpager_Fragment();
                break;
            case 3:
                fragment = new Calendar_Day_Fragment();
                break;
            case 4:
                fragment = new Locker_Fragment();
                break;
            case 5:
                fragment = new News_Fragment();
                break;
            case 6:
                fragment = new About_Fragment();
            default:
                break;
        }
        SelectedPosition = position;

        if (fragment != null) {
            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.content_frame, fragment).commit();
            //       FragmentManager fragmentManager = getFragmentManager();
            //       fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            setTitle(mNavigationDrawerItemTitles[position]);
            mDrawerLayout.closeDrawer(mDrawerLinear);

        } else {
            Log.e("MainActivity", "Error in creating fragment");
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }
}



