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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    private FirebaseFirestore db;

    public RequestListAdapter(Activity activity, Context context, List<RequestListItem> items, View fragment) {
        super(context, 0, items);
        this.activity=activity;
        this.items = items;
        this.fragment = fragment;
        db = FirebaseFirestore.getInstance();
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
        // Check if the recycled view is null; inflate a new view if needed.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.request_list_item, parent, false);
        }

        // Retrieve references to TextView elements within the inflated view.
        TextView dayOfWeek = convertView.findViewById(R.id.dayOfWeek);
        TextView time = convertView.findViewById(R.id.time);
        TextView address = convertView.findViewById(R.id.address);
        TextView contact = convertView.findViewById(R.id.contact);

        // Get the RequestListItem object associated with the current position.
        RequestListItem item = getItem(position);

        // Populate TextView elements with data.
        if (item != null) {
            dayOfWeek.setText(item.getDayOfweek());
            if (item.getStatus().equals("done") || item.getStatus().equals("cancelled")) {
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

            // Set an OnClickListener to handle item clicks and show a dialog.
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // The request item details is passed to the dialog to populates the
                    // TextView elements in dialog with data
                    showDialog(activity, view.getContext(), item);
                }
            });
        }

        // Return the configured view for rendering in the ListView.
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
                    boolean isAfter = true;
                    SimpleDateFormat dateFormat = new SimpleDateFormat("d-M-yyyy h:mm a");
                    String currentDateTimeStr = dateFormat.format(new Date());

                    try {
                        Date givenDateTime = dateFormat.parse(item.getDayOfweek() + " " + item.getTime());
                        Date currentDateTime = dateFormat.parse(currentDateTimeStr);

                        if (givenDateTime.after(currentDateTime)) {
                            isAfter = true;
                        } else if (givenDateTime.before(currentDateTime)) {
                            isAfter = false;
                        } else {
                            isAfter = false;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (isAfter) {
                        setStatusForRequest(item, dialog, "accepted");
                    } else {
                        Toast.makeText(activity, "Cannot accept a request which is in the past or current!", Toast.LENGTH_SHORT).show();
                    }

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

    public String convertTimeTo24HourFormat(String time) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("h:mm a");
        LocalTime time12Hour = LocalTime.parse(time, inputFormatter);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String time24Hour = time12Hour.format(outputFormatter);
        LocalTime inputLocalTime = LocalTime.parse(time24Hour, DateTimeFormatter.ofPattern("HH:mm"));
        return inputLocalTime.toString();
    }

    public void setStatusForRequest(RequestListItem item, Dialog dialog, String status) {
        // Access the FireStore collection and update the status and timestamp of the recycling request.
        db.collection("pick_up_schedule").document(item.getId())
                .update("status", status, "update_at", new Date().getTime())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Remove RequestListItem from the items list locally.
                    items.remove(item);
                    // If the status is "accepted", create a task for the collaborator.
                    if (status.equals("accepted")) {
                        createTask(item);
                    }
                    // Notify the user about the updated status of the recycling request.
                    notifyUserAboutRequestStatus(item, status);
                    // Update the ListView
                    updateView();
                    // Dismiss the dialog
                    dialog.dismiss();
                }
            }
        });
    }

    public void updateView() {
        LinearLayout noRecords = fragment.findViewById(R.id.no_records);
        ListView requestList = fragment.findViewById(R.id.request_list);
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
    }

    public void notifyUserAboutRequestStatus(RequestListItem item, String status) {
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
    }

    public void createTask(RequestListItem item){
        db.collection("pick_up_schedule").document(item.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task2) {
                if (task2.isSuccessful()) {
                    String userID = task2.getResult().get("userID") + "";
                    String recyclingCenterID = task2.getResult().get("recyclingCenterID") + "";
                    getRecyclingCenterName(recyclingCenterID, item, userID);
                }
            }
        });


    }

    public void getRecyclingCenterName(String recyclingCenterID, RequestListItem item, String userID) {
        db.collection("recycling_center_information").document(recyclingCenterID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task3) {
                if (task3.isSuccessful()) {
                    String recyclingCenterName = task3.getResult().get("name") + "";
                    String recyclingCenterUserId = task3.getResult().get("userID") + "";
                    getCustomerName(userID, recyclingCenterUserId, item, recyclingCenterName);
                }
            }
        });
    }

    private void getCustomerName(String userID, String recyclingCenterUserId, RequestListItem item, String recyclingCenterName) {

        db.collection("user").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> documentSnapshotTask) {
                if (documentSnapshotTask.isSuccessful()) {
                    String customerName = documentSnapshotTask.getResult().get("username").toString();
                    addCustomerTask(userID, item, recyclingCenterName);
                    addCollaboratorTask(customerName, recyclingCenterUserId, item);
                }
            }
        });
    }

    public void addCustomerTask(String userID, RequestListItem item, String recyclingCenterName) {
        // Create a unique requestCode for Alarm setting purpose
        String requestCode = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

        // Get the information of the request
        String time = convertTimeTo24HourFormat(item.getTime());
        String date = item.getDayOfweek();
        String id = item.getId();

        // Create a Task object with the request list item information
        com.example.ecopulse.Model.Task task =
                new com.example.ecopulse.Model.Task("Recycling Pick Up Schedule", recyclingCenterName, date, time, requestCode, id);

        // Create a task record for the user (customer who make the request) in FireStore
        db.collection("user").document(userID).collection("tasks")
                .add(task).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(activity, "Fail to create task for customer!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void addCollaboratorTask(String customerName, String recyclingCenterUserId, RequestListItem item) {
        // Create a unique requestCode for Alarm setting purpose
        String requestCode = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
        // Get the information of the request
        String time = convertTimeTo24HourFormat(item.getTime());
        String contact = item.getContact();
        String address = item.getAddress();
        String note = item.getNote();
        String date = item.getDayOfweek();
        String id = item.getId();
        String description =
                String.format("Name: %s,\nContact: %s,\nAddress: %s,\nNote: %s", customerName, contact, address, note);

        // Create a Task object with the request list item information
        com.example.ecopulse.Model.Task collabTask =
                new com.example.ecopulse.Model.Task("Pick Up Request", description, date, time, requestCode, id);

        // Create a task record for the collaborator (customer who make the request) in FireStore
        db.collection("user").document(recyclingCenterUserId).collection("tasks")
                .add(collabTask).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> documentReferenceTask1) {
                        if (documentReferenceTask1.isSuccessful()) {
                            // Set alarm for collaborator for the task
                            setAlarm(collabTask);
                        } else {
                            Toast.makeText(activity, "Fail to create task!", Toast.LENGTH_SHORT).show();
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
