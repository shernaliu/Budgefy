package com.mapp.budgefy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

/**
 * Created by shern on 13/11/2016.
 */
public class Splashscreen extends AppCompatActivity {

    private static int SPLASH_TIMEOUT = 2000; //2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //Hide the Action Bar before the setContentView
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();

        setContentView(R.layout.splashscreen);

        //Create a new Handler
        new Handler().postDelayed(new Runnable(){

            @Override
            public void run() {

                Intent mainIntent = new Intent(Splashscreen.this, SignInActivity.class);
                startActivity(mainIntent);
                finish();
            }

        },SPLASH_TIMEOUT);


    }
}
