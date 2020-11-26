package com.LAM.GiftToMe.Fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.LAM.GiftToMe.Adapter.MyGiftTweetsAdapter;
import com.LAM.GiftToMe.Adapter.UserTweetsAdapter;
import com.LAM.GiftToMe.MainActivity;
import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.Twitter.TwitterRequests;
import com.LAM.GiftToMe.Twitter.VolleyListener;
import com.LAM.GiftToMe.UsefulClass.AddressPermissionUtils;
import com.LAM.GiftToMe.UsefulClass.UsersGift;
import com.android.volley.VolleyError;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mapsforge.map.rendertheme.renderinstruction.Line;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class UserTweetsFragment extends Fragment {

    public RecyclerView recyclerViewSport, recyclerViewElectronics, recyclerViewClothing, recyclerViewMusic, recyclerViewOther;
    private Context mContext;
    private ScrollView scrollView;
    private EditText searchLocation;
    private ImageView searchButtonLocation, turnOnGps;
    private LinearLayout linearGpsOff;
    private ArrayList<UsersGift> sportA, electronicsA, clothingA, musicA, otherA;
    private UserTweetsAdapter userTweetsAdapter;
    public static FloatingActionButton fab;

    private Fragment fragment;

    private static final String TWEET_ARTICLE_HASHTAG = "#LAM_giftToMe_2020-article";
    private ArrayList<UsersGift> arrayUsersGifts;
    private double[] coordMarker = new double[2];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.users_tweets_fragment, container, false);
        fragment = getActivity().getSupportFragmentManager().findFragmentByTag(MainActivity.usersGiftListFragmentTag);

        mContext = getActivity().getApplicationContext();

        searchLocation = v.findViewById(R.id.add_position_list);
        searchButtonLocation = v.findViewById(R.id.search_position_list);

        recyclerViewSport = v.findViewById(R.id.user_tweets_sport);
        recyclerViewElectronics = v.findViewById(R.id.user_tweets_electronics);
        recyclerViewClothing = v.findViewById(R.id.user_tweets_clothing);
        recyclerViewMusic = v.findViewById(R.id.user_tweets_music);
        recyclerViewOther = v.findViewById(R.id.user_tweets_other);

        sportA = new ArrayList<>();
        electronicsA = new ArrayList<>();
        clothingA = new ArrayList<>();
        musicA = new ArrayList<>();
        otherA = new ArrayList<>();

        linearGpsOff = v.findViewById(R.id.linear_gps_off);
        turnOnGps = v.findViewById(R.id.turn_on_gps);
        scrollView = v.findViewById(R.id.scroll_tweets_list);

        fab = v.findViewById(R.id.return_to_map);

        //altezza floating action bar
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) fab.getLayoutParams();
            params.verticalBias = 0.8f;
            fab.setLayoutParams(params);
        }else if (orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT){
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) fab.getLayoutParams();
            params.verticalBias = 0.88f;
            fab.setLayoutParams(params);
        }

        //nascondo la scritta del gps spento e metto margine alla scrollview
        final float density = mContext.getResources().getDisplayMetrics().density;
        if (HomeFragment.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            linearGpsOff.setVisibility(View.GONE);

            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            int top = (int) (25 * density);
            params.setMargins(0, top, 0, 0);
            scrollView.setLayoutParams(params);
        }

        turnOnGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableGPS();
            }
        });

        //chiedo i permessi e cambio fragment tramite la mainactivity
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                    AddressPermissionUtils.requestPermissionsIfNecessary(new String[]{

                            Manifest.permission.ACCESS_FINE_LOCATION, //serve per i permessi della posizione

                            Manifest.permission.WRITE_EXTERNAL_STORAGE //mi serve per far visualizzzare la mappa
                    }, mContext, v);

                }else{

                    HomeFragment homeFragment = new HomeFragment();
                    FragmentTransaction fragmentTransaction =  getActivity().getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.anim.lefttoright, R.anim.none);
                    fragmentTransaction.replace(R.id.fragment_container, homeFragment, MainActivity.homeFragmentTag).commit();
                    fragmentTransaction.addToBackStack(MainActivity.homeFragmentTag);
                    MainActivity.activeFragment = homeFragment;

                }

            }
        });


        TwitterRequests.searchTweets(mContext, TWEET_ARTICLE_HASHTAG, new VolleyListener() {

            @Override
            public void onResponse(String response) {
                arrayUsersGifts = new ArrayList<>();

                String id, text, hashtag = "";
                try {
                    JSONObject jObj = new JSONObject(response);
                    JSONArray jsonArray = (JSONArray) jObj.get(getResources().getString(R.string.json_statuses));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        String idString = getResources().getString(R.string.json_id);

                        id = jsonObject.getString(idString);
                        text = jsonObject.getString(getResources().getString(R.string.json_full_text));

                        UsersGift userGift = new UsersGift();
                        userGift.setTweetId(id);

                        String tweetWithoutHashtag = text.replace(TWEET_ARTICLE_HASHTAG, "");
                        String lat = getResources().getString(R.string.user_gift_parsing_lat);
                        String lon = getResources().getString(R.string.user_gift_parsing_lon);

                        JSONObject tweetWithoutHashtagJSON  = null;

                        try{
                            tweetWithoutHashtagJSON = new JSONObject(tweetWithoutHashtag);
                        }
                        catch (JSONException e){
//                            e.printStackTrace();
                            continue;
                        }

                        userGift.setGiftId(tweetWithoutHashtagJSON.getString(idString));
                        userGift.setName(tweetWithoutHashtagJSON.getString(getResources().getString(R.string.user_gift_parsing_name)));
                        userGift.setCategory(String.valueOf(Html.fromHtml(tweetWithoutHashtagJSON.getString(getResources().getString(R.string.user_gift_parsing_category)))));
                        userGift.setDescription(tweetWithoutHashtagJSON.getString(getResources().getString(R.string.user_gift_parsing_description)));
                        userGift.setLat(tweetWithoutHashtagJSON.getString(lat));
                        userGift.setLon(tweetWithoutHashtagJSON.getString(lon));
                        userGift.setAddress(AddressPermissionUtils.addressString(mContext, Double.parseDouble(tweetWithoutHashtagJSON.getString(lat)), Double.parseDouble(tweetWithoutHashtagJSON.getString(lon))));
                        userGift.setIssuer(tweetWithoutHashtagJSON.getString(getResources().getString(R.string.json_issuer)));

                        if (!userGift.getIssuer().equals(MainActivity.userName)) {
                            switch (userGift.getCategory()){
                                case "Sport":
                                    sportA.add(userGift);
                                    break;
                                case "Electronics":
                                    electronicsA.add(userGift);
                                    break;
                                case "Clothing":
                                    clothingA.add(userGift);
                                    break;
                                case "Music&Books":
                                    musicA.add(userGift);
                                    break;
                                case "Other":
                                    otherA.add(userGift);
                                    break;
                            }
                            arrayUsersGifts.add(userGift);
                            Log.i("giftgift", "array " + userGift.getName());
                        }


                    }
                    Log.i("giftgift", "sport " + sportA);
                    Log.i("giftgift", "elec " + electronicsA);
                    Log.i("giftgift", "clo " + clothingA);
                    Log.i("giftgift", "mus " + musicA);
                    Log.i("giftgift", "oth " + otherA);

                    setupRecyclerView(sportA, recyclerViewSport);
                    setupRecyclerView(electronicsA, recyclerViewElectronics);
                    setupRecyclerView(clothingA, recyclerViewClothing);
                    setupRecyclerView(musicA, recyclerViewMusic);
                    setupRecyclerView(otherA, recyclerViewOther);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                for (UsersGift userGift : arrayUsersGifts) {
//                    LatLng latLng = new LatLng(Double.parseDouble(userGift.getLat()), Double.parseDouble(userGift.getLon()));
//                    addMarker(latLng, userGift.getCategory());
                    coordMarker[0] = Double.parseDouble(userGift.getLat());
                    coordMarker[1] = Double.parseDouble(userGift.getLon());
                    //addMarker(coordMarker, userGift.getCategory());
                }

//                bCallback.onLoadComplete();
//
//
//                viewPagerAdapter = new PageViewAdapter(mContext, usersGifts,activity);
//                viewPager.setAdapter(viewPagerAdapter);
//                viewPager.setPadding(0, 0, 300, 0);
//                viewPager.setClipToPadding(false);
//
//
//                viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//                    @Override
//                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//                    }
//
//                    @Override
//                    public void onPageSelected(int position) {
//                        LatLng latLng = new LatLng(Double.parseDouble(usersGifts.get(position).getLat()), Double.parseDouble(usersGifts.get(position).getLon()));
//                        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.5f), 1000, null); //1000 is animation time
//                    }
//
//                    @Override
//                    public void onPageScrollStateChanged(int state) {
//
//                    }
//                });
            }

            @Override
            public void onError(VolleyError error) {
                error.printStackTrace();
            }
        });

        //setUpBottomSheetDialog();

        return v;
    }

    private void enableGPS(){

        // Build the alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Attiva la posizione");
        builder.setMessage("Devi attivare la posizione con modalità alta");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                // Show location settings when the user acknowledges the alert dialog
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, 1003);
            }
        });

        Dialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("permperm", String.valueOf(requestCode));

        if (requestCode == HomeFragment.GPS_SETTING_CODE) {
            if (HomeFragment.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                MyGiftTweetsAdapter.reloadFragment(fragment, getActivity());
            }
        }


    }

    private void setupRecyclerView(ArrayList<UsersGift> usersGiftsList, RecyclerView recyclerView){

        userTweetsAdapter = new UserTweetsAdapter(mContext, usersGiftsList, getActivity(),this);

        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(userTweetsAdapter);

    }
}
