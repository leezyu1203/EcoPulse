package com.example.ecopulse.Information;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

import com.example.ecopulse.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TimeslotFragment extends Fragment {
    private View timeslotLocation = null;
    private AppCompatImageButton nextDay = null;
    private AppCompatImageButton previousDay = null;
    private TimePicker timePicker = null;
    private AppCompatButton addTimeslot = null;
    private TextView day = null;
    private ListView timeslotlist = null;
    final private List<String> dayList = Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
    private String selectedDay;
    final private Map<String, ArrayList<String>> dayTimeSlot = new HashMap<String, ArrayList<String>>(){{
        put("Sunday", new ArrayList<>());
        put("Monday", new ArrayList<>());
        put("Tuesday", new ArrayList<>());
        put("Wednesday", new ArrayList<>());
        put("Thursday", new ArrayList<>());
        put("Friday", new ArrayList<>());
        put("Saturday", new ArrayList<>());
    }};

    private ArrayList<String> timeslot = new ArrayList<>();
    private int selectedDayIndex;
    FirebaseFirestore db;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        String currentDay = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());
        selectedDayIndex = dayList.indexOf(currentDay);
        selectedDay = dayList.get(selectedDayIndex);

        timeslotLocation = inflater.inflate(R.layout.timeslot_fragment, container, false);
        timeslotlist = timeslotLocation.findViewById(R.id.timeslot_list);
        timePicker = timeslotLocation.findViewById(R.id.time_picker);
        addTimeslot = timeslotLocation.findViewById(R.id.add_timeslot);
        nextDay = timeslotLocation.findViewById(R.id.nextDay);
        previousDay = timeslotLocation.findViewById(R.id.previousDay);
        day = timeslotLocation.findViewById(R.id.day);
        day.setText(currentDay);
        ArrayAdapter<String> adapter = new TimeslotAdapter(getContext(), timeslot, selectedDay);
        timeslotlist.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = "";
        if (user != null) {
            userEmail = user.getEmail();
        }
        db = FirebaseFirestore.getInstance();
        showLoadingBar(true);
        db.collection("user").whereEqualTo("email", userEmail).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                showLoadingBar(false);
                if (task.isSuccessful()) {
                    QueryDocumentSnapshot document1 = task.getResult().iterator().next();
                    String userID = document1.getId();
                    db.collection("recycling_center_information").where(Filter.equalTo("userID", userID)).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                QueryDocumentSnapshot document2 = task.getResult().iterator().next();
                                String documentID = document2.getId();
                                ArrayList<String> timeslotList = (ArrayList<String>) document2.getData().get("timeslot");
                                timeslotList.stream().forEach((slot)-> {
                                    String[] day_time_slot = slot.split(", ");
                                    dayTimeSlot.get(day_time_slot[0]).add(day_time_slot[1]);
                                });
                                timeslot.clear();
                                timeslot.addAll(dayTimeSlot.get(dayList.get(selectedDayIndex)));
                                adapter.notifyDataSetChanged();

                                nextDay.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (selectedDayIndex < 6) {
                                            selectedDayIndex += 1;
                                            day.setText(dayList.get(selectedDayIndex));
                                        } else {
                                            selectedDayIndex = 0;
                                            day.setText(dayList.get(selectedDayIndex));
                                        }

                                        timeslot.clear();
                                        timeslot.addAll(dayTimeSlot.get(dayList.get(selectedDayIndex)));
                                        selectedDay = dayList.get(selectedDayIndex);
                                        ArrayAdapter<String> adapter = new TimeslotAdapter(getContext(), dayTimeSlot.get(selectedDay), selectedDay);
                                        timeslotlist.setAdapter(adapter);
                                    }
                                });

                                previousDay.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (selectedDayIndex > 0) {
                                            selectedDayIndex -= 1;
                                            day.setText(dayList.get(selectedDayIndex));
                                        } else {
                                            selectedDayIndex = 6;
                                            day.setText(dayList.get(selectedDayIndex));
                                        }
                                        timeslot.clear();
                                        timeslot.addAll(dayTimeSlot.get(dayList.get(selectedDayIndex)));
                                        selectedDay = dayList.get(selectedDayIndex);
                                        ArrayAdapter<String> adapter = new TimeslotAdapter(getContext(), dayTimeSlot.get(selectedDay), selectedDay);
                                        timeslotlist.setAdapter(adapter);
                                    }
                                });

                                addTimeslot.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        addTimeslot(documentID);
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
        return timeslotLocation;
    }

    private void addTimeslot(String documentID) {
        // Retrieve the currently selected day based on the index.
        selectedDay = dayList.get(selectedDayIndex);

        // Format the selected time from the TimePicker into a readable string.
        String timeslotString = getTimeslot();

        // Check if the timeslot for the selected day already exists.
        if (dayTimeSlot.get(selectedDay).contains(timeslotString)) {
            // Display a toast message indicating that the timeslot already exists.
            Toast.makeText(getContext(),"Timeslot already exist!", Toast.LENGTH_SHORT).show();
        } else {
            // Update the Firestore document with the new timeslot for the selected day.
            db.collection("recycling_center_information")
                    .document(documentID).update("timeslot", FieldValue.arrayUnion(selectedDay + ", " + timeslotString));

            // Add the new timeslot to the local data structure.
            dayTimeSlot.get(selectedDay).add(timeslotString);

            // Refresh the UI by updating the timeslot list.
            timeslot.clear();
            timeslot.addAll(dayTimeSlot.get(dayList.get(selectedDayIndex)));
            ArrayAdapter<String> adapter = new TimeslotAdapter(getContext(), timeslot, selectedDay);
            timeslotlist.setAdapter(adapter);
        }
    }

    private String getTimeslot() {
        int hour = timePicker.getHour();
        String minute = timePicker.getMinute() >= 10 ? timePicker.getMinute() + "" : "0" + timePicker.getMinute();
        String meridiem = "";

        if (hour >= 12) {
            meridiem = "PM";
            if (hour > 12) {
                hour -= 12;
            }
        } else {
            if (hour == 0) {
                hour = 12;
            }
            meridiem = "AM";
        }

        return hour + ":" + minute + " " + meridiem;
    }

    private void showLoadingBar(boolean show) {
        LinearLayout loading = timeslotLocation.findViewById(R.id.loading);
        if (show) {
            timeslotlist.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 0.0f));
            loading.setVisibility(View.VISIBLE);
        } else {
            loading.setVisibility(View.GONE);
            timeslotlist.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
            loading.setLayoutParams(new LinearLayout.LayoutParams(0,0,0.0f));
        }
    }
}
