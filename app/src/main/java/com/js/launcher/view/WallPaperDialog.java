package com.js.launcher.view;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.js.launcher.R;

import androidx.annotation.NonNull;

public class WallPaperDialog extends Dialog {

    private TextView tvMessage;
    private LinearLayout btnSettings, btnCancel;

    public WallPaperDialog(@NonNull Context context) {
        super(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_wallpaper, null);

        tvMessage = view.findViewById(R.id.tv_message);
        btnSettings = view.findViewById(R.id.btn_settings);
        btnCancel = view.findViewById(R.id.btn_cancel);

        setContentView(view);
    }

    public void setMessage(String text) {
        tvMessage.setText(text);
    }

    public void setWallpaperSettingsOnClickListener(View.OnClickListener onClickListener) {
        btnSettings.setOnClickListener(onClickListener);
    }

    public void setWallpaperCancelOnClickListener(View.OnClickListener onClickListener) {
        btnCancel.setOnClickListener(onClickListener);
    }
}
