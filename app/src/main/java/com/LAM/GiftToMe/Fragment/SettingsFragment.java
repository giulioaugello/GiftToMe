package com.LAM.GiftToMe.Fragment;

import android.animation.Animator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.LAM.GiftToMe.Adapter.MyGiftTweetsAdapter;
import com.LAM.GiftToMe.MainActivity;
import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.UsefulClass.MyGift;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class SettingsFragment extends Fragment {

    private Context mContext;
    private SharedPreferences sharedPreferences;

    private Spinner spinner;
    private LottieAnimationView darkModeOn, darkModeOff;
    private SwitchMaterial switchDarkMode;
    private RadioButton lightRadio, darkRadio;
    private RadioGroup radioGroup;

    private boolean isDarkModeOn;
    private boolean isDarkMap;
    private Fragment fragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v =  inflater.inflate(R.layout.settings_fragment, container, false);

        mContext = getActivity().getApplicationContext();
        fragment = getActivity().getSupportFragmentManager().findFragmentByTag(getResources().getString(R.string.settings_fragment_tag));

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

        float radius = sharedPreferences.getFloat("radiusSearch",100);
        isDarkModeOn = sharedPreferences.getBoolean("darkMode",false);
        isDarkMap = sharedPreferences.getBoolean("darkMap",false);

        int index = 0;

        switch((int)radius){
            case 100:
                index = 0;
                break;
            case 300:
                index = 1;
                break;
            case 500:
                index = 2;
                break;
            case 1000:
                index = 3;
                break;
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext, R.array.radius_search, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelected(false);  // otherwise listener will be called on initialization
        spinner.setSelection(index,true);  // otherwise listener will be called on initialization

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                float radius = Float.parseFloat(parent.getItemAtPosition(position).toString());

                //metto nelle sharedPreferences
                editor.putFloat("radiusSearch",radius).apply();
                MainActivity.radiusSearch = radius;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //int nightModeFlags = mContext.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        //inizializza switch e animazione
        if (isDarkModeOn){
            darkModeOff.setVisibility(View.VISIBLE);
            switchDarkMode.setChecked(true);
        }else {
            darkModeOn.setVisibility(View.VISIBLE);
            switchDarkMode.setChecked(false);
        }
        //inizializzo quale radio button deve essere checked
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
                    case R.id.light_radio: //se è checked il lightRadio
                        isDarkMap = false;

                        //metto nelle sharedPreferences
                        editor.putBoolean("darkMap", false).apply();
                        MainActivity.darkMapOn = isDarkMap;
                        Log.i("settingssettings", "IsDarkMap: " + isDarkMap);
                        break;
                    case R.id.dark_radio: //se è checked il darkRadio
                        isDarkMap = true;

                        //metto nelle sharedPreferences
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
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            editor.putBoolean("darkMode", true).apply();
                            MainActivity.darkModeOn = isDarkModeOn;
                            //Log.i("darkdark", "dark1: " + isDarkModeOn);
                            //MyGiftTweetsAdapter.reloadFragment(fragment, getActivity());
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });

                    //Log.i("darkdark", "IsDarkModeOn IF: " + isDarkModeOn);
                }else {
                    isDarkModeOn = false;

                    darkModeOn.setVisibility(View.GONE);
                    darkModeOff.setVisibility(View.VISIBLE);

                    darkModeOff.playAnimation();

                    darkModeOff.addAnimatorListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            darkModeOff.setVisibility(View.GONE);
                            darkModeOn.setVisibility(View.VISIBLE);
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            editor.putBoolean("darkMode", false).apply();
                            MainActivity.darkModeOn = isDarkModeOn;
                            //Log.i("darkdark", "dark2: " + isDarkModeOn);
                            //MyGiftTweetsAdapter.reloadFragment(fragment, getActivity());
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    //Log.i("darkdark", "IsDarkModeOn ELSE: " + isDarkModeOn);
                }

            }
        });

        Log.i("darkdark", "IsDarkModeOn ELSE: " + isDarkModeOn);

        ImageView goBackButton = v.findViewById(R.id.go_back_profile_settings);

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String profileFragmentTag = getResources().getString(R.string.profile_fragment_tag);
                ProfileFragment profileFragment = new ProfileFragment();
                FragmentTransaction fragmentTransaction =  getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.bottomtotop, R.anim.toptobottom);
                fragmentTransaction.replace(R.id.fragment_container, profileFragment, profileFragmentTag).commit();
                fragmentTransaction.addToBackStack(profileFragmentTag);
                MainActivity.activeFragment = profileFragment;
            }
        });

        return v;
    }

}
