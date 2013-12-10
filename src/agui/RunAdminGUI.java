package agui;

import auth.*;

public class RunAdminGUI {
	private static final String serverAddress = "localhost";
	private static final int PORT = 1234;
	public static AuthClient myclient;
	public static Session mysession;

	public static void main(String[] args) {

		myclient = new AuthClient(serverAddress, PORT);
		mysession = new Session();

//		QResults a = new QResults(myclient,mysession);
//		a.open();
//		
		Alogin login = new Alogin(myclient,mysession);
		login.open();
		if (mysession.isAuthorized()) {
			AHome home = new AHome(myclient,mysession);
			home.open();
		}
	}
}
