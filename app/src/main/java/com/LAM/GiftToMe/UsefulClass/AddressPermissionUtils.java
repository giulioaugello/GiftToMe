package com.LAM.GiftToMe.UsefulClass;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static com.LAM.GiftToMe.Fragment.HomeFragment.REQUEST_PERMISSIONS_REQUEST_CODE;

public class AddressPermissionUtils {

    private static final String TAG = "AddressPermissionTAG";

    //ritorna un indirizzo (stringa) tramite coordinate
    public static String addressString(Context mContext, double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.i(TAG,"My Current loction address " + strReturnedAddress.toString());
            } else {
                Log.i(TAG, "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "Cannot get Address!");
        }
        return strAdd;
    }

    //ritorna le coordinate tramite l'indirizzo
    public static ArrayList<Double> getCoordsFromAddress(String strAddress, Context mContext){

        //fare vari controlli quando l'indirizzo è sbagliato

        Geocoder coder = new Geocoder(mContext);
        List<Address> address;
        ArrayList<Double> coords = new ArrayList<Double>();
        Log.i(TAG,strAddress);

        try {
            address = coder.getFromLocationName(strAddress,5);
            Log.i(TAG,"address " +address + "////////");
            if (address == null || address.size() == 0) {
                Log.i(TAG,"address " +address + "null///");
                return null;
            }

            Address location = address.get(0);
            coords.add(location.getLatitude());
            coords.add(location.getLongitude());

        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG,"coords: " + coords);
        Log.i(TAG,"coordsize: " + coords.size());

        return coords;
    }

    //funzione per chiedere i permessi
    public static void requestPermissionsIfNecessary(String[] permissions, Context context, View view) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);

            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    (Activity) view.getContext(),
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE); //mette in REQUEST_PERMISSIONS_REQUEST_CODE (chiamato in MainActivity dall'onRequest) i permessi da richiedere
        }
    }

}
