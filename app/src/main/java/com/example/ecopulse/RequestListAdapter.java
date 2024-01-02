package com.example.ecopulse;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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

            if (!item.getStatus().equals("pending")) {
                accept.setVisibility(View.GONE);
                reject.setVisibility(View.GONE);
            }

            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setStatusForRequest(item, dialog, "accepted");
                }
            });

            reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setStatusForRequest(item, dialog, "rejected");
                }
            });
        }


        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.drawerAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void setStatusForRequest(RequestListItem item, Dialog dialog, String status) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("pick_up_schedule").document(item.getId()).update("status", status).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    LinearLayout noRecords = fragment.findViewById(R.id.no_records);
                    ListView requestList = fragment.findViewById(R.id.request_list);
                    item.setStatus(status);
                    items.remove(item);

                    if (status.equals("accepted")) {
                        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("h:mm a");
                        LocalTime time12Hour = LocalTime.parse(item.getTime(), inputFormatter);
                        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm");
                        String time24Hour = time12Hour.format(outputFormatter);
                        LocalTime inputLocalTime = LocalTime.parse(time24Hour, DateTimeFormatter.ofPattern("HH:mm"));
                        LocalTime currentTime = LocalTime.now();

                        LocalDate today = LocalDate.now();
                        DayOfWeek targetDayOfWeek = DayOfWeek.valueOf(item.getDayOfweek().toUpperCase());
                        LocalDate nearestDate = today.with(targetDayOfWeek);

                        if (nearestDate.isBefore(today) || (nearestDate.isEqual(today) && currentTime.isAfter(inputLocalTime))) {
                            nearestDate = nearestDate.plusWeeks(1);
                        }

                        LocalDate localDate = LocalDate.parse(nearestDate.toString(), DateTimeFormatter.ISO_LOCAL_DATE);
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
                        String outputDate = localDate.format(formatter);

                        uploadData("Recycling Pick Up Schedule", outputDate, inputLocalTime.toString(), item.getId());
                    }

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
            }
        });

    }

    public void uploadData(String title, String date, String time, String id){



        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("pick_up_schedule").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task2) {
                if (task2.isSuccessful()) {
                    String userID = task2.getResult().get("userID") + "";
                    String recyclingCenterID = task2.getResult().get("recyclingCenterID") + "";

                    db.collection("recycling_center_information").document(recyclingCenterID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task3) {
                            if (task3.isSuccessful()) {
                                String recyclingCenterName = task3.getResult().get("name") + "";
                                String requestCode= DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

                                com.example.ecopulse.Model.Task task = new com.example.ecopulse.Model.Task(title, recyclingCenterName, date, time, requestCode);
                                // Add the task to Firestore
                                db.collection("user").document(userID).collection("tasks")
                                        .add(task)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                setAlarm(task);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    });



                }
            }
        });


    }

    private void setAlarm(com.example.ecopulse.Model.Task task){

        AlarmManager alarmManager=(AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

        int requestCode = task.getRequestCode().hashCode();

        Intent intent =new Intent(getContext(),AlarmReceiver.class);
        intent.putExtra("title",task.getTaskTitle());
        intent.putExtra("desc",task.getTaskDescription());

        PendingIntent pendingIntent= PendingIntent.getBroadcast(getContext(),requestCode,intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);


        // Parse the date and time strings to create a Calendar instance
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(dateFormat.parse(task.getDate() + " " + task.getFirstAlarmTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Set the alarm using AlarmManager
        if (calendar.getTimeInMillis() > System.currentTimeMillis()) {
            // Only set the alarm if it's in the future
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

}
