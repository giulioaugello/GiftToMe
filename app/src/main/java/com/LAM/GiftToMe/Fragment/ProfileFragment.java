package com.LAM.GiftToMe.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.LAM.GiftToMe.FCMFirebase.DBFirestore;
import com.LAM.GiftToMe.FCMFirebase.MyFirebaseMessagingService;
import com.LAM.GiftToMe.MainActivity;
import com.LAM.GiftToMe.Picasso.CircleTransformation;
import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.Twitter.TwitterRequests;
import com.LAM.GiftToMe.Twitter.VolleyListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.Callback;


import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class ProfileFragment extends Fragment {

    private Context mContext;
    private View v;

    private TwitterLoginButton loginButton;
    private CardView logoutButton;
    private TextView text;
    private ImageView logo;
    private TextView twitterUsername;
    private ConstraintLayout constraintLayout, secondConstraint, constraintBeginning;
    private CardView myGift;
    private LinearLayout linearSettings;
    private ImageView twitterBanner, twitterPhoto;

    public String userName;
    private String fcmToken;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        initializeTwitter();

        v = inflater.inflate(R.layout.profile_fragment, container, false);

        mContext = getActivity().getApplicationContext();

        loginButton = v.findViewById(R.id.login_button);
        logoutButton = v.findViewById(R.id.logout);
        text = v.findViewById(R.id.text);
        logo = v.findViewById(R.id.logo);
        twitterUsername = v.findViewById(R.id.twitterUsername);
        constraintLayout = v.findViewById(R.id.constraint);
        secondConstraint = v.findViewById(R.id.secondConstraint);
        myGift = v.findViewById(R.id.myGift);
        linearSettings = v.findViewById(R.id.linearSettings);
        twitterPhoto = v.findViewById(R.id.twitterPhoto);
        twitterBanner = v.findViewById(R.id.twitterBanner);
        constraintBeginning = v.findViewById(R.id.constraint_beginning);

        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {

                MainActivity.session = TwitterCore.getInstance().getSessionManager().getActiveSession();
                MainActivity.userName = MainActivity.session.getUserName();
                MainActivity.userId = MainActivity.session.getUserId();
                MainActivity.token = MainActivity.session.getAuthToken().token;
                MainActivity.tokenSecret = MainActivity.session.getAuthToken().secret;

                MainActivity.isLogged = true;

                SharedPreferences prefs = mContext.getSharedPreferences(getResources().getString(R.string.fcm_pref_name), Context.MODE_PRIVATE);
                fcmToken = prefs.getString(getResources().getString(R.string.fcm_token),null);
                Log.i("FCMTAG", "bello " + fcmToken);

                if(fcmToken != null) {
                    //Il token viene salvato nel db se non esiste già l'utente con questo token
                    Log.i("FCMTAG", "bello");
                    DBFirestore.checkIfExist(MainActivity.userName, fcmToken);
                }

//                Log.i("LOGLOGLO","Username: " + MainActivity.userName);
//                Log.i("LOGLOGLO","Userid: " + MainActivity.userId);
//                Log.i("LOGLOGLO","token: " + MainActivity.token);
//                Log.i("LOGLOGLO","tokenSecret: " + MainActivity.tokenSecret);

                updateUI(true);
            }
            @Override
            public void failure(TwitterException exception) {
                Log.i("LOGLOGLO","exexex" + exception);

                updateUI(false);
                Toast.makeText(mContext, exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        if(MainActivity.isLogged) updateUI(true);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TwitterCore.getInstance().getSessionManager().clearActiveSession();
                Toast.makeText(mContext, "Logged out!", Toast.LENGTH_LONG).show();
                MainActivity.isLogged = false;
                MainActivity.session = null;
                MainActivity.userName = null;
                MainActivity.userId = null;
                MainActivity.token = null;
                MainActivity.tokenSecret = null;
                updateUI(false);
            }
        });

        myGift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String myGiftFragmentTag = getResources().getString(R.string.mygift_fragment_tag);
                MyGiftFragment myGiftFragment = new MyGiftFragment();
                FragmentTransaction fragmentTransaction =  getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.bottomtotop, R.anim.none);
                fragmentTransaction.replace(R.id.fragment_container, myGiftFragment, myGiftFragmentTag).commit();
                fragmentTransaction.addToBackStack(myGiftFragmentTag);
                MainActivity.activeFragment = myGiftFragment;
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
            setPageWithUserInfo();
            Log.i("wewewe","wewe");
            constraintBeginning.setVisibility(View.GONE);
            constraintLayout.setVisibility(View.VISIBLE);
            secondConstraint.setVisibility(View.VISIBLE);
        } else {
            constraintLayout.setVisibility(View.GONE);
            secondConstraint.setVisibility(View.GONE);
            constraintBeginning.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

    private void setPageWithUserInfo(){
        TwitterRequests.getUserInfo(mContext, MainActivity.userName, new VolleyListener() {

            @Override
            public void onResponse(String response) {

                try {
                    JSONObject userObject = new JSONObject(response);

                    //replace serve per prendere l'immagine con le dimensioni (width e height) originali
                    Uri profileImgUri = Uri.parse((userObject.getString(getResources().getString(R.string.json_profile_image_url_https))).replace(getResources().getString(R.string.json_profile_image_normal),""));
                    Picasso.with(mContext).load(profileImgUri).transform(new CircleTransformation()).into(twitterPhoto);

                    Uri profileBannerUri = Uri.parse((userObject.getString(getResources().getString(R.string.json_profile_banner_url))));
                    Picasso.with(mContext).load(profileBannerUri).into(twitterBanner);

                    userName = String.valueOf(userObject.get(getResources().getString(R.string.user_gift_parsing_name)));
                    twitterUsername.setText(userName);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VolleyError error) {
                error.printStackTrace();
            }
        });
    }

}
