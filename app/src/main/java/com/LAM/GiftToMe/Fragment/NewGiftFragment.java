package com.LAM.GiftToMe.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.LAM.GiftToMe.R;
import com.google.android.material.chip.Chip;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class NewGiftFragment extends Fragment {

    private View v;
    private Context mContext;

    private boolean sportBool, electronicsBool, clothingBool, musicBool, otherBool;
    private TextView nameForm, descriptionForm, addressForm;
    private String giftCategory,giftName,giftDescription,giftAddress;
    private Button addGift;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.new_gift_fragment, container, false);
        mContext = getActivity().getApplicationContext();

        nameForm = v.findViewById(R.id.name_edit);
        descriptionForm = v.findViewById(R.id.descr_gift);
        addressForm = v.findViewById(R.id.addr_gift);
        addGift = v.findViewById(R.id.add_gift);

        final Chip sportChip, electronicChip, clothingChip, musicChip, otherChip;

        sportChip = v.findViewById(R.id.sport_chip);
        electronicChip = v.findViewById(R.id.electronic_chip);
        clothingChip = v.findViewById(R.id.clothing_chip);
        musicChip = v.findViewById(R.id.music_chip);
        otherChip = v.findViewById(R.id.other_chip);

        sportBool = sportChip.isChecked();
        electronicsBool = electronicChip.isChecked();
        clothingBool = clothingChip.isChecked();
        musicBool = musicChip.isChecked();
        otherBool = otherChip.isChecked();

        sportChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!sportBool){
                    if (!electronicsBool && !clothingBool && !musicBool && !otherBool){
                        sportBool = true;
                        sportChip.setChipBackgroundColorResource(R.color.colorChipSelected);
                    }else{
                        Log.i("checkedchecked", "nope");
                    }
                }else{
                    sportBool = false;
                    sportChip.setChipBackgroundColorResource(R.color.ghost_white);
                }

            }
        });
        electronicChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!electronicsBool){
                    if (!sportBool && !clothingBool && !musicBool && !otherBool){
                        electronicsBool = true;

                        electronicChip.setChipBackgroundColorResource(R.color.colorChipSelected);
                    }else{
                        Log.i("checkedchecked", "nope");
                    }
                }else{
                    electronicsBool = false;
                    electronicChip.setChipBackgroundColorResource(R.color.ghost_white);
                }
                //Log.i("checkedchecked", arrayActive + "");
            }
        });
        clothingChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!clothingBool){
                    clothingBool = true;
                    clothingChip.setChipBackgroundColorResource(R.color.colorChipSelected);
                }else{
                    clothingBool = false;
                    clothingChip.setChipBackgroundColorResource(R.color.ghost_white);
                }
                //Log.i("checkedchecked", arrayActive + "");
            }
        });
        musicChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!musicBool){
                    musicBool = true;
                    musicChip.setChipBackgroundColorResource(R.color.colorChipSelected);
                }else{
                    musicBool = false;
                    musicChip.setChipBackgroundColorResource(R.color.ghost_white);
                }
                //Log.i("checkedchecked", arrayActive + "");
            }
        });
        otherChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!otherBool){
                    otherBool = true;
                    otherChip.setChipBackgroundColorResource(R.color.colorChipSelected);
                }else{
                    otherBool = false;
                    otherChip.setChipBackgroundColorResource(R.color.ghost_white);
                }
//                activeFilter(otherBool, otherChip, "Other");
                //Log.i("checkedchecked", arrayActive + "");
            }
        });

        addGift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                giftName = nameForm.getText().toString();
                giftDescription = descriptionForm.getText().toString();
                giftAddress = addressForm.getText().toString();


            }
        });

        return v;
    }
}





















