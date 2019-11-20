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
import android.widget.ExpandableListView;

import java.util.ArrayList;


/**
 * @henriette:
 *
 * Koden her er basert på Android Studio sin Master/detail-mal
 * PLUSS kode hentet fra eksempler på expandableList:
 * https://stackoverflow.com/questions/24083886/expandablelistview-in-fragment-issue
 * https://androidexample.com/Custom_Expandable_ListView_Tutorial_-_Android_Example/index.php?view=article_discription&aid=107&aaid=129
 * Kode fra dette eksemplet er beholdt med sine opprinnelige variabelnavn der det
 * er mulig, f.ex children, groups, rootView, lv //
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
    private ArrayList<TilsynsDetalj> detaljListe;

    //endpoint for CRUD-api
    private static final String ENDPOINT_DETALJ =
            "https://hotell.difi.no/api/json/mattilsynet/smilefjes/kravpunkter?tilsynid=";

    //logtag
    private static final String TAG = "JsonLog";

    View rootView;
    ExpandableListView lv;
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

        //starter metode for uthenting av tilsynsdetaljdata for gjeldende tilsynsId
        assert valgtTilsyn != null;
        hentTilsynsdetaljer(valgtTilsyn.getTilsynId());

        //setter opp hovedvindu (kode satt opp av master/detail-mal)
        Activity activity = this.getActivity();
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(valgtTilsyn.getNavn() + "/n" + valgtTilsyn.getDato());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.tilsyn_detail, container, false);

        return rootView;
    }

    //kode hentet fra Listview-eksempel
    //https://stackoverflow.com/questions/24083886/expandablelistview-in-fragment-issue
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lv = (ExpandableListView) view.findViewById(R.id.expListView);
        lv.setAdapter(new MyExpandableListAdapter(mContext, valgtTilsyn, detaljListe));
        lv.setGroupIndicator(null);

    }


    private void hentTilsynsdetaljer(String tilsynId) {

        String URL = ENDPOINT_DETALJ + tilsynId;
        Log.d(TAG, "DetaljURL: " + URL);

        if (isOnline()) {
            RequestQueue queue = Volley.newRequestQueue(mContext);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, this, this);
            queue.add(stringRequest);
        }

    }

    @Override
    public void onResponse(String response) {

        Log.d(TAG, "DetaljVolleyrespons: " + response);
        detaljListe = TilsynsDetalj.listTilsynsDetaljer(response);

    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }


    // Checks network connection
    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }




}
