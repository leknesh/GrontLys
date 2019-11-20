package com.hle.grontlys;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class Spisested implements Comparable, Serializable {

    private String orgNr;
    private String navn;
    private String adresse;
    private String postNr;
    private String postSted;
    private String objektId;
    private String dato;
    private String totKarakter;

    //json-keys
    private static final String KOL_NAVN        = "navn";
    private static final String KOL_ORGNR       = "orgnummer";
    private static final String KOL_ADRESSE     = "adrlinje1";
    private static final String KOL_POSTNR      = "postnr";
    private static final String KOL_POSTSTED    = "poststed";
    private static final String KOL_OBJEKTID    = "tilsynsobjektid";
    private static final String KOL_DATO        = "dato";
    private static final String KOL_KARAKTER    = "total_karakter";

    //logtag
    private static final String TAG = "JsonLog";

    /******************************
     * Konstruktør
     *
     */

    //JSON-konstruktør
    public Spisested(JSONObject jsonObject){
        this.orgNr = jsonObject.optString(KOL_ORGNR);
        this.navn = jsonObject.optString(KOL_NAVN);
        this.adresse = jsonObject.optString(KOL_ADRESSE);
        this.postNr = jsonObject.optString(KOL_POSTNR);
        this.postSted = jsonObject.optString(KOL_POSTSTED);
        this.objektId = jsonObject.optString(KOL_OBJEKTID);
        this.totKarakter = jsonObject.optString(KOL_KARAKTER);
        this.dato = jsonObject.optString(KOL_DATO);

    }

    /******************************
     * Gettere og settere
     *
     */
    public String getOrgNr() {
        return orgNr;
    }

    public String getNavn() {
        return navn;
    }

    public String getAdresse() {
        return adresse;
    }

    public String getPostNr() {
        return postNr;
    }

    public String getPostSted() {
        return postSted;
    }

    public String getObjektId() {
        return objektId;
    }

    public String getDato() {

        return dato.substring(0,2)
                + "." + dato.substring(2,4)
                + "." + dato.substring(4);
    }

    public String getArstall() {
        return dato.substring(4);
    }

    public String getTotKarakter() {
        return totKarakter;
    }

    /******************************
     * Metoder
     *
     */

    //Metode som bygger liste over INDIVIDUELLE spisesteder ut fra JSON-respons

    public static ArrayList<Spisested> listSpisesteder(String response) {

        ArrayList<Spisested> spisesteder = new ArrayList<>();
        //henter ut array av jsonobjekter
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("entries");

            //løper igjennom arrayet ooppretter spisesteder
            for (int i=0; i<jsonArray.length(); i++){
                Spisested spisested = new Spisested(jsonArray.getJSONObject(i));

                //ønsker KUN individuelle spisesteder i denne omgang, sorterer på ObjektId
                if (!spisesteder.contains(spisested)){

                    Log.d(TAG, spisested.toString());
                    spisesteder.add(spisested);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "JSONException");
        }
        return spisesteder;
    }


    //brukes for sjekk av om et orgnummer finnes i gjeldende treffliste
    @Override
    public boolean equals(Object o){
        Spisested s = (Spisested) o;

        return (this.objektId.equals(s.getObjektId()));

    }

    @Override
    public String toString() {
        return "Spisested{" +
                "orgNr='" + orgNr + '\'' +
                ", navn='" + navn + '\'' +
                ", År='" + dato + '\'' +
                '}';
    }

    @Override
    public int compareTo(Object o) {
        Spisested s = (Spisested) o;
        if (navn.equals(s.getNavn()))
            return 0;
        else if (navn.compareTo(s.getNavn()) > 0)
            return 1;
        else
            return -1;
    }
}
