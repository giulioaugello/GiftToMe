package com.LAM.GiftToMe.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.LAM.GiftToMe.Fragment.MyGiftFragment;
import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.Twitter.TwitterRequests;
import com.LAM.GiftToMe.Twitter.VolleyListener;
import com.LAM.GiftToMe.UsefulClass.AddressPermissionUtils;
import com.LAM.GiftToMe.UsefulClass.MyGift;
import com.LAM.GiftToMe.UsefulClass.EditString;
import com.android.volley.VolleyError;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MyGiftTweetsAdapter extends RecyclerView.Adapter<MyGiftTweetsAdapter.ViewHolder> {

    private Context mContext;
    private Activity activity;
    private Fragment fragment;
    private ArrayList<MyGift> myGift;
    private ArrayList<MyGift> allGift;
    private static final String TAG = "UserGiftRecyclerViewTAG";
    private TextView nameLong, descriptionLong, addressLong;
    private ImageView imageCategory;
    private String nameString,addressString,descriptionString,categoryString, issuer;

    public static boolean isOnTouch = false;

    public MyGiftTweetsAdapter(Context mContext, ArrayList<MyGift> myGift, Activity activity, Fragment fragment) {
        this.mContext = mContext;
        this.activity = activity;
        this.fragment = fragment;
        this.myGift = myGift;
        this.allGift = myGift;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //setta il layout che andrà a repiclare
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_mygift, parent, false);
        return new ViewHolder(view); //il ViewHolder fornisce il layout da popolare con i dati e viene utilizzato dalla RecyclerView per ridurre il numero di layout da creare
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) { //serve per recuperare i riferimenti agli elementi interni da popolare con i dati

        holder.giftName.setText(myGift.get(position).getName());

        String activeCategory = myGift.get(position).getCategory();

        changeCategoryImage(activeCategory, holder.imgCategory);

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRemoveDialog(myGift.get(position).getTweetId(), position);
            }
        });

        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editGift(position);
            }
        });

        showDialogLongClick(position, holder.card);

    }

    @Override
    public int getItemCount() { //ritorna il numero di elementi che avrà la recycler view
        return myGift.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView deleteButton, editButton, imgCategory;
        private TextView giftName;
        private CardView card;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            deleteButton = itemView.findViewById(R.id.delete);
            editButton = itemView.findViewById(R.id.edit);
            imgCategory = itemView.findViewById(R.id.img_category);
            giftName = itemView.findViewById(R.id.gift_name);
            card = itemView.findViewById(R.id.card_standard);
        }
    }


    private void showRemoveDialog(final String id, final int position){
        final Dialog dialog = new Dialog(activity);
        dialog.setCancelable(false);

        final View v  = activity.getLayoutInflater().inflate(R.layout.delete_mygift_dialog,null);
        dialog.setContentView(v);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button cancelDelete, confirmDelete;
        cancelDelete = v.findViewById(R.id.cancel_delete);
        confirmDelete = v.findViewById(R.id.delete_button);

        cancelDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

       confirmDelete.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               removeGift(id, position);
               myGift.remove(position);
               notifyItemRemoved(position);
               reloadFragment(fragment, activity);
               dialog.dismiss();
           }
       });

        dialog.show();
    }

    private void removeGift(final String id, final int position){

        TwitterRequests.removeGift(id, mContext, new VolleyListener() {

            @Override
            public void onResponse(String response) {
                Log.i(TAG, response);
            }

            @Override
            public void onError(VolleyError error) {
                error.printStackTrace();
            }
        });

    }

    public void editGift(final int position) {

        final Dialog dialog = new Dialog(activity);
        dialog.setCancelable(false);

        final View v  = activity.getLayoutInflater().inflate(R.layout.edit_mygift_dialog,null);
        dialog.setContentView(v);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        nameLong = v.findViewById(R.id.name_edit);
        descriptionLong = v.findViewById(R.id.descr_edit);
        addressLong = v.findViewById(R.id.addr_edit);

        nameLong.setText(myGift.get(position).getName());
        descriptionLong.setText(myGift.get(position).getDescription());
        addressLong.setText(myGift.get(position).getAddress());

        issuer = myGift.get(position).getIssuer();

        final RadioButton sport, electronics, clothing, music, other;
        Button editButton = v.findViewById(R.id.edit);
        Button cancelButton = v.findViewById(R.id.cancel);
        RadioGroup radioGroup = v.findViewById(R.id.myRadioGroup);
        sport = v.findViewById(R.id.sportRadio);
        electronics = v.findViewById(R.id.electronicsRadio);
        clothing = v.findViewById(R.id.clothingRadio);
        music = v.findViewById(R.id.musicRadio);
        other = v.findViewById(R.id.otherRadio);

        categoryString = myGift.get(position).getCategory();

        int selectedId = radioGroup.getCheckedRadioButtonId();
        switch (categoryString) {
            case "Sport":
                selectedId = sport.getId();
                break;
            case "Electronics":
                selectedId = electronics.getId();
                break;
            case "Clothing":
                selectedId = clothing.getId();
                break;
            case "Music&Books":
                selectedId = music.getId();
                break;
            case "Other":
                selectedId = other.getId();
                break;
        }
        RadioButton radioButtonActive = v.findViewById(selectedId);
        radioButtonActive.setChecked(true);
        //Toast.makeText(mContext, radioButtonActive.getText(), Toast.LENGTH_SHORT).show();

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                RadioButton radioChecked = v.findViewById(checkedId);
                //Log.i("chk", "id " + radioChecked.getText());

                switch (checkedId){
                    case R.id.sportRadio:
                    case R.id.electronicsRadio:
                    case R.id.clothingRadio:
                    case R.id.musicRadio:
                    case R.id.otherRadio:
                        categoryString = (String) radioChecked.getText();
                        //Log.i("chk", "Nuova categoria " + categoryString);
                        break;
                }

            }
        });


        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nameString = nameLong.getText().toString();
                descriptionString = descriptionLong.getText().toString();
                addressString = addressLong.getText().toString();
                ArrayList<Double> addressCoords = AddressPermissionUtils.getCoordsFromAddress(addressString, mContext);

                if(nameString.isEmpty() || descriptionString.isEmpty() || addressString.isEmpty()){
                    Toast.makeText(mContext, "Inserisci tutti i campi", Toast.LENGTH_LONG).show();
                    return;
                }
                if(AddressPermissionUtils.getCoordsFromAddress(addressString, mContext) == null) {
                    Toast.makeText(mContext, "Inserisci un indirizzo valido", Toast.LENGTH_SHORT).show();
                    return;
                }

                String task = "";
                task = EditString.normalizeTask(addressCoords.get(0), addressCoords.get(1), nameString, descriptionString, categoryString, issuer);

                removeGift(myGift.get(position).getTweetId(),position);
                myGift.remove(position);
                notifyItemRemoved(position);

                TwitterRequests.postTweet(task, "", mContext, new VolleyListener() {
                    @Override
                    public void onError(VolleyError message) {
                        message.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response) {
                        reloadFragment(fragment,activity);
                        dialog.dismiss();
                    }
                });
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void showDialogLongClick(final int position, CardView cardView){

        final Dialog dialog = new Dialog(activity);
        dialog.setCancelable(true);

        View view  = activity.getLayoutInflater().inflate(R.layout.onlongclick_card_mygift,null);
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        nameLong = view.findViewById(R.id.name_longclick);
        descriptionLong = view.findViewById(R.id.gift_description);
        addressLong = view.findViewById(R.id.gift_address);
        imageCategory = view.findViewById(R.id.small_category);

        nameLong.setText(myGift.get(position).getName());
        descriptionLong.setText(myGift.get(position).getDescription());
        addressLong.setText(myGift.get(position).getAddress());
        String cat = myGift.get(position).getCategory();
        changeCategoryImage(cat, imageCategory);

        cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                dialog.show();
                return true;
            }
        });

        cardView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (dialog.isShowing()) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    int action = event.getActionMasked();
                    if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_BUTTON_RELEASE) {
                        dialog.dismiss();
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        return true;
                    }
                }
                return false;
            }
        });

    }

    public void filter(ArrayList<String> arrayList){

        if (arrayList.size() == 0){ //mi serve per far visualizzare tutti i regali
            myGift = allGift;
        }else{
            ArrayList<MyGift> filteredList = new ArrayList<>();

            for(String userCategory : arrayList){
                for(MyGift userGift : allGift) {
                    if (userGift.getCategory().equals(userCategory)) {
                        filteredList.add(userGift);
                    }
                }
            }
            myGift = filteredList;
        }

        notifyDataSetChanged();
    }

    public static void reloadFragment(Fragment fragment, Activity activity){

        //Log.i("fragmentfragment", "Sono in " + fragment.getTag());

        FragmentManager fragmentManager = ((AppCompatActivity)activity).getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.detach(fragment).attach(fragment).commit();

    }

    public static void changeCategoryImage(String category, ImageView imageView){
        switch (category) {
            case "Sport":
                imageView.setBackgroundResource(R.drawable.sport);
                break;
            case "Electronics":
                imageView.setBackgroundResource(R.drawable.electronic);
                break;
            case "Clothing":
                imageView.setBackgroundResource(R.drawable.clothing);
                break;
            case "Music&Books":
                imageView.setBackgroundResource(R.drawable.musicbook);
                break;
            default:
                imageView.setBackgroundResource(R.drawable.file);
                break;
        }
    }
}
