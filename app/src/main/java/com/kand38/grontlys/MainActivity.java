package com.kand38.grontlys;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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

/******************************
 * Oppstart acv applikasjon,
 * deklarerer variabler, oppretter views og tar imot brukerinput
 * til søk
 *
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //view-variabler
    private EditText navnEditText;
    private EditText poststedEditText;
    private Spinner arstallSpinner;
    private ArrayAdapter spinnerAdapter;
    private String sokeNavn;
    private String sokePoststed;
    private String arstall;
    private int nynorsk;
    private ArrayList<Spisested> spisestedListe= new ArrayList<>();

    //preference-variabler
    public static boolean lagreSted;
    public static boolean lagreArstall;
    public static boolean brukNynorsk;
    public final String PREF_ARSTALL_KEY = "arstall";
    public final String PREF_STED_KEY = "poststed";
    public final String PREF_MALFORM_KEY = "malform";

    //spesifisering av søketype
    public static final int INTENT_STANDARD = 1;
    public static final int INTENT_LOKASJON = 2;

    //lokasjonsrelatert
    public final static int MY_REQUEST_LOCATION = 3;

    //Lokasjon
    private Location myLocation;

    //logtag
    private static final String TAG = "JsonLog";

    //lager view og henter inn lagrede innstillinger
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

        // Settings får "default"-verdier
        androidx.preference.PreferenceManager.
                setDefaultValues(this, R.xml.root_preferences, false);

        SharedPreferences sharedPref = androidx.preference.PreferenceManager.
                getDefaultSharedPreferences(this);

        //starter henting av preference-settinger og lagrede variabler
        sjekkPreferences();

    }

    //lagrer preferanser ved pause
    @Override
    protected void onPause() {

        super.onPause();
        lagrePreferences();
    }

    //henter inn preferanser ved resume
    @Override
    protected void onResume(){

        //her er det et problem med at når nynorsk skrus av, implementeres
        // ikke dette før appen er restartet. Motsatt vei fungerer ok.
        sjekkPreferences();
        super.onResume();

    }


    //sjekker lagredepreferences
    private void sjekkPreferences() {

        SharedPreferences sharedPref = androidx.preference.PreferenceManager.
                getDefaultSharedPreferences(this);
        lagreSted = sharedPref.getBoolean(SettingsActivity.SAVE_POSTSTED_SWITCH, false);
        lagreArstall = sharedPref.getBoolean(SettingsActivity.SAVE_ARSTALL_SWITCH, false);
        brukNynorsk = sharedPref.getBoolean(SettingsActivity.MALFORM_SWITCH, false);

        hentFavoritter();

    }

    //henter inn lagret søkepoststed og årstall dersom tilgjengelig
    private void hentFavoritter() {

        SharedPreferences sharedPref = androidx.preference.PreferenceManager.
                getDefaultSharedPreferences(this);

        String sted = sharedPref.getString(PREF_STED_KEY, "");
        String ar = sharedPref.getString(PREF_ARSTALL_KEY, "");
        nynorsk = sharedPref.getInt(PREF_MALFORM_KEY, 0);

        //legger lagrede variabler inn i view
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
        SharedPreferences sharedPref = androidx.preference.PreferenceManager.
             getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();

        if (lagreSted){
            editor.putString(PREF_STED_KEY, poststedEditText.getText().toString());
        }
        if (lagreArstall){
            editor.putString(PREF_ARSTALL_KEY, arstallSpinner.getSelectedItem().toString());
        }
        if (brukNynorsk == true)
            nynorsk = 1;
        else
            nynorsk = 0;

        editor.putInt(PREF_MALFORM_KEY, nynorsk);

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

    //Behandler knappetrykk, starter lokasjons- eller standard søk
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
                lagrePreferences();
                hentLokasjon();
                break;
            default:
                break;
        }
    }

    //henter inn variabler til standard søk fra view og starter søkeactivity
    private void startSpisestedSok() {
        sokeNavn        = navnEditText.getText().toString();
        sokePoststed    = poststedEditText.getText().toString();
        arstall         = arstallSpinner.getSelectedItem().toString();

        //lar ikke bruker hente hele datasettet
        if (sokeNavn.isEmpty() && sokePoststed.isEmpty()){
            displayToast("Legg inn navn på spisested og/eller poststed");
        }
        //oppretter intent, legger inn variabler og starter søkeactivity
        else {
            Intent intent = new Intent(this, SokelisteActivity.class);
            intent.putExtra("soketype", INTENT_STANDARD);
            intent.putExtra("sokenavn", sokeNavn);
            intent.putExtra("sokepoststed", sokePoststed);
            intent.putExtra("arstall", arstall);
            intent.putExtra("nynorsk", nynorsk);

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
            // Appen har allerede fått tillatelse
            //henter inn siste kjente lokasjon
            else {
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

    //starter søkeactivity med lokasjon som søkeparameter
    private void startLokasjonssok(Location myLocation) {

        if (myLocation == null){
            displayToast("Finner ikke lokasjon");
        }
        //legger inn lokasjon i intent, sammen med en konstant som indikerer
        //at søket er et lokasjonssøk
        else {
            Intent intent = new Intent(this, SokelisteActivity.class);
            intent.putExtra("lokasjon", myLocation);
            intent.putExtra("soketype", INTENT_LOKASJON);
            intent.putExtra("nynorsk", nynorsk);
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
     * Utility-metoder
     *
     */

    //viser toastmelding med valgt tekstinput
    public void displayToast(String message) {
        Toast.makeText(getApplicationContext(), message,
                Toast.LENGTH_SHORT).show();
    }





}
