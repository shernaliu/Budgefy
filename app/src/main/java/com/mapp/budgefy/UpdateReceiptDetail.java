package com.mapp.budgefy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mapp.budgefy.model.Receipt;
import com.mapp.budgefy.receipt.ReceiptDataProvider;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by shern on 25/12/2016.
 */

public class UpdateReceiptDetail extends AppCompatActivity {

    //Declare request code for image gallery
    public static final int IMAGE_GALLERY_REQUEST = 20;

    //Declare the required variables to work with
    public static final String RECEIPT_ITEM = "receipt_item";
    Receipt receipt;

    ImageView iv_receiptImage;

    EditText et_RecordName;
    EditText et_MerchantName;
    EditText et_location;
    EditText et_totalAmt;
    EditText et_date;

    Button btn_update_record;
    Button btn_update_image;

    ArrayAdapter<CharSequence> adapter;
    Spinner categorySpinner;

    ProgressDialog progressDialog;

    Receipt updatedReceipt;

    String recordName;
    String merchantName;
    String location;
    String totalAmtStr;
    double totalAmt;
    String category;
    String categoryIcon;
    String imageURL;
    String date;
    String imageFileName;
    Uri image_uri;

    //firebase related stuff
    private static DatabaseReference databaseFixedReference;
    private static GoogleSignInAccount mSignInAccount;
    private static StorageReference storageFixedReference;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_record);

        //---------------------------------------------------------------------------
        //Retrieve the Receipt object you passed in the intent object
        receipt = getIntent().getExtras().getParcelable(Fragment1Home.RECEIPT_ITEM);

        //Retrieve & set the recordName
        et_RecordName = (EditText) findViewById(R.id.etRecordName);
        et_RecordName.setText(receipt.getrRecordName());

        //Retrieve & set the merchantName
        et_MerchantName = (EditText) findViewById(R.id.etMerchantName);
        et_MerchantName.setText(receipt.getMerchantName());

        //Retrieve & set the location
        et_location = (EditText) findViewById(R.id.etLocation);
        et_location.setText(receipt.getLocation());

        //Retrieve & set the totalAmtStr
        et_totalAmt = (EditText) findViewById(R.id.etTotalAmt);
        et_totalAmt.setText(Double.toString(receipt.getTotalAmt()));

        //Retrieve & set the date
        et_date = (EditText) findViewById(R.id.etDate);
        et_date.setText(receipt.getDate());

        //---------------------------------------------------------------------------
        //Setup the Spinner (drop down menu)
        //Get a reference to the spinner
        categorySpinner = (Spinner) findViewById(R.id.spinnerCategory);

        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = ArrayAdapter.createFromResource(
                UpdateReceiptDetail.this,
                R.array.category_choices,
                android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        categorySpinner.setAdapter(adapter);
        //---------------------------------------------------------------------------

        //---------------------------------------------------------------------------
        //Catgory - Get a reference to the spinner (drop down menu)
        //Implement setOnItemSelectedListener
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            //when an item(category) is selected, it will be put inside the variable category
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category = parent.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //---------------------------------------------------------------------------
        //Retrieve the category of the Receipt to set it inside the categorySpinner
        categorySpinner.setSelection(adapter.getPosition(receipt.getCategory()));

        //---------------------------------------------------------------------------
        //TODO: RARA - CODE FOR RETRIEVE IMAGE FROM FIREBASE AND DISPLAY INSIDE THE IMAGEVIEW
        //Receipt ImageView - Get a reference to the ImageView
        iv_receiptImage = (ImageView) findViewById(R.id.ivReceiptImage);
        if(!receipt.getImageFileName().isEmpty()){
            Picasso.with(this)
                    .load(receipt.getImageUrl())
                    .error(R.drawable.no_internet)
                    .placeholder( R.drawable.progress_animation )//http://stackoverflow.com/questions/24826459/animated-loading-image-in-picasso
                    .into(iv_receiptImage);
        }


        //TODO: RARA - UPDATE IMAGE IN UPDATE RECEIPT DETAIL

        btn_update_image = (Button) findViewById(R.id.btn_update_image);

        btn_update_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //invoke image gallery using an implicit intent
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);

                //finding the data
                File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

                String pictureDirectoryPath = pictureDirectory.getPath();

                //get URI representation
                Uri data = Uri.parse(pictureDirectoryPath);

                //set data and type. Get ALL image types
                photoPickerIntent.setDataAndType(data, "image/*");

                //we will invoke this activity and get something back from it
                startActivityForResult(photoPickerIntent, IMAGE_GALLERY_REQUEST);
            }

        });


        //---------------------------------------------------------------------------

        //setup the progress dialog for when uploading img
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Image to OCR");

        //---------------------------------------------------------------------------

        //Get a reference to btnCreateRecord
        btn_update_record = (Button) findViewById(R.id.btnUpdateRecord);

        btn_update_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: KL - UPDATE RECORD TO FIREBASE
                //---------------------------------------------------------------------------
                //To link and reference to FireBase.
                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                databaseFixedReference = database.getReference();

                //get the user google account
                mSignInAccount = SignInActivity.publicSignInAccount;

                //get the variables
                //---------------------------------------------------------------------------
                recordName = et_RecordName.getText().toString();

                merchantName = et_MerchantName.getText().toString();

                location = et_location.getText().toString();

                totalAmtStr = et_totalAmt.getText().toString();
                if (totalAmtStr.length() > 0) {
                    try {
                        totalAmt = Double.parseDouble(totalAmtStr);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                categoryIcon = getImageString(category);

                date = et_date.getText().toString();

                imageFileName = receipt.getImageFileName();

                imageURL = receipt.getImageUrl();
                //---------------------------------------------------------------------------

                //create a new/updated Receipt object
                updatedReceipt = new Receipt(
                        receipt.getReceiptId(),
                        recordName,
                        merchantName,
                        location,
                        category,
                        totalAmt,
                        categoryIcon,
                        date,
                        imageFileName,
                        imageURL
                        );

                //---------------------------------------------------------------------------


                //check if null so that imageFileName can be empty
                if(image_uri!=null){
                    //TODO: RARA - UPDATE IMAGE TO FIREBASE IF THERE IS IMAGE
                    progressDialog.show();

                    storageFixedReference = FirebaseStorage.getInstance().getReference();

                    StorageReference filepath = storageFixedReference
                        .child(mSignInAccount.getId()) //Photos uploaded belongs to a user account
                        .child(updatedReceipt.getReceiptId() + "_" + image_uri.getLastPathSegment()); //Individual photos listed by their uri address so that duplicates will not be uploaded

                    UploadTask uploadTask = filepath.putFile(image_uri);

                    uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            progressDialog.setMessage(String.format( "%.2f", progress ) + "%");
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(UpdateReceiptDetail.this, "Upload Done.", Toast.LENGTH_LONG).show();
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();

                            updatedReceipt.setImageFileName(updatedReceipt.getReceiptId() + "_" + image_uri.getLastPathSegment());
                            updatedReceipt.setImageUrl(downloadUrl.toString());

                            databaseFixedReference //the database in ur firebase
                                    .child(mSignInAccount.getId()) //which user is it
                                    .child(receipt.getReceiptId())
                                    .setValue(updatedReceipt);

                            Log.d("UpdateReceiptDetail", downloadUrl.toString());

                            if(!imageFileName.isEmpty()){
                                //delete the old file in firebase storage
                                storageFixedReference.child(mSignInAccount.getId())
                                        .child(receipt.getImageFileName())
                                        .delete();
                            }

                            progressDialog.dismiss();

                            //Once everything is done,
                            //Close this activity because we ain't coming back here anymore
                            finish();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UpdateReceiptDetail.this, "Error uploading img to firebase.", Toast.LENGTH_LONG).show();
                        }
                    });

                }//end of if(image_uri!=null)

                databaseFixedReference //the database in ur firebase
                        .child(mSignInAccount.getId()) //which user is it
                        .child(receipt.getReceiptId())
                        .setValue(updatedReceipt); //all the data in the receipt

                if(image_uri == null){
                    finish();
                }

            }//end of onclick
        });

    }


    public String getImageString(String inputString) {
        //declare mCategory to hold the string to return at the end of this method
        String mCategory = "";

        //loop thru each item in categoryList to determine if the inputString is contained in categoryString
        for (String categoryString : ReceiptDataProvider.categoryList) {

            if (categoryString.contains(inputString)) {
                //if found, then set it to mCategory variable declared in this method
                mCategory = categoryString;
            }
        }

        return mCategory;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_GALLERY_REQUEST) {
            if (resultCode == RESULT_OK) {
                //address of the image on the SD card
                Uri imageUri = data.getData();
                String uriRealPath = new File(getRealPathFromURI(imageUri)).toString(); //all thanks to getRealPathFromURI
                Log.d("TAGE", uriRealPath); //FI FUCKING ANALLY
                //declare a stream to read the image data from the SD card
                image_uri = Uri.parse("file://" + uriRealPath);
                iv_receiptImage.setImageURI(image_uri);
            }
        }
    }

    //This method obtains the real uri obtained from selected image in gallery
    private String getRealPathFromURI(Uri contentURI) {
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }

}
