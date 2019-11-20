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
import android.widget.TextView;

import java.util.ArrayList;

import static androidx.core.content.ContextCompat.getSystemService;


/**
 * A fragment representing a single Tilsyn detail screen.
 * This fragment is either contained in a {@link TilsynListActivity}
 * in two-pane mode (on tablets) or a {@link TilsynDetailActivity}
 * on handsets.
 */
public class TilsynDetailFragment extends Fragment implements Response.ErrorListener, Response.Listener<String> {

    //liste tilsynsdetaljobjekter
    private ArrayList<TilsynsDetalj> detaljListe = new ArrayList<>();

    //endpoint for CRUD-api
    private static final String ENDPOINT_DETALJ =
            "https://hotell.difi.no/api/json/mattilsynet/smilefjes/kravpunkter?tilsynid=";

    //logtag
    private static final String TAG = "JsonLog";

    private Context mContext;
    /**
     * The dummy content this fragment is presenting.
     */
    private Tilsyn valgtTilsyn;
    //private TilsynsDetalj mItem;

    //viewelementer
    TextView textView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TilsynDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getContext();

        //henter ut det valgte tilsynsobjektet
        assert getArguments() != null;
        valgtTilsyn = (Tilsyn) getArguments().getSerializable("valgtTilsyn");

        //starter metode for uthenting av tilsynsdetaljdata
        assert valgtTilsyn != null;
        hentTilsynsdetaljer(valgtTilsyn.getTilsynId());

        Activity activity = this.getActivity();
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(valgtTilsyn.getNavn() + " " + valgtTilsyn.getDato());
        }
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
        Log.d(TAG, "Antall vurderingspunkter: " + detaljListe.size());

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tilsyn_detail, container, false);

        //textView = rootView.findViewById(R.id.tilsyn_detail);

        if (valgtTilsyn != null && detaljListe != null) {
            Log.d("TAG", "Valgt tilsyn: " + valgtTilsyn.toString());
            textView.setText(valgtTilsyn.getTema1());
        }

        return rootView;
    }


}
