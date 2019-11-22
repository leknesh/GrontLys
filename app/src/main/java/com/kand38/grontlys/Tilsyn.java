package com.kand38.grontlys;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//Klassen extender spisested, og benyttes for visning av hvert enkelt tilsynsobjekt
public class Tilsyn extends Spisested implements Serializable {

    private String tilsynId;

    private ArrayList<Temaresultat> tilsynResultater;

    private static final String KOL_TILSYNID    = "tilsynid";
    private static final String KOL_KARAKTER1   = "karakter1";
    private static final String KOL_KARAKTER2   = "karakter2";
    private static final String KOL_KARAKTER3   = "karakter3";
    private static final String KOL_KARAKTER4   = "karakter4";

    //kolonnenavn, kan endres i konstruktør ved valg av nynorsk
    private static String kol_tema1  = "tema1_no";
    private static String kol_tema2  = "tema2_no";
    private static String kol_tema3  = "tema3_no";
    private static String kol_tema4  = "tema4_no";


    //hashmap til bruk for master/detail workflow setup
    public static final Map<String, Tilsyn> ITEM_MAP = new HashMap<>();
    public static final ArrayList<Tilsyn> ITEMS = new ArrayList<>();

    //logtag
    private static final String TAG = "JsonLog";


    //JSON-konstruktør
    public Tilsyn(JSONObject jsonObject){

        //denne gir navn/adr/totalkarakter mm
        super(jsonObject);

        //bruker andre kolonnebetegnelser hvis nynorsk er valgt
        if (MainActivity.brukNynorsk){
            kol_tema1 = "tema1_nn";
            kol_tema2 = "tema2_nn";
            kol_tema3 = "tema3_nn";
            kol_tema4 = "tema4_nn";
        }

        //henter ut tilsynsId, temanavn og tilhørende karakter
        this.tilsynId = jsonObject.optString(KOL_TILSYNID);

        this.tilsynResultater = new ArrayList<>();

        //legger inn resultater fra de enkelte temagruppene
        tilsynResultater.add(new Temaresultat(jsonObject.optString(kol_tema1), jsonObject.optString(KOL_KARAKTER1) ));
        tilsynResultater.add(new Temaresultat(jsonObject.optString(kol_tema2), jsonObject.optString(KOL_KARAKTER2) ));
        tilsynResultater.add(new Temaresultat(jsonObject.optString(kol_tema3), jsonObject.optString(KOL_KARAKTER3) ));
        tilsynResultater.add(new Temaresultat(jsonObject.optString(kol_tema4), jsonObject.optString(KOL_KARAKTER4) ));

    }

    /******************************
     * Metoder
     *
     */

    //Metode som bygger liste over tilsyn ut fra JSON-respons.
    // Responsen vil komme etter nytt Volley-søk med spesifikk tilsynsobjektId, dvs kun ett spisested
    public static void listTilsyn(String response) {

        //henter ut array av jsonobjekter
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("entries");

            //løper igjennom arrayet og oppretter tilsynsobjekter
            for (int i=0; i<jsonArray.length(); i++){
                Tilsyn tilsyn = new Tilsyn(jsonArray.getJSONObject(i));

                //legger forekomsten inn i listene
                ITEM_MAP.put(tilsyn.tilsynId, tilsyn);
                ITEMS.add(tilsyn);
                ITEMS.sort(Spisested.DatoKomparator);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "JSONException Tilsyn");
        }

    }


    @Override
    public String toString() {
        return "Tilsyn{" +
                "tilsynId='" + tilsynId + '\'' +
                ", Navn='" + getNavn() + '\'' +
                ", Årstall='" + getArstall() + '\'' +
                '}';
    }

    /******************************
     * Gettere
     *
     */

    public String getTilsynId() {
        return tilsynId;
    }

    public ArrayList<Temaresultat> getTilsynResultater(){
        return tilsynResultater;
    }


    //indre klasse for å bygge arrayliste av resultater
    public static class Temaresultat implements Serializable {
        private String temanavn;
        private String temakarakter;

        private Temaresultat(String temanavn, String temakarakter){
            this.temanavn = temanavn;
            this.temakarakter = temakarakter;
        }

        public String getTemanavn() {
            return temanavn;
        }

        public void setTemanavn(String temanavn) {
            this.temanavn = temanavn;
        }

        public String getTemakarakter() {
            return temakarakter;
        }

        public void setTemakarakter(String temakarakter) {
            this.temakarakter = temakarakter;
        }
    }
}
