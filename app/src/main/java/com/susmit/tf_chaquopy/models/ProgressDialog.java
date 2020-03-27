package com.susmit.tf_chaquopy.models;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.susmit.tf_chaquopy.R;

class ProgressDialog extends AlertDialog {
    private ProgressBar progress;
    private TextView downloadText;
    private Context context;

    public ProgressDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_progress_dialog);
        progress = findViewById(R.id.downloadProgress);
        downloadText = findViewById(R.id.downloadText);
        setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancel();
            }
        });
    }

    public void setText(String text) {
        downloadText.setText(text);
    }

    public CharSequence getText() {
        return downloadText.getText();
    }

    public void updateProgress(int prog) {
        progress.setProgress(prog, true);
    }

    public void setContentSize(int size){
        progress.setMax(size);
    }
}
