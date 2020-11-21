package com.LAM.GiftToMe.Fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.LAM.GiftToMe.MainActivity;
import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.Twitter.TwitterRequests;
import com.LAM.GiftToMe.Twitter.VolleyListener;
import com.LAM.GiftToMe.UsefulClass.AddressUtils;
import com.LAM.GiftToMe.UsefulClass.EditString;
import com.LAM.GiftToMe.UsefulClass.UsersGift;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment implements LocationListener {

    private View v;
    private Context mContext;
    private boolean isDown = false;

    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private int GPS_SETTING_CODE = 1003;
    private static final String TWEET_ARTICLE_HASHTAG = "#LAM_giftToMe_2020-article";

    private MapView map = null;
    private IMapController mapController;
    private MyLocationNewOverlay myLocationNewOverlay;

    protected LocationManager locationManager;

    private ArrayList<UsersGift> arrayUsersGifts;
    private double[] coordMarker = new double[2];

    private EditText addPosition;
    private ImageView searchPosition;
    private ArrayList<Marker> removeFirstMarker = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity().getApplicationContext();
        v = inflater.inflate(R.layout.home_fragment, container, false);
        Configuration.getInstance().load(mContext, PreferenceManager.getDefaultSharedPreferences(mContext));

        final ImageView dropdown, zoomIn, zoomOut, userPos;
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

        map = v.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        //map.getOverlayManager().getTilesOverlay().setColorFilter(TilesOverlay.INVERT_COLORS); //mappa in dark mode

        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER); //per togliere i tasti zoom built in
        //map.getZoomController().getDisplay().setPositions(false, CustomZoomButtonsDisplay.HorizontalPosition.LEFT, CustomZoomButtonsDisplay.VerticalPosition.TOP);
        map.setMultiTouchControls(true); //per il pinch to zoom

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

        mapController = map.getController();
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

        enablePermissionsAndGPS();

        getUsersGifts();

        return v;
    }



    public void getUsersGifts() {
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
                        userGift.setAddress(AddressUtils.addressString(mContext, Double.parseDouble(tweetWithoutHashtagJSON.getString(lat)), Double.parseDouble(tweetWithoutHashtagJSON.getString(lon))));
                        userGift.setIssuer(tweetWithoutHashtagJSON.getString(getResources().getString(R.string.json_issuer)));

                        if (!userGift.getIssuer().equals(MainActivity.userName)) {
                            arrayUsersGifts.add(userGift);
                            Log.i("giftgift", "array " + userGift.getName());
                        }


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                for (UsersGift userGift : arrayUsersGifts) {
//                    LatLng latLng = new LatLng(Double.parseDouble(userGift.getLat()), Double.parseDouble(userGift.getLon()));
//                    addMarker(latLng, userGift.getCategory());
                    coordMarker[0] = Double.parseDouble(userGift.getLat());
                    coordMarker[1] = Double.parseDouble(userGift.getLon());
                    addMarker(coordMarker, userGift.getCategory());
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

    private void addMarker(double[] array, String category) {

        int markerIcon = 0;

        switch (category) {
            case "Sport":
                markerIcon = R.drawable.sport_marker;
                break;
            case "Electronics":
                markerIcon = R.drawable.electronic_marker;
                break;
            case "Clothing":
                markerIcon = R.drawable.clothing_marker;
                break;
            case "Music&Books":
                markerIcon = R.drawable.music_marker;
                break;
            case "Other":
                markerIcon = R.drawable.other_marker;
                break;
        }

        GeoPoint startPoint = new GeoPoint(array[0], array[1]);
        Marker marker = new Marker(map);
        marker.setIcon(ContextCompat.getDrawable(mContext, markerIcon));
        marker.setPosition(startPoint);
        //marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(marker);

//        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(marker);
//
//        gMap.addMarker(new MarkerOptions().position(latLng).icon(icon));
//
//        addGeofence(latLng, MainActivity.radius);

    }

    //mette il marker nella posizione cercata se il gps non è attivo
    private void setMarkerPosition(){
        Toast.makeText(mContext, "Inserisci una posizione che ti interessa",Toast.LENGTH_LONG).show();
        searchPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!addPosition.getText().toString().equals("")){
                    if (AddressUtils.getCoordsFromAddress(addPosition.getText().toString(), mContext) != null) {
                        ArrayList<Double> coord = AddressUtils.getCoordsFromAddress(addPosition.getText().toString(), mContext);

                        GeoPoint startPoint = new GeoPoint(coord.get(0), coord.get(1));
                        Log.i("geogeo", "coord " + coord.get(0) + " " + coord.get(1));

                        Marker marker = new Marker(map);
                        removeFirstMarker.add(marker);
                        marker.setIcon(ContextCompat.getDrawable(mContext, R.drawable.position));
                        marker.setPosition(startPoint);
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        Log.i("geogeo", "coord " + marker.getPosition());

                        map.getOverlays().add(marker);
                        mapController.setCenter(startPoint);

                        if (removeFirstMarker.size() > 1){ //mi serve per rimuovere il marker precedente
                            if (startPoint.toString().equals(removeFirstMarker.get(removeFirstMarker.size() - 1).getPosition().toString())){
                                map.getOverlayManager().remove(removeFirstMarker.get(removeFirstMarker.size() - 2));
                                map.invalidate();
                            }
                        }

                        addPosition.setText("");

                    }else{
                        Toast.makeText(mContext, "Inserisci una posizione valida",Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(mContext, "Inserisci una posizione che ti interessa",Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public void enablePermissionsAndGPS(){
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

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
                        setMarkerPosition();
                    }
                });

                Dialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();

            }else {
                checkUserLocation();
            }

        }else {
            //Richiedo i permessi e richiamo enablePermissionsAndGPS() per far uscire il dialog del gps
            requestPermissionsIfNecessary(new String[] {

                    Manifest.permission.ACCESS_FINE_LOCATION, //serve per i permessi della posizione

                    Manifest.permission.WRITE_EXTERNAL_STORAGE //mi serve per far visualizzzare la mappa
            });
            enablePermissionsAndGPS();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GPS_SETTING_CODE) {
            Log.i("gpsgps", String.valueOf(requestCode));
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && checkIsHighAccuracy() == 3) {
                checkUserLocation();
            }
        }

    }

    private void checkUserLocation() {

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            myLocationNewOverlay.enableFollowLocation(); //segue la posizione dell'utente
        } else {
            //richiedo i permessi
            requestPermissionsIfNecessary(new String[] {

                    Manifest.permission.ACCESS_FINE_LOCATION, //serve per i permessi della posizione

                    Manifest.permission.WRITE_EXTERNAL_STORAGE //mi serve per far visualizzzare la mappa
            });
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

    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(mContext, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
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
