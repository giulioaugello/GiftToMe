package com.LAM.GiftToMe.Fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.LAM.GiftToMe.Adapter.MyGiftTweetsAdapter;
import com.LAM.GiftToMe.MainActivity;
import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.Twitter.TwitterFunctions;
import com.LAM.GiftToMe.Twitter.VolleyListener;
import com.LAM.GiftToMe.UsefulClass.AddressPermissionUtils;
import com.LAM.GiftToMe.UsefulClass.MyGift;
import com.android.volley.VolleyError;
import com.google.android.material.chip.Chip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MyGiftFragment extends Fragment {

    private static final String TAG = "MyGiftFragmentTAG";
    public static RecyclerView recyclerView;
    private Context mContext;
    private ArrayList<MyGift> userGifts;
    private MyGiftTweetsAdapter myGiftTweetsAdapter;
    public static ArrayList<String> arrayActive;

    private boolean sportBool, electronicsBool, clothingBool, musicBool, otherBool;

    private static final int NUMBER_OF_TWEET = 100;
    private static final String TWEET_ARTICLE_HASHTAG = "#LAM_giftToMe_2020-article";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.mygift_fragment, container, false);

        mContext = getActivity().getApplicationContext();
        recyclerView = v.findViewById(R.id.userTweets);
        userGifts = new ArrayList<>();

        TwitterFunctions.userLoggedTweets(mContext, NUMBER_OF_TWEET, new VolleyListener() {
            @Override
            public void onResponse(String response) {

                String id, text;

                try {
                    JSONArray jsonArray = new JSONArray(response);

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        text = jsonObject.getString(getResources().getString(R.string.json_full_text));
                        id = jsonObject.getString(getResources().getString(R.string.json_id));

                        if(text.contains(TWEET_ARTICLE_HASHTAG)){

                            //creo i vari regali
                            MyGift myGift = new MyGift();

                            String jsonParameter = text.replace(TWEET_ARTICLE_HASHTAG,"");
                            JSONObject tweetJSON = new JSONObject(jsonParameter);
                            String lat = getResources().getString(R.string.user_gift_parsing_lat);
                            String lon = getResources().getString(R.string.user_gift_parsing_lon);

                            myGift.setTweetId(id);
                            myGift.setName(tweetJSON.getString(getResources().getString(R.string.user_gift_parsing_name)));
                            myGift.setDescription(tweetJSON.getString(getResources().getString(R.string.user_gift_parsing_description)));
                            myGift.setCategory(String.valueOf(Html.fromHtml(tweetJSON.getString(getResources().getString(R.string.user_gift_parsing_category)))));
                            myGift.setLat(tweetJSON.getString(lat));
                            myGift.setLon(tweetJSON.getString(lon));
                            myGift.setAddress(AddressPermissionUtils.addressString(mContext, (Double)tweetJSON.get(lat), (Double)tweetJSON.get(lon)));
                            myGift.setIssuer(tweetJSON.getString(getResources().getString(R.string.json_issuer)));

                            userGifts.add(myGift);

                        }
                    }

                    setupRecyclerView(userGifts);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(VolleyError error) {
                error.printStackTrace();
            }
        });

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

        arrayActive = new ArrayList<>();

        sportChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!sportBool){
                    arrayActive.add("Sport");
                    sportBool = true;
                    sportChip.setChipBackgroundColorResource(R.color.colorChipSelected);
                }else{
                    arrayActive.remove("Sport");
                    sportBool = false;
                    sportChip.setChipBackgroundColorResource(R.color.ghost_white);
                }
                myGiftTweetsAdapter.filter(arrayActive);
                Log.i(TAG, arrayActive + "");
            }
        });
        electronicChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!electronicsBool){
                    arrayActive.add("Electronics");
                    electronicsBool = true;
                    electronicChip.setChipBackgroundColorResource(R.color.colorChipSelected);
                }else{
                    arrayActive.remove("Electronics");
                    electronicsBool = false;
                    electronicChip.setChipBackgroundColorResource(R.color.ghost_white);
                }
                myGiftTweetsAdapter.filter(arrayActive);
                Log.i(TAG, arrayActive + "");
            }
        });
        clothingChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!clothingBool){
                    arrayActive.add("Clothing");
                    clothingBool = true;
                    clothingChip.setChipBackgroundColorResource(R.color.colorChipSelected);
                }else{
                    arrayActive.remove("Clothing");
                    clothingBool = false;
                    clothingChip.setChipBackgroundColorResource(R.color.ghost_white);
                }
                myGiftTweetsAdapter.filter(arrayActive);
                Log.i(TAG, arrayActive + "");
            }
        });
        musicChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!musicBool){
                    arrayActive.add("Music&Books");
                    musicBool = true;
                    musicChip.setChipBackgroundColorResource(R.color.colorChipSelected);
                }else{
                    arrayActive.remove("Music&Books");
                    musicBool = false;
                    musicChip.setChipBackgroundColorResource(R.color.ghost_white);
                }
                myGiftTweetsAdapter.filter(arrayActive);
                Log.i(TAG, arrayActive + "");
            }
        });
        otherChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!otherBool){
                    arrayActive.add("Other");
                    otherBool = true;
                    otherChip.setChipBackgroundColorResource(R.color.colorChipSelected);
                }else{
                    arrayActive.remove("Other");
                    otherBool = false;
                    otherChip.setChipBackgroundColorResource(R.color.ghost_white);
                }
                myGiftTweetsAdapter.filter(arrayActive);
                Log.i(TAG, arrayActive + "");
            }
        });


        ImageView goBackButton = v.findViewById(R.id.go_back_profile_mygift);

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String profileFragmentTag = getResources().getString(R.string.profile_fragment_tag);
                ProfileFragment profileFragment = new ProfileFragment();
                FragmentTransaction fragmentTransaction =  getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.bottomtotop, R.anim.toptobottom);
                fragmentTransaction.replace(R.id.fragment_container, profileFragment, profileFragmentTag).commit();
                fragmentTransaction.addToBackStack(profileFragmentTag);
                MainActivity.activeFragment = profileFragment;
            }
        });

        return v;
    }

    private void setupRecyclerView(ArrayList<MyGift> uGiftsList){
        myGiftTweetsAdapter = new MyGiftTweetsAdapter(mContext, uGiftsList, getActivity(),this);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape (orizzontale)
            recyclerView.setLayoutManager(new GridLayoutManager(mContext, 3));
        } else {
            // In portrait (verticale)
            recyclerView.setLayoutManager(new GridLayoutManager(mContext, 2));
        }

        recyclerView.setAdapter(myGiftTweetsAdapter);
    }

}
