package de.uos.igf.stkappe.osnareport;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

/**
 * Diese Klasse definiert die Bildschirmansicht zur nutzerseitigen Erfassung
 * einer Beobachtung. Es ist moeglich, die Beobachtung in ein Textfeld
 * einzugeben, sie in eine der vorgegenenen Kategorien einzuordnen und ihre
 * Dringlichkeit einzustufen. Wurde der Ort zuvor auf einer Karte markiert, so
 * wird ein kleiner Ausschnitt der Karte auf dieser Bildschirmseite angezeigt
 * und der Nutzer kann durch Antippen zur MapsActivity zurueckkehren und den Ort
 * korrigieren. Diese Klasse verwendet das Layout
 * res/layout/activity_observation_maps.xml fuer das Hauptfenster, falls eine
 * kartenbasierte Ortseingabe vorliegt oder
 * res/layout/activity_observation_withoutmaps.xml, falls dies nicht der Fall
 * ist, sowie res/layout/activity_adress_title.xml fuer die obere Aktionsleiste,
 * in der zwei Buttons integriert sind, um entweder zur vorigen
 * Bildschirmansicht zurueckzukehren oder um die Eingaben zu bestaetigen und zur
 * Angabe der Personendaten fortzufahren. Diese Klasse speichert bisherige
 * Nutzereingaben aus anderen Bildschirmansichten zwischen. Diese werden zur
 * jeweils anschlieﬂend aufgerufenen Bildschirmansicht uebertragen. Sie werden
 * dort ggf. wieder dargestellt, sodass bereits eingegebene Daten nicht verloren
 * gehen.
 * 
 * @author Steffen Kappe
 */
public class ObservationActivity extends MapActivity implements
		OnClickListener, OnItemSelectedListener {

	// Gibt an, welche Activity (AdressActivity oder MapsActivity) diese
	// Activity aufgerufen hat
	private String calledActivity;

	// UI-Elemente, wie im Layout definiert
	private MapView mapView;
	private EditText edittext_view;
	private Button button_back;
	private Button button_continue;
	private Button button_maps;
	private Spinner spinner_cat;
	private Spinner spinner_urg;

	// Adapter, um die Spinner mit auswaehlbaren Daten zu versehen
	private ArrayAdapter<CharSequence> adapter_cat;
	private ArrayAdapter<CharSequence> adapter_urg;

	// benoetigte boolsche Variablen
	private boolean input_dialog = true;
	private boolean keyboard_open = false;
	private boolean onCreateRunned = false;

	// Nutzerdaten der aufrufenden Bildschirmseite (s. Klassenbeschreibung)
	private GeoPoint loc;
	private String locadress;
	private String citypart;
	private String comment;
	private String observation_description;
	private String spinner_urg_other_time = null;
	private String name;
	private String adress;
	private String telnumber;

	/**
	 * Diese Methode setzt das Layout in Abhaengigkeit der aufrufenden Activity.
	 * Auﬂerdem initialisiert sie alle noetigen UI-Elemente und empfaengt
	 * bisherige Nutzereingaben (s. einleitende Klassenbeschreibung).
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

		Bundle provideddata = getIntent().getExtras();
		calledActivity = provideddata.getString("Activity");

		// Setzt Layout in Abhaengigkeit davon, ob MapsActivity oder
		// AdressActivity diese Activity aufruft
		// Im Falle von MapsActivity wird ein kleines Kartenfenster am rechten
		// oberen Rand
		// dargestellt, mit dessen Hilfe der Nutzer die Ortsangabe korrigieren
		// kann
		if (calledActivity.equals("MapsActivity"))
			setContentView(R.layout.activity_observation_maps);
		else
			setContentView(R.layout.activity_observation_withoutmaps);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.activity_observation_title);

		onCreateRunned = true;

		// Initialisiert die Spinner und das Eingabefenster fuer die Beobachtung
		spinner_cat = (Spinner) findViewById(R.id.observation_activity_spinner_category);
		adapter_cat = ArrayAdapter.createFromResource(this,
				R.array.spinner_category_data,
				android.R.layout.simple_spinner_item);
		adapter_cat
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_cat.setAdapter(adapter_cat);
		spinner_cat.setSelection(0);
		spinner_cat.setOnItemSelectedListener(this);

		spinner_urg = (Spinner) findViewById(R.id.observation_activity_spinner_urgency);
		adapter_urg = ArrayAdapter.createFromResource(this,
				R.array.spinner_urgency_data,
				android.R.layout.simple_spinner_item);
		adapter_urg
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_urg.setAdapter(adapter_urg);
		spinner_urg.setSelection(0);
		spinner_urg.setOnItemSelectedListener(this);

		edittext_view = (EditText) findViewById(R.id.observation_activity_edittext);

		// Speichert Nutzereingaben zwischen (s. Klassenbeschreibung)
		loc = new GeoPoint(provideddata.getInt("location_overlay_y"),
				provideddata.getInt("location_overlay_x"));
		locadress = provideddata.getString("locadress");
		citypart = provideddata.getString("citypart");
		comment = provideddata.getString("comment");
		spinner_cat.setSelection(provideddata
				.getInt("spinner_cat_selected_item"));
		spinner_urg.setSelection(provideddata
				.getInt("spinner_urg_selected_item"));
		spinner_urg_other_time = provideddata
				.getString("spinner_urg_other_time");
		name = provideddata.getString("name");
		adress = provideddata.getString("adress");
		telnumber = provideddata.getString("telnumber");
		observation_description = provideddata
				.getString("observation_input_string");
		edittext_view.setText(observation_description);

		// Verhindert, dass der Nutzer bei wiederholtem Aufruf dieser Klasse
		// erneut eine Angabe bei der Dringlichkeit "anderer Zeitpunkt" taetigen
		// muss
		if (spinner_urg.getSelectedItemPosition() == 2) {
			input_dialog = false;
		}

		// Initialisiert die Buttons und weist ihnen Listener zu
		button_back = (Button) findViewById(R.id.observation_activity_button_back);
		button_back.setOnClickListener(this);

		button_continue = (Button) findViewById(R.id.observation_activity_button_continue);
		button_continue.setOnClickListener(this);

		// Falls MapsActivity diese Bildschirmansicht startete, wird ein kleiner
		// Kartenausschnitt am rechten oberen Rand dargestellt, welcher hier
		// initialisiert wird.
		if (calledActivity.equals("MapsActivity")) {

			button_maps = (Button) findViewById(R.id.observation_activity_button_mapview);
			button_maps.setOnClickListener(this);

			mapView = (MapView) findViewById(R.id.observation_activity_mapview);
			MapController mapcontroller = mapView.getController();
			mapcontroller.animateTo(loc);
			mapcontroller.setZoom(17);

			Drawable drawable = getResources().getDrawable(
					R.drawable.stecknadel_mitte2);
			new MapsActivity().addOverlay(mapView, drawable, loc);
		}
		findViewById(R.id.relativelayout1).requestFocus();
	}

	/**
	 * Diese Methode wird aufgerufen, sobal der Nutzer das Display antippt.
	 * Falls der Nutzer im Eingabefeld "edittext_view" Buchstaben eingibt und
	 * anschlieﬂend auf eine Position auﬂerhalb des Eingabefeldes tippt, so soll
	 * diese Methode die softwareseitige Tastatur verschwinden lassen.
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {

		View v = getCurrentFocus();

		boolean ret = super.dispatchTouchEvent(event);

		// Sollte der Nutzer das Eingabefeld angetippt haben, wird dieser
		// Block ausgefuehrt
		if (v instanceof EditText) {
			View w = getCurrentFocus();
			int scrcoords[] = new int[2];
			w.getLocationOnScreen(scrcoords);

			// Zieht vom angetippten Punkt (event.getRawX() / event.getRawY())
			// die Hoehe der
			// StatusBar und TitleBar ab, sodass beim Klick auf die Unterkante
			// der TitleBar genau float y = 0 ist.
			float x = event.getRawX() + w.getLeft() - scrcoords[0];
			float y = event.getRawY() + w.getTop() - scrcoords[1];

			// Tippt der Nutzer auf das Display neben das Eingabefeld
			// "edittext_view", so ist diese if-Bedingung erfuellt.
			if (event.getAction() == MotionEvent.ACTION_UP
					&& x > button_continue.getRight()
					|| x < button_continue.getLeft()
					&& y < button_continue.getTop()
					|| y > button_continue.getBottom()
					&& x > edittext_view.getRight()
					|| x < edittext_view.getLeft()
					&& y < edittext_view.getTop()
					|| y > edittext_view.getBottom()) {

				// Die Karte ist wird nicht in jedem Fall angezeigt und muss
				// extra abgefragt werden
				if (calledActivity.equals("MapsActivity")) {
					if (x > mapView.getRight() || x < mapView.getLeft()
							&& y < mapView.getTop() || y > mapView.getBottom()) {
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(getWindow()
								.getCurrentFocus().getWindowToken(), 0);
						keyboard_open = false;
					}
				}

				// Schlieﬂe die Tastatur
				else {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(getWindow().getCurrentFocus()
							.getWindowToken(), 0);
					keyboard_open = false;
				}

			}
			if (x < edittext_view.getRight() && x > edittext_view.getLeft()
					&& y > edittext_view.getTop()
					&& y < edittext_view.getBottom()) {
				keyboard_open = true;
			}
		}
		return ret;
	}

	/**
	 * Definiert den Ablauf bei Antippen eines Buttons
	 */
	public void onClick(View v) {

		observation_description = edittext_view.getText().toString();

		// Falls dieser Button angetippt wurde, so bewegt sich der Nutzer
		// im Ablauf eine Bildschirmseite zurueck.
		// Bereits eingegebene Nutzerdaten werden mitgeliefert (s.
		// Klassenbeschreibung)
		if (v == button_back) {
			// Wenn diese Klasse von MapsActivity aufgerufen wurde, so
			// muss der Nutzer zu dieser Activity zurueckgeleitet werden
			if (calledActivity.equals("MapsActivity")) {
				Intent intent = new Intent(this, MapsActivity.class);
				intent.putExtra("observation_input_string",
						observation_description);
				intent.putExtra("spinner_cat_selected_item",
						spinner_cat.getSelectedItemPosition());
				intent.putExtra("spinner_urg_selected_item",
						spinner_urg.getSelectedItemPosition());
				intent.putExtra("spinner_urg_other_time",
						spinner_urg_other_time);
				intent.putExtra("name", name);
				intent.putExtra("adress", adress);
				intent.putExtra("telnumber", telnumber);
				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
				finish();
			}
			// Wenn diese Klasse von AdressActivity aufgerufen wurde, so
			// muss der Nutzer zu dieser Activity zurueckgeleitet werden
			else {
				Intent intent = new Intent(this, AdressActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				intent.putExtra("calledActivity", "AdressActivity");
				intent.putExtra("locadress", locadress);
				intent.putExtra("citypart", citypart);
				intent.putExtra("comment", comment);
				intent.putExtra("observation_input_string",
						observation_description);
				intent.putExtra("spinner_cat_selected_item",
						spinner_cat.getSelectedItemPosition());
				intent.putExtra("spinner_urg_selected_item",
						spinner_urg.getSelectedItemPosition());
				intent.putExtra("spinner_urg_other_time",
						spinner_urg_other_time);
				intent.putExtra("name", name);
				intent.putExtra("adress", adress);
				intent.putExtra("telnumber", telnumber);
				startActivity(intent);
				finish();
			}
		}

		// Wurde auf die Karte getippt, so wird der Nutzer zur MapsActivity
		// zurueckgeleitet
		if (v == button_maps) {
			if (keyboard_open) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getWindow().getCurrentFocus()
						.getWindowToken(), 0);
				keyboard_open = false;
			} else {
				Intent intent = new Intent(this, MapsActivity.class);
				intent.putExtra("observation_input_string",
						observation_description.toString());
				intent.putExtra("spinner_cat_selected_item",
						spinner_cat.getSelectedItemPosition());
				intent.putExtra("spinner_urg_selected_item",
						spinner_urg.getSelectedItemPosition());
				intent.putExtra("spinner_urg_other_time",
						spinner_urg_other_time);
				intent.putExtra("name", name);
				intent.putExtra("adress", adress);
				intent.putExtra("telnumber", telnumber);

				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
				finish();
			}
		}

		// Wurde der Button angetippt, um voranzuschreiten, so wird die Activity
		// zur Eingabe der
		// personenbezogenen Daten geoeffnet
		if (v == button_continue) {
			if (keyboard_open) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getWindow().getCurrentFocus()
						.getWindowToken(), 0);
				keyboard_open = false;
			} else {
				Intent intent = new Intent(this, PersonActivity.class);
				intent.putExtra("observation_input_string",
						observation_description.toString());
				intent.putExtra("location_overlay_x", loc.getLongitudeE6());
				intent.putExtra("location_overlay_y", loc.getLatitudeE6());
				intent.putExtra("locadress", locadress);
				intent.putExtra("citypart", citypart);
				intent.putExtra("comment", comment);
				intent.putExtra(
						"spinner_cat_choice",
						spinner_cat.getItemAtPosition(
								spinner_cat.getSelectedItemPosition())
								.toString());
				if (spinner_urg_other_time != null)
					intent.putExtra("spinner_urg_choice",
							spinner_urg_other_time);
				else
					intent.putExtra(
							"spinner_urg_choice",
							spinner_urg.getItemAtPosition(
									spinner_urg.getSelectedItemPosition())
									.toString());
				intent.putExtra("name", name);
				intent.putExtra("adress", adress);
				intent.putExtra("telnumber", telnumber);
				keyboard_open = true;
				startActivity(intent);
			}
		}
	}

	/**
	 * onPause wird aufgerufen, sobald eine andere Activity in den Vordergrund
	 * tritt.
	 */
	@Override
	protected void onPause() {
		super.onPause();
		onCreateRunned = false;
	}

	/**
	 * Methode onResume() wird bei Start der Bildschirmansicht nach onCreate()
	 * ausgefuehrt oder im spaeteren Applikationsverlauf bei erneutem Aufruf
	 * dieser Bildschirmseite, nachdem der Nutzer zuruecknavigierte. In
	 * letzterem Fall sollen die uebermittelten Daten der bis zu diesem
	 * Zeitpunkt aufgerufenen Bildschirmseiten hier voruebergehend
	 * zwischengespeichert werden (s. einleitenden Klassentext).
	 */
	@Override
	public void onResume() {
		super.onResume();

		if (!onCreateRunned) {
			Bundle provideddata = getIntent().getExtras();
			name = provideddata.getString("name");
			adress = provideddata.getString("adress");
			telnumber = provideddata.getString("telnumber");
		}
	}

	/**
	 * Diese Methode wird aufgerufen, sobald der Nutzer im Spinner ein Element
	 * auswaehlt. Der ausgewaehlte Eintrag wird in den Datenfeldern gespeichert.
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		if (view != null) {

			if (parent.getAdapter() == adapter_urg) {

				final TextView textview = (TextView) view;

				if (spinner_urg.getSelectedItemPosition() == 2 && input_dialog) {
					AlertDialog.Builder alert = new AlertDialog.Builder(this);

					alert.setTitle("Wie akut bedarf das Anliegen einer Loesung?");

					final EditText input = new EditText(this);
					alert.setView(input);

					alert.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									String value = input.getText().toString();
									spinner_urg_other_time = value;
									if (spinner_urg_other_time.length() <= 15) {
										textview.setText(spinner_urg_other_time);
									} else {
										textview.setText(spinner_urg_other_time
												.substring(0, 15) + " ...");
									}
								}
							});
					alert.show();

				}

				// ObservationActivity wird mind. zum 2. Mal aufgerufen. Dabei
				// wird die
				// vorherigen Eingabe in dem Spinner "Dringlichkeit" /
				// "sonstige Dringlichkeit" wiederhergestellt
				// und der AlertDialog soll nicht erneut aufgerufen werden.
				if (spinner_urg.getSelectedItemPosition() == 3 && !input_dialog) {
					textview.setText(spinner_urg_other_time);
				}
				input_dialog = true;
			}

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

	// Durch Implementation von OnItemSelectedListener vorgegeben
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	// Durch Erben von MapActivity vorgegeben
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}
