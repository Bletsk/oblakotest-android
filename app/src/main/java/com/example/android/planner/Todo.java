package com.example.android.planner;

public class Todo {
    int id;
    String text;
    boolean isCompleted;
    int projectId;

    Todo(int id, String text, boolean isCompleted, int projectId){
        this.id = id;
        this.text = text;
        this.isCompleted = isCompleted;
        this.projectId = projectId;
    }
}
