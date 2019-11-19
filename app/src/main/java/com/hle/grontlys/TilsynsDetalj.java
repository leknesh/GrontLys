package com.hle.grontlys;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TilsynsDetalj {

    //knyttes mot tilsynsobjekt
    private String tilsynDetaljId;

    //brukes for å linke underkategori til hovedtema, hentes fra "ordningsverdi"
    private String temaId;

    private String punktNavn;
    private String punktKarakter;
    private String punktForklaring;

    private static final String KOL_TILSYNID    = "tilsynid";
    private static final String KOL_TEMAID      = "ordningsverdi";
    private static final String KOL_KARAKTER    = "karakter";

    private static String kol_punktNavn         = "kravpunktnavn_no";
    private static String kol_punktForklaring   = "tekst_no";

    //logtag
    private static final String TAG = "JsonLog";


    //JSON-konstruktør
    public TilsynsDetalj(JSONObject jsonObject){

        this.tilsynDetaljId     = jsonObject.optString(KOL_TILSYNID);
        this.temaId             = jsonObject.optString(KOL_TEMAID);
        this.punktKarakter      = jsonObject.optString(KOL_KARAKTER);

        //kan legge inn if/else mot en lagret målform-preference!!!
        this.punktNavn          = jsonObject.optString(kol_punktNavn);
        this.punktForklaring    = jsonObject.optString(kol_punktForklaring);

    }

    /******************************
     * Metoder
     *
     */

    //Metode som bygger liste over tilsynsdetaljer ut fra JSON-respons.
    // Responsen vil komme etter nytt Volley-søk med spesifikk tilsynsId, dvs kun ett spesifikt tilsyn
    public static ArrayList<TilsynsDetalj> listTilsynsDetaljer(String response) {

        ArrayList<TilsynsDetalj> detaljListe = new ArrayList<>();
        //henter ut array av jsonobjekter
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("entries");

            //løper igjennom arrayet og oppretter tilsynsobjekter
            for (int i=0; i<jsonArray.length(); i++){
                TilsynsDetalj tilsynsDetalj = new TilsynsDetalj(jsonArray.getJSONObject(i));

                Log.d(TAG, tilsynsDetalj.toString());
                detaljListe.add(tilsynsDetalj);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "JSONException Tilsyn");
        }
        return detaljListe;
    }

    /******************************
     * Gettere og settere
     *
     */

    public String getTilsynDetaljId() {
        return tilsynDetaljId;
    }

    public String getTemaId() {
        return temaId;
    }

    public String getPunktNavn() {
        return punktNavn;
    }

    public String getPunktKarakter() {
        return punktKarakter;
    }

    public String getPunktForklaring() {
        return punktForklaring;
    }
}
