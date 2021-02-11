package com.LAM.GiftToMe.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.LAM.GiftToMe.Adapter.MyReplyAdapter;
import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.Twitter.TwitterFunctions;
import com.LAM.GiftToMe.Twitter.VolleyListener;
import com.LAM.GiftToMe.UsefulClass.MyReply;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyReplyFragment extends Fragment {

    private RecyclerView recyclerView;
    private Context mContext;
    private String TWEET_REPLY_HASHTAG ;
    private static ArrayList<MyReply> myReplies;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.reply_fragment_layout, container, false);
        mContext = getActivity().getApplicationContext();

        TWEET_REPLY_HASHTAG = "#LAM_giftToMe_2020-reply";
        recyclerView = v.findViewById(R.id.my_reply);

        myReplies = new ArrayList<>();

        TwitterFunctions.userLoggedTweets(mContext, 50, new VolleyListener() {

            @Override
            public void onResponse(String response) {
                String id, replyId, text, hashtag = "";
                String idString = getResources().getString(R.string.json_id);
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
                        id = jsonObject.getString(idString);
                        replyId = jsonObject.getString(getResources().getString(R.string.json_in_reply_to_status_id));


                        if(text.contains(TWEET_REPLY_HASHTAG)){

                            MyReply myReply = new MyReply();
                            String tweetWithoutHashtag = text.replace(TWEET_REPLY_HASHTAG,"");

                            if(tweetWithoutHashtag.contains("@")){
                                Pattern regex = Pattern.compile("\\{(.*?)\\}");
                                Matcher matcher = regex.matcher(tweetWithoutHashtag);
                                if(matcher.find()) tweetWithoutHashtag = Objects.requireNonNull(matcher.group(0));
                            }

                            JSONObject tweetWithoutHashtagJSON = new JSONObject(tweetWithoutHashtag);

                            myReply.setTweetId(id);
                            myReply.setTweetReplyId(replyId);
                            myReply.setReplyId(tweetWithoutHashtagJSON.get(idString).toString());
                            myReply.setSender(tweetWithoutHashtagJSON.get(getResources().getString(R.string.json_sender)).toString());
                            myReply.setTargetId(tweetWithoutHashtagJSON.get(getResources().getString(R.string.json_target)).toString());
                            myReply.setReceiverName(tweetWithoutHashtagJSON.get(getResources().getString(R.string.json_receiver)).toString());
                            myReply.setMessage(tweetWithoutHashtagJSON.get(getResources().getString(R.string.json_message)).toString());

                            myReplies.add(myReply);
                        }

                    }

                    setupRecyclerView();


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onError(VolleyError error) {

            }
        });

        ImageView goBackButton = v.findViewById(R.id.go_back_profile_myreply);

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyReplyAdapter.goBack(mContext, getActivity());
            }
        });

        return v;
    }

    private void setupRecyclerView(){
        MyReplyAdapter myReplyAdapter = new MyReplyAdapter(mContext, myReplies, getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(myReplyAdapter);

    }
}
