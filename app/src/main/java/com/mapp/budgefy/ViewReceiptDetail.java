package com.mapp.budgefy;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mapp.budgefy.model.Receipt;
import com.squareup.picasso.Picasso;


public class ViewReceiptDetail extends AppCompatActivity {

    //Declare the required variables to work with
    public static final String RECEIPT_ITEM = "receipt_item";
    Receipt receipt;
    Uri image_uri;
    Context context;

    //firebase related stuff
    private static DatabaseReference databaseFixedReference;
    private static GoogleSignInAccount mSignInAccount;
    private static StorageReference storageFixedReference;

    AlertDialog.Builder deleteDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_receipt);

        //Retrieve the Receipt object you passed in the intent object
        receipt = getIntent().getExtras().getParcelable(Fragment1Home.RECEIPT_ITEM);

        Log.d("TEST VALUES", receipt.toString());

        //Clear the mStringItems list first before adding stuff to it
        ViewReceiptDetailAdapter.mStringItems.clear();

        //Get the receipt's member variables and store in a list of strings...
        ViewReceiptDetailAdapter.mStringItems.add(receipt.getrRecordName());
        ViewReceiptDetailAdapter.mStringItems.add(receipt.getMerchantName());
        ViewReceiptDetailAdapter.mStringItems.add(receipt.getLocation());
        ViewReceiptDetailAdapter.mStringItems.add(receipt.getCategory());
        ViewReceiptDetailAdapter.mStringItems.add(Double.toString(receipt.getTotalAmt()));
        ViewReceiptDetailAdapter.mStringItems.add(receipt.getDate());

        //HANDY SHORTCUT TO WRITE THIS: type inn + tab key
        if (receipt != null) {
            //Get a reference of the listView
            ListView listView = (ListView) findViewById(R.id.lvReceiptDetails);

            //Get reference of the ImageView
            final ImageView iv_ReceiptImage = (ImageView)findViewById(R.id.dispImage);
//
//            if (receipt.getImageURL() != "" || !receipt.getImageURL().isEmpty()) {
//                Picasso.with(this)
//                        .load(receipt.getImageURL())
//                        .error(R.drawable.no_internet)
//                        .placeholder( R.drawable.progress_animation )//http://stackoverflow.com/questions/24826459/animated-loading-image-in-picasso
//                        .into(iv_ReceiptImage);
//            //}else if(receipt.getImageURL().isEmpty()){
//            }else {
//                iv_ReceiptImage.setImageResource(R.drawable.receipt_placeholder);
//            }

            if(receipt.getImageUrl().isEmpty()) {
                iv_ReceiptImage.setImageResource(R.drawable.receipt_placeholder);
            } else {
                Picasso.with(this)
                        .load(receipt.getImageUrl())
                        .error(R.drawable.no_internet)
                        .placeholder( R.drawable.progress_animation )//http://stackoverflow.com/questions/24826459/animated-loading-image-in-picasso
                        .into(iv_ReceiptImage);
            }

            //Create an instance of the custom adapter you have defined
            ViewReceiptDetailAdapter adapter =
                    new ViewReceiptDetailAdapter(ViewReceiptDetail.this,
                            ViewReceiptDetailAdapter.mStringItems);

            //set the listView adapter
            listView.setAdapter(adapter);
            Log.d("TAGViewReceiptDetail", "Set ListView's adapter liao!");

        }

        //------------------------------delete dialog--------------------------------
        deleteDialog = new AlertDialog.Builder(ViewReceiptDetail.this);
        deleteDialog.setTitle("Delete Record?");
        deleteDialog.setMessage("This action is permanent");
        deleteDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO: KL - DELETE RECORD FROM FIREBASE
                //-----------------------firebase stuff setup-----------------------------
                //To link and reference to FireBase.
                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                databaseFixedReference = database.getReference();

                //get the user google account
                mSignInAccount = SignInActivity.publicSignInAccount;

                //------------------delete record in firebase realtime----------------
                databaseFixedReference.child(mSignInAccount.getId())
                        .child(receipt.getReceiptId())
                        .removeValue();

                Toast.makeText(getApplicationContext(), "Delete Successful",
                        Toast.LENGTH_SHORT).show();
                //-------------------------------------------------------------------------

                //delete the tied image file from firebase storage
                if(!receipt.getImageFileName().isEmpty()){
                    //TODO: RARA - DELETE IMAGE FROM FIREBASE

                    // Create a storage reference
                    storageFixedReference = FirebaseStorage.getInstance().getReference();

                    // delete the image file in firebase storage
                    storageFixedReference.child(mSignInAccount.getId())
                            .child(receipt.getImageFileName())
                            .delete();
                }

                //---------------------------------------------------------------------------
                dialog.dismiss();
                //Once everything is done,
                //Close this activity because we ain't coming back here anymore
                finish();
            }
        });
        deleteDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        //-------------------------------------------------------------------------
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_receipt, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.action_edit:
                //---------------------------------------------------------------------------
                //Hokay. Time to whip up the UpdateReceiptDetail view

                //Create an Explicit intent
                Intent intent = new Intent(ViewReceiptDetail.this, UpdateReceiptDetail.class);

                //Place the Receipt object as an Extra in the Intent so u can retrieve in EditReceipt view
                intent.putExtra(RECEIPT_ITEM, receipt);
                startActivity(intent);

                Toast.makeText(getApplicationContext(), "Edit",
                        Toast.LENGTH_SHORT).show();


                //Close this activity because we ain't coming back here anymore
                finish();

                return true;

            case R.id.action_delete:
                //all the delete codes are in this deleteDialog
                deleteDialog.show();

                return true;

        }


        return super.onOptionsItemSelected(item);
    }
}
