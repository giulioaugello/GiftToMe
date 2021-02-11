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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.LAM.GiftToMe.FCMFirebase.Chat;
import com.LAM.GiftToMe.Fragment.ProfileFragment;
import com.LAM.GiftToMe.MainActivity;
import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.Twitter.TwitterFunctions;
import com.LAM.GiftToMe.Twitter.VolleyListener;
import com.LAM.GiftToMe.UsefulClass.EditString;
import com.LAM.GiftToMe.UsefulClass.MyReply;
import com.android.volley.VolleyError;

import java.util.ArrayList;

public class MyReplyAdapter extends RecyclerView.Adapter<MyReplyAdapter.ViewHolder>{

    private static final String TAG = "MyReplyAdapterTAG";
    private Context mContext;
    private Activity activity;
    private ArrayList<MyReply> myReplies;

    private EditText editText;


    public MyReplyAdapter(Context mContext, ArrayList<MyReply> myReplies, Activity activity) {
        this.mContext = mContext;
        this.activity = activity;
        this.myReplies = myReplies;
    }

    @NonNull
    @Override
    public MyReplyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.replies_adapter_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyReplyAdapter.ViewHolder holder, final int position) {
        TwitterFunctions.getGiftName(mContext, myReplies.get(position).getTweetReplyId(), new VolleyListener() {
            @Override
            public void onResponse(String response) {
                holder.giftName.setText(response);

            }

            @Override
            public void onError(VolleyError error) {
                error.printStackTrace();
            }


        });

        holder.myReply.setText(myReplies.get(position).getMessage());

        holder.editReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog(position);
            }
        });

        holder.removeReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRemoveDialog(position);
//                removeReply(myReplies.get(position).getTweetId(), position);
//                myReplies.remove(position);
//                notifyItemRemoved(position);
//                goBack(mContext, activity);

            }
        });
    }

    @Override
    public int getItemCount() {
        return myReplies.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView giftName, myReply;
        private ImageView editReply, removeReply;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            giftName = itemView.findViewById(R.id.gift_name_reply);
            myReply = itemView.findViewById(R.id.gift_reply_message);
            removeReply = itemView.findViewById(R.id.remove_reply);
            editReply = itemView.findViewById(R.id.edit_reply);

        }
    }

    //dialog per rimozione
    private void showRemoveDialog(final int position){
        final Dialog dialog = new Dialog(activity);
        dialog.setCancelable(false);

        final View v  = activity.getLayoutInflater().inflate(R.layout.delete_myreply_dialog,null);
        dialog.setContentView(v);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button cancelDelete, confirmDelete;
        cancelDelete = v.findViewById(R.id.cancel_delete_reply);
        confirmDelete = v.findViewById(R.id.delete_button_reply);

        cancelDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        confirmDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TwitterFunctions.getGiftName(mContext, myReplies.get(position).getTweetReplyId(), new VolleyListener() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("receiverreceiver", "ci sono1");
                        Chat.deleteFirstMessage(MainActivity.userName, myReplies.get(position).getReceiverName(), response);

                    }

                    @Override
                    public void onError(VolleyError error) {
                        error.printStackTrace();
                    }


                });

                removeReply(myReplies.get(position).getTweetId(), position);
                myReplies.remove(position);
                notifyItemRemoved(position);
                goBack(mContext, activity);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    protected void showEditDialog(final int position){

        final Dialog dialog = new Dialog(activity);
        dialog.setCancelable(true);

        View view  = activity.getLayoutInflater().inflate(R.layout.reply_edit_dialog, null);
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        editText = view.findViewById(R.id.reply_edit);

        editText.setText(myReplies.get(position).getMessage());

        Button editButton = view.findViewById(R.id.edit_edit);
        Button cancelButton = view.findViewById(R.id.cancel_edit);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String yourReply = editText.getText().toString();

                if(yourReply.isEmpty()) {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.empty_answer), Toast.LENGTH_SHORT).show();
                    return;
                }

                String reply = "@" + myReplies.get(position).getReceiverName() + " " + EditString.correctReply(myReplies.get(position).getTweetId(), MainActivity.userName, myReplies.get(position).getReceiverName(), yourReply, myReplies.get(position).getTargetId());
                Log.i("receiverreceiver", myReplies.get(position).getReceiverName() + " ... " + myReplies.get(position).getTargetId());
                TwitterFunctions.postTweet(reply, myReplies.get(position).getTweetReplyId(), mContext, new VolleyListener() {
                    @Override
                    public void onError(VolleyError message) {

                    }

                    @Override
                    public void onResponse(String response) {
                        removeReply(myReplies.get(position).getTweetId(), position);
                        myReplies.remove(position);
                        notifyItemRemoved(position);



                        dialog.dismiss();
                        goBack(mContext, activity);

                    }
                });

                TwitterFunctions.getGiftName(mContext, myReplies.get(position).getTweetReplyId(), new VolleyListener() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("receiverreceiver", "ci sono");
                        Chat.changeFirstMessage(MainActivity.userName, myReplies.get(position).getReceiverName(), response, myReplies.get(position).getMessage(), yourReply);

                    }

                    @Override
                    public void onError(VolleyError error) {
                        error.printStackTrace();
                    }


                });
            }
        });

        dialog.show();
    };

    private void removeReply(String id,final int position){
        TwitterFunctions.deleteGift(id, mContext, new VolleyListener() {

            @Override
            public void onResponse(String response) {
                Log.i(TAG,response);
            }

            @Override
            public void onError(VolleyError error) {
                error.printStackTrace();
            }
        });
    }

    public static void goBack(Context mContext, Activity activity){
        String profileFragmentTag = mContext.getResources().getString(R.string.profile_fragment_tag);
        ProfileFragment profileFragment = new ProfileFragment();
        FragmentManager fragmentManager = ((AppCompatActivity)activity).getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.bottomtotop, R.anim.toptobottom);
        fragmentTransaction.replace(R.id.fragment_container,profileFragment ,profileFragmentTag).commit();
        fragmentTransaction.addToBackStack(profileFragmentTag);
        MainActivity.activeFragment = profileFragment ;
    }
}
