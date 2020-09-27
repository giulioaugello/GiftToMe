package com.LAM.GiftToMe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.LAM.GiftToMe.Fragment.ChatFragment;
import com.LAM.GiftToMe.Fragment.HomeFragment;
import com.LAM.GiftToMe.Fragment.NewGiftFragment;
import com.LAM.GiftToMe.Fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private HomeFragment homeFragment;
    private NewGiftFragment newGiftFragment;
    private ChatFragment chatFragment;
    private ProfileFragment profileFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //primo fragment
        homeFragment = new HomeFragment();
        activeFragment = homeFragment;
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, homeFragment).commit();



        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = homeFragment;

            switch (item.getItemId()){
                case R.id.nav_home:
                    if(homeFragment == null) {
                        homeFragment = new HomeFragment();
                    }
                    selectedFragment = homeFragment;
                    break;
                case R.id.nav_newGift:
                    if(newGiftFragment == null) {
                        newGiftFragment = new NewGiftFragment();
                    }
                    selectedFragment = newGiftFragment;
                    break;
                case R.id.nav_chat:
                    if(chatFragment == null) {
                        chatFragment = new ChatFragment();
                    }
                    selectedFragment = chatFragment;
                    break;
                case R.id.nav_profile:
                    if(profileFragment == null) {
                        profileFragment = new ProfileFragment();
                    }
                    selectedFragment = profileFragment;
                    break;
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();

            activeFragment = selectedFragment;

            return true;
        }
    };
}