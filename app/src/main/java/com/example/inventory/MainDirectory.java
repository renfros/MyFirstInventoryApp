package com.example.inventory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toolbar;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.EventListener;

public class MainDirectory extends AppCompatActivity {

    TabLayout tabs;
    ViewPager2 mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_directory);
        setUpUi();

    }

    private void setUpUi(){

        mViewPager = findViewById(R.id.container);
        tabs = findViewById(R.id.tabs);

        mViewPager.setAdapter(new ViewPager2Adapter(MainDirectory.this));
        mViewPager.setUserInputEnabled(false);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(
                tabs, mViewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {

                //setting the tab names
                //can also update the icon and badges(ie BadgeDrawable)
                switch(position){

                    case 0:
                        tab.setText("Categories");
                        break;
                    case 1:
                        tab.setText("Reports");
                        break;
                    case 2:
                        tab.setText("Inventory");
                        break;

                }

            }
        }
        );
        tabLayoutMediator.attach();

    }


}
