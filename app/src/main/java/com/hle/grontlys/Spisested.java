package com.hle.grontlys;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.SpinnerAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import static java.lang.Integer.parseInt;

public class Spisested implements Serializable, Comparable<Spisested> {

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

    public String getDatoTekst() {

        return dato.substring(0,2)
                + "." + dato.substring(2,4)
                + "." + dato.substring(4);
    }

    public Date getDato(){
        DateFormat df = new SimpleDateFormat("ddMMyyyy");

        //initierer datovariabel med dagens dato
        Date date = new Date();

        try {
            date = df.parse(dato);
        } catch (ParseException pe){
            Log.d(TAG, "Datoproblem: " + dato);
        }
        return date;
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

    //metode som bygger arrayliste av jsonRespons
    public static ArrayList<Spisested> listSpisesteder(String response) {
        ArrayList<Spisested> liste = new ArrayList<>();
        //henter ut array av jsonobjekter
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("entries");

            //løper igjennom arrayet ooppretter spisesteder
            for (int i = 0; i < jsonArray.length(); i++) {
                Spisested spisested = new Spisested(jsonArray.getJSONObject(i));
                liste.add(spisested);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "JSONException");
        }
        return liste;
    }

    //Metode som fletter to lister og henter ut INDIVIDUELLE spisesteder

    public  static ArrayList<Spisested> hentIndividuelle(ArrayList<Spisested> liste) {

        HashMap<String, Spisested> spisestedHashMap = new HashMap<>();

        //legger in ett objekt for å kunne starte sammenlikning.
        spisestedHashMap.put(liste.get(0).getObjektId(), liste.get(0));

        for (Spisested s: liste){
            //hvis hashmap inneholder denne objektId fra før
            if (spisestedHashMap.containsKey(s.getObjektId())){
                //hent inn objektet fra hashmap
                Spisested funn = spisestedHashMap.get(s.getObjektId());
                //hvis listeobjekt er av nyere årgang enn det som ligger i hashmap skal
                //funn i hashmap erstattes med listeobjektet
                if (s.getDato().after(funn.getDato())){
                    spisestedHashMap.remove(funn.getObjektId(), funn);
                    spisestedHashMap.put(s.getObjektId(), s);
                }
            }
            else {
                //hashmap har ikke denne ID fra før
                spisestedHashMap.put(s.getObjektId(), s);
            }
        }

        Collection<Spisested> values = spisestedHashMap.values();
        ArrayList<Spisested> resultat = new ArrayList<Spisested>(values);

        return resultat;

    }






    /*brukes for sjekk av om et orgnummer finnes i gjeldende treffliste
    @Override
    public boolean equals(Object o){
        Spisested s = (Spisested) o;

        return (this.objektId.equals(s.getObjektId()));

    } */

    @Override
    public String toString() {
        return "Spisested{" +
                "orgNr='" + orgNr + '\'' +
                ", navn='" + navn + '\'' +
                ", År='" + dato + '\'' +
                '}';
    }


   @Override
    public int compareTo(Spisested o) {
        Spisested s = (Spisested) o;
        if (navn.equals(s.getNavn()))
            return 0;
        else if (navn.compareTo(s.getNavn()) > 0)
            return 1;
        else
            return -1;
    }

}
