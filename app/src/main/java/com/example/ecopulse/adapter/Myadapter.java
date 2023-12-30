package com.example.ecopulse.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecopulse.Model.Task;
import com.example.ecopulse.R;
import com.example.ecopulse.updateFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Myadapter extends RecyclerView.Adapter<Myadapter.ViewHolder> {
    private List<Task> taskList;
    private Context context;
    public SimpleDateFormat dateFormat = new SimpleDateFormat("EE dd MMM yyyy", Locale.US);
    public SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd-M-yyyy", Locale.US);
    Date date ;
    String outputDateString ;

    public Myadapter(List<Task> taskList, Context context) {
        this.taskList = taskList;
        this.context=context;
    }

    @NonNull
    @Override
    //create when every item is created
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task,parent,false);
        return new ViewHolder(v);
    }


    @Override
    //bind data to viewholder
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            Task task = taskList.get(position);
            holder.title.setText(task.getTaskTitle());
            holder.description.setText((task.getTaskDescription()));
            holder.time.setText(task.getFirstAlarmTime());

        try {
            date = inputDateFormat.parse(task.getDate());
            outputDateString = dateFormat.format(date);

            String[] items1 = outputDateString.split(" ");
            String day = items1[0];
            String dd = items1[1];
            String month = items1[2];

            holder.day.setText(day);
            holder.date.setText(dd);
            holder.month.setText(month);

        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.recCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fragmentManager = ((AppCompatActivity) v.getContext()).getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    // Create your fragment instance
                     updateFragment updateFragment = new updateFragment();

                    // Pass data to the fragment using Bundle
                    Bundle bundle = new Bundle();
                    bundle.putString("Title", taskList.get(holder.getAdapterPosition()).getTaskTitle());
                    bundle.putString("Desc", taskList.get(holder.getAdapterPosition()).getTaskDescription());
                    bundle.putString("Date", taskList.get(holder.getAdapterPosition()).getDate());
                    bundle.putString("Time", taskList.get(holder.getAdapterPosition()).getFirstAlarmTime());
                    bundle.putString("Key", taskList.get(holder.getAdapterPosition()).getKey());
                    bundle.putString("requestCode",taskList.get(holder.getAdapterPosition()).getRequestCode());

                    updateFragment.setArguments(bundle);

                    // Perform fragment transaction
                    fragmentTransaction.replace(R.id.main_fragment, updateFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            });



             }

    @Override
    public int getItemCount() {
        return taskList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        TextView description;
        TextView day;
        TextView date;
        TextView month;
        TextView status;
        TextView time;
        CardView recCard;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
           title=(TextView) itemView.findViewById(R.id.title);
           description=(TextView) itemView.findViewById(R.id.descriptionDetail);
           date=(TextView) itemView.findViewById(R.id.dateDetail);
           day=(TextView) itemView.findViewById(R.id.day);
           month=(TextView) itemView.findViewById(R.id.Month);
           time=(TextView)itemView.findViewById(R.id.time);
           recCard=(CardView) itemView.findViewById(R.id.recCard);
        }
    }
} 
