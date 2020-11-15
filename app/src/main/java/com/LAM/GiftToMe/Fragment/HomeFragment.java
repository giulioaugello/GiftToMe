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
import android.widget.ImageView;

import com.LAM.GiftToMe.MainActivity;
import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.Twitter.TwitterRequests;
import com.LAM.GiftToMe.Twitter.VolleyListener;
import com.LAM.GiftToMe.UsefulClass.AddressUtils;
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
import org.osmdroid.views.CustomZoomButtonsDisplay;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

public class HomeFragment extends Fragment implements LocationListener {

    private View v;
    private Context mContext;

    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView map = null;
    private IMapController mapController;
    private MyLocationNewOverlay myLocationNewOverlay;

    protected LocationManager locationManager;
    protected LocationListener locationListener;

    String lat;
    String provider;
    protected String latitude, longitude;
    protected boolean gps_enabled, network_enabled;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private int FINE_LOCATION_ACCESS_REQUEST_CODE = 1001;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity().getApplicationContext();
        v = inflater.inflate(R.layout.home_fragment, container, false);
        Configuration.getInstance().load(mContext, PreferenceManager.getDefaultSharedPreferences(mContext));

        ImageView zoomIn = v.findViewById(R.id.zoom_in);
        ImageView zoomOut = v.findViewById(R.id.zoom_out);

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

        requestPermissionsIfNecessary(new String[] {

                Manifest.permission.ACCESS_FINE_LOCATION, //serve per la posizione dell'utente

                Manifest.permission.WRITE_EXTERNAL_STORAGE //mi serve per far visualizzzare la mappa
        });

        myLocationNewOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(mContext), map); //overlay per la posizione dell'utente
        myLocationNewOverlay.enableMyLocation();
        map.getOverlays().add(myLocationNewOverlay);
        myLocationNewOverlay.enableFollowLocation(); //segue l'overlay della posizione dell'utente, anche quando si muove
        mapController.setZoom(17.50);
        Bitmap bitmapNotMoving = BitmapFactory.decodeResource(getResources(), R.drawable.position); //icona per utente fermo
        Bitmap bitmapMoving = BitmapFactory.decodeResource(getResources(), R.drawable.position); //icona per utente in movimento
        myLocationNewOverlay.setDirectionArrow(bitmapNotMoving, bitmapMoving);

        return v;
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
