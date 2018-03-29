package com.example.android.planner;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.app.ActionBar;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Button;
import android.content.Intent;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.scalified.fab.ActionButton;

import com.google.gson.Gson;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.koushikdutta.ion.Ion;

public class Index extends AppCompatActivity {

    ActionButton newTodoBtn;
    ListView mainList;
    ProgressBar dataLoadingProgressBar;
    LinearLayout errorLayout;
    Button reloadButton;

    private CustomAdapter mAdapter;
    ArrayList<Project> projectList;
    ArrayList<Todo> todoList;
    int dataCount = 0;
    public static Context context;

    Object dataGroup;


    JsonArray jsonArray = new JsonArray();
    JsonObject jsonString = new JsonObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/OpenSans-Light.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_index);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);

        mainList = (ListView)findViewById(R.id.main_list);

        dataLoadingProgressBar = (ProgressBar) findViewById(R.id.dataLoadingProgressBar);

        errorLayout = (LinearLayout)findViewById(R.id.errorLayout);
        reloadButton = (Button)findViewById(R.id.reloadButton);
        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLoading();
            }
        });

        newTodoBtn = (ActionButton) findViewById(R.id.new_todo_btn);
        newTodoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle b = new Bundle();

                ArrayList<String> projectNames = new ArrayList<String>();
                ArrayList<Integer> projectIds = new ArrayList<Integer>();
                ArrayList<String> todoNames = new ArrayList<String>();
                ArrayList<Integer> todoParents = new ArrayList<Integer>();

                for(Project p: projectList){
                    projectNames.add(p.title);
                    projectIds.add(p.id);
                }
                for(Todo t: todoList){
                    todoNames.add(t.text);
                    todoParents.add(t.projectId);
                }

                b.putStringArrayList("projectNames", projectNames);
                b.putIntegerArrayList("projectIds", projectIds);
                b.putStringArrayList("todoNames", todoNames);
                b.putIntegerArrayList("todoParents", todoParents);

                Intent intent = new Intent(Index.this, NewTodoActivity.class);
                intent.putExtras(b);
                startActivityForResult(intent, 0);
            }
        });

        startLoading();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void fillData(){
        ArrayList<Todo> __todos = new ArrayList<Todo>();
        if(dataCount < 2) return;

        dataLoadingProgressBar.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
        newTodoBtn.show();
        mainList.setVisibility(View.VISIBLE);

        mAdapter = new CustomAdapter(this);
        for(Project p: projectList){
            mAdapter.addSectionHeaderItem(p.title);
            for(Todo t: todoList){
                if(t.projectId == p.id){
                    mAdapter.addItem(t);
                }
            }
        }
        mainList.setAdapter(mAdapter);
    }

    private void startLoading(){
        newTodoBtn.hide();
        mainList.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
        dataLoadingProgressBar.setVisibility(View.VISIBLE);

        Ion.with(this).load(this.getResources().getString(R.string.server_address) + "projects/all.json")
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        if(e != null) {
                            showError();
                        }
                        if (result != null) {
                            projectList = new ArrayList<Project>();
                            for (final JsonElement projectJsonElement : result) {
                                projectList.add(new Gson().fromJson(projectJsonElement, Project.class));
                            }

                            dataCount++;
                            fillData();
                        }
                    }
                });

        Ion.with(this).load(this.getResources().getString(R.string.server_address) + "todos/all.json")
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        if(e != null) {
                            showError();
                        }
                        if (result != null) {
                            todoList = new ArrayList<Todo>();
                            for (final JsonElement projectJsonElement : result) {
                                todoList.add(new Gson().fromJson(projectJsonElement, Todo.class));
                            }

                            dataCount++;
                            fillData();
                        }
                    }
                });
    }

    private void showError(){
        dataLoadingProgressBar.setVisibility(View.GONE);
        newTodoBtn.hide();
        mainList.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 0){
            if(resultCode == 1){
                startLoading();
            }
        }
    }
}
