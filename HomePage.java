package sessionManagement;

import java.io.*;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

@WebServlet("/home-page")
public class HomePage extends HttpServlet 
{
	private static final long serialVersionUID = -829543884406545493L;
	private String message = "";
	private String sessionID = "";
	private String cookieValue = "";
	private Integer version = 0;
	private String currentTimestamp = "";
	private String expireTimestamp = "";
	private String readServerID = "";
	private String writeServerID = "";
	private int callID = 0;	
	public SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");
	
	@SuppressWarnings("unused")
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		boolean newbie = true;
		Cookie[] cookies = request.getCookies();
		Cookie cookie = null;
		if (cookies != null) 
		{
			for(Cookie c: cookies) 
			{
				if (c.getName().equals("CS5300PROJ1SESSION")) 
				{
					newbie = false;
					cookie = c;
					break;
				}
			}
		}
		
		Date now = new Date(System.currentTimeMillis());
		currentTimestamp = sdf.format(now);
		
		if (newbie) 
		{
			System.out.println("For NEWBIE!!");
			
			// create a new session ID
			sessionID = UUID.randomUUID().toString();
			
			// calculate the cookie expire timestamp
			Date expired = new Date(System.currentTimeMillis()+5*60*1000);
			expireTimestamp = sdf.format(expired);
			
			// setup initial message
			message = "Hello User";
			
			// setup initial version
			version = 1;
			
    		RPCClient client = new RPCClient();
    		SessionValues sv = client.sessionWriteClient(sessionID + "_0", message, callID);
    		if(sv != null)
    		{
    			expireTimestamp = sv.sessionExpiredTS;
    			sessionID = sv.returnSessionID;
    			writeServerID = sv.writeServerID;
    			readServerID = "";
    		}
    		callID++;	 
    		
    		cookieValue = sessionID + "_" + sv.sessionVersion + "," + sv.locMetaData + "dummyParam";
    		
    		// create a new cookie object (timeout set to 5 minutes)	        		
    		Cookie returnVisitorCookie = new Cookie("CS5300PROJ1SESSION", cookieValue);
    		returnVisitorCookie.setDomain(Globals.cookieDomain);
    		returnVisitorCookie.setPath("/");
    		returnVisitorCookie.setMaxAge(60*5);
    		response.addCookie(returnVisitorCookie);
		} 
		else 
		{
	        if (request.getParameter("btnLogout") != null) // logout button clicked
	        {
				System.out.println("For NEWBIE!!");
				
				// create a new session ID
				sessionID = UUID.randomUUID().toString();
				
				// calculate the cookie expire timestamp
				Date expired = new Date(System.currentTimeMillis()+5*60*1000);
				expireTimestamp = sdf.format(expired);
				
				// setup initial message
				message = "Hello User";
				
				// setup initial version
				version = 1;
				
	    		RPCClient client = new RPCClient();
	    		SessionValues sv = client.sessionWriteClient(sessionID + "_0", message, callID);
	    		if(sv != null)
	    		{
	    			expireTimestamp = sv.sessionExpiredTS;
	    			sessionID = sv.returnSessionID;
	    			writeServerID = sv.writeServerID;
	    			readServerID = "";
	    		}
	    		callID++;	 
	    		
	    		cookieValue = sessionID + "_" + sv.sessionVersion + "," + sv.locMetaData + "dummyParam";
	    		
	    		// create a new cookie object (timeout set to 5 minutes)	        		
	    		Cookie returnVisitorCookie = new Cookie("CS5300PROJ1SESSION", cookieValue);
	    		returnVisitorCookie.setDomain(Globals.cookieDomain);
	    		returnVisitorCookie.setPath("/");
	    		returnVisitorCookie.setMaxAge(60*5);
	    		response.addCookie(returnVisitorCookie);
	        }
	        else if(request.getParameter("btnNetworkRead") != null)
	        {
	        	if (cookie != null)
	        	{
	        		// get the sessionID from the cookie
	        		sessionID = cookie.getValue();
	        		
	        		System.out.println("Received Cookie Value: " + sessionID);
	        		
	        		// get the locatoin metaData
	        		String[] parts = sessionID.split(",");
	        		RPCClient client = new RPCClient(parts[1]);
	        		SessionValues sv = client.sessionReadClient(parts[0], callID);
	        		sv.locMetaData = parts[1];
	        		if(sv != null)
	        		{
	        			version = sv.sessionVersion;
	        			message = sv.sessionMessage;
	        			sessionID = sv.returnSessionID;
	        			expireTimestamp = sv.sessionExpiredTS;
	        			readServerID = sv.readServerID;
	        			writeServerID = "";
	        		}
	        		callID++;
	        		
	        		cookieValue = sessionID + "_" + sv.sessionVersion + "," + sv.locMetaData;
	        		
	        		// create a new cookie object (timeout set to 5 minutes)
	        		Cookie returnVisitorCookie = new Cookie("CS5300PROJ1SESSION", cookieValue);
	        		returnVisitorCookie.setDomain(Globals.cookieDomain);
	        		returnVisitorCookie.setPath("/");
	        		returnVisitorCookie.setMaxAge(60*5);
	        		response.addCookie(returnVisitorCookie);
	        	}
	        }
	        else if (request.getParameter("btnNetworkWrite") != null)
	        {
	        	message = request.getParameter("cookieMessage");
	        	if (cookie != null && message != null) // replace the message
	        	{
	        		// get the sessionID from the cookie
	        		sessionID = cookie.getValue();
	        		
	        		System.out.println("Received Cookie Value: " + sessionID);
	        		
	        		// get the locatoin metaData
	        		String[] parts = sessionID.split(",");
	        		RPCClient client = new RPCClient(parts[1]);
	        		SessionValues sv = client.sessionWriteClient(parts[0], message, callID);
	        		if(sv != null)
	        		{
	        			version = sv.sessionVersion;
	        			message = sv.sessionMessage;
	        			sessionID = sv.returnSessionID;
	        			expireTimestamp = sv.sessionExpiredTS;
	        			writeServerID = sv.writeServerID;
	        			readServerID = "";
	        		}
	        		callID++;
	        		
	        		cookieValue = sessionID + "_" + sv.sessionVersion + "," + sv.locMetaData + "dummyParam";
	        		
	        		// create a new cookie object (timeout set to 5 minutes)	        		
	        		Cookie returnVisitorCookie = new Cookie("CS5300PROJ1SESSION", cookieValue);
	        		returnVisitorCookie.setDomain(Globals.cookieDomain);
	        		returnVisitorCookie.setPath("/");
	        		returnVisitorCookie.setMaxAge(60*5);
	        		response.addCookie(returnVisitorCookie);
	        	}
	        }
	        else // just a subsequent get request from returning user
	        {
	        	if (cookie != null)
	        	{
	        		// get the sessionID from the cookie
	        		sessionID = cookie.getValue();
	        		
	        		System.out.println("Received Cookie Value: " + sessionID);
	        		
	        		// get the locatoin metaData
	        		String[] parts = sessionID.split(",");
	        		RPCClient client = new RPCClient(parts[1]);
	        		SessionValues sv = client.sessionReadClient(parts[0], callID);
	        		sv.locMetaData = parts[1];
	        		if(sv != null)
	        		{
	        			version = sv.sessionVersion;
	        			message = sv.sessionMessage;
	        			sessionID = sv.returnSessionID;
	        			expireTimestamp = sv.sessionExpiredTS;
	        			readServerID = sv.readServerID;
	        			writeServerID = "";
	        		}
	        		callID++;
	        		
	        		cookieValue = sessionID + "_" + sv.sessionVersion + "," + sv.locMetaData;
	        		
	        		// create a new cookie object (timeout set to 5 minutes)
	        		Cookie returnVisitorCookie = new Cookie("CS5300PROJ1SESSION", cookieValue);
	        		returnVisitorCookie.setDomain(Globals.cookieDomain);
	        		returnVisitorCookie.setPath("/");
	        		returnVisitorCookie.setMaxAge(60*5);
	        		response.addCookie(returnVisitorCookie);
	        	}
	        }
		}
		// get the current reboot number
		BufferedReader rebootReader = new BufferedReader(new FileReader("/home/ec2-user/rebootNum.txt"));
		if(rebootReader != null){
			Globals.rebootNum = rebootReader.readLine();
			rebootReader.close();
		}
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		String docType = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " + "Transitional//EN\">\n";
		out.println(docType +
			"<HTML>\n" +
			"<HEAD><TITLE>CS 5300 Project 1b</TITLE></HEAD><BODY BGCOLOR=\"#FDF5E6\">\n" +
			"NetID: ah935, nl443, wl533<br><br>\n" +
			"Session: " + sessionID + "<br><br>\n" +
			"Version: " + version + "<br><br>\n" +
			"You are currently on server ID: " + Globals.currentServerID + "<br><br>\n" +
			"Current server reboot number: " + Globals.rebootNum + "<br><br>\n" +
			"Getting Read From Server ID: " + readServerID + "<br><br>\n" +
			"Sending Write To Server IDs: " + writeServerID + "<br><br>\n" +
			"Cookie Domain: " + Globals.cookieDomain + "<br><br>\n" +
			"Date: " + currentTimestamp + "\n" +
			"<H1>" + message + "</H1>\n" +
			"<form action=\"/project-1b/home-page\" method=\"post\"><input name=\"cookieMessage\" type=\"text\">&nbsp&nbsp<input name=\"btnNetworkWrite\" type=\"submit\" value=\"Network Write\"></form>\n" +
			"<form action=\"/project-1b/home-page\" method=\"get\"><input name=\"btnNetworkRead\" type=\"submit\" value=\"Network Read\"><br><br><input name=\"btnLogout\" type=\"submit\" value=\"Logout\"></form>" +
			"Cookie Value: " + cookieValue + "<br><br>\n" +
			"Expires: " + expireTimestamp + "\n" +
			"</BODY></HTML>");
		try
		{
			checkForExpiredCookies();
		}
		catch(ParseException e)
		{
			out.println(e.getMessage());
		}
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doGet(request, response);
	}
	
	// loop through the hash table and check for expired cookies
	// Note that we do not need synchronization in cleanup because expired cookies will never be modified.
	public void checkForExpiredCookies() throws ParseException
	{
		ArrayList<String> keysToDelete = new ArrayList<String>();
		Date now = new Date();
		Set<String> keys = Globals.hashtable.keySet();
		for (String key: keys)
		{
			SessionValues sv = Globals.hashtable.get(key);
			Date expire = sdf.parse(sv.sessionExpiredTS);
			if(expire.before(now)) // remove the entry
			{
				keysToDelete.add(key);
			}
		}
		for (String key: keysToDelete)
		{
			Globals.hashtable.remove(key);
		}
	}
	
}


