package com.LAM.GiftToMe.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.LAM.GiftToMe.R;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.Callback;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    private Context mContext;
    private TwitterLoginButton loginButton;
    private CardView logoutButton;
    private View v;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        initializeTwitter();

        v = inflater.inflate(R.layout.profile_fragment, container, false);

        mContext = getActivity().getApplicationContext();

        loginButton = v.findViewById(R.id.login_button);
        logoutButton = v.findViewById(R.id.logout);

//        CardView cardView = v.findViewById(R.id.myGift);
//
//        cardView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(mContext, "Ciao", Toast.LENGTH_LONG).show();
//            }
//        });

        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                updateUI(true);
                TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
                TwitterAuthToken authToken = session.getAuthToken();
                Toast.makeText(mContext, "Logged! My toke is: "+authToken.token, Toast.LENGTH_LONG).show();
            }
            @Override
            public void failure(TwitterException exception) {
                updateUI(false);
                Toast.makeText(mContext, exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TwitterCore.getInstance().getSessionManager().clearActiveSession();
                Toast.makeText(mContext, "Logged out!", Toast.LENGTH_LONG).show();
                updateUI(false);
            }
        });


        return v;
    }

    private void initializeTwitter() {
        TwitterConfig config = new TwitterConfig.Builder(getActivity().getApplicationContext())
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(getString(R.string.CONSUMER_KEY), getString(R.string.CONSUMER_SECRET)))
                .debug(true)
                .build();
        Twitter.initialize(config);
    }

    private void updateUI(boolean signedIn) {
        if (signedIn) {
            loginButton.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
        } else {
            loginButton.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);

    }
}
