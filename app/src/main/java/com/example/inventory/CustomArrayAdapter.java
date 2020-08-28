package com.example.inventory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class CustomArrayAdapter extends ArrayAdapter<InventoryItems> {

    private static final String TAG = "InventoryListAdapter";

    private Context mContext;
    private int mResource;

    public CustomArrayAdapter(@NonNull Context context, int resource, @NonNull ArrayList<InventoryItems> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        String name = getItem(position).getName();
        int amount = getItem(position).getAmount();

        //Create a person object with the Information
        //InventoryItems items = new InventoryItems(name,amount);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource,parent,false);

        TextView invItem = (TextView)convertView.findViewById(R.id.txtItem);
        final TextView itemAmount = (TextView)convertView.findViewById(R.id.txtNumItem);
        Button btnAdd = (Button)convertView.findViewById(R.id.btnplus);
        Button btnMinus = (Button)convertView.findViewById(R.id.btnminus);

        invItem.setText(name);
        itemAmount.setText(Integer.toString(amount));

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int num = Integer.parseInt(itemAmount.getText().toString());
                num += 1;
                getItem(position).setAmount(num);
                itemAmount.setText(Integer.toString(num));
            }
        });

        btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int num = Integer.parseInt(itemAmount.getText().toString());
                if(num > 0) {
                    num -= 1;
                    getItem(position).setAmount(num);
                    itemAmount.setText(Integer.toString(num));
                }
            }
        });


        return convertView;
    }
}
