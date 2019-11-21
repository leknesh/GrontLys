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

    private static String kol_punktForklaring   = "tekst_no";
    private static String kol_punktNavn         = "kravpunktnavn_no";

    //logtag
    private static final String TAG = "JsonLog";


    //JSON-konstruktør
    public TilsynsDetalj(JSONObject jsonObject){

        this.tilsynDetaljId     = jsonObject.optString(KOL_TILSYNID);
        this.temaId             = jsonObject.optString(KOL_TEMAID).substring(0,1);
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

                //dersom karakter for punktet er 5, er punktet ikke vurdert og tas ikke med i resultatlisten!

                if (!tilsynsDetalj.punktKarakter.equals("5"))
                    detaljListe.add(tilsynsDetalj);
                }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "JSONException Tilsyn");
        }
        return detaljListe;
    }

    //metode som henter ut kun de rader som hører til et gitt tilsynspunkt
    public static ArrayList<TilsynsDetalj> hentChildRows(ArrayList<TilsynsDetalj> alleDetaljer, int temaId){
        String id = "" + temaId;
        ArrayList<TilsynsDetalj> childRows = new ArrayList<>();

        //gjennolløper inputArray og henter ut rader fra valgte temakategori
        for (TilsynsDetalj td : alleDetaljer){
            if (td.temaId.equals(id) ){
                childRows.add(td);
            }
        }

        return childRows;
    }

    @Override
    public String toString(){
        return "Tilsyndetaljid: " + tilsynDetaljId;
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
