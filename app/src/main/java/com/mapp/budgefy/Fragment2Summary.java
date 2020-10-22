package com.mapp.budgefy;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapp.budgefy.model.Receipt;
import com.mapp.budgefy.receipt.ReceiptDataProvider;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by shern on 13/11/2016.
 */

public class Fragment2Summary extends Fragment {

    //Declare the required variables in this class
    List<Receipt> mReceiptList;

    Receipt receipt;

    double limitAmt = 1000;
    double sum = 0.0;
    double average = 0.0;
    double highest = 0.0;
    double lowest = 0.0;

    //Declare the TextViews
    TextView tv_limit_amount, tv_total, tv_average, tv_highest, tv_lowest;

    //Declare the ImageButton
    ImageButton ib_edit;

    //a constant name for shared preferences
    private static final String MY_GLOBAL_PREFS = "my_global_prefs";
    private static final String LIMIT_AMT_KEY = "Limit_Amount";

    //firebase related stuff
    private static DatabaseReference databaseFixedReference;
    private static GoogleSignInAccount mSignInAccount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_2_summary, container, false);
        //---------------------------------------------------------------------------
        //Instantiate a SharedPreferences object here to retrieve the saved limit amount
        SharedPreferences prefs = getActivity().getSharedPreferences(MY_GLOBAL_PREFS, MODE_PRIVATE);

        //Retrieve value using .get() method
        //param 1: the name of the preference set where you want to retrieve the data
        //param 2: the default value to retrieve in case the key is not found
        //parse it to a Double to store it in limitAmt
        String sp_limit_amount = prefs.getString(LIMIT_AMT_KEY, "");

        //Use TextUtil which is part of Android SDK to check if its empty (NULL or 0 characters)
        //passing in sp_limit_amount
        if(!TextUtils.isEmpty(sp_limit_amount)){
            //if its not empty, go ahead and parse it to double and store in limitAmt
            limitAmt = Double.parseDouble(sp_limit_amount);
        }

        //---------------------------------------------------------------------------
        //Get the references to the 5 TextViews + 1 ImageButton
        tv_limit_amount = (TextView) rootView.findViewById(R.id.tvLimitAmount);
        tv_total = (TextView) rootView.findViewById(R.id.tvTotal);
        tv_average = (TextView) rootView.findViewById(R.id.tvAverage);
        tv_highest = (TextView) rootView.findViewById(R.id.tvHighest);
        tv_lowest = (TextView) rootView.findViewById(R.id.tvLowest);
        ib_edit = (ImageButton) rootView.findViewById(R.id.ibEdit);

        //set the default limit amount
        tv_limit_amount.setText(Double.toString(limitAmt));

        //---------------------------------------------------------------------------
        /** Added by Sherna
         * Because the way fragments work, I cannot just get a reference to the receiptList in
         * the first fragment to compute sum, average, highest, lowest.
         *
         * I need to fetch the Receipt records into mReceiptList from Firebase in order to do so.
         */

        //To link and reference to FireBase.
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseFixedReference = database.getReference();

        //get the user google account
        mSignInAccount = SignInActivity.publicSignInAccount;

        //create new list of receipt objects
        mReceiptList = new ArrayList<>();

        //value event listener for realtime data update
        databaseFixedReference.child(mSignInAccount.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //added by sherna - remember to clear if not will duplicate
                mReceiptList.clear();

                //reset sum and average since they are to be calculated everytime
                sum = 0;
                average = 0;

                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                    //getting data from snapshot
                    Receipt receipt = postSnapShot.getValue(Receipt.class);

                    //converting database receipt and add to the receipt arraylist
                    if (mReceiptList.isEmpty() || !mReceiptList.isEmpty()) {
                        mReceiptList.add(receipt);
                    }
                }
                if (mReceiptList.size() != 0) {
                    //set the lowest to the first receipt's totalAmt
                    lowest = mReceiptList.get(0).getTotalAmt();

                    //compute the sum after done populating mReceiptList
                    for (Receipt receipt : mReceiptList) {
                        sum += receipt.getTotalAmt();

                        //compare for highest
                        if (receipt.getTotalAmt() > highest) {
                            highest = receipt.getTotalAmt();
                        }

                        //compare for lowest
                        if (receipt.getTotalAmt() < lowest) {
                            lowest = receipt.getTotalAmt();
                        }
                    }

                    //compute the average
                    average = sum / mReceiptList.size();

                    Log.d("Sum", Double.toString(sum));
                    Log.d("Average", Double.toString(average));

                    //round to 2dp
                    DecimalFormat df = new DecimalFormat("##.##");

                    //set the limit, average, highest, lowest into their respective TextViews
                    tv_total.setText("$" + sum);
                    tv_average.setText("$" + df.format(average));
                    tv_highest.setText("$" + highest);
                    tv_lowest.setText("$" + lowest);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Unable to get data from database", Toast.LENGTH_LONG).show();
            }
        });

        //---------------------------------------------------------------------------
        //Set the (Edit Limit Amount button) Imagebutton's onClickListener to show a Dialog for editing
        ib_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Inflate the dialog_limit_amount layout to show EditText
                final View view = (LayoutInflater.from(getContext())).inflate(R.layout.dialog_limit_amount, null);

                //Build the Dialog
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());
                alertBuilder.setView(view);

                //Build the Ok button
                alertBuilder.setCancelable(true)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //get a reference to the EditText
                                final EditText edit_limit_amount = (EditText) view.findViewById(R.id.et_limitAmt);

                                //Get the EditText's number and store it in limitAmt
                                limitAmt = Double.parseDouble(edit_limit_amount.getText().toString());

                                //Set the limitAmt into the TextView
                                tv_limit_amount.setText(Double.toString(limitAmt));

                                //---------------------------------------------------------------------------
                                //Instantiate a SharedPreferences object here to save Limit Amount into local storage
                                //1st param: the name of the preference set where you want to save the data
                                //always use MODE_PRIVATE
                                SharedPreferences.Editor editor =
                                        getActivity().getSharedPreferences(MY_GLOBAL_PREFS, MODE_PRIVATE).edit();

                                editor.putString(LIMIT_AMT_KEY, Double.toString(limitAmt));

                                //Save the changes by calling the apply() method
                                editor.apply();
                            }
                        });

                //Create and show the Dialog
                Dialog dialog = alertBuilder.create();
                dialog.setTitle("Enter Limit Amount");
                dialog.show();
            }
        });


        return rootView;
    }
}
