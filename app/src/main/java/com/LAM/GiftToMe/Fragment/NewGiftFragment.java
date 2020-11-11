package com.LAM.GiftToMe.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.LAM.GiftToMe.Adapter.UserTweetsAdapter;
import com.LAM.GiftToMe.MainActivity;
import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.Twitter.TwitterRequests;
import com.LAM.GiftToMe.Twitter.VolleyListener;
import com.LAM.GiftToMe.UsefulClass.AddressUtils;
import com.LAM.GiftToMe.UsefulClass.NormalizeString;
import com.android.volley.VolleyError;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class NewGiftFragment extends Fragment {

    private Context mContext;

    private TextView nameForm, descriptionForm, addressForm;
    private String newGiftCategory, newGiftName, newGiftDescription, newGiftAddress;
    private ArrayList<Double> newGiftCoords;
    private Button addGift;
    private String activeCategory = "";
    //private Fragment fragment = new NewGiftFragment();

    private static final String TAG = "NewGiftFragmentTAG";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.new_gift_fragment, container, false);
        mContext = getActivity().getApplicationContext();

        nameForm = v.findViewById(R.id.name_gift);
        descriptionForm = v.findViewById(R.id.descr_gift);
        addressForm = v.findViewById(R.id.addr_gift);
        addGift = v.findViewById(R.id.add_gift);

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
        onClickChip(musicChip, listChip, "Music&Games");
        onClickChip(otherChip, listChip, "Other");

//        sportChip.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(activeCategory.equals("Sport")){
//                    activeCategory = "";
//                }
//                else{
//                    activeCategory = "Sport";
//                }
//                colorChip(listChip);
//                Log.i("checkedchecked", activeCategory + "");
//            }
//        });

        addGift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                newGiftName = nameForm.getText().toString();
                newGiftDescription = descriptionForm.getText().toString();
                newGiftAddress = addressForm.getText().toString();

                newGiftCategory = activeCategory;

                newGiftCoords = AddressUtils.getCoordsFromAddress(newGiftAddress, mContext);

                if(newGiftCoords == null) {
                    Toast.makeText(mContext, "Inserisci un indirizzo valido",Toast.LENGTH_SHORT).show();
                    return;
                }

                //Poichè la lunghezza massima di tutto il tweet è di 280 caratteri
                if(newGiftDescription.length() > 90){
                    Toast.makeText(mContext, "Inserisci una descrizione di al massimo 90 caratteri",Toast.LENGTH_SHORT).show();
                    return;
                }

                //Controllo se tutti i campi stono stati inseriti correttamente
                if (newGiftName.isEmpty() || newGiftDescription.isEmpty() || newGiftAddress.isEmpty() || newGiftCategory.equals("")){
                    Toast.makeText(mContext, "Inserisci tutti i campi",Toast.LENGTH_SHORT).show();
                    return;
                }

                String tweet = NormalizeString.normalizeTask(newGiftCoords.get(0), newGiftCoords.get(1), newGiftName, newGiftDescription, newGiftCategory, MainActivity.userName);

                TwitterRequests.postTweet(tweet, "", mContext, new VolleyListener() {
                    @Override
                    public void onError(VolleyError error) {
                        error.printStackTrace();

                    }

                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG,response);
                        nameForm.clearComposingText();
                        //UserTweetsAdapter.reloadFragment(fragment, getActivity());
                    }
                });

            }
        });

        return v;
    }

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
                Log.i("checkedchecked", activeCategory + "");
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
            case "Music&Games":
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





















