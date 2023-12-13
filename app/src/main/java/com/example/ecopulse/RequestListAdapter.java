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

import com.google.android.gms.maps.model.Marker;

import org.w3c.dom.Text;

import java.util.List;

public class RequestListAdapter extends ArrayAdapter<RequestListItem> {
    private Activity activity;
    private List<RequestListItem> items;
    private View fragment;

    public RequestListAdapter(Activity activity, Context context, List<RequestListItem> items, View fragment) {
        super(context, 0, items);
        this.activity=activity;
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
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.request_list_item, parent, false);
        }

        TextView dayOfWeek = convertView.findViewById(R.id.dayOfWeek);
        TextView time = convertView.findViewById(R.id.time);
        TextView address = convertView.findViewById(R.id.address);
        TextView contact = convertView.findViewById(R.id.contact);

        RequestListItem item = getItem(position);

        if (item != null) {
            dayOfWeek.setText(item.getDayOfweek());
            time.setText(item.getTime());
            address.setText(item.getAddress());
            contact.setText(item.getContact());
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDialog(activity, view.getContext(), item);

                }
            });
        }



        return convertView;
    }

    private void showDialog(Activity activity, Context context, RequestListItem item) {
        final FrameLayout overlay = new FrameLayout(context);
        overlay.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlay.setBackgroundColor(Color.parseColor("#80000000"));
        activity.getWindow().addContentView(overlay, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        final Dialog dialog = new Dialog(context);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.request_item_drawer);
        dialog.show();

        dialog.setOnDismissListener(dialogInterface -> {
            overlay.setVisibility(View.GONE);
        });

        if (dialog != null) {
            TextView address = dialog.findViewById(R.id.address);
            TextView contact = dialog.findViewById(R.id.contact);
            TextView datetime = dialog.findViewById(R.id.datetime);
            TextView note = dialog.findViewById(R.id.note);
            AppCompatButton accept = dialog.findViewById(R.id.acceptBtn);
            AppCompatButton reject = dialog.findViewById(R.id.rejectBtn);
            address.setText(item.getAddress());
            contact.setText(item.getContact());
            datetime.setText(item.getDayOfweek() + ", " + item.getTime());
            note.setText(item.getNote());

            if (item.getStatus() != "pending") {
                accept.setVisibility(View.GONE);
                reject.setVisibility(View.GONE);
            }
            LinearLayout noRecords = fragment.findViewById(R.id.no_records);
            ListView requestList = fragment.findViewById(R.id.request_list);

            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    item.setStatus("accepted");
                    items.remove(item);
                    if (items.isEmpty()) {
                        noRecords.setVisibility(View.VISIBLE);
                        requestList.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 0.0f));
                        noRecords.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,1.0f));
                    } else {
                        noRecords.setVisibility(View.GONE);
                        requestList.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
                        noRecords.setLayoutParams(new LinearLayout.LayoutParams(0,0,0.0f));
                    }
                    notifyDataSetChanged();
                    dialog.dismiss();
                }
            });

            reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    item.setStatus("rejected");
                    items.remove(item);
                    if (items.isEmpty()) {
                        noRecords.setVisibility(View.VISIBLE);
                        requestList.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 0.0f));
                        noRecords.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,1.0f));
                    } else {
                        noRecords.setVisibility(View.GONE);
                        requestList.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
                        noRecords.setLayoutParams(new LinearLayout.LayoutParams(0,0,0.0f));
                    }
                    notifyDataSetChanged();
                    dialog.dismiss();
                }
            });
        }


        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.drawerAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }
}
