package com.jxr.leui_clear_anim;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

    RelativeLayout mRecentContent;
    LePinWheelWidget mClearAllBtn;
    //EuiClearAllTextView mClearAllTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mClearAllBtn = (LePinWheelWidget)findViewById(R.id.eui_recent_clear_all_widget);
        //mClearAllTextView = (EuiClearAllTextView)findViewById(R.id.eui_recent_clear_all_txtview);
        if (mClearAllBtn != null /*&& mClearAllTextView != null*/){
            mClearAllBtn.setOnClickListener(new ClearAllBtnListener());
            mClearAllBtn.init(/*mClearAllTextView*/);
            //mClearAllTextView.updateView();
        }
    }

    class ClearAllBtnListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.eui_recent_clear_all_widget:
                    mClearAllBtn.start();
                    break;
                default:
                    break;
            }
        }
    }
}
