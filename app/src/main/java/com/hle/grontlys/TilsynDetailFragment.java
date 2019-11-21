package com.hle.grontlys;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * @henriette:
 *
 * Koden her er basert på Android Studio sin Master/detail-mal
 * PLUSS kode hentet fra to eksempler på expandableList:
 * https://stackoverflow.com/questions/24083886/expandablelistview-in-fragment-issue
 * https://androidexample.com/Custom_Expandable_ListView_Tutorial_-_Android_Example/index.php?view=article_discription&aid=107&aaid=129
 * Kode fra eksempler er beholdt med sine opprinnelige variabelnavn der det
 * er mulig, f.ex children, groups, rootView, expListView //
 */

/**
 * Master/detail mal kommentar:
 * A fragment representing a single Tilsyn detail screen.
 * This fragment is either contained in a {@link TilsynListActivity}
 * in two-pane mode (on tablets) or a {@link TilsynDetailActivity}
 * on handsets.
 */

public class TilsynDetailFragment extends Fragment implements Response.ErrorListener, Response.Listener<String> {


    //Tilsynsobjekt som sendes inn i fragmentet
    private Tilsyn valgtTilsyn;

    //liste fylles med alle vurderte tilsynsdetaljobjekter for dette tilsynet
    private ArrayList<TilsynsDetalj> detaljListe = new ArrayList<>();

    //endpoint for CRUD-api
    private static final String ENDPOINT_DETALJ =
            "https://hotell.difi.no/api/json/mattilsynet/smilefjes/kravpunkter?tilsynid=";

    //logtag
    private static final String TAG = "JsonLog";

    View rootView;
    ExpandableListView expListView;
    MyExpandableListAdapter listAdapter;
    private Context mContext;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TilsynDetailFragment() {
    }

    //oncreate starter uthenting av tilsynsdetaljer ut fra tilsynsobjekt sendt fra parent activity
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getContext();

        //henter ut det valgte tilsynsobjektet
        assert getArguments() != null;
        valgtTilsyn = (Tilsyn) getArguments().getSerializable("valgtTilsyn");
        Log.d(TAG, "Fragment mottatt valgtTilsyn: " + valgtTilsyn.getTilsynId());

        //starter metode for uthenting av tilsynsdetaljdata for gjeldende tilsynsId
        assert valgtTilsyn != null;
        hentTilsynsdetaljer(valgtTilsyn.getTilsynId());

        //setter opp hovedvindu med toolbar (kode satt opp av master/detail-mal)
        Activity activity = this.getActivity();
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(valgtTilsyn.getNavn() + "\n" + valgtTilsyn.getDato());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.tilsyn_detail, null);
        expListView = (ExpandableListView) rootView.findViewById(R.id.expListView);

        return rootView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    //Starter henting av data fra kravpunkttabell fror valgt tilsynsid via volley
    private void hentTilsynsdetaljer(String tilsynId) {

        String URL = ENDPOINT_DETALJ + tilsynId;
        Log.d(TAG, "DetaljURL: " + URL);

        if (isOnline()) {
            RequestQueue queue = Volley.newRequestQueue(mContext);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, this, this);
            queue.add(stringRequest);
        }

    }

    //Svar på volleyrequest starter metode som genererer lister og listview
    @Override
    public void onResponse(String response) {

        Log.d(TAG, "DetaljVolleyrespons: " + response);

        bearbeidRespons(response);
    }


    @Override
    public void onErrorResponse(VolleyError error) {
        displayToast("Problemer med innhenting av data!");
    }

    //Metode som genererer resultatliste fra JSONrespons, oppretter expandablelistview og
    // tilhørende adapter
    private void bearbeidRespons(String response) {

        //henter alle entries i kravliste-tabell for dette tilsynet
        detaljListe = TilsynsDetalj.listTilsynsDetaljer(response);

        //genererer liste av temaresultat-objekter fra tilsyn-tabellen (<Temanavn, Temakarakter>)
        ArrayList<Tilsyn.Temaresultat> temaResultater = valgtTilsyn.getTilsynResultater();

        //henter inn expandablelist
        expListView = rootView.findViewById(R.id.expListView);

        //setter opp adapter som fyller expandableList med innhold
        expListView.setAdapter(new MyExpandableListAdapter(mContext, temaResultater, detaljListe));
        expListView.setGroupIndicator(null);
    }


    // Checks network connection
    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    //viser toastmelding med valgt tekstinput
    public void displayToast(String message) {
        Toast.makeText(mContext, message,
                Toast.LENGTH_SHORT).show();
    }




}
