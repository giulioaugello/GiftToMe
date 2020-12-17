package com.LAM.GiftToMe.Fragment;

import android.animation.Animator;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.LAM.GiftToMe.R;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    private Context mContext;

    private Spinner spinner;
    private LottieAnimationView darkModeOn, darkModeOff;
    private SwitchMaterial switchDarkMode;
    private RadioButton lightRadio, darkRadio;

    private boolean isDarkModeOn = false; //prendere dalle shared preferences
    private boolean isMapDark = false; //prendere dalle shared preferences

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.settings_fragment, container, false);

        mContext = getActivity().getApplicationContext();

        spinner = v.findViewById(R.id.radius_spinner);

        darkModeOn = v.findViewById(R.id.animationDarkModeOn);
        darkModeOff = v.findViewById(R.id.animationDarkModeOff);

        lightRadio = v.findViewById(R.id.light_radio);
        darkRadio = v.findViewById(R.id.dark_radio);

        switchDarkMode = v.findViewById(R.id.switch_dark_mode);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext, R.array.radius_search, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        //inizializzare isDarkModeOn con le sharedPreferences
        if (!isDarkModeOn){
            darkModeOn.setVisibility(View.VISIBLE);
        }else {
            darkModeOff.setVisibility(View.VISIBLE);
        }

        switchDarkMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isDarkModeOn){
                    isDarkModeOn = true;

                    darkModeOff.setVisibility(View.GONE);
                    darkModeOn.setVisibility(View.VISIBLE);

                    darkModeOn.playAnimation();

                    darkModeOn.addAnimatorListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            darkModeOn.setVisibility(View.GONE);
                            darkModeOff.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });

                    Log.i("settingssettings", "IsChecked IF: " + isDarkModeOn);
                }else {
                    isDarkModeOn = false;

                    darkModeOn.setVisibility(View.GONE);
                    darkModeOff.setVisibility(View.VISIBLE);

                    darkModeOff.playAnimation();

                    darkModeOn.addAnimatorListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            darkModeOff.setVisibility(View.GONE);
                            darkModeOn.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    Log.i("settingssettings", "IsChecked ELSE: " + isDarkModeOn);
                }

            }
        });




        return v;
    }
}
