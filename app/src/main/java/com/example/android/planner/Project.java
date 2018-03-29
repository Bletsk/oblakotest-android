package com.example.android.planner;

import java.util.List;
import java.util.ArrayList;

public class Project {
    public int id;
    public String title;
    //List<Integer> todos;

    Project(String title, int id){
        this.title = title;
        this.id = id;
        //todos = new ArrayList<>();
    }

    /*public void addTodo(int id){
        todos.add(id);
    }*/
}
