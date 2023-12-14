package com.example.ecopulse;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;

import com.google.android.gms.maps.model.Marker;

import org.w3c.dom.Text;

import java.util.List;

public class TimeslotAdapter extends ArrayAdapter<String> {
    private List<String> items;
    private View fragment;

    public TimeslotAdapter( Context context, List<String> items, View fragment) {
        super(context, 0, items);
        this.items = items;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createCustomView(position, convertView, parent);
    }

    private View createCustomView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.timeslot_list_item, parent, false);
        }

        TextView slot = convertView.findViewById(R.id.timeslot_item);
        AppCompatImageButton delete = convertView.findViewById(R.id.delete_btn);

        String item = getItem(position);

        if (item != null) {
            slot.setText(item);
            Log.d("debug1", items.toString());
            Log.d("debug2", item + " debug " + items.toString());
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("debug1", items.toString());
                    items.remove(item);
                    notifyDataSetChanged();
                    Log.d("debug2", item + " debug " + items.toString());
                }
            });
        }



        return convertView;
    }


}
