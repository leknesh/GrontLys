package com.kand38.grontlys;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SokelisteActivity extends AppCompatActivity implements Response.Listener<String>, Response.ErrorListener,
        SearchView.OnQueryTextListener {

    private String sokeNavn, sokePoststed, arstall;
    private boolean bruknynorsk;
    private Location myLocation;
    private String url;
    private int sidetall;

    private RecyclerView recyclerView;
    private SearchView searchView;
    private ArrayList<Spisested> spisestedListe = new ArrayList<>();
    private SpisestedAdapter spisestedAdapter;

    //endpoint for CRUD-api
    private static final String ENDPOINT = "https://hotell.difi.no/api/json/mattilsynet/smilefjes/tilsyn?";
    //søkeparametre for api
    private static final String KOL_NAVN        = "navn";
    private static final String KOL_POSTSTED    = "poststed";
    private static final String KOL_POSTNR      = "postnr";
    private static final String KOL_DATO        = "dato";


    //logtag
    private static final String TAG = "JsonLog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sokeliste);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //henter views
        recyclerView = findViewById(R.id.spisested_recyclerView);
        searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(this);


        Intent intent = getIntent();

        //henter inndata for viewet. Sjekker om man har lokasjonssøk eller vanlig søk
        if (intent != null) {
            int soketype = intent.getIntExtra("soketype", 0);
            Log.d(TAG, "Mottatt søketype: " + soketype);

            //hvis man har valgt lokasjonssøk hentes lokasjonen fra intent
            if (soketype == MainActivity.INTENT_LOKASJON) {
                myLocation = (Location) intent.getParcelableExtra("lokasjon");

                //må ha dummy-verdi, lokasjonssøk sender inn "Alle"
                arstall = intent.getStringExtra("arstall");
                Log.d(TAG, "Mottatt lokasjon: " + myLocation.toString());

            //hvis standard søk hentes navn/sted/år
            } else if (soketype == MainActivity.INTENT_STANDARD) {

                //gjenoppretter variabler fra savedinstancestate hvis de er tilgjengelige
                if (savedInstanceState != null) {
                    sokeNavn = savedInstanceState.getString("sokenavn");
                    sokePoststed = savedInstanceState.getString("sokepoststed");
                    arstall = savedInstanceState.getString("arstall");
                    bruknynorsk = savedInstanceState.getBoolean("nynorsk");
                } else {

                    sokeNavn = intent.getStringExtra("sokenavn");
                    Log.d(TAG, "Mottatt søkenavn: " + sokeNavn);

                    sokePoststed = intent.getStringExtra("sokepoststed");
                    Log.d(TAG, "Mottatt søkepoststed: " + sokePoststed);

                    arstall = intent.getStringExtra("arstall");
                    Log.d(TAG, "Mottatt årstall: " + arstall);

                    bruknynorsk = intent.getBooleanExtra("nynorsk", false);
                    Log.d(TAG, "Mottatt nynorskvalg: " + bruknynorsk);
                }

            }
        }

        //starter metode for datasøk
        byggSokeUrl();

        //setter swipemetoder for cards. Kode hentet direkte fra forelesningsslides
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
        savedInstanceState.putBoolean("nynorsk", bruknynorsk);
    }

    // På retur hit fra TilsynListActivity trengs det å nullstille static tilsynslistevariabler
    // fra Tilsyn-objekt, ellers henger liste og hashmap igjen til neste runde
    // Disse listene er konstante så lenge samme spisested er aktivt.
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
     * Metoden bygger opp url for bruk i søk, avhengig av brukerinput
     */
    private void byggSokeUrl() {
        url = ENDPOINT;

        //hvis ingen lokasjon skal løket utføres med søkevariablene
        if (myLocation == null) {

            //APIet godtar tomme søkefelt, bygger derfor en url med både spisestednavn og poststed
            url += KOL_NAVN + "=" + sokeNavn + "&" + KOL_POSTSTED + "=" + sokePoststed;

            Log.d(TAG, url);

            //apiet godtar wildcardsøk i kolonne dato (ddmmyyyy), legger inn valgt år som wildcardsøk
            //%22 (") gir "innramming" av wildcardsøkestreng
            if (!arstall.equals("Alle")){
                url += "&" + KOL_DATO + "=%22****" + arstall + "%22";
            }

            Log.d(TAG, url);
        }
        //hvis det er gjort lokasjonssøk bygges url på basis av postnummer
        else {
            //bruker Geocoder for å oversette koordinater til adresse
            Geocoder coder = new Geocoder(getApplicationContext());
            List<Address> geocodeResults;

            try {
                if (Geocoder.isPresent()){
                    //henter inn 2 adresser i nærheten av lokasjo
                    geocodeResults = coder.getFromLocation(myLocation.getLatitude(), myLocation.getLongitude(), 2);
                    //henter postnummer fra det første treffet
                    String mittPostNummer = geocodeResults.get(0).getPostalCode();

                    //velger å gjøre et wildcard-søk på de tre første sifferne i postnummeret
                    //for høyere sannsynlighet for treff
                    String sokestreng = "%22" + mittPostNummer.substring(0,3) + "*%22";
                    url += KOL_POSTNR + "=" + sokestreng;

                    Log.d(TAG, "Lokasjonssøk: " + url);
                }
            } catch (IOException ex){
                displayToast("Problemer med lokasjonsdata!");
            }

        }
        //ved oppstart av søket søkes det på side 1 i datasettet
        sidetall = 1;
        startSok(url, sidetall);

    }

    //
    private void startSok(String url, int sidetall) {
        //henter resultat asynkront vhja Volley
        if (isOnline()){
            url += "&page=" + sidetall;
            Log.d(TAG, url);

            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url, this, this);
            queue.add(stringRequest);
        }
    }

    //behandling av svar på volley
    @Override
    public void onResponse(String response) {

        opprettListe(response);

    }

    //ved volleyfeil
    @Override
    public void onErrorResponse(VolleyError error) {
        displayToast("Problemer med innhenting av data");
    }

    private void opprettListe(String response) {

        //genererer nye spisestedobjekter fra volleyresponse inn i ny liste

        ArrayList<Spisested> nyListe = Spisested.listSpisesteder(response);
        Log.d(TAG, "OnResponse, spisestedListe: " + spisestedListe.size());
        Log.d(TAG, "OnResponse, nyListe: " + nyListe.size());

        //hvis ingen treff i listen er søket ferdig og aktiviteten lukkes
        if (nyListe.size() == 0 && spisestedListe.size() == 0){
            finish();
            displayToast("Ingen spisesteder funnet!");
        }

        //hvis søkeresultat er 100 eller fler må søk repeteres med bruk av paginering
        else if (nyListe.size() > 99){

            //legger resultat fra gjeldende søk til på hovedlisten
            spisestedListe.addAll(nyListe);

            //øker sideteller og starter nytt volleysøk, gir neste side i det paginerte datasettet
            sidetall ++;
            startSok(url, sidetall);
        }

        //hvis det er mindre enn 100 treff er søket ferdig og viewet bygges opp
        //ved flersidig søk vil siste side ende opp her
        else {
            spisestedListe.addAll(nyListe);

            //sorterer ut individuelle spisesteder når søket er ferdig
            spisestedListe = Spisested.hentIndividuelle(spisestedListe);
            genererListeView();
        }
    }


    //viser liste med alle individuelle spisesteder funnet
    private void genererListeView() {

        displayToast("Antall treff: " + spisestedListe.size());

        //sorterer alfabetisk på navn
        Collections.sort(spisestedListe);

        spisestedAdapter = new SpisestedAdapter(this, spisestedListe);
        recyclerView.setAdapter(spisestedAdapter);

        //henter inn antall kolonner fra values, verdi 2 i landscape
        // https://stackoverflow.com/questions/29579811/changing-number-of-columns-with-gridlayoutmanager-and-recyclerview
        int columns = getResources().getInteger(R.integer.list_columns);
        recyclerView.setLayoutManager(new GridLayoutManager(this, columns));
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

        return super.onOptionsItemSelected(item);

    }

    /******************************
     * Filtermetoder
     *
     */

    @Override
    public boolean onQueryTextSubmit(String query) {
        spisestedAdapter.getFilter().filter(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        spisestedAdapter.getFilter().filter(newText);
        return false;
    }
}
