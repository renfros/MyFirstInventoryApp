package com.example.inventory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class LoginScreen extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private final String SHARED_REF = "USER";
    private final String USER_EMAIL = "EMAIL";
    private final String USER_UID = "UID";

    ProgressBar pBar;
    Button btnLogin;
    EditText txtEmail;
    EditText txtPassword;
    TextView txtForgotPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
//        getSupportActionBar().hide();

        setUpFireAuth();
        setUpUi();
    }

    private void setUpFireAuth(){
        mAuth = FirebaseAuth.getInstance();
    }

    private void setUpUi(){

        btnLogin = (Button)findViewById(R.id.btnLogin);
        txtEmail = (EditText)findViewById(R.id.txtEmail);
        txtPassword = (EditText)findViewById(R.id.txtPassword);
        txtForgotPassword = (TextView)findViewById(R.id.txtForgotPassword);
        pBar = (ProgressBar)findViewById(R.id.pBar);

        setUpLoginButton();
        setUpForgotPassword();

    }

    private void setUpForgotPassword(){

        txtForgotPassword.setClickable(true);
        txtForgotPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        txtForgotPassword.setTextColor(Color.parseColor("#000000"));
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        txtForgotPassword.setTextColor(Color.parseColor("#2C00EE"));

                        Intent intent = new Intent(LoginScreen.this, ResetPassword.class);
                        startActivity(intent);
                }
                return false;
            }
        });
    }

    private void setUpLoginButton(){

        btnLogin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        btnLogin.setBackgroundResource(R.drawable.login_btn_pressed);
                        btnLogin.setTextColor(Color.parseColor("#FFFFFF"));
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        btnLogin.setBackgroundResource(R.drawable.yellow_button);
                        btnLogin.setTextColor(Color.parseColor("#EEFF00"));

                        String email = txtEmail.getText().toString();
                        String password = txtPassword.getText().toString();

                        txtEmail.setText("");
                        txtPassword.setText("");
                        pBar.setVisibility(View.VISIBLE);
                        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
//                                   Toast.makeText(MainActivity.this, "Login successful: " + mAuth.getCurrentUser().getEmail().toString(), Toast.LENGTH_SHORT).show();

                                    //Save to shared preferences
                                    SharedPreferences sharedPreferences = getSharedPreferences(SHARED_REF,MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString(USER_EMAIL,mAuth.getCurrentUser().getEmail().toString());
                                    editor.putString(USER_UID,mAuth.getCurrentUser().getUid().toString());
                                    editor.apply();

                                    //get user data
                                    getUserData();

                                    startActivity(new Intent(getApplicationContext(),MainDirectory.class));
                                    pBar.setVisibility(View.INVISIBLE);
                                }else{
                                    Toast.makeText(LoginScreen.this,"Failed to login",Toast.LENGTH_SHORT).show();
                                    pBar.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                        break;
                }
                return false;
            }
        });
    }

    private void getUserData(){

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_REF,MODE_PRIVATE);
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File directory = contextWrapper.getDir(getFilesDir().getName(), Context.MODE_PRIVATE);
        if(!directory.exists()){
            directory.mkdir();
            Log.d("FILE","Directory created");
        }
        File file = new File(directory,"Categories_" + sharedPreferences.getString(USER_UID,""));
        File reports = new File(directory,"Reports_" + sharedPreferences.getString(USER_UID,"") + ".zip");
        File inventory = new File(directory,"Inventory_" + sharedPreferences.getString(USER_UID,"") + ".zip");



        //get the link for the download
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference mRef = storageReference.child("UserDocs/" + sharedPreferences.getString(USER_UID,"") + "/Categories.txt");
        StorageReference reportsRef = storageReference.child("UserDocs/" + sharedPreferences.getString(USER_UID,"") + "/Reports.zip");
        StorageReference inventoryRef = storageReference.child("UserDocs/" + sharedPreferences.getString(USER_UID,"") + "/Inventory.zip");


        mRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.d("FIREBASE","file downloaded from firebase");
                //Toast.makeText(Categories.this,"Downloaded file data",Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("FIREBASE","Failed to download file information" + e.getStackTrace().toString());
                // Toast.makeText(Categories.this,"Failed to download the file",Toast.LENGTH_LONG).show();
            }
        });

        reportsRef.getFile(reports).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.d("FIREBASE","Retrieved reports from firebase");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("FIREBASE","failed to retrieve reports");
            }
        });
        inventoryRef.getFile(inventory).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.d("FIREBASE","Retrieved inventory from firebase");
                unzip();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("FIREBASE","failed to retrieve inventory");
            }
        });


    }

    private void unzip(){

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_REF,Context.MODE_PRIVATE);
        File zipFilePath = new File(new ContextWrapper(getApplicationContext()).getDir(getFilesDir().getName(),Context.MODE_PRIVATE),
                "Inventory_" + sharedPreferences.getString(USER_UID,"") + ".zip");

        File folder = new File(new ContextWrapper(getApplicationContext()).getDir(getFilesDir().getName(),Context.MODE_PRIVATE),
                "Inventory_" + sharedPreferences.getString(USER_UID,""));
        if(!folder.exists()){
            folder.mkdir();
        }

        try {
            ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
            ZipEntry entry = zipIn.getNextEntry();

            while (entry != null) {

                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(folder.getAbsolutePath().toString() + File.separator + entry.getName()));
                byte[] bytesIn = new byte[4096];
                int read = 0;
                while ((read = zipIn.read(bytesIn)) != -1) {
                    bos.write(bytesIn, 0, read);
                }
                bos.close();

                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
            zipIn.close();

        }catch(FileNotFoundException e){
            Log.d("FILE","Failed to find zip file");
        }catch(IOException e){
            Log.d("FILE","Some error occurred while unzipping");
        }

    }

}
