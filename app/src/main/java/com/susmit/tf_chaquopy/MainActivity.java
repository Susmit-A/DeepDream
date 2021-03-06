package com.susmit.tf_chaquopy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.camerakit.CameraKitView;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.susmit.tf_chaquopy.models.InceptionV3;
import com.susmit.tf_chaquopy.models.Model;
import com.susmit.tf_chaquopy.models.ResNet50;
import com.susmit.tf_chaquopy.models.VGG16;
import com.susmit.tf_chaquopy.models.VGG19;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class MainActivity extends Activity {

    static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }

    Python py;

    Model model;
    SetupDialog setupDialog;

    static final int CAMERA_PERMISSION_CODE = 80;
    static final int READ_PERMISSION_CODE = 88;
    static final int FETCH_IMAGE_CODE = 89;

    FloatingActionButton cameraButton;
    CameraKitView cameraView;
    BottomNavigationView optionsMenu;

    boolean setupFinished = false;

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, READ_PERMISSION_CODE);
        }

        if(!Python.isStarted())
            Python.start(new AndroidPlatform(MainActivity.this));
        py = Python.getInstance();

        cameraView = findViewById(R.id.cameraView);
        cameraButton = findViewById(R.id.camera);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Globals.step_size <= 0 || Globals.steps <= 0) {
                    Toast.makeText(MainActivity.this, "Enter valid values for Steps and Step Size", Toast.LENGTH_LONG).show();
                    return;
                }
                cameraView.captureImage(new CameraKitView.ImageCallback() {
                    @Override
                    public void onImage(CameraKitView cameraKitView, byte[] bytes) {
                        MediaActionSound sound = new MediaActionSound();
                        sound.play(MediaActionSound.SHUTTER_CLICK);
                        new DreamTaskRaw(MainActivity.this).execute(bytes);
                    }
                });
            }
        });

        setupDialog = new SetupDialog(MainActivity.this);
        setupDialog.setCanceledOnTouchOutside(false);
        setupDialog.setOnDownloadClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupDialog.dismiss();
                switch(Globals.modelType){
                    case VGG16:
                        model = new VGG16(MainActivity.this);
                        break;

                    case VGG19:
                        model = new VGG19(MainActivity.this);
                        break;

                    case InceptionV3:
                        model = new InceptionV3(MainActivity.this);
                        break;

                    case ResNet50:
                        model = new ResNet50(MainActivity.this);
                        break;
                }
                model.setOnDownloadCompleteListener(new Model.OnDownloadCompleteListener() {
                    @Override
                    public void onDownloadComplete(Model model) {

                    }

                    @Override
                    public void runOnUiThread(Model model) {
                        loadModel(model);
                    }

                    @Override
                    public void onDownCancelled(Model model) {

                    }
                });
                model.downloadAsync();
            }
        });
        setupDialog.setSetupFinishedListener(new SetupDialog.SetupFinishedListener() {
            @Override
            public void onSetupFinished() {
                setupDialog.dismiss();
                if(model==null || model.getType() != Globals.modelType) {
                    switch(Globals.modelType){
                        case VGG16:
                            model = new VGG16(MainActivity.this);
                            break;

                        case VGG19:
                            model = new VGG19(MainActivity.this);
                            break;

                        case InceptionV3:
                            model = new InceptionV3(MainActivity.this);
                            break;

                        case ResNet50:
                            model = new ResNet50(MainActivity.this);
                            break;
                    }
                    if(!model.exists()) {
                        model.setOnDownloadCompleteListener(new Model.OnDownloadCompleteListener() {
                            @Override
                            public void onDownloadComplete(Model model) {

                            }

                            @Override
                            public void runOnUiThread(Model model) {
                                loadModel(model);
                            }

                            @Override
                            public void onDownCancelled(Model model) {

                            }
                        });
                        model.downloadAsync();
                    }
                    else {
                        loadModel(model);
                    }
                }
                setupFinished = true;
            }
        });

        optionsMenu = findViewById(R.id.optionsMenu);
        optionsMenu.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.app_bar_settings) {
                    setupDialog.show();
                    return true;
                }
                else if(item.getItemId() == R.id.app_bar_gallery) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setDataAndType( MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    intent.putExtra("return-data", true);
                    String[] mimeTypes = {"image/png", "image/jpeg", "image/webp"};
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                    startActivityForResult(intent, FETCH_IMAGE_CODE);
                }
                else {
                    Toast.makeText(MainActivity.this, "This is a placeholder", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }

    private void loadModel(Model model) {
        final ProgressDialog pd = new ProgressDialog(MainActivity.this);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage("Loading " + model.getType().toString());
        pd.show();
        model.loadModelAsync(py, new Model.OnModelLoadedListener() {
            @Override
            public void onLoaded(Model model) {
                pd.dismiss();
            }
        });
    }

    Model getModel() {
        return model;
    }

    @Override
    protected void onStart() {
        super.onStart();
        cameraView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.onResume();
        if(!setupFinished) {
            setupDialog.show();
            Window window = setupDialog.getWindow();
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        cameraView.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            cameraView.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        if (!(grantResults[0] == PackageManager.PERMISSION_GRANTED)){
            Toast.makeText(MainActivity.this, "Permission denied, exiting", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == FETCH_IMAGE_CODE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            Toast.makeText(MainActivity.this, imageUri.toString(), Toast.LENGTH_SHORT).show();
            try {
                InputStream istream =   getContentResolver().openInputStream(imageUri);
                byte[] bytes = new byte[istream.available()];
                istream.read(bytes, 0, bytes.length);
                new DreamTaskRaw(MainActivity.this).execute(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bottom_menu, menu);
        return true;
    }

    public static class DreamTaskRaw extends AsyncTask<byte[], Void, Void> {
        ProgressDialog progressDialog;
        WeakReference<Activity> activity;

        public DreamTaskRaw(Activity activity) {
            this.activity = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(Globals.step_size <= 0 || Globals.steps <= 0) {
                Toast.makeText(activity.get(), "Enter valid values for Steps and Step Size", Toast.LENGTH_LONG).show();
                cancel(true);
                return;
            }
            progressDialog = new ProgressDialog(activity.get());
            progressDialog.setMessage("Processing...");
        }

        @Override
        protected Void doInBackground(byte[]... byteArrays) {
            byte[] outputArray = ((MainActivity)activity.get()).model.dream(byteArrays[0], Globals.steps, Globals.step_size);

            activity.get().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.show();
                }
            });
            Bitmap output = BitmapFactory.decodeByteArray(outputArray, 0, outputArray.length);

            try (FileOutputStream out = new FileOutputStream(new File(activity.get().getDataDir(), "tmp.png"))) {
                output.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void avoid) {
            Intent i = new Intent(activity.get(), ImageActivity.class);
            progressDialog.dismiss();
            activity.get().startActivity(i);
        }
    }
}
