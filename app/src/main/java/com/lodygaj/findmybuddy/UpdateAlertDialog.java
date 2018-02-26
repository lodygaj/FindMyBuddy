package com.lodygaj.findmybuddy;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

/**
 * Created by Joey Laptop on 6/29/2017.
 */
public class UpdateAlertDialog extends Activity implements
        android.view.View.OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setFinishOnTouchOutside(false);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.alert_update_location);

        // Get buttons from layout
        final Button btnYes = (Button) findViewById(R.id.btnYes);
        final Button btnNo = (Button) findViewById(R.id.btnNo);

        // Set click listeners
        btnYes.setOnClickListener(this);
        btnNo.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnYes:
                updateLocation();
                break;
            case R.id.btnNo:

                break;
            default:
                break;
        }

    }

    public void updateLocation() {

    }
}