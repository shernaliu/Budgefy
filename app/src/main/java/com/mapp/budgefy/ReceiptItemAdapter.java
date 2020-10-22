package com.mapp.budgefy;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapp.budgefy.model.Receipt;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by shern on 23/11/2016.
 */

public class ReceiptItemAdapter extends ArrayAdapter<Receipt> {

    //Declare a List<Receipt> to store "objects"
    List<Receipt> mReceiptItems;

    //Declare an inflater object to open & read into memory the xml layout file.
    LayoutInflater mInflater;

    public ReceiptItemAdapter(Context context, List<Receipt> objects) {
        super(context, R.layout.list_item, objects);

        //Save the reference of the List<Receipt> objects inside mStringItems
        mReceiptItems = objects;

        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // If its null, it needs to be instantiated.
        //By doing this, you will always have a non-null view that represents the row
        if(convertView == null){

            //1st argument: the layout file you want
            //2nd argument: the parent view (get that from the parent argument)
            //3rd argument: a boolean val named attachtoroot
            convertView = mInflater.inflate(R.layout.list_item, parent, false);
        }

        //Now, get references to your TextViews and ImageView
        TextView tvName = (TextView) convertView.findViewById(R.id.itemNameText);
        TextView tvPrice = (TextView) convertView.findViewById(R.id.priceText);

        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);

        Receipt receipt = mReceiptItems.get(position);

        tvName.setText(receipt.getrRecordName());
        tvPrice.setText("$" + receipt.getTotalAmt());

        //---------------------------------------------------------------------------
        //Create an input stream
        InputStream inputStream = null;
        try {
            //Load the image file dynamically into the ListView

            //Get the name of the image file in order to load image dynamically
            String imageFile = receipt.getCategoryIcon();

            //open the imageFile for the inputStream
            inputStream = getContext().getAssets().open(imageFile);

            //Create an instance of drawable to load the image file
            //1st argument: the inputstream object
            //2nd argument: source name
            Drawable d = Drawable.createFromStream(inputStream, null);

            //Load the drawable object into the ImageView
            imageView.setImageDrawable(d);
        } catch (IOException e) {
            e.printStackTrace();
        } finally{

            //Remember to always close an inputStream when you finished using it.
            try {
                if(inputStream != null){
                    //Close the inputStream
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //---------------------------------------------------------------------------

        return convertView;
    }
}
