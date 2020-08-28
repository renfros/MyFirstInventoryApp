package com.example.inventory;


import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.EventListener;

public class MainActivity extends AppCompatActivity {

    Button btnMoveToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//       getSupportActionBar().hide();
        
        setUpUi();
    }
    
    private void setUpUi(){
        
        btnMoveToLogin = (Button)findViewById(R.id.btnMoveToLogin);
        
        setUpButton();
    }
    
    private void setUpButton(){
        
        btnMoveToLogin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnMoveToLogin.setBackgroundResource(R.drawable.login_btn_pressed);
                        btnMoveToLogin.setTextColor(Color.parseColor("#FFFFFF"));
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        btnMoveToLogin.setBackgroundResource(R.drawable.yellow_button);
                        btnMoveToLogin.setTextColor(Color.parseColor("#EEFF00"));

                        startActivity(new Intent(MainActivity.this,LoginScreen.class));
                        break;       
                }
                return false;
            }
        });

    }
    
    

}
