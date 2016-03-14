import java.text.DateFormat;
import java.util.GregorianCalendar;

public class DateServer extends JavaWebserver {

	/**
	 * Konstruktor mit Port
	 * 
	 * @param listen_port
	 */
	public DateServer(int listen_port) {
		super(listen_port);
	}

	/**
	 * Auswerten der Server-Anfrage vom Client
	 */
	@Override
	void anfrageErgebnis(String anfrageText) {
		// Konsolenantwort
		System.out.println("Ausgabe->" + anfrageText);
		
		// Kalender für Datum und Uhrzeit
		GregorianCalendar gCalendar = new GregorianCalendar();
		
		// Formatierung um Datum ausgeben
		DateFormat dFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
		DateFormat tFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);

		// Datum in String speichern
		String dateString = dFormat.format(gCalendar.getTime());
		String timeString = tFormat.format(gCalendar.getTime());

		// Erzeugen der HTML-Seite
		String htmlText = "<html>\n<body>\n";
		htmlText += "Datum="+dateString+"<br />\n";
		htmlText += "Zeit="+timeString+"<br />\n";
		htmlText += "</body>\n</html>";
		
		// Antwort an den Client schicken
		clientAntwort(htmlText); // HTML-Seite in einer Einzelkette
	}

	/**
	 * Der Java-Webserver 
	 * @param args
	 */
	public static void main(String[] args) {

		DateServer server = new DateServer(80);

		server.start();
	}
}
