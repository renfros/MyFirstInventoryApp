package com.example.inventory;


import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class FragmentCategories extends Fragment {

    private static final String TAG = "Fragment Categories";

    private final String TEMP_ITEM_LIST_NAME = "CATEGORY_NAME";
    private final String SHARED_REF = "USER";
    private final String USER_EMAIL = "EMAIL";
    private final String USER_UID = "UID";

    File file;
    Scanner read;
    FileWriter writer;
    Button btnAddCat;
    Button btnSaveCat;
    EditText newCat;
    Spinner spnCat;
    ArrayAdapter<String> adapter;
    ArrayList<String> array;
    SwipeMenuListView list;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.fragment_categories,container,false);

        setUpUi(view);

        return view;

    }

    private void setUpUi(final View view) {

        btnAddCat = (Button)view.findViewById(R.id.btnAddCat);
        btnSaveCat = (Button)view.findViewById(R.id.btnSaveCat);
        newCat = (EditText)view.findViewById(R.id.newCat);
//        spnCat = (Spinner)view.findViewById(R.id.spnCat);
        list = (SwipeMenuListView) view.findViewById(R.id.catListView);

        setUpFile(view);
        setUpAdaptor(view);
        setupButton(view);
        setUpSave(view);
        /*NO NEED FOR THIS METHOD*/
        //createInvItemListFromFile(view);
        setUpSpinnerActions(view);

    }



    private void setUpFile(final View view) {

        SharedPreferences sharedPreferences = view.getContext().getSharedPreferences(SHARED_REF,Context.MODE_PRIVATE);
//          File root = new File(this.getFilesDir(), "UserDocs/"+sharedPreferences.getString(USER_UID,""));
        File root = new File(new ContextWrapper(view.getContext().getApplicationContext()).getDir(view.getContext().getFilesDir().getName(), Context.MODE_PRIVATE),"");
        if (!root.exists()) {
            root.mkdir();
            Toast.makeText(view.getContext(), "Created new file", Toast.LENGTH_SHORT).show();
        }
        try {

            //File Location
            file = new File(root, "Categories_" + sharedPreferences.getString(USER_UID,""));

        } catch (Exception e) { }

    }

    private void setUpAdaptor(final View view){

        array = new ArrayList<String>();
        try {
            read = new Scanner(file);
            read.useDelimiter("`");
            while(read.hasNext()){
                array.add(read.next());
            }
            read.close();
        }catch(IOException e){
            Toast.makeText(view.getContext(),"Failed to inventory from cloud",Toast.LENGTH_SHORT);
        }
        Collections.sort(array);
        adapter = new ArrayAdapter<String>(view.getContext(),android.R.layout.simple_list_item_1,array);
        list.setAdapter(adapter);
//        spnCat.setAdapter(adapter);

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        view.getContext().getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(340);
                // set a icon
                deleteItem.setIcon(R.drawable.ic_delete_forever_red_no_background);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        list.setMenuCreator(creator);

        list.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        final int p = position;
                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                        builder.setMessage("Removing this category will also remove the inventory associated with it. Are you sure you want to delete?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        String fileToDelete = array.get(p);
                                        array.remove(p);
                                        adapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_list_item_1,array);
                                        list.setAdapter(adapter);
                                        //Change the file
                                        try{
                                            //Remove the Inventory file
                                            SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_REF,Context.MODE_PRIVATE);
                                            File tempRoot = new File(new ContextWrapper(getActivity().getApplicationContext()).getDir(getActivity().getFilesDir().getName(), Context.MODE_PRIVATE),"Inventory_"
                                                    +sharedPreferences.getString(USER_UID,""));
                                            if(tempRoot.exists()) {
                                                try {

                                                    //File Location
                                                    File tempFile = new File(tempRoot, "InventoryItems_" + fileToDelete);
                                                    if(tempFile.exists()) {
                                                        tempFile.delete();
                                                    }

                                                } catch (Exception e) {
                                                }
                                            }
                                            //Update existing Categories
                                            FileWriter writer = new FileWriter(file);
                                            for(String item : array){
                                                writer.write(item + "`");
                                            }
                                            writer.close();
                                        }catch(IOException e){
                                            Toast.makeText(view.getContext(), "An ERROR writing to file", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();


                        break;
                    case 1:
                        // delete
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });

    }

    private void setupButton(final View view){

        btnAddCat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!newCat.getText().toString().equals("")) {
                    array.add(newCat.getText().toString());
                    Collections.sort(array);
                    adapter.notifyDataSetChanged();
                    newCat.setText("");

                    try{
                        FileWriter fileWriter = new FileWriter(file);
                        for(String i : array){
                            fileWriter.write(i);
                            fileWriter.write("`");
                        }
                        fileWriter.close();

                    }catch(IOException e) {

                    }
                }else{

                }
            }
        });

    }

    private void setUpSave(final View view){

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
                    saveFile(file,view);
                }catch(IOException e){
                    Toast.makeText(view.getContext(), "An ERROR writing to file", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void saveFile(File mFile, final View view){

        //Store the File in Firebase
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        Uri uri_file = Uri.fromFile(mFile);
        //find sharedPref
        SharedPreferences sharedPreferences = view.getContext().getSharedPreferences(SHARED_REF,Context.MODE_PRIVATE);
        StorageReference mRef = mStorageRef.child("UserDocs/" + sharedPreferences.getString(USER_UID,"") + "/Categories.txt");

        mRef.putFile(uri_file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d("FIREBASE","Uploaded to firebase");
                        Toast.makeText(view.getContext(), "Saved",Toast.LENGTH_SHORT).show();
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

        //May not even need this if I update the list view on resume in the inventory fragment
        //this was temporary resolution to updating the inventory but we are now instead doing this in onResume
//        getActivity().recreate();

    }

    private void createInvItemListFromFile(View view) {

//        try{
//            Scanner scanner = new Scanner(file);
//            scanner.useDelimiter("`");
//            ArrayList<String> arrayStr = new ArrayList<String>();
//            while(scanner.hasNext()){
//
//                arrayStr.add(scanner.next());
//
//            }
//
//            scanner.close();
//            //Send the item to be created
//            createInvItems(arrayStr,view);
//        }catch(IOException e){
//
//        }

    }

    private void createInvItems(ArrayList<String> arrayStr, final View view){

        File tempFile;

//        for(String i : arrayStr){
//
//            SharedPreferences sharedPreferences = view.getContext().getSharedPreferences(SHARED_REF,Context.MODE_PRIVATE);
//            File stem = new File(new ContextWrapper(view.getContext().getApplicationContext()).getDir(view.getContext().getFilesDir().getName(),Context.MODE_PRIVATE),"");
//            if (!stem.exists()) {
//                stem.mkdir();
//                Toast.makeText(view.getContext(), "Created new file", Toast.LENGTH_SHORT).show();
//            }
//            try {
//
//                //File Location
//                tempFile = new File(stem, "InventoryItems_" + i + "_" + sharedPreferences.getString(USER_UID,""));
//                if(!tempFile.exists()) {
//                    tempFile.createNewFile();
//                }
//                Log.d("FILE", "File created for inventory items under: " + i);
//
//            } catch (Exception e) {
//                Log.d("FILE","Failed to create new inventory file for: " + i);
//            }
//
//        }
    }


    private void setUpSpinnerActions(final View view) {

//        spnCat.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//
//                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
//                builder.setMessage("Are you sure you want to delete?")
//                        .setCancelable(false)
//                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                array.remove(p);
//                                adapter = new CustomArrayAdapter(view.getContext(),R.layout.list_view_layout,array);
//                                list.setAdapter(adapter);
//                                //Change the file
//                                try{
//                                    FileWriter writer = new FileWriter(file);
//                                    for(InventoryItems item : array){
//                                        writer.write(item.getName());
//                                        writer.write("`");
//                                    }
//                                    writer.close();
//                                }catch(IOException e){
//                                    Toast.makeText(view.getContext(), "An ERROR writing to file", Toast.LENGTH_SHORT).show();
//                                }
//
//                            }
//                        })
//                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.cancel();
//                            }
//                        });
//                AlertDialog alert = builder.create();
//                alert.show();
//
//                return false;
//            }
//        });

    }
}
