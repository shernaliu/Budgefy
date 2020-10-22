package com.mapp.budgefy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;
import com.mapp.budgefy.model.Receipt;
import com.mapp.budgefy.receipt.ReceiptDataProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by shern on 23/11/2016.
 */
public class CreateRecord extends AppCompatActivity {

    private static final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpg");
    private static GoogleSignInAccount mSignInAccount;

    private static final int RESPONSE_OK_GOT_TEXT = 111;
    private static final int RESPONSE_OK_NO_TEXT = 222;
    private static final int RESPONSE_NOT_OK = 333;

    int is_ocr_successful = 0;

    //Declare the variables required in this class
    ImageView iv_receiptImage;
    int requested_from;
    Uri image_uri;
    RequestBody requestBody;
    Request request;
    Response response;
    String jsonString = "";
    JSONObject jsonObj = null;
    String parsedText = "";
    String extracted_date = "";
    String extracted_merchantName = "";
    String extracted_price = "";

    ProgressDialog progressDialog;

    Receipt newRecord;

    Button btn_create_record;
    Spinner categorySpinner;
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
    private StorageReference mStorageRef;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_record);

        //---------------------------------------------------------------------------
        //Setup the Spinner (drop down menu)
        //Get a reference to the spinner
        categorySpinner = (Spinner) findViewById(R.id.spinnerCategory);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(
                        CreateRecord.this,
                        R.array.category_choices,
                        android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        categorySpinner.setAdapter(adapter);

        //---------------------------------------------------------------------------
        //Implement setOnItemSelectedListener to retrieve the selected category
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            //when an item (category) is selected, store it in the category variable
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category = parent.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //---------------------------------------------------------------------------
        //ImageView - Retrieving image from GALLERY / CAMERA to display in CreateRecord
        //Get a reference of the ImageView
        iv_receiptImage = (ImageView) findViewById(R.id.dispImage);

        /**
         * Added by Sherna
         *
         * Because Sherna & Rachel's way of setting image to the imageView is slightly different,
         *
         * What I did was I put the IMAGE_CAPTURE_REQUEST and IMAGE_GALLERY_REQUEST in their
         respective methods under the key named "REQUESTED_FROM".
         So when we come into the CreateRecord.java, it will retrieve the the int value under "REQUESTED_FROM"
         and determine which set of Imageview code to run.
         */

        //Retrieve the intent of this to retrieve the extras (for gallery)
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        requested_from = intent.getIntExtra("REQUESTED_FROM", -1);
        Log.d("ANSWER", Integer.toString(requested_from));

        //Setting image in ImageView
        //Rachel's way of setting image from Gallery
        if (requested_from == MainActivity.IMAGE_GALLERY_REQUEST) {

            String uriString = extras.getString("real_uri_image_file");
            image_uri = Uri.parse(uriString);

            Log.d("image_url", image_uri.toString());

            if (extras != null) {
                byte[] byteArray = extras.getByteArray("picture");
                Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

                iv_receiptImage.setImageBitmap(bmp);
            }

            //Sherna's way of setting image by URI
        } else if (requested_from == MainActivity.IMAGE_CAPTURE_REQUEST) {
            //Get the uri string and parse it into a Uri object
            image_uri = Uri.parse(extras.getString("uri_image_file"));
            iv_receiptImage.setImageURI(image_uri);
            //debugging
            Log.d("image_url", image_uri.toString());
        }

        //---------------------------------------------------------------------------
        // Added by Sherna - right now, we will use OkHttpClient to send a HTTP POST to the
        // FREE OCR API to do processing (if we come from CAMERA / GALLERY, NOT MANUAL)

        if (requested_from == MainActivity.IMAGE_GALLERY_REQUEST | requested_from == MainActivity.IMAGE_CAPTURE_REQUEST) {

            Log.d("ATTENTION", "GOING INTO API NOW!");

            //Create a Thread that is separate from the Main UI thread to run networking stuff
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    //Create a File object from the uri
                    File imageFile = new File(image_uri.getPath());

                    //---------debugging - start ------------------
                    String filepath1 = image_uri.getPath();
                    long size1 = filepath1.length();

                    Log.d("FilePath1", filepath1);
                    Log.d("FileSize2", Long.toString(size1));
                    //---------debugging - end -----------------

                    /**
                     * Because the FREE OCR API has a file size limit of 1MB when you upload to their API,
                     * so images must be less than 1MB.
                     *
                     * Also, a smaller file size = shorter waiting time for upload.
                     *
                     * To achieve this, users must use the lowest Megapixel setting
                     * in the Camera settings, preferably 2MP.
                     *
                     * Using 2MP to capture the image is sufficient for FREE OCR API because their
                     * OCR engine is actually very good and powerful.
                     *
                     * To avoid the possibility of overshooting 1MB file size, I will
                     * Compress the file using just 1 line of code,
                     * thanks to a powerful library called SiliCompressor
                     */
                    String filepath2 = SiliCompressor.with(CreateRecord.this).compress(filepath1);

                    //retrieve the size of the newly compressed file for debugging purposes
                    long size2 = filepath2.length();

                    Log.d("FilePath2", filepath2);
                    Log.d("FileSize2", Long.toString(size2));

                    //Create a new File from the compressed filePath
                    File file2 = new File(filepath2);

                    //------- Time to send the image File to FREE OCR API using OkHttpClient!-------
                    //Call the sendToOcrApi(), passing in the image File as the parameter & store in
                    //Response object, receivedResponse
                    Response receivedResponse = sendToOcrApi(file2);

                    Log.d("JsonString1", jsonString);

                    //time to evaluate the response. if response = 200 OK, things are good (for now).
                    if (receivedResponse.code() == 200) {

                        //Now, we try to extract out the parsedText OCR has given
                        try {
                            //Retrieve the JSON string
                            jsonString = receivedResponse.body().string();

                            //parse it into a JSON object
                            jsonObj = new JSONObject(jsonString);

                            //Read the JSON object to extract out the "ParsedText" field
                            JSONArray json_PR_Array = jsonObj.getJSONArray("ParsedResults");
                            Log.d("jsonArray", json_PR_Array.toString());

                            JSONObject jsOb = json_PR_Array.getJSONObject(0);
                            Log.d("jsOb", jsOb.toString());

                            parsedText = jsOb.get("ParsedText").toString();
                            Log.d("PARSEDTEXT", parsedText);


                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("JSONFAILED", "Failed to parse JSON Object");
                        }

                        //parsedText do contains the OCR-ed texted
                        if (parsedText.length() != 0) {

                            //Call the extract methods from the class RegexExtractor
                            extracted_date = RegexExtractor.extractDate(parsedText);
                            extracted_merchantName = RegexExtractor.extractMerchantName(parsedText);
                            extracted_price = RegexExtractor.extractPrice(parsedText);

                            Log.d("varDate", extracted_date);
                            Log.d("varMN", extracted_merchantName);
                            Log.d("varPrice", extracted_price);

                            // This is the ideal scenario. Exit with RESPONSE_OK_GOT_TEXT
                            is_ocr_successful = RESPONSE_OK_GOT_TEXT;

                            Log.d("is_ocr_successful", Integer.toString(is_ocr_successful));
                        } else {

                            /**
                             * Sometimes the image uploaded cannot be OCR-ed (i.e. non-text image)
                             * So, the API will return 200 OK, but parsedText is empty
                             *
                             * In this case, exit with RESPONSE_OK_NO_TEXT
                             */
                            is_ocr_successful = RESPONSE_OK_NO_TEXT;
                        }

                    } else {
                        /**
                         * This is the worst case scenario. Response not OK
                         * This can happen when the API is down and return us
                         * 500 Internal Server Error
                         *
                         * Since we can't do anything about it, just display Toast message
                         * to tell user API is down and use manual input or try again later
                         */

                        is_ocr_successful = RESPONSE_NOT_OK;
                    }

                }
            });//end of Thread

            //start the thread
            t.start();

            //Wait for the thread to complete its task, then run this code to set the EditText's fields
            //because if you want to touch the widgets, only the Main UI Thread can do it.
            try {
                t.join();

                Log.d("isDone2", Integer.toString(is_ocr_successful));

                if (is_ocr_successful == RESPONSE_OK_GOT_TEXT) {

                    Log.d("setText", "Ideal scenario, now set the EditTexts");

                    //Get a reference to the EditTexts
                    EditText et_MerchantName = (EditText) findViewById(R.id.etMerchantName);
                    EditText et_totalAmt = (EditText) findViewById(R.id.etTotalAmt);
                    EditText et_Date = (EditText) findViewById(R.id.etDate);

                    //Set the EditTexts
                    et_MerchantName.setText(extracted_merchantName);
                    et_totalAmt.setText(extracted_price);
                    et_Date.setText(extracted_date);

                } else if (is_ocr_successful == RESPONSE_OK_NO_TEXT) {
                    //Just display a Toast message since there's nothing we can do about it.
                    Toast.makeText(CreateRecord.this, "No text OCR-ed",
                            Toast.LENGTH_LONG).show();
                } else {
                    //RESPONSE_NOT_OK
                    Toast.makeText(CreateRecord.this, "OCR API is down. Try again later or use manual input.",
                            Toast.LENGTH_LONG).show();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }//end of if


        //---------------------------------------------------------------------------
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Image");

        //---------------------------------------------------------------------------
        //Setup the Create Record Button
        //Get a reference to btnCreateRecord
        btn_create_record = (Button) findViewById(R.id.btnCreateRecord);

        //Implement the button setOnClickListener to define what this button does (save record)
        btn_create_record.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {

            //Retrieve the uploaded image
                final ImageView iv_ReceiptImage = (ImageView)findViewById(R.id.dispImage);
                iv_ReceiptImage.setImageURI(image_uri);

                     //Retrieve the recordName
                     EditText et_RecordName = (EditText) findViewById(R.id.etRecordName);
                     recordName = et_RecordName.getText().toString();

                     //Retrieve the merchantName
                     EditText et_MerchantName = (EditText) findViewById(R.id.etMerchantName);
                     merchantName = et_MerchantName.getText().toString();

                     //Retrieve the location
                     EditText et_location = (EditText) findViewById(R.id.etLocation);
                     location = et_location.getText().toString();

                     //Retrieve the totalAmtStr
                     EditText et_totalAmt = (EditText) findViewById(R.id.etTotalAmt);
                     totalAmtStr = et_totalAmt.getText().toString();

                     //Parse the limit amt to double
                     if (totalAmtStr.length() > 0) {
                         try {
                             totalAmt = Double.parseDouble(totalAmtStr);
                         } catch (Exception e) {
                             e.printStackTrace();
                         }
                     }

                     //Retrieve the date
                     EditText et_date = (EditText)findViewById(R.id.etDate);
                     date = et_date.getText().toString();

                    imageURL = "";
                 imageFileName = "";

                     //-------Show me ur stuff------
                     Log.d("TAG", recordName);
                     Log.d("TAG", merchantName);
                     Log.d("TAG", location);
                     Log.d("TAG", totalAmtStr);
                     Log.d("TAG", category);
                     Log.d("TAG", date);

                     //---------------------------------------------------------------------------
                     //Determine which image it should display depending on the category type
                     categoryIcon = getImageString(category);
                     Log.d("TAGImageName", categoryIcon);

                     //---------------------------------------------------------------------------
                     //Create a Receipt object
                     //receiptID will be the Primary Key in your Firebase.

                     //--------------------------------------------stuff kl added start----------------------------------------------
                     //To link and reference to FireBase.
                     FirebaseDatabase database = FirebaseDatabase.getInstance();
                     final DatabaseReference databaseReference = database.getReference();

                     //get the user google account
                     mSignInAccount = SignInActivity.publicSignInAccount;

                     //.push() create a unique id
                     final String newRecordId = databaseReference.push().getKey();

                     //create a new receipt object
                     newRecord = new Receipt(newRecordId, recordName, merchantName,
                             location, category, totalAmt, categoryIcon, date, imageFileName, imageURL);


                     //----------------------------------------------stuff kl added end--------------------------------------------

                     // TODO: Add Image to Firebase Storage
                     //------------------------------------------ rara edit start -------------------------------------------------

                     if (requested_from == MainActivity.IMAGE_GALLERY_REQUEST | requested_from == MainActivity.IMAGE_CAPTURE_REQUEST) {
                         progressDialog.show();


                         mStorageRef = FirebaseStorage.getInstance().getReference();

                         final Bundle extras = getIntent().getExtras();

                         //sherna declared a var image_uri at the top so you dont need to do this everytime
//                                                     Uri stringUri = Uri.parse(extras.getString("uri"));

                         imageFileName = newRecordId + "_" + image_uri.getLastPathSegment();

                         StorageReference filepath = mStorageRef
                                 .child(mSignInAccount.getId())
                                 //.child(newRecordId)//Photos uploaded belongs to a user account
                                 .child(imageFileName); //Individual photos listed by their uri address so that duplicates will not be uploaded

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
                                 Toast.makeText(CreateRecord.this, "Upload Done.", Toast.LENGTH_LONG).show();
                                 Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                 newRecord.setImageFileName(imageFileName);
                                 newRecord.setImageUrl(downloadUrl.toString());

                                 //pass the data into firebase
                                 databaseReference //the database in ur firebase
                                         .child(mSignInAccount.getId()) //which user is it
                                         .child(newRecordId)
                                         .setValue(newRecord);

                                 progressDialog.dismiss();

                                //finish everything liao, close the activity
                                finish();
                             }
                         });

                         //------------------------------------------ rara edit end  -------------------------------------------------
                     }

//                         //manual record w/o image
                         //pass the data into firebase
                         databaseReference //the database in ur firebase
                                 .child(mSignInAccount.getId()) //which user is it
                                 .child(newRecordId)
                                 .setValue(newRecord); //all the data in the receipt

                 if(image_uri == null){
                     finish();
                 }
                     //--------------------------------------------------------------------------

                 }
             }

        );


    }//end of onCreate() method

    /**
     * Function to determine which image(string) name it should return
     * depending on the category the user selected
     *
     * @param inputString the category input
     * @return mCategory
     * the name of the image file (eg. bill.png / food.png / transport.png / etc...)
     */
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

    /**
     * This method is called when you are ready to send the compressed image File to the OCR API using
     * OKHttpClient
     *
     * @param imageFile the File object of the image
     * @return a Response object
     */
    public Response sendToOcrApi(File imageFile) {

        try {
            //Create an instance of OkHttpClient
            OkHttpClient client = new OkHttpClient();

            //Create an instance of the RequestBody to put in apikey, language, isOverlayRequired
            //and the file object
            requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("apikey", "72e5c2ff5288957")
                    .addFormDataPart("language", "eng")
                    .addFormDataPart("isOverlayRequired", "false")
                    .addFormDataPart("file", "receipt.jpg",
                            RequestBody.create(MEDIA_TYPE_JPG, imageFile))
                    .build();

            //Create an instance of the Request object with the destination URL and post and build it.
            request = new Request.Builder()
                    .url("https://api.ocr.space/parse/image")
                    .post(requestBody)
                    .build();

            //Create an instance of the Response object
            //execute the client's request and store the result in response
            response = client.newCall(request).execute();

            //if response is unsuccessful
            if (!response.isSuccessful()) {
                Log.d("ERROR", "Unsuccessful!");
                Log.d("ERROR", response.toString());
                //return the response object anyway
                return response;
            }

            //else, it is successful, then return the response object
            Log.d("SUCCESS1", Integer.toString(response.code()));
//            Log.d("SUCCESS2", response.body().string());

        } catch (IOException e) {
            Log.d("ERROR", "Exception Error!");
        }

        //return the response object
        return response;
    }
}
