package com.fonsecakarsten.ahsl;

import android.app.IntentService;
import android.content.Intent;

import com.fonsecakarsten.ahsl.Misc.API;

import java.io.IOException;

public class KeepAliveService extends IntentService {
    private static final int UPDATE_INTERVAL = (int) (60 * 1000 * 8); // set to update every 8 minutes
    private volatile boolean isToRun;

    public KeepAliveService() {
        super("KeepAliveService");
        isToRun = true;
    }

    public void stopKeepAlive() {
        //System.out.println("---------- STOPPING KEEP ALIVE ------------");
        isToRun = false;
    }

    private void doKeepAlive() {
        try {
            //System.out.println("---------- REFRESHING PORTAL ------------");
            API.get().preventCookieExpire();
        } catch (IOException e) {
            //Toast.makeText(context, "Failed to refresh School Loop. Please check your " +
            //						"Internet connection and re-login.", Toast.LENGTH_LONG).show();
            return;
        }

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        new Thread(new Runnable() {
            public void run() {
                while (isToRun) {
                    synchronized (this) {
                        try {
                            Thread.sleep(UPDATE_INTERVAL);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        doKeepAlive();
                    }
                }
            }
        }).start();
    }
}

