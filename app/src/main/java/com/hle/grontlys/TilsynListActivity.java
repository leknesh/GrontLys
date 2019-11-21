package com.hle.grontlys;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.NavUtils;
import androidx.appcompat.app.ActionBar;

import android.view.MenuItem;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;


//@henriette:
// Activity som viser oversikt over alle tilsyn utført på et gitt spisested (sendt fra SokelisteActivity)
// Spisestedobjekt som sendes hit er kun ett av tilsynene som er gjennomført på spisestedet,
// gjør nytt oppslag i tilsynstabell for å hente full liste over tilsyn på valgte spisested.
// TilsynsListActivity er satt opp av Android Studio som Master/detail-mal, og en del av koden og
// kommentarene er autogenerert ved opprettelse av denne malen


/**
 * An activity representing a list of Tilsyner. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link TilsynDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class TilsynListActivity extends AppCompatActivity implements Response.ErrorListener, Response.Listener<String> {

    //input fra Intent
    private Spisested valgtSpisested;

    //Tilsyn og liste av tilsynobjekter
    private Tilsyn valgtTilsyn;
    //protected ArrayList<Tilsyn> tilsynsListe = new ArrayList<>();

    //endpoint for CRUD-api
    private static final String ENDPOINT_TILSYN =
            "https://hotell.difi.no/api/json/mattilsynet/smilefjes/tilsyn?tilsynsobjektid=";

    //logtag
    private static final String TAG = "JsonLog";

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tilsyn_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        /* Ikke implementert p.t
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        } */

        if (findViewById(R.id.tilsyn_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        //gjenoppretter variabler fra savedinstancestate hvis de er tilgjengelige
        if (savedInstanceState != null){
            valgtSpisested = (Spisested) savedInstanceState.getSerializable("valgtspisested");
         }
        else {
            //henter ellers inn valgt spisested fra intent
            Intent intent = getIntent();
            if (intent != null){
                valgtSpisested = (Spisested) intent.getSerializableExtra("valgtspisested");
                Log.d(TAG, "Valgt spisested: " + valgtSpisested.toString());
            }
            else {
                Log.d(TAG, "Tom intent");
            }
        }

        //metode som legger data om spisestedet inn i viewet
        fyllSpisestedCard();
        
        //starter henting av tilsynsdata
        hentTilsynsOversikt();

    }

    // Lagrer valgt spisested
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable("valgtspisested", valgtSpisested);

    }


    /**
     * Egne metoder og utilities
     */


    //legger info om spisestedet inn i viewet
    private void fyllSpisestedCard() {

        //View-elementer
        TextView navnTV, orgNrTV, adresseTV, postNrTV, postStedTV, arstallTV;

        //tilordning av viewelementer til layout-elementer
        navnTV = findViewById(R.id.navn_card);
        orgNrTV = findViewById(R.id.orgnr_card);
        adresseTV = findViewById(R.id.adresse_card);
        postNrTV = findViewById(R.id.postnr_card);
        postStedTV = findViewById(R.id.poststed_card);
        arstallTV = findViewById(R.id.dato_card);

        //knytter inn data fra valgt spisested.
        navnTV.setText(valgtSpisested.getNavn());
        orgNrTV.setText(valgtSpisested.getOrgNr());
        adresseTV.setText(valgtSpisested.getAdresse());
        postNrTV.setText(valgtSpisested.getPostNr());
        postStedTV.setText(valgtSpisested.getPostSted());
        arstallTV.setText("");

    }


    //henter ut oversiktsdata fra tilsynstabell for gjeldende spisested
    private void hentTilsynsOversikt() {

        //henter først alle tilsyn hos det valgte spisestedet
        String URL_tilsyn = ENDPOINT_TILSYN + valgtSpisested.getObjektId();

        if (isOnline()) {
            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_tilsyn, this, this);
            queue.add(stringRequest);
        }
    }


    @Override
    public void onResponse(String response) {

        //bygger liste og hashmap av tilsyn ved respons
        Tilsyn.listTilsyn(response);

        //sjekk av status på liste/hashmap
        Log.d(TAG, "Tilsynsliste: " + Tilsyn.ITEMS.size()
            + ", tilsynshashmap: " + Tilsyn.ITEM_MAP.size());

        //view til listen
        View recyclerView = findViewById(R.id.tilsyn_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        displayToast("Problemer med datainnhenting!");
    }

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




    /**
     * Defaultmetoder
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView(RecyclerView recyclerView) {

        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, Tilsyn.ITEMS, mTwoPane));
    }

    /**
     * Autogenerert kode for oppbygging av Master/detail workflow views,
     * supplert med de rette variabler og klasser.
     * Activity og fragment kjøres forskjellig her avhengig av skjermstørrelse.
     * Valgt tilsyn sendes med i begge tilfeller.
     */

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final TilsynListActivity mParentActivity;
        private final List<Tilsyn> mValues;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //set og get tag metoder i viewklassen, sørger for tilordning av klikket element
                Tilsyn tilsyn = (Tilsyn) view.getTag();

                Log.d(TAG, "Valgt tilsyn: " + tilsyn.toString() );

                //hvis stor skjerm/tablet kjøres activity og fragment i samme view
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(TilsynDetailFragment.ARG_ITEM_ID, tilsyn.getTilsynId());
                    TilsynDetailFragment fragment = new TilsynDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.tilsyn_detail_container, fragment)
                            .commit();
                //ved liten skjerm startes først activity, deretter fragment fra activityen
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, TilsynDetailActivity.class);
                    intent.putExtra(TilsynDetailFragment.ARG_ITEM_ID, tilsyn.getTilsynId());
                    Log.d(TAG, "Extra puttet: "+ tilsyn.getTilsynId());
                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(TilsynListActivity parent,
                                      List<Tilsyn> items,
                                      boolean twoPane) {
            Log.d(TAG, "SimpleRecyclerViewAdapter: " + items.size());
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.tilsyn_list_content, parent, false);
            return new ViewHolder(view);
        }


        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            valgtTilsyn = mValues.get(position);
            String karakter = Tilsyn.ITEMS.get(position).getTotKarakter();
            String datoText = getString(R.string.tilsynsdato) + Tilsyn.ITEMS.get(position).getDato();

            Log.d(TAG, "OnBindViewHolder datotekst:" + datoText);

            holder.mDatoView.setText(datoText);
            //ref karakterskala beskrevet her: https://data.norge.no/data/mattilsynet/smilefjestilsyn-p%C3%A5-serveringssteder

            switch (karakter) {
                case "0":
                case "1":
                    holder.mKarakterView.setImageResource(R.drawable.ic_sentiment_satisfied_green_60dp);
                    break;
                case "2":
                    holder.mKarakterView.setImageResource(R.drawable.ic_sentiment_neutral_yellow_60dp);
                    break;
                case "3":
                    holder.mKarakterView.setImageResource(R.drawable.ic_sentiment_dissatisfied_red_60dp);
                    break;
                default:
                    holder.mKarakterView.setImageResource(R.drawable.ic_highlight_off_blue_60dp);
                    break;
            }

            Log.d(TAG, "Switch på karakter ok, kar " + karakter);

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            Log.d(TAG, "getItemCount: " + mValues.size());
            return mValues.size();

        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView mDatoView;
            ImageView mKarakterView;

            ViewHolder(View view) {
                super(view);
                Log.d(TAG, "ViewHolder: ");
                mDatoView = view.findViewById(R.id.dato_tilsynlist);
                mKarakterView = view.findViewById(R.id.karakter_tilsynList);
            }
        }
    }
}
