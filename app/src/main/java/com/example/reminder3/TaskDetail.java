package com.example.reminder3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TaskDetail extends AppCompatActivity {
 TextView detailTitle,detailDesc,detailDate,detailTime;
 ImageView deleteBtn,editBtn;
 String key="";
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_detail);

      detailTitle=findViewById(R.id.titleContent);
      detailDesc=findViewById(R.id.descContent);
      detailDate=findViewById(R.id.dateContent);
      detailTime=findViewById(R.id.timeContent);
      deleteBtn=findViewById(R.id.deleteButton);
      editBtn=findViewById(R.id.editButton);


      Bundle bundle=getIntent().getExtras();
      if(bundle!=null){
          detailTitle.setText(bundle.getString("Title"));
          detailDesc.setText(bundle.getString("Desc"));
          detailDate.setText(bundle.getString("Date"));
          detailTime.setText(bundle.getString("Time"));
          key=bundle.getString("Key");

      }
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference reference= FirebaseDatabase.getInstance().getReference("task");
                reference.child(key).removeValue();
                Toast.makeText(TaskDetail.this,"Deleted",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
            }
        });

      editBtn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              Intent intent=new Intent(TaskDetail.this, updateActivity.class)
                      .putExtra("Title",detailTitle.getText().toString()
                      ).putExtra("Desc",detailDesc.getText().toString()).putExtra("Date",detailDate.getText().toString())
                      .putExtra("Time",detailTime.getText().toString()).putExtra("Key",key);
              startActivity(intent);
          }
      });
    }
}
