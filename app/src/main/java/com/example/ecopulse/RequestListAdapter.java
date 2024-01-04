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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
            if (item.getStatus().equals("done")) {
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm");
                    SimpleDateFormat outputFormat = new SimpleDateFormat("h:mm a");

                    Date date = inputFormat.parse(item.getTime());
                    String formattedTime = outputFormat.format(date);
                    time.setText(formattedTime);
                } catch (ParseException e) {
                    Log.e("PARSE EXCEPTION", e.getMessage());
                }
            } else {
                time.setText(item.getTime());
            }
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
            if (item.getStatus().equals("done")) {
               try {
                   SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm");
                   SimpleDateFormat outputFormat = new SimpleDateFormat("h:mm a");

                   Date date = inputFormat.parse(item.getTime());
                   String formattedTime = outputFormat.format(date);
                   datetime.setText(item.getDayOfweek() + ", " + formattedTime);
               } catch (ParseException e) {
                   Log.e("PARSE EXCEPTION", e.getMessage());
               }
            } else {
                datetime.setText(item.getDayOfweek() + ", " + item.getTime());
            }

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
        db.collection("pick_up_schedule").document(item.getId()).update("status", status, "update_at", new Date().getTime()).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                        uploadData("Recycling Pick Up Schedule", item.getDayOfweek(), inputLocalTime.toString(), item);
                    }

                    db.collection("pick_up_schedule").document(item.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                String customerID = task.getResult().get("userID").toString();
                                String recyclingCenterID = task.getResult().get("recyclingCenterID").toString();

                                db.collection("recycling_center_information").document(recyclingCenterID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            String centerName = task.getResult().get("name").toString();
                                            Map<String, Object> messageObj = new HashMap<>();
                                            if (status.equals("accepted")) {
                                                messageObj.put("title", "Your request on " + centerName + " has been accepted!");
                                                messageObj.put("desc", "Date: " + item.getDayOfweek() + "\nTime: " + item.getTime());
                                            } else {
                                                messageObj.put("title", "Your request on " + centerName + " has been rejected!");
                                                messageObj.put("desc", "Make your new request!");
                                            }
                                            messageObj.put("added_at", new Date().getTime());
                                            db.collection("user").document(customerID).collection("messages").add(messageObj);
                                        }
                                    }
                                });

                            }
                        }
                    });

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

    public void uploadData(String title, String date, String time, RequestListItem item){



        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("pick_up_schedule").document(item.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
                                String recyclingCenterUserId = task3.getResult().get("userID") + "";
                                String requestCode = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

                                com.example.ecopulse.Model.Task task = new com.example.ecopulse.Model.Task(title, recyclingCenterName, date, time, requestCode, item.getId());
                                // Add the task to Firestore
                                db.collection("user").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> documentSnapshotTask) {
                                        if (documentSnapshotTask.isSuccessful()) {
                                            db.collection("user").document(userID).collection("tasks")
                                                    .add(task)
                                                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentReference> documentReferenceTask) {
                                                            if (documentReferenceTask.isSuccessful()){
                                                                //setAlarm(task);
                                                                String customerName = documentSnapshotTask.getResult().get("username").toString();
                                                                String requestCode2 = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                                                                String description = String.format("Name: %s,\nContact: %s,\nAddress: %s,\nNote: %s", customerName, item.getContact(), item.getAddress(), item.getNote());
                                                                com.example.ecopulse.Model.Task collabTask = new com.example.ecopulse.Model.Task("Pick Up Request", description, date, time, requestCode2, item.getId());
                                                                db.collection("user").document(recyclingCenterUserId).collection("tasks")
                                                                        .add(collabTask).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<DocumentReference> documentReferenceTask1) {
                                                                                if (documentReferenceTask1.isSuccessful()) {
                                                                                    setAlarm(collabTask);
                                                                                }

                                                                            }
                                                                        });
                                                            }
                                                        }

                                                    });
                                        }
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
