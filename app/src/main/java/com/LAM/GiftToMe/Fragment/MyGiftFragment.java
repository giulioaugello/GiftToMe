package com.LAM.GiftToMe.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.LAM.GiftToMe.Adapter.UserTweetsAdapter;
import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.Twitter.TwitterRequests;
import com.LAM.GiftToMe.Twitter.VolleyListener;
import com.LAM.GiftToMe.UsefulClass.MyGift;
import com.airbnb.lottie.utils.Utils;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MyGiftFragment extends Fragment {

    private RecyclerView recyclerView;
    private Context mContext;
    private ScrollView scrollView;
    private ArrayList<MyGift> userGifts;
    private String activeFilter = "";
    private UserTweetsAdapter userTweetsAdapter;

    private static final int NUMBER_OF_TWEET = 200;
    private static final String TAG = "ListFragmentTAG";
    private static final String TWEET_ARTICLE_HASHTAG = "#LAM_giftToMe_2020-article";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.mygift_fragment, container, false);

        mContext = getActivity().getApplicationContext();
        scrollView = v.findViewById(R.id.scrollGift);
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
                            //userGift.setAddress(Utils.getCompleteAddressString(mContext,(Double)tweetWithoutHashtagJSON.get(lat),(Double)tweetWithoutHashtagJSON.get(lon)));
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

        return v;
    }

    private void setupRecyclerView(ArrayList<MyGift> uGiftsList){
        userTweetsAdapter = new UserTweetsAdapter(mContext, uGiftsList, getActivity(),this);
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, 2));
        recyclerView.setAdapter(userTweetsAdapter);
    }

    private void setFilter(String category){
        if(activeFilter.equals(category)){
            activeFilter = "";
            userTweetsAdapter.filter("");

        }
        else{
            activeFilter = category;
            userTweetsAdapter.filter(category);
        }
    }


}
