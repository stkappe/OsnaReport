package de.uos.igf.stkappe.osnareport;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

import de.uos.igf.stkappe.osnareport.R;

/**
 * Klasse fuer die Seite der App, die dem Nutzer die Funktion der App
 * erlaeutert. Auﬂerdem werden in dieser Ansicht die Quellen der verwendeten
 * Ressourcen (Bilder, Symbole) angegeben. Sie Verwendet das Layout
 * res/layout/activity_info.xml, sowie fuer die obere Aktionsleiste
 * activity_info_title.xml. Der Nutzer hat von der oberen Aktionsleiste dieser
 * Seite aus die Moeglichkeit, zur Titelseite zurueckzukehren.
 * 
 * @author Steffen Kappe
 */

public class InfoActivity extends Activity {

	/**
	 * Methode onCreate() ist ueberschrieben, um das Layout zu setzen und um den
	 * Button in der oberen AKtionsleiste mit einer Funktion zu versehen.
	 * 
	 * @param savedInstanceState
	 *            von Android vorgegeben, stellt bei erneutem Start vorigen
	 *            Zustand der Klasse wieder her.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

		setContentView(R.layout.activity_info);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.activity_info_title);

		findViewById(R.id.info_activity_button_back).setOnClickListener(
				new OnClickListener() {

					// onClick-Listener fuer den Button in der Titelleiste,
					// um zur Titelseite zurueckzukehren.
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(getApplicationContext(),
								TitleActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
						startActivity(intent);

					}
				});

	}

}
