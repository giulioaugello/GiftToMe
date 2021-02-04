package com.LAM.GiftToMe.FCMFirebase;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import com.LAM.GiftToMe.MainActivity;
import com.LAM.GiftToMe.R;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class FCMNotification extends ContextWrapper {

    public static String CHANNEL_NAME = "High priority channel";
    public static String CHANNEL_ID = "com.LAM.GiftToMe" + CHANNEL_NAME;

    public static String FCM_SEND_ENDPOINT = "https://fcm.googleapis.com/fcm/send";

    public FCMNotification(Context base) {
        super(base);
        //con android >= O si richiedono i canali
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("this is the desc of the channel");
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public void highPriorityNotification(String title, String body){
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,267, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_baseline_card_giftcard_24)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setColor(Color.BLUE)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().setSummaryText("GiftToMe").setBigContentTitle(title).bigText(body))
                .build();

        //se l'id non è random ma è uguale per tutte le notifiche la nuova notifica sovrascriverà la precendente se presente
        NotificationManagerCompat.from(this).notify(new Random().nextInt(), notification);

    }


    public static void sendNotification(ArrayList<String> message , String token, final Context mContext){
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);

        Log.i("FCMTAG", "eccomi in sendFCM " + token + ", message: " + message);
        JSONObject json = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(mContext.getResources().getString(R.string.not_title), message.get(0));
            jsonObject.put(mContext.getResources().getString(R.string.not_body), message.get(1));
            jsonObject.put("forceStart","1");


            json.put(mContext.getResources().getString(R.string.not_priority), mContext.getResources().getString(R.string.not_priority_value));

            json.put(mContext.getResources().getString(R.string.not_data), jsonObject);
            json.put(mContext.getResources().getString(R.string.not_to), token);

        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_SEND_ENDPOINT, json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            public Map<String, String> getHeaders(){

                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "key=" + mContext.getResources().getString(R.string.FCM_Server_Key));
                params.put("Content-Type", "application/json");
                return params;
            }
        };
        requestQueue.add(jsonObjectRequest);

    }


    public void displayNotification(RemoteMessage remoteMessage){

        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, activityIntent, 0);


        String titleString = getResources().getString(R.string.not_title);
        String bodyString = getResources().getString(R.string.not_body);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle((remoteMessage.getData().get(titleString)));
        bigTextStyle.bigText(remoteMessage.getData().get(bodyString));


        android.app.Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_card_giftcard_24)
                .setContentTitle((remoteMessage.getData().get(titleString)))
                .setContentText(remoteMessage.getData().get(bodyString))
                .setStyle(bigTextStyle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setColor(Color.BLUE)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .build();

        NotificationManagerCompat notificationManager =  NotificationManagerCompat.from(this);
        notificationManager.notify(new Random().nextInt(), notification);
    }
}
