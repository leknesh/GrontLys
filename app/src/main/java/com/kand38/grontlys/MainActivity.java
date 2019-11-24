package com.kand38.grontlys;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.time.LocalDate;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText navnEditText, poststedEditText;
    private Spinner arstallSpinner;
    private ArrayAdapter spinnerAdapter;
    private String sokeNavn, sokePoststed, arstall;
    private ArrayList<Spisested> spisestedListe= new ArrayList<>();

    public static boolean lagreSted, lagreArstall, brukNynorsk;
    public final String PREF_ARSTALL_KEY = "arstall";
    public final String PREF_STED_KEY = "poststed";
    public final String PREF_MALFORM_KEY = "malform";

    //spesifisering av søketype
    public static final int INTENT_STANDARD = 1;
    public static final int INTENT_LOKASJON = 2;

    //lokasjonsrelatert
    public final static int MY_REQUEST_LOCATION = 3;

    private Location myLocation;

    //logtag
    private static final String TAG = "JsonLog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //setter opp view-variabler
        navnEditText = findViewById(R.id.spisested_sokenavn);
        poststedEditText = findViewById(R.id.poststed_sokenavn);

        arstallSpinner = findViewById(R.id.arstall_spinner);
        byggSpinner();

        Button sokeKnapp = findViewById(R.id.sok_knapp);
        sokeKnapp.setOnClickListener(this);

        Button visHerKnapp = findViewById(R.id.vis_her_knapp);
        visHerKnapp.setOnClickListener(this);


        // Oppdaterer og henter settingsverdier
        //Shared Preferences
        SharedPreferences sharedPref = androidx.preference.PreferenceManager.
                getDefaultSharedPreferences(this);

        // Settings får "default"-verdier
        androidx.preference.PreferenceManager.
                setDefaultValues(this, R.xml.root_preferences, false);

        //sjekker om noen av switchene er på
        sjekkPreferences(sharedPref);

    }

    private void sjekkPreferences(SharedPreferences sharedPref) {

        lagreSted = sharedPref.getBoolean(SettingsActivity.SAVE_POSTSTED_SWITCH, false);
        Log.d(TAG, "Lagre kommune? " + lagreSted);

        lagreArstall = sharedPref.getBoolean(SettingsActivity.SAVE_ARSTALL_SWITCH, false);
        Log.d(TAG, "Lagre Årstall? " + lagreArstall);

        brukNynorsk = sharedPref.getBoolean(SettingsActivity.MALFORM_SWITCH, false);
        Log.d(TAG, "Nynorsk? " + brukNynorsk);

        // Hvis lagring er valgt hentes lagrede verdier
        if (lagreSted || lagreArstall || brukNynorsk) {
            hentFavoritter();
            Log.d(TAG, "Lagring valgt");
        }

    }

    private void hentFavoritter() {

        SharedPreferences myPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        String sted = myPreferences.getString(PREF_STED_KEY, "");
        String ar = myPreferences.getString(PREF_ARSTALL_KEY, "");

        Log.d(TAG, "Lagret sted: " + sted + ", lagret årstall" + ar);

        if (lagreSted && !(sted.isEmpty()))
            poststedEditText.setText(sted);

        if (lagreArstall && !(ar.isEmpty())){
            try {
                int pos = spinnerAdapter.getPosition(ar);
                arstallSpinner.setSelection(pos);
            } catch (Exception ex){
                Log.d(TAG, "Ooops, spinnerproblem");
                arstallSpinner.setSelection(0);
            }

        }

    }


    public void lagrePreferences(){
        SharedPreferences myPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        SharedPreferences.Editor editor = myPreferences.edit();

        if (lagreSted){
            editor.putString(PREF_STED_KEY, poststedEditText.getText().toString());
        }
        if (lagreArstall){
            editor.putString(PREF_ARSTALL_KEY, arstallSpinner.getSelectedItem().toString());
        }

        editor.putBoolean(PREF_MALFORM_KEY, brukNynorsk);

        editor.apply();
    }

    private void byggSpinner() {
        //lager arrayliste av årstall fra i år og ned til 2016
        ArrayList<String> ARSTALL = new ArrayList<>();
        ARSTALL.add("Alle");

        //henter inneværende år
        int iaar = LocalDate.now().getYear();
        //bygger arrayliste med årstall
        for (int y = iaar; y > 2015 ; y--){
            ARSTALL.add("" + y);
        }

        spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, ARSTALL );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        arstallSpinner.setAdapter(spinnerAdapter);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //søkeknappen skal sørge for lagring av preferencevariabler og starte søk
            case R.id.sok_knapp:
                lagrePreferences();
                startSpisestedSok();
                break;
            case R.id.vis_her_knapp:
               //starter alternativ rute med lokasjonsbasert søk
                hentLokasjon();
        }
    }


    private void startSpisestedSok() {
        sokeNavn        = navnEditText.getText().toString();
        sokePoststed    = poststedEditText.getText().toString();
        arstall         = arstallSpinner.getSelectedItem().toString();

        //lar ikke bruker hente hele datasettet
        if (sokeNavn.isEmpty() && sokePoststed.isEmpty()){
            displayToast("Legg inn navn på spisested og/eller poststed");
        }
        else {
            Intent intent = new Intent(this, SokelisteActivity.class);
            intent.putExtra("soketype", INTENT_STANDARD);
            intent.putExtra("sokenavn", sokeNavn);
            intent.putExtra("sokepoststed", sokePoststed);
            intent.putExtra("arstall", arstall);
            intent.putExtra("nynorsk", brukNynorsk);

            startActivity(intent);
        }
    }



    /******************************
     * Lokasjonsrelaterte metoder
     *
     */
    //sjekker om lokasjonstillatelse gitt, starter evt tilgangsactivity
    //kode hentet fra forelesningsslides

    private void hentLokasjon() {

        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        assert locationManager != null;
        String locationProvider = LocationManager.GPS_PROVIDER;
        if (!locationManager.isProviderEnabled(locationProvider)) {
            Toast.makeText(this, "Aktiver " + locationProvider + " under Location i Settings", Toast.LENGTH_LONG).show();
        } else {
            int permissionCheck = this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_REQUEST_LOCATION);
            else { // Appen har allerede fått tillatelse
                myLocation = locationManager.getLastKnownLocation(locationProvider);

                startLokasjonssok(myLocation);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == MY_REQUEST_LOCATION) {
        // Sjekk om bruker har gitt tillatelsen.
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    hentLokasjon();
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            } else // Bruker avviste spørsmål om tillatelse. Kan ikke lese posisjon
                Toast.makeText(this, "Kan ikke vise posisjon uten brukertillatelse.", Toast.LENGTH_LONG).show();
        }
    }


    private void startLokasjonssok(Location myLocation) {

        if (myLocation == null){
            displayToast("Finner ikke lokasjon");
        }
        else {
            Intent intent = new Intent(this, SokelisteActivity.class);
            intent.putExtra("lokasjon", myLocation);
            intent.putExtra("soketype", INTENT_LOKASJON);
            startActivity(intent);
        }
    }

    /******************************
     * Meny-behandling
     *
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //hovedmeny gir tilgang til settings
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /******************************
     * Håndtering av Landscape/portrait
     *
     */

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /******************************
     * Utility-metoder
     *
     */

    //viser toastmelding med valgt tekstinput
    public void displayToast(String message) {
        Toast.makeText(getApplicationContext(), message,
                Toast.LENGTH_SHORT).show();
    }





}
