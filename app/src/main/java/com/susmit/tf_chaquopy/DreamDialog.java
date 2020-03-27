package com.susmit.tf_chaquopy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class DreamDialog extends Dialog {

    private ProgressBar progress;
    private TextView dreamText;
    private Button cancelDream;
    private Activity context;

    public DreamDialog(@NonNull Activity context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_dream_dialog);
        progress = findViewById(R.id.dreamProgress);
        dreamText = findViewById(R.id.dreamText);
        cancelDream = findViewById(R.id.cancelDream);
        cancelDream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
    }

    public void setText(String text) {
        dreamText.setText(text);
    }

    public CharSequence getText() {
        return dreamText.getText();
    }

    public void updateProgress(int prog) {
        progress.setProgress(prog, true);
    }

    public void setContentSize(int size){
        progress.setMax(size);
    }
}
