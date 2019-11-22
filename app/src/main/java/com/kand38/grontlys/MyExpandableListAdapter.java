package com.kand38.grontlys;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


/**
 *  Kandidat38:
 *  Klasse for håndtering av data til ExpandableList.
 *  Klassen er i hovedsak bearbeidet/tilpasset kode hentet fra disse kildene:
 *  https://androidexample.com/Custom_Expandable_ListView_Tutorial_-_Android_Example/index.php?view=article_discription&aid=107&aaid=129
 *  https://stackoverflow.com/questions/24083886/expandablelistview-in-fragment-issue
 *  Koden er bearbeidet og endret for tilpassing til bruk i fragment, og som egen klasse.
 */

//A Custom adapter to create Parent view (Used grouprow.xml) and Child View((Used childrow.xml).

public class MyExpandableListAdapter extends BaseExpandableListAdapter {

    //logtag
    private static final String TAG = "JsonLog";

    //inputvariabler
    private ArrayList<Tilsyn.Temaresultat> parents;
    private ArrayList<TilsynsDetalj> childrenMaster;
    private Context mContext;

    //children til bruk i viewet er et subsett av alle entries i detaljlisten og genereres ved behov
    private ArrayList<TilsynsDetalj> children;

    private LayoutInflater inflater;

    //konstruktør. Må sende inn context pga bruk i fragment!
    public MyExpandableListAdapter(Context mContext, ArrayList<Tilsyn.Temaresultat> temaResultater,
                                   ArrayList<TilsynsDetalj> detaljListe) {
        this.mContext = mContext;
        this.parents =  temaResultater;
        this.childrenMaster = detaljListe;

        inflater = LayoutInflater.from(mContext);

        Log.d(TAG, "ExpListadapter input parent: " + parents.size() + ", children" + childrenMaster.size());
    }


    //brukes av BaseExpandableListAdapter for å holde styr på antall grupper
    @Override
    public int getGroupCount() {
        Log.d(TAG, "getGroupCount: " + parents.size());
        return parents.size();
    }


    //brukes av BaseExpandableListAdapter for å holde styr på antall children i en gruppe
    //dette vil variere ihht hvilken gruppe som er valgt
    @Override
    public int getChildrenCount(int groupPosition) {

        int id = groupPosition + 1;

        //metoden her tar inn en id som velger ut temagruppe 1-4 og returnerer aktuelle rader
        // Tilsvare GroupPosition + 1
        children = TilsynsDetalj.hentChildRows(childrenMaster, id);

        return children.size();
    }


    //brukes av BaseExpandableListAdapter for å hente ut valgt gruppe f.ex ved onclick
    @Override
    public Object getGroup(int groupPosition) {

        Object o = parents.get(groupPosition);

        return o;
    }


    //henter her først en liste av resultater for det gjeldende tema (groupPosition)
    //deretter ett child
    @Override
    public Object getChild(int groupPosition, int childPosition) {

        int id = groupPosition + 1;
        children = TilsynsDetalj.hentChildRows(childrenMaster, id);

        Object o = children.get(childPosition);

        return o;
    }


    //Call when parent row clicked
    @Override
    public long getGroupId(int groupPosition) {

        return groupPosition;
    }

    //Kalles ved klikk på child row. Dette er ikke videre implementert
    @Override
    public long getChildId(int groupPosition, int childPosition) {

        return childPosition;
    }

    //hvis false vil expandableList regenereres ved endring av inndata
    @Override
    public boolean hasStableIds() {
        return false;
    }

    /**
    * Kode getGoupView og getChildView hentet fra:
    * https://androidexample.com/Custom_Expandable_ListView_Tutorial_-_Android_Example/index.php?view=article_discription&aid=107&aaid=129
     **/

    // This Function used to inflate parent rows view
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parentView) {

        //henter gjeldende temaresultat
        final Tilsyn.Temaresultat parent = parents.get(groupPosition);
        //Tekst til nummerering av gruppe/tema
        String temaTekst = "Tema " + (groupPosition + 1);

        // Inflate grouprow.xml file for parent rows
        convertView = inflater.inflate (R.layout.list_group_tilsyn, parentView, false);

        // Get grouprow.xml file elements and set values
        ((TextView) convertView.findViewById(R.id.tema_gruppe)).setText(temaTekst);
        ((TextView) convertView.findViewById(R.id.tema_text)).setText(parent.getTemanavn());
        ImageView image = convertView.findViewById(R.id.tema_karakter);

        //setter smilefjes på bakgrunn av karakter
        String karakter = parent.getTemakarakter();

        switch (karakter) {
            case "0":
            case "1":
                image.setImageResource(R.drawable.ic_sentiment_satisfied_green_60dp);
                break;
            case "2":
                image.setImageResource(R.drawable.ic_sentiment_neutral_yellow_60dp);
                break;
            case "3":
                image.setImageResource(R.drawable.ic_sentiment_dissatisfied_red_60dp);
                break;
            default:
                image.setImageResource(R.drawable.ic_remove_circle_outline_blue_60dp);
                break;
        }

        //ferdig bygget parentview
        return convertView;
    }

    // This Function used to inflate child rows view
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parentView) {

        //henter ut liste av resultater fra rett kategori (groupPosition + 1), deretter en rad av disse
        int id = groupPosition + 1;
        children = TilsynsDetalj.hentChildRows(childrenMaster, id);

        final TilsynsDetalj child = children.get(childPosition);

        // Inflate childrow.xml file for child rows
        convertView = inflater.inflate(R.layout.list_item_tilsynsdetalj, parentView, false);

        // Get childrow.xml file elements and set values
        ((TextView) convertView.findViewById(R.id.detalj_punktnavn)).setText(child.getPunktNavn());
        ((TextView) convertView.findViewById(R.id.detalj_forklaring)).setText(child.getPunktForklaring());;

        //henter imageview til karakter
        ImageView image=(ImageView)convertView.findViewById(R.id.detalj_karakter);

        //setter smilefjes på bakgrunn av karakter
        String karakter = child.getPunktKarakter();

        switch (karakter) {
            case "0":
            case "1":
                image.setImageResource(R.drawable.ic_sentiment_satisfied_green_24dp);
                return convertView;
            case "2":
                image.setImageResource(R.drawable.ic_sentiment_neutral_yellow_24dp);
                return convertView;
            case "3":
                image.setImageResource(R.drawable.ic_sentiment_dissatisfied_red_24dp);
                return convertView;
            default:
                image.setImageResource(R.drawable.ic_remove_circle_outline_blue_24dp);
                return convertView;
        }

        //Childview ferdigbygget
    }

    @Override
    public boolean areAllItemsEnabled(){
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
