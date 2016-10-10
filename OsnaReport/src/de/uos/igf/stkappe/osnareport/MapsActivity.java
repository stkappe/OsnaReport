package de.uos.igf.stkappe.osnareport;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

/**
 * Klasse fuer die kartenbasierte Ortseingabe. Stellt im Hauptfenster eine
 * Kartenansicht unter Verwendung von Google Maps-Kartenmaterial dar. Aus diesem
 * Grund erbt die Klasse von MapActivity anstelle von Activity. Verwendet fuer
 * das Hauptfenster das Layout res/layout/activity_map.xml und fuer die obere
 * Aktionsleiste das Layout res/layout/activity_map_title.xml. Bei Aufruf dieser
 * Bildschirmansicht wird dem Nutzer ein AlertDialog praesentiert, der
 * zusaetzliche Informationen bietet. Dieser Info-Dialog verwendet das Layout
 * res/layout/activity_map_alert_infotask_layout.xml. Nach Bestaetigung des
 * Dialogs stellt diese Klasse einen Banner am unteren Rand des Bildschirms dar,
 * der die wichtigsten Informationen des Dialogs dauerhaft bereitstellt.
 * Hierfuer wird das Layout res/layout/activity_map_toast_infotask_layout.xml
 * verwendet. Die Bildschirmansicht unterstuetzt sowohl das Hoch- als auch das
 * Querformat. Deshalb existiert ein Ordner res/layout-land, der das Layout die
 * Darstellung im Querformat enthaelt. Der Nutzer hat in der oberen
 * Aktionsleiste die Moeglichkeit, die GPS-Ortsbestimmung zu aktivieren, einen
 * Ort zu markieren, zur vorigen Bildschirmansicht zurueckzukehren oder den
 * Prozess fortzusetzen, indem zur naechsten Bildschirmseite gewechselt wird.
 * Diese Klasse speichert bisherige Nutzereingaben aus anderen
 * Bildschirmansichten zwischen. Diese werden zur jeweils anschlieﬂend
 * aufgerufenen Bildschirmansicht uebertragen. Sie werden dort ggf. wieder
 * dargestellt, sodass bereits eingegebene Daten nicht verloren gehen.
 * 
 * @author Steffen Kappe
 */
public class MapsActivity extends MapActivity implements LocationListener,
		OnClickListener {

	// Attribute, um die Karte steuern zu koennen
	private MapController mapcontroller;
	private LocationManager locationManager;
	private MapView mapView;

	// benoetigte boolsche Variablen
	private boolean location_input;
	private boolean alertdialog_closed = false;
	private boolean GPSStatus = true;
	private boolean onCreateRunned = false;

	// Die im Layout definierten Buttons
	private Button button_back;
	private Button button_mark_position;
	private Button button_gps;
	private Button button_continue;

	// Attribute, um auf die Banner zugreifen zu koennen, welche
	// Hilfsinformationen liefern
	private Toast toast_infotask;
	private Toast toast_gps;

	// benoetigt, um die Anzeigedauer der Toasts zu steuern
	private Timer timer = new Timer();

	// der ausgewaehlte Ort
	private GeoPoint loc;

	// bereits getaetigte Nutzereingaben (s. Klassenbeschreibung)
	private String observation_description;
	private int spinner_cat_selected_item;
	private int spinner_urg_selected_item;
	private String spinner_urg_other_time;
	private String name;
	private String adress;
	private String telnumber;

	/**
	 * Methode onCreate() ist ueberschrieben, um das Layout zu setzen und um die
	 * noetigen Funktionen und UI-Elemente zu initialisieren. Auﬂerdem werden
	 * bereits eingegebene Nutzerdaten von der aufrufenden Bildschirmseite
	 * empfangen.
	 * 
	 * @param savedInstanceState
	 *            von Android vorgegeben, stellt bei erneutem Start vorigen
	 *            Zustand der Klasse wieder her.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

		setContentView(R.layout.activity_map);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.activity_map_title);

		onCreateRunned = true;

		// Falls Dialog nutzerseitig bereits geschlossen, soll
		// er nicht erneut geoeffnet werden koennen.
		try {
			alertdialog_closed = savedInstanceState
					.getBoolean("alertdialog_closed");
		} catch (NullPointerException e) {
		}

		// Nutzerdaten der aufrufenden Bildschirmseite
		Bundle provideddata = getIntent().getExtras();
		loc = new GeoPoint(provideddata.getInt("location_overlay_y"),
				provideddata.getInt("location_overlay_x"));
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

		// stellt einen Dialog dar, um den Nutzer in diese Bildschirmansicht
		// einzufuehren. Nach Wechsel Der Bildschirmausrichtung
		// von Hoch- auf Querformat oder umgekehrt wird der Dialog
		// nur sichtbar sein, wenn er nicht vorher schon einmal geschlossen
		// wurde.
		if (!alertdialog_closed && loc.getLatitudeE6() == 0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Bestimmen Sie den Ort des Geschehens");
			LayoutInflater inflater2 = LayoutInflater.from(this);
			View addView = inflater2.inflate(
					R.layout.activity_map_alert_infotask_layout, null);
			builder.setView(addView);
			builder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							toast_infotask();
							alertdialog_closed = true;
						}
					});
			builder.setCancelable(false);
			AlertDialog dialog = builder.create();
			dialog.show();
		}

		// initialisieren der Info-Banner
		toast_infotask = new Toast(getBaseContext());
		toast_gps = new Toast(getBaseContext());

		// naechsten zwei zeilen ermoeglichen das zoomen
		mapView = (MapView) findViewById(R.id.mapview_maps);
		mapView.setBuiltInZoomControls(true);

		// um vorher gesetzte Overlays nach erneutem start wiederherzustellen
		if (loc.getLatitudeE6() != 0) {
			addOverlay(mapView,
					getResources().getDrawable(R.drawable.stecknadel_mitte1),
					loc);
		}

		// Um die Overlays nach Wechsel Hoch<->Querformat beizubehalten
		if (savedInstanceState != null
				&& savedInstanceState.getInt("overlay_x") != 0
				&& mapView.getOverlays().size() == 0) {
			loc = new GeoPoint(savedInstanceState.getInt("overlay_y"),
					savedInstanceState.getInt("overlay_x"));
			addOverlay(mapView,
					getResources().getDrawable(R.drawable.stecknadel_mitte1),
					loc);
		}

		location_input = false;

		// Fuer die GPS-Funktion
		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		// Setze initiale Position der Karte auf Osnabrueck
		// Stelle geeigneten Zoom-Level ein
		mapcontroller = mapView.getController();
		mapcontroller.animateTo(new GeoPoint(52273688, 8049009));
		mapcontroller.setZoom(13);

		// Initialisiere die Buttons und setze ihre Listener
		button_back = (Button) findViewById(R.id.maps_activity_button_back);
		button_back.setOnClickListener(this);
		button_mark_position = (Button) findViewById(R.id.maps_activity_button_mark_location);
		button_mark_position.setOnClickListener(this);
		button_gps = (Button) findViewById(R.id.maps_activity_button_gps);
		button_gps.setOnClickListener(this);
		button_continue = (Button) findViewById(R.id.maps_activity_button_continue);
		button_continue.setOnClickListener(this);

		mapView.setClickable(true);

	}

	/**
	 * Diese Methode stellt einen Banner (Toast) auf dem Bildschirm dar.
	 */
	public void toast_infotask() {
		timer = new Timer();
		LayoutInflater inflater = getLayoutInflater();
		View view = inflater.inflate(
				R.layout.activity_map_toast_infotask_layout,
				(ViewGroup) findViewById(R.id.toast_relativelayout));
		toast_infotask.setView(view);
		toast_infotask.setDuration(Toast.LENGTH_SHORT);

		TimerTask timertask = new TimerTask() {
			public void run() {
				toast_infotask.show();
			}
		};

		timer.schedule(timertask, 1000, 1000);
	}

	/**
	 * Diese Methode bestimmt die Funktionalitaet eines Buttons.
	 */
	public void onClick(View v) {

		// Falls "Zurueck"-Knopf betaetigt, wechselt der Nutzer
		// zur vorigen Bildschirmseite. Bereits eingegebene Nutzerdaten
		// werden mitgeliefert, um im spaeteren Programmverlauf ggf. Eingaben
		// in den entsprechenden Eingabefeldern wiederherzustellen
		if (v == button_back) {
			if (loc == null)
				loc = new GeoPoint(0, 0);
			Intent intent = new Intent(this, ChooseInputActivity.class);
			intent.putExtra("calledActivity", "MapsActivity");
			intent.putExtra("location_overlay_x", loc.getLongitudeE6());
			intent.putExtra("location_overlay_y", loc.getLatitudeE6());
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

		// Falls ein Ort markiert werden soll, wird die Abfrage des
		// Ortes per GPS gestoppt.
		if (v == button_mark_position) {
			location_input = true;
			locationManager.removeUpdates(this);
		}

		// GPS-Ortserkennung wird gestartet, sofern die GPS-Funktion
		// auf dem Geraet vorhanden und aktiviert ist.
		if (v == button_gps) {
			if (!locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				Toast toast = Toast.makeText(v.getContext(),
						"GPS nicht verfuegbar. Bitte aktivieren!",
						Toast.LENGTH_LONG);
				toast.show();

			} else {
				if (GPSStatus) {
					locationManager.removeUpdates(this);
					GPSStatus = false;
				} else {
					locationManager.requestLocationUpdates(
							LocationManager.GPS_PROVIDER, 60000, 0, this);
					GPSStatus = true;
				}
			}
		}

		// Nutzer moechte zur naechsten Bildschirmseite, um die Beobachtung
		// einzugeben. Falls kein Ort markiert wurde, wird eine Fehlermeldung
		// dargestellt.
		if (v == button_continue) {
			if (loc.getLatitudeE6() == 0) {
				timer.cancel();
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Bitte Ort angeben!");
				builder.setMessage("Bitte geben Sie einen Ort an, bevor sie fortfahren.");
				builder.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								toast_infotask();
								alertdialog_closed = true;
							}
						});
				AlertDialog dialog = builder.create();
				dialog.show();
			} else {
				Intent intent = new Intent(this, ObservationActivity.class);
				intent.putExtra("Activity", "MapsActivity");
				intent.putExtra("location_overlay_x", loc.getLongitudeE6());
				intent.putExtra("location_overlay_y", loc.getLatitudeE6());
				intent.putExtra("observation_input_string",
						observation_description);
				intent.putExtra("spinner_cat_selected_item",
						spinner_cat_selected_item);
				intent.putExtra("spinner_urg_selected_item",
						spinner_urg_selected_item);
				intent.putExtra("spinner_urg_other_time",
						spinner_urg_other_time);
				intent.putExtra("name", name);
				intent.putExtra("adress", adress);
				intent.putExtra("telnumber", telnumber);

				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
			}
		}

	}

	/**
	 * Diese Methode wird aufgerufen, sobald der Nutzer das Display antippt.
	 * Sofern eine Ortseingabe gewuenscht ist, hat location_input den Wert true
	 * und es wird eine Markierung auf der Karte an Stelle der Tippposition
	 * gesetzt.
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {

		// Ueberprueft, ob zuvor das Symbol zur Ortseingabe aktiviert wurde.
		// In diesem Fall ist der Fingertipp auf die Karte als Ortsangabe
		// (und nicht bpsw. als Verschiebung des Kartenausschnittes) zu deuten.
		if (location_input) {

			// bestimmt die Groeﬂe der Titlebar und der Statusbar des
			// Smartphones
			Rect rectgle = new Rect();
			Window window = getWindow();
			window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
			int StatusBarHeight = rectgle.top;
			int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT)
					.getTop();
			int TitleBarHeight = contentViewTop - StatusBarHeight;

			timer.cancel();
			toast_infotask.cancel();
			Projection proj = mapView.getProjection();

			// der Punkt auf dem Display, in genauen Pixelkoordinaten, wo
			// der Nutzer hingetippt hat. loc und loc_overlay beschreiben
			// denselben
			// Punkt, werden aber auf andere Weise weiterverarbeitet.
			loc = proj.fromPixels((int) ev.getX(), (int) (ev.getY()
					- StatusBarHeight - TitleBarHeight));
			GeoPoint loc_overlay = proj.fromPixels((int) (ev.getX()),
					(int) (ev.getY() - StatusBarHeight - TitleBarHeight));

			// das Icon, das auf dem angetippten Ort auf dem Display platziert
			// wird
			Drawable drawable = getResources().getDrawable(
					R.drawable.stecknadel_mitte1);
			addOverlay(this.mapView, drawable, loc_overlay);

			location_input = !location_input;
		}

		System.gc();
		return super.dispatchTouchEvent(ev);
	}

	/**
	 * Platziert auf der Karte einen durchsichtigen Layer, in der ein Icon
	 * (hier: die Stecknadel) platziert wird. So kann eine Kartenposition
	 * markiert werden.
	 * 
	 * @param view
	 *            Die zugrundeliegende Karte
	 * @param drawable
	 *            Grafik des Icons, das auf der Karte platziert werden soll
	 * @param loc_overlay
	 *            Position, an der drawable auf view platziert wird
	 */
	public void addOverlay(MapView view, Drawable drawable, GeoPoint loc_overlay) {
		List<Overlay> mapOverlays = view.getOverlays();

		// sofern bisher keine Overlays eingefuegt, erstelle ein neues und fuege
		// es
		// den (leeren) mapOverlays hinzu.
		if (mapOverlays.isEmpty()) {

			CustomItemizedOverlay customitemizedoverlay = new CustomItemizedOverlay(
					drawable, this);
			OverlayItem overlayitem = new OverlayItem(loc_overlay, "", "");
			customitemizedoverlay.addOverlay(overlayitem);
			mapOverlays.add(customitemizedoverlay);

			// wurde bereits eine Kartenmarkierung erstellt, so wird dieses
			// geloescht und
			// das neue auf der Karte eingefuegt.
		} else {
			view.getOverlays().remove(0);
			CustomItemizedOverlay customitemizedoverlay = new CustomItemizedOverlay(
					drawable, this);
			OverlayItem overlayitem = new OverlayItem(loc_overlay, "", "");
			customitemizedoverlay.addOverlay(overlayitem);
			mapOverlays.add(customitemizedoverlay);
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Sollte der GPS-Empfaenger eine neue Position empfangen haben, wird die
	 * Karte an diese Position bewegt.
	 */
	@Override
	public void onLocationChanged(Location location) {
		int lon = (int) (location.getLongitude() * 1000000);
		int lat = (int) (location.getLatitude() * 1000000);

		mapcontroller.animateTo(new GeoPoint(lat, lon));
		mapcontroller.setZoom(16);
	}

	/**
	 * onPause wird aufgerufen, sobald eine andere Activity in den Vordergrund
	 * tritt. Dann muessen die Banner vom Sichtfeld entfernt und die
	 * GPS-Ortsbestimmung beendet werden.
	 */
	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
		timer.cancel();
		toast_infotask.cancel();
		toast_gps.cancel();
		onCreateRunned = false;
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
	 * Methode onResume() wird bei Start der Bildschirmansicht nach onCreate()
	 * ausgefuehrt oder im spaeteren Applikationsverlauf bei erneutem Aufruf
	 * dieser Bildschirmseite, nachdem der Nutzer zuruecknavigierte. In
	 * letzterem Fall sollen die uebermittelten Daten der bis zu diesem
	 * Zeitpunkt aufgerufenen Bildschirmseiten hier voruebergehend
	 * zwischengespeichert werden (s. einleitenden Klassentext).
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (!onCreateRunned) {
			toast_infotask();
			Bundle provideddata = getIntent().getExtras();
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

			try {
				mapcontroller.zoomOut();
				mapcontroller.zoomIn();
			} catch (IllegalArgumentException e) {

			}
		}
	}

	/**
	 * Diese Methode speichert den Status des User Interfaces in das Objekt
	 * savedInstanceState. Durch Aufruf dieses Objektes in der Methode onCreate
	 * wird der Status wiederhergestellt.
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putBoolean("alertdialog_closed", alertdialog_closed);
		savedInstanceState.putBoolean("drawoverlay", true);
		if (loc != null) {
			savedInstanceState.putInt("overlay_x", loc.getLongitudeE6());
			savedInstanceState.putInt("overlay_y", loc.getLatitudeE6());
		}
	}

	/**
	 * Stellt den Status des User Interfaces wieder her. savedInstanceState wird
	 * onCreate uebergeben.
	 */
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// Restore UI state from the savedInstanceState.
		// This bundle has also been passed to onCreate.
		alertdialog_closed = savedInstanceState
				.getBoolean("alertdialog_closed");
	}

	/**
	 * Diese Methode beendet den Timer, um in jedem Fall zu verhindern, dass das
	 * Toast bei geschlossener App noch sichtbar ist.
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		timer.cancel();
	}

	// verpflichtende Methode durch Implementation von OnLocationListener
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	// verpflichtende Methode durch Implementation von OnLocationListener
	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	// verpflichtende Methode durch Implementation von OnLocationListener
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}
}
