package com.LAM.GiftToMe.Adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.LAM.GiftToMe.FCMFirebase.ReceiverModel;
import com.LAM.GiftToMe.Fragment.UserTweetsFragment;
import com.LAM.GiftToMe.MainActivity;
import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.UsefulClass.MyGift;
import com.LAM.GiftToMe.UsefulClass.UsersGift;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;


public class UserTweetsAdapter extends RecyclerView.Adapter<UserTweetsAdapter.ViewHolder> {

    private Context mContext;
    private Activity activity;
    private Fragment fragment;
    private ArrayList<UsersGift> usersGifts;

    public UserTweetsAdapter(Context mContext, ArrayList<UsersGift> usersGifts, Activity activity, Fragment fragment) {
        this.mContext = mContext;
        this.activity = activity;
        this.fragment = fragment;
        this.usersGifts = usersGifts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.cardview_users_gift, parent, false);
        return new UserTweetsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.nameGift.setText(usersGifts.get(position).getName());
        holder.nameGift.setSelected(true);

        String category = usersGifts.get(position).getCategory();
        MyGiftTweetsAdapter.changeCategoryImage(category, holder.imageCategory);

        holder.cardUserGift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDetailsGift(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return usersGifts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private CardView cardUserGift;
        private ImageView imageCategory;
        private TextView nameGift;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardUserGift = itemView.findViewById(R.id.card_list);
            imageCategory = itemView.findViewById(R.id.img_category_list);
            nameGift = itemView.findViewById(R.id.gift_name);

        }
    }

    private void showDetailsGift(final int position){

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity, R.style.BottomSheetDialogTheme);

        View bottomSheetView = LayoutInflater.from(mContext).inflate(R.layout.users_gift_bottom_dialog, null);

        Button contactUser = bottomSheetView.findViewById(R.id.contact_user_button);
        ImageView categoryUserGift = bottomSheetView.findViewById(R.id.category_user_gift);

        String categoryString = usersGifts.get(position).getCategory();

        TextView nameUserGift, descriptionUserGift, addressUserGift;
        nameUserGift = bottomSheetView.findViewById(R.id.name_user_gift);
        descriptionUserGift = bottomSheetView.findViewById(R.id.description_user_gift);
        addressUserGift = bottomSheetView.findViewById(R.id.address_user_gift);

        nameUserGift.setText(usersGifts.get(position).getName());
        descriptionUserGift.setText(usersGifts.get(position).getDescription());
        addressUserGift.setText(usersGifts.get(position).getAddress());
        MyGiftTweetsAdapter.changeCategoryImage(categoryString, categoryUserGift);

        contactUser.setText("Contatta " + usersGifts.get(position).getIssuer());
        contactUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MainActivity.isLogged){
                    Toast.makeText(mContext, "Per contattare un utente devi prima fare l'accesso", Toast.LENGTH_SHORT).show();
                }else{
                    PopupAdapter.showReplyDialog(position, activity, mContext, usersGifts);
                }
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }


    //aggiorna la lista
    public void updateList(ArrayList<UsersGift> list){
        usersGifts = list;
        notifyDataSetChanged();
    }
}
