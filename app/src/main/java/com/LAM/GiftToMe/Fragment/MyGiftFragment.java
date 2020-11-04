package com.LAM.GiftToMe.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.LAM.GiftToMe.Adapter.UserTweetsAdapter;
import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.Twitter.TwitterRequests;
import com.LAM.GiftToMe.Twitter.VolleyListener;
import com.LAM.GiftToMe.UsefulClass.MyGift;
import com.android.volley.VolleyError;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

            }

            @Override
            public void onError(VolleyError error) {
                error.printStackTrace();
            }
        });

        return v;
    }

}
