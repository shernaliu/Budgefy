package com.mapp.budgefy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by shern on 13/11/2016.
 */

public class Fragment3About extends Fragment {
    Button btn_send_email;
    public static String email = "budgefy@gmail.com";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_3_about, container, false);

        //Get a reference to btnSendEmail
        btn_send_email = (Button)rootView.findViewById(R.id.btnSendEmail);

        //Implement the button setOnClickListener to define what this button does (send email)
        btn_send_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Send an Email to Budgefy
                String[] addresses = {email};

                //Create an implicit Intent to call another activity in another app, eg. Gmail
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:"));

                //The following defines the email address, subject and text so when it is launched,
                //These strings are already inserted into the email
                emailIntent.putExtra(Intent.EXTRA_EMAIL, addresses);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Budgefy App Feedback");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Dear Budgefy Team");

                //If there is an app available on the phone to send email (aka != null), then startActivity
                if(emailIntent.resolveActivity(getActivity().getPackageManager()) != null){
                    startActivity(emailIntent);
                }
            }
        });

        return rootView;
    }
}
