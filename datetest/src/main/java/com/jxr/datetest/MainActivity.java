package com.jxr.datetest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    TextView dateView;
    EditText editText;
    Calendar mTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dateView = (TextView) findViewById(R.id.date);
        editText = (EditText) findViewById(R.id.edit_text);


        mTime = Calendar.getInstance();


        Locale locale = Locale.getDefault();
        String mFormat = DateFormat.getBestDateTimePattern(locale, "MMMd") +"  "+ DateFormat.getBestDateTimePattern(locale, "EEEE");
        //dateView.setFormat24Hour(date);

        dateView.setText(DateFormat.format(mFormat, mTime));
        editText.setText(DateFormat.format(mFormat, mTime));

        dateView.setText(DateFormat.format(DateFormat.getBestDateTimePattern(locale, "EEE"), Calendar.getInstance()));
    }
}
