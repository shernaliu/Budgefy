package com.mapp.budgefy.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by shern on 23/11/2016.
 */

public class Receipt implements Parcelable {
    private String receiptId;
    private String rRecordName;
    private String merchantName;
    private String location;
    private String category;
    private double totalAmt;
    private String categoryIcon;
    private String imageFileName; //new datatype to store imageFileName
    private String imageUrl;
    private String date;

    //empty constructor needed for datasnapshot
    public Receipt(){}

    public Receipt(String receiptId, String rRecordName, String merchantName,
                   String location, String category, double totalAmt, String categoryIcon,
                   String date, String imageFileName, String imageURL) {
        this.receiptId = receiptId;
        this.rRecordName = rRecordName;
        this.merchantName = merchantName;
        this.location = location;
        this.category = category;
        this.totalAmt = totalAmt;
        this.categoryIcon = categoryIcon;
        this.date = date;
        this.imageFileName = imageFileName;
        this.imageUrl = imageURL;

    }

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

    public String getrRecordName() {
        return rRecordName;
    }

    public void setrRecordName(String rRecordName) {
        this.rRecordName = rRecordName;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getTotalAmt() {
        return totalAmt;
    }

    public void setTotalAmt(double totalAmt) {
        this.totalAmt = totalAmt;
    }

    public String getCategoryIcon() {
        return categoryIcon;
    }

    public void setCategoryIcon(String categoryIcon) {
        this.categoryIcon = categoryIcon;
    }

    public String getImageFileName(){ return imageFileName; }

    public void setImageFileName(String imageFileName){ this.imageFileName = imageFileName; }

    public String getDate(){ return date; }

    public void setDate(String date){ this.date = date; }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "Receipt{" +
                "receiptId='" + receiptId + '\'' +
                ", rRecordName='" + rRecordName + '\'' +
                ", merchantName='" + merchantName + '\'' +
                ", location='" + location + '\'' +
                ", category='" + category + '\'' +
                ", totalAmt=" + totalAmt +
                ", categoryIcon='" + categoryIcon + '\'' +
                ", imageFileName='" + imageFileName + '\'' +
                ", date='" + date + '\'' +
                ", imageUrl='" + imageUrl+ '\'' +
                '}';
    }

    //---------------------------------------------------------------------------
    /*
    Because when you click on an item in the listView, I want to pass a complex object (Receipt)
    as intent extras.

    So when the intent goes from Fragment1Home -> ViewReceiptDetail,
    the Receipt object is sent along with the intent.
    Then I can use the Receipt object to display the details.

    To do this, you need to implement Parcelable.
    I installed the plugin, and choose Code > Generate > Parcelable
    and generated all the methods.

    You dont need to worry about all of these methods.

    Tutorial: https://www.lynda.com/Android-tutorials/Pass-parcelable-objects-intent-extras/486757/555455-4.html
    */
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.receiptId);
        dest.writeString(this.rRecordName);
        dest.writeString(this.merchantName);
        dest.writeString(this.location);
        dest.writeString(this.category);
        dest.writeDouble(this.totalAmt);
        dest.writeString(this.categoryIcon);
        dest.writeString(this.imageFileName);
        dest.writeString(this.date);
        dest.writeString(this.imageUrl);
    }

    protected Receipt(Parcel in) {
        this.receiptId = in.readString();
        this.rRecordName = in.readString();
        this.merchantName = in.readString();
        this.location = in.readString();
        this.category = in.readString();
        this.totalAmt = in.readDouble();
        this.categoryIcon = in.readString();
        this.imageFileName = in.readString();
        this.date = in.readString();
        this.imageUrl = in.readString();
    }

    public static final Parcelable.Creator<Receipt> CREATOR = new Parcelable.Creator<Receipt>() {
        @Override
        public Receipt createFromParcel(Parcel source) {
            return new Receipt(source);
        }

        @Override
        public Receipt[] newArray(int size) {
            return new Receipt[size];
        }
    };
}
