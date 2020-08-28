package com.example.inventory;


import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FragmentInventory extends Fragment {

    private static final String TAG = "Fragment Categories";
    private static String categoryToSave;
    private static int checker = 0;

    //Shared preferences
    private final String TEMP_ITEM_LIST_NAME = "CATEGORY_NAME";
    private final String SHARED_REF = "USER";
    private final String USER_EMAIL = "EMAIL";
    private final String USER_UID = "UID";

    //Ui components
    Button btnAddNewInvItem;
    Button btnSaveInventory;
    EditText addNewInvItem;
    SwipeMenuListView list;

    //Spinner to move categories and change inventory items
    Spinner spnChangeItems;
    ArrayList<String> spinnerArrray;
    ArrayAdapter<String> spinnerAdapter;


    //In class items
    ArrayList<InventoryItems> array;
    CustomArrayAdapter adapter;
    File file;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.fragment_inventory,container,false);
        checker = 0;
        setUpUi(view);


        return view;

    }

    private void setUpUi(View view) {

        btnAddNewInvItem = (Button)view.findViewById(R.id.btnAddNewInvItem);
        btnSaveInventory = (Button)view.findViewById(R.id.btnSaveInventory);
        addNewInvItem = (EditText)view.findViewById(R.id.addNewInvItem);
        list = (SwipeMenuListView)view.findViewById(R.id.ListView);


        setUpListView(view);
        //Spinner
        setUpSpinner(view);
        //END
        setUpButton(view);

    }

    private void setUpButton(final View view) {

        btnAddNewInvItem.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){

                    case MotionEvent.ACTION_DOWN:
                        btnAddNewInvItem.setBackgroundResource(R.drawable.login_btn_pressed);
                        btnAddNewInvItem.setTextColor(Color.parseColor("#FFFFFF"));
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        btnAddNewInvItem.setBackgroundResource(R.drawable.yellow_button);
                        btnAddNewInvItem.setTextColor(Color.parseColor("#FFFFFF"));
                        if(addNewInvItem.getText().toString() != ""){
                            if (addNewInvItem.getText().toString() != "") {
                                array.add(new InventoryItems(addNewInvItem.getText().toString(), 0));
                                Collections.sort(array);
                                adapter = new CustomArrayAdapter(view.getContext(), R.layout.list_view_layout, array);
                                list.setAdapter(adapter);
                                addNewInvItem.setText("");

                            try{
                                //Change the file
                                FileWriter writer = new FileWriter(file);
                                for(InventoryItems item : array){
                                    writer.write(item.getName() + "`" + item.getAmount());
                                    writer.write("`");
                                }
                                writer.close();
                            }catch(IOException e){
                                Toast.makeText(view.getContext(), "An ERROR writing to file", Toast.LENGTH_SHORT).show();
                            }}

                        }

                        break;
                }

                return false;
            }
        });

        btnSaveInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToFirebase(view);
            }
        });

    }

    private void setUpSpinner(final View view) {

        spnChangeItems = (Spinner)view.findViewById(R.id.spnChangeitems);
        spinnerArrray = new ArrayList<String>();

        //Find the file holding the categories
        SharedPreferences sharedPreferences = view.getContext().getSharedPreferences(SHARED_REF,view.getContext().MODE_PRIVATE);
        File root = new File(new ContextWrapper(view.getContext().getApplicationContext()).getDir(view.getContext().getFilesDir().getName(), Context.MODE_PRIVATE),"");
        if (!root.exists()) {
            root.mkdir();
            Toast.makeText(view.getContext(), "Created new file", Toast.LENGTH_SHORT).show();
        }
        try {
            //File Location
            file = new File(root, "Categories_" + sharedPreferences.getString(USER_UID,""));
        } catch (Exception e) { }
        //END
        //READ THE FILE INFO INTO THE ARRAY
        try {
            Scanner read = new Scanner(file);
            read.useDelimiter("`");
            while(read.hasNext()){
                spinnerArrray.add(read.next());
            }
            read.close();
        }catch(IOException e){
            Toast.makeText(view.getContext(),"Failed to inventory from cloud",Toast.LENGTH_SHORT);
        }
        Collections.sort(spinnerArrray);
        spinnerAdapter = new ArrayAdapter<>(view.getContext(),android.R.layout.simple_list_item_1,spinnerArrray);
        spnChangeItems.setAdapter(spinnerAdapter);
        //END
        setUpSpinnerListener(view);

    }

    private void setUpSpinnerListener(final View view) {

        //Save the last after selected
        spnChangeItems.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //check if need to save

                String itemSelected = spnChangeItems.getItemAtPosition(position).toString();
                //Save the current array under the category it is
                checker += 1;
                if(checker > 1) {
                    autoSave(categoryToSave);
                }

                createNewItemView(itemSelected,view);

                categoryToSave = itemSelected;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void autoSave(String save){

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_REF,Context.MODE_PRIVATE);
        File root = new File(new ContextWrapper(getActivity().getApplicationContext()).getDir(getActivity().getFilesDir().getName(), Context.MODE_PRIVATE),"Inventory_"
        + sharedPreferences.getString(USER_UID,""));
        root.mkdir();
        try {

            //File Location
            file = new File(root, "InventoryItems_"+ save);

        } catch (Exception e) { }

        try{
            FileWriter writer = new FileWriter(file);
            for(InventoryItems item : array){
                writer.write(item.getName() + "`" + item.getAmount());
                writer.write("`");
            }
            writer.close();
        }catch(IOException e){
            //Toast.makeText(view.getContext(), "An ERROR writing to file", Toast.LENGTH_SHORT).show();
        }

    }



    private void createNewItemView(String itemSelected, final View view){

        array.clear();
        //changed to Context.MODE_PRIVATE
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_REF,Context.MODE_PRIVATE);
        File root = new File(new ContextWrapper(getActivity().getApplicationContext()).getDir(getActivity().getFilesDir().getName(), Context.MODE_PRIVATE),"Inventory_"
                +sharedPreferences.getString(USER_UID,""));
        root.mkdir();
        try {

            //File Location
            file = new File(root, "InventoryItems_"+ itemSelected);

        } catch (Exception e) { }
        //Read from the file
        try{
            Scanner scanner = new Scanner(file);
            scanner.useDelimiter("`");
            while(scanner.hasNext()){
                array.add(new InventoryItems(scanner.next(),Integer.parseInt(scanner.next())));
            }
            scanner.close();
        }catch (FileNotFoundException e){

        }

        //END
        adapter = new CustomArrayAdapter(getActivity(),R.layout.list_view_layout,array);
        list.setAdapter(adapter);

    }

    private void setUpListView(final View view) {

        array = new ArrayList<InventoryItems>();

        //END
        adapter = new CustomArrayAdapter(view.getContext(),R.layout.list_view_layout,array);
        list.setAdapter(adapter);

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
                        builder.setMessage("Are you sure you want to delete?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        array.remove(p);
                                        adapter = new CustomArrayAdapter(view.getContext(),R.layout.list_view_layout,array);
                                        list.setAdapter(adapter);
                                        //Change the file
                                        try{
                                            FileWriter writer = new FileWriter(file);
                                            for(InventoryItems item : array){
                                                writer.write(item.getName() + "`" + item.getAmount());
                                                writer.write("`");
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

    /*
    NEED TO FIRST ZIP THE FILE BEFORE SAVING TO FIREBASE
    WHEN WE LOAD THE INVENTORY ITEMS BACK FROM FIREBASE WE WILL DO SO IN THE LOADING SCREEN OR MAIN ACTIVITY
    THIS IS WHERE WE RECOVER ALL OF THE FILES NEEDED TO RUN THE APPLICATION
     */

    private void saveToFirebase(final View view){

        //zip the File
        zipFile();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_REF,Context.MODE_PRIVATE);
        File mFile = new File(new ContextWrapper(getActivity().getApplicationContext()).getDir(getActivity().getFilesDir().getName(),Context.MODE_PRIVATE),
                "Inventory_" + sharedPreferences.getString(USER_UID,"") + ".zip");

        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        Uri uri_file = Uri.fromFile(mFile);
        //find sharedPref
        StorageReference mRef = mStorageRef.child("UserDocs/" + sharedPreferences.getString(USER_UID,"") + "/Inventory.zip");

        mRef.putFile(uri_file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d("FIREBASE","Uploaded to firebase");
                        Toast.makeText(getActivity(), "Saved",Toast.LENGTH_SHORT).show();
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

    private void zipFile(){

        //Create a new zip folder
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_REF,Context.MODE_PRIVATE);
        File root = new File(new ContextWrapper(getActivity().getApplicationContext()).getDir(getActivity().getFilesDir().getName(),Context.MODE_PRIVATE),
                "Inventory_" + sharedPreferences.getString(USER_UID,"") + ".zip");
        //END
        //Create a file Object to get the folder holding the files needing to be zipped
        File folder = new File(new ContextWrapper(getActivity().getApplicationContext()).getDir(getActivity().getFilesDir().getName(),Context.MODE_PRIVATE),
                "Inventory_" + sharedPreferences.getString(USER_UID,""));

        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(root));
            for(File currentFile : folder.listFiles()){
                //save to zos
                zos.putNextEntry(new ZipEntry(currentFile.getName()));
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(currentFile));
                long bytesRead = 0;
                byte[] bytesIn = new byte[4096];
                int read = 0;
                while((read = bis.read(bytesIn)) != -1) {
                    zos.write(bytesIn, 0, read);
                    bytesRead += read;
                }
                zos.closeEntry();
            }
            zos.flush();
            zos.close();

//            //create new File to store in firebase
//            storeToZipFireBase();

        }catch(FileNotFoundException e){
            Log.d("FILE","Unable to zip file");
        }catch (IOException i){
            Log.d("FILE","Could not zip the files some error occurred");
        }

    }

    @Override
    public void onResume() {
        //Update the Iventory selections
        spinnerArrray.clear();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_REF,Context.MODE_PRIVATE);
        File root = new File(new ContextWrapper(getActivity().getApplicationContext()).getDir(getActivity().getFilesDir().getName(), Context.MODE_PRIVATE),"");
        if (!root.exists()) {
            root.mkdir();
            Toast.makeText(getActivity(), "Created new file", Toast.LENGTH_SHORT).show();
        }
        try {
            //File Location
            File mfile = new File(root, "Categories_" + sharedPreferences.getString(USER_UID,""));
        //END
        //READ THE FILE INFO INTO THE ARRAY

            Scanner read = new Scanner(mfile);
            read.useDelimiter("`");
            while(read.hasNext()){
                spinnerArrray.add(read.next());
            }
            read.close();
        }catch(IOException e){
            Toast.makeText(getActivity(),"Failed to inventory from cloud",Toast.LENGTH_SHORT);
        }catch (Exception e) { }
        Collections.sort(spinnerArrray);
        spinnerAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,spinnerArrray);
        spnChangeItems.setAdapter(spinnerAdapter);

        Log.d("INVENTORY","On resume");
        super.onResume();
    }

    @Override
    public void onStart() {
        Log.d("INVENTORY","On start");
        super.onStart();
    }

    @Override
    public void onPause() {
        autoSave(categoryToSave);
        Log.d("INVENTORY","inventory auto saved in on pause");
        super.onPause();
    }

    @Override
    public void onStop() {
        autoSave(categoryToSave);
        Log.d("INVENTORY","inventory auto saved in on stop");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
