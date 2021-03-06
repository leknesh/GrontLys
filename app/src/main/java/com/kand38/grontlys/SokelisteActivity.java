package com.kand38.grontlys;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

/******************************
 *
 * Activity som initierer søk og genererer resultatlister som vises frem i
 * et recyclerview ved hjelp av en adapterklasse
 */

public class SokelisteActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private String sokeNavn, sokePoststed, arstall, mittPostnummer;
    protected static int nynorsk;
    private String url;
    private int sidetall;
    private int soketype;

    private RecyclerView recyclerView;
    private ArrayList<Spisested> spisestedListe = new ArrayList<>();
    private SpisestedAdapter spisestedAdapter;

    //endpoint for CRUD-api
    private static final String ENDPOINT = "https://hotell.difi.no/api/json/mattilsynet/smilefjes/tilsyn?";
    private static final String ENDPOINT_ADRESSE = "https://ws.geonorge.no/adresser/v1/punktsok?";
    //søkeparametre for api
    private static final String KOL_NAVN        = "navn";
    private static final String KOL_POSTSTED    = "poststed";
    private static final String KOL_POSTNR      = "postnr";
    private static final String KOL_DATO        = "dato";


    //logtag
    private static final String TAG = "JsonLog";

    /******************************
     *
     * onCreate henter inn søkevariabler fra intent evt savedInstancestate. Dersom det skal gjøres
     * lokasjonssøk hentes først lokalt postnummer fra Geonorge før hovedsøk starter. Dersom det er
     * standard søk starter hovedsøk med en gang på bakgrunn av innhentede variabler.
     * Det meste av layouthåndtering gjøres av spisestedadapter, men her settes lyttermetoder for
     * sveip av resultatcards
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sokeliste);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //henter views
        recyclerView = findViewById(R.id.spisested_recyclerView);
        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(this);

        Intent intent = getIntent();

        //henter inndata for acivity. Sjekker om man har lokasjonssøk eller vanlig søk
        if (intent != null) {
            soketype = intent.getIntExtra("soketype", 0);
            nynorsk = intent.getIntExtra("nynorsk", 0);

            //hvis man har valgt lokasjonssøk hentes lokasjonen fra intent, og postnummeroppslag kalles
            if (soketype == MainActivity.INTENT_LOKASJON) {
                Location myLocation = intent.getParcelableExtra("lokasjon");

                //må ha dummy-verdi, lokasjonssøk sender inn "Alle"
                arstall = "Alle";

                //kaller metode som henter postnummer og deretter starter spisestedsøk
                if (myLocation != null) {
                    finnPostnummer(myLocation);
                }
                else {
                    //hvis problemer med lokasjon stoppes søkelisteactivity
                    displayToast("Prpblemer med lokasjonsinnhenting");
                    finish();
                }


            }
            //hvis standard søk hentes navn/sted/år fra intent
             else if (soketype == MainActivity.INTENT_STANDARD) {

                //gjenoppretter variabler fra savedinstancestate hvis de er tilgjengelige,
                //eller henter fra intent hvis ikke
                if (savedInstanceState != null) {
                    sokeNavn = savedInstanceState.getString("sokenavn");
                    sokePoststed = savedInstanceState.getString("sokepoststed");
                    arstall = savedInstanceState.getString("arstall");
                    nynorsk = savedInstanceState.getInt("nynorsk");
                } else {
                    sokeNavn = intent.getStringExtra("sokenavn");
                    sokePoststed = intent.getStringExtra("sokepoststed");
                    arstall = intent.getStringExtra("arstall");
                    nynorsk = intent.getIntExtra("nynorsk", 0);
                    Log.d(TAG, "Mottatt nynorskvalg: " + nynorsk);
                }

                //starter metode for datasøk
                byggSokeUrl();
            }
        }

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
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // Lagrer søkestrenger
        savedInstanceState.putString("sokenavn", sokeNavn);
        savedInstanceState.putString("sokepoststed", sokePoststed);
        savedInstanceState.putString("arstall", arstall);
        savedInstanceState.putInt("nynorsk", nynorsk);
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
     * Dialogvindu for verifisering av cardswipe, kode hentet fra:
     * https://medium.com/@suragch/making-an-alertdialog-in-android-2045381e2edb
     * https://stackoverflow.com/questions/50137310/confirm-dialog-before-swipe-delete-using-itemtouchhelper
     */

    private void visDialog(final RecyclerView.ViewHolder viewHolder) {

        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
     * Metoden henter ut postnummer fra Geonorge-database, kalles ved søk på lokasjon
     * som ble hentet inn fra MainActivity
     * */
    private void finnPostnummer(Location myLocation) {

        mittPostnummer = "";

        String postNrUrl = ENDPOINT_ADRESSE
                + "radius=1000&lat=" + myLocation.getLatitude()
                + "&lon=" + myLocation.getLongitude()
                + "&treffPerSide=10&side=0&asciiKompatibel=true";

        Log.d(TAG, postNrUrl);

        //velger å kjøre et internt volleysøk med egen listener i denne metoden for å ikke
        //rote til respons fra tilsynssøkene
        if (isOnline()) {

            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, postNrUrl, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    try {
                        //responsen er nøstet, må hente ut ett adresseobjekt
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray jsonArray = jsonObject.getJSONArray("adresser");

                        //bruker første treff i listen
                        JSONObject adresse = jsonArray.getJSONObject(0);

                        mittPostnummer = adresse.optString("postnummer");

                        byggSokeUrl();

                    }
                    //avslutter activity dersom respons fra api ikke lar seg avlese
                    catch (JSONException ex) {
                        displayToast("Finner ikke din adresse!");
                        finish();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    displayToast("Finner ikke din adresse!");
                    finish();
                }
            });

            queue.add(stringRequest);
        }
    }

    /******************************
     *
     * Metoden bygger opp url for bruk i søk, avhengig av brukerinput
     */
    private void byggSokeUrl() {

        url = ENDPOINT;

        //hvis ingen lokasjon skal søket utføres med søkevariablene
        if (soketype == MainActivity.INTENT_STANDARD) {

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

        //hvis det er gjort lokasjonssøk bygges url på basis av postnummer hentet ut av Geonorge-db
        else if (soketype == MainActivity.INTENT_LOKASJON) {
            //velger å gjøre et wildcard-søk på de tre første sifferne i postnummeret
            //for høyere sannsynlighet for treff

            String sokestreng ="";

            if(mittPostnummer.isEmpty()) {
                displayToast("Beklager, problem med lokasjonstjenester!");
                finish();
            } else {
                sokestreng = "%22" + mittPostnummer.substring(0, 3) + "*%22";
            }

            url += KOL_POSTNR + "=" + sokestreng;

            Log.d(TAG, "Lokasjonssøk: " + url);
        }

        //hvis verken lokasjon-eller standardsøk :)
        else {
            displayToast("Beklager,jeg forstår ikke hva du vil!");
            finish();
        }

        //ved oppstart av søket søkes det på side 1 i datasettet
        sidetall = 1;
        startSok(url, sidetall);
    }


    //metode sender søkestreng til tilsynsdatabasen. Respons starter metode som
    //bygger liste av spisesteder. Sender inn sidetall, denne telles opp i listegenerator
    //og muliggjør søk i paginert api
    private void startSok(String url, int sidetall) {

        //henter resultat asynkront vhja Volley
        if (isOnline()){

            url += "&page=" + sidetall;
            Log.d(TAG, url);

            RequestQueue queue = Volley.newRequestQueue(this);

            //håndterer respons lokalt i metoden da det er to forskjellige apier som
            //kan gi respons i denne activityen
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        //sender respons videre til behandling
                        opprettListe(response);
                    }},
                new Response.ErrorListener() {
                    //ved feil avsluttes aktiviteten
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        displayToast("Problemer med å søke i tilsynsdatabasen");
                        finish();
                    }
                });

            queue.add(stringRequest);
        }
        //hvis ikke online
        else {
            displayToast("Problemer med nettilgang");
            finish();
        }
    }

    //Metode som behandler svar fra tilsynsdatabasen.
    //Bygger liste over spisesteder, sjekker antall og fyrer evt nytt søk hvis
    //100treff, da er det flere sider i søkeresultatet

    private void opprettListe(String response) {

        //genererer nye spisestedobjekter fra volleyresponse, legger inn i ny liste
         ArrayList<Spisested> nyListe = Spisested.listSpisesteder(response);

        //hvis ingen treff i listen er søkeprosess ferdig uten treff, og aktiviteten lukkes
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

            //sorterer ut individuelle spisesteder når søket er ferdig,
            //og kaller visningsmetode
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
     * Filter-listenermetoder, kjører filtrering
     *  ved tasting i søkevindu.
     */

    @Override
    public boolean onQueryTextSubmit(String query) {

        if (spisestedAdapter != null)
            spisestedAdapter.getFilter().filter(query);
        return false;
    }
    //denne håndterer bla. backspace men krasjer applikasjon med nullpointerexception
    //ved rotasjon av skjerm. Derfor sjekkes adapter før kall på filter.
    @Override
    public boolean onQueryTextChange(String newText) {

        if (spisestedAdapter != null)
            spisestedAdapter.getFilter().filter(newText);

        return false;
    }
}
