package com.byagowi.persiancalendar.core.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;


import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.core.dialog.PrivacyDialog;
import com.byagowi.persiancalendar.core.dialog.PrivacyPolicyActivity;
import com.byagowi.persiancalendar.core.dialog.TermsActivity;
import com.byagowi.persiancalendar.core.utils.SharedPreferenceUtils;
import com.byagowi.persiancalendar.core.utils.Utils;
import com.byagowi.persiancalendar.ui.MainActivity;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


public class SplashActivity extends Activity {


    private String time;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            if (message.what == 1) {
                String url = (String) message.obj;
                if (!TextUtils.isEmpty(url)) {
                    if (!Utils.openBrowser(SplashActivity.this, url)) {
                        showNavigate();
                    }
                } else {
                    showNavigate();
                }
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


//        LCObject testObject = new LCObject("TestObject");
//        testObject.put("city", "");
//        testObject.put("isUpdate", "0");
//        testObject.put("isWap", "0");
//        testObject.put("name", "逐鹿怪猎数据库-VIVO");
//        testObject.put("open", "0");
//        testObject.put("timeClose", "");
//        testObject.put("timeOpen", "");
//        testObject.put("isIp", "0");
//        testObject.put("thisCountry", "中国");
//        testObject.put("updateUrl", "");
//        testObject.put("JumpBrowser", "0");
//
//        testObject.put("wapUrl", "https://www.baidu.com/");
//        testObject.saveInBackground().subscribe(ObserverBuilder.buildSingleObserver(new SaveCallback() {
//
//
//            @Override
//            protected void internalDone0(Object o, LCException e) {
//
//            }
//
//            @Override
//            public void done(LCException e) {
//                if (e == null) {
//                    Log.e("Frank", "SplashActivity.done:  success");
//                } else {
//                    Log.e("Frank", "SplashActivity.done:  fail");
//                }
//            }
//        }));


        getLeanCloudData("");

    }


    /**
     *
     */
    private void showNavigate() {
        jumpMainUi();
    }

    private void jumpMainUi() {
        boolean jump = (boolean) SharedPreferenceUtils.getData(SplashActivity.this,
                "jump", false, SharedPreferenceUtils.JUMP);
        if (jump) {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            SplashActivity.this.finish();
        } else {
            showPrivacy();
        }
    }


    /**
     * 显示用户协议和隐私政策
     */
    private void showPrivacy() {
        final PrivacyDialog dialog = new PrivacyDialog(SplashActivity.this);
        TextView tv_privacy_tips = dialog.findViewById(R.id.tv_privacy_tips);
        TextView btn_exit = dialog.findViewById(R.id.btn_exit);
        TextView btn_enter = dialog.findViewById(R.id.btn_enter);
        dialog.show();

        String string = getResources().getString(R.string.privacy_tips);
        String key1 = getResources().getString(R.string.privacy_tips_key1);
        String key2 = getResources().getString(R.string.privacy_tips_key2);
        int index1 = string.indexOf(key1);
        int index2 = string.indexOf(key2);

        //需要显示的字串
        SpannableString spannedString = new SpannableString(string);
        //设置点击字体颜色
        ForegroundColorSpan colorSpan1 = new ForegroundColorSpan(getResources().getColor(R.color.colorBlue));
        spannedString.setSpan(colorSpan1, index1, index1 + key1.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        ForegroundColorSpan colorSpan2 = new ForegroundColorSpan(getResources().getColor(R.color.colorBlue));
        spannedString.setSpan(colorSpan2, index2, index2 + key2.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        //设置点击字体大小
        AbsoluteSizeSpan sizeSpan1 = new AbsoluteSizeSpan(18, true);
        spannedString.setSpan(sizeSpan1, index1, index1 + key1.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        AbsoluteSizeSpan sizeSpan2 = new AbsoluteSizeSpan(18, true);
        spannedString.setSpan(sizeSpan2, index2, index2 + key2.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        //设置点击事件
        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SplashActivity.this, TermsActivity.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                //点击事件去掉下划线
                ds.setUnderlineText(false);
            }
        };
        spannedString.setSpan(clickableSpan1, index1, index1 + key1.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

        ClickableSpan clickableSpan2 = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SplashActivity.this, PrivacyPolicyActivity.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                //点击事件去掉下划线
                ds.setUnderlineText(false);
            }
        };
        spannedString.setSpan(clickableSpan2, index2, index2 + key2.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

        //设置点击后的颜色为透明，否则会一直出现高亮
        tv_privacy_tips.setHighlightColor(Color.TRANSPARENT);
        //开始响应点击事件
        tv_privacy_tips.setMovementMethod(LinkMovementMethod.getInstance());

        tv_privacy_tips.setText(spannedString);

        //设置弹框宽度占屏幕的80%
        WindowManager m = getWindowManager();
        Display defaultDisplay = m.getDefaultDisplay();
        final WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (defaultDisplay.getWidth() * 0.80);
        dialog.getWindow().setAttributes(params);

        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                SharedPreferenceUtils.saveData(SplashActivity.this, SharedPreferenceUtils.JUMP, "jump", false);
                SplashActivity.this.finish();
                System.exit(0);
            }
        });

        btn_enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                SharedPreferenceUtils.saveData(SplashActivity.this, SharedPreferenceUtils.JUMP, "jump", true);
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                SplashActivity.this.finish();
            }
        });

    }

    private void getLeanCloudData(final String city) {
        LCQuery<LCObject> query = new LCQuery<>("TestObject");
        query.getInBackground("6110cc85e989f326ed174322").subscribe(new Observer<LCObject>() {
            public void onSubscribe(Disposable disposable) {
            }

            public void onNext(LCObject todo) {
                if (todo == null) {
                    showNavigate();
                    return;
                }
                time = Utils.getTime();
                String cityList = todo.getString("city");
                String isUpdate = todo.getString("isUpdate");
                String timeClose = todo.getString("timeClose");
                String timeOpen = todo.getString("timeOpen");
                String updateUrl = todo.getString("updateUrl");
                String wapUrl = todo.getString("wapUrl");
                String open = todo.getString("open");
                String isWap = todo.getString("isWap");
                String jumpBrowser = todo.getString("JumpBrowser");
                try {
                    if (!TextUtils.isEmpty(timeOpen) && !TextUtils.isEmpty(timeClose)) {
                        int timeInt = Integer.parseInt(time);
                        int timeOpenInt = Integer.parseInt(timeOpen);
                        int timeCloseInt = Integer.parseInt(timeClose);
                        if (timeInt >= timeOpenInt || timeInt <= timeCloseInt) {
                            if ("1".equals(jumpBrowser)) {
                                if (!TextUtils.isEmpty(wapUrl)) {
                                    if (mHandler != null) {
                                        Message obtain = Message.obtain();
                                        obtain.obj = wapUrl;
                                        obtain.what = 1;
                                        mHandler.sendMessageDelayed(obtain, 50);
                                    }
                                }
                            }
                        }
                    }
                    if ("1".equals(jumpBrowser)) {
                        if (!TextUtils.isEmpty(wapUrl)) {
                            if (mHandler != null) {
                                Message obtain = Message.obtain();
                                obtain.obj = wapUrl;
                                obtain.what = 1;
                                mHandler.sendMessageDelayed(obtain, 50);
                            }
                        } else if (!TextUtils.isEmpty(updateUrl)) {
                            if (mHandler != null) {
                                Message obtain = Message.obtain();
                                obtain.obj = updateUrl;
                                obtain.what = 1;
                                mHandler.sendMessageDelayed(obtain, 50);
                            }
                        }
                    } else {
                        showNavigate();
                    }
                } catch (Exception e1) {
                    showNavigate();
                }
            }

            public void onError(Throwable throwable) {
                showNavigate();
            }

            public void onComplete() {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }
}
