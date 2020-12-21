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
import android.widget.ScrollView;

import com.LAM.GiftToMe.Adapter.MyGiftTweetsAdapter;
import com.LAM.GiftToMe.MainActivity;
import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.Twitter.TwitterRequests;
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

    public static RecyclerView recyclerView;
    private Context mContext;
    //public static ScrollView scrollView;
    private ArrayList<MyGift> userGifts;
    private MyGiftTweetsAdapter myGiftTweetsAdapter;
    public static ArrayList<String> arrayActive;

    private boolean sportBool, electronicsBool, clothingBool, musicBool, otherBool;

    private static final int NUMBER_OF_TWEET = 200;
    private static final String TWEET_ARTICLE_HASHTAG = "#LAM_giftToMe_2020-article";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.mygift_fragment, container, false);

        mContext = getActivity().getApplicationContext();
        //scrollView = v.findViewById(R.id.scrollGift);
        recyclerView = v.findViewById(R.id.userTweets);
        userGifts = new ArrayList<>();

        TwitterRequests.getUserTweets(NUMBER_OF_TWEET, mContext, new VolleyListener() {
            @Override
            public void onResponse(String response) {

                String id,text,hashtag = "";

                try {
                    JSONArray jsonArray = new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        JSONObject entities = new JSONObject(jsonObject.getString(getResources().getString(R.string.json_entities)));
                        JSONArray jsonArrayHashtags = entities.getJSONArray(getResources().getString(R.string.json_hashtags));

                        for (int j = 0 ;j < jsonArrayHashtags.length(); j++)
                        {
                            JSONObject jsonObjectDates = jsonArrayHashtags.getJSONObject(j);
                            hashtag = jsonObjectDates.getString(getResources().getString(R.string.json_text));

                        }

                        text = jsonObject.getString(getResources().getString(R.string.json_full_text));
                        id = jsonObject.getString(getResources().getString(R.string.json_id));

                        if(text.contains(TWEET_ARTICLE_HASHTAG)){

                            MyGift userGift = new MyGift();
                            userGift.setTweetId(id);
                            String tweetWithoutHashtag = text.replace(TWEET_ARTICLE_HASHTAG,"");
                            String lat = getResources().getString(R.string.user_gift_parsing_lat);
                            String lon = getResources().getString(R.string.user_gift_parsing_lon);

                            JSONObject tweetWithoutHashtagJSON = new JSONObject(tweetWithoutHashtag);

                            userGift.setName(tweetWithoutHashtagJSON.getString(getResources().getString(R.string.user_gift_parsing_name)));
                            userGift.setCategory(String.valueOf(Html.fromHtml(tweetWithoutHashtagJSON.getString(getResources().getString(R.string.user_gift_parsing_category)))));  //HTml.fromHtml to escape & in Home&Office

                            userGift.setDescription(tweetWithoutHashtagJSON.getString(getResources().getString(R.string.user_gift_parsing_description)));
                            userGift.setLat(tweetWithoutHashtagJSON.getString(lat));
                            userGift.setLon(tweetWithoutHashtagJSON.getString(lon));
                            userGift.setAddress(AddressPermissionUtils.addressString(mContext, (Double)tweetWithoutHashtagJSON.get(lat), (Double)tweetWithoutHashtagJSON.get(lon)));
                            userGift.setIssuer(tweetWithoutHashtagJSON.getString(getResources().getString(R.string.json_issuer)));

                            userGifts.add(userGift);

                        }
                    }
                    for(MyGift c: userGifts){
                        Log.i("ciaociao", c.getDescription() + "");
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
                //Log.i("checkedchecked", arrayActive + "");
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
                //Log.i("checkedchecked", arrayActive + "");
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
                //Log.i("checkedchecked", arrayActive + "");
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
                //Log.i("checkedchecked", arrayActive + "");
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
//                activeFilter(otherBool, otherChip, "Other");
                myGiftTweetsAdapter.filter(arrayActive);
                //Log.i("checkedchecked", arrayActive + "");
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

//    private void activeFilter(boolean bool, final Chip chipSelected, final String category){
//
//        chipSelected.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i("checkedchecked", "Fuori sopra " + bool);
//                if (!bool){
//                    arrayActive.add(category);
//                    bool = true;
//                    Log.i("checkedchecked", "Dentro sopra " + bool);
//                    chipSelected.setChipBackgroundColorResource(R.color.colorChipSelected);
//                }else{
//                    arrayActive.remove(category);
//                    bool = false;
//                    Log.i("checkedchecked", "Dentro sotto " + bool);
//                    chipSelected.setChipBackgroundColorResource(R.color.ghost_white);
//                }
//                Log.i("checkedchecked", "Fuori sotto " + bool);
//            }
//        });
//
//    }

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
