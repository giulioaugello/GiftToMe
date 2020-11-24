package com.LAM.GiftToMe.UsefulClass;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.LAM.GiftToMe.R;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

public class CustomInfoWindow extends MarkerInfoWindow {

    private String title, description, issuer;
    private Context mContext;

    public CustomInfoWindow(int layoutResId, MapView mapView, String title, String description, String issuer, Context context) {
        super(layoutResId, mapView);

        this.title = title;
        this.description = description;
        this.issuer = issuer;
        this.mContext = context;

    }

    @Override
    public void onOpen(Object item) {
        super.onOpen(item);

        /**
         * @param layoutResId layout that must contain these ids: bubble_title,bubble_description,
         *                    bubble_subdescription, bubble_image
         * @param mapView
         */
        TextView titleText = getView().findViewById(R.id.bubble_title);
        TextView descriptionText = getView().findViewById(R.id.bubble_description);
        Button contactButton = getView().findViewById(R.id.contact);

        titleText.setText(title);
        descriptionText.setText(description);
        contactButton.setText("Contatta " + issuer);

        contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "ciao", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
