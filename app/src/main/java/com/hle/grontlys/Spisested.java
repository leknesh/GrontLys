package com.hle.grontlys;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class Spisested implements Serializable {
    private String orgNr;
    private String navn;
    private String adresse;
    private String postNr;
    private String postSted;
    private String tilsynId;
    private String arstall;
    private String totKarakter;

    //json-keys
    private static final String KOL_NAVN        = "navn";
    private static final String KOL_ORGNR       = "orgnummer";
    private static final String KOL_ADRESSE     = "adrlinje1";
    private static final String KOL_POSTNR      = "postnr";
    private static final String KOL_POSTSTED    = "poststed";
    private static final String KOL_TILSYNID    = "tilsynid";
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
        this.tilsynId = jsonObject.optString(KOL_TILSYNID);
        this.totKarakter = jsonObject.optString(KOL_KARAKTER);
        //plukker ut årstallet (kanskje)
        this.arstall = jsonObject.optString(KOL_DATO).substring(4);

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

    public String getTilsynId() {
        return tilsynId;
    }

    public String getArstall() {
        return arstall;
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

                //ønsker KUN individuelle spisesteder i denne omgang, sorterer på OrgNr
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

        return (this.orgNr.equals(s.getOrgNr()));

    }

    @Override
    public String toString() {
        return "Spisested{" +
                "orgNr='" + orgNr + '\'' +
                ", navn='" + navn + '\'' +
                ", År='" + arstall + '\'' +
                '}';
    }
}
