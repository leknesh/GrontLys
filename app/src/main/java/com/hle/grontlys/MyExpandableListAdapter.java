package com.hle.grontlys;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 *  Klassen er i hovedsak hentet fra:
 *  https://androidexample.com/Custom_Expandable_ListView_Tutorial_-_Android_Example/index.php?view=article_discription&aid=107&aaid=129
 */

//A Custom adapter to create Parent view (Used grouprow.xml) and Child View((Used childrow.xml).

public class MyExpandableListAdapter extends BaseExpandableListAdapter {

    private final LayoutInflater inf;
    private ArrayList<Tilsyn.Temaresultat> parents;
    private ArrayList<TilsynsDetalj> children;

    public MyExpandableListAdapter(Context mContext, Tilsyn valgtTilsyn,
                                   ArrayList<TilsynsDetalj> detaljListe) {
        inf = LayoutInflater.from(mContext);
        this.parents =  valgtTilsyn.getTilsynResultater();
        this.children = detaljListe;
    }

    @Override
    public int getGroupCount() {
        return parents.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return TilsynsDetalj.hentChildRows(children, groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return parents.get(groupPosition);
    }

    //henter her først en liste av resultater for det gjeldende tema (groupPosition)
    //deretter ett child
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return TilsynsDetalj.hentChildRows(children, groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    //hvis false vil expandableList regenereres ved endring av inndata (?)
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

        final Tilsyn.Temaresultat parent = parents.get(groupPosition);

        // Inflate grouprow.xml file for parent rows
        convertView = inf.inflate(R.layout.list_group_tilsyn, parentView, false);

        // Get grouprow.xml file elements and set values
        ((TextView) convertView.findViewById(R.id.tema_gruppe)).setText("Tema " + groupPosition);
        ((TextView) convertView.findViewById(R.id.tema_text)).setText(parent.getTemanavn());
        ImageView image = (ImageView)convertView.findViewById(R.id.tema_karakter);

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
                             View convertView, ViewGroup parentView)
    {
        final Tilsyn.Temaresultat parent = parents.get(groupPosition);

        //henter ut liste av resultated fra rett kategori (groupPosition), deretter en rad av disse.
        final TilsynsDetalj child = TilsynsDetalj.hentChildRows(children, groupPosition).get(childPosition);

        // Inflate childrow.xml file for child rows
        convertView = inf.inflate(R.layout.list_item_tilsynsdetalj, parentView, false);

        // Get childrow.xml file elements and set values
        ((TextView) convertView.findViewById(R.id.detalj_punktnavn)).setText(child.getPunktNavn());
        ((TextView) convertView.findViewById(R.id.detalj_forklaring)).setText(child.getPunktForklaring());
        ImageView image=(ImageView)convertView.findViewById(R.id.detalj_karakter);

        //setter smilefjes på bakgrunn av karakter
        String karakter = child.getPunktKarakter();

        switch (karakter) {
            case "0":
            case "1":
                image.setImageResource(R.drawable.ic_sentiment_satisfied_green_24dp);
                break;
            case "2":
                image.setImageResource(R.drawable.ic_sentiment_neutral_yellow_24dp);
                break;
            case "3":
                image.setImageResource(R.drawable.ic_sentiment_dissatisfied_red_24dp);
                break;
            default:
                image.setImageResource(R.drawable.ic_remove_circle_outline_blue_24dp);
                break;
        }

        //Childview ferdigbygget
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
