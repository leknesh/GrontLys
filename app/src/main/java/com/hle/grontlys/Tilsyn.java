package com.hle.grontlys;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//Klassen extender spisested, og benyttes for visning av hvert enkelt tilsynsobjekt
public class Tilsyn extends Spisested {

    private String tilsynId;
    private String karakter1;
    private String karakter2;
    private String karakter3;
    private String karakter4;

    //velger å også legge temabetegnelser i variabler, da disse betegnelsene kanskje kan variere over tid
    //muliggjør også valg av nynorsk?
    private String tema1;
    private String tema2;
    private String tema3;
    private String tema4;

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

    //logtag
    private static final String TAG = "JsonLog";


    //JSON-konstruktør
    public Tilsyn(JSONObject jsonObject){

        super(jsonObject);

        this.tilsynId = jsonObject.optString(KOL_TILSYNID);

        this.karakter1 = jsonObject.optString(KOL_KARAKTER1);
        this.karakter2 = jsonObject.optString(KOL_KARAKTER2);
        this.karakter3 = jsonObject.optString(KOL_KARAKTER3);
        this.karakter4 = jsonObject.optString(KOL_KARAKTER4);

        this.tema1 = jsonObject.optString(kol_tema1);
        this.tema2 = jsonObject.optString(kol_tema2);
        this.tema3 = jsonObject.optString(kol_tema3);
        this.tema4 = jsonObject.optString(kol_tema4);

    }

    /******************************
     * Metoder
     *
     */

    //Metode som bygger liste over tilsyn ut fra JSON-respons.
    // Responsen vil komme etter nytt Volley-søk med spesifikk tilsynsobjektId, dvs kun ett spisested
    public static ArrayList<Tilsyn> listTilsyn(String response) {

        ArrayList<Tilsyn> tilsynsListe = new ArrayList<>();
        //henter ut array av jsonobjekter
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("entries");

            //løper igjennom arrayet og oppretter tilsynsobjekter
            for (int i=0; i<jsonArray.length(); i++){
                Tilsyn tilsyn = new Tilsyn(jsonArray.getJSONObject(i));

                Log.d(TAG, tilsyn.toString());
                tilsynsListe.add(tilsyn);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "JSONException Tilsyn");
        }
        return tilsynsListe;
    }

    //hashmap til bruk i master/detail workflow
    public static Map<String, Tilsyn>  lagTilsynHashMap(ArrayList<Tilsyn> tilsynsListe){
        Map tilsynMap = new HashMap<String, Tilsyn>();
        for (Tilsyn t : tilsynsListe){
            tilsynMap.put(t.getDato(), t);
        }
        return tilsynMap;
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
     * Gettere og settere
     *
     */

    public String getTilsynId() {
        return tilsynId;
    }

    public String getKarakter1() {
        return karakter1;
    }

    public String getKarakter2() {
        return karakter2;
    }

    public String getKarakter3() {
        return karakter3;
    }

    public String getKarakter4() {
        return karakter4;
    }

    public String getTema1() {
        return tema1;
    }

    public String getTema2() {
        return tema2;
    }

    public String getTema3() {
        return tema3;
    }

    public String getTema4() {
        return tema4;
    }
}
