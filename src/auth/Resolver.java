/**
 * 
 */
package auth;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import com.mysql.jdbc.*;




import cfg.PrivateSettings.TicketSettings;
import cfg.PrivateSettings.SQLDatabaseSettings;
import blackdoor.auth.AuthTicket;
import blackdoor.util.SHE;
import util.Hash;
import util.Misc;
import util.Watch;

/**
 * @author kAG0
 *
 */
public class Resolver {
	private Connection connection = null;
	private String serverAddress = SQLDatabaseSettings.serverAddress;//"localhost";
	private final int PORT = SQLDatabaseSettings.PORT;// = 3306;
	private final String DATABASE = SQLDatabaseSettings.DATABASE;// = "Portunes";
	private final String USERNAME = SQLDatabaseSettings.USERNAME;// = "portunes";
	private final String PASSWORD = SQLDatabaseSettings.PASSWORD;// = "drowssap";
	/**
	 * notes:
	 * working with IPv4 addresses in mySQL:
	 * 	store ip's as unsigned 4 byte int's
	 * 	use the mySQL built in functions INET_ATON() and INET_NTOA() in your queries
	 * 	ATON converts things from the form '192.168.1.1' to an integer
	 * 	NTOA converts from an integer to the above form.
	 *
	 * checking user validity is probably redundant as long as you check Request.admin
	 * because if !admin, username and userPW would have to be valid to get the request
	 * resolver.
	 * 
	 * lots of things should be changed to prepared statements
	 */
	/**
	 * 
	 */
	public Resolver() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	public Resolver(String dbServer) {
		serverAddress = dbServer;
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	private Connection getConnection(String sqlUserName, String sqlUserPassword) throws SQLException{
		return DriverManager.getConnection("jdbc:mysql://" + serverAddress + ":" + PORT + "/" + DATABASE, sqlUserName, sqlUserPassword);
	}
	
	public void connect() throws SQLException{
		if(connection == null)
			connection = getConnection(USERNAME, PASSWORD);
	}
	public void disconnect() throws SQLException{
		connection.close();
		connection = null;
	}
	
	/**
	 * 
	 * @param userName
	 * @return the salt of userName, if userName DNE then return null
	 * @throws IOException 
	 */
	public byte[] getUserSalt(String userName) throws UserNotFoundException, IOException{
		//Connection connection;
		//System.out.println("getting salt");
		try {
			if(connection == null)
				connect();//connection = getConnection(USERNAME, PASSWORD);
			String query = "SELECT salt FROM User WHERE userName = '" + userName + "';";
			Statement stmt = (Statement) connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if(rs.next())
			{
				byte[] salt = rs.getBytes("salt");
				//System.out.println(Misc.getHexBytes(salt, ""));
				return salt;
			}else
				throw new UserNotFoundException(userName);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IOException("Problem getting user salt from DB.");
		}
	}
	
	public byte[] getUserHash(String userName) throws UserNotFoundException, IOException{
		//Connection connection;
		//System.out.println("getting hash");
		try {
			if(connection == null)
				connect();//connection = getConnection(USERNAME, PASSWORD);
			String query = "SELECT password FROM User WHERE userName = '" + userName + "';";
			Statement stmt = connection.createStatement();
			//System.out.println(query);
			ResultSet rs = stmt.executeQuery(query);
			if(rs.next())
			{
				return rs.getBytes("password");
			}else
				throw new UserNotFoundException(userName);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IOException("Problem getting user hash from DB.");
		}
	}
	/**
	 * parses request and returns a Request that is the same as request but with
	 * the reply data member appropriately filled.
	 * @param request the request to handle
	 * @return same as request but with the reply data member appropriately filled.
	 */
	public Request resolve(Request request) throws UserNotFoundException{
		//connection = getConnection(user, pass);
		//Statement stmt = connection.createStatement();
		/*
		 * maybe do the following only in the cases where it's needed
		 if(request.admin){
			if(!isValidUser(request.adminName, request.getAuthUserPW()))
				//return null or something
		}*/
		switch(request.operation){
		// In each switch statement make query = to something different depending on what we want to query
			case TICKET:
				request.setReply(getTicket(request.username, request.userPW, request.origin));
				break;
			case ADD: 
				if(isAdmin(request.adminName)){
					request.setReply(add((ADD) request));
				}else
					request.setReply(false);
				break;
			case REMOVE:
				if(request.admin && isValidAdmin(request.username, request.adminName, request.adminPW))
					request.setReply(removeUser((REMOVE) request));
				else request.setReply(removeUser((REMOVE) request));
				break;
			case CHECK:
				request.setReply(isValidUser(request.username, request.userPW));
				CHECK reply = (CHECK) request;
				if(!request.admin && reply.reply)
					recordLogin(request.origin, request.username);
				break;
			case CHANGENAME:
				if(!request.admin){
					if(isValidUser(request.username, request.userPW)){
						request.setReply(changeName((CHANGENAME) request));
					}
				}
				else if	(isValidAdmin(request.username, request.adminName, request.adminPW)){
					request.setReply(changeName((CHANGENAME) request));
				}
				break;
			case CHANGEPASSWORD://TODO
				if(!request.admin){
					if(isValidUser(request.username, request.userPW)){
						request.setReply(changePass((CHANGEPASS) request));
					}
				}
				else if	(isValidAdmin(request.username, request.adminName, request.adminPW)){
					request.setReply(changePass((CHANGEPASS) request));
				}
				break;
			case GETINFO:
				if(request.admin && isValidAdmin(request.username, request.adminName, request.adminPW)){
					request.setReply(getInfo((GETINFO) request));
				}else request.setReply(getInfo((GETINFO) request));
				break;
			case SETADMIN:
				SETADMIN set = (SETADMIN) request;
				if(!isValidAdmin(request.username, request.adminName, request.adminPW) || 
						!isValidUser(set.newAdminName, set.newAdminPassword))
					break;
				// get the newAdminName
				// SQL INSERT newAdminName and yeah
				request.setReply(makeAdmin(set.newAdminName, request.username)); // true if the newAdminName has been made an administrator of userName
				break;
			case LIST:
				request.setReply(listUsers((LIST) request));
				break;
			case HISTORY:
				if(request.admin && isValidAdmin(request.username, request.adminName, request.adminPW))
					request.setReply(getHistory((HISTORY) request));
				else
					request.setReply(getHistory((HISTORY) request));
				break;
			case CHECKADMIN:
				if(isValidUser(request.username, request.userPW) && isAdmin(request.username)){
					request.setReply(true);
					recordLogin(request.origin, request.username);
				}
				else request.setReply(false);
				break;
				
		}
		try {
			disconnect();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return request;
	}
	
	private boolean makeAdmin(String adminName, String userName){
		//		"INSERT INTO Admin values(" + adminName + "," + userName + ");"
		
		try {
			connect();
			Statement stmt = connection.createStatement();
			//"INSERT INTO `Portunes`.`Admin` (`adminName`, `userName`) VALUES ('"+ adminName +"', '" + userName + "');"

			stmt.executeUpdate("INSERT INTO Admin (`adminName`, `userName`) values('" + adminName + "','" + userName + "');");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	//TODO sanitize name input to prevent injection
	private boolean add(ADD request){
		if(request.username.matches("^.*[^a-zA-Z0-9 ].*$"))
			return false;
		byte[] salt = new byte[AuthServer.saltLength];
		new SecureRandom().nextBytes(salt);
		String query_create = "INSERT INTO User values('" + request.username + "','" + request.name +
				"', 0x" + Misc.getHexBytes(Hash.getStretchedSHA256(request.userPW, salt, AuthServer.stretchLength), "") + 
				", 0x" + Misc.getHexBytes(salt, "") + ");";
		String query_history = "INSERT INTO History(length, lastLoginIndex, userName) values(" +
				"6"/*<<history length*/ + ", 0, '" + request.username + "');";
		
		try {
			if(connection == null)
				connect();
			connection.setAutoCommit(false);

			Statement stmt_create = connection.createStatement();
			Statement stmt_history = connection.createStatement();
			
			//System.out.println(query_create + "\n" +query_history);
			stmt_create.executeUpdate(query_create);
			stmt_history.executeUpdate(query_history);
			
			if(!makeAdmin(request.adminName, request.username))
				throw new SQLException();
			connection.commit();
			recordLogin(request.origin, request.username);
		} catch (SQLException e) {
			e.printStackTrace();
			if(connection != null){
				try {
					connection.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			return false;
		}finally{
			try {
				if(connection != null)
					connection.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return true;
//		"INSERT INTO User values(" + userName + "," + name +
//				", 0x" + Misc.getHexBytes(stretchedPassword, "") + 
//				", 0x" + Misc.getHexBytes(salt, "") + ");"
		
//		"INSERT INTO History(length, lastLoginIndex, userName) values(" +
//				historyLength + ", 0, " + userName + ");"
	}
	
	private boolean removeUser(REMOVE request){
		String query = "DELETE FROM User "+
				"WHERE User.userName = '" + request.username + "';";
		//System.out.println(query);
		try {
			if(connection == null)
				connect();

			Statement stmt = connection.createStatement();
			
			stmt.executeUpdate(query);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		//return true;
		//get the info on what to delete
		//delete: DELETE FROM ___ WHERE user = what we want to delete
	}
	//TODO sanitize name input to prevent injection
	private boolean changeName(CHANGENAME request){
		//get the name we have to change
		//UPDATE tablename SET name = "newname" WHERE name = "oldname" AND password ="password" AND so on...
		String query = "UPDATE User SET name = '" + request.name + "' WHERE userName = '" +request.username+ "';";
		try {
			if (connection == null)
				connect();
			Statement stmt = connection.createStatement();
			stmt.executeUpdate(query);
		} catch (SQLException e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean changePass(CHANGEPASS request){
		// get the password we have to change
		// UPDATE tablename SET password = "newpassword" WHERE name = "name" AND password ="oldpassword" AND so on... 
		try {
			byte[] salt = new byte[AuthServer.saltLength];
			new SecureRandom().nextBytes(salt);
			String query = "UPDATE USER SET password = 0x" + Hash.getStretchedSHA256(request.userPW, salt, AuthServer.stretchLength) + ", salt = 0x" + Misc.bytesToHex(salt) + " WHERE userName = '" + request.username +"';";
			if (connection == null)
				connect();
			Statement stmt = connection.createStatement();
			stmt.executeUpdate(query);
		} catch (SQLException e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	private Map<String, Object> getInfo(GETINFO request){
		// SELECT * FROM table WHERE user ="username" AND etc.
		Map<String, Object> reply = new HashMap<String, Object>();
		String query = "SELECT h.hid, u.name, l.month,l.day,l.year,l.hours,l.minutes, INET_NTOA(l.ip) as ip " +
				"FROM History h JOIN LogIn l USING(hid) JOIN User u USING(userName)" +
				"WHERE h.userName = '"+request.username+"' AND l.index = ((h.lastLoginIndex - "+ request.time+") MOD h.length) ;"; // the history on the user with a specific username
		int hid, month, day, year, hours, minutes;
		InetAddress ip = null;
		try {
			if(connection == null)
				connect();
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if(rs.next()){
				hid = rs.getInt("hid");
				try {
					ip = InetAddress.getByName(rs.getString("ip"));
					//System.out.println(rs.getString("ip"));
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				month = rs.getInt("month");
				day = rs.getInt("day");
				year = rs.getInt("year");
				hours = rs.getInt("hours");
				minutes = rs.getInt("minutes");
				reply.put("userName", request.username);
				reply.put("name", rs.getString("name"));
				reply.put("hid", hid);
				reply.put("ip", ip);
				reply.put("month", month);
				reply.put("day", day);
				reply.put("year", year);
				reply.put("hours", hours);
				reply.put("minutes",minutes);
			}
			return reply;
			
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	private List<Map<String, Object>> getHistory(HISTORY request){
		// while rs.next(){getInfo(request + 1 time)
		// SELECT allPreviousLogins FROM table WHERE user = "username"
		List<Map<String, Object>> reply = new ArrayList<Map<String, Object>>();
		Map<String, Object> replyM = null;
		String query = "SELECT * FROM History h JOIN LogIn l USING(hid) WHERE h.userName = '"+request.username+"';"; // the history on the user with a specific username
		int hid, month, day, year, hours, minutes;
		InetAddress ip = null;
		try {
			if(connection == null)
				connect();
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()){
				replyM = new HashMap<String, Object>();
				hid = rs.getInt("hid");
				try {
					ip = InetAddress.getByName(rs.getString("ip"));
					//System.out.println(rs.getString("ip"));
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				month = rs.getInt("month");
				day = rs.getInt("day");
				year = rs.getInt("year");
				hours = rs.getInt("hours");
				minutes = rs.getInt("minutes");
				replyM.put("hid", hid);
				replyM.put("ip", ip);
				replyM.put("month", month);
				replyM.put("day", day);
				replyM.put("year", year);
				replyM.put("hours", hours);
				replyM.put("minutes",minutes);
				replyM.put("userName", request.username);
				reply.add(replyM);
			}
			return reply;
			
		}catch (SQLException e) {
			e.printStackTrace();
		}		
		return null;
	}
	
	private List<Map<String, Object>> listUsers(LIST request){
		// ASSUME ITS ADMIN get admin name and pword
		// SELECT allPreviousLogins FROM table WHERE adminname = "admin name" AND etc.
		List<Map<String, Object>> reply = new ArrayList<Map<String, Object>>();
		String query = "SELECT h.userName, u.name, l.hid, INET_NTOA(l.ip) as ip, l.month, l.day, l.year, l.hours, l.minutes" // reutrn back what we need
				+ " FROM ((History h JOIN LogIn l USING (hid)) JOIN User u USING (userName)) JOIN Admin a USING (userName) " // join all tables
				+ " WHERE adminName = '"+ request.adminName +"' AND l.index = (h.lastLoginIndex MOD h.length);"; // admin = the user of the request
		String userName = "";
		int hid, month, day, year, hours, minutes, i;
		InetAddress ip = null;
		i=0;
		try {
			if(connection == null)
				connect();
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			Map<String, Object> map;
			while(rs.next()){
				map = new HashMap<String, Object>();
				userName = rs.getString("userName");
				hid = rs.getInt("hid");
				try {
					ip = InetAddress.getByName(rs.getString("ip"));
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				userName = rs.getString("userName");
				month = rs.getInt("month");
				day = rs.getInt("day");
				year = rs.getInt("year");
				hours = rs.getInt("hours");
				minutes = rs.getInt("minutes");
				map.put("userName", userName);
				map.put("name", rs.getString("name"));
				map.put("hid", hid);
				map.put("ip", ip);
				map.put("month", month);
				map.put("day", day);
				map.put("year", year);
				map.put("hours", hours);
				map.put("minutes",minutes);
				reply.add(map);
				//i++;
			}
			return reply;
			
		}catch (SQLException e) {
			e.printStackTrace();
		}		
		return null;			
	}

	//TODO make this work (updating logIn instead of inserting. maybe add entire empty login history at user creation
	public boolean recordLogin(InetAddress origin, String userName) {
		boolean ret = false;
		Watch time = new Watch();
		String incQuery = "UPDATE History SET lastLoginIndex = lastLoginIndex + 1 MOD length WHERE userName = '"+userName+"';";
		String delQuery = "DELETE LogIn FROM LogIn JOIN History ON(LogIn.hid = History.hid) WHERE LogIn.index = ((History.lastLoginIndex + 1) MOD length) AND History.userName = '" + userName + "';";
		String query = "INSERT INTO LogIn(hid, ip, month, day, year, `index`, hours, minutes)" +
				" SELECT hid, INET_ATON('" + origin.getHostAddress() + "'), " 
					+ time.getMonth() + ", " + time.getDate() + ", " + time.getYear() + 
					", lastLoginIndex MOD length, " + time.getHours() + ", " + time.getMinutes() + " " +
				"FROM History " +
				"WHERE userName = '" + userName + "';";
		//System.out.println(query);
		try {
			connect();
			Statement stmt = connection.createStatement();
			stmt.executeUpdate(delQuery);
			connection.setAutoCommit(false);
			stmt.executeUpdate(incQuery);
			stmt.executeUpdate(query);
			connection.commit();
			ret = true;
		} catch (SQLException e) {
			e.printStackTrace();
			ret = false;
			try {
				if(connection != null)
					connection.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}finally{
			if(connection != null)
				try {
					connection.setAutoCommit(true);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return ret;
	}
	
	private byte[] getTicket(String userName, byte[] password, InetAddress origin){
		if(isValidUser(userName, password)){
			AuthTicket ticket = new AuthTicket(userName, TicketSettings.SESSON_DURATION, origin, (byte) 0);
			return ticket.generate(TicketSettings.TICKET_KEY);
		}else{
			byte[] fake = new byte[(int) (SHE.BLOCKSIZE +  Math.ceil((float)(16+userName.length())/SHE.BLOCKSIZE)*SHE.BLOCKSIZE)];
			new SecureRandom().nextBytes(fake);
			return fake;
		}
	}
	
	private boolean isValidUser(String userName2Check, byte[] password){
		String queryValidUser = "SELECT * FROM User WHERE userName = '"+ userName2Check+"';";

		try {
			if(connection == null)
				connect();
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(queryValidUser);
			if(rs.next()){
				byte[] storedPW = rs.getBytes("password"); // get password for userName from db
				byte[] salt = rs.getBytes("salt");
				byte[] computedPW = Hash.getStretchedSHA256(password, salt, AuthServer.stretchLength);
				//System.out.println(DatatypeConverter.printHexBinary(password) +"\nserver " + DatatypeConverter.printHexBinary(storedPW) + "\n" +
				//		"compu " + DatatypeConverter.printHexBinary(computedPW) + "\n" +
				//		DatatypeConverter.printHexBinary(password) + " " + DatatypeConverter.printHexBinary(salt));
				return Arrays.equals(computedPW, storedPW);
			}
			//else
				//return false;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;

	}
	
	
	private boolean isAdmin(String adminName){
		String query = "SELECT * " +
				"FROM Admin JOIN User ON adminName = User.userName " +
				"WHERE adminName = '" + adminName + "' ";
		try {
			connect();
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if(rs.next()){
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	//not efficient, needs optimization up the wazoo
	private boolean isValidAdmin(String userName, String adminName, byte[] adminPW){
		if(!isAdmin(adminName) || !isValidUser(adminName, adminPW))
			return false; //this check saves time if adminName isnt' an admin 
							//or user, but only adds a couple (to the n ;( ) queries if they are
		String q1 = "SELECT adminName FROM Admin WHERE userName = '" + userName +"';";
		try {
			return isAdminOf(adminName, userName);
//			connect();
//			Statement stmt1 = connection.createStatement();
//			ResultSet rs1 = stmt1.executeQuery(q1);
//			while(rs1.next()){
//				if(rs1.getString("adminName").equalsIgnoreCase(adminName))
//					return true;
//			}
//			rs1.beforeFirst();
//			while(rs1.next()){
//				if(isAdminOf(adminName, rs1.getString("adminName")))
//					return true;
//			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
			//if adminname is and admin of a user who is an admin of userName return true
			// potential exemplary query here
	}
	private boolean isAdminOf(String adminName, String userName) throws SQLException{
		String q1 = "SELECT adminName FROM Admin WHERE userName = '" + userName +"';";
		connect();
		Statement stmt1 = connection.createStatement();
		ResultSet rs1 = stmt1.executeQuery(q1);
		while(rs1.next()){
			if(rs1.getString("adminName").equalsIgnoreCase(adminName))
				return true;
		}
		rs1.beforeFirst();
		while(rs1.next()){
			if(isAdminOf(adminName, rs1.getString("adminName")))
				return true;
		}
		return false;
	}
//	private boolean isValidAdmin(String userName, String adminName, byte[] adminPW){
//		String query1 = "SELECT userName FROM User WHERE userName = '" +userName+"';";// query that gets username of a specific user
//		String query2 = "SELECT adminName FROM Admin WHERE adminName = '" +adminName+ "' AND userName = '"+ userName+"' ;";// query that gets the admin name of a specific user
//		String userName2Check = "";
//		String adminName2Check = "";
//		byte[] storedPW = null;
//		byte[] salt = null;
//		try{
//			connect();
//			Statement stmt = connection.createStatement();
//			ResultSet rs = stmt.executeQuery(query1);
//			Statement stmt2 = connection.createStatement();
//			ResultSet rs2 = stmt2.executeQuery(query2);// gets the admin name if it exists from the query
//			
//			if(rs.next())
//				userName2Check = rs.getString("userName");
//			
//			if( userName.equals(userName2Check)/* userName is in db */ && isValidUser(adminName, adminPW) ){
//				if(rs2.next())
//					adminName2Check = rs2.getString("adminName");
//				if(adminName.equals(adminName2Check) /* adminName is set as an admin of userName in db */)
//					storedPW = rs2.getBytes("password"); // get password for userName from db
//					salt = rs2.getBytes("salt");
//					return Arrays.equals(Hash.getStretchedSHA256(adminPW, salt, AuthServer.stretchLength), storedPW);
//			}
//			else
//				return false;
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return false;
//			//if adminname is and admin of a user who is an admin of userName return true
//			// potential exemplary query here
//	}
	
	/**
	 * note: this exception should only be thrown as part of the authentication process
	 * when handling requests like CHECK the response field of the request should just be null
	 * @author kAG0
	 *
	 */
	public static class UserNotFoundException extends Exception{
		private String username;
		UserNotFoundException(String userName){
			super(userName +" not found in database.");
			username = userName;
		}		
		public String getUserName(){
			return username;
		}
	}
}
