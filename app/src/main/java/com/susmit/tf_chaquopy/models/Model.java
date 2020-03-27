package com.susmit.tf_chaquopy.models;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.chaquo.python.Python;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

public abstract class Model {
    public interface OnDownloadCompleteListener {
        public void onDownloadComplete(Model model);
        public void runOnUiThread(Model model);
        public void onDownCancelled(Model model);
    }

    public interface OnModelLoadedListener {
        public void onLoaded(Model model);
    }

    private OnDownloadCompleteListener onDownloadCompleteListener;
    protected String weights_path;
    protected String weights_url;
    private Thread downloader;

    public static enum Types {
        VGG16,
        VGG19,
        ResNet50,
        InceptionV3
    }

    protected Types type;
    HashMap<Types, String> modelMap;
    protected Activity activity;

    public Model(Activity activity){
        this.activity = activity;
        modelMap = new HashMap<>();
        modelMap.put(Types.VGG16,
            "https://github.com/Susmit-A/DeepDreamWeights/blob/master/VGG16/VGG_full.h5?raw=true"
        );
        modelMap.put(Types.VGG19,
            "https://github.com/Susmit-A/DeepDreamWeights/blob/master/VGG19/VGG_full.h5?raw=true"
        );
        modelMap.put(Types.InceptionV3,
            "https://github.com/Susmit-A/DeepDreamWeights/raw/master/InceptionV3/InceptionV3_full.h5"
        );
        modelMap.put(Types.ResNet50,
            "https://github.com/Susmit-A/DeepDreamWeights/raw/master/ResNet50/ResNetV2_full.h5"
        );
    }

    public void setOnDownloadCompleteListener(OnDownloadCompleteListener listener) {
        onDownloadCompleteListener = listener;
    }

    public void downloadAsync(final Types type) {
        if (weights_path == null)
            weights_path = activity.getDataDir().getAbsolutePath() + "/" + type.toString() + ".h5";
        if (weights_url == null)
            weights_url = modelMap.get(type);
        downloader = new Thread(new Runnable() {
            ProgressDialog dialog;
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, "Started Download", Toast.LENGTH_LONG).show();
                        dialog = new ProgressDialog(activity);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                downloader.interrupt();
                                onDownloadCompleteListener.onDownCancelled(Model.this);
                            }
                        });
                        dialog.create();
                        dialog.show();
                        dialog.setText("Downloading " + type.toString());
                        dialog.updateProgress(0);
                    }
                });
                try {
                    URL u = new URL(weights_url);
                    URLConnection conn = u.openConnection();
                    final int contentLength = conn.getContentLength();
                    dialog.setContentSize(contentLength);
                    DataOutputStream fos = new DataOutputStream(
                            new FileOutputStream(
                                    new File(weights_path)
                            )
                    );
                    DataInputStream stream = new DataInputStream(u.openStream());
                    int bufsize = 1024;
                    byte[] buffer = new byte[bufsize];
                    int downloaded = 0;
                    int length;
                    while((length = stream.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                        final int dl = downloaded;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.setText("Downloading " + type.toString() + "\n\n" + dl + "/" + contentLength + " bytes");
                                dialog.updateProgress(dl);
                            }
                        });
                        downloaded += bufsize;
                    }
                    fos.flush();
                    fos.close();
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, "Finished Download", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        onDownloadCompleteListener.runOnUiThread(Model.this);
                    }
                });
                onDownloadCompleteListener.onDownloadComplete(Model.this);
            }
        });
        downloader.start();
    }

    abstract public void loadModel(Python py);
    abstract public void loadModelAsync(Python py, OnModelLoadedListener listener);
    abstract public boolean exists();
    abstract public byte[] dream(byte[] image, int steps, float step_size);
    abstract public void downloadAsync();
    abstract public Types getType();
}
