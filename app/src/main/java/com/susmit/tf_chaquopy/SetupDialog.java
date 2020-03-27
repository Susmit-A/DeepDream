package com.susmit.tf_chaquopy;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.susmit.tf_chaquopy.models.Model;

public class SetupDialog extends Dialog {
    public interface SetupFinishedListener {
        public void onSetupFinished();
    }

    EditText steps;
    EditText step_size;
    RadioGroup choices;
    Button positiveButton;

    Context context;

    SetupFinishedListener listener;

    protected SetupDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    protected SetupDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
    }

    protected SetupDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_setup_dialog);

        choices = findViewById(R.id.choices);
        steps = findViewById(R.id.steps);
        step_size = findViewById(R.id.stepSize);
        positiveButton = findViewById(R.id.positiveButton);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(steps.getText().toString().isEmpty() || step_size.getText().toString().isEmpty()){
                    Toast.makeText(context, "Enter values for Steps and Step Size", Toast.LENGTH_SHORT).show();
                    return;
                }
                switch(choices.getCheckedRadioButtonId()) {
                    case -1:
                        Toast.makeText(context, "Select a model", Toast.LENGTH_SHORT).show();
                        return;

                    case R.id.vgg16choice:
                        Globals.modelType = Model.Types.VGG16;
                        break;

                    case R.id.vgg19choice:
                        Globals.modelType = Model.Types.VGG19;
                        break;

                    case R.id.inceptionv3choice:
                        Globals.modelType = Model.Types.InceptionV3;
                        break;

                    case R.id.resnet50choice:
                        Globals.modelType = Model.Types.ResNet50;
                        break;
                }
                Globals.steps = Integer.valueOf(steps.getText().toString());
                Globals.step_size = Float.valueOf(step_size.getText().toString());
                listener.onSetupFinished();
            }
        });
    }

    public void setSetupFinishedListener(SetupFinishedListener listener) {
        this.listener = listener;
    }
}
