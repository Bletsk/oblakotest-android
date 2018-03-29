package com.example.android.planner;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class NewTodoActivity extends AppCompatActivity {

    ListView projectList;
    Button buttonCancel,
           buttonCreate;

    EditText editText;

    Spinner projectSpinner;

    Context context;

    ArrayList<LocalProject> data = new ArrayList<LocalProject>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_todo);
        context = this;

        getDataFromParent();

        projectList = findViewById(R.id.projectList);

        projectSpinner = (Spinner)findViewById(R.id.projectSpinner);
        setSpinnerData();
        projectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setListViewData(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        editText = (EditText) findViewById(R.id.editText);

        buttonCancel = findViewById(R.id.buttoncancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        buttonCreate = findViewById(R.id.buttoncreate);
        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editText.getText() == null || editText.getText().toString().equals("")){
                    return;
                }
                LinearLayout headerLayout = (LinearLayout)findViewById(R.id.newTodoHeaderLayout);
                headerLayout.setVisibility(View.GONE);
                LinearLayout bottomLayout = (LinearLayout)findViewById(R.id.newTodoBottomLayout);
                bottomLayout.setVisibility(View.GONE);
                ProgressBar pb = (ProgressBar)findViewById(R.id.jsonPostingProgressBar);
                pb.setVisibility(View.VISIBLE);

                JsonObject params = new JsonObject();
                params.addProperty("text", editText.getText().toString());
                params.addProperty("project_id", projectSpinner.getSelectedItemPosition() + 1);

                Ion.with(Index.context).load("POST",Index.context.getResources().getString(R.string.server_address) + "todos/")
                        .setJsonObjectBody(params)
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                setResult(1);
                                finish();
                            }
                        });
            }
        });

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void getDataFromParent(){
        Bundle b = this.getIntent().getExtras();
        int x = 0;
        if(b != null){
            ArrayList<String> projectNames = b.getStringArrayList("projectNames");
            ArrayList<Integer> projectIds = b.getIntegerArrayList("projectIds");
            ArrayList<String> todoNames = b.getStringArrayList("todoNames");
            ArrayList<Integer> todoParents = b.getIntegerArrayList("todoParents");

            for(int i = 0; i < projectNames.size(); i++){
                ArrayList<String> _todos = new ArrayList<String>();
                ArrayList<Boolean> _todoStates = new ArrayList<Boolean>();
                for(int j = 0; j < todoNames.size(); j++){
                    if(todoParents.get(j) == projectIds.get(i)){
                        _todos.add(todoNames.get(j));
                    }
                }
                LocalProject p = new LocalProject(projectNames.get(i), projectIds.get(i), _todos);
                data.add(p);
            }
        }
        else {
            Log.d("getDataFromParent", "Bundle is empty!");
        }
    }

    private void setSpinnerData(){
        ArrayList<String> _projects = new ArrayList<String>();
        for(LocalProject p:data){
            _projects.add(p.title);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, _projects);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        projectSpinner.setAdapter(adapter);
        projectSpinner.setPrompt("Название");
    }

    private void setListViewData(int index){
        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this, R.layout.projectlist_cell, data.get(index).todos);
        projectList.setAdapter(listAdapter);
    }


    private class LocalProject {
        String title;
        int id;
        ArrayList<String> todos;

        LocalProject(String title, int id, ArrayList<String> todos){
            this.title = title;
            this.id = id;
            this.todos = todos;
        }
    }
}
