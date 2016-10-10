package de.uos.igf.stkappe.osnareport;

import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

/**
 * Klasse, die MapsActivity.java verwendet, um einen Overlay-Layer 
 * auf der Karte zu erzeugen und dessen Elemente zu verwalten. 
 * 
 * @author Steffen Kappe
 *
 */
public class CustomItemizedOverlay extends ItemizedOverlay {

	// die Items, die auf der Karte in einer neuen Schicht/Overlay platziert
	// werden
	public ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

	Context mContext;

	Long time;

	// Methode gibt die Position des Default Markers auf der Karte an.
	// In diesem Fall: "unten Mitte/CenterBottom"
	public CustomItemizedOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		// TODO Auto-generated constructor stub
	}

	// Methode gibt die Position des Default Markers auf der Karte an.
	// In diesem Fall: "unten Mitte/CenterBottom"
	// Initialisiert den Context
	public CustomItemizedOverlay(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		mContext = context;
	}

	// Fuegt ein Item zur ArrayList hinzu. populate() muss fuer
	// bestimmungsgemaeﬂen
	// Ablauf aufgerufen werden, denn sie bereitet das Item zum Zeichnen vor.
	// populate() ruft createItem() auf.
	public void addOverlay(OverlayItem overlay) {
		mOverlays.add(overlay);
		populate();
		time = new Date().getTime();
	}

	// populate() ruft diese methode auf, um das Item zu erhalten, was
	// gezeichnet werden soll.
	// Erhaelt das Item aus der Arraylist entsprechend dem i-ten Index.
	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	// Gibt die Groeﬂe der Arraylist/die Anzahl der Elemente in der Arraylist an
	@Override
	public int size() {
		return mOverlays.size();
	}

}
