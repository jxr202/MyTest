package com.jxr.mytest;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void start(View view) {
        //Intent intent = new Intent("android.bluetooth.devicepicker.action.LAUNCH");
        //Intent intent = new Intent(this, Activity_02.class);

        Intent intent = getShareResult(new Intent());

        startActivity(intent);
    }

    private static Intent getShareResult(Intent intent) {
        final String mimeType = "image/jpg";//getMimeType(type);
        intent.setAction(Intent.ACTION_SEND).setType(mimeType);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(Environment.getExternalStorageDirectory() +  "/abc.jpg")));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra("leShareAppTag", "gallery");
        return intent;
    }
}
