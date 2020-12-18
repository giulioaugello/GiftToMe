package com.LAM.GiftToMe.Fragment;

import android.animation.Animator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.LAM.GiftToMe.MainActivity;
import com.LAM.GiftToMe.R;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    private Context mContext;
    private SharedPreferences sharedPreferences;

    private Spinner spinner;
    private LottieAnimationView darkModeOn, darkModeOff;
    private SwitchMaterial switchDarkMode;
    private RadioButton lightRadio, darkRadio;
    private RadioGroup radioGroup;

    private boolean isDarkModeOn;
    private boolean isDarkMap; //prendere dalle shared preferences

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v =  inflater.inflate(R.layout.settings_fragment, container, false);

        mContext = getActivity().getApplicationContext();

        spinner = v.findViewById(R.id.radius_spinner);

        darkModeOn = v.findViewById(R.id.animationDarkModeOn);
        darkModeOff = v.findViewById(R.id.animationDarkModeOff);

        radioGroup = v.findViewById(R.id.radio_group_settings);
        lightRadio = v.findViewById(R.id.light_radio);
        darkRadio = v.findViewById(R.id.dark_radio);

        switchDarkMode = v.findViewById(R.id.switch_dark_mode);

        //SharedPreferences per darkMode e darkMap
        sharedPreferences = mContext.getSharedPreferences("settingsPref", Context.MODE_PRIVATE);

        //cancella le preferenze
        //sharedPreferences.edit().clear().apply();

        final SharedPreferences.Editor editor = sharedPreferences.edit();
        isDarkModeOn = sharedPreferences.getBoolean("darkMode",false);

        isDarkMap = sharedPreferences.getBoolean("darkMap",false);

        //Log.i("settingssettings", "IsDarkModeOn: " + isDarkModeOn);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext, R.array.radius_search, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        //inizializza switch e animazione
        if (!isDarkModeOn){
            darkModeOn.setVisibility(View.VISIBLE);
            switchDarkMode.setChecked(false);
        }else {
            darkModeOff.setVisibility(View.VISIBLE);
            switchDarkMode.setChecked(true);
        }

        if (!isDarkMap){
            lightRadio.setChecked(true);
        }else {
            darkRadio.setChecked(true);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                //RadioButton radioChecked = v.findViewById(checkedId);
                switch (checkedId){
                    case R.id.light_radio:
                        isDarkMap = false;
                        editor.putBoolean("darkMap", false).apply();
                        MainActivity.darkMapOn = isDarkMap;

                        Log.i("settingssettings", "IsDarkMap: " + isDarkMap);
                        break;
                    case R.id.dark_radio:
                        isDarkMap = true;
                        editor.putBoolean("darkMap", true).apply();
                        MainActivity.darkMapOn = isDarkMap;
                        Log.i("settingssettings", "IsDarkMap1: " + isDarkMap);
                        break;
                }

            }
        });

        //switch dark mode on/off and save sharedPref
        switchDarkMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isDarkModeOn){
                    isDarkModeOn = true;
                    editor.putBoolean("darkMode", true).apply();
                    MainActivity.darkModeOn = isDarkModeOn;

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

                    Log.i("settingssettings", "IsDarkModeOn IF: " + isDarkModeOn);
                }else {
                    isDarkModeOn = false;
                    editor.putBoolean("darkMode", false).apply();
                    MainActivity.darkModeOn = isDarkModeOn;

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
                    Log.i("settingssettings", "IsDarkModeOn ELSE: " + isDarkModeOn);
                }

            }
        });




        return v;
    }
}
