package com.hle.grontlys;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

public class SokelisteActivity extends AppCompatActivity implements Response.Listener<String>, Response.ErrorListener {

    private String sokeNavn, sokePoststed, arstall;

    private RecyclerView recyclerView;
    private ArrayList<Spisested> spisestedListe = new ArrayList<>();
    private SpisestedAdapter spisestedAdapter;

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
        setContentView(R.layout.activity_sokeliste);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //gjenoppretter variabler fra savedinstancestate hvis de er tilgjengelige
        if (savedInstanceState != null){
            sokeNavn = savedInstanceState.getString("sokenavn");
            sokePoststed = savedInstanceState.getString("sokepoststed");
            arstall = savedInstanceState.getString("arstall");
        }
        //Henter søkeresultatet fra Intent
        else {
            Intent intent = getIntent();

            if (intent != null){
                sokeNavn = intent.getStringExtra("sokenavn");
                Log.d(TAG, "Mottatt søkenavn: " + sokeNavn);

                sokePoststed = intent.getStringExtra("sokepoststed");
                Log.d(TAG, "Mottatt søkepoststed: " + sokePoststed);

                arstall = intent.getStringExtra("arstall");
                Log.d(TAG, "Mottatt årstall: " + arstall);

            }
            else {
                Log.d(TAG, "Intent = null");
            }

        }

        //henter recyclerview
        recyclerView = findViewById(R.id.spisested_recyclerView);

        //starter metode for datasøk
        hentSpisestedData();

        //setter swipemetoder. Kode hentet direkte fra forelesningsslides
        ItemTouchHelper helper= new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            //drag bytter plass på elementer i listen
            @Override
            public boolean onMove(
                    RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                int fraPos = viewHolder.getAdapterPosition();
                int tilPos = target.getAdapterPosition();
                Collections.swap(spisestedListe, fraPos, tilPos);
                spisestedAdapter.notifyItemMoved(fraPos, tilPos);
                return true;
            }

            // Sveip fjerner entries fra listen etter dialogpopup med ok/ikke
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                visDialog(viewHolder);

            }
        });

        helper.attachToRecyclerView(recyclerView);
    }

    //tar vare på variabler i viewet
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // Lagrer søkestrenger
        savedInstanceState.putString("sokenavn", sokeNavn);
        savedInstanceState.putString("sokepoststed", sokePoststed);
        savedInstanceState.putString("arstall", arstall);
    }

    // På retur hit fra TilsynListActivity trenger det å nullstille static tilsynslistevariabler
    // fra Tilsyn-objekt, ellers henger liste og hashmap igjen til neste runde
    //Disse listene er konstante så lenge samme spisested er aktivt.
    @Override
    public void onResume() {

        super.onResume();

        Tilsyn.ITEM_MAP.clear();
        Tilsyn.ITEMS.clear();


    }


    /******************************
     *
     * Dialogvindu, kode hentet fra:
     * https://medium.com/@suragch/making-an-alertdialog-in-android-2045381e2edb
     * https://stackoverflow.com/questions/50137310/confirm-dialog-before-swipe-delete-using-itemtouchhelper
     */

    private void visDialog(final RecyclerView.ViewHolder viewHolder) {

        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setTitle("Sikker?");
        builder.setMessage("Fjerne spisested fra listen?");
        // add a button
        builder.setPositiveButton("Jepps!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                spisestedListe.remove(viewHolder.getAdapterPosition());
                spisestedAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            }
        });
        // add a button
        builder.setNegativeButton("Nei forresten...", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                spisestedAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
            }
        });

        //bygger dialogvindu
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    /******************************
     *
     * Søker asynkront ved Volley-oppslag
     */
    private void hentSpisestedData() {

        //APIet godtar tomme søkefelt, bygger derfor en URL med både spisestednavn og poststed
        //Årstall må evt sorteres ut etter mottatt respons
        String URL = ENDPOINT + KOL_NAVN + "=" + sokeNavn
                        + "&" + KOL_POSTSTED + "=" + sokePoststed;

        Log.d(TAG, URL);

        //henter resultat asynkront vhja Volley
        if (isOnline()){
            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, this, this);
            queue.add(stringRequest);

        }
    }

    @Override
    public void onResponse(String response) {
        ArrayList<Spisested> liste = Spisested.listSpisesteder(response);

        bearbeidListe(liste);

    }

    private void bearbeidListe(ArrayList<Spisested> liste) {

        //dersom man har valgt et årstall må dette sorteres ut
        if (!arstall.equals("Alle")){

            //itererer igjennom listen og henter ut valgte årstall
            for (Spisested s : liste) {
                if (s.getArstall().equals(arstall)){
                    spisestedListe.add(s);
                }
            }

            //årstall = alle
        } else {
            spisestedListe = liste;
        }

        if (spisestedListe.size() == 0){
            finish();
            displayToast("Ingen spisesteder funnet!");

        }
        else {
            genererListeView();
        }

    }


    private void genererListeView() {
        Collections.sort(spisestedListe);
        spisestedAdapter = new SpisestedAdapter(this, spisestedListe);
        recyclerView.setAdapter(spisestedAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    /******************************
     * Utility-metoder
     *
     */

    // Checks network connection
    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        assert connMgr != null;
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    //viser toastmelding med valgt tekstinput
    public void displayToast(String message) {
        Toast.makeText(getApplicationContext(), message,
                Toast.LENGTH_SHORT).show();
    }

    /******************************
     * Menymetoder
     *
     */

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
