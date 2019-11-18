package com.hle.grontlys;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, Response.ErrorListener, Response.Listener<String> {

    private EditText navnEditText, poststedEditText;
    private String sokeNavn, sokePoststed;
    private ArrayList<Spisested> spisestedListe= new ArrayList<>();

    //endpoint for CRUD-api
    private static final String ENDPOINT = "https://hotell.difi.no/api/json/mattilsynet/smilefjes/tilsyn?";
    //søkeparametre for api
    private static final String KOL_NAVN        = "navn";
    private static final String KOL_POSTSTED    = "poststed";

    //logtag
    private static final String TAG = "JsonLog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navnEditText = findViewById(R.id.spisested_sokenavn);
        poststedEditText = findViewById(R.id.poststed_sokenavn);

        Button sokeKnapp = findViewById(R.id.sok_knapp);
        sokeKnapp.setOnClickListener(this);

        Button visHerKnapp = findViewById(R.id.vis_her_knapp);
        visHerKnapp.setOnClickListener(this);

        /* Foreløpig ikke i bruk
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }); */
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
        String URL = ENDPOINT;

        //lar ikke bruker hente hele datasettet
        if (sokeNavn.isEmpty() && sokePoststed.isEmpty()){
            displayToast("Legg inn navn på spisested og/eller poststed");
        }
        else if (!sokeNavn.isEmpty()){
            URL += KOL_NAVN + "=" + sokeNavn;
        }
        else {
            URL += KOL_POSTSTED + "=" + sokePoststed;
        }

        //henter resultat asynkront vhja Volley
        if (isOnline()){
            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, this, this);
            queue.add(stringRequest);
            Log.d(TAG, URL);
        }


    }

    private void startOmradeSok() {
    }

    /******************************
     *
     * Behandling av Volley-oppslag
     */

    @Override
    public void onResponse(String response) {

        spisestedListe = Spisested.listSpisesteder(response);

        if (spisestedListe.isEmpty()){
            displayToast("Ingen spisesteder funnet!");
        }

    }

    @Override
    public void onErrorResponse(VolleyError error) {

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
     * Utility-metoder
     *
     */

    // Checks network connection
    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    //viser toastmelding med valgt tekstinput
    public void displayToast(String message) {
        Toast.makeText(getApplicationContext(), message,
                Toast.LENGTH_SHORT).show();
    }





}
