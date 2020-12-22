package com.LAM.GiftToMe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.ListFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
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

    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private static final int FINE_LOCATION_ACCESS_REQUEST_CODE = 1001;
    private static final int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 1002;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private HomeFragment homeFragment;
    private NewGiftFragment newGiftFragment;
    private ChatFragment chatFragment; //rimettere ChatFragment
    private UserTweetsFragment userTweetsFragment;
    private ProfileFragment profileFragment;
    private ListFragment listFragment;
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

    public static String homeFragmentTag, usersGiftListFragmentTag, chatFragmentTag, newGiftFragmentTag, profileFragmentTag, conversationFragmentTag, settingsFragmentTag, myGiftFragmentTag, receiverChatFragmentTag;

    private static final int TIME_INTERVAL = 2000; // tempo che intercorre tra due back (millisecondi)
    private long mBackPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //SharedPreferences per Settings
        settingsSharedPreferences = getSharedPreferences("settingsPref", MODE_PRIVATE);
        darkModeOn = settingsSharedPreferences.getBoolean("darkMode", false);

        int nightModeFlags = getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (darkModeOn){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Log.i("darkdark", AppCompatDelegate.MODE_NIGHT_YES + " pippo"); //2
        }else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            Log.i("darkdark", AppCompatDelegate.MODE_NIGHT_NO + " pluto"); //1
        }

        super.onCreate(savedInstanceState);
        //per togliere la barra di stato in alto
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        //userTweetsFragment = new UserTweetsFragment();
        //primo fragment
        homeFragment = new HomeFragment();
        activeFragment = homeFragment;
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment, homeFragmentTag).commit();

        //get FragmentTags
        homeFragmentTag = getResources().getString(R.string.map_fragment_tag);
        usersGiftListFragmentTag = getResources().getString(R.string.users_tweets_fragment_tag);
        newGiftFragmentTag = getResources().getString(R.string.newgift_fragment_tag);
        chatFragmentTag = getResources().getString(R.string.chat_fragment_tag);
        profileFragmentTag = getResources().getString(R.string.profile_fragment_tag);
        conversationFragmentTag = getResources().getString(R.string.conversation_fragment_tag);
        settingsFragmentTag = getResources().getString(R.string.settings_fragment_tag);
        myGiftFragmentTag = getResources().getString(R.string.mygift_fragment_tag);
        receiverChatFragmentTag = getResources().getString(R.string.receiverchat_fragment_tag);

        sharedUsername = getResources().getString(R.string.shared_preferences_user_name);
        sharedUserId = getResources().getString(R.string.shared_preferences_user_id);
        sharedToken = getResources().getString(R.string.shared_preferences_token);
        sharedTokenSecret = getResources().getString(R.string.shared_preferences_token_secret);

        Log.i("LOGLOG", "eccolo: " + sharedUsername);
        Log.i("LOGLOG", "eccolo: " + sharedUserId);
        Log.i("LOGLOG", "eccolo: " + sharedToken);
        Log.i("LOGLOG", "eccolo: " + sharedTokenSecret);

        //sharedPreferences Login
        sharedPreferences = getSharedPreferences("loginPref", MODE_PRIVATE);
        isLogged = sharedPreferences.getBoolean("isLogged", false); //ritorna false se nn esiste quella chiave

        //recupero variabili salvate
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
                            chatFragment = new ChatFragment(); //rimettere ChatFragment()
                        }
                        selectedFragment = chatFragment;
                        fragmentTag = chatFragmentTag; //rimettere il tag della chat
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

            Log.i("activeactive", "" + activeFragment);

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
        }

        else if(activeFragment.equals(fragmentManager.findFragmentByTag(receiverChatFragmentTag))){
            fragmentTransaction.replace(R.id.fragment_container, chatFragment, chatFragmentTag).commit();
            fragmentTransaction.addToBackStack(chatFragmentTag);
            activeFragment = chatFragment;
            bottomNavigationView.setSelectedItemId(R.id.nav_chat);
        }

        else if(activeFragment.equals(getSupportFragmentManager().findFragmentByTag(settingsFragmentTag)) || activeFragment.equals(getSupportFragmentManager().findFragmentByTag(myGiftFragmentTag))){
            fragmentTransaction.setCustomAnimations(R.anim.bottomtotop, R.anim.toptobottom);
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

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Toast.makeText(this, String.valueOf(requestCode), Toast.LENGTH_LONG).show();

        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE); //continua a chiedere sempre i permessi che non dai
                UserTweetsFragment userTweetsFragment = new UserTweetsFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.righttoleft, R.anim.none);
                fragmentTransaction.replace(R.id.fragment_container, userTweetsFragment, usersGiftListFragmentTag).commit();
                fragmentTransaction.addToBackStack(usersGiftListFragmentTag);
                activeFragment = userTweetsFragment;

            } else {
                HomeFragment homeFragment = new HomeFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.lefttoright, R.anim.none);
                fragmentTransaction.replace(R.id.fragment_container, homeFragment, homeFragmentTag).commit();
                fragmentTransaction.addToBackStack(homeFragmentTag);
                activeFragment = homeFragment;
            }

            if (Build.VERSION.SDK_INT >= 29) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);

                } else {
                    Toast.makeText(this, "Non hai i permessi", Toast.LENGTH_SHORT).show();
                }
            } else {
                homeFragment.checkUserLocation();
            }
        }

//        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//
//                //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE); //continua a chiedere sempre i permessi che non dai
//                UserTweetsFragment userTweetsFragment = new UserTweetsFragment();
//                FragmentTransaction fragmentTransaction =  getSupportFragmentManager().beginTransaction();
//                fragmentTransaction.setCustomAnimations(R.anim.righttoleft, R.anim.none);
//                fragmentTransaction.replace(R.id.fragment_container, userTweetsFragment, usersGiftListFragmentTag).commit();
//                fragmentTransaction.addToBackStack(usersGiftListFragmentTag);
//                activeFragment = userTweetsFragment;
//
//            }else{
//                HomeFragment homeFragment = new HomeFragment();
//                FragmentTransaction fragmentTransaction =  getSupportFragmentManager().beginTransaction();
//                fragmentTransaction.setCustomAnimations(R.anim.lefttoright, R.anim.none);
//                fragmentTransaction.replace(R.id.fragment_container, homeFragment, homeFragmentTag).commit();
//                fragmentTransaction.addToBackStack(homeFragmentTag);
//                activeFragment = homeFragment;
//            }
//        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

//            if (activeFragment == homeFragment){
//                ConstraintLayout.LayoutParams params1 = (ConstraintLayout.LayoutParams) HomeFragment.floatingActionButton.getLayoutParams();
//                params1.verticalBias = 0.8f;
//                HomeFragment.floatingActionButton.setLayoutParams(params1);
//            }else if (activeFragment == HomeFragment.userTweetsFragment){
//                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) UserTweetsFragment.fab.getLayoutParams();
//                params.verticalBias = 0.8f;
//                UserTweetsFragment.fab.setLayoutParams(params);
//            }else if (activeFragment.equals(fragmentManager.findFragmentByTag(myGiftFragmentTag))){
//                MyGiftFragment.recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
//            }

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

//            if (activeFragment == homeFragment){
//                ConstraintLayout.LayoutParams params1 = (ConstraintLayout.LayoutParams) HomeFragment.floatingActionButton.getLayoutParams();
//                params1.verticalBias = 0.88f;
//                HomeFragment.floatingActionButton.setLayoutParams(params1);
//            }else if (activeFragment == HomeFragment.userTweetsFragment){
//                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) UserTweetsFragment.fab.getLayoutParams();
//                params.verticalBias = 0.88f;
//                UserTweetsFragment.fab.setLayoutParams(params);
//            }else if (activeFragment.equals(fragmentManager.findFragmentByTag(myGiftFragmentTag))){
//                MyGiftFragment.recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
//            }

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
//        FragmentManager fragment = getSupportFragmentManager();
//        fragment.findFragmentByTag(profileFragmentTag).onActivityResult(requestCode, resultCode, data);
        if (activeFragment == fragment.findFragmentByTag(profileFragmentTag)){
            fragment.findFragmentByTag(profileFragmentTag).onActivityResult(requestCode, resultCode, data);
        }else if (activeFragment == fragment.findFragmentByTag(usersGiftListFragmentTag)){
            fragment.findFragmentByTag(usersGiftListFragmentTag).onActivityResult(requestCode, resultCode, data);
        }
    }
}