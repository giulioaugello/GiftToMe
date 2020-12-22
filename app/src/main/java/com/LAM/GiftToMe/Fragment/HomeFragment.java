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
import com.LAM.GiftToMe.Twitter.TwitterRequests;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HomeFragment extends Fragment implements LocationListener {

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

    //public static UserTweetsFragment userTweetsFragment;

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
//        Drawable clusterIconD = getResources().getDrawable(R.drawable.marker_cluster, null);
//        Bitmap clusterIcon = ((BitmapDrawable)clusterIconD).getBitmap();
//        poiMarkers.setIcon(clusterIcon);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Drawable clusterIconD = getResources().getDrawable(R.mipmap.cluster_pie, null);
            Bitmap bitmap = ((BitmapDrawable) clusterIconD).getBitmap();
            //Drawable drawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, (int) (65.0f * getResources().getDisplayMetrics().density), (int) (65.0f * getResources().getDisplayMetrics().density), true));

            //Bitmap icon = BitmapFactory.decodeResource(mContext.getResources(), );
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


        geofencingClient = LocationServices.getGeofencingClient(mContext);
        geofencesMain = new GeofencesMain();

        removeGeofences();


        return v;
    }

    //resetto i valori degli array
    @Override
    public void onDestroy() {
        super.onDestroy();
//        Log.i("giftgift", "Prima: " + selectedGift.size());
//        selectedGift.clear();
//        Log.i("giftgift", "Dopo: " + selectedGift.size());
//        selectedMarker.clear();
//        allMarkers.clear();
//        arrayUsersGifts.clear();
    }

    private void setupMap(){

        map.setTileSource(TileSourceFactory.MAPNIK);

        //mappa in dark mode
        if (MainActivity.darkMapOn){
            map.getOverlayManager().getTilesOverlay().setColorFilter(TilesOverlay.INVERT_COLORS);
        }

        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER); //per togliere i tasti zoom built in
        //map.getZoomController().getDisplay().setPositions(false, CustomZoomButtonsDisplay.HorizontalPosition.LEFT, CustomZoomButtonsDisplay.VerticalPosition.TOP);
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

        Log.i("gpsgps", "Dentro");
        String message = "";
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) message = "Devi attivare la posizione";
        else if(checkIsHighAccuracy() != 3) message = "La posizione deve essere in modalità alta";
        else message = "altro";
        //qui il gps non è attivo quindi mostro dialog
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || checkIsHighAccuracy() != 3) {
            // Build the alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Attiva la posizione");
            builder.setMessage(message);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Show location settings when the user acknowledges the alert dialog
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, 1003);
                }
            });

            builder.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setSearchPosition();
                    //se faccio annulla controllo se ho dato i permessi
                }
            });

            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();

        }else {
            checkUserLocation();
        }
//
//        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && checkIsHighAccuracy() != 3){
//            //se nn ho dato i permessi e il gps è spento: ho solo la lista in base alla posizione scritta
//            Log.i("permperm", "Eccomi");
//        }else if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && checkIsHighAccuracy() == 3){
//            //se non ho dato i permessi e il gps è attivo: ho solo la lista con i regali vicini alla posizione dell'utente
//            Log.i("permperm", "Eccomi1");
//        }else if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && checkIsHighAccuracy() != 3){
//            //se ho dato i permessi ma non il gps: ho la mappa e lista con regali in base alla posizione scritta
//            Log.i("permperm", "Eccomi2");
//        }else{
//            //se ho attivato tutto: ho la mappa e la lista dei regali in base alla posizione dell'utente
//            Log.i("permperm", "Eccomi3");
//        }
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

    private int checkIsHighAccuracy(){
        try {
            return (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.LOCATION_MODE));
        } catch (Exception exception) {
            Log.i("gpsgps", "check " + exception.getMessage());
        }
        return 0;
    }

    //mette il marker nella posizione cercata se il gps non è attivo
    private void setSearchPosition(){
        //Toast.makeText(mContext, "Inserisci una posizione che ti interessa",Toast.LENGTH_LONG).show();

        //controllo se ho attivato i permessi
        checkUserLocation();

        if (!addPosition.getText().toString().equals("")){
            if (AddressPermissionUtils.getCoordsFromAddress(addPosition.getText().toString(), mContext) != null) {
                ArrayList<Double> coord = AddressPermissionUtils.getCoordsFromAddress(addPosition.getText().toString(), mContext);

                final GeoPoint start = new GeoPoint(coord.get(0), coord.get(1));
                Log.i("geogeo", "coord " + coord.get(0) + " " + coord.get(1));


                final Marker marker = new Marker(map);
                removeFirstMarker.add(marker);
                drawableBuildVersion(marker, ContextCompat.getDrawable(mContext, R.drawable.position), ContextCompat.getDrawable(mContext, R.mipmap.position));
                //marker.setIcon(ContextCompat.getDrawable(mContext, R.drawable.position));
                marker.setPosition(start);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setInfoWindow(null); //toglie il popup di default
                Log.i("geogeo", "coord " + marker.getPosition());

                map.getOverlays().add(marker);

                if (removeFirstMarker.size() > 1){ //mi serve per rimuovere il marker precedente
                    if (start.toString().equals(removeFirstMarker.get(removeFirstMarker.size() - 1).getPosition().toString())){
                        map.getOverlayManager().remove(removeFirstMarker.get(removeFirstMarker.size() - 2));
                        map.invalidate();
                    }
                }

                if (removeFirstMarker.size() == 1){
                    Log.i("geogeo", "size " + removeFirstMarker.size());
                    myLocationNewOverlay.disableFollowLocation();
                    mapController.setCenter(start);
                }

                myLocationNewOverlay.disableFollowLocation();

                mapController.setCenter(start);

                userPos.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && checkIsHighAccuracy() == 3) {
                            myLocationNewOverlay.enableFollowLocation();
                            mapController.setZoom(17.50);
                        }else{
                            Log.i("geogeo", "size1 " + removeFirstMarker.size());
                            enablePermissionsAndGPS();
                        }
                        map.getOverlayManager().remove(marker);
                        map.invalidate();

                        //controllare se il gps è attivo

                    }
                });


                addPosition.setText("");

            }else{
                Toast.makeText(mContext, "Inserisci una posizione valida",Toast.LENGTH_LONG).show();
            }
        }else {
            Toast.makeText(mContext, "Non hai attivato il gps, inserisci una posizione",Toast.LENGTH_LONG).show();
        }

    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("permperm", String.valueOf(requestCode));

        if (requestCode == GPS_SETTING_CODE) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && checkIsHighAccuracy() == 3) {
                checkUserLocation();
            }
        }

        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION){
            Log.i("permperm", "eccomi");
        }

    }


//    private void requestPermissionsIfNecessary(String[] permissions) {
//        ArrayList<String> permissionsToRequest = new ArrayList<>();
//        for (String permission : permissions) {
//            if (ContextCompat.checkSelfPermission(mContext, permission)
//                    != PackageManager.PERMISSION_GRANTED) {
//                // Permission is not granted
//                permissionsToRequest.add(permission);
//
//            }
//        }
//        if (permissionsToRequest.size() > 0) {
//            ActivityCompat.requestPermissions(
//                    getActivity(),
//                    permissionsToRequest.toArray(new String[0]),
//                    REQUEST_PERMISSIONS_REQUEST_CODE); //mette in REQUEST_PERMISSIONS_REQUEST_CODE i permessi da richiedere
//        }
//    }

    public void getUsersGifts() {
        TwitterRequests.searchTweets(mContext, TWEET_ARTICLE_HASHTAG, new VolleyListener() {

            @Override
            public void onResponse(String response) {
                arrayUsersGifts = new ArrayList<>();
                allMarkers = new ArrayList<>();
                selectedMarker = new ArrayList<>();
                selectedGift = new ArrayList<>();

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
                            arrayUsersGifts.add(userGift);
//                            Log.i("giftgift", "array " + userGift.getName() + " " + userGift.getIssuer());
                        }


                    }
//                    Log.i("giftgift", "username " + MainActivity.userName);
//                    Log.i("giftgift", "array " + arrayUsersGifts);
//                    Log.i("giftgift", "size " + arrayUsersGifts.size());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                for (UsersGift userGift : arrayUsersGifts) {
                    //aggiungo i marker alla mappa
                    addMarker(userGift.getGeoPoint(), userGift.getCategory(), userGift.getGiftId(), userGift.getTweetId(), userGift.getName(), userGift.getDescription(), userGift.getAddress(), userGift.getIssuer());

                }

                //cerco gli elementi con lo stesso indirizzo (ho anche i duplicati)
                for (int i = 0; i < arrayUsersGifts.size()-1; i++){
                    for (int j = i+1; j < arrayUsersGifts.size(); j++){
                        if (arrayUsersGifts.get(j).getAddress().equals(arrayUsersGifts.get(i).getAddress())){

                            //Cluster veloce sullo stesso indirizzo
//                            RadiusMarkerClusterer poiMarkers = new RadiusMarkerClusterer(mContext);
//                            Drawable clusterIconD = getResources().getDrawable(R.drawable.marker_cluster, null);
//                            Bitmap clusterIcon = ((BitmapDrawable)clusterIconD).getBitmap();
//                            poiMarkers.setIcon(clusterIcon);
//                            poiMarkers.add(allMarkers.get(i));
//                            poiMarkers.add(allMarkers.get(j));
//                            map.getOverlays().remove(allMarkers.get(i));
//                            map.getOverlays().remove(allMarkers.get(j));
//                            map.getOverlays().add(poiMarkers);

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
                        //markerSamePosition.setIcon(ContextCompat.getDrawable(mContext, R.drawable.location_same));
                        markerSamePosition.setPosition(gift.getGeoPoint());
                        markerSamePosition.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        markerSamePosition.setTitle(gift.getGiftId());
                        //Log.i("giftgift", markerSamePosition.getTitle());
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

        //moveCameraToUserLocation();
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

    //cerca i duplicati nell'array dei marker da aggiungere
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
    private void addMarker(GeoPoint geoPoint, String category, String giftId, String tweetId, String title, String description, String address, String issuer) {

        Drawable markerDrawablePie = null;
        Drawable markerDrawableNoPie = null;
        Drawable drawableImagePopup = null;

        switch (category) {
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



        //marker senza cluster
        //GeoPoint startPoint = new GeoPoint(array[0], array[1]);
        //MarkerInfoWindow markerInfoWindow = new MarkerInfoWindow(R.layout.popup_marker, map);
        //popup custom
//        CustomInfoWindow customInfoWindow = new CustomInfoWindow(R.layout.popup_marker, map, title, description, issuer, mContext);
//
//        Marker marker = new Marker(map);
//        marker.setIcon(ContextCompat.getDrawable(mContext, markerIcon));
//        marker.setPosition(geoPoint);
//        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//
//        marker.setInfoWindow(customInfoWindow); //setta il popup
//        marker.setSubDescription("Tocca per chiudere oppure"); //sottodescrizione
//        marker.setImage(drawableImagePopup); //setta l'immagine nel popup
//        marker.setInfoWindowAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_TOP);
//
//        allMarkers.add(marker);
//
//        map.getOverlays().add(marker);



        CustomInfoWindow customInfoWindow = new CustomInfoWindow(R.layout.popup_marker, map, giftId, tweetId, title, description, issuer, address, mContext, getActivity());

        Marker marker = new Marker(map);
        drawableBuildVersion(marker, markerDrawableNoPie, markerDrawablePie);
        marker.setPosition(geoPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(giftId);

        marker.setInfoWindow(customInfoWindow); //setta il popup
        marker.setSubDescription("Tocca per chiudere oppure"); //sottodescrizione
        marker.setImage(drawableImagePopup); //setta l'immagine nel popup
        marker.setInfoWindowAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_TOP);

        allMarkers.add(marker);

        poiMarkers.add(marker);

        map.getOverlays().add(poiMarkers);

        //se sono vicino al marker apre da solo il popup?

        //aggiungo geofence
        addGeofences(address, MainActivity.radiusSearch);

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
    private void addGeofences(String address, float radiusSearch){
        Geofence geofence = geofencesMain.createGeofence(UUID.randomUUID().toString(), AddressPermissionUtils.getCoordsFromAddress(address, mContext), radiusSearch);
        GeofencingRequest geofencingRequest = geofencesMain.getGeofencingRequest(geofence);
        PendingIntent intent = geofencesMain.getGeofencePendingIntent(mContext);

        Log.i("geofencegeofence", "Address coords: " + AddressPermissionUtils.getCoordsFromAddress(address, mContext) + ", raggio di ricerca: " + radiusSearch);

        geofencingClient.addGeofences(geofencingRequest, intent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("geofencegeofence", "OK");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("geofencegeofence", "Error " + e.getMessage());
                    }
                });
    }

    private void removeGeofences(){
        geofencingClient.removeGeofences(geofencesMain.getGeofencePendingIntent(mContext))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("geofencegeofence", "rimosso");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("geofencegeofence", "Error: " + e.getMessage());
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
//        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
//            //enablePermissionsAndGPS();
//            String userTweetsFragmentTag = getResources().getString(R.string.users_tweets_fragment_tag);
//            UserTweetsFragment userTweetsFragment = new UserTweetsFragment();
//            FragmentTransaction fragmentTransaction =  getActivity().getSupportFragmentManager().beginTransaction();
//            fragmentTransaction.setCustomAnimations(R.anim.bottomtotop, R.anim.none);
//            fragmentTransaction.replace(R.id.fragment_container, userTweetsFragment, userTweetsFragmentTag).commit();
//            fragmentTransaction.addToBackStack(userTweetsFragmentTag);
//            MainActivity.activeFragment = userTweetsFragment;
//        }

        //resetto per non far aumentare il numero dei marker nel cluster dello stesso luogo
//        selectedGift.clear();
//        selectedMarker.clear();
//        allMarkers.clear();
        //arrayUsersGifts.clear();
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
