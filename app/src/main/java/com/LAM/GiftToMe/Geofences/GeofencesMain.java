package com.LAM.GiftToMe.Geofences;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;

import java.util.ArrayList;

public class GeofencesMain {

    PendingIntent geofencePendingIntent;

    public Geofence createGeofence(String id, String lat, String lon, float radiusSearch){

        Geofence.Builder builder = new Geofence.Builder();
        builder.setRequestId(id);
        builder.setCircularRegion(Double.parseDouble(lat), Double.parseDouble(lon), radiusSearch);
        builder.setExpirationDuration(Geofence.NEVER_EXPIRE);
        builder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);

        return builder.build();

    }

    public GeofencingRequest getGeofencingRequest(Geofence geofence) {

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(geofence);

        return builder.build();
    }

    public PendingIntent getGeofencePendingIntent(Context mContext) {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(mContext, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

}
