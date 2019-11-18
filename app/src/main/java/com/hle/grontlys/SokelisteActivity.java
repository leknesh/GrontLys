package com.hle.grontlys;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

public class SokelisteActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<Spisested> spisestedListe = new ArrayList<>();
    private SpisestedAdapter spisestedAdapter;

    //logtag
    private static final String TAG = "JsonLog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sokeliste);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* Ikke implementert p.t
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }); */

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Henter søkeresultatet fra Intent
        Intent intent = getIntent();

        if (intent != null){
            spisestedListe = intent.getParcelableExtra("spisesteder");
            Log.d(TAG, "Hentet arrayliste");
        }
        else {
            Log.d(TAG, "Intent = null");
        }

        Log.d(TAG, "Hentet arrayliste" + spisestedListe.size());

        recyclerView = findViewById(R.id.spisested_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        spisestedAdapter = new SpisestedAdapter(this, spisestedListe);
        recyclerView.setAdapter(spisestedAdapter);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*switch (item.getItemId()) {
            case R.id.action_mainActivity:
                Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            default:
                // Gjøre noe her?!
        } */
        return super.onOptionsItemSelected(item);

    }


}
