package com.kand38.grontlys;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
*   Klasse for opprettelse av spisested-cardviews etter mønster fra pensum.
 *
 * Kode for filtrering hentet på:
 * https://stackoverflow.com/questions/29792187/add-a-search-filter-on-recyclerview-with-cards
 * https://codingwithmitch.com/blog/filtering-recyclerview-searchview/
 */
public class SpisestedAdapter extends RecyclerView.Adapter<SpisestedAdapter.SpisestedViewHolder>
        implements Filterable {

    //logtag
    private static final String TAG = "JsonLog";

    private ArrayList<Spisested> spisestedListe;
    private ArrayList<Spisested> filtrertListe;
    private Context mContext;


    public SpisestedAdapter(Context context, ArrayList<Spisested> spisestedListe) {
        this.spisestedListe = spisestedListe;
        this.mContext = context;
        this.filtrertListe = (ArrayList<Spisested>) spisestedListe.clone();
    }

    @NonNull
    @Override
    public SpisestedAdapter.SpisestedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SpisestedViewHolder(LayoutInflater.from(mContext).inflate(R.layout.spisested_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SpisestedAdapter.SpisestedViewHolder holder, int position) {
        Spisested spisestedet = filtrertListe.get(position);
        holder.bindTo(spisestedet);
    }

    @Override
    public int getItemCount() {
        if (filtrertListe != null)
            return filtrertListe.size();
        else
            return 0;
    }

    //starter filtrering, se klasse UserFilter nederst
    @Override
    public Filter getFilter() {
        Log.d(TAG, "GetFilter, listestr: " + spisestedListe.size() );

        return new UserFilter(this, spisestedListe);

    }

    //legger inn data i cardviewet
    class SpisestedViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        //views i et card
        private TextView orgNrTextView;
        private TextView navnTextView;
        private TextView adresseTextView;
        private TextView postNrTextView;
        private TextView postStedTextView;
        private TextView datoTextView;
        private ImageView karakterImageView;

        SpisestedViewHolder(View itemView) {

            super(itemView);

            //tilordner views til layoutelementer
            navnTextView = itemView.findViewById(R.id.navn_card);
            orgNrTextView = itemView.findViewById(R.id.orgnr_card);
            adresseTextView = itemView.findViewById(R.id.adresse_card);
            postNrTextView = itemView.findViewById(R.id.postnr_card);
            postStedTextView = itemView.findViewById(R.id.poststed_card);
            datoTextView = itemView.findViewById(R.id.dato_card);
            karakterImageView = itemView.findViewById(R.id.karakter_card);

            itemView.setOnClickListener(this);
        }

        //legger data inn i views
        void bindTo(Spisested spisestedet) {
            String txt = "Org.nr: " + spisestedet.getOrgNr();

            navnTextView.setText(spisestedet.getNavn());
            orgNrTextView.setText(txt);
            adresseTextView.setText(spisestedet.getAdresse());
            postNrTextView.setText(spisestedet.getPostNr());
            postStedTextView.setText(spisestedet.getPostSted());
            datoTextView.setText(spisestedet.getArstall());

            //Velger bilde på basis av totalkarakter for tilsynet
            //ref karakterskala beskrevet her: https://data.norge.no/data/mattilsynet/smilefjestilsyn-p%C3%A5-serveringssteder
            switch (spisestedet.getTotKarakter()) {
                case "0":
                case "1":
                    karakterImageView.setImageResource(R.drawable.ic_sentiment_satisfied_green_60dp);
                    break;
                case "2":
                    karakterImageView.setImageResource(R.drawable.ic_sentiment_neutral_yellow_60dp);
                    break;
                case "3":
                    karakterImageView.setImageResource(R.drawable.ic_sentiment_dissatisfied_red_60dp);
                    break;
                default:
                    karakterImageView.setImageResource(R.drawable.ic_highlight_off_blue_60dp);
                    break;
            }

        }

        //klikk på et spisestedobjekt skal starte visning av tilsyn med tilsynsdetaljer.
        @Override
        public void onClick(View v) {

            //henter valgt spisested
            Spisested spisestedet = filtrertListe.get(getAdapterPosition());

            //oppretter intent og legger ved valgt spisested
            Intent intent = new Intent(mContext, TilsynListActivity.class);
            intent.putExtra("valgtspisested", spisestedet);

            //kontroll
            Log.d(TAG, "Valgt Spisested: " + spisestedet.getNavn());

            //starter ny aktivitet
            mContext.startActivity(intent);
        }

    }

    //kode for klassen Userfilter hentet og bearbeidet ut fra:
    //https://stackoverflow.com/questions/29792187/add-a-search-filter-on-recyclerview-with-cards
    //pluss https://codingwithmitch.com/blog/filtering-recyclerview-searchview/

    static class UserFilter extends Filter {

        private final SpisestedAdapter adapter;
        private final ArrayList<Spisested> originalListe;
        private final ArrayList<Spisested> nyListe = new ArrayList<>();

        private UserFilter(SpisestedAdapter adapter, ArrayList<Spisested> originalListe ){
            super();
            this.adapter = adapter;
            this.originalListe = originalListe;

        }

        //Sjekker spisestednavn for innhold av input-teksten constraint,
        // og legger treff på en ny liste
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            nyListe.clear();
            String filterPattern = constraint.toString().toLowerCase().trim();
            final FilterResults results = new FilterResults();

            if (filterPattern.isEmpty()) {
                nyListe.addAll(originalListe);
            } else {
                for (Spisested s : originalListe) {
                    if (s.getNavn().toLowerCase().contains(filterPattern)) {
                        nyListe.add(s);
                    }
                }
            }
            results.values = nyListe;
            results.count = nyListe.size();
            return results;
        }

        //henter inn resultatliste og oppdaterer listen i adapteret
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.filtrertListe.clear();
            adapter.filtrertListe.addAll((ArrayList<Spisested>) results.values);
            adapter.notifyDataSetChanged();
        }

    }

}
