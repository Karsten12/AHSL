package com.fonsecakarsten.ahsl.Locker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.fonsecakarsten.ahsl.R;

import org.apache.http.client.HttpClient;

public class Locker_Fragment extends Fragment {
    String username;
    String pass;
    HttpClient client;

    public Locker_Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.locker_fragment, container, false);

        /*
        HttpClient client = Utils.getNewHttpClient();

          CookieStore cookies = Utils.getCookies(LockerURL);
          API.get().setAuthCookies(cookies);

        String cookie = API.get().getAuthCookies().getCookies();

        if (! cookies.isEmpty()){

            CookieSyncManager.createInstance(YourContext.this);
            CookieManager cookieManager = CookieManager.getInstance();

            //sync all the cookies in the httpclient with the webview by generating cookie string
            for (Cookie cookie : cookies){

                sessionInfo = cookie;

                String cookieString = sessionInfo.getName() + "=" + sessionInfo.getValue() + "; domain=" + sessionInfo.getDomain();
                cookieManager.setCookie(YOUR_DOMAIN, cookieString);
                CookieSyncManager.getInstance().sync();
            }
        }
        */

        WebView mWebView = (WebView) rootView.findViewById(R.id.webView1);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        String lockerURL = "https://ahs-antioch-ca.schoolloop.com/locker2/view?d=x";
        mWebView.loadUrl(lockerURL);
        mWebView.setWebViewClient(new WebViewClient() {

        });

        return rootView;
    }
    /*
    public void setCookie(String LockerURL, String cookie) {
        CookieSyncManager.createInstance(getActivity());
        CookieManager cookieManager = CookieManager.getInstance();
    }
    */
}