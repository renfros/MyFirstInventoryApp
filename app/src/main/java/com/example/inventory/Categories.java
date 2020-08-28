package com.example.inventory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Categories extends AppCompatActivity {

    private int check = 0;
    //Shared prefs
    private final String TEMP_ITEM_LIST_NAME = "CATEGORY_NAME";
    private final String SHARED_REF = "USER";
    private final String USER_EMAIL = "EMAIL";
    private final String USER_UID = "UID";

    File file;
    Scanner read;
    FileWriter writer;
    Button btnAddCat;
    Button btnSaveCat;
    Button btnBackToDir;
    EditText newCat;
    Spinner spnCat;
    ArrayAdapter<String> adapter;
    ArrayList<String> array;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

//        getSupportActionBar().hide();
        setUpUi();
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_REF,MODE_PRIVATE);

        Toast.makeText(this,"Logged in",Toast.LENGTH_LONG).show();
    }

    private void setUpUi(){

        btnAddCat = (Button)findViewById(R.id.btnAddCat);
        btnSaveCat = (Button)findViewById(R.id.btnSaveCat);
        btnBackToDir = (Button)findViewById(R.id.btnBackToDir);
        newCat = (EditText)findViewById(R.id.newCat);
        spnCat = (Spinner)findViewById(R.id.spnCat);

        setUpFile();
        setUpAdaptor();
        setupButton();
        setUpSave();
        createInvItemListFromFile();
        setUpSpinnerActions();
    }

    private void setUpAdaptor(){
        //create Array first
        array = new ArrayList<String>();
                        try {
                            read = new Scanner(file);
                            read.useDelimiter("`");
                            while(read.hasNext()){
                                array.add(read.next());
                            }
                            read.close();
                        }catch(IOException e){
                            Toast.makeText(Categories.this,"Failed to inventory from cloud",Toast.LENGTH_SHORT);
                        }
        Collections.sort(array);
        adapter = new ArrayAdapter<String>(Categories.this,R.layout.support_simple_spinner_dropdown_item,array);
        spnCat.setAdapter(adapter);

    }


    private void setUpFile(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_REF,MODE_PRIVATE);
//          File root = new File(this.getFilesDir(), "UserDocs/"+sharedPreferences.getString(USER_UID,""));
        File root = new File(new ContextWrapper(getApplicationContext()).getDir(getFilesDir().getName(),Context.MODE_PRIVATE),"");
          if (!root.exists()) {
              root.mkdir();
              Toast.makeText(this, "Created new file", Toast.LENGTH_SHORT).show();
          }
          try {

              //File Location
              file = new File(root, "Categories_" + sharedPreferences.getString(USER_UID,""));

          } catch (Exception e) { }

    }

    private void setupButton(){

        btnAddCat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!newCat.getText().toString().equals("")) {
                    array.add(newCat.getText().toString());
                    Collections.sort(array);
                    adapter.notifyDataSetChanged();
                    newCat.setText("");
                }else{

                }
            }
        });

        btnBackToDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Categories.this,MainDirectory.class));
            }
        });

    }
    private void setUpSave(){
        btnSaveCat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try{
                    writer = new FileWriter(file);
                    for(String str : array){
                        writer.write(str);
                        writer.write("`");
                    }
                    writer.close();
                    //Save the file
                   saveFile(file);
                }catch(IOException e){
                    Toast.makeText(Categories.this, "An ERROR writing to file", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void createInvItemListFromFile(){

        try{
            Scanner scanner = new Scanner(file);
            scanner.useDelimiter("`");
            ArrayList<String> arrayStr = new ArrayList<String>();
            while(scanner.hasNext()){

                arrayStr.add(scanner.next());

            }

            scanner.close();
            //Send the item to be created
            createInvItems(arrayStr);
        }catch(IOException e){

        }

    }

    private void createInvItems(ArrayList<String> arrayStr){

        File tempFile;

        for(String i : arrayStr){

            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_REF,MODE_PRIVATE);
            File stem = new File(new ContextWrapper(getApplicationContext()).getDir(getFilesDir().getName(),Context.MODE_PRIVATE),"");
            if (!stem.exists()) {
                stem.mkdir();
                Toast.makeText(this, "Created new file", Toast.LENGTH_SHORT).show();
            }
            try {

                //File Location
                tempFile = new File(stem, "InventoryItems_" + i + "_" + sharedPreferences.getString(USER_UID,""));
                if(!tempFile.exists()) {
                    tempFile.createNewFile();
                }
                Log.d("FILE", "File created for inventory items under: " + i);

            } catch (Exception e) {
                Log.d("FILE","Failed to create new inventory file for: " + i);
            }

        }


    }


    private void saveFile(File mFile){

        //Store the File in Firebase
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        Uri uri_file = Uri.fromFile(mFile);
        //find sharedPref
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_REF,MODE_PRIVATE);
        StorageReference mRef = mStorageRef.child("UserDocs/" + sharedPreferences.getString(USER_UID,"") + "/Categories.txt");

        mRef.putFile(uri_file)
        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("FIREBASE","Uploaded to firebase");
                Toast.makeText(Categories.this, "Saved",Toast.LENGTH_SHORT).show();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        Log.d("FIREBASE","Unsuccessful upload to firebase");
                    }
                });


    }

    private void setUpSpinnerActions() {

        spnCat.setSelection(0, false);
        spnCat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Put the users category in shared preferences so the inventory
                //Knows what file name to read

                String tempSelected = spnCat.getItemAtPosition(position).toString();
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_REF, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(TEMP_ITEM_LIST_NAME, tempSelected);
                editor.apply();

                startActivity(new Intent(Categories.this, MainInventory.class));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
//        spnCat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                     String tempSelected = spnCat.getItemAtPosition(position).toString();
//                    SharedPreferences sharedPreferences = getSharedPreferences(SHARED_REF, MODE_PRIVATE);
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.putString(TEMP_ITEM_LIST_NAME, tempSelected);
//                    editor.apply();
//
//                    startActivity(new Intent(Categories.this, MainInventory.class));
//            }
//        });
//
    }

}
