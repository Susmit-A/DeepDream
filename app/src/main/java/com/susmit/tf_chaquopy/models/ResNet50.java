package com.susmit.tf_chaquopy.models;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

import java.io.File;

public class ResNet50 extends Model {
    PyObject modelWrapper;

    public ResNet50(Activity activity) {
        super(activity);
        type = type = getType();;
        weights_path = activity.getDataDir().getAbsolutePath() + "/" + type.toString() + ".h5";
        weights_url = modelMap.get(type);
    }

    @Override
    public void loadModel(Python py) {
        if(modelWrapper == null) {
            modelWrapper = py.getModule("Model").callAttr("Model", weights_path);
        }
    }

    @Override
    public void loadModelAsync(final Python py, final OnModelLoadedListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadModel(py);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onLoaded(ResNet50.this);
                    }
                });
            }
        }).start();
    }

    @Override
    public boolean exists() {
        return new File(weights_path).exists();
    }

    @Override
    public void downloadAsync() {
        super.downloadAsync(type);
    }

    @Override
    public byte[] dream(byte[] image, int steps, float step_size) {
        return modelWrapper.callAttr("dream", activity, image, steps, step_size).toJava(byte[].class);
    }

    @Override
    public Types getType() {
        return Types.ResNet50;
    }
}
