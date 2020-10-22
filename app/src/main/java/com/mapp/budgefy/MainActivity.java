package com.mapp.budgefy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import static com.mapp.budgefy.SignInActivity.REQUEST_CODE;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener{

    //Declare request codes for CAMERA AND GALLERY
    public static final int IMAGE_GALLERY_REQUEST = 20;
    public static final int IMAGE_CAPTURE_REQUEST = 100;

    //Declare Uri object variable
    Uri uri_image_file;

    //Declare a ProgressDialog variable (for use when uploading image to FREE OCR API)
    ProgressDialog progress;


    //Declare variables for FloatingActionButton, Animation and a bool for fab related stuff
    FloatingActionButton fab_plus, fab_camera, fab_gallery, fab_manual;
    Animation FabOpen, FabClose,FabRClockwise,FabRAntiClockwise;
    boolean isOpen = false;

    ListView listView;
    private GoogleApiClient mGoogleApiClient;
    private static GoogleSignInAccount mSignInAccount;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    /**
     * onCreate() method is called when going from Splashscreen -> MainActivity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //---------------------------------------------------------------------------
        //Get a reference to the toolbar to "set view" for the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //---------------------------------------------------------------------------
        // Google Sign In implemented by KL, adapted from TaskManager project
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        //get the user google account
        mSignInAccount = SignInActivity.publicSignInAccount;

        //The black bar that shows at the bottom of the screen.
        //Similar to toast, but more aesthetically pleasing.
        Snackbar snackbar = Snackbar.make(toolbar, "Welcome " +
                mSignInAccount.getDisplayName(), Snackbar.LENGTH_LONG);
        snackbar.show();

        //---------------------------------------------------------------------------
        // Setting up the Tab layout and stuff
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //TabLayout is the "tool bar" that is responsible for showing which tab you are in
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        //---------------------------------------------------------------------------
        //Implement images in the TabLayout
        //Next time then settle this
        //Setting image to the tablayout's tab
//        final int[] ICONS = new int[]{
//                R.drawable.ic_action_document,
//                R.drawable.ic_action_tick,
//                R.drawable.ic_action_trash};
//
//
//        //Set the tab's label to an image instead
//        tabLayout.getTabAt(0).setIcon(ICONS[0]);
//        tabLayout.getTabAt(1).setIcon(ICONS[1]);
//        tabLayout.getTabAt(2).setIcon(ICONS[2]);

        //---------------------------------------------------------------------------
        //Get a reference to the 4 Floating Action Buttons
        fab_plus = (FloatingActionButton)findViewById(R.id.fab_plus);
        fab_camera = (FloatingActionButton)findViewById(R.id.fab_camera);
        fab_gallery = (FloatingActionButton)findViewById(R.id.fab_gallery);
        fab_manual = (FloatingActionButton)findViewById(R.id.fab_manual);

        //Get a reference to the animations
        FabOpen = (AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_open));
        FabClose = (AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close));
        FabRClockwise = (AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_clockwise));
        FabRAntiClockwise = (AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_anticlockwise));

        //the "+" button's onClickListener opens or closes the other 3 fab
        fab_plus.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                if(isOpen){
                    //animate the 2 fab to close
                    fab_gallery.startAnimation(FabClose);
                    fab_camera.startAnimation(FabClose);
                    fab_manual.startAnimation(FabClose);

                    //rotate the plus fab anti-clockwise
                    fab_plus.startAnimation(FabRAntiClockwise);

                    //set clickable on the 2 fab to false
                    fab_gallery.setClickable(false);
                    fab_camera.setClickable(false);
                    fab_manual.setClickable(false);

                    //change isOpen to false
                    isOpen = false;
                }else{
                    //animate the 2 fab to open
                    fab_gallery.startAnimation(FabOpen);
                    fab_camera.startAnimation(FabOpen);
                    fab_manual.startAnimation(FabOpen);

                    //rotate the plus fab clockwise
                    fab_plus.startAnimation(FabRClockwise);

                    //set clickable on the 2 fab to true
                    fab_gallery.setClickable(true);
                    fab_camera.setClickable(true);
                    fab_manual.setClickable(true);

                    //change isOpen to true
                    isOpen = true;
                }
            }

        });

        //---------------------------------------------------------------------------
        //Implement the onClickListener for the Camera fab
        fab_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Camera codes here
                takePicture(v);

                //I want to close these buttons
                //animate the 2 fab to close
                fab_gallery.startAnimation(FabClose);
                fab_camera.startAnimation(FabClose);
                fab_manual.startAnimation(FabClose);

                //rotate the plus fab anti-clockwise
                fab_plus.startAnimation(FabRAntiClockwise);

                //set clickable on the 2 fab to false
                fab_gallery.setClickable(false);
                fab_camera.setClickable(false);
                fab_manual.setClickable(false);

                //change isOpen to false
                isOpen = false;
            }
        });

        //---------------------------------------------------------------------------
        //Implement the onClickListener for the Gallery fab
        fab_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //gallery codes here
//TODO

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


                //I want to close these buttons
                //animate the 2 fab to close
                fab_gallery.startAnimation(FabClose);
                fab_camera.startAnimation(FabClose);
                fab_manual.startAnimation(FabClose);

                //rotate the plus fab anti-clockwise
                fab_plus.startAnimation(FabRAntiClockwise);

                //set clickable on the 2 fab to false
                fab_gallery.setClickable(false);
                fab_camera.setClickable(false);
                fab_manual.setClickable(false);

                //change isOpen to false
                isOpen = false;
            }
        });

        //---------------------------------------------------------------------------
        //Implement the onClickListener for the Manual fab
        fab_manual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Open up the Create Record view
                Intent intent = new Intent(MainActivity.this, CreateRecord.class);
                startActivity(intent);

                //I want to close these buttons
                //animate the 2 fab to close
                fab_gallery.startAnimation(FabClose);
                fab_camera.startAnimation(FabClose);
                fab_manual.startAnimation(FabClose);

                //rotate the plus fab anti-clockwise
                fab_plus.startAnimation(FabRAntiClockwise);

                //set clickable on the 2 fab to false
                fab_gallery.setClickable(false);
                fab_camera.setClickable(false);
                fab_manual.setClickable(false);

                //change isOpen to false
                isOpen = false;
            }
        });


        //---------------------------------------------------------------------------
        //Check if permission is granted for CAMERA (and WRITE_EXTERNAL_STORAGE implicitly)
        if(ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){

            //If user doesn't grant CAMERA permission, then disable the fab_camera
            fab_camera.setEnabled(false);

            //This will show a dialog box to request for CAMERA & WRITE_EXTERNAL_STORAGE permissions
            ActivityCompat.requestPermissions(this,
                    new String[] {android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    0);
        }
        //---------------------------------------------------------------------------
    }

    /**
     * This method is like a listener method to check if the 2 Permissions we requested for are granted or not
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED){

                //If user has granted both permissions, then enable the fab_camera
                fab_camera.setEnabled(true);
            }
        }

    }

    /**
     * This method is called in the fab_camera's onClickListener
     * @param view
     */
    public void takePicture(View view){

        //Create an Implicit Intent called takePictureIntent that allows user to capture image
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //Call the getOutputMediaFile() to get the path of the file we are going to save into
        //and store in a URI object called "uri_image_file"
        uri_image_file = Uri.fromFile(getOutputMediaFile());

        //Put the uri_image_file as an extra in the intent
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri_image_file);

        /**
         *  the startActivityForResult() method is protected by a condition that calls resolveActivity(),
         *  which returns the first activity component that can handle the intent.
         *  Performing this check is important because if you call startActivityForResult()
         *  using an intent that no app can handle, your app will crash.
         */
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            Log.d("TAG1", "starting takePictureIntent...");
            startActivityForResult(takePictureIntent, IMAGE_CAPTURE_REQUEST);
        }

    }

    //set the imageView's image data to the URI set by the system's camera activity if result is RESULT_OK

    /**
     * This method is like a listener that retrieves the result from whatever happened in
     * startActivityForResult in the takePicture() method.
     *
     * Since we passed in the uri_image_file as an extra in the takePictureIntent,
     * we can retrieve uri of the image file we captured and PROCEED TO API STUFF?
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == IMAGE_CAPTURE_REQUEST){
            if(resultCode == RESULT_OK){

                //save the image so that it shows up in the Gallery app under Budgefy folder
                saveImageInGallery();

                //Open up the Create Record view, passing the uri as an Extra in the intent
                Intent intent = new Intent(this, CreateRecord.class);

                //Convert the uri to a string to store in as an Extra
                intent.putExtra("uri_image_file", uri_image_file.toString());

                //Added by Sherna
                intent.putExtra("REQUESTED_FROM", IMAGE_CAPTURE_REQUEST);

                startActivity(intent);

            }
        }

        if(requestCode == IMAGE_GALLERY_REQUEST){
            if(resultCode == RESULT_OK){
                //address of the image on the SD card
                Uri imageUri = data.getData();
                String uriRealPath = new File(getRealPathFromURI(imageUri)).toString(); //all thanks to getRealPathFromURI
                Log.d("TAGE", uriRealPath); //FI FUCKING ANALLY
                //declare a stream to read the image data from the SD card
                InputStream inputStream;

                //we are getting an input stream, based on the URI of the image
                try{
                    inputStream = getContentResolver().openInputStream(imageUri);

                    //get a bitmap from the system
                    Bitmap image = BitmapFactory.decodeStream(inputStream);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG, 50 , stream);
                    byte[] byteArray = stream.toByteArray();

                    Intent intent = new Intent(this, CreateRecord.class);

                    intent.putExtra("picture", byteArray);
                    intent.putExtra("real_uri_image_file", "file://"+uriRealPath);

                    //Added by Sherna
                    intent.putExtra("REQUESTED_FROM", IMAGE_GALLERY_REQUEST);

                    startActivity(intent);

                } catch(FileNotFoundException e){
                    e.printStackTrace();
                    //show a message to the user indicating that the image is unavailable
                    Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * This method adds the newly created image to the Gallery app on your phone
     */
    private void saveImageInGallery() {

        //Create an Implicit Intent
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

        //Set the URI of your newly created image file to the intent
        mediaScanIntent.setData(uri_image_file);

        //Send the broadcast
        this.sendBroadcast(mediaScanIntent);

        //display msg
        Log.d("TAGA", "Saved picture in gallery!");
        Toast.makeText(this, "Saved picture in Gallery!", Toast.LENGTH_SHORT).show();
    }

    /**
     * This method creates the directory, "Budgefy" to store all the images we captured inside this folder.
     * If the directory does not exist, then it calls mkdirs() to create that directory.
     *
     * Since we want to name each image file uniquely, we name the images by time stamp to avoid
     * overriding image files due to same names.
     *
     * It then creates the new image file.
     *
     * @return
     * the newly created .JPG file
     *
     */
    private static File getOutputMediaFile(){

        //get an access to the public directory where images are saved on device
        //Note: To access this folder in your device, go to My Files>Pictures>Budgefy
        File mediaStorageDir = new File
                (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Budgefy");

        //check if the subdirectory we want exists
        //if it does not exist, call the mkdirs() to create the file path
        //if it fails to create the file path, then return null
        if(!mediaStorageDir.exists()){
            if(!mediaStorageDir.mkdirs()){
                Log.d("TAG3", "Failed to create subdirectory!");
                return null;
            }
        }

        //now, we create the file with the timestamp in the name
        //new Date will return the timestamp in the specified format
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        //create the filepath to be in the sub-directory in the public pictures directory
        //with a file separator, and a IMG_ prefix then the name of the file and the file extension.
        return new File(mediaStorageDir.getPath() + File.separator + "IMG_" +timeStamp + ".jpg");

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


    /**
     * This method inflates (aka create) the Options Menu on the Top Right Hand Corner
     * It inflates the menu_main.XML layout
     *
     * @param menu
     * the menu object
     *
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * When a user selects an item in the Options Menu,
     * then do the corresponding stuff that you wanna do
     * @param item
     * menu item
     *
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.logout:
                // Do Something
                signOut();
                Toast.makeText(getApplicationContext(), "Logout...",
                        Toast.LENGTH_SHORT).show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        Log.d("TaskListFrag","Signing Out");
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);

        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            //Return the current Fragment
            switch(position){
                case 0:
                    Fragment1Home frag1 = new Fragment1Home();
                    return frag1;
                case 1:
                    Fragment2Summary frag2 = new Fragment2Summary();
                    return frag2;
                case 2:
                    Fragment3About frag3 = new Fragment3About();
                    return frag3;
                default:
                return null;
            }


        }

        @Override
        public int getCount() {
            // Show 3 limit pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Home";
                case 1:
                    return "Summary";
                case 2:
                    return "About";
            }
            return null;
        }
    }

}
