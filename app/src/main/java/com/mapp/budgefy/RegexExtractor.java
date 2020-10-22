package com.mapp.budgefy;

import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by shern on 25/12/2016.
 */

public class RegexExtractor {
    static BufferedReader bufferedReader;

    static Pattern DATE_PATTERN_1 = Pattern.compile("[0-9]+\\s[A-z]+\\s[0-9]+"); //10 Oct 16
    static Pattern DATE_PATTERN_2 = Pattern.compile("[0-9]+\\-[0-9]+\\-[0-9]+"); //2016-10-23
    static Pattern DATE_PATTERN_3 = Pattern.compile("([0-9]+\\/)+\\d{4}"); //31/10/2016

    static Pattern DATE_PATTERN_COMBINED =
            Pattern.compile("([0-9]+\\s[A-z]+\\s[0-9]+)|([0-9]+\\-[0-9]+\\-[0-9]+)|(([0-9]+\\/)+\\d{4})");

    static Pattern PRICE_PATTERN = Pattern.compile("(\\d{1,3}.\\d{1,2})|\\$(\\d+\\.\\d{2})"); //$10.70 or 3.00 or 10.70


    public static String extractDate(String rawString) {

        String date = "";

        //use ByteArrayInputStream to get the bytes of the String and convert them to InputStream.
        InputStream inputStream = new ByteArrayInputStream(rawString.getBytes(Charset.forName("UTF-8")));

        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String line = null;

        try {
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.isEmpty()) {

                    Log.d("line:", line);

                    Matcher m = DATE_PATTERN_3.matcher(line);
                    if (m.find()) {
                        date = m.group(0);
                        Log.d("MatchedDate:",date);

                    }
                }
            }

            bufferedReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return date;
    }


    public static String extractMerchantName(String rawString) {

        String merchantName = "";

        //use ByteArrayInputStream to get the bytes of the String and convert them to InputStream.
        InputStream inputStream = new ByteArrayInputStream(rawString.getBytes(Charset.forName("UTF-8")));

        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));


        try {
            merchantName = bufferedReader.readLine();
            Log.d("merchantname", merchantName);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return merchantName;
    }


    public static String extractPrice(String rawString) {

        String price = "";

        //use ByteArrayInputStream to get the bytes of the String and convert them to InputStream.
        InputStream inputStream = new ByteArrayInputStream(rawString.getBytes(Charset.forName("UTF-8")));

        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String line = null;

        try {
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.isEmpty()) {

                    Matcher m = PRICE_PATTERN.matcher(line);
                    if (m.find()) {
                        price = m.group(0);
                        Log.d("MatchedPrice:",price);

                        if (price.contains("$")){
                            price = price.replace("$","");
                            Log.d("MatchedPrice:",price);

                        }
                    }
                }
            }

            bufferedReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return price;
    }
}
