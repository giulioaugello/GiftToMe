package com.LAM.GiftToMe.Fragment;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.LAM.GiftToMe.Adapter.MyGiftTweetsAdapter;
import com.LAM.GiftToMe.FCMFirebase.Chat;
import com.LAM.GiftToMe.FCMFirebase.FirestoreCheckName;
import com.LAM.GiftToMe.MainActivity;
import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.Twitter.TwitterFunctions;
import com.LAM.GiftToMe.Twitter.VolleyListener;
import com.LAM.GiftToMe.UsefulClass.AddressPermissionUtils;
import com.LAM.GiftToMe.UsefulClass.EditString;
import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.VolleyError;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class NewGiftFragment extends Fragment {

    private static final String TAG = "NewGiftFragmentTAG";
    private Context mContext;
    private Fragment fragment;

    private ArrayList<Double> newGiftCoords;
    private Button recapGift;
    private String activeCategory = "";
    private TextView nameForm, descriptionForm, addressForm;

    private LinearLayout linearLayout;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.new_gift_fragment, container, false);

        mContext = getActivity().getApplicationContext();
        linearLayout = v.findViewById(R.id.linear_edit);

        nameForm = v.findViewById(R.id.name_gift);
        descriptionForm = v.findViewById(R.id.descr_gift);
        addressForm = v.findViewById(R.id.addr_gift);
        recapGift = v.findViewById(R.id.add_gift);

        fragment = getActivity().getSupportFragmentManager().findFragmentByTag(getResources().getString(R.string.newgift_fragment_tag));

        Chip sportChip, electronicChip, clothingChip, musicChip, otherChip;
        sportChip = v.findViewById(R.id.sport_chip_new);
        electronicChip = v.findViewById(R.id.electronic_chip_new);
        clothingChip = v.findViewById(R.id.clothing_chip_new);
        musicChip = v.findViewById(R.id.music_chip_new);
        otherChip = v.findViewById(R.id.other_chip_new);

        final List<Chip> listChip = Arrays.asList(sportChip, electronicChip, clothingChip, musicChip, otherChip);

        onClickChip(sportChip, listChip, "Sport");
        onClickChip(electronicChip, listChip, "Electronics");
        onClickChip(clothingChip, listChip, "Clothing");
        onClickChip(musicChip, listChip, "Music&Books");
        onClickChip(otherChip, listChip, "Other");

        recapGift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //controlli su form e chip
                if (checkFormAndChip()){
                    return;
                }

                showDialogRecap(nameForm.getText().toString());

            }
        });

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activeCategory = "";
    }

    private boolean checkFormAndChip(){
        boolean isReturn = false;
        //Controllo se tutti i campi stono stati inseriti
        Log.i(TAG, "Nome: " + nameForm.getText().toString());
        if (nameForm.getText().toString().equals("") || descriptionForm.getText().toString().equals("") || addressForm.getText().toString().equals("")){
            Toast.makeText(mContext, mContext.getResources().getString(R.string.all_fields), Toast.LENGTH_SHORT).show();
            isReturn = true;
            return isReturn;
        }

        //la lunghezza massima di TUTTO il tweet è di 280 caratteri
        Log.i(TAG, "Lunghezza descrizione: " + descriptionForm.getText().length());
        if(descriptionForm.getText().length() > 90){
            Toast.makeText(mContext, mContext.getResources().getString(R.string.long_descr), Toast.LENGTH_SHORT).show();
            isReturn = true;
            return isReturn;
        }

        //controllo se l'indirizzo è valido
        Log.i(TAG, "Indirizzo: " + AddressPermissionUtils.getCoordsFromAddress(addressForm.getText().toString(), mContext));
        if(AddressPermissionUtils.getCoordsFromAddress(addressForm.getText().toString(), mContext) == null) {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.valid_address), Toast.LENGTH_SHORT).show();
            isReturn = true;
            return isReturn;
        }

        //controllo se è stata selezionata una categoria
        Log.i(TAG, "Categoria: " + activeCategory);
        if (activeCategory.equals("")){
            Toast.makeText(mContext, mContext.getResources().getString(R.string.check_categ), Toast.LENGTH_SHORT).show();
            isReturn = true;
            return isReturn;
        }

        return isReturn;
    }

    //dialog riassuntivo
    private void showDialogRecap(final String giftName){

        final Dialog dialog = new Dialog(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.recap_newgift_dialog,null);
        dialog.setCancelable(false);
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView recapCategory = view.findViewById(R.id.recap_category);
        TextView recapName = view.findViewById(R.id.recap_name);
        TextView recapDescription = view.findViewById(R.id.recap_descr);
        TextView recapAddress = view.findViewById(R.id.recap_addr);
        ImageView recapImg = view.findViewById(R.id.recap_img);
        Button back = view.findViewById(R.id.back_button);
        Button uploadGift = view.findViewById(R.id.post_tweet);

        recapCategory.setText(activeCategory);
        recapName.setText(nameForm.getText().toString());
        recapDescription.setText(descriptionForm.getText().toString());
        recapAddress.setText(addressForm.getText().toString());

        MyGiftTweetsAdapter.changeCategoryImage(activeCategory, recapImg);
        newGiftCoords = AddressPermissionUtils.getCoordsFromAddress(addressForm.getText().toString(), mContext);

        // controllo se il nome del regalo esiste già nel db, se non esiste mostro il dialog
        Chat.checkNameExist(MainActivity.userName, giftName, mContext, new FirestoreCheckName() {
            @Override
            public void onReceiverRetrieve(boolean exist) {
                if (!exist){
                    dialog.show();
                }
            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        uploadGift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String tweet = EditString.correctTask(newGiftCoords.get(0), newGiftCoords.get(1), nameForm.getText().toString(), descriptionForm.getText().toString(), activeCategory, MainActivity.userName);

                TwitterFunctions.postTweet(tweet, "", mContext, new VolleyListener() {
                    @Override
                    public void onError(VolleyError error) {
                        error.printStackTrace();
                        dialog.dismiss();
                        onErrorDialog();
                    }

                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG,response);
                        dialog.dismiss();
                        Chat.newGiftUpload(MainActivity.userName, giftName); //controllo se esiste un regalo con quel nome
                        onSuccessDialog();

                    }
                });

            }
        });
    }

    //animazione per aggiunta regalo
    private void onSuccessDialog(){
        final Dialog dialog = new Dialog(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.gift_upload_animation,null);
        dialog.setCancelable(false);
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        LottieAnimationView lottieAnimationView = view.findViewById(R.id.animationViewNewGift);
        TextView textView = view.findViewById(R.id.success);

        dialog.show();

        lottieAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.e("Animation:","start");
            }

            @Override
            public void onAnimationEnd(Animator animation) {

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        MyGiftTweetsAdapter.reloadFragment(fragment, getActivity());
                        clearForm(linearLayout);
                    }
                }, 1600);

            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Log.e("Animation:","cancel");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                Log.e("Animation:","repeat");
            }
        });

    }

    private void onErrorDialog(){
        final Dialog dialog = new Dialog(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.gift_error_animation,null);
        dialog.setCancelable(false);
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        LottieAnimationView lottieAnimationView = view.findViewById(R.id.animationViewNewGiftError);

        dialog.show();

        lottieAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.e("Animation:","start");
            }

            @Override
            public void onAnimationEnd(Animator animation) {

//                Handler handler = new Handler(); //serve per ritardare la chiusura del dialog
//                handler.postDelayed(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        dialog.dismiss();
//                        fragment = getActivity().getSupportFragmentManager().findFragmentByTag(MainActivity.newGiftFragmentTag);
//                        MyGiftTweetsAdapter.reloadFragment(fragment, getActivity());
//                    }
//
//                }, 1600);

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        fragment = getActivity().getSupportFragmentManager().findFragmentByTag(MainActivity.newGiftFragmentTag);
                        MyGiftTweetsAdapter.reloadFragment(fragment, getActivity());
                    }
                }, 1600);

            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Log.e("Animation:","cancel");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                Log.e("Animation:","repeat");
            }
        });

    }

    //cancella tutto ciò che c'è negli editText
    private void clearForm(ViewGroup group) {
        for (int i = 0, count = group.getChildCount(); i < count; ++i) {
            View view = group.getChildAt(i);
            if (view instanceof EditText) {
                ((EditText)view).setText("");
            }

            if(view instanceof ViewGroup && (((ViewGroup)view).getChildCount() > 0))
                clearForm((ViewGroup)view);
        }
        activeCategory = "";
    }

    //seleziona la categoria in base al chip selezionato e cambia colore
    private void onClickChip(Chip chip, final List<Chip> list, final String category){
        chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(activeCategory.equals(category)){
                    activeCategory = "";
                }
                else{
                    activeCategory = category;
                }
                colorChip(list);
                Log.i(TAG, activeCategory + "");
            }
        });
    }

    private void colorChip(List<Chip> list){
        switch (activeCategory){
            case "Sport":
                for (Chip chip: list){
                    chip.setChipBackgroundColorResource(R.color.chipUnchecked);
                }
                list.get(0).setChipBackgroundColorResource(R.color.colorChipSelected);
                break;
            case "Electronics":
                for (Chip chip: list){
                    chip.setChipBackgroundColorResource(R.color.chipUnchecked);
                }
                list.get(1).setChipBackgroundColorResource(R.color.colorChipSelected);
                break;
            case "Clothing":
                for (Chip chip: list){
                    chip.setChipBackgroundColorResource(R.color.chipUnchecked);
                }
                list.get(2).setChipBackgroundColorResource(R.color.colorChipSelected);
                break;
            case "Music&Books":
                for (Chip chip: list){
                    chip.setChipBackgroundColorResource(R.color.chipUnchecked);
                }
                list.get(3).setChipBackgroundColorResource(R.color.colorChipSelected);
                break;
            case "Other":
                for (Chip chip: list){
                    chip.setChipBackgroundColorResource(R.color.chipUnchecked);
                }
                list.get(4).setChipBackgroundColorResource(R.color.colorChipSelected);
                break;
            default:
                for (Chip chip: list){
                    chip.setChipBackgroundColorResource(R.color.chipUnchecked);
                }
                break;
        }
    }
}





















