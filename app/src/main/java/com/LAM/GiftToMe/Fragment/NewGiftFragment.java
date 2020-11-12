package com.LAM.GiftToMe.Fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class NewGiftFragment extends Fragment {

    private Context mContext;
    private Fragment fragment;

    private ArrayList<Double> newGiftCoords;
    private Button addGift;
    private String activeCategory = "";
    private TextView nameForm, descriptionForm, addressForm;

    private TextView recapCategory, recapName, recapDescription, recapAddress;
    private ImageView recapImg;
    private Button back, uploadGift;

    private LinearLayout linearLayout;



    private static final String TAG = "NewGiftFragmentTAG";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.new_gift_fragment, container, false);

        mContext = getActivity().getApplicationContext();
        linearLayout = v.findViewById(R.id.linear_edit);

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

                //Controllo se tutti i campi stono stati inseriti correttamente
                Log.i("formform", "Nome: " + nameForm.getText().toString());
                if (nameForm.getText().toString().equals("") || descriptionForm.getText().toString().equals("") || addressForm.getText().toString().equals("")){
                    Toast.makeText(mContext, "Inserisci tutti i campi", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Poichè la lunghezza massima di tutto il tweet è di 280 caratteri
                Log.i("formform", "Lunghezza descrizione: " + descriptionForm.getText().length());
                if(descriptionForm.getText().length() > 90){
                    Toast.makeText(mContext, "Inserisci una descrizione di al massimo 90 caratteri", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.i("formform", "Indirizzo: " + AddressUtils.getCoordsFromAddress(addressForm.getText().toString(), mContext));
                if(AddressUtils.getCoordsFromAddress(addressForm.getText().toString(), mContext) == null) {
                    Toast.makeText(mContext, "Inserisci un indirizzo valido", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.i("formform", "Categoria: " + activeCategory);
                if (activeCategory.equals("")){
                    Toast.makeText(mContext, "Spunta una categoria", Toast.LENGTH_SHORT).show();
                    return;
                }

                showDialogRecap();

            }
        });

        Log.i("categorycategory", activeCategory);

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activeCategory = "";
    }

    private void showDialogRecap(){

        final Dialog dialog = new Dialog(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.recap_newgift_dialog,null);
        dialog.setCancelable(true);
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        recapCategory = view.findViewById(R.id.recap_category);
        recapName = view.findViewById(R.id.recap_name);
        recapDescription = view.findViewById(R.id.recap_descr);
        recapAddress = view.findViewById(R.id.recap_addr);
        recapImg = view.findViewById(R.id.recap_img);
        back = view.findViewById(R.id.back_button);
        uploadGift = view.findViewById(R.id.post_tweet);

        recapCategory.setText(activeCategory);
        recapName.setText(nameForm.getText().toString());
        recapDescription.setText(descriptionForm.getText().toString());
        recapAddress.setText(addressForm.getText().toString());

        UserTweetsAdapter.changeCategoryImage(activeCategory, recapImg);
        newGiftCoords = AddressUtils.getCoordsFromAddress(addressForm.getText().toString(), mContext);

        dialog.show();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        uploadGift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String tweet = NormalizeString.normalizeTask(newGiftCoords.get(0), newGiftCoords.get(1), nameForm.getText().toString(), descriptionForm.getText().toString(), activeCategory, MainActivity.userName);

                TwitterRequests.postTweet(tweet, "", mContext, new VolleyListener() {
                    @Override
                    public void onError(VolleyError error) {
                        error.printStackTrace();

                    }

                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG,response);
                        dialog.dismiss();
                        fragment = getActivity().getSupportFragmentManager().findFragmentByTag(MainActivity.newGiftFragmentTag);
                        UserTweetsAdapter.reloadFragment(fragment, getActivity());
                        clearForm(linearLayout);
                    }
                });

            }
        });
    }

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





















