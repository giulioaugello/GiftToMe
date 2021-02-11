package com.LAM.GiftToMe.Adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.LAM.GiftToMe.FCMFirebase.FirestoreCheckName;
import com.LAM.GiftToMe.MainActivity;
import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.Twitter.TwitterFunctions;
import com.LAM.GiftToMe.Twitter.VolleyListener;
import com.LAM.GiftToMe.UsefulClass.EditString;
import com.LAM.GiftToMe.UsefulClass.UsersGift;
import com.android.volley.VolleyError;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class PopupAdapter extends RecyclerView.Adapter<PopupAdapter.ViewHolder> {

    private Context mContext;
    private Activity activity;
    private Fragment fragment;
    private ArrayList<UsersGift> usersGifts;

    public PopupAdapter(Context mContext, ArrayList<UsersGift> usersGifts, Activity activity, Fragment fragment) {
        this.mContext = mContext;
        this.activity = activity;
        this.fragment = fragment;
        this.usersGifts = usersGifts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.popup_marker, parent, false);
        return new PopupAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        holder.title.setText(usersGifts.get(position).getName());
        holder.description.setText(usersGifts.get(position).getDescription());
        holder.address.setText(usersGifts.get(position).getAddress());
        holder.subDesc.setVisibility(View.GONE);
        holder.contactButton.setText(mContext.getResources().getString(R.string.contact, usersGifts.get(position).getIssuer()));
        String category = usersGifts.get(position).getCategory();
        MyGiftTweetsAdapter.changeCategoryImage(category, holder.imageCategory);

        holder.contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MainActivity.isLogged){
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.login_first), Toast.LENGTH_SHORT).show();
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
        topText.setText(mContext.getResources().getString(R.string.contact_for, usersGifts.get(position).getIssuer(), usersGifts.get(position).getName()));
        Button sendReply = view.findViewById(R.id.send_button);

        //controllo se la chat già esiste
        Chat.checkIfChatExist(MainActivity.userName, usersGifts.get(position).getIssuer(), usersGifts.get(position).getName(), new FirestoreCheckName() {
            @Override
            public void onReceiverRetrieve(boolean exist) {
                if (exist){
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
                final String reply = "@" + usersGifts.get(position).getIssuer() + " " + EditString.correctReply(id, MainActivity.userName, usersGifts.get(position).getIssuer(), yourReply, usersGifts.get(position).getGiftId());

                TwitterFunctions.postTweet(reply, usersGifts.get(position).getTweetId(), mContext, new VolleyListener() {
                    @Override
                    public void onError(VolleyError message) {

                    }

                    @Override
                    public void onResponse(String response) {

                        //creo la notifica
                        final ArrayList<String> message = new ArrayList<>();
                        String notificationTitle = mContext.getResources().getString(R.string.reply_notification_title);
                        String notificationText = mContext.getResources().getString(R.string.reply_notification_text,MainActivity.userName,usersGifts.get(position).getName());

                        message.add(notificationTitle);
                        message.add(notificationText);

                        String receiverUserName = usersGifts.get(position).getIssuer();

                        //prendo token e invio notifica
                        DBFirestore.getToken(receiverUserName, message, mContext);

                        //invio messaggio
                        Chat.sendMessageFromGift(MainActivity.userName, usersGifts.get(position).getIssuer(), yourReply, usersGifts.get(position).getName());

                    }
                });

                replyGiftDialog.dismiss();

            }
        });

    }

}
