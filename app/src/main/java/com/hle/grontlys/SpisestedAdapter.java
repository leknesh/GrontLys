package com.hle.grontlys;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SpisestedAdapter extends RecyclerView.Adapter<SpisestedAdapter.SpisestedViewHolder> {

    //logtag
    private static final String TAG = "JsonLog";

    private ArrayList<Spisested> spisestedListe = new ArrayList<>();
    private Context mContext;

    SpisestedAdapter(Context context, ArrayList<Spisested> spisestedListe) {
        this.spisestedListe = spisestedListe;
        this.mContext = context;
    }

    @Override
    public SpisestedAdapter.SpisestedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SpisestedViewHolder(LayoutInflater.from(mContext).inflate(R.layout.spisested_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SpisestedAdapter.SpisestedViewHolder holder, int position) {
        Spisested spisestedet = spisestedListe.get(position);
        holder.bindTo(spisestedet);
    }

    @Override
    public int getItemCount() {
        if (spisestedListe != null)
            return spisestedListe.size();
        else
            return 0;
    }

    class SpisestedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView orgNrTextView;
        private TextView navnTextView;
        private TextView adresseTextView;
        private TextView postNrTextView;
        private TextView postStedTextView;
        private TextView datoTextView;
        private ImageView karakterImageView;

        SpisestedViewHolder(View itemView) {

            super(itemView);

            navnTextView = itemView.findViewById(R.id.navn_card);
            orgNrTextView = itemView.findViewById(R.id.orgnr_card);
            adresseTextView = itemView.findViewById(R.id.adresse_card);
            postNrTextView = itemView.findViewById(R.id.postnr_card);
            postStedTextView = itemView.findViewById(R.id.poststed_card);
            datoTextView = itemView.findViewById(R.id.dato_card);
            karakterImageView = itemView.findViewById(R.id.karakter_card);

            itemView.setOnClickListener(this);
        }

        void bindTo(Spisested spisestedet) {
            navnTextView.setText(spisestedet.getNavn());
            orgNrTextView.setText("Org.nr: " + spisestedet.getOrgNr());
            adresseTextView.setText(spisestedet.getAdresse());
            postNrTextView.setText(spisestedet.getPostNr());
            postStedTextView.setText(spisestedet.getPostSted());
            datoTextView.setText(spisestedet.getArstall());

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

        @Override
        public void onClick(View v) {
            Spisested spisestedet = spisestedListe.get(getAdapterPosition());

            //Intent intent = new Intent(mContext, BarnehageDetaljActivity.class);

            //sender med barnehageId for oppslag)
            //intent.putExtra("barnehageNr", denneBarnehagen.getBarnehageNr());
            Log.d(TAG, "Valgt Spisested: " + spisestedet.getNavn());

            //mContext.startActivity(intent);
        }
    }


}
