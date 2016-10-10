package de.uos.igf.stkappe.osnareport;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.BadTokenException;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import java.lang.String;

/**
 * Diese Klasse definiert die Bildschirmansicht, um die persoenlichen Daten
 * angeben zu koennen. Es ist moeglich, Name, Adresse und Telefonnummer
 * einzugeben. Anschlieﬂend koennen die Daten mit Tipp auf den Button
 * "button_proceed" zum Empfaenger uebertragen werden. Diese Klasse verwendet
 * das Layout res/layout/activity_person.xml fuer das Hauptfenster und
 * res/layout/activity_person_title.xml fuer die obere Aktionsleiste. Auﬂerdem
 * res/layout/activity_person_alert_X.xml fuer den jeweiligen Status der
 * Uebertragung (done, error, waiting). In der oberen Aktionsleiste befindet
 * sich ein Button, um zur vorigen Bildschirmseite zurueckzukehren. Diese Klasse
 * speichert bisherige Nutzereingaben aus anderen Bildschirmansichten zwischen.
 * Falls eine vorige Bildschirmansicht aufgerufen wird, so werden diese zur
 * jeweiligen Bildschirmansicht uebertragen. Sie werden dort in den
 * entsprechenen Eingabefeldern wieder dargestellt, sodass bereits eingegebene
 * Daten nicht verloren gehen. Falls der Prozess hier abgeschlossen wird, so
 * werden die Daten dazu verwendet, um sie an den Empfanger zu uebertragen
 * 
 * @author Steffen Kappe
 */
public class PersonActivity extends Activity implements OnClickListener {

	// Im Layout definierte UI-Elemente
	Button button_back;
	Button button_proceed;
	EditText edittext_name;
	EditText edittext_adress;
	EditText edittext_telnumber;

	AlertDialog dialog;
	PersonActivity personact = this;
	View view;

	// Benoetigte boolsche Variablen
	private boolean error;
	private boolean waiting;

	// provided Data from other Activities
	public int location_overlay_x;
	public int location_overlay_y;
	private String locadress;
	private String citypart;
	private String comment;
	public String observation_description;
	public String spinner_cat_choice;
	public String spinner_urg_choice;
	public String name;
	public String adress;
	public String telnumber;

	/**
	 * onCreate weist das Layout zu, liest bereits eingegebene Nutzerdaten von
	 * anderen Activities aus, weist die Eingaben ggf. den auf dieser
	 * Bildschirmseite vorhandenen Eingabefeldern zu, liest die Telefonnummer
	 * vom Geraet aus, sofern noetig und moeglich und intialisiert die
	 * UI-Elemente.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

		// Setze das Layout
		setContentView(R.layout.activity_person);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.activity_person_title);

		// Uebermittelte Daten von anderen Activities
		Bundle provideddata = getIntent().getExtras();
		location_overlay_x = provideddata.getInt("location_overlay_x");
		location_overlay_y = provideddata.getInt("location_overlay_y");
		locadress = provideddata.getString("locadress");
		citypart = provideddata.getString("citypart");
		comment = provideddata.getString("comment");
		observation_description = provideddata
				.getString("observation_input_string");
		edittext_name = (EditText) findViewById(R.id.person_activity_edittext_name);
		edittext_adress = (EditText) findViewById(R.id.person_activity_edittext_adress);
		edittext_telnumber = (EditText) findViewById(R.id.person_activity_edittext_telnumber);
		spinner_cat_choice = provideddata.getString("spinner_cat_choice");
		spinner_urg_choice = provideddata.getString("spinner_urg_choice");
		name = provideddata.getString("name");
		adress = provideddata.getString("adress");
		telnumber = provideddata.getString("telnumber");

		// Weist den Eingabefeldern bisherige Eingaben zu, falls
		// diese Bildschirmansicht wiederholt aufgerufen wird
		char[] tmp;
		if (name != null) {
			tmp = name.toCharArray();
			edittext_name.setText(tmp, 0, tmp.length);
		}
		if (adress != null) {
			tmp = adress.toCharArray();
			edittext_adress.setText(tmp, 0, tmp.length);
		}

		// Liest die Telefonnummer automatisch vom Telefon aus,
		// sofern bisher keine Eingabe der Telefonnummer erfolgt ist
		// und das automatische Auslesen vom Geraet aus moeglich ist
		if (telnumber == null) {
			try {
				TelephonyManager tMgr = (TelephonyManager) this
						.getSystemService(Context.TELEPHONY_SERVICE);
				tmp = tMgr.getLine1Number().toCharArray();
				edittext_telnumber.setText(tmp, 0, tmp.length);
			} catch (Exception e) {
				edittext_telnumber.setText("");
				// Telefonmanager nicht vorhanden (z.B. Flugmodus)
			}
		} else {
			tmp = telnumber.toCharArray();
			edittext_telnumber.setText(tmp, 0, tmp.length);
		}

		// Initialisiert die verwendeten Buttons und belegt sie mit Listenern
		button_back = (Button) findViewById(R.id.person_activity_button_back);
		button_back.setOnClickListener(this);

		button_proceed = (Button) findViewById(R.id.person_activity_button_proceed);
		button_proceed.setOnClickListener(this);

	}

	/**
	 * Wird aufgerufen, sobald auf einen Button getippt wurde. Definiert den
	 * Ablauf, der nach Tipp auf einen Button ausgefuehrt wird
	 */
	@Override
	public void onClick(View v) {

		// Macht die Eingaben der TextViews zur Eingabe des Namens,
		// der Adresse und der Telefonnummer in der Klasse bekannt
		name = edittext_name.getText().toString();
		adress = edittext_adress.getText().toString();
		telnumber = edittext_telnumber.getText().toString();
		String request;

		// Nutzer moechte zur vorigen Bildschirmansicht zurueckkehren
		if (v == button_back) {
			Intent intent = new Intent(this, ObservationActivity.class);

			intent.putExtra("name", name);
			intent.putExtra("adress", adress);
			intent.putExtra("telnumber", telnumber);
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
			finish();
		}

		// Nutzer moechte die Daten zum Empfaenger uebermitteln
		if (v == button_proceed) {

			// Stelle Dialog dar, der den Nutzer ueber den Status der
			// Uebertragung informiert
			LayoutInflater inflater2 = LayoutInflater.from(this);
			View addView = inflater2.inflate(
					R.layout.activity_person_alert_waiting, null);
			displayAlertDialog(addView, true, "waiting");

			// Schleife wird ausgefuehrt, wenn der Nutzer den Ort auf der Karte
			// angegeben hat. Dann: Stelle String mit allen noetigen
			// Informationen
			// zusammen, starte eine Instanz der Klasse Client in einem neuen
			// Thread, uebergebe den String,
			// um dann die Daten zu uebertragen. Das Auslagern der Uebertragung
			// in einen neuen
			// Thread beguenstigt einen fluessigen Programmablauf
			if (location_overlay_x != 0 && locadress == null) {
				request = String.valueOf((double) location_overlay_x / 1000000)
						+ ","
						+ String.valueOf((double) location_overlay_y / 1000000)
						+ "," + observation_description + ","
						+ spinner_cat_choice + "," + spinner_urg_choice + ","
						+ name + "," + adress + "," + telnumber;
				Thread t1 = new Thread(new Client(this,
						"vm158.rz.uni-osnabrueck.de", 5432, "kappeGIS",
						"kappe", "cxptra", request, "map"));
				t1.start();
			}

			// Schleife wird ausgefuehrt, wenn der Nutzer den Ort als Adresse
			// angegeben hat. Dann: Stelle String mit allen noetigen
			// Informationen
			// zusammen, starte eine Instanz der Klasse Client in einem neuen
			// Thread, uebergebe den String,
			// um dann die Daten zu uebertragen. Das Auslagern der Uebertragung
			// in einen neuen
			// Thread beguenstigt einen fluessigen Programmablauf
			else if (location_overlay_x == 0) { // Dann ist location_overlay_y
												// ebenfalls = 0
				request = locadress + "," + citypart + "," + comment + ","
						+ observation_description + "," + spinner_cat_choice
						+ "," + spinner_urg_choice + "," + name + "," + adress
						+ "," + telnumber;

				Thread t1 = new Thread(new Client(this,
						"vm158.rz.uni-osnabrueck.de", 5432, "kappeGIS",
						"kappe", "cxptra", request, "adress"));
				t1.start();
			}

		}
	}

	/**
	 * Methode, um das PopUp darzustellen, was den Nutzer ueber den Status der
	 * Uebertragung der Daten informiert
	 * 
	 * @param addView
	 *            gibt den im PopUp darzustellenden Inhalt an
	 * @param result
	 *            gibt an, ob ein Fehler aufgetreten ist
	 * @param message
	 *            mitgelieferte erlaeuternde Nachricht
	 */
	public void displayAlertDialog(View addView, boolean result, String message) {

		view = addView;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("‹bertragung der Eingaben...");

		builder.setView(addView);

		// Die Uebertragung der Eingaben laeuft.
		if (message.equals("waiting")) {
			ImageView img = (ImageView) addView
					.findViewById(R.id.person_activity_alert_image_waiting);
			img.setBackgroundResource(R.drawable.spin_animation);

			AnimationDrawable frameAnimation = (AnimationDrawable) img
					.getBackground();

			// Startet eine Animation, die den aktuellen Status mittels eines
			// sich drehenden, farbigen Kreises visualisiert
			frameAnimation.start();
			waiting = true;
		}

		// Die Uebertragung der Daten war erfolgreich
		else if (message.equals("done")) {
			waiting = false;
		}

		// Die Uebertragung der Daten war nicht erfolgreich.
		// Stelle in der TextView "txt" die Fehlermeldung dar.
		else {
			TextView txt = (TextView) addView
					.findViewById(R.id.person_activity_alert_texterrormessage);
			txt.setText("Fehlermeldung: " + message);
			waiting = false;
			error = true;
		}

		// Stelle einen Button zum Bestaetigen im PopUp dar, welcher
		// das PopUp beendet. Falls die Uebertragung
		// erfolgreich war, wird der gesamte Vorgang abgeschlossen und der
		// Nutzer kehrt zur Titelseite zurueck. Falls die Uebertragung
		// noch im Gange ist oder fehlerhaft war, kann der Nutzer die
		// Uebertragung
		// ggf. abbrechen und dieses PopUp schlieﬂen.
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				if (!error && !waiting) {
					Intent intent = new Intent(getApplicationContext(),
							TitleActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}
			}
		});

		// Falls die Uebertragung fehlerhaft war, stelle einen zweiten Knopf im
		// PopUp
		// dar, ueber den der Nutzer trotz einer nicht erfolgreichen
		// Uebertragung
		// zurueck zur Titelseite gelangt.
		if (error) {
			builder.setNegativeButton("Vorgang beenden",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							Intent intent = new Intent(getApplicationContext(),
									TitleActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
						}
					});
		}

		builder.setCancelable(false);
		dialog = builder.create();
		dialog.show();
	}

	/**
	 * onPause wird aufgerufen, sobald eine andere Activity in den Vordergrund
	 * tritt.
	 */
	@Override
	protected void onPause() {
		super.onPause();

	}

	/**
	 * Methode onResume() wird bei Start der Bildschirmansicht nach onCreate()
	 * ausgefuehrt.
	 */
	@Override
	protected void onResume() {
		super.onResume();

	}

	/**
	 * Methode kommuniziert mit dem nebenlaeufigen Thread zur Uebertragung der
	 * Eingaben. Falls dieser Thread eine Nachricht bzgl. des
	 * Uebertragungsstatus sendet, so kann diese Klasse sie entgegennehmen und
	 * den Nutzer ueber die Darstellung eines PopUp (prepare_AlertDialog())
	 * entsprechend informieren.
	 */
	final Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bundle receivedData = msg.getData();
			boolean result = receivedData.getBoolean("result");
			String message = receivedData.getString("error");
			dialog.dismiss();

			try {
				prepareView_AlertDialog(result, message);
			} catch (BadTokenException b) {
				// Exception, wenn der nebenlaeufige Thread zur Uebertragung der
				// Eingaben
				// nicht beendet ist, aber die App bereits geschlossen ist.
				// Dann: schlieﬂt sich der Thread automatisch und hier ist keine
				// Aktion erforderlich
			}
		}
	};

	/**
	 * Wird von Methode handleMessage() aufgerufen und bereitet die Darstellung
	 * eines PopUps vor, welcher dann per displayAlertDialog mit den
	 * entsprechenden Parametern gestartet wird.
	 * 
	 * @param result
	 *            Erfolg der Datenuebertragung
	 * @param message
	 *            mitgelieferte erlaeuternde Nachricht
	 */
	public void prepareView_AlertDialog(boolean result, String message) {
		if (result) {
			LayoutInflater inflater2 = LayoutInflater.from(this);
			View addView = inflater2.inflate(
					R.layout.activity_person_alert_done, null);
			message = "done";
			displayAlertDialog(addView, result, message);
		} else {
			LayoutInflater inflater2 = LayoutInflater.from(this);
			View addView = inflater2.inflate(
					R.layout.activity_person_alert_error, null);
			displayAlertDialog(addView, result, message);
		}
	}

}
