package com.mapp.budgefy;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapp.budgefy.model.Receipt;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shern on 23/11/2016.
 */

public class ViewReceiptDetailAdapter extends ArrayAdapter<String> {

    //Declare a List<Receipt> to store "objects"
    public static List<String> mStringItems = new ArrayList<>();


    //Declare an inflater object to open & read into memory the xml layout file.
    LayoutInflater mInflater;

    public ViewReceiptDetailAdapter(Context context, List<String> objects) {
        super(context, R.layout.list_item_view_receipt_details, objects);

        //Save the reference of the List<String> objects inside mStringItems
        mStringItems = objects;

        mInflater = LayoutInflater.from(context);

        Log.d("Tag1", "Hello, ViewReceiptDetailAdapter constructor is called!");
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
            convertView = mInflater.inflate(R.layout.list_item_view_receipt_details, parent, false);
        }

        //Now, get references to your 2 TextViews
        TextView tv1 = (TextView) convertView.findViewById(R.id.tv_1);
        TextView tv2 = (TextView) convertView.findViewById(R.id.tv_2);

        //Declare a string variable called header;
        String header = "";

        //Settle the heading part
        switch(position){
            case 0:
                header = "RECORD NAME";
                break;
            case 1:
                header = "MERCHANT NAME";
                break;
            case 2:
                header = "LOCATION";
                break;
            case 3:
                header = "CATEGORY";
                break;
            case 4:
                header = "TOTAL AMOUNT";
                break;
            case 5:
                header = "DATE";
                break;
            default:
                header = "";
                break;
        }

        //Get the string in that position in the List
        String string = mStringItems.get(position);

        tv1.setText(header);
        tv2.setText(string);

        return convertView;

    }
}
