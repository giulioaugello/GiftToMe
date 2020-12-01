package com.LAM.GiftToMe.UsefulClass;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.LAM.GiftToMe.MainActivity;
import com.LAM.GiftToMe.R;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

public class CustomInfoWindow extends MarkerInfoWindow {

    public String title, description, issuer, address, giftId;
    private Context mContext;
    private Activity activity;

    public CustomInfoWindow(int layoutResId, MapView mapView, String giftId, String title, String description, String issuer, String address, Context context, Activity activity) {
        super(layoutResId, mapView);

        this.giftId = giftId;
        this.title = title;
        this.description = description;
        this.issuer = issuer;
        this.address = address;
        this.mContext = context;
        this.activity = activity;

    }

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

        titleText.setText(title);
        descriptionText.setText(description);
        addressText.setText(address);
        contactButton.setText("Contatta " + issuer);

        contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MainActivity.isLogged){
                    Toast.makeText(mContext, "Per contattare un utente devi prima fare l'accesso", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(mContext, "ciao", Toast.LENGTH_SHORT).show();
                    showReplyDialog();
                }
            }
        });
    }

    public void showReplyDialog(){

        final Dialog replyGiftDialog = new Dialog(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.reply_gift_dialog,null);
        replyGiftDialog.setContentView(view);
        replyGiftDialog.setCancelable(true);
        replyGiftDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText replyGiftText = view.findViewById(R.id.get_text_reply);
        TextView topText = view.findViewById(R.id.top_text);
        topText.setText("Contatta " + issuer + " per il regalo: " + title);
        Button sendReply = view.findViewById(R.id.send_button);

//        sendReply.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final String yourReply = replyGiftText.getText().toString();
//
//                final String id = "";
//                final String reply = "@" + usersGifts.get(position).getIssuer() + " " + EditString.normalizeReply(id,MainActivity.userName,usersGifts.get(position).getIssuer(),yourReply,usersGifts.get(position).getGiftId());
//
//                TwitterRequests.postTweet(reply, usersGifts.get(position).getTweetId(), mContext, new VolleyListener() {
//                    @Override
//                    public void onError(VolleyError message) {
//
//                    }
//
//                    @Override
//                    public void onResponse(String response) {
//
//                        final ArrayList<String> message = new ArrayList<>();
////                        String notificationTitle = mContext.getResources().getString(R.string.reply_notification_title);
////                        String notificationText = mContext.getResources().getString(R.string.reply_notification_text,MainActivity.userName,usersGifts.get(position).getName());
//
////                        message.add(notificationTitle);
////                        message.add(notificationText);
//
//                        String receiverUserName = usersGifts.get(position).getIssuer();
//
////                        FirestoreFunctions.getUserToken(receiverUserName, new FirestoreResponseListener() {
////                            @Override
////                            public void onComplete(boolean isDocumentExist) {
////
////                            }
////
////                            @Override
////                            public void onTokenRetrieved(String token) {
////                                NotificationHelper.sendFCMNotification(message,token,mContext);
////                            }
////                        });
//                    }
//                });
//
//                String chatTweet = EditString.normalizeChatTweet(usersGifts.get(position).getTweetId(),usersGifts.get(position).getIssuer(),MainActivity.userName);
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
//                            String message = EditString.normalizeChatMessage(usersGifts.get(position).getIssuer(),MainActivity.userName,yourReply);
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
//
//            }
//        });

        replyGiftDialog.show();
    }
}
