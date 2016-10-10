package de.uos.igf.stkappe.osnareport;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Klasse fuer die Titelseite der App. Sie Verwendet das Layout
 * res/layout/activity_title.xml. 
 * Sie beinhaltet Buttons, um 
 * 		- ein Ereignis melden zu koennen 
 * 		- den App-Beschreibungstext anzeigen zu lassen. 
 * Wird diese Klasse erneut aufgerufen, ohne dass ein Ereignis erfolgreich 
 * uebermittelt wurde, so erhaelt diese Klasse die bereits vom Nutzer eingegebenen 
 * Daten. Moechte der Nutzer mit dem Ereignismeldeprozess fortfahren, so werden 
 * diese Daten in der neuen Bildschirmansicht verarbeitet. So koennen bereits 
 * eingegebene Daten wiederhergestellt werden.
 * 
 * @author Steffen Kappe
 */
public class TitleActivity extends Activity implements OnClickListener {

	// Buttons, wie sie im Layout definiert wurden.
	private Button button_choose;
	private Button button_info;
	
	// Gibt an, ob die Methode onCreate in dieser Instanz ausgefuehrt wurde
	private boolean onCreateRunned = false;

	// Daten, die vom Nutzer in anderen Activities bereits eingegeben wurden
	// (s. einleitende Klassenbeschreibung).
	private int location_overlay_x;
	private int location_overlay_y;
	private String locadress;
	private String citypart;
	private String comment;
	private String observation_description;
	private int spinner_cat_selected_item;
	private int spinner_urg_selected_item;
	private String spinner_urg_other_time;
	private String name;
	private String adress;
	private String telnumber;

	/**
	 * Methode onCreate() ist ueberschrieben, um das Layout zu setzen und um die
	 * Buttons an das Layout zu binden.
	 * 
	 * @param savedInstanceState
	 *            von Android vorgegeben, stellt bei erneutem Start vorigen
	 *            Zustand der Klasse wieder her.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_title);

		onCreateRunned = true;

		button_choose = (Button) findViewById(R.id.title_activity_button_continue_exclmark);
		button_choose.setOnClickListener(this);
		button_info = (Button) findViewById(R.id.title_activity_button_continue_info);
		button_info.setOnClickListener(this);

	}

	/**
	 * Methode onClick() wird ausgefuehrt, sobald auf einen Button getippt
	 * wurde. Ein Button startet in dieser Klasse immer eine andere Ansicht.
	 * Intents werden hierfuer erzeugt und schlieﬂlich gestartet, um den Vorgang
	 * abzuschlieﬂen. Jeder Intent wird mit Daten befuellt (s. einleitenden
	 * Klassentext).
	 * 
	 * @param v
	 *            Verweis auf den Button, der angetippt wurde
	 */
	@Override
	public void onClick(View v) {
			if (v == button_choose) {
				Intent intent = new Intent(this, ChooseInputActivity.class);
				intent.putExtra("location_overlay_x", location_overlay_x);
				intent.putExtra("location_overlay_y", location_overlay_y);
				intent.putExtra("locadress",locadress);
				intent.putExtra("citypart",citypart);
				intent.putExtra("comment",comment);
				intent.putExtra("observation_input_string", observation_description);
				intent.putExtra("spinner_cat_selected_item",
						spinner_cat_selected_item);
				intent.putExtra("spinner_urg_selected_item",
						spinner_urg_selected_item);
				intent.putExtra("spinner_urg_other_time", spinner_urg_other_time);
				intent.putExtra("name", name);
				intent.putExtra("adress", adress);
				intent.putExtra("telnumber", telnumber);
				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);

			}


		if (v == button_info) {
			Intent intent = new Intent(this, InfoActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
		}
	}

	/**
	 * Methode onResume() wird bei App-Start nach onCreate() ausgefuehrt oder im
	 * spaeteren Applikationsverlauf bei erneutem Aufruf dieser Titelseite,
	 * nachdem der Nutzer zuruecknavigierte. In letzterem Fall sollen die
	 * uebermittelten Daten der bis zu diesem Zeitpunkt aufgerufenen
	 * Bildschirmseiten hier voruebergehend zwischengespeichert werden (s.
	 * einleitenden Klassentext).
	 */
	@Override
	public void onResume() {

		super.onResume();

		Bundle provideddata = getIntent().getExtras();

		if (provideddata != null && !onCreateRunned) {
			location_overlay_x = provideddata.getInt("location_overlay_x");
			location_overlay_y = provideddata.getInt("location_overlay_y");
			locadress = provideddata.getString("locadress");
			citypart = provideddata.getString("citypart");
			comment = provideddata.getString("comment");
			observation_description = provideddata
					.getString("observation_input_string");
			spinner_cat_selected_item = provideddata
					.getInt("spinner_cat_selected_item");
			spinner_urg_selected_item = provideddata
					.getInt("spinner_urg_selected_item");
			spinner_urg_other_time = provideddata
					.getString("spinner_urg_other_time");
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
	public void onPause() {
		super.onPause();
		onCreateRunned = false;
	}

	/**
	 * Durch Implementierung von onNewIntent() ist die jeweils korrekte Bildschirmansicht
	 * bekannt, von der aus diese Bildschirmansicht gestartet wurde.
	 */
	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}
}
