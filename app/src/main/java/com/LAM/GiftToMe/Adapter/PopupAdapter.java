package com.LAM.GiftToMe.Adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.LAM.GiftToMe.FCMFirebase.Chat;
import com.LAM.GiftToMe.FCMFirebase.DBFirestore;
import com.LAM.GiftToMe.MainActivity;
import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.Twitter.TwitterRequests;
import com.LAM.GiftToMe.Twitter.VolleyListener;
import com.LAM.GiftToMe.UsefulClass.EditString;
import com.LAM.GiftToMe.UsefulClass.UsersGift;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class PopupAdapter extends RecyclerView.Adapter<PopupAdapter.ViewHolder> {

    private Context mContext;
    private Activity activity;
    private Fragment fragment;
    private ArrayList<UsersGift> usersGifts;

    private EditText replyGiftText;
    private Button sendReply;

    public PopupAdapter(Context mContext, ArrayList<UsersGift> usersGifts, Activity activity, Fragment fragment) {
        this.mContext = mContext;
        this.activity = activity;
        this.fragment = fragment;
        this.usersGifts = usersGifts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.popup_marker, parent, false);
        return new PopupAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        holder.title.setText(usersGifts.get(position).getName());
        holder.description.setText(usersGifts.get(position).getDescription());
        holder.address.setText(usersGifts.get(position).getAddress());
        //holder.address.setVisibility(View.GONE);
        holder.subDesc.setVisibility(View.GONE);
        holder.contactButton.setText("Contatta " + usersGifts.get(position).getIssuer());
        String category = usersGifts.get(position).getCategory();
        MyGiftTweetsAdapter.changeCategoryImage(category, holder.imageCategory);

        holder.contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MainActivity.isLogged){
                    Toast.makeText(mContext, "Per contattare un utente devi prima fare l'accesso", Toast.LENGTH_SHORT).show();
                }else{
                    showReplyDialog(position, activity, mContext, usersGifts);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersGifts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView imageCategory;
        private TextView title, description, address, subDesc;
        private Button contactButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageCategory = itemView.findViewById(R.id.bubble_image);
            title = itemView.findViewById(R.id.bubble_title);
            description = itemView.findViewById(R.id.bubble_description);
            address = itemView.findViewById(R.id.address_popup);
            subDesc = itemView.findViewById(R.id.bubble_subdescription);
            contactButton = itemView.findViewById(R.id.contact);

        }
    }

    public static void showReplyDialog(final int position, Activity activity, final Context mContext, final ArrayList<UsersGift> usersGifts){

        final Dialog replyGiftDialog = new Dialog(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.reply_gift_dialog,null);
        replyGiftDialog.setContentView(view);
        replyGiftDialog.setCancelable(true);
        replyGiftDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final EditText replyGiftText = view.findViewById(R.id.get_text_reply);
        TextView topText = view.findViewById(R.id.top_text);
        topText.setText("Contatta " + usersGifts.get(position).getIssuer() + " per il regalo: " + usersGifts.get(position).getName());
        Button sendReply = view.findViewById(R.id.send_button);

        sendReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String yourReply = replyGiftText.getText().toString();

//                final ArrayList<String> message = new ArrayList<>();
//                String notificationTitle = mContext.getResources().getString(R.string.reply_notification_title);
//                String notificationText = mContext.getResources().getString(R.string.reply_notification_text,MainActivity.userName,usersGifts.get(position).getName());
//
//                message.add(notificationTitle);
//                message.add(notificationText);
//
//                String receiverUserName = usersGifts.get(position).getIssuer();
//
//                DBFirestore.getToken(receiverUserName, message, mContext);

                Chat.sendMessage(MainActivity.userName, usersGifts.get(position).getIssuer(), yourReply, usersGifts.get(position).getName());

                replyGiftDialog.dismiss();

                //Chat.getMessages("L_A_M98", "Giulio2803");

//                final String id = "";
//                final String reply = "@" + usersGifts.get(position).getIssuer() + " " + EditString.normalizeReply(id, MainActivity.userName, usersGifts.get(position).getIssuer(), yourReply, usersGifts.get(position).getGiftId());
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
//                        String notificationTitle = mContext.getResources().getString(R.string.reply_notification_title);
//                        String notificationText = mContext.getResources().getString(R.string.reply_notification_text,MainActivity.userName,usersGifts.get(position).getName());
//
//                        message.add(notificationTitle);
//                        message.add(notificationText);
//
//                        String receiverUserName = usersGifts.get(position).getIssuer();
//
//                        DBFirestore.getToken(receiverUserName, message, mContext);
//                    }
//                });

                //String chatTweet = EditString.normalizeChatTweet(usersGifts.get(position).getTweetId(),usersGifts.get(position).getIssuer(),MainActivity.userName);

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

            }
        });

        replyGiftDialog.show();
    }

//    public void showReplyDialog(final int position){
//
//        final Dialog replyGiftDialog = new Dialog(activity);
//        View view = activity.getLayoutInflater().inflate(R.layout.reply_gift_dialog,null);
//        replyGiftDialog.setContentView(view);
//        replyGiftDialog.setCancelable(true);
//        replyGiftDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//
//        replyGiftText = view.findViewById(R.id.get_text_reply);
//        TextView topText = view.findViewById(R.id.top_text);
//        topText.setText("Contatta " + usersGifts.get(position).getIssuer() + " per il regalo: " + usersGifts.get(position).getName());
//        sendReply = view.findViewById(R.id.send_button);
//
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
//                        String notificationTitle = mContext.getResources().getString(R.string.reply_notification_title);
//                        String notificationText = mContext.getResources().getString(R.string.reply_notification_text,MainActivity.userName,usersGifts.get(position).getName());
//
//                        message.add(notificationTitle);
//                        message.add(notificationText);
//
//                        String receiverUserName = usersGifts.get(position).getIssuer();
//
//                        DBFirestore.getToken(receiverUserName, message, mContext);
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
//
//        replyGiftDialog.show();
//    }

}
