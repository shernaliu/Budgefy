package com.mapp.budgefy.receipt;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.mapp.budgefy.SignInActivity;
import com.mapp.budgefy.model.Receipt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by shern on 23/11/2016.
 */

public class ReceiptDataProvider {

    //Declare a List of Receipt objects called receiptItemList
    public static List<Receipt> receiptItemList;

    //Declare a List<String> to store the 7 category strings
    public static List<String> categoryList;

    //Declare a Map called receiptItemMap that maps the receiptID to the Receipt object
    public static Map<String, Receipt> receiptItemMap;

    //Declare a List of Strings to store record Names
    public static List<String> receiptNameList;

    //To be deleted when Firebase is implemented
    //Just create an int array to store [1,100]
    public static int counter;

    //firebase related stuff
    private static DatabaseReference databaseFixedReference;
    private static GoogleSignInAccount mSignInAccount;

    //To initialize this static variables, you need the static initializer block.
    static{
        //Initialize both variables
        receiptItemList = new ArrayList<>();
        categoryList = new ArrayList<>();
        receiptItemMap = new HashMap<>();
        receiptNameList = new ArrayList<>();

        counter = 1;

        //Populate the categoryList with the 6 category types
        addCategoryItem(new String("food.png"));
        addCategoryItem(new String("grocery.png"));
        addCategoryItem(new String("entertainment.png"));
        addCategoryItem(new String("transport.png"));
        addCategoryItem(new String("bill.png"));
        addCategoryItem(new String("misc.png"));


//        //Populate the receiptItemList & receiptItemMap with some sample Receipt data
//        addReceiptItem(new Receipt("101","Today's Breakfast", "McDonald", "Singapore Polytechnic",
//                "Food", 9.90, "food.png", "receipt_placeholder.png", "21/07/12"));
//
//        addReceiptItem(new Receipt("102", "Bus Fare", "SMRT", "Boon Lay Bus Interchange",
//                "Transport", 1.65, "transport.png", "receipt_placeholder.png", "12/04/10"));
//
//        addReceiptItem(new Receipt("103", "NTUC Groceries", "NTUC Fairprice", "Jurong Point",
//                "Groceries", 15.90, "grocery.png", "receipt_placeholder.png", "15/02/14"));
//
//        addReceiptItem(new Receipt("104", "Medication", "Clementi Polyclinic", "Clementi",
//                "Bills", 6.00, "bill.png", "receipt_placeholder.png", "11/09/90"));
//
//        addReceiptItem(new Receipt("105", "Astro Boy Movie Poster", "Toy Outpost", "JEM",
//                "Miscellaneous", 19.00, "misc.png", "receipt_placeholder.png", "09/04/15"));
//
//        addReceiptItem(new Receipt("106", "Shark Soft Toy", "IKEA", "IKEA Alexandra",
//                "Miscellaneous", 30.00, "misc.png", "receipt_placeholder.png", "08/02/11"));
//
//        addReceiptItem(new Receipt("107", "Dinner date with Bumba", "Sushi Restaurant", "Orchard Road",
//                "Food", 150.00, "food.png", "receipt_placeholder.png", "29/08/94"));
//
//        addReceiptItem(new Receipt("108", "Buy wine for celebration", "Cold Storage", "Sembawang Cold Storage",
//                "Entertainment", 29.90, "entertainment.png", "receipt_placeholder.png", "24/07/21"));
    }

    /**
     * Static method to add the Receipt object to both of the collections
     *
     * @param receipt
     * the receipt object to be added
     */
    private static void addReceiptItem(Receipt receipt){
        receiptItemList.add(receipt);
        receiptItemMap.put(receipt.getReceiptId(), receipt);
    }

    /**
     * Static method to add the category string to categoryList
     * @param category
     */
    private static void addCategoryItem(String category){
        categoryList.add(category);
    }



}


