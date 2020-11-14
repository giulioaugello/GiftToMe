package com.LAM.GiftToMe.Fragment;

import android.Manifest;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.LAM.GiftToMe.MainActivity;
import com.LAM.GiftToMe.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.CustomZoomButtonsDisplay;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment implements LocationListener {

    private View v;
    private Context mContext;

    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView map = null;
    private MyLocationNewOverlay myLocationNewOverlay;

    protected LocationManager locationManager;
    protected LocationListener locationListener;

    String lat;
    String provider;
    protected String latitude, longitude;
    protected boolean gps_enabled, network_enabled;
    private int FINE_LOCATION_ACCESS_REQUEST_CODE = 1001;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mContext = getActivity().getApplicationContext();

        Configuration.getInstance().load(mContext, PreferenceManager.getDefaultSharedPreferences(mContext));

        v = inflater.inflate(R.layout.home_fragment, container, false);

        ImageView zoomIn = v.findViewById(R.id.zoom_in);
        ImageView zoomOut = v.findViewById(R.id.zoom_out);

        map = v.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        requestPermissionsIfNecessary(new String[]{
                // if you need to show the current location, uncomment the line below
                Manifest.permission.ACCESS_FINE_LOCATION,
                // WRITE_EXTERNAL_STORAGE is required in order to show the map
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });

        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        //map.getZoomController().getDisplay().setPositions(false, CustomZoomButtonsDisplay.HorizontalPosition.LEFT, CustomZoomButtonsDisplay.VerticalPosition.TOP);
        map.setMultiTouchControls(true);

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

        IMapController mapController = map.getController();
        mapController.setZoom(15.56);
        map.setMaxZoomLevel(19.0);

        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        gps_enabled = false;
        network_enabled = false;

        GeoPoint startPoint = new GeoPoint(44.4932655, 11.3370644);
        mapController.setCenter(startPoint);

        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            ex.getMessage();
        }

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            ex.getMessage();
        }

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }else{
            //getLocation();
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        if(!gps_enabled && !network_enabled) {
            // notify user
//            new AlertDialog.Builder(mContext)
//                    .setMessage("Devi dare i permessi")
//                    .setPositiveButton("apri le impostazioni", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
//                            mContext.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//                        }
//                    })
//                    .setNegativeButton("annulla",null)
//                    .show();
//            GeoPoint startPoint = new GeoPoint(44.4932655, 11.3370644);
//            mapController.setCenter(startPoint);
        }else{
            myLocationNewOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(mContext), map);
            myLocationNewOverlay.enableMyLocation();
            map.getOverlays().add(myLocationNewOverlay);
            myLocationNewOverlay.enableFollowLocation();
            mapController.setZoom(18.90);
            Bitmap bitmapNotMoving = BitmapFactory.decodeResource(getResources(), R.drawable.position);
            Bitmap bitmapMoving = BitmapFactory.decodeResource(getResources(), R.drawable.position);
            myLocationNewOverlay.setDirectionArrow(bitmapNotMoving, bitmapMoving);
            myLocationNewOverlay.setPersonIcon(bitmapNotMoving);
            Log.i("geogeo", "Sono qui " + myLocationNewOverlay);
        }




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
        Log.i("geogeo", "Latitudine: " + location.getLatitude() + ", Longitudine: " + location.getLongitude());
        myLocationNewOverlay.enableFollowLocation();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i("geogeo","status");
    }

    @Override
    public void onProviderEnabled(String provider) {
        //Log.i("geogeo","enable");
    }

    @Override
    public void onProviderDisabled(String provider) {
        //Log.i("geogeo","disable");
    }
}
