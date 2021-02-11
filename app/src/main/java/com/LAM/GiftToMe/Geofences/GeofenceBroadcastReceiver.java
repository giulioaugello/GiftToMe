package com.LAM.GiftToMe.Geofences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.LAM.GiftToMe.FCMFirebase.FCMNotification;
import com.LAM.GiftToMe.R;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GeofenceBroadcastRecTAG";

    @Override
    public void onReceive(Context context, Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        FCMNotification fcmNotification = new FCMNotification(context);

        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes
                    .getStatusCodeString(geofencingEvent.getErrorCode());
            Log.i(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            //invio una notifica quando sono vicino ad un regalo
            fcmNotification.highPriorityNotification(context.getResources().getString(R.string.gift_nearby), context.getResources().getString(R.string.descr_gift_nearby));

        } else {
            // Log the error.
            Log.i(TAG, "Non ci sono regali nella zona");
        }

        List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

        for (Geofence geofence: triggeringGeofences){
            Log.i(TAG, geofence.getRequestId());
        }
    }
}
