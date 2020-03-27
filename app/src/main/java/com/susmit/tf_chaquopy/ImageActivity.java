package com.susmit.tf_chaquopy;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.io.OutputStream;

public class ImageActivity extends Activity {

    ImageView imageView;
    FloatingActionButton fab;

    Bitmap image;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        imageView = findViewById(R.id.imageView);
        fab = findViewById(R.id.save);
        
        image = BitmapFactory.decodeFile(getDataDir() + "/tmp.png");

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentResolver resolver = getContentResolver();
                ContentValues imageValues = new ContentValues();
                imageValues.put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis());
                imageValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    imageValues.put(MediaStore.MediaColumns.RELATIVE_PATH,  Environment.DIRECTORY_PICTURES);
                    imageValues.put(MediaStore.MediaColumns.IS_PENDING, 1);
                }
                Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageValues);
                try {
                    OutputStream imageStream = resolver.openOutputStream(uri);
                    image.compress(Bitmap.CompressFormat.PNG, 100, imageStream);
                    imageStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    resolver.delete(uri, null, null);
                }
                finally {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        imageValues.put(MediaStore.MediaColumns.IS_PENDING, 0);
                    Toast.makeText(ImageActivity.this, "Saved to gallery", Toast.LENGTH_SHORT).show();
                }
            }
        });
        imageView.setImageBitmap(image);
    }
}
