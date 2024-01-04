package com.example.ecopulse;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminApprovalListAdapter extends ArrayAdapter<AdminApprovalListItem> {

    private List<AdminApprovalListItem> items;
    private TextView name, address, contact, email, role, opening, type;
    private AppCompatButton approve, reject;
    public AdminApprovalListAdapter( Context context, List<AdminApprovalListItem> items) {
        super(context, 0, items);
        this.items = items;
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
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.admin_approval_list_item, parent, false);
        }

        AdminApprovalListItem Item = getItem(position);
        name = convertView.findViewById(R.id.name);
        contact = convertView.findViewById(R.id.contact);
        address = convertView.findViewById(R.id.address);
        email = convertView.findViewById(R.id.email);
        approve = convertView.findViewById(R.id.approve_btn);
        reject = convertView.findViewById(R.id.reject_btn);
        role = convertView.findViewById(R.id.role);
        opening = convertView.findViewById(R.id.opening);
        type = convertView.findViewById(R.id.type);

        name.setText(Item.getName());
        contact.setText(Item.getContact());
        address.setText(Item.getAddress());
        email.setText(Item.getEmail());
        role.setText(Item.getRole());

        if (Item.getRole().equals("Recycling Center Collaborator")) {
            opening.setText(Item.getOpening());
            type.setText(Item.getType());
        } else {
            opening.setVisibility(View.GONE);
            type.setVisibility(View.GONE);
        }

        if (Item.getStatus().equals("approved")) {
            approve.setVisibility(View.GONE);
            reject.setVisibility(View.GONE);
        }

        approve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeStatus("approved", Item);
                items.remove(position);
                notifyDataSetChanged();
            }
        });

        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeStatus("rejected", Item);
                items.remove(position);
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    public void changeStatus(String status, AdminApprovalListItem Item) {
        FirebaseFirestore firestoreRef = FirebaseFirestore.getInstance();
        firestoreRef.collection("user").document(Item.getId()).update("verified", status).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful() && status.equals("approved")) {
                    if (Item.getRole().equals("Recycling Center Collaborator")) {
                        Map<String, Object> newRecyclingCollab = new HashMap<>();

                        newRecyclingCollab.put("address", Item.getAddress());
                        newRecyclingCollab.put("contact", Item.getContact());
                        newRecyclingCollab.put("name", Item.getName());
                        newRecyclingCollab.put("opening", Item.getOpening());
                        newRecyclingCollab.put("timeslot", new ArrayList<String>());
                        newRecyclingCollab.put("type", Item.getType());
                        newRecyclingCollab.put("lat", "");
                        newRecyclingCollab.put("lng", "");
                        newRecyclingCollab.put("userID", Item.getId());

                        firestoreRef.collection("recycling_center_information").add(newRecyclingCollab);
                    }
                }
            }
        });
    }


}
