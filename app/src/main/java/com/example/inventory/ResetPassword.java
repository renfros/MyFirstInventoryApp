package com.example.inventory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPassword extends AppCompatActivity {

        Button btnReset;
        EditText txtReset;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_reset_password);

            setUpUi();
        }

        private void setUpUi(){

            btnReset = (Button)findViewById(R.id.btnReset);
            txtReset = (EditText)findViewById(R.id.txtReset);

            setUpBtnReset();
        }

        private void setUpBtnReset(){

            btnReset.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            btnReset.setBackgroundResource(R.drawable.login_btn_pressed);
                            btnReset.setTextColor(Color.parseColor("#FFFFFF"));
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            btnReset.setBackgroundResource(R.drawable.custom_login_button);
                            btnReset.setTextColor(Color.parseColor("#EEFF00"));

                            FirebaseAuth auth = FirebaseAuth.getInstance();

                            auth.sendPasswordResetEmail(txtReset.getText().toString())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // do something when mail was sent successfully.
                                                AlertDialog.Builder builder = new AlertDialog.Builder(ResetPassword.this);
                                                builder.setMessage("Password reset link sent to email")
                                                        .setCancelable(false);
                                                builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
//                                                        Intent intent = new Intent(ResetPassword.this,MainActivity.class);
//                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                });
                                                AlertDialog alert = builder.create();
                                                alert.show();
                                            } else {
                                                // ...
                                                AlertDialog.Builder builder = new AlertDialog.Builder(ResetPassword.this);
                                                builder.setMessage("Email does not exist")
                                                        .setCancelable(false);
                                                builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
//                                                        Intent intent = new Intent(ResetPassword.this,MainActivity.class);
//                                                        startActivity(intent);
//                                                        finish();
                                                    }
                                                });
                                                AlertDialog alert = builder.create();
                                                alert.show();
                                            }
                                        }
                                    });

                            break;
                    }
                    return false;
                }
            });

        }



    }