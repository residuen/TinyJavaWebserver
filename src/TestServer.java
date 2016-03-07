
public class TestServer extends JavaWebserver {

	/**
	 * Konstruktor mit Port
	 * @param listen_port
	 */
	public TestServer(int listen_port)
	{	
		super(listen_port);
	}

	/**
	 * Auswerten der Server-Anfrage vom Klient
	 */
	@Override
	void anfrageErgebnis(String anfrageText)
	{
		// Konsolenantwort
		System.out.println("Ausgabe->"+anfrageText);
		
		// Antwort an den Client schicken
		clientAntwort("<html><body>"+anfrageText+"</body></html>");	// HTML-Seite in einer Einzelkette
	}

	/**
	 * Der Java-Webserver
	 * Teste mich mit dem Aufruf http://localhost/client?message=Der Java-Webserver
	 * @param args
	 */
	public static void main(String[] args) {

		TestServer server = new TestServer(80);
		
		server.start();
	}
}
