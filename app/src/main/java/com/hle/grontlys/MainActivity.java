package com.hle.grontlys;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText navnEditText, poststedEditText;
    private Spinner arstallSpinner;
    private String sokeNavn, sokePoststed, arstall;
    private ArrayList<Spisested> spisestedListe= new ArrayList<>();


    //logtag
    private static final String TAG = "JsonLog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //setter opp view-variabler
        navnEditText = findViewById(R.id.spisested_sokenavn);
        poststedEditText = findViewById(R.id.poststed_sokenavn);

        arstallSpinner = findViewById(R.id.arstall_spinner);
        byggSpinner();

        Button sokeKnapp = findViewById(R.id.sok_knapp);
        sokeKnapp.setOnClickListener(this);

        Button visHerKnapp = findViewById(R.id.vis_her_knapp);
        visHerKnapp.setOnClickListener(this);



    }

    private void byggSpinner() {
        final String[] ARSTALL = {"Alle", "2020", "2019", "2018", "2017", "2016"};

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, ARSTALL );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        arstallSpinner.setAdapter(spinnerAdapter);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sok_knapp:
                startSpisestedSok();
                break;
            case R.id.vis_her_knapp:
                startOmradeSok();
        }
    }

    private void startSpisestedSok() {
        sokeNavn        = navnEditText.getText().toString();
        sokePoststed    = poststedEditText.getText().toString();
        arstall = arstallSpinner.getSelectedItem().toString();

        //lar ikke bruker hente hele datasettet
        if (sokeNavn.isEmpty() && sokePoststed.isEmpty()){
            displayToast("Legg inn navn på spisested og/eller poststed");
        }
        else {
            Intent intent = new Intent(this, SokelisteActivity.class);
            intent.putExtra("sokenavn", sokeNavn);
            intent.putExtra("sokepoststed", sokePoststed);
            intent.putExtra("arstall", arstall);

            startActivity(intent);
        }
    }

    private void startOmradeSok() {
    }



    /******************************
     * Meny-behandling
     * Ikke implementert p.t
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /******************************
     * Håndtering av Landscape/portrait
     *
     */

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /******************************
     * Utility-metoder
     *
     */

    //viser toastmelding med valgt tekstinput
    public void displayToast(String message) {
        Toast.makeText(getApplicationContext(), message,
                Toast.LENGTH_SHORT).show();
    }





}
