package com.byagowi.persiancalendar.core.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工具类
 */
public class Utils {

    /**
     * 获取当前的时间
     *
     * @return
     */
    public static String getTime() {
        Calendar cal;
        String hour;
        cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        if (cal.get(Calendar.AM_PM) == 0) {
            hour = String.valueOf(cal.get(Calendar.HOUR));
        } else {
            hour = String.valueOf(cal.get(Calendar.HOUR) + 12);
        }
        return hour;
    }

    public static String readAssetsTxt(Context context, String fileName) {
        try {
            StringBuilder sb = new StringBuilder();
            InputStream is = context.getResources().getAssets().open(fileName + ".txt");
            InputStreamReader isReader = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                isReader = new InputStreamReader(is, StandardCharsets.UTF_8);
            } else {
                isReader = new InputStreamReader(is, "UTF-8");
            }
            //使用bufferReader去读取内容
            BufferedReader reader = new BufferedReader(isReader);
            String out = "";
            while ((out = reader.readLine()) != null) {
                sb.append(out).append("<br>");
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "读取失败";
    }

    /**
     * 字符Base64加密
     *
     * @param str
     * @return
     */
    public static String encodeToString(String str) {
        try {
            return Base64.encodeToString(str.getBytes("UTF-8"), Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean openBrowser(Context context, String url) {
        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        // 注意此处的判断intent.resolveActivity()可以返回显示该Intent的Activity对应的组件名
        // 官方解释 : Name of the component implementing an activity that can display the intent
        if (intent.resolveActivity(context.getPackageManager()) != null) {
//            context.startActivity(Intent.createChooser(intent, "请选择浏览器"));
            context.startActivity(intent);
            return true;
        }
        return false;
    }

    /**
     * 字符Base64解密
     *
     * @param str
     * @return
     */
    public static String decodeToString(String str) {
        try {
            return new String(Base64.decode(str.getBytes("UTF-8"), Base64.DEFAULT));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }


    public static Integer[] getWidthAndHeight(Window window) {
        if (window == null) {
            return null;
        }
        Integer[] integer = new Integer[2];
        DisplayMetrics dm = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        } else {
            window.getWindowManager().getDefaultDisplay().getMetrics(dm);
        }
        integer[0] = dm.widthPixels;
        integer[1] = dm.heightPixels;
        return integer;
    }


    public static String getPic() {
        Random random = new Random();
        return "http://106.14.135.179/ImmersionBar/" + random.nextInt(40) + ".jpg";
    }

    public static ArrayList<String> getPics() {
        return getPics(4);
    }

    public static ArrayList<String> getPics(int num) {
        ArrayList<String> pics = new ArrayList<>();
        Random random = new Random();

        do {
            String s = "http://106.14.135.179/ImmersionBar/" + random.nextInt(40) + ".jpg";
            if (!pics.contains(s)) {
                pics.add(s);
            }
        } while (pics.size() < num);
        return pics;
    }

    public static String getFullPic() {
        Random random = new Random();
        return "http://106.14.135.179/ImmersionBar/phone/" + random.nextInt(40) + ".jpeg";
    }


    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = manager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }


    /**
     * 判断是否为手机号
     *
     * @param phone
     * @return
     */
    public static boolean isPhone(String phone) {
        String regex = "^((13[0-9])|(14[5,7,9])|(15([0-3]|[5-9]))|(166)|(17[0,1,3,5,6,7,8])|(18[0-9])|(19[8|9]))\\d{8}$";
        if (phone.length() != 11) {
            return false;
        } else {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(phone);
            return m.matches();
        }
    }


    public static void setWeb(final WebView webView, Activity activity) {
        // 禁止即在网页顶出现一个空白，又自动回去
//        webView.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        try {
            webView.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        } catch (Throwable e) {
            String messageCause = e.getCause() == null ? e.toString() : e.getCause().toString();
            String trace = Log.getStackTraceString(e);
            if (trace.contains("android.content.pm.PackageManager$NameNotFoundException")
                    || trace.contains("java.lang.RuntimeException: Cannot load WebView")
                    || trace.contains("android.webkit.WebViewFactory$MissingWebViewPackageException: Failed to load WebView provider: No WebView installed")) {
                e.printStackTrace();
            } else {
                throw e;
            }
        }
        WebSettings webSettings = webView.getSettings();
        webSettings.setUseWideViewPort(true);
        //适应屏幕，内容将自动缩放
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        //PC网页里面没有设置 meta标签 viewport的缩放设置也没有关系
        webSettings.setUseWideViewPort(true);
        // 充满全屏幕
        webSettings.setLoadWithOverviewMode(true);
        //设定缩放控件隐藏
        webSettings.setDisplayZoomControls(false);
        // //启用或禁用DOM缓存
        webSettings.setDomStorageEnabled(true);
        // 设置显示缩放按钮
        webSettings.setBuiltInZoomControls(false);
        // 把图片加载放在最后来加载渲染
        webSettings.setBlockNetworkImage(false);
        // 支持缩放
        webSettings.setSupportZoom(true);
        // 允许访问文件
        webSettings.setAllowFileAccess(true);
        //设置是否打开 WebView 表单数据的保存功能
        webSettings.setSaveFormData(true);
        //打开 WebView 的 LBS 功能，这样 JS 的 geolocation 对象才可以使用
        webSettings.setGeolocationEnabled(true);
        //开启 database storage API 功能
        webSettings.setDatabaseEnabled(true);
        //支持JavaScript
        webSettings.setJavaScriptEnabled(true);
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int mDensity = metrics.densityDpi;
        if (mDensity == 240) {
            webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
        } else if (mDensity == 160) {
            webSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
        } else if (mDensity == 120) {
            webSettings.setDefaultZoom(WebSettings.ZoomDensity.CLOSE);
        } else if (mDensity == DisplayMetrics.DENSITY_XHIGH) {
            webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
        } else if (mDensity == DisplayMetrics.DENSITY_TV) {
            webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
        } else {
            webSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
        }
        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK
                            // 表示按返回键
                            && webView.canGoBack()) {
                        // 后退
                        webView.goBack();
                        // 已处理
                        return true;
                    }
                }
                return false;
            }
        });
        //不加这个图片显示不出来
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        //允许cookie 不然有的网站无法登陆
        CookieManager mCookieManager = CookieManager.getInstance();
        mCookieManager.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCookieManager.setAcceptThirdPartyCookies(webView, true);
        }
    }

}
