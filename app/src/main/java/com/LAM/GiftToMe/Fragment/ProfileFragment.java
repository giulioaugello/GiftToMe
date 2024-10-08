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
import com.LAM.GiftToMe.MainActivity;
import com.LAM.GiftToMe.Picasso.CircleTransformation;
import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.Twitter.TwitterFunctions;
import com.LAM.GiftToMe.Twitter.VolleyListener;
import com.android.volley.VolleyError;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
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

    private static final String TAG = "ProfileFragmentTAG";
    private Context mContext;
    private View v;

    private TwitterLoginButton loginButton;
    private CardView logoutButton;
    private TextView text;
    private ImageView logo;
    private TextView twitterUsername;
    private ConstraintLayout constraintLayout, secondConstraint, constraintBeginning;
    private CardView myGift, settings, myReply;
    private LinearLayout linearSettings;
    private ImageView twitterBanner, twitterPhoto;

    public String userName;
    private String fcmToken;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        initializeTwitter(); //inizializza le cose di twitter

        v = inflater.inflate(R.layout.profile_fragment2, container, false);

        mContext = getActivity().getApplicationContext();

        loginButton = v.findViewById(R.id.login_button);
        logoutButton = v.findViewById(R.id.logout);

        text = v.findViewById(R.id.text);
        logo = v.findViewById(R.id.logo);

        constraintLayout = v.findViewById(R.id.constraint);
        secondConstraint = v.findViewById(R.id.secondConstraint);

        linearSettings = v.findViewById(R.id.linearSettings);
        myGift = v.findViewById(R.id.myGift);
        settings = v.findViewById(R.id.settings);
        myReply = v.findViewById(R.id.myReply);

        twitterPhoto = v.findViewById(R.id.twitterPhoto);
        twitterBanner = v.findViewById(R.id.twitterBanner);
        twitterUsername = v.findViewById(R.id.twitterUsername);
        constraintBeginning = v.findViewById(R.id.constraint_beginning);

        prefs = mContext.getSharedPreferences(getResources().getString(R.string.fcm_pref_name), Context.MODE_PRIVATE);
        fcmToken = prefs.getString(getResources().getString(R.string.fcm_token),null); //token FCM e DB

        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {

                MainActivity.session = TwitterCore.getInstance().getSessionManager().getActiveSession();
                MainActivity.userName = MainActivity.session.getUserName();
                MainActivity.userId = MainActivity.session.getUserId();
                MainActivity.token = MainActivity.session.getAuthToken().token;
                MainActivity.tokenSecret = MainActivity.session.getAuthToken().secret;

                Log.i(TAG,"Utente: " + MainActivity.session + ", " + MainActivity.userName + ", " + MainActivity.userId + ", " + MainActivity.token + ", " + MainActivity.tokenSecret);

                MainActivity.isLogged = true;

                if(fcmToken != null) {
                    //Il token viene salvato nel db se l'utente con questo token non esiste già
                    DBFirestore.checkIfExist(MainActivity.userName, fcmToken);
                }

                updateUI(true);
            }
            @Override
            public void failure(TwitterException exception) {
                Log.i(TAG,"ex: " + exception);

                updateUI(false);
                Toast.makeText(mContext, exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        if(MainActivity.isLogged) updateUI(true);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //rimuove il token se esco
                DBFirestore.removeToken(MainActivity.userName, fcmToken);

                TwitterCore.getInstance().getSessionManager().clearActiveSession();
                Toast.makeText(mContext, mContext.getResources().getString(R.string.log_out), Toast.LENGTH_LONG).show();
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

        myReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String myreplyFragmentTag = getResources().getString(R.string.myreply_fragment_tag);
                MyReplyFragment myReplyFragment = new MyReplyFragment();
                FragmentTransaction fragmentTransaction =  getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.bottomtotop, R.anim.none);
                fragmentTransaction.replace(R.id.fragment_container, myReplyFragment, myreplyFragmentTag).commit();
                fragmentTransaction.addToBackStack(myreplyFragmentTag);
                MainActivity.activeFragment = myReplyFragment;
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String settingsFragmentTag = getResources().getString(R.string.settings_fragment_tag);
                SettingsFragment settingsFragment = new SettingsFragment();
                FragmentTransaction fragmentTransaction =  getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.bottomtotop, R.anim.none);
                fragmentTransaction.replace(R.id.fragment_container, settingsFragment, settingsFragmentTag).commit();
                fragmentTransaction.addToBackStack(settingsFragmentTag);
                MainActivity.activeFragment = settingsFragment;
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

    //ritorna e imposta nome, immagine del profilo e banner di twitter
    private void setPageWithUserInfo(){
        TwitterFunctions.userInfo(mContext, MainActivity.userName, new VolleyListener() {

            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    //replace serve per prendere l'immagine con le dimensioni originali
                    Uri profileImgUri = Uri.parse((jsonObject.getString(getResources().getString(R.string.json_profile_image_url_https))).replace(getResources().getString(R.string.json_profile_image_normal),""));
                    Picasso.with(mContext).load(profileImgUri).transform(new CircleTransformation()).into(twitterPhoto);

                    Uri profileBannerUri = Uri.parse((jsonObject.getString(getResources().getString(R.string.json_profile_banner_url))));
                    Picasso.with(mContext).load(profileBannerUri).into(twitterBanner);

                    userName = String.valueOf(jsonObject.get(getResources().getString(R.string.user_gift_parsing_name)));
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
