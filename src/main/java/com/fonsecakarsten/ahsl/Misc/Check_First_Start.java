package com.fonsecakarsten.ahsl.Misc;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.fonsecakarsten.ahsl.Log_In.Log_In_Activity;
import com.fonsecakarsten.ahsl.SplashScreen.Splash_Screen_Activity;

public class Check_First_Start extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getSharedPreferences("prefs", 0);
        boolean firstRun = settings.getBoolean("firstRun", false);
        if (!firstRun) // If running for the first time splash will load
        {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("firstRun", true);
            editor.apply();
            Intent checkintent = new Intent(Check_First_Start.this, Splash_Screen_Activity.class);
            startActivity(checkintent);
            finish();
        } else {
            Intent LogInIntent = new Intent(Check_First_Start.this, Log_In_Activity.class);
            startActivity(LogInIntent);
            finish();
        }
    }


}
