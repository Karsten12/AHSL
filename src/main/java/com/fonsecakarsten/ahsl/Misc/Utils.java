package com.fonsecakarsten.ahsl.Misc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.fonsecakarsten.ahsl.Constants;
import com.fonsecakarsten.ahsl.Log_In.Log_In_Activity;
import com.fonsecakarsten.ahsl.MySSLSocketFactory;
import com.fonsecakarsten.ahsl.R;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.KeyStore;

public class Utils {
    public static void printHTTPResponse(InputStream entityStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(entityStream));
        String result;

        while ((result = reader.readLine()) != null)
            System.out.println(result);
    }

    public static BasicHttpContext getCookifiedHttpContext(CookieStore cookies) {
        BasicHttpContext cookiedContext = new BasicHttpContext();
        cookiedContext.setAttribute(ClientContext.COOKIE_STORE, cookies);

        return cookiedContext;
    }

    public static Document getJsoupDocFromUrl(String url, String baseUrl, CookieStore cookies) throws IllegalStateException, IOException {
        HttpClient client = getNewHttpClient();
        BasicHttpContext context = Utils.getCookifiedHttpContext(cookies);
        HttpGet httpGet = new HttpGet(url);

        HttpResponse response = client.execute(httpGet, context);

        return Jsoup.parse(response.getEntity().getContent(), null, baseUrl);
    }

    public static HttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }

    public static void lockOrientation(final Activity act) {
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // lock orientation to avoid crash during login
                int currOrientation = act.getResources().getConfiguration().orientation;

                if (currOrientation == Configuration.ORIENTATION_LANDSCAPE)
                    act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                else
                    act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        });
    }

    public static void unlockOrientation(final Activity act) {
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // unlock orientation after logging in
                act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }
        });
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return (netInfo != null && netInfo.isConnectedOrConnecting());
    }

    public static String getPrintViewifiedUrl(String rootUrl) {
        return rootUrl + "&template=print";
    }

    public static CookieStore getCookies(String url) {
        DefaultHttpClient client = (DefaultHttpClient) Utils.getNewHttpClient();
        HttpGet httpGet = new HttpGet(url);

        try {
            client.execute(httpGet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return client.getCookieStore();
    }

    public static String getExceptionString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        return sw.toString();
    }

    // Log out dialog
    public static void logOut(final Activity activity) {
        new AlertDialog.Builder(activity)
                .setIcon(R.drawable.ic_alert)
                .setTitle("Exit AHSL?")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(activity, "Logged out successfully.", Toast.LENGTH_SHORT).show();

                        Thread logOutThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (Utils.isOnline(activity)) {
                                    API.get().logOut();
                                }
                            }
                        });

                        logOutThread.start();

                        Intent LogOutIntent = new Intent(activity.getApplicationContext(), Log_In_Activity.class);
                        LogOutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        LogOutIntent.putExtra(Log_In_Activity.IS_FROM_LOGOUT, true);

                        activity.startActivity(LogOutIntent);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public static TextView getCenteredTextView(Context c, String s) {
        TextView text = new TextView(c);

        text.setPadding(40, 40, 40, 40);
        text.setGravity(Gravity.CENTER);
        //noinspection ResourceType
        text.setTextAppearance(c, android.R.attr.textAppearanceLarge);
        text.setText(s);

        return text;
    }

    public static void showViewOnTop(View a, View b) {
        ViewGroup parent = (ViewGroup) a.getParent();

        parent.addView(b);
        parent.bringChildToFront(b);
    }

    public static Dialog createLoopedDialog(Context parent, View contentView, int width) {

        return createLoopedDialog(parent, contentView, width, R.id.popup_dismiss);
    }

    public static Dialog createLoopedDialog(Context parent, View contentView, int width, int exitBtnResId) {
        final Dialog popup = new Dialog(parent);

        popup.requestWindowFeature(Window.FEATURE_NO_TITLE);
        popup.setContentView(contentView, new LayoutParams(width - ((int) (0.1 * width)), LayoutParams.WRAP_CONTENT));

        contentView.findViewById(R.id.popup_dismiss).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.safelyDismissDialog(popup);
            }
        });

        return popup;
    }

    public static boolean isNetworkOffline(Context parent) {
        boolean isOffline = false;

        if (!isOnline(parent)) {
            Toast.makeText(parent, "Internet connectivity is lost. Please re-connect and try again.", Toast.LENGTH_LONG).show();
            isOffline = true;
        }

        return isOffline;
    }

    public static void safelyDismissDialog(Dialog dialog) {
        try {
            if (dialog != null)
                dialog.dismiss();
        } catch (Exception ignored) {
        }
    }

    public static void doDemographicLog(String url) {
        //	RemoteDebug.setUrl(url);
        String request = Constants.LOGIN_CHECK + "?c=" + url;

        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(request);

        try {
            httpclient.execute(httpGet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

}
