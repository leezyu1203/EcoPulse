package com.example.ecopulse;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

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
    final private List<String> dayList = Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
    final private Map<String, ArrayList<String>> dayTimeSlot = new HashMap<String, ArrayList<String>>(){{
        put("Sunday", new ArrayList<>());
        put("Monday", new ArrayList<>());
        put("Tuesday", new ArrayList<>());
        put("Wednesday", new ArrayList<>());
        put("Thursday", new ArrayList<>());
        put("Friday", new ArrayList<>());
        put("Saturday", new ArrayList<>());
    }};
    private int selectedDayIndex;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        String currentDay = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());
        selectedDayIndex = dayList.indexOf(currentDay);


        timeslotLocation = inflater.inflate(R.layout.timeslot_fragment, container, false);
        timePicker = timeslotLocation.findViewById(R.id.time_picker);
        addTimeslot = timeslotLocation.findViewById(R.id.add_timeslot);
        nextDay = timeslotLocation.findViewById(R.id.nextDay);
        previousDay = timeslotLocation.findViewById(R.id.previousDay);
        day = timeslotLocation.findViewById(R.id.day);
        day.setText(currentDay);

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
            }
        });

        addTimeslot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedDay = dayList.get(selectedDayIndex);
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();
                String meridiem = "";
                if (hour >= 12) {
                    meridiem = "PM";
                    if (hour > 12) {
                        hour -= 12;
                    }
                } else {
                    meridiem = "AM";
                }
                String timeslot = selectedDay + ", " + hour + ":" + minute + " " + meridiem;
                dayTimeSlot.get(selectedDay).add(timeslot);
                Log.d("Display", dayTimeSlot.get(selectedDay).toString());
            }
        });
        return timeslotLocation;
    }
}
