package com.LAM.GiftToMe.Adapter;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.UsefulClass.MyGift;
import com.LAM.GiftToMe.UsefulClass.UsersGift;

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
    private static final String TAG = "UserGiftRecyclerViewTAG";
    private TextView nameLong, descriptionLong, addressLong;
    private ImageView imageCategory;
    private String nameString,addressString,descriptionString,categoryString, issuer;
    private ScrollView scroll;

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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.nameGift.setText(usersGifts.get(position).getName());

        String category = usersGifts.get(position).getCategory();
        MyGiftTweetsAdapter.changeCategoryImage(category, holder.imageCategory);

    }

    @Override
    public int getItemCount() {
        return usersGifts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private CardView cardView;
        private ImageView imageCategory;
        private TextView nameGift;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.card_list);
            imageCategory = itemView.findViewById(R.id.img_category_list);
            nameGift = itemView.findViewById(R.id.gift_name);

        }
    }

}
