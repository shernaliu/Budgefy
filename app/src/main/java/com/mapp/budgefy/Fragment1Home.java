package com.mapp.budgefy;

/**
 * Created by shern on 13/11/2016.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mapp.budgefy.model.Receipt;
import com.mapp.budgefy.receipt.ReceiptDataProvider;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Fragment1Home extends Fragment {

    public static final String RECEIPT_ITEM = "receipt_item";

    ReceiptItemAdapter adapter = null;
    ListView listView;
    public static List<Receipt> receiptList;

    //firebase related stuff
    private static DatabaseReference databaseFixedReference;
    private static GoogleSignInAccount mSignInAccount;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get receipt object from firebase and add it into the listview
        //-------------------------------------kl start-----------------------------------------------
        //To link and reference to FireBase.
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseFixedReference = database.getReference();

        //get the user google account
        mSignInAccount = SignInActivity.publicSignInAccount;

        //create new list of receipt objects
        receiptList = new ArrayList<>();

        //value event listener for realtime data update
        databaseFixedReference.child(mSignInAccount.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //added by sherna - remember to clear if not will duplicate
                receiptList.clear();

                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                    //getting data from snapshot
                    Receipt receipt = postSnapShot.getValue(Receipt.class);

                    //converting database receipt and add to the receipt arraylist
                    receiptList.add(receipt);
                }
                //---------------------------------------------------------------------------
                //todo sorting the listview by? for now its according to records names
                //default sorting is top oldest , bot newest
                //Implement the sorting to sort the object by the itemName
                Collections.sort(receiptList, new Comparator<Receipt>() {
                    @Override
                    public int compare(Receipt o1, Receipt o2) {
                        return o1.getrRecordName().compareTo(o2.getrRecordName());
                    }
                });
                //make top newest , bot oldest
//                Collections.reverse(receiptList);

                //notify the adapter that smth changed so that it will refresh the listview/adapter
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Unable to get data from database", Toast.LENGTH_LONG).show();
            }
        });
        //-------------------------------------kl end-----------------------------------------------
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_1_home, container, false);

        //---------------------------------------------------------------------------

        //Create an instance of the custom adapter you have defined
        adapter = new ReceiptItemAdapter(getActivity(), receiptList);

        //ALWAYS REMEMBER TO GET A REFERENCE TO THE LISTVIEW OMG
        listView = (ListView) rootView.findViewById(R.id.lvReceiptRecords);
        listView.setAdapter(adapter);

        Log.d("TAG", "Set ListView's adapter liao!");

        //---------------------------------------------------------------------------
        //Define the ListView's onItemClickListener so that user can select an item in the listview
        //to open up the activity to display Receipt Details
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String value = listView.getItemAtPosition(position).toString();
                Toast.makeText(getContext(), "You selected " + value, Toast.LENGTH_LONG).show();

                Log.d("TAGSelected", "You selected: " + value);

                //---------------------------------------------------------------------------
                //Hokay. Time to whip up the ViewReceiptDetail page

                //Create an Explicit intent
                Intent intent = new Intent(getActivity(), ViewReceiptDetail.class);

                //Get a reference of the Receipt item selected
                Receipt selectedReceipt = (Receipt) listView.getItemAtPosition(position);

                //place the selected Receipt object as an Extra in the intent and start the activity
                intent.putExtra(RECEIPT_ITEM, selectedReceipt);
                startActivity(intent);

            }
        });
        //---------------------------------------------------------------------------
        return rootView;
    }

}
