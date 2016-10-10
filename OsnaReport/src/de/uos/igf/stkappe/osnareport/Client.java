package de.uos.igf.stkappe.osnareport;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

/**
 * Klasse zur Uebertragung der Nutzerdaten an den Empfaenger. Wird von
 * PersonActivity aufgerufen und Die Uebertragung der Daten geschieht in einem
 * seperatem Thread.
 * 
 * @author Steffen Kappe
 */
public class Client implements Runnable {

	// Benoetigte Daten zum Aufbau der Datenbankverbindung
	String server;
	int port;
	String dbname;
	String username;
	String password;
	String type;

	// Die eingegebenen Nutzerdaten
	String[] location;
	String[] observation;
	String[] person;

	// Benoetigte zusaetzliche Datenfelder
	PersonActivity inst_person;
	boolean result = false;
	String error = "";

	// Konstruktor, welcher die uebergebenen Daten den Datenfeldern
	// zuweist. Dabei wird der uebergebene requeststring gemaeﬂ seinem
	// Inhalt in den entsprechenden String-Arrays (s.o.) gespeichert.
	public Client(PersonActivity inst_person, String server, int port,
			String dbname, String username, String password,
			String requeststring, String type) {

		// Informationen zur Datenbankverbindung
		this.server = server;
		this.port = port;
		this.dbname = dbname;
		this.username = username;
		this.password = password;
		this.inst_person = inst_person;
		this.type = type;

		// Weist die uebergebenen Ortsangaben dem entsprechenden
		// Array zu
		String[] tmp = requeststring.split(",");
		this.observation = new String[6];
		this.person = new String[3];
		if (type.equals("map")) {
			location = new String[2];
			location[0] = tmp[0];
			location[1] = tmp[1];

			for (int i = 0; i < tmp.length - 5; i++) {

				// greift auf die Stellen 2,3,4 des tmp-String zu
				observation[i] = tmp[i + 2];

				// greift auf die Stellen 5,6,7 des tmp-String zu
				person[i] = tmp[i + 5];
			}
		} else if (type.equals("adress")) {
			location = new String[3];
			for (int i = 0; i < 3; i++)
				location[i] = tmp[i];
			for (int i = 0; i < tmp.length - 6; i++) {

				// greift auf die Stellen 3,4,5 des tmp-String zu
				observation[i] = tmp[i + 3];

				// greift auf die Stellen 6,7,8
				person[i] = tmp[i + 6];
			}
		}
	}

	/**
	 * Stellt eine Verbindung zur Datenbank her und ruft die Methode addDataToDB
	 * auf, um die Daten in die Datenbank zu integrieren. Gibt schlieﬂlich der
	 * aufrufenden Activity PersonActivity eine Nachricht ueber den Status der
	 * Datenuebertragung.
	 */
	@Override
	public void run() {

		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			result = false;
			error = e1.getMessage();
		}

		Connection con = null;

		try {
			// vm158.rz.uni-osnabrueck.de:5432/kappeGIS , nutzername, password
			con = DriverManager.getConnection("jdbc:postgresql://" + server
					+ ":" + port + "/" + dbname, username, password);
			result = addDataToDB(con);
		} catch (SQLException e) {
			// falls DriverManager.getConnection(...) einen Fehler wirft
			e.printStackTrace();
			result = false;
			error = e.getMessage();
		}

		finally {
			// Informiere PersonActivity ueber den Uebertragungsstatus.
			// Erstelle dazu ein Bundle, welche die benoetigten Daten
			// enthaelt und versende die Nachricht anschlieﬂend mittels
			// einem Message-Objekt.
			Bundle data = new Bundle();
			data.putBoolean("result", result);
			data.putString("error", error);
			Message msg = new Message();
			msg.setData(data);
			inst_person.myHandler.sendMessage(msg);
		}
	}

	/**
	 * Benutze die hergestelle Datenbankverbindung durch das Connection-Objekt
	 * dazu, um die Daten in die Datenbank zu schreiben. Erstelle dazu die
	 * benoetigten Tabellen und integriere die Daten an die passende Stelle. Die
	 * Zuordnung der eingefuegten Daten erfolgt ueber eine ID.
	 * 
	 * @param con
	 *            die Datenbankverbindung
	 * @return Uebertragungsstatus
	 */
	public boolean addDataToDB(Connection con) {
		Statement stmt = null;
		result = false;
		int id = 0;

		// Erstelle die Tabellen zur Speicherung der observation
		// und der persoenlichen Daten.
		try {
			stmt = con.createStatement();
			try {
				stmt.execute("CREATE TABLE observation (id SERIAL, description TEXT, "
						+ "category TEXT, urgency TEXT, " + "time TIMESTAMP)");
				stmt.execute("CREATE TABLE person (id BIGINT, name TEXT, "
						+ "adress TEXT, telnumber TEXT)");
			}

			// Falls das Erstellen der Tabellen fehlschlug
			catch (Exception e) {
				e.printStackTrace();
			}

			// Erfahre die aktuelle Zeit
			ResultSet rs = stmt.executeQuery("SELECT CURRENT_TIMESTAMP");
			rs.next();
			String time = rs.getString("now");
			Log.i("info", "time: " + time);

			// Fuege die Daten mit diesem Zeitstempel ein
			stmt.execute("INSERT INTO observation (description, "
					+ "category, urgency, time) VALUES ('" + observation[0]
					+ "','" + observation[1] + "','" + observation[2] + "','"
					+ time + "')");

			// Ermittele die eingefuegten Daten anhand ihres Zeitstempels
			// und erfrage die ID
			rs = stmt.executeQuery("SELECT id FROM observation WHERE time = '"
					+ time + "'");
			rs.next();
			id = rs.getInt("id");
			Log.i("info", String.valueOf(id));

			// Fuege die Daten zur Person mit der ermittelten ID ein
			stmt.execute("INSERT INTO person (id, name, "
					+ "adress, telnumber) VALUES (" + id + ",'" + person[0]
					+ "','" + person[1] + "','" + person[2] + "')");

			result = true;
		} catch (SQLException e) {

			// Exception erhalten, wenn ein Statement nicht erstellt oder
			// nicht ausgefuehrt werden konnte
			e.printStackTrace();
			result = false;
			error = e.getMessage();
		}

		// Bei kartenbasierter Eingabe wird eine Tabelle erstellt, die
		// zwei Koordinaten abspeichert
		if (type.equals("map")) {
			try {
				stmt = con.createStatement();

				try {
					stmt.execute("CREATE TABLE location_xy(id BIGINT, x NUMERIC(10,8), "
							+ "y NUMERIC(10,8))");
				}

				// Exception z.B. wenn die Tabelle bei Erstellung bereits unter
				// diesem
				// Namen existiert
				catch (Exception e) {
				}

				// Fuege die Werte ein
				stmt.execute("INSERT INTO location_xy (id,x,y) VALUES (" + id
						+ "," + location[0] + "," + location[1] + ")");

				result = true;
			} catch (SQLException e) {

				// Exception, wenn das Statement nicht erstellt oder
				// nicht ausgefuehrt werden konnte
				e.printStackTrace();
				result = false;
				error = e.getMessage();
			}
		}

		// Block wird durchlaufen, wenn der Nutzer den Ort als Adresse
		// eingegeben hat
		else if (type.equals("adress")) {
			try {
				stmt = con.createStatement();

				// Erstelle eine Tabelle, welche die Adresse abspeichert
				try {
					stmt.execute("CREATE TABLE location_adress(id BIGINT, adress TEXT, "
							+ "citypart TEXT, comment TEXT)");
				}
				// Exception z.B. wenn die Tabelle zum Zeitpunkt des Erstellens
				// bereits existiert
				catch (Exception e) {

				}

				// Fuege die Werte ein
				stmt.execute("INSERT INTO location_adress (id, adress, citypart, comment) VALUES ("
						+ id
						+ ",'"
						+ location[0]
						+ "','"
						+ location[1]
						+ "','"
						+ location[2] + "')");

				result = true;
			} catch (SQLException e) {

				// Exception, wenn das Statement nicht erstellt oder
				// nicht ausgefuehrt werden konnte
				e.printStackTrace();
				result = false;
				error = e.getMessage();
			}
		}

		return result;
	}
}
