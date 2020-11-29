package com.LAM.GiftToMe.Adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.LAM.GiftToMe.MainActivity;
import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.UsefulClass.UsersGift;

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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

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
                    Toast.makeText(mContext, "ciao", Toast.LENGTH_SHORT).show();
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
}
