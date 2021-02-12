package com.LAM.GiftToMe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.LAM.GiftToMe.Fragment.ChatFragment;
import com.LAM.GiftToMe.Fragment.HomeFragment;
import com.LAM.GiftToMe.Fragment.MyGiftFragment;
import com.LAM.GiftToMe.Fragment.NewGiftFragment;
import com.LAM.GiftToMe.Fragment.ProfileFragment;
import com.LAM.GiftToMe.Fragment.UserTweetsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.twitter.sdk.android.core.TwitterSession;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivityTAG";
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private HomeFragment homeFragment;
    private NewGiftFragment newGiftFragment;
    private ChatFragment chatFragment;
    private ProfileFragment profileFragment;
    public static Fragment activeFragment;
    public BottomNavigationView bottomNavigationView;

    public SharedPreferences sharedPreferences, settingsSharedPreferences;
    private String sharedUsername,sharedToken,sharedTokenSecret,sharedUserId;

    public static Boolean isLogged = false;
    public static TwitterSession session;
    public static String userName,token,tokenSecret;
    public static Long userId;
    public static float radiusSearch;
    public static Boolean darkModeOn, darkMapOn;

    public static String homeFragmentTag, usersGiftListFragmentTag, chatFragmentTag, newGiftFragmentTag, profileFragmentTag, settingsFragmentTag, myGiftFragmentTag, replyChatFragmentTag, myReplyFragmentTag;

    private static final int TIME_INTERVAL = 2000; // tempo tra due "indietro" (millisecondi)
    private long mBackPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //SharedPreferences per Settings
        settingsSharedPreferences = getSharedPreferences("settingsPref", MODE_PRIVATE);
        darkModeOn = settingsSharedPreferences.getBoolean("darkMode", false);

        int nightModeFlags = getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (darkModeOn){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        //per togliere la barra di stato in alto
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        //primo fragment
        homeFragment = new HomeFragment();
        activeFragment = homeFragment;
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment, homeFragmentTag).commit();

        //tag per fragment
        homeFragmentTag = getResources().getString(R.string.home_fragment_tag);
        usersGiftListFragmentTag = getResources().getString(R.string.users_tweets_fragment_tag);
        newGiftFragmentTag = getResources().getString(R.string.newgift_fragment_tag);
        chatFragmentTag = getResources().getString(R.string.chat_fragment_tag);
        profileFragmentTag = getResources().getString(R.string.profile_fragment_tag);
        settingsFragmentTag = getResources().getString(R.string.settings_fragment_tag);
        myGiftFragmentTag = getResources().getString(R.string.mygift_fragment_tag);
        replyChatFragmentTag = getResources().getString(R.string.reply_chat_fragment_tag);
        myReplyFragmentTag = getResources().getString(R.string.myreply_fragment_tag);

        //stringhe shared preferences per login utente
        sharedUsername = getResources().getString(R.string.shared_preferences_user_name);
        sharedUserId = getResources().getString(R.string.shared_preferences_user_id);
        sharedToken = getResources().getString(R.string.shared_preferences_token);
        sharedTokenSecret = getResources().getString(R.string.shared_preferences_token_secret);

        Log.i(TAG, "username: " + sharedUsername);
        Log.i(TAG, "userId: " + sharedUserId);
        Log.i(TAG, "token: " + sharedToken);
        Log.i(TAG, "tokenSecret: " + sharedTokenSecret);

        //sharedPreferences Login
        sharedPreferences = getSharedPreferences("loginPref", MODE_PRIVATE);
        isLogged = sharedPreferences.getBoolean("isLogged", false); //ritorna false se nn esiste quella chiave

        //se sono loggato recupero shared preference
        if(isLogged){
            userName = sharedPreferences.getString(sharedUsername,null);
            userId = sharedPreferences.getLong(sharedUserId, 0);
            token = sharedPreferences.getString(sharedToken, null);
            tokenSecret = sharedPreferences.getString(sharedTokenSecret, null);
        }

        //Listener per bottomNavigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        radiusSearch = settingsSharedPreferences.getFloat("radiusSearch",100);
        darkMapOn = settingsSharedPreferences.getBoolean("darkMap", false);

    }

    //listener per bottomNavigation
    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
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
                    if (isLogged){
                        if(newGiftFragment == null) {
                            newGiftFragment = new NewGiftFragment();
                        }
                        selectedFragment = newGiftFragment;
                        fragmentTag = newGiftFragmentTag;
                    }else{
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.need_login_newg), Toast.LENGTH_LONG).show();
                        return false;
                    }
                    break;
                case R.id.nav_chat:
                    if (isLogged){
                        if(chatFragment == null) {
                            chatFragment = new ChatFragment(); //rimettere ChatFragment()
                        }
                        selectedFragment = chatFragment;
                        fragmentTag = chatFragmentTag; //rimettere il tag della chat
                    }else{
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.need_login_chat), Toast.LENGTH_LONG).show();
                        return false;
                    }
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

            Log.i(TAG, "activeFragment: " + activeFragment);

            return true;
        }
    };

    @Override
    public void onBackPressed() {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if(activeFragment.equals(fragmentManager.findFragmentByTag(homeFragmentTag)) || activeFragment.equals(fragmentManager.findFragmentByTag(usersGiftListFragmentTag))) {

            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
                finishAndRemoveTask();
                return;
            }
            else {
                Toast.makeText(this, getResources().getString(R.string.double_tap_to_exit_message), Toast.LENGTH_SHORT).show();
            }

            mBackPressed = System.currentTimeMillis();

        } else if(activeFragment.equals(fragmentManager.findFragmentByTag(replyChatFragmentTag))){

            fragmentTransaction.setCustomAnimations(R.anim.bottomtotop, R.anim.toptobottom);
            fragmentTransaction.replace(R.id.fragment_container, chatFragment, chatFragmentTag).commit();
            fragmentTransaction.addToBackStack(chatFragmentTag);
            activeFragment = chatFragment;
            bottomNavigationView.setSelectedItemId(R.id.nav_chat);

        } else if(activeFragment.equals(getSupportFragmentManager().findFragmentByTag(settingsFragmentTag)) || activeFragment.equals(getSupportFragmentManager().findFragmentByTag(myGiftFragmentTag)) || activeFragment.equals(getSupportFragmentManager().findFragmentByTag(myReplyFragmentTag))){

            fragmentTransaction.setCustomAnimations(R.anim.bottomtotop, R.anim.toptobottom);
            fragmentTransaction.replace(R.id.fragment_container, profileFragment, profileFragmentTag).commit();
            fragmentTransaction.addToBackStack(profileFragmentTag);
            activeFragment = profileFragment;
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        } else {

            fragmentTransaction.replace(R.id.fragment_container, homeFragment, homeFragmentTag).commit();
            fragmentTransaction.addToBackStack(homeFragmentTag);
            activeFragment = homeFragment;
            bottomNavigationView.setSelectedItemId(R.id.nav_home);

        }
    }

    //per permessi
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Toast.makeText(this, String.valueOf(requestCode), Toast.LENGTH_LONG).show();

        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE); //continua a chiedere sempre i permessi che non dai

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        UserTweetsFragment userTweetsFragment = new UserTweetsFragment();
                        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.setCustomAnimations(R.anim.righttoleft, R.anim.none);
                        fragmentTransaction.replace(R.id.fragment_container, userTweetsFragment, usersGiftListFragmentTag).commit();
                        fragmentTransaction.addToBackStack(usersGiftListFragmentTag);
                        activeFragment = userTweetsFragment;
                    }
                }, 500);

            } else {

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        HomeFragment homeFragment = new HomeFragment();
                        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.setCustomAnimations(R.anim.lefttoright, R.anim.none);
                        fragmentTransaction.replace(R.id.fragment_container, homeFragment, homeFragmentTag).commit();
                        fragmentTransaction.addToBackStack(homeFragmentTag);
                        activeFragment = homeFragment;
                    }
                }, 500);

            }

        }

    }

    //la richiamo per aggiustare il floating action button
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            if (activeFragment == homeFragment){
                ConstraintLayout.LayoutParams params1 = (ConstraintLayout.LayoutParams) HomeFragment.floatingActionButton.getLayoutParams();
                params1.verticalBias = 0.8f;
                HomeFragment.floatingActionButton.setLayoutParams(params1);
            }else if (activeFragment.equals(fragmentManager.findFragmentByTag(usersGiftListFragmentTag))){
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) UserTweetsFragment.fab.getLayoutParams();
                params.verticalBias = 0.7f;
                UserTweetsFragment.fab.setLayoutParams(params);
            }else if (activeFragment.equals(fragmentManager.findFragmentByTag(myGiftFragmentTag))){
                MyGiftFragment.recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
            }

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){

            if (activeFragment == homeFragment){
                ConstraintLayout.LayoutParams params1 = (ConstraintLayout.LayoutParams) HomeFragment.floatingActionButton.getLayoutParams();
                params1.verticalBias = 0.88f;
                HomeFragment.floatingActionButton.setLayoutParams(params1);
            }else if (activeFragment.equals(fragmentManager.findFragmentByTag(usersGiftListFragmentTag))){
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) UserTweetsFragment.fab.getLayoutParams();
                params.verticalBias = 0.88f;
                UserTweetsFragment.fab.setLayoutParams(params);
            }else if (activeFragment.equals(fragmentManager.findFragmentByTag(myGiftFragmentTag))){
                MyGiftFragment.recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            }

        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        //salvo le variabili nelle sharedPreferences per essere loggato quando riapro l'app
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("isLogged", isLogged).apply();

        Log.i(TAG, "isLogged: " + isLogged);

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
//        FragmentManager fragment = getSupportFragmentManager();
//        fragment.findFragmentByTag(profileFragmentTag).onActivityResult(requestCode, resultCode, data);
        if (activeFragment == fragment.findFragmentByTag(profileFragmentTag)){
            fragment.findFragmentByTag(profileFragmentTag).onActivityResult(requestCode, resultCode, data);
        }else if (activeFragment == fragment.findFragmentByTag(usersGiftListFragmentTag)){
            fragment.findFragmentByTag(usersGiftListFragmentTag).onActivityResult(requestCode, resultCode, data);
        }else if (activeFragment == fragment.findFragmentByTag(homeFragmentTag)){
            fragment.findFragmentByTag(homeFragmentTag).onActivityResult(requestCode, resultCode, data);
        }
    }
}