package com.LAM.GiftToMe.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.LAM.GiftToMe.Adapter.PopupAdapter;
import com.LAM.GiftToMe.Geofences.GeofencesMain;
import com.LAM.GiftToMe.MainActivity;
import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.Twitter.TwitterFunctions;
import com.LAM.GiftToMe.Twitter.VolleyListener;
import com.LAM.GiftToMe.UsefulClass.AddressPermissionUtils;
import com.LAM.GiftToMe.UsefulClass.CustomInfoWindow;
import com.LAM.GiftToMe.UsefulClass.UsersGift;
import com.android.volley.VolleyError;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.type.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mapsforge.map.android.layers.MyLocationOverlay;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HomeFragment extends Fragment implements LocationListener {

    private static final String TAG = "HomeFragmentTAG";
    private View v;
    private Context mContext;
    private boolean isDown = false;

    public static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    public static int GPS_SETTING_CODE = 1003;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final String TWEET_ARTICLE_HASHTAG = "#LAM_giftToMe_2020-article";

    private MapView map = null;
    private IMapController mapController;
    private MyLocationNewOverlay myLocationNewOverlay;

    private ArrayList<Marker> allMarkers;
    private ArrayList<Marker> selectedMarker;
    private ArrayList<UsersGift> selectedGift;
    private RecyclerView recyclerView;

    public static LocationManager locationManager;

    private ArrayList<UsersGift> arrayUsersGifts;

    private EditText addPosition;
    private ImageView searchPosition, userPos;
    private ArrayList<Marker> removeFirstMarker = new ArrayList<>();
    public static FloatingActionButton floatingActionButton;


    private RadiusMarkerClusterer poiMarkers;

    private GeofencingClient geofencingClient;
    private GeofencesMain geofencesMain;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity().getApplicationContext();
        v = inflater.inflate(R.layout.home_fragment, container, false);
        Configuration.getInstance().load(mContext, PreferenceManager.getDefaultSharedPreferences(mContext));

        map = v.findViewById(R.id.map);
        mapController = map.getController();

        //serve per cluster
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        poiMarkers = new RadiusMarkerClusterer(mContext);

        //sceglie quale icona usare in base alla build
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Drawable clusterIconD = getResources().getDrawable(R.mipmap.cluster_pie, null);
            Bitmap bitmap = ((BitmapDrawable) clusterIconD).getBitmap();
            poiMarkers.setIcon(bitmap);
        }else{
            Drawable clusterIconD = getResources().getDrawable(R.drawable.marker_cluster, null);
            Bitmap clusterIcon = ((BitmapDrawable)clusterIconD).getBitmap();
            poiMarkers.setIcon(clusterIcon);
        }

        //dropdown
        final ImageView dropdown, zoomIn, zoomOut;
        dropdown = v.findViewById(R.id.dropdown_option);
        zoomIn = v.findViewById(R.id.zoom_in);
        zoomOut = v.findViewById(R.id.zoom_out);
        userPos = v.findViewById(R.id.position);
        addPosition = v.findViewById(R.id.add_position);
        searchPosition = v.findViewById(R.id.search_position);

        dropdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isDown){
                    zoomIn.setVisibility(View.VISIBLE);
                    zoomOut.setVisibility(View.VISIBLE);
                    userPos.setVisibility(View.VISIBLE);
                    dropdown.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24);
                    isDown = true;
                }else{
                    zoomIn.setVisibility(View.GONE);
                    zoomOut.setVisibility(View.GONE);
                    userPos.setVisibility(View.GONE);
                    dropdown.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24);
                    isDown = false;
                }
            }
        });

        zoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.getController().zoomIn();
            }
        });

        zoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.getController().zoomOut();
            }
        });

        //tasto nel dropdown
        userPos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || checkIsHighAccuracy() != 3) { //se non ho i permessi li attivo
                    enablePermissionsAndGPS();
                }else{
                    myLocationNewOverlay.enableFollowLocation(); //altrimenti mette il focus sulla posizione dell'utente
                }
            }
        });

        setupMap(); //inizializza elementi della mappa

        enablePermissionsAndGPS(); //richiesta permessi e gps

        getUsersGifts(); //prende i regali

        searchPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSearchPosition();
            }
        });

        floatingActionButton = v.findViewById(R.id.go_to_list);

        //setto l'altezza per il FAB
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) floatingActionButton.getLayoutParams();
            params.verticalBias = 0.8f;
            floatingActionButton.setLayoutParams(params);
        }else if (orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT){
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) floatingActionButton.getLayoutParams();
            params.verticalBias = 0.88f;
            floatingActionButton.setLayoutParams(params);
        }

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserTweetsFragment userTweetsFragment = new UserTweetsFragment();
                FragmentTransaction fragmentTransaction =  getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.righttoleft, R.anim.none);
                fragmentTransaction.replace(R.id.fragment_container, userTweetsFragment, MainActivity.usersGiftListFragmentTag).commit();
                fragmentTransaction.addToBackStack(MainActivity.usersGiftListFragmentTag);
                MainActivity.activeFragment = userTweetsFragment;
            }
        });

        //geofences
        geofencingClient = LocationServices.getGeofencingClient(mContext);
        geofencesMain = new GeofencesMain();

        removeGeofences();

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //crea il necessario per la mappa
    private void setupMap(){

        map.setTileSource(TileSourceFactory.MAPNIK);

        //mappa in dark mode
        if (MainActivity.darkMapOn){
            map.getOverlayManager().getTilesOverlay().setColorFilter(TilesOverlay.INVERT_COLORS);
        }

        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER); //per togliere i tasti zoom built in
        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(map);
        mRotationGestureOverlay.setEnabled(true);
        map.getOverlays().add(mRotationGestureOverlay);
        map.setMultiTouchControls(true); //per il pinch to zoom e rotation

        mapController.setZoom(15.56);
        map.setMaxZoomLevel(19.0);

        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        GeoPoint startPoint = new GeoPoint(44.4932655, 11.3370644);
        mapController.setCenter(startPoint);

        myLocationNewOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(mContext), map); //overlay per la posizione dell'utente
        myLocationNewOverlay.enableMyLocation();
        map.getOverlays().add(myLocationNewOverlay);
        myLocationNewOverlay.enableFollowLocation(); //segue l'overlay della posizione dell'utente, anche quando si muove
        myLocationNewOverlay.setDrawAccuracyEnabled(false); //non disegna il cerchio dell'utente
        mapController.setZoom(17.50);
        Bitmap bitmapNotMoving = BitmapFactory.decodeResource(getResources(), R.drawable.position); //icona per utente fermo
        Bitmap bitmapMoving = BitmapFactory.decodeResource(getResources(), R.drawable.position); //icona per utente in movimento
        myLocationNewOverlay.setDirectionArrow(bitmapNotMoving, bitmapMoving);

    }

    private void enablePermissionsAndGPS(){

        String message = "";
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            message = mContext.getResources().getString(R.string.descr_gps);
        } else if(checkIsHighAccuracy() != 3) {
            message = mContext.getResources().getString(R.string.descr_high);
        } else {
            message = mContext.getResources().getString(R.string.other2);
        }

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || checkIsHighAccuracy() != 3) {
            // Build the alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(mContext.getResources().getString(R.string.title_gps));
            builder.setMessage(message);
            builder.setPositiveButton(mContext.getResources().getString(R.string.ook), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Show location settings when the user acknowledges the alert dialog
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, 1003);
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    if (MainActivity.activeFragment.equals(fragmentManager.findFragmentByTag(MainActivity.homeFragmentTag)) && (checkIsHighAccuracy() != 3 || !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))){
                        enablePermissionsAndGPS();
                    }
                }
            });

            builder.setNegativeButton(mContext.getResources().getString(R.string.dismiss), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //setto i marker nella posizione cercata
                    setSearchPosition();
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    if (MainActivity.activeFragment.equals(fragmentManager.findFragmentByTag(MainActivity.homeFragmentTag)) && checkIsHighAccuracy() != 3 && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                        enablePermissionsAndGPS();
                    }
                }
            });

            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();

        }else {
            checkUserLocation();
        }

    }

    //serve per far funzionare le geofences
    private int checkIsHighAccuracy(){
        try {
            return (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.LOCATION_MODE));
        } catch (Exception exception) {
            Log.i(TAG, "location mode: " + exception.getMessage());
        }
        return 0;
    }

    public void checkUserLocation() {

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            myLocationNewOverlay.enableFollowLocation(); //segue la posizione dell'utente
        } else {
            //richiedo i permessi
            AddressPermissionUtils.requestPermissionsIfNecessary(new String[] {

                    Manifest.permission.ACCESS_FINE_LOCATION, //serve per i permessi della posizione

                    Manifest.permission.WRITE_EXTERNAL_STORAGE, //mi serve per far visualizzzare la mappa

            }, mContext, v);
        }

        //Per geofences mi servono i permessi in background
        if (Build.VERSION.SDK_INT >= 29) {

            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                AddressPermissionUtils.requestPermissionsIfNecessary(new String[] {

                        Manifest.permission.ACCESS_BACKGROUND_LOCATION //permessi per background

                }, mContext, v);

            }else{
                myLocationNewOverlay.enableFollowLocation();
            }

        }
    }

    //mette il marker nella posizione cercata se il gps non è attivo
    private void setSearchPosition(){

        //controllo se ho attivato i permessi
        checkUserLocation();

        //se la barra di ricerca non è vuota, controllo che l'indirizzo scritto esista e aggiungo marker
        if (!addPosition.getText().toString().equals("")){
            if (AddressPermissionUtils.getCoordsFromAddress(addPosition.getText().toString(), mContext) != null) {
                ArrayList<Double> coord = AddressPermissionUtils.getCoordsFromAddress(addPosition.getText().toString(), mContext);

                final GeoPoint start = new GeoPoint(coord.get(0), coord.get(1));

                final Marker marker = new Marker(map);
                removeFirstMarker.add(marker);
                drawableBuildVersion(marker, ContextCompat.getDrawable(mContext, R.drawable.position), ContextCompat.getDrawable(mContext, R.mipmap.position));
                marker.setPosition(start);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setInfoWindow(null); //toglie il popup di default

                map.getOverlays().add(marker);

                //for one marker per search
                if (removeFirstMarker.size() > 1){ //mi serve per rimuovere il marker precedente
                    if (start.toString().equals(removeFirstMarker.get(removeFirstMarker.size() - 1).getPosition().toString())){
                        map.getOverlayManager().remove(removeFirstMarker.get(removeFirstMarker.size() - 2));
                        map.invalidate();
                    }
                }

//                if (removeFirstMarker.size() == 1){
//                    myLocationNewOverlay.disableFollowLocation();
//                    mapController.setCenter(start);
//                }

                myLocationNewOverlay.disableFollowLocation();
                
                mapController.setCenter(start);

                userPos.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && checkIsHighAccuracy() == 3) {
                            myLocationNewOverlay.enableFollowLocation();
                            mapController.setZoom(17.50);
                        }else{
                            enablePermissionsAndGPS();
                        }
                        map.getOverlayManager().remove(marker);
                        map.invalidate();

                    }
                });


                addPosition.setText("");

            }else{
                Toast.makeText(mContext, mContext.getResources().getString(R.string.correct_position),Toast.LENGTH_LONG).show();
            }
        }else {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.no_gps_location),Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GPS_SETTING_CODE) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                checkUserLocation();
            }
        }

        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION){
            Log.i(TAG, "eccomi");
        }

    }

    //prende i regali degli utenti tranne quelli dell'utente loggato
    public void getUsersGifts() {
        TwitterFunctions.usersTweets(mContext, TWEET_ARTICLE_HASHTAG, new VolleyListener() {

            @Override
            public void onResponse(String response) {
                arrayUsersGifts = new ArrayList<>();
                allMarkers = new ArrayList<>();
                selectedMarker = new ArrayList<>();
                selectedGift = new ArrayList<>();

                String id, text;
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

                        String lat = getResources().getString(R.string.user_gift_parsing_lat);
                        String lon = getResources().getString(R.string.user_gift_parsing_lon);

                        JSONObject tweetJSON;
                        String jsonParameter = text.replace(TWEET_ARTICLE_HASHTAG, "");

                        try{
                            tweetJSON = new JSONObject(jsonParameter);
                        }
                        catch (JSONException e){
                            continue;
                        }

                        //creo il regalo con tutte le informazioni e lo aggiungo all'array
                        userGift.setGiftId(tweetJSON.getString(idString));
                        userGift.setName(tweetJSON.getString(getResources().getString(R.string.user_gift_parsing_name)));
                        userGift.setDescription(tweetJSON.getString(getResources().getString(R.string.user_gift_parsing_description)));
                        userGift.setCategory(String.valueOf(Html.fromHtml(tweetJSON.getString(getResources().getString(R.string.user_gift_parsing_category)))));
                        userGift.setLat(tweetJSON.getString(lat));
                        userGift.setLon(tweetJSON.getString(lon));
                        userGift.setAddress(AddressPermissionUtils.addressString(mContext, Double.parseDouble(tweetJSON.getString(lat)), Double.parseDouble(tweetJSON.getString(lon))));
                        userGift.setIssuer(tweetJSON.getString(getResources().getString(R.string.json_issuer)));

                        if (!userGift.getIssuer().equals(MainActivity.userName)) {
                            arrayUsersGifts.add(userGift);
                            Log.i(TAG, "arrayUsersGifts " + userGift.getName() + " " + userGift.getIssuer());
                        }


                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                for (UsersGift userGift : arrayUsersGifts) {
                    //aggiungo i marker alla mappa
                    addMarker(userGift.getGeoPoint(), userGift);

                }

                //cerco gli elementi con lo stesso indirizzo (ho anche i duplicati)
                for (int i = 0; i < arrayUsersGifts.size()-1; i++){
                    for (int j = i+1; j < arrayUsersGifts.size(); j++){
                        if (arrayUsersGifts.get(j).getAddress().equals(arrayUsersGifts.get(i).getAddress())){

                            //popolo gli array con gli elementi che hanno la stessa via
                            selectedMarker.add(allMarkers.get(i));
                            selectedMarker.add(allMarkers.get(j));
                            selectedGift.add(arrayUsersGifts.get(i));
                            selectedGift.add(arrayUsersGifts.get(j));

                        }
                    }
                }

                //elimino i marker dal cluster (prendendo l'arraylist creato prima e togliendogli i duplicati)
                if (dupMarker(selectedMarker).size() > 0){
                    for (Marker object: dupMarker(selectedMarker)) {
                        poiMarkers.getItems().remove(object);
                    }
                }

                //ricreo lo stesso numero di marker con diversa icona
                if (dupGift(selectedGift).size() > 0){
                    for (UsersGift gift: dupGift(selectedGift)){

                        Marker markerSamePosition = new Marker(map);
                        drawableBuildVersion(markerSamePosition, ContextCompat.getDrawable(mContext, R.drawable.location_same), ContextCompat.getDrawable(mContext, R.mipmap.location_same));
                        markerSamePosition.setPosition(gift.getGeoPoint());
                        markerSamePosition.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        markerSamePosition.setTitle(gift.getGiftId());
                        poiMarkers.add(markerSamePosition);

                        //onClick per vedere il dialog con la recyclerView (contiene i regali aventi lo stesso indirizzo)
                        markerSamePosition.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker, MapView mapView) {
                                showDialogSamePosition(AddressPermissionUtils.addressString(mContext, marker.getPosition().getLatitude(), marker.getPosition().getLongitude()));
                                setupRecyclerView(searchGiftsSamePosition(dupGift(selectedGift), AddressPermissionUtils.addressString(mContext, marker.getPosition().getLatitude(), marker.getPosition().getLongitude())), recyclerView);
                                return true;
                            }
                        });
                        map.getOverlays().add(poiMarkers);
                    }
                }

            }

            @Override
            public void onError(VolleyError error) {
                error.printStackTrace();
            }
        });

    }

    //dialog per mostrare la recyclerView per i regali con lo stesso indirizzo
    private void showDialogSamePosition(String markerAddress){
        final Dialog dialog = new Dialog(getActivity());
        dialog.setCancelable(true);

        final View v  = getActivity().getLayoutInflater().inflate(R.layout.recyclerview_same_position,null);
        dialog.setContentView(v);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView generalAddress = v.findViewById(R.id.address_recycler);
        recyclerView = v.findViewById(R.id.recycler_popup);

        generalAddress.setText(markerAddress);

        dialog.show();
    }

    //cerca all'interno dell'array i regali che hanno address come indirizzo
    private ArrayList<UsersGift> searchGiftsSamePosition(ArrayList<UsersGift> arrayList, String address){
        ArrayList<UsersGift> returnArray = new ArrayList<>();
        for (UsersGift gift: arrayList){
            if (gift.getAddress().equals(address)){
                returnArray.add(gift);
            }
        }
        return returnArray;
    }

    //collega recyclerView all'adapter
    private void setupRecyclerView(ArrayList<UsersGift> usersGiftsList, RecyclerView recyclerView){

        //recyclerview per stesso indirizzo
        PopupAdapter popupAdapter = new PopupAdapter(mContext, usersGiftsList, getActivity(), this);

        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(popupAdapter);

    }

    //cerca i duplicati nell'array dei marker da eliminare
    private Set<Marker> dupMarker(ArrayList<Marker> arrayList){
        Set<Marker> set = new HashSet<>();
        for(int i =0; i < arrayList.size(); i++) {
            if (!set.contains(arrayList.get(i))) {
                set.add(arrayList.get(i));
            }
        }
        return set;
    }

    //cerca i duplicati nell'array dei regali da aggiungere
    private ArrayList<UsersGift> dupGift(ArrayList<UsersGift> arrayList){
        Set<UsersGift> set = new HashSet<>();
        ArrayList<UsersGift> ret = new ArrayList<>();
        for(int i =0; i < arrayList.size(); i++) {
            if (!set.contains(arrayList.get(i))) {
                set.add(arrayList.get(i));
                ret.add(arrayList.get(i));
            }
        }
        return ret;
    }

    //creo un marker e lo aggiungo al cluster
    @SuppressLint("UseCompatLoadingForDrawables")
    private void addMarker(GeoPoint geoPoint, UsersGift usersGift) {

        Drawable markerDrawablePie = null;
        Drawable markerDrawableNoPie = null;
        Drawable drawableImagePopup = null;

        switch (usersGift.getCategory()) {
            case "Sport":
                markerDrawablePie = getResources().getDrawable(R.mipmap.sport_marker, null);
                markerDrawableNoPie = getResources().getDrawable(R.drawable.sport_marker, null);
                drawableImagePopup = getResources().getDrawable(R.drawable.sport, null);
                break;
            case "Electronics":
                markerDrawablePie = getResources().getDrawable(R.mipmap.electronic_marker, null);
                markerDrawableNoPie = getResources().getDrawable(R.drawable.electronic_marker, null);
                drawableImagePopup = getResources().getDrawable(R.drawable.electronic, null);
                break;
            case "Clothing":
                markerDrawablePie = getResources().getDrawable(R.mipmap.clothing_marker, null);
                markerDrawableNoPie = getResources().getDrawable(R.drawable.clothing_marker, null);
                drawableImagePopup = getResources().getDrawable(R.drawable.clothing, null);
                break;
            case "Music&Books":
                markerDrawablePie = getResources().getDrawable(R.mipmap.music_marker, null);
                markerDrawableNoPie = getResources().getDrawable(R.drawable.music_marker, null);
                drawableImagePopup = getResources().getDrawable(R.drawable.musicbook, null);
                break;
            case "Other":
                markerDrawablePie = getResources().getDrawable(R.mipmap.other_marker, null);
                markerDrawableNoPie = getResources().getDrawable(R.drawable.other_marker, null);
                drawableImagePopup = getResources().getDrawable(R.drawable.file, null);
                break;
        }

        CustomInfoWindow customInfoWindow = new CustomInfoWindow(R.layout.popup_marker, map, usersGift, mContext, getActivity());

        Marker marker = new Marker(map);
        try {
            drawableBuildVersion(marker, markerDrawableNoPie, markerDrawablePie);
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        marker.setPosition(geoPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(usersGift.getGiftId());

        marker.setInfoWindow(customInfoWindow); //setta il popup
        marker.setSubDescription(mContext.getResources().getString(R.string.close_popup)); //sottodescrizione
        marker.setImage(drawableImagePopup); //setta l'immagine nel popup
        marker.setInfoWindowAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_TOP);

        allMarkers.add(marker);

        poiMarkers.add(marker);

        map.getOverlays().add(poiMarkers);

        //aggiungo geofence
        addGeofences(usersGift.getLat(), usersGift.getLon(), MainActivity.radiusSearch);

    }

    //In base alla versione di android prendo i drawable da due cartelle diverse (uscivano marker piccoli in android superiore a pie)
    private void drawableBuildVersion(Marker marker, Drawable drawable, Drawable mipmap){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Bitmap bitmap = ((BitmapDrawable) mipmap).getBitmap();
            Drawable dr = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, (int) (64.0f * getResources().getDisplayMetrics().density), (int) (64.0f * getResources().getDisplayMetrics().density), true));
            marker.setIcon(dr);
        }else{
            marker.setIcon(drawable);
        }
    }

    @SuppressLint("MissingPermission")
    private void addGeofences(String lat, String lon, float radiusSearch){
        Geofence geofence = geofencesMain.createGeofence(UUID.randomUUID().toString(), lat, lon, radiusSearch);
        GeofencingRequest geofencingRequest = geofencesMain.getGeofencingRequest(geofence);
        PendingIntent intent = geofencesMain.getGeofencePendingIntent(mContext);

        Log.i(TAG, "Geofences address coords: " + lat + " " + lon + ", radius: " + radiusSearch);

        geofencingClient.addGeofences(geofencingRequest, intent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Geofences OK");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "Add Geofences Error " + e.getMessage());
                    }
                });

    }

    private void removeGeofences(){
        geofencingClient.removeGeofences(geofencesMain.getGeofencePendingIntent(mContext))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Geofences removed");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "Remove Geofences Error: " + e.getMessage());
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        Configuration.getInstance().load(mContext, PreferenceManager.getDefaultSharedPreferences(mContext));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up

    }

    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        Configuration.getInstance().save(mContext, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onLocationChanged(Location location) {
        myLocationNewOverlay.enableFollowLocation();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
