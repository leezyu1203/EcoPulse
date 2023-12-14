package com.example.reminder3.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reminder3.Model.Task;
import com.example.reminder3.R;
import com.example.reminder3.TaskDetail;
import com.example.reminder3.updateActivity;

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
           //setting details to TextView in item(task)

        // holder.status.setText(task.isComplete() ? "COMPLETED" : "UPCOMING");
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
                    Intent intent=new Intent(context, updateActivity.class);

                   intent.putExtra("Title",taskList.get(holder.getAdapterPosition()).getTaskTitle());
                   intent.putExtra("Desc",taskList.get(holder.getAdapterPosition()).getTaskDescription());
                   intent.putExtra("Date",taskList.get(holder.getAdapterPosition()).getDate());
                   intent.putExtra("Time",taskList.get(holder.getAdapterPosition()).getFirstAlarmTime());
                   intent.putExtra("Key",taskList.get(holder.getAdapterPosition()).getKey());


                    context.startActivity(intent);
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

        ImageView options;

        TextView time;
        String id;

        CardView recCard;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
           title=(TextView) itemView.findViewById(R.id.title);
           description=(TextView) itemView.findViewById(R.id.descriptionDetail);
           date=(TextView) itemView.findViewById(R.id.dateDetail);
           day=(TextView) itemView.findViewById(R.id.day);
           month=(TextView) itemView.findViewById(R.id.Month);
           //status=(TextView)itemView.findViewById(R.id.status);
          // options=(ImageView)itemView.findViewById(R.id.options);
           time=(TextView)itemView.findViewById(R.id.time);
           recCard=(CardView) itemView.findViewById(R.id.recCard);
        }
    }
} 
