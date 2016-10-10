package de.uos.igf.stkappe.osnareport;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;

import de.uos.igf.stkappe.osnareport.R;

/**
 * Diese Klasse definiert die Bildschirmansicht, um einen Ort per Adresse
 * eingeben zu koennen. Es ist moeglich, Straﬂe und Hausnummer, den Osnabruecker
 * Stadtteil und einen Kommentar zum Ort anzugeben.
 * Diese Klasse verwendet das Layout res/layout/activity_adress.xml
 * fuer das Hauptfenster und res/layout/activity_adress_title.xml fuer
 * die obere Aktionsleiste.
 * In der oberen Aktionsleiste befinden sich Buttons, um sowohl den gesamten Prozess
 * fortzusetzen, indem als naechstes die Beobachtung eingegeben werden kann,
 * als auch einen Schritt zurueck zu navigieren, indem die vorige Bildschirmansicht
 * aufgerufen wird.
 * Wird diese Klasse erneut aufgerufen, ohne dass versucht wurde, das Ereignis
 * an den Empfaenger zu uebermitteln, so erhaelt diese Klasse die bereits vom Nutzer
 * in anderen Bildschirmansichten eingegebenen Daten. Wechselt der Nutzer die
 * Bildschirmansicht erneut, so werden die Daten dafuer verwendet, auf der
 * naechsten Bildschirmansicht bereits getaetigte Benutzereingaben
 * wiederherzustellen. 
 * 
 * @author Steffen Kappe
 */
public class AdressActivity extends Activity implements OnClickListener {

	// bereits getaetigte Nutzereingaben (s. Klassenbeschreibung)
	private String observation_description;
	private int spinner_cat_selected_item;
	private int spinner_urg_selected_item;
	private String spinner_urg_other_time;
	private String name;
	private String adress;
	private String telnumber;
	private boolean onCreateRunned = false;
	
	/**
	 * Methode onCreate() ist ueberschrieben, um das Layout zu setzen und um die
	 * Buttons an das Layout zu binden. Auﬂerdem werden bereits eingegebene
	 * Nutzerdaten von der aufrufenden Bildschirmseite empfangen.
	 * 
	 * @param savedInstanceState
	 *            von Android vorgegeben, stellt bei erneutem Start vorigen
	 *            Zustand der Klasse wieder her.
	 */
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

		setContentView(R.layout.activity_adress);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.activity_adress_title);
		
		// Nutzerdaten der aufrufenden Bildschirmseite
		Bundle provideddata = getIntent().getExtras();
		((EditText)findViewById(R.id.adress_activity_edittext_adress)).setText(provideddata.getString("locadress"));
		((EditText)findViewById(R.id.adress_activity_edittext_citypart)).setText(provideddata.getString("citypart"));
		((EditText)findViewById(R.id.adress_activity_edittext_comment)).setText(provideddata.getString("comment"));
		observation_description = provideddata.getString("observation_input_string");
		spinner_cat_selected_item =  provideddata.getInt("spinner_cat_selected_item");
		spinner_urg_selected_item =  provideddata.getInt("spinner_urg_selected_item");
		spinner_urg_other_time =  provideddata.getString("spinner_urg_other_time");
		name = provideddata.getString("name");
		adress = provideddata.getString("adress");
		telnumber = provideddata.getString("telnumber");
		
		// In dieser Bildschirmansicht verfuegbare Buttons werden mit ihren
		// Listenern verbunden, um ihnen die gewuenschte Funktionalitaet
		// zuzuweisen.
		findViewById(R.id.adress_activity_button_back).setOnClickListener(this);
		findViewById(R.id.adress_activity_button_continue).setOnClickListener(this);
		
		onCreateRunned = true;
	}

	/**
	 * onClick() definiert den Ablauf nach Betaetigung eines Buttons.
	 * In jedem Fall wird nach Betaetigung eine neue Bildschirmansicht aufgerufen.
	 * Hierfuer werden die benoetigten Daten, die mitgeliefert werden muessen (s.
	 * Klassenbeschreibung), angegeben.
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.adress_activity_button_back:
				Intent intent = new Intent(this, ChooseInputActivity.class);
				intent.putExtra("calledActivity","AdressActivity");
				intent.putExtra("locadress", ((EditText)findViewById(R.id.adress_activity_edittext_adress)).getText().toString());
				intent.putExtra("citypart", ((EditText)findViewById(R.id.adress_activity_edittext_citypart)).getText().toString());
				intent.putExtra("comment", ((EditText)findViewById(R.id.adress_activity_edittext_comment)).getText().toString());
				intent.putExtra("observation_input_string", observation_description);
				intent.putExtra("spinner_cat_selected_item", spinner_cat_selected_item);
				intent.putExtra("spinner_urg_selected_item", spinner_urg_selected_item);
				intent.putExtra("spinner_urg_other_time", spinner_urg_other_time);
				intent.putExtra("name",name);
				intent.putExtra("adress",adress);
				intent.putExtra("telnumber",telnumber);
				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
				finish();
				break;
			
			case R.id.adress_activity_button_continue:
				Intent intent2 = new Intent(this, ObservationActivity.class);
				intent2.putExtra("locadress", ((EditText)findViewById(R.id.adress_activity_edittext_adress)).getText().toString());
				intent2.putExtra("citypart", ((EditText)findViewById(R.id.adress_activity_edittext_citypart)).getText().toString());
				intent2.putExtra("comment", ((EditText)findViewById(R.id.adress_activity_edittext_comment)).getText().toString());
				intent2.putExtra("observation_input_string", observation_description);
				intent2.putExtra("spinner_cat_selected_item", spinner_cat_selected_item);
				intent2.putExtra("spinner_urg_selected_item", spinner_urg_selected_item);
				intent2.putExtra("spinner_urg_other_time", spinner_urg_other_time);
				intent2.putExtra("name",name);
				intent2.putExtra("adress",adress);
				intent2.putExtra("telnumber",telnumber);
				intent2.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				intent2.putExtra("Activity","AdressActivity");
				startActivity(intent2);

				break;
		}
	}
	
	/**
	 * Methode onResume() wird bei Start der Bildschirmansicht nach onCreate()
	 * ausgefuehrt oder im spaeteren Applikationsverlauf bei erneutem Aufruf
	 * dieser Bildschirmseite, nachdem der Nutzer zuruecknavigierte. In letzterem
	 * Fall sollen die uebermittelten Daten der bis zu diesem Zeitpunkt
	 * aufgerufenen Bildschirmseiten hier voruebergehend zwischengespeichert
	 * werden (s. einleitenden Klassentext).
	 */
	@Override
    public void onResume(){
    	super.onResume();

    	if (!onCreateRunned){
	    	Bundle provideddata = getIntent().getExtras();
			observation_description = provideddata.getString("observation_input_string");
			spinner_cat_selected_item =  provideddata.getInt("spinner_cat_selected_item");
			spinner_urg_selected_item =  provideddata.getInt("spinner_urg_selected_item");
			spinner_urg_other_time =  provideddata.getString("spinner_urg_other_time");
	    	name = provideddata.getString("name");
			adress = provideddata.getString("adress");
			telnumber = provideddata.getString("telnumber");
    	}
    }
	
	/**
	 * Sollte eine andere Bildschirmseite in den Vordergrund treten, so wird
	 * onPause() ausgefuehrt und onCreateRunned wird auf false gesetzt. Dies
	 * verhindert, dass der Inhalt der onResume()-Methode bereits bei App-Start
	 * ausgefuehrt wird.
	 */
	@Override
	public void onPause(){
		super.onPause();
		onCreateRunned = false;
	}
	
	/**
	 * Durch Implementierung von onNewIntent() ist die jeweils korrekte
	 * Bildschirmansicht bekannt, von der aus diese Bildschirmansicht gestartet
	 * wurde.
	 */
	@Override
	public void onNewIntent (Intent intent){
		super.onNewIntent(intent);
		setIntent(intent);
	}
}
