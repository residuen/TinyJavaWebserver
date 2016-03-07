/*
 * Dieses Programm ist freie Software. Sie koennen es unter den Bedingungen der GNU General Public License,
 * wie von der Free Software Foundation veroeffentlicht, weitergeben und/oder modifizieren, entweder gemaess
 * Version 3 der Lizenz oder (nach Ihrer Option) jeder spaeteren Version.
 * Die Veroeffentlichung dieses Programms erfolgt in der Hoffnung, dass es Ihnen von Nutzen sein wird, aber
 * OHNE IRGENDEINE GARANTIE, sogar ohne die implizite Garantie der MARKTREIFE oder der VERWENDBARKEIT FUER
 * EINEN BESTIMMTEN ZWECK. Details finden Sie in der GNU General Public License.
 * Sie sollten ein Exemplar der GNU General Public License zusammen mit diesem Programm erhalten haben.
 * Falls nicht, siehe <http://www.gnu.org/licenses/>.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Copyright: 2016
 * 
 * @author: Karsten Blauel
 * @version 0.1 Licence: GPL 3.0 or higher file: Server.java
 */
public abstract class JavaWebserver extends Thread {

	// Wenn die Konstante true ist, werden Kontrollausgaben auf der Konsole
	// ausgeführt
	private final boolean KONSOLEN_MSG = true;

	// Für die Verbindung erforderlicher Server-Socket
	private ServerSocket serversocket = null;

	// Erhält Informationen über Host-Daten
	private InetAddress inetAdress = null;

	// Erhält den Antwort-Text für den anfragenden Client
	private OutputStreamWriter output = null;

	// Speichert die Port-Adresse
	private int port;

	// Strings fuer die Anfrage-URI des Clients
	private String clientAnfrageText = "";
	private String serverHostAdresse = null;

	// private String serverHostName = null;

	/**
	 * Der Konstruktor bekommt den zu verwendenden Port übergeben. Standardmäßig
	 * wird für HTTP Port 80 verwendet.
	 * 
	 * @param benutzer_port
	 */
	public JavaWebserver(int benutzer_port) {
		port = benutzer_port;

		try {
			inetAdress = InetAddress.getLocalHost();

			serverHostAdresse = inetAdress.getHostAddress();
			// serverHostName = inetAdress.getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		// Festlegen der Priorität
		// this.setPriority(6);
	}

	/**
	 * Erhält in der abgeleiteten Klasse den Anfragetext des Clients Beispiel:
	 * Der Client ruft folgende Seite auf:
	 * http://192.168.56.1/client?message=Der%20Java-Webserver requestText
	 * enthält dann den Text "client?message=Der%20Java-Webserver"
	 * 
	 * @param anfrageText
	 */
	abstract void anfrageErgebnis(String anfrageText);

	/**
	 * Schickt die Antwort an den Client
	 * 
	 * @param text
	 */
	public void clientAntwort(String text) {
		sendeAntwortAnClient(text);
	}

	/**
	 * In dieser Thread-Methode "läuft" der Web-Server
	 */
	public void run() {
		System.out.println("<Der Java-Webserver>");
		System.out.println("<Tippe http://" + serverHostAdresse
				+ "/client?message=Der Java-Webserver im Webbrowser ein>\n");

		try {
			// Konsolenmeldung ausgeben
			System.out.println("Versuche den lokalen Host mit Port "
					+ Integer.toString(port) + " zu verbinden ...");

			// Ein Server-Socket wird mit Port 80 Initialisiert
			serversocket = new ServerSocket(port);
		} catch (Exception e) { // Im Fehlerfall wird eine Meldung an die
								// Konsole ausgegeben
			System.out.println("Fehler:" + e.getMessage());
			return;
		}

		System.out.println("OK!\n");

		/**
		 * Einstieg in eine Endlosschleife. Hier werden - Verbindungen zu
		 * Clients aufgebaut - Anfragen von Clients entgegengenommen -
		 * Nachrichten an Clients gesendet
		 */
		while (!isInterrupted() && !serversocket.isClosed()) {

			if (KONSOLEN_MSG)
				System.out.println("Bereit, warten auf Anfragen ...\n");

			try {
				// Warten, bis jemand/ein Client eine gültige Anfrage auf dem
				// eingestellten Port sendet
				Socket connectionsocket = serversocket.accept();

				// Die IP-Adresse des Clients auslesen
				InetAddress client = connectionsocket.getInetAddress();

				// Erfolgsmeldung
				if (KONSOLEN_MSG)
					System.out.println(client.getHostName()
							+ " ist mit dem Server verbunden.\n");

				// Anfrage des Clients in den Puffer (input) einlesen
				BufferedReader input = new BufferedReader(
						new InputStreamReader(connectionsocket.getInputStream()));

				// Ausgabe-Objekt erzeugen, mit dem der Server dem Client
				// antwortet, z.B. mit HTTP-Header und HTML-Text
				output = new OutputStreamWriter(
						connectionsocket.getOutputStream());

				// Übergeben der Client-Adresse, der Eingangs- und
				// Ausgabeobjekte an den Ereignishandler
				http_EreignisHandler(client.getHostAddress(), input, output);
			} catch (Exception e) { // Meldung, wenn Fehler
				if (KONSOLEN_MSG)
					System.out.println("Fehler:" + e.getMessage() + "\n");
			}

		} // Ende der Endlosschleife, zurück zum Schleifenkopf
	}

	/**
	 * 
	 * @param clientIpAdresse
	 * @param input
	 * @param output
	 */
	private void http_EreignisHandler(String clientIpAdresse,
			BufferedReader input, OutputStreamWriter output) {

		int methode = 0; // 1 get, 2 head, 0 nicht unterstützt

		String path = new String(); // dient dem Zusammensetzen des
									// Client-Anfragetextes (HTTP, Pfad)

		try {
			String tmp = null;

			try {
				tmp = input.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			} // Auslesen des Input-Streams

			String tmp2 = new String(tmp);
			tmp.toUpperCase(); // Konvertieren in Großbuchstaben
			if (tmp.startsWith("GET")) { // Vergleiche auf GET
				methode = 1; // methode auf 1 setzen
			}
			if (tmp.startsWith("HEAD")) { // Vergleiche auf HEAD
				methode = 2; // methode auf 2 setzen
			}

			if (methode == 0) { // nicht unterstützt
				try {
					output.write(http_header_erzeugen(501, 0));
					output.close();
					return;
				} catch (Exception e3) { // Fehler anfangen und melden
					if (KONSOLEN_MSG)
						System.out.println("Fehler:" + e3.getMessage());
				}
			}

			// tmp beinhaltet "GET /index.html HTTP/1.0 ......."
			// finde 1. Leerzeichen
			// finde nächstes Leerzeichen
			// kopiere was zwischen Leerzeichen steht, das ist dann z.B. "index.html"
			int start = 0;
			int ende = 0;

			for (int a = 0; a < tmp2.length(); a++) {
				if (tmp2.charAt(a) == ' ' && start != 0) {
					ende = a;
					break;
				}

				if (tmp2.charAt(a) == ' ' && start == 0) {
					start = a;
				}
			}
			path = tmp2.substring(start + 2, ende); // den Pfad ablegen
		} catch (Exception e) {
			if (KONSOLEN_MSG) {
				System.out.println("Fehler " + e.getMessage());
				System.out.println(e.getLocalizedMessage());
			}
		} // alle Fehler abfangen und melden

		// Client-Anfrage-String abspeichern
		clientAnfrageText = new File(path).getName();

		/**
		 * Client-Anfrage-String an abstrakte Methode zur Weiterverarbeitung
		 * übergeben. Enthalten sind der angefragte Dateiname (z.B. index.html)
		 * und angehängte Parameter
		 */
		anfrageErgebnis(clientAnfrageText);

		// Client-Anfrage ausgeben
		if (KONSOLEN_MSG)
			System.out.println("Client Anfrage: requestText="
					+ clientAnfrageText + "\n");
	}

	/**
	 *  Hier wird der HTTP-Header erstellt, er wird von den Browsern benötigt
	 *  um zu erkennen, ob die Übertragung erfolgreich war oder nicht
	 * @param return_code
	 * @param datei_type
	 * @return
	 */
	private String http_header_erzeugen(int return_code, int datei_type) {

		String header = "HTTP/1.0 ";
		
		// Bekannte Fehlercodes auf Webseiten
		switch (return_code) {
		case 200:
			header = header + "200 OK";
			break;
		case 400:
			header = header + "400 Bad Request";
			break;
		case 403:
			header = header + "403 Forbidden";
			break;
		case 404:
			header = header + "404 Not Found";
			break;
		case 500:
			header = header + "500 Internal Server Error";
			break;
		case 501:
			header = header + "501 Not Implemented";
			break;
		}

		header = header + "\r\n"; // other header fields,
		header = header + "Connection: close\r\n"; // Bersistente Verbindungen können nicht bearbeitet werden
		header = header + "Server: JavaWebserver v0.1\r\n"; // Name des Servers
		header = header + "Content-Type: text/html\r\n";
		header = header + "\r\n"; // Ende des HTTP-Headers und beginn des Bodys

		// Header-String zurückgeben
		return header;
	}

	/**
	 * Der Inhalt des Übergebenen Strings wird an den Client geliefert
	 * @param text
	 */
	private void sendeAntwortAnClient(final String text) {
		
		try {

			// Die Server-Antwort an den Client
			output.write(http_header_erzeugen(200, 5));

			output.write(text + "\n");

			// Infotext fuer WebserverGui
			if (KONSOLEN_MSG)
				System.out.println("Nachricht an Client:\n"
						+ text.replaceAll(";", "\n") + "\n");

			output.close(); // output-Stream schließen
		}

		catch (Exception e) {
		}
	}
}