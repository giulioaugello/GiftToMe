package com.LAM.GiftToMe.UsefulClass;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.LAM.GiftToMe.FCMFirebase.Chat;
import com.LAM.GiftToMe.FCMFirebase.DBFirestore;
import com.LAM.GiftToMe.FCMFirebase.FirestoreCheckName;
import com.LAM.GiftToMe.MainActivity;
import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.Twitter.TwitterRequests;
import com.LAM.GiftToMe.Twitter.VolleyListener;
import com.android.volley.VolleyError;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import java.util.ArrayList;

public class CustomInfoWindow extends MarkerInfoWindow {

//    public String title, description, issuer, address, giftId, tweetId;
//    private Context mContext;
//    private Activity activity;

    public UsersGift usersGift;
    private Context mContext;
    private Activity activity;


//    public CustomInfoWindow(int layoutResId, MapView mapView, String giftId, String tweetId, String title, String description, String issuer, String address, Context context, Activity activity) {
//        super(layoutResId, mapView);
//
//        this.giftId = giftId;
//        this.tweetId = tweetId;
//        this.title = title;
//        this.description = description;
//        this.issuer = issuer;
//        this.address = address;
//        this.mContext = context;
//        this.activity = activity;
//
//    }

    public CustomInfoWindow(int layoutResId, MapView mapView, UsersGift usersGift, Context context, Activity activity) {
        super(layoutResId, mapView);

        this.usersGift = usersGift;
        this.mContext = context;
        this.activity = activity;

    }

//    @Override
//    public void onOpen(Object item) {
//        super.onOpen(item);
//
//        /**
//         * @param layoutResId layout that must contain these ids: bubble_title,bubble_description,
//         *                    bubble_subdescription, bubble_image
//         * @param mapView
//         */
//        TextView titleText = getView().findViewById(R.id.bubble_title);
//        TextView descriptionText = getView().findViewById(R.id.bubble_description);
//        TextView addressText = getView().findViewById(R.id.address_popup);
//        Button contactButton = getView().findViewById(R.id.contact);
//
//        titleText.setText(title);
//        descriptionText.setText(description);
//        addressText.setText(address);
//        contactButton.setText("Contatta " + issuer);
//
//        contactButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!MainActivity.isLogged){
//                    Toast.makeText(mContext, "Per contattare un utente devi prima fare l'accesso", Toast.LENGTH_SHORT).show();
//                }else{
//                    showReplyDialog();
//                }
//            }
//        });
//    }

    @Override
    public void onOpen(Object item) {
        super.onOpen(item);

        /**
         * @param layoutResId layout that must contain these ids: bubble_title,bubble_description,
         *                    bubble_subdescription, bubble_image
         * @param mapView
         */
        TextView titleText = getView().findViewById(R.id.bubble_title);
        TextView descriptionText = getView().findViewById(R.id.bubble_description);
        TextView addressText = getView().findViewById(R.id.address_popup);
        Button contactButton = getView().findViewById(R.id.contact);

        titleText.setText(usersGift.getName());
        descriptionText.setText(usersGift.getDescription());
        addressText.setText(usersGift.getAddress());
        //contactButton.setText("Contatta " + usersGift.getIssuer());
        contactButton.setText(mContext.getResources().getString(R.string.contact, usersGift.getIssuer()));

        contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MainActivity.isLogged){
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.login_first), Toast.LENGTH_SHORT).show();
                }else{
                    showReplyDialog();
                }
            }
        });
    }

//    private void showReplyDialog(){
//
//        final Dialog replyGiftDialog = new Dialog(activity);
//        View view = activity.getLayoutInflater().inflate(R.layout.reply_gift_dialog,null);
//        replyGiftDialog.setContentView(view);
//        replyGiftDialog.setCancelable(true);
//        replyGiftDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//
//        final EditText replyGiftText = view.findViewById(R.id.get_text_reply);
//        TextView topText = view.findViewById(R.id.top_text);
//        topText.setText("Contatta " + issuer + " per il regalo: " + title);
//        Button sendReply = view.findViewById(R.id.send_button);
//
//        sendReply.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final String yourReply = replyGiftText.getText().toString();
//
//                if (yourReply.isEmpty()){
//                    Toast.makeText(mContext, "Inserisci una risposta", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                final ArrayList<String> message = new ArrayList<>();
//                String notificationTitle = mContext.getResources().getString(R.string.reply_notification_title);
//                String notificationText = mContext.getResources().getString(R.string.reply_notification_text, MainActivity.userName, title);
//
//                message.add(notificationTitle);
//                message.add(notificationText);
//
//                String receiverUserName = issuer;
//
//                DBFirestore.getToken(receiverUserName, message, mContext);
//
//                //Chat.sendMessage(MainActivity.userName, issuer, yourReply);
//                Chat.sendMessageFromGift(MainActivity.userName, issuer, yourReply, title);
//
//                replyGiftDialog.dismiss();
//
//
////                final String id = "";
////                final String reply = "@" + issuer + " " + EditString.normalizeReply(id, MainActivity.userName, issuer, yourReply, giftId);
////
////                TwitterRequests.postTweet(reply, tweetId, mContext, new VolleyListener() {
////                    @Override
////                    public void onError(VolleyError message) {
////
////                    }
////
////                    @Override
////                    public void onResponse(String response) {
////
////                        final ArrayList<String> message = new ArrayList<>();
////                        String notificationTitle = mContext.getResources().getString(R.string.reply_notification_title);
////                        String notificationText = mContext.getResources().getString(R.string.reply_notification_text, MainActivity.userName, title);
////
////                        message.add(notificationTitle);
////                        message.add(notificationText);
////
////                        String receiverUserName = issuer;
////
////                        DBFirestore.getToken(receiverUserName, message, mContext);
////                    }
////                });
//
////                String chatTweet = EditString.normalizeChatTweet(tweetId, issuer, MainActivity.userName);
////
////                TwitterRequests.postTweet(chatTweet, "", mContext, new VolleyListener() {
////                    @Override
////                    public void onError(VolleyError error) {
////                        String data = new String(error.networkResponse.data);
////                        try {
////
////                            JSONObject dataJSON = new JSONObject(data);
////                            JSONArray jsonArray = new JSONArray(dataJSON.getString(mContext.getResources().getString(R.string.json_errors)));
////                            for(int i = 0; i<jsonArray.length();i++){
////
////                                JSONObject JObj = jsonArray.getJSONObject(i);
////                                if(Integer.parseInt(JObj.getString(mContext.getResources().getString(R.string.json_code))) == 187){
////                                    Toast.makeText(mContext, "Non puoi iniziare una nuova chat perchè già ne hai una", Toast.LENGTH_SHORT).show();
////                                }
////                            }
////
////                        }
////                        catch (JSONException e){
////                            e.printStackTrace();
////                        }
////
////                        replyGiftDialog.dismiss();
////
////                    }
////
////                    @Override
////                    public void onResponse(String response) {
////
////                        try{
////                            JSONObject responseJSON = new JSONObject(response);
////                            String tweetId = responseJSON.getString(mContext.getResources().getString(R.string.json_id));
////                            String message = EditString.normalizeChatMessage(issuer, MainActivity.userName, yourReply);
////
////                            TwitterRequests.postTweet(message, tweetId, mContext, new VolleyListener() {
////
////                                @Override
////                                public void onResponse(String response) {
////                                    replyGiftDialog.dismiss();
////                                }
////
////                                @Override
////                                public void onError(VolleyError error) {
////                                    error.printStackTrace();
////                                }
////                            });
////
////                        }
////                        catch (JSONException e){
////                            e.printStackTrace();
////                        }
////                    }
////                });
//
//            }
//        });
//
//        replyGiftDialog.show();
//    }

    private void showReplyDialog(){

        final Dialog replyGiftDialog = new Dialog(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.reply_gift_dialog,null);
        replyGiftDialog.setContentView(view);
        replyGiftDialog.setCancelable(true);
        replyGiftDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final EditText replyGiftText = view.findViewById(R.id.get_text_reply);
        TextView topText = view.findViewById(R.id.top_text);
        topText.setText(mContext.getResources().getString(R.string.contact_for, usersGift.getIssuer(), usersGift.getName()));
        Button sendReply = view.findViewById(R.id.send_button);

        Chat.checkIfChatExist(MainActivity.userName, usersGift.getIssuer(), usersGift.getName(), new FirestoreCheckName() {
            @Override
            public void onReceiverRetrieve(boolean exist) {
                if (exist){
                    Log.i("existexist", "eccomi");
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.chat_exist), Toast.LENGTH_LONG).show();
                }else {
                    replyGiftDialog.show();
                }
            }
        });

        sendReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String yourReply = replyGiftText.getText().toString();

                if (yourReply.isEmpty()){
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.empty_answer), Toast.LENGTH_SHORT).show();
                    return;
                }

                final String id = "";
                final String reply = "@" + usersGift.getIssuer() + " " + EditString.normalizeReply(id, MainActivity.userName, usersGift.getIssuer(), yourReply, usersGift.getGiftId());

                TwitterRequests.postTweet(reply, usersGift.getTweetId(), mContext, new VolleyListener() {
                    @Override
                    public void onError(VolleyError message) {

                    }

                    @Override
                    public void onResponse(String response) {

                        final ArrayList<String> message = new ArrayList<>();
                        String notificationTitle = mContext.getResources().getString(R.string.reply_notification_title);
                        String notificationText = mContext.getResources().getString(R.string.reply_notification_text, MainActivity.userName, usersGift.getName());

                        message.add(notificationTitle);
                        message.add(notificationText);

                        String receiverUserName = usersGift.getIssuer();

                        DBFirestore.getToken(receiverUserName, message, mContext);

                        //Chat.sendMessage(MainActivity.userName, issuer, yourReply);
                        Chat.sendMessageFromGift(MainActivity.userName, usersGift.getIssuer(), yourReply, usersGift.getName());

                    }
                });

                replyGiftDialog.dismiss();

//                String chatTweet = EditString.normalizeChatTweet(tweetId, issuer, MainActivity.userName);
//
//                TwitterRequests.postTweet(chatTweet, "", mContext, new VolleyListener() {
//                    @Override
//                    public void onError(VolleyError error) {
//                        String data = new String(error.networkResponse.data);
//                        try {
//
//                            JSONObject dataJSON = new JSONObject(data);
//                            JSONArray jsonArray = new JSONArray(dataJSON.getString(mContext.getResources().getString(R.string.json_errors)));
//                            for(int i = 0; i<jsonArray.length();i++){
//
//                                JSONObject JObj = jsonArray.getJSONObject(i);
//                                if(Integer.parseInt(JObj.getString(mContext.getResources().getString(R.string.json_code))) == 187){
//                                    Toast.makeText(mContext, "Non puoi iniziare una nuova chat perchè già ne hai una", Toast.LENGTH_SHORT).show();
//                                }
//                            }
//
//                        }
//                        catch (JSONException e){
//                            e.printStackTrace();
//                        }
//
//                        replyGiftDialog.dismiss();
//
//                    }
//
//                    @Override
//                    public void onResponse(String response) {
//
//                        try{
//                            JSONObject responseJSON = new JSONObject(response);
//                            String tweetId = responseJSON.getString(mContext.getResources().getString(R.string.json_id));
//                            String message = EditString.normalizeChatMessage(issuer, MainActivity.userName, yourReply);
//
//                            TwitterRequests.postTweet(message, tweetId, mContext, new VolleyListener() {
//
//                                @Override
//                                public void onResponse(String response) {
//                                    replyGiftDialog.dismiss();
//                                }
//
//                                @Override
//                                public void onError(VolleyError error) {
//                                    error.printStackTrace();
//                                }
//                            });
//
//                        }
//                        catch (JSONException e){
//                            e.printStackTrace();
//                        }
//                    }
//                });

            }
        });

    }
}
