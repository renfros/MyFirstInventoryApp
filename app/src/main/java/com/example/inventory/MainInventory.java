package com.example.inventory;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class MainInventory extends AppCompatActivity {

    //Shared preferences
    private final String TEMP_ITEM_LIST_NAME = "CATEGORY_NAME";
    private final String SHARED_REF = "USER";
    private final String USER_EMAIL = "EMAIL";
    private final String USER_UID = "UID";

    //Ui components
    Button btnAddNewInvItem;
    EditText addNewInvItem;
    SwipeMenuListView list;
    ImageView imgBack;

    //Spinner to move categories and change inventory items
    Spinner spnChangeItems;
    ArrayList<String> spinnerArrray;
    ArrayAdapter<String> spinnerAdapter;


    //In class items
    ArrayList<InventoryItems> array;
    CustomArrayAdapter adapter;
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_inventory);

//        getSupportActionBar().hide();
//
        setUpUi();
    }

    private void setUpUi(){

        btnAddNewInvItem = (Button)findViewById(R.id.btnAddNewInvItem);
        addNewInvItem = (EditText)findViewById(R.id.addNewInvItem);
        list = (SwipeMenuListView)findViewById(R.id.ListView);
        imgBack = (ImageView)findViewById(R.id.imgBack);


        setUpListView();
        //Spinner
        setUpSpinner();
        //END
        setUpButton();

    }

    private void setUpSpinner(){

        spnChangeItems = (Spinner)findViewById(R.id.spnChangeitems);
        spinnerArrray = new ArrayList<String>();

        //Find the file holding the categories
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_REF,MODE_PRIVATE);
        File root = new File(new ContextWrapper(getApplicationContext()).getDir(getFilesDir().getName(),Context.MODE_PRIVATE),"");
        if (!root.exists()) {
            root.mkdir();
            Toast.makeText(this, "Created new file", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(MainInventory.this,"Failed to inventory from cloud",Toast.LENGTH_SHORT);
        }
        Collections.sort(spinnerArrray);
        spinnerAdapter = new ArrayAdapter<>(MainInventory.this,android.R.layout.simple_list_item_1,spinnerArrray);
        spnChangeItems.setAdapter(spinnerAdapter);
        //END
        setUpSpinnerListener();

    }

    private void setUpSpinnerListener(){

        spnChangeItems.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String itemSelected = spnChangeItems.getItemAtPosition(position).toString();
                createNewItemView(itemSelected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void createNewItemView(String itemSelected){

        array.clear();
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_REF,MODE_PRIVATE);
        File root = new File(new ContextWrapper(getApplicationContext()).getDir(getFilesDir().getName(), Context.MODE_PRIVATE),"");
        try {

            //File Location
            file = new File(root, "InventoryItems_"+ itemSelected
                    + "_" + sharedPreferences.getString(USER_UID,""));

        } catch (Exception e) { }
        //Read from the file
        try{
            Scanner scanner = new Scanner(file);
            scanner.useDelimiter("`");
            while(scanner.hasNext()){
                array.add(new InventoryItems(scanner.next(),0));
            }
            scanner.close();
        }catch (FileNotFoundException e){

        }

        //END
        adapter = new CustomArrayAdapter(MainInventory.this,R.layout.list_view_layout,array);
        list.setAdapter(adapter);

    }

    private void setUpListView(){

        //Set up the array elements
        array = new ArrayList<InventoryItems>();
            //Get The file
//            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_REF,MODE_PRIVATE);
//            File root = new File(new ContextWrapper(getApplicationContext()).getDir(getFilesDir().getName(), Context.MODE_PRIVATE),"");
//            try {
//
//                //File Location
//                file = new File(root, "InventoryItems_"+ sharedPreferences.getString(TEMP_ITEM_LIST_NAME,"")
//                        + "_" + sharedPreferences.getString(USER_UID,""));
//
//            } catch (Exception e) { }
//            //Read from the file
//        try{
//            Scanner scanner = new Scanner(file);
//            scanner.useDelimiter("`");
//                    while(scanner.hasNext()){
//                        array.add(new InventoryItems(scanner.next(),0));
//                    }
//                    scanner.close();
//        }catch (FileNotFoundException e){
//
//        }

        //END
        adapter = new CustomArrayAdapter(MainInventory.this,R.layout.list_view_layout,array);
        list.setAdapter(adapter);

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainInventory.this);
                        builder.setMessage("Are you sure you want to delete?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        array.remove(p);
                                        adapter = new CustomArrayAdapter(MainInventory.this,R.layout.list_view_layout,array);
                                        list.setAdapter(adapter);
                                        //Change the file
                                        try{
                                            FileWriter writer = new FileWriter(file);
                                            for(InventoryItems item : array){
                                                writer.write(item.getName());
                                                writer.write("`");
                                            }
                                            writer.close();
                                        }catch(IOException e){
                                            Toast.makeText(MainInventory.this, "An ERROR writing to file", Toast.LENGTH_SHORT).show();
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

                        /*
                        temporary cant figure out how to update the element
                         */

//                            arrayInv.remove(position);
//                            adapter = new CustomInventoryAdapter(MainActivity.this,R.layout.custome_swipe_menu_layout,arrayInv);
//                            listView.setAdapter(adapter);

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

    private void setUpButton(){

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
                            array.add(new InventoryItems(addNewInvItem.getText().toString(),0));
                            Collections.sort(array);
                            adapter = new CustomArrayAdapter(MainInventory.this,R.layout.list_view_layout,array);
                            list.setAdapter(adapter);

                            try{
                                //Change the file
                                FileWriter writer = new FileWriter(file);
                                for(InventoryItems item : array){
                                    writer.write(item.getName());
                                    writer.write("`");
                                }
                                writer.close();
                            }catch(IOException e){
                                Toast.makeText(MainInventory.this, "An ERROR writing to file", Toast.LENGTH_SHORT).show();
                            }

                        }

                        break;
                }

                return false;
            }
        });

        imgBack.setClickable(true);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

}
