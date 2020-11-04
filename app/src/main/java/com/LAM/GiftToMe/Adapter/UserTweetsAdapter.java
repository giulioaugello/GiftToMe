package com.LAM.GiftToMe.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.UsefulClass.MyGift;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UserTweetsAdapter extends RecyclerView.Adapter<UserTweetsAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<MyGift> myGift;
    private ArrayList<MyGift> allGift;

    public UserTweetsAdapter(Context mContext, ArrayList<MyGift> myGift, ArrayList<MyGift> allGift) {
        this.mContext = mContext;
        this.myGift = myGift;
        this.allGift = allGift;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //setta il layout che andrà a repiclare
        View view = LayoutInflater.from(mContext).inflate(R.layout.cardview_mygift, parent, false);
        return new ViewHolder(view); //il ViewHolder fornisce il layout da popolare con i dati e viene utilizzato dalla RecyclerView per ridurre il numero di layout da creare
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) { //serve per recuperare i riferimenti agli elementi interni da popolare con i dati

        holder.giftName.setText(myGift.get(position).getName());
        holder.giftDescription.setText(myGift.get(position).getDescription());
        holder.giftAddress.setText(myGift.get(position).getAddress());
        ImageView imageCategory = holder.itemView.findViewById(R.id.img_category);

        String activeCategory = myGift.get(position).getCategory();

        switch (activeCategory){
            case "Sport":
                imageCategory.setBackgroundResource(R.drawable.ic_launcher_foreground);
            case "Clothing":
                imageCategory.setBackgroundResource(R.drawable.ic_launcher_foreground);
            case "Electronics":
                imageCategory.setBackgroundResource(R.drawable.ic_launcher_foreground);
            case "Music&Games":
                imageCategory.setBackgroundResource(R.drawable.ic_launcher_foreground);
            case "Other":
                imageCategory.setBackgroundResource(R.drawable.ic_launcher_foreground);
        }
    }

    @Override
    public int getItemCount() { //ritorna il numero di elementi che avrà la recycler view
        return myGift.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView deleteButton, editButton, imgCategory;
        private TextView giftName, giftDescription, giftAddress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            deleteButton = itemView.findViewById(R.id.delete);
            editButton = itemView.findViewById(R.id.edit);
            imgCategory = itemView.findViewById(R.id.img_category);
            giftName = itemView.findViewById(R.id.gift_name);
        }
    }
}
