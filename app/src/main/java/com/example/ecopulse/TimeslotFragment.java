package com.example.ecopulse;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimeslotFragment extends Fragment {
    private View timeslotLocation = null;
    private AppCompatImageButton nextDay = null;
    private AppCompatImageButton previousDay = null;
    private TextView day = null;
    final private List<String> dayList = Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
    private int selectedDayIndex;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        String currentDay = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());
        selectedDayIndex = dayList.indexOf(currentDay);


        timeslotLocation = inflater.inflate(R.layout.timeslot_fragment, container, false);
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
        return timeslotLocation;
    }
}
