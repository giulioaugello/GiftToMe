package com.LAM.GiftToMe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.ListFragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.LAM.GiftToMe.Fragment.ChatFragment;
import com.LAM.GiftToMe.Fragment.HomeFragment;
import com.LAM.GiftToMe.Fragment.NewGiftFragment;
import com.LAM.GiftToMe.Fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.twitter.sdk.android.core.TwitterSession;

import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;

public class MainActivity extends AppCompatActivity {

    private HomeFragment homeFragment;
    private NewGiftFragment newGiftFragment;
    private ChatFragment chatFragment;
    private ProfileFragment profileFragment;
    private ListFragment listFragment;
    public static Fragment activeFragment;
    public BottomNavigationView bottomNavigationView;

    public SharedPreferences sharedPreferences;
    private String sharedUsername,sharedToken,sharedTokenSecret,sharedUserId;

    public static Boolean isLogged = false;
    public static TwitterSession session;
    public static String userName,token,tokenSecret;
    public static Long userId;

    public static String homeFragmentTag, listFragmentTag, messagesFragmentTag, newGiftFragmentTag, profileFragmentTag, conversationFragmentTag, settingsFragmentTag, myGiftFragmentTag;

    private static final int TIME_INTERVAL = 2000; // tempo che intercorre tra due back (millisecondi)
    private long mBackPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //per togliere la barra di stato in alto
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        //primo fragment
        homeFragment = new HomeFragment();
        activeFragment = homeFragment;
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, homeFragment, homeFragmentTag).commit();

        //get FragmentTags
        homeFragmentTag = getResources().getString(R.string.map_fragment_tag);
        listFragmentTag = getResources().getString(R.string.list_fragment_tag);
        newGiftFragmentTag = getResources().getString(R.string.newgift_fragment_tag);
        messagesFragmentTag = getResources().getString(R.string.messages_fragment_tag);
        profileFragmentTag = getResources().getString(R.string.profile_fragment_tag);
        conversationFragmentTag = getResources().getString(R.string.conversation_fragment_tag);
        settingsFragmentTag = getResources().getString(R.string.settings_fragment_tag);
        myGiftFragmentTag = getResources().getString(R.string.mygift_fragment_tag);

        sharedUsername = getResources().getString(R.string.shared_preferences_user_name);
        sharedUserId = getResources().getString(R.string.shared_preferences_user_id);
        sharedToken = getResources().getString(R.string.shared_preferences_token);
        sharedTokenSecret = getResources().getString(R.string.shared_preferences_token_secret);

        Log.i("LOGLOG", "eccolo: " + sharedUsername);
        Log.i("LOGLOG", "eccolo: " + sharedUserId);
        Log.i("LOGLOG", "eccolo: " + sharedToken);
        Log.i("LOGLOG", "eccolo: " + sharedTokenSecret);

        sharedPreferences = getSharedPreferences("loginPref", MODE_PRIVATE);
        isLogged = sharedPreferences.getBoolean("isLogged", false); //ritorna false se nn esiste quella chiave

        //se l'utente è loggato recuperiamo le informazioni
        if(isLogged){
            userName = sharedPreferences.getString(sharedUsername,null);
            userId = sharedPreferences.getLong(sharedUserId, 0);
            token = sharedPreferences.getString(sharedToken, null);
            tokenSecret = sharedPreferences.getString(sharedTokenSecret, null);
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = homeFragment;
            String fragmentTag = "";

            switch (item.getItemId()){
                case R.id.nav_home:
                    if(homeFragment == null) {
                        homeFragment = new HomeFragment();
                    }
                    selectedFragment = homeFragment;
                    fragmentTag = homeFragmentTag;
                    break;
                case R.id.nav_newGift:
//                    if (isLogged){
                        if(newGiftFragment == null) {
                            newGiftFragment = new NewGiftFragment();
                        }
                        selectedFragment = newGiftFragment;
                        fragmentTag = newGiftFragmentTag;
//                    }else{
//                        Toast.makeText(getApplicationContext(), "Devi accedere per poter aggiungere un regalo",Toast.LENGTH_LONG).show();
//                        return false;
//                    }
                    break;
                case R.id.nav_chat:
//                    if (isLogged){
                        if(chatFragment == null) {
                            chatFragment = new ChatFragment();
                        }
                        selectedFragment = chatFragment;
                        fragmentTag = messagesFragmentTag;
//                    }else{
//                        Toast.makeText(getApplicationContext(), "Devi accedere per poter entrare in questa sezione",Toast.LENGTH_LONG).show();
//                        return false;
//                    }
                    break;
                case R.id.nav_profile:
                    if(profileFragment == null) {
                        profileFragment = new ProfileFragment();
                    }
                    selectedFragment = profileFragment;
                    fragmentTag = profileFragmentTag;
                    break;
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment, fragmentTag).commit();
            getSupportFragmentManager().beginTransaction().addToBackStack(fragmentTag);

            activeFragment = selectedFragment;

            return true;
        }
    };

    @Override
    public void onBackPressed() {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if(activeFragment.equals(fragmentManager.findFragmentByTag(homeFragmentTag))) {
            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
                finishAndRemoveTask();
                return;
            }
            else {
                Toast.makeText(this, getResources().getString(R.string.double_tap_to_exit_message), Toast.LENGTH_SHORT).show();
            }

            mBackPressed = System.currentTimeMillis();
        }

        else if(activeFragment.equals(fragmentManager.findFragmentByTag(conversationFragmentTag))){
            fragmentTransaction.replace(R.id.fragment_container, chatFragment, messagesFragmentTag).commit();
            fragmentTransaction.addToBackStack(messagesFragmentTag);
            activeFragment = chatFragment;
            bottomNavigationView.setSelectedItemId(R.id.nav_chat);
        }

        else if(activeFragment.equals(getSupportFragmentManager().findFragmentByTag(settingsFragmentTag)) || activeFragment.equals(getSupportFragmentManager().findFragmentByTag(myGiftFragmentTag))){
            fragmentTransaction.setCustomAnimations(R.anim.toptobottom, R.anim.toptobottom);
            fragmentTransaction.replace(R.id.fragment_container, profileFragment, profileFragmentTag).commit();
            fragmentTransaction.addToBackStack(profileFragmentTag);
            activeFragment = profileFragment;
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        }

        else {
            fragmentTransaction.replace(R.id.fragment_container, homeFragment, homeFragmentTag).commit();
            fragmentTransaction.addToBackStack(homeFragmentTag);
            activeFragment = homeFragment;
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Viene salvata la sessione utente per essere ancora loggato quando riapre l'app
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("isLogged", isLogged).apply();

        Log.i("loggedlogged", isLogged + "");

        if(isLogged){
            editor.putString(sharedUsername, userName).apply();
            editor.putLong(sharedUserId, userId).apply();
            editor.putString(sharedToken, token).apply();
            editor.putString(sharedTokenSecret, tokenSecret).apply();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FragmentManager fragment = getSupportFragmentManager();
        fragment.findFragmentByTag(profileFragmentTag).onActivityResult(requestCode, resultCode, data);
    }
}