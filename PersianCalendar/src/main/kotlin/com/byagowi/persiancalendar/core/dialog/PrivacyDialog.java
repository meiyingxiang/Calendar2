package com.byagowi.persiancalendar.core.dialog;

import android.app.Dialog;
import android.content.Context;

import com.byagowi.persiancalendar.R;


public class PrivacyDialog extends Dialog {

    public PrivacyDialog(Context context) {
        super(context, R.style.PrivacyThemeDialog);

        setContentView(R.layout.dialog_privacy);

        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }
}
