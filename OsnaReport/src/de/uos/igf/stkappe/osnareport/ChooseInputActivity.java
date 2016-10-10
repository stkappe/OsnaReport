package de.uos.igf.stkappe.osnareport;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

/**
 * Diese Klasse definiert eine Ansicht, um zwischen den Ortsangabemoeglichkeiten
 * (z.B. Adresse, Karte) auswaehlen zu koennen. Sie verwendet das Layout
 * res/layout/activity_chooseinput.xml fuer den Inhalt,
 * res/layout/activity_chooseinput_title.xml fuer die obere Aktionsleiste und
 * res/layout/activity_chooseinput_alert_info.xml fuer einen AlertDialog, der
 * beim erstmaligen Starten dieser Ansicht erscheint und zusaetzliche
 * Erlaeuterungen bietet. Diese Ansicht beinhaltet Buttons, um 
 * 	- zur kartenbasierten Ortseingabe zu wechseln 
 *  - zur Eingabe in Adressform zu wechseln 
 * 	- zur Titelseite zurueckzukehren (Button in oberer Aktionsleiste)
 * Wird diese Klasse erneut aufgerufen, ohne dass versucht wurde, das Ereignis
 * an den Empfaenger zu uebermitteln, so erhaelt diese Klasse die bereits vom Nutzer
 * in anderen Bildschirmansichten eingegebenen Daten. Wechselt der Nutzer die
 * Bildschirmansicht erneut, so werden die Daten dafuer verwendet, auf der
 * naechsten Bildschirmansicht bereits getaetigte Benutzereingaben
 * wiederherzustellen.
 * Es wird dafuer gesorgt, dass der Ort nur auf der Karte oder als Adresse
 * eingegeben werden kann, um Inkonsistenzen der Angabe zu vermeiden.
 * 
 * @author Steffen Kappe
 */
public class ChooseInputActivity extends Activity implements OnClickListener {

	// Buttons, wie sie im Layout definiert wurden.
	private Button button_back;
	private Button button_map;
	private Button button_adress;

	// Gibt an, ob die Methode onCreate in dieser Instanz ausgefuehrt wurde
	private boolean onCreateRunned = false;

	// Beinhaltet den Namen der Activity, die den Start der vorliegenden
	// veranlasste
	private String calledActivity;

	// bereits getaetigte Nutzereingaben (s. Klassenbeschreibung)
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
	 * Buttons an das Layout zu binden. Auﬂerdem werden bereits eingegebene
	 * Nutzerdaten von der aufrufenden Bildschirmseite empfangen und der
	 * AlertDialog definiert, der dem Nutzer bei Aufruf dieser Bildschirmansicht
	 * zusaetzliche Informationen liefert.
	 * 
	 * @param savedInstanceState
	 *            von Android vorgegeben, stellt bei erneutem Start vorigen
	 *            Zustand der Klasse wieder her.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

		setContentView(R.layout.activity_chooseinput);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.activity_chooseinput_title);

		onCreateRunned = true;

		// Nutzerdaten der aufrufenden Bildschirmseite
		Bundle provideddata = getIntent().getExtras();
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

		// In dieser Bildschirmansicht verfuegbare Buttons werden mit ihren
		// Listenern verbunden, um ihnen die gewuenschte Funktionalitaet
		// zuzuweisen.
		button_map = (Button) findViewById(R.id.chooseinput_activity_button_map);
		button_map.setOnClickListener(this);

		button_back = (Button) findViewById(R.id.chooseinput_activity_button_back);
		button_back.setOnClickListener(this);

		button_adress = (Button) findViewById(R.id.chooseinput_activity_button_adress);
		button_adress.setOnClickListener(this);

		// AlertDialog, der dem Nutzer bei Aufruf dieser Bildschirmansicht
		// zusaetzliche Informationen liefert.
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Bestimmen Sie den Ort");
		LayoutInflater inflater2 = LayoutInflater.from(this);
		View addView = inflater2.inflate(
				R.layout.activity_chooseinput_alert_info, null);
		builder.setView(addView);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.setCancelable(false);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	/**
	 * onClick() startet einen AlertDialog, wenn bereits eine Ortsangabe in der
	 * Kartenansicht stattfand und der Nutzer von dieser Ansicht aus nun
	 * zusaetzlich eine Ortsangabe in Adressform angeben moechte, oder
	 * umgekehrt. Der AlertDialog weist darauf hin, dass genau eine Ortsangabe
	 * moeglich ist. Der Nutzer muss sich fuer eine Ortsangabeform entscheiden,
	 * die Nutzerangaben der jeweils anderen Form werden geloescht.
	 * 
	 * Schlieﬂlich wird mit submitIntent() der Uebergang zur naechsten
	 * Bildschirmansicht gestartet.
	 */
	@Override
	public void onClick(View v) {
		if ((v == button_adress) && (location_overlay_x != 0)) {

			displayAlertDialog(
					v,
					"Sie haben bereits einen Ort auf der Karte markiert. Moechten Sie "
							+ "diese Angabe loeschen und mit der Adresseingabe fortfahren?");
		}

		else if ((v == button_map)
				&& (locadress != null && !locadress.equals(""))) {

			displayAlertDialog(
					v,
					"Sie haben bereits eine Adresse eingegeben. Moechten Sie "
							+ "diese Angabe loeschen und mit der Ortsangabe auf der Kartenansicht fortfahren?");
		} else {
			submitIntent(v);
		}

	}

	/**
	 * submitIntent() wird von onClick() aufgerufen, welche den angetippten
	 * Button an diese Methode ueberliefert. Diese Methode hat die Aufgabe, eine
	 * neue Bildschirmansicht zu starten. Die bereits im Programmablauf
	 * getaetigten Nutzereingaben werden der neuen Bildschirmansicht verfuegbar
	 * gemacht (s. Klassenbeschreibung).
	 * 
	 * @param v
	 *            der Button, der vom Nutzer betaetigt wurde.
	 */
	public void submitIntent(View v) {
		if (v == button_map) {
			Intent intent = new Intent(this, MapsActivity.class);

			intent.putExtra("location_overlay_x", location_overlay_x);
			intent.putExtra("location_overlay_y", location_overlay_y);
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
		if (v == button_back) {
			Intent intent = new Intent(this, TitleActivity.class);

			intent.putExtra("location_overlay_x", location_overlay_x);
			intent.putExtra("location_overlay_y", location_overlay_y);
			intent.putExtra("locadress", locadress);
			intent.putExtra("citypart", citypart);
			intent.putExtra("comment", comment);
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
			finish();
		}
		if (v == button_adress) {
			Intent intent = new Intent(this, AdressActivity.class);

			intent.putExtra("locadress", locadress);
			intent.putExtra("citypart", citypart);
			intent.putExtra("comment", comment);
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
	public void onResume() {

		super.onResume();

		Bundle provideddata = getIntent().getExtras();
		calledActivity = provideddata.getString("calledActivity");

		if (calledActivity != null && !onCreateRunned) {

			if (calledActivity.equals("MapsActivity")) {
				location_overlay_x = provideddata.getInt("location_overlay_x");
				location_overlay_y = provideddata.getInt("location_overlay_y");
			} else {
				locadress = provideddata.getString("locadress");
				citypart = provideddata.getString("citypart");
				comment = provideddata.getString("comment");
			}

			if (provideddata.getString("observation_input_string") != null)
				observation_description = provideddata.getString(
						"observation_input_string").toString();
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
	 * Durch Implementierung von onNewIntent() ist die jeweils korrekte
	 * Bildschirmansicht bekannt, von der aus diese Bildschirmansicht gestartet
	 * wurde.
	 */
	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
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
	 * Stellt einen AlertDialog dar, der den Nutzer ueber eine bestimmte
	 * Nachricht informiert.
	 * 
	 * @param v
	 *            ein View-Element
	 * @param message
	 *            die darzustellende Nachricht
	 */
	public void displayAlertDialog(final View v, String message) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("Ort wurde bereits eingegeben!");

		builder.setMessage(message);

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Nutzer wuenscht Eingabe einer Adresse, hat aber Ort bereits
				// auf Karte eingegeben.
				// In diesem Fall werden die bereits eingegebenen Koordinaten
				// geloescht.
				if (v == findViewById(R.id.chooseinput_activity_button_adress)) {
					location_overlay_x = 0;
					location_overlay_y = 0;

					// Nutzer wuenscht Eingabe auf der Karte, hat aber bereits
					// Adresse eingegeben.
					// In diesem Fall wird die bereits eingegebene Adresse
					// geloescht.
				} else if (v == findViewById(R.id.chooseinput_activity_button_map)) {
					locadress = "";
					citypart = "";
					comment = "";
				}
				dialog.cancel();
				submitIntent(v);
			}
		});

		builder.setNegativeButton("Abbruch",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

		builder.setCancelable(false);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

}
