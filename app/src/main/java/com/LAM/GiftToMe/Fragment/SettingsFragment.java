package com.LAM.GiftToMe.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.LAM.GiftToMe.R;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    private LottieAnimationView switchColorMap, darkModeOn;
    private SwitchMaterial switchDarkMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.settings_fragment, container, false);

        switchColorMap = v.findViewById(R.id.animationSwitch);
        darkModeOn = v.findViewById(R.id.animationDarkMode);

        switchDarkMode = v.findViewById(R.id.switch_dark_mode);

        switchColorMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchColorMap.playAnimation();
            }
        });

        switchDarkMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                darkModeOn.playAnimation();
            }
        });

        return v;
    }
}
