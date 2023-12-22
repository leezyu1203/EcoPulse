package com.example.ecopulse.Model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

import java.io.Serializable;

@Entity
public class Task implements Serializable{



    String requestCode;
    String key;
    @ColumnInfo(name = "taskTitle")
    String taskTitle;
    @ColumnInfo(name="date")
    String date;
    @ColumnInfo(name = "taskDescription")
    String taskDescription;
    @ColumnInfo(name = "isComplete")
    boolean isComplete;
    @ColumnInfo(name = "firstAlarmTime")
    String firstAlarmTime;

    public Task(String title, String taskDescription, String date, String time,String requestCode) {
   this.taskTitle=title;
   this.taskDescription=taskDescription;
   this.date=date;
   this.firstAlarmTime=time;
   this.requestCode=requestCode;
    }


    public String getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(String requestCode) {
        this.requestCode = requestCode;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
    public boolean isComplete() {
        return isComplete;
    }


    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public String getFirstAlarmTime() {
        return firstAlarmTime;
    }

    public void setFirstAlarmTime(String firstAlarmTime) {
        this.firstAlarmTime = firstAlarmTime;
    }


    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescrption(String taskDescrption) {
        this.taskDescription = taskDescrption;
    }
public Task(){

}
}
