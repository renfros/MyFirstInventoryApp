package com.example.inventory;


import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FragmentReports extends Fragment {

    private static final String TAG = "Fragment Categories";
    Button generate;
    PDFView mPdfView;
    ListView mlistView;
    EditText searchReports;

    //Shared preferences
    private final String SHARED_REF = "USER";
    private final String USER_EMAIL = "EMAIL";
    private final String USER_UID = "UID";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.fragment_reports,container,false);

        setUpView(view);

        return view;

    }

    private void setUpView(final View view){

        generate = (Button) view.findViewById(R.id.generate);
        mPdfView = view.findViewById(R.id.mPdfView);
        searchReports = (EditText)view.findViewById(R.id.searchReports);
        getFolderFromZip(view);
        setUpListView(view);

        setUpGenerateFunction(view);

    }


    private void getFolderFromZip(final View view){

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_REF,Context.MODE_PRIVATE);
        File zipFilePath = new File(new ContextWrapper(getActivity().getApplicationContext()).getDir(getActivity().getFilesDir().getName(),Context.MODE_PRIVATE),
                "Reports_" + sharedPreferences.getString(USER_UID,"") + ".zip");

        File folder = new File(new ContextWrapper(getActivity().getApplicationContext()).getDir(getActivity().getFilesDir().getName(),Context.MODE_PRIVATE),
                "Reports_" + sharedPreferences.getString(USER_UID,""));
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

    private void setUpListView(final View view){

        mlistView = (ListView) view.findViewById(R.id.reportView);
        ArrayList<String> array = new ArrayList<String>();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_REF,Context.MODE_PRIVATE);
        File root = new File(new ContextWrapper(getActivity().getApplicationContext()).getDir(getActivity().getFilesDir().getName(),Context.MODE_PRIVATE),
                "Reports_" + sharedPreferences.getString(USER_UID,""));
        if(root.exists()){
            File mFile[] = root.listFiles();

            for(File i : mFile){
                array.add(i.getName());
            }
        }

        Collections.sort(array, new Comparator<String>(){

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
            @Override
            public int compare(String o1, String o2) {
                try {
                    return -1 * (dateFormat.parse(o1).compareTo(dateFormat.parse(o2)));
                }catch(ParseException e){
                    Log.d("SORT","Could not sort dates");
                }
                return 0;
            }
        });
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_selectable_list_item,array);

        mlistView.setAdapter(adapter);
        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String fileName = mlistView.getItemAtPosition(position).toString();
                SharedPreferences sharedPreferences1 = getActivity().getSharedPreferences(SHARED_REF,Context.MODE_PRIVATE);
                File tempRoot = new File(new ContextWrapper(getActivity().getApplicationContext()).getDir(getActivity().getFilesDir().getName(),Context.MODE_PRIVATE),
                        "Reports_" + sharedPreferences1.getString(USER_UID,""));
                File tempFile = new File(tempRoot,fileName);
                if(tempFile.exists()){
                    mPdfView.fromFile(tempFile).load();
                }
            }
        });

        searchReports.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }


    private void setUpGenerateFunction(final View view){

        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = view.getContext().getSharedPreferences(SHARED_REF,Context.MODE_PRIVATE);
                File root = new File(new ContextWrapper(view.getContext().getApplicationContext()).getDir(view.getContext().getFilesDir().getName(), Context.MODE_PRIVATE),"Reports_"
                + sharedPreferences.getString(USER_UID,""));
                if (!root.exists()) {
                    root.mkdir();
                    Toast.makeText(view.getContext(), "Created new file", Toast.LENGTH_SHORT).show();
                }
                try {
                    //Date formatter
                    Date c = Calendar.getInstance().getTime();
                    System.out.println("Current time => " + c);

                    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                    final String formattedDate = df.format(c);
                    //END

                    //Create file
                    final File file = new File(root, formattedDate +".pdf");

                    if(file.exists()){
                        //IF FILE EXISTS ASK USER IF THEY WANT TO OVERWRITE IT
                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                        builder.setMessage("Looks like today's file already exists do you want to overwrite?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        try {
                                            createNewPdf(file,formattedDate,view);
                                        } catch (IOException e) {
                                            e.printStackTrace();
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
                    }else{
                        file.createNewFile();
                        createNewPdf(file,formattedDate,view);

                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                        builder.setMessage("File Created")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {


                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();

                    }

                } catch (Exception e) {



                }
            }
        });

    }

    private void createNewPdf(File file,String formattedDate,final View view) throws IOException{

        String tempCategory;

        PdfWriter writer = new PdfWriter(file.getAbsolutePath().toString());

        PdfDocument pdf = new PdfDocument(writer);

        Document document = new Document(pdf);

        Paragraph paragraph = new Paragraph("Date: " + formattedDate);

        //ADD Date
        document.add(paragraph);

        //Create tables

        try{
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_REF,Context.MODE_PRIVATE);
            File root = new File(new ContextWrapper(view.getContext().getApplicationContext()).getDir(view.getContext().getFilesDir().getName(), Context.MODE_PRIVATE),"");
            if (!root.exists()) {
                root.mkdir();
                Toast.makeText(view.getContext(), "Created new file", Toast.LENGTH_SHORT).show();
            }
            File thisFile = new File(root, "Categories_" + sharedPreferences.getString(USER_UID,""));

            Scanner scanner = new Scanner(thisFile);
            scanner.useDelimiter("`");
            while(scanner.hasNext()){
                tempCategory = scanner.next();
                Table category = new Table(new float[]{100});
                category.setWidthPercent(100);
                category.setHeight(50);
                category.setHorizontalAlignment(HorizontalAlignment.CENTER);
                category.addCell(new Cell().setBackgroundColor(Color.GRAY).add(new Paragraph(tempCategory).
                        setFontSize(20).setTextAlignment(TextAlignment.CENTER)));
                document.add(category);

                //GET THE INVENTORY ASSOCIATED WITH THE FILE
//                SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_REF,Context.MODE_PRIVATE);
                File mRoot = new File(new ContextWrapper(getActivity().getApplicationContext()).getDir(getActivity().getFilesDir().getName(), Context.MODE_PRIVATE),"Inventory_"
                + sharedPreferences.getString(USER_UID,""));
                mRoot.mkdir();
                try {
                    File mFile = new File(mRoot, "InventoryItems_"+ tempCategory);
                    Scanner read = new Scanner(mFile);
                    read.useDelimiter("`");
                    while(read.hasNext()){
                        Table inventory = new Table(new float[]{50,50});
                        inventory.setWidthPercent(100);
                        inventory.setHeight(40);
                        inventory.setHorizontalAlignment(HorizontalAlignment.CENTER);
                        inventory.addCell(new Cell().add(new Paragraph(read.next())));
                        inventory.addCell(new Cell().add(new Paragraph(read.next())));
                        document.add(inventory);
                    }

                } catch (Exception e) { }
            }
            scanner.close();
        }catch (FileNotFoundException e){

        }

        document.close();

        /*To display the file into the PDF view
        mPdfView.fromFile(file).load();

         */
        zipFile();
        updateArrayList();


    }

    private void zipFile(){

        //Create a new zip folder
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_REF,Context.MODE_PRIVATE);
        File root = new File(new ContextWrapper(getActivity().getApplicationContext()).getDir(getActivity().getFilesDir().getName(),Context.MODE_PRIVATE),
                "Reports_" + sharedPreferences.getString(USER_UID,"") + ".zip");
        //END
        //Create a file Object to get the folder holding the files needing to be zipped
        File folder = new File(new ContextWrapper(getActivity().getApplicationContext()).getDir(getActivity().getFilesDir().getName(),Context.MODE_PRIVATE),
                "Reports_" + sharedPreferences.getString(USER_UID,""));

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

            //create new File to store in firebase
            storeToZipFireBase();

        }catch(FileNotFoundException e){
            Log.d("FILE","Unable to zip file");
        }catch (IOException i){
            Log.d("FILE","Could not zip the files some error occurred");
        }

    }

    private void updateArrayList(){

        ArrayList<String> array = new ArrayList<String>();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_REF,Context.MODE_PRIVATE);
        File root = new File(new ContextWrapper(getActivity().getApplicationContext()).getDir(getActivity().getFilesDir().getName(),Context.MODE_PRIVATE),
                "Reports_" + sharedPreferences.getString(USER_UID,""));
        if(root.exists()){
            File mFile[] = root.listFiles();

            for(File i : mFile){
                array.add(i.getName());
            }
        }

        Collections.sort(array, new Comparator<String>(){

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
            @Override
            public int compare(String o1, String o2) {
                try {
                    return -1 * (dateFormat.parse(o1).compareTo(dateFormat.parse(o2)));
                }catch(ParseException e){
                    Log.d("SORT","Could not sort dates");
                }
                return 0;
            }
        });

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_selectable_list_item,array);
        mlistView.setAdapter(adapter);

        searchReports.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void storeToZipFireBase(){

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_REF,Context.MODE_PRIVATE);
        File mFile = new File(new ContextWrapper(getActivity().getApplicationContext()).getDir(getActivity().getFilesDir().getName(),Context.MODE_PRIVATE),
                "Reports_" + sharedPreferences.getString(USER_UID,"") + ".zip");

        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        Uri uri_file = Uri.fromFile(mFile);
        //find sharedPref
        StorageReference mRef = mStorageRef.child("UserDocs/" + sharedPreferences.getString(USER_UID,"") + "/Reports.zip");

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

    /*Save Here*/
    @Override
    public void onPause() {
        Log.d("REPORTS","On Pause");
        super.onPause();
    }

    /*Save Here*/
    @Override
    public void onStop() {
        Log.d("REPORTS","On Stop");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Log.d("REPORTS","On Destroy View");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.d("REPORTS","On Destroy");
        super.onDestroy();
    }
}
