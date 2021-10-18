package com.byagowi.persiancalendar.core.dialog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.core.utils.ToolsCore;
import com.byagowi.persiancalendar.core.utils.Utils;


/**
 * 用户协议
 */
public class TermsActivity extends Activity {

    private static final String TAG = TermsActivity.class.getSimpleName();

    private LinearLayout web_view_container;
    private Toolbar layout_terms_title;
    private WebView web_view;

    private final String LANGUAGE_CN = "zh-CN";
    private int flag;

    private boolean networkConnected;
    //声明进度条对话框
    private ProgressDialog pdDialog = null;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            if (message.what == 1) {
                if (pdDialog != null && pdDialog.isShowing())
                    pdDialog.dismiss();
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);
        initView();
    }

    private void initView() {
        //创建ProgressDialog对象
        pdDialog = new ProgressDialog(this);
        pdDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pdDialog.setMessage("Loading...");
        pdDialog.show();
        layout_terms_title = findViewById(R.id.layout_terms_title);
        web_view_container = findViewById(R.id.web_view_container);
        web_view = new WebView(getApplicationContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        web_view.setLayoutParams(params);
        web_view.setWebViewClient(new WebViewClient());
        //动态添加WebView，解决在xml引用WebView持有Activity的Context对象，导致内存泄露
        web_view_container.addView(web_view);
        Utils.setWeb(web_view, this);

//        web_view.loadUrl("http://note.youdao.com/noteshare?id=4189f785f7226a2226167b1a0a1ba592&sub=F4A05EDEE5D64473A92361E63EC837AE");

        web_view.getSettings().setTextZoom(300);
        web_view.loadDataWithBaseURL(null, Utils.readAssetsTxt(this,"terms"),
                "text/html", "utf-8", null);


        web_view.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                networkConnected = ToolsCore.isNetworkConnected(TermsActivity.this);
                if (!networkConnected) {
                    view.setVisibility(View.GONE);
                    Toast.makeText(TermsActivity.this, "未联网，请打开网络", Toast.LENGTH_SHORT).show();
                } else {
                    view.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (pdDialog != null) {
                    mHandler.sendEmptyMessageDelayed(1, 7000);
                }
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                view.loadUrl(ToolsCore.javascript);
                view.loadUrl("javascript:displayNone('.banner');" +
                        "displayNone('.comment-area');" +
                        "displayNone('.mianze');" +
                        "displayNone('.file-info-drawer');" +
                        "displayNone('.content-bottom');" +
                        "displayNone('#disclaimer');" +
                        "displayNone('#app-dl');" +
                        "displayNone('#footer-wrap');" +
                        "displayNone('#img-sdk');" +
                        "displayNone('#text-sdk');" +
                        "displayNone('.lg-content');" +
                        "displayNone('.login');" +
                        "displayNone('.save-note');" +
                        "displayNone('#jubaoUrl');");

            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (request.isForMainFrame()) {//是否是为 main frame创建
                        //这个布局为出现网络异常时显示的布局
//                        tab1_net_layout.setVisibility(View.VISIBLE);
//                        view.setVisibility(View.GONE);
                        if (flag > 1) {
                            view.setVisibility(View.GONE);
                            return;
                        }
                        view.reload();
                        flag++;
                    }
                }
            }
        });

        web_view.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                // android 6.0 以下通过title获取判断
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    if (title.contains("404") || title.contains("500") || title.contains("Error") || title.contains("找不到网页") || title.contains("网页无法打开")) {
                        if (flag > 1) {
                            view.setVisibility(View.GONE);
                            return;
                        }
                        view.reload();
                        flag++;
                    }
                }
            }
        });

        layout_terms_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TermsActivity.this.finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        web_view_container.removeAllViews();
        web_view.destroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }
}
