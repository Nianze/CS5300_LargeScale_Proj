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
	private Integer version = 0;
	private String currentTimestamp = "";
	private String expireTimestamp = "";
	private int callID = 0;	
	public SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");
	
	@SuppressWarnings("unused")
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		boolean newbie = true;
		boolean regularPage = true;
		Cookie[] cookies = request.getCookies();
		Cookie cookie = null;
		if (cookies != null) 
		{
			for(Cookie c: cookies) 
			{
				if (c.getName().equals("CS5300PROJ1SESSION")) 
				{
					// check this user has not logged out
					if(Globals.hashtable.containsKey(c.getValue()))
					{
						newbie = false;
						cookie = c;
						break;
					}
					else
					{
						break;
					}
				}
			}
		}
		
		if (newbie) 
		{
			// create a new session ID
			sessionID = UUID.randomUUID().toString();
			
			// get current timestamp and calculate the cookie expire timestamp
			Date now = new Date(System.currentTimeMillis());
			Date expired = new Date(System.currentTimeMillis()+5*60*1000);
			currentTimestamp = sdf.format(now);
			expireTimestamp = sdf.format(expired);
			
			// setup initial message
			message = "Hello User";
			
			// setup initial version
			version = 0;
			
			// create a SessionValues Object and store it in to the hashmap
			SessionValues sessionValue = new SessionValues(0, message, expireTimestamp);
			Globals.hashtable.put(sessionID, sessionValue);
			
			// create a new cookie object (timeout set to 5 minutes)
			Cookie returnVisitorCookie = new Cookie("CS5300PROJ1SESSION", sessionID);
			returnVisitorCookie.setMaxAge(60*5);
			response.addCookie(returnVisitorCookie);
		} 
		else 
		{
	        if (request.getParameter("btnRefresh") != null) // refresh button clicked
	        {
	        	if(cookie != null)
	        	{
	        		// setup response timestamps and extend the existing timestamp
	    			Date now = new Date(System.currentTimeMillis());
	    			Date expired = new Date(System.currentTimeMillis()+5*60*1000);
	    			currentTimestamp = sdf.format(now);
	    			expireTimestamp = sdf.format(expired);
	        		
	        		// get the sessionID from the cookie
	        		sessionID = cookie.getValue();
	        		
	        		// retrieve the SessionValue object associated with the sessionID and update the values
	        		SessionValues sessionValue = null;
	        		synchronized(Globals.hashtable)
	        		{
	        			sessionValue = Globals.hashtable.get(sessionID);
	        			sessionValue.sessionVersion += 1;
	        			sessionValue.sessionExpiredTS = expireTimestamp;
	        			Globals.hashtable.put(sessionID, sessionValue);
	        		}
					
	        		if (sessionValue != null)
	        		{
	        			// setup response version number
	        			version = sessionValue.sessionVersion;
				
	        			// setup response message
	        			message = sessionValue.sessionMessage;
	        		}
	        		else // error handling in case of null pointer
	        		{
	        			version = 0;
	        			message = "Hello User";
	        		}
					
					// create a new cookie object (timeout set to 5 minutes)
					Cookie returnVisitorCookie = new Cookie("CS5300PROJ1SESSION", sessionID);
					returnVisitorCookie.setMaxAge(60*5);
					response.addCookie(returnVisitorCookie);
	        	}
	        }
	        else if (request.getParameter("btnLogout") != null) // logout button clicked
	        {
	        	if(cookie != null)
	        	{
	        		// get the sessionID from the cookie
	        		sessionID = cookie.getValue();
	        		
	        		// delete the Session Value Object associated with this sessionID
	        		Globals.hashtable.remove(sessionID);
	        		
	        		// set the return page flag to false to return a special logout page
	        		regularPage = false;
	        	}
	        }
	        else if (request.getParameter("btnReplace") != null) // replace button clicked
	        {
	        	message = request.getParameter("cookieMessage");
	        	if (cookie != null && message != null) // replace the message
	        	{
	        		// setup response timestamps and extend the existing timestamp
	    			Date now = new Date(System.currentTimeMillis());
	    			Date expired = new Date(System.currentTimeMillis()+5*60*1000);
	    			currentTimestamp = sdf.format(now);
	    			expireTimestamp = sdf.format(expired);
	        		
	        		sessionID = cookie.getValue();
	        		SessionValues sessionValue = null;
	        		synchronized(Globals.hashtable)
	        		{
	        			sessionValue = Globals.hashtable.get(sessionID);
		        		sessionValue.sessionMessage = message;
						sessionValue.sessionVersion += 1;
						sessionValue.sessionExpiredTS = expireTimestamp;
						Globals.hashtable.put(sessionID, sessionValue);
	        		}
	        		
	        		if (sessionValue != null)
	        		{
	        			// setup response version number
	        			version = sessionValue.sessionVersion;
				
	        			// setup response message
	        			message = sessionValue.sessionMessage;
	        		}
	        		else // error handling in case of null pointer
	        		{
	        			version = 0;
	        			message = "Hello User";
	        		}
					
					// create a new cookie object (timeout set to 5 minutes)
					Cookie returnVisitorCookie = new Cookie("CS5300PROJ1SESSION", sessionID);
					returnVisitorCookie.setMaxAge(60*5);
					response.addCookie(returnVisitorCookie);
	        	}
	        }
	        else if(request.getParameter("btnNetworkRead") != null)
	        {
	        	if (cookie != null)
	        	{
	        		// get the sessionID from the cookie
	        		sessionID = cookie.getValue();
	        		// get the locatoin metaData
	        		String[] parts = sessionID.split("_+_");
	        		RPCClient client = new RPCClient(parts[1]);
	        		SessionValues sv = client.sessionReadClient(sessionID, callID); // version add 1 inside sessionReadClient()
	        		if(sv != null)
	        		{
	        			version = sv.sessionVersion;
	        			message = sv.sessionMessage + " Network Version!!";
	        			expireTimestamp = sv.sessionExpiredTS;
	        		}
	        		callID++;
	        		
	        		// create a new cookie object (timeout set to 5 minutes)
	        		Cookie returnVisitorCookie = new Cookie("CS5300PROJ1SESSION", sessionID+"_"+sv.sessionVersion+"_+_"+sv.locMetaData);
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
	        		// get the locatoin metaData
	        		String[] parts = sessionID.split("_+_");
	        		RPCClient client = new RPCClient(parts[1]);
	        		SessionValues sv = client.sessionWriteClient(sessionID, message, callID);
	        		if(sv != null)
	        		{
	        			version = sv.sessionVersion;
	        			message = sv.sessionMessage + " Network Version!!";
	        			expireTimestamp = sv.sessionExpiredTS;
	        		}
	        		callID++;	 
	        		
	        		// create a new cookie object (timeout set to 5 minutes)	        		
	        		Cookie returnVisitorCookie = new Cookie("CS5300PROJ1SESSION", sessionID+"_"+sv.sessionVersion+"_+_"+sv.locMetaData);
	        		returnVisitorCookie.setMaxAge(60*5);
	        		response.addCookie(returnVisitorCookie);
	        	}
	        }
	        else // just a subsequent get request from returning user
	        {
	        	if (cookie != null)
	        	{
	        		// setup response timestamps and extend the existing timestamp
	    			Date now = new Date(System.currentTimeMillis());
	    			Date expired = new Date(System.currentTimeMillis()+5*60*1000);
	    			currentTimestamp = sdf.format(now);
	    			expireTimestamp = sdf.format(expired);
	        		
	        		// get the sessionID from the cookie
	        		sessionID = cookie.getValue();
	        		
	        		// retrieve the SessionValue object associated with the sessionID and update the values
	        		SessionValues sessionValue = null;
	        		synchronized(Globals.hashtable)
	        		{
	        			sessionValue = Globals.hashtable.get(sessionID);
	        			sessionValue.sessionVersion += 1;
	        			sessionValue.sessionExpiredTS = expireTimestamp;
	        			Globals.hashtable.put(sessionID, sessionValue);
	        		}
					
	        		if (sessionValue != null)
	        		{
	        			// setup response version number
	        			version = sessionValue.sessionVersion;
				
	        			// setup response message
	        			message = sessionValue.sessionMessage;
	        		}
	        		else // error handling in case of null pointer
	        		{
	        			version = 0;
	        			message = "Hello User";
	        		}
					
					// create a new cookie object (timeout set to 5 minutes)
					Cookie returnVisitorCookie = new Cookie("CS5300PROJ1SESSION", sessionID);
					returnVisitorCookie.setMaxAge(60*5);
					response.addCookie(returnVisitorCookie);
	        	}
	        }
		}
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		String docType = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " + "Transitional//EN\">\n";
		if(regularPage) // return a regular page
		{
			out.println(docType +
					"<HTML>\n" +
					"<HEAD><TITLE>CS 5300 Project 1a</TITLE></HEAD><BODY BGCOLOR=\"#FDF5E6\">\n" +
					"NetID: ah935<br><br>\n" +
					"Session: " + sessionID + "<br><br>\n" +
					"Version: " + version + "<br><br>\n" +
					"Date: " + currentTimestamp + "\n" +
					"<H1>" + message + "</H1>\n" +
					"<form action=\"/Project_1b/home-page\" method=\"post\"><input name=\"cookieMessage\" type=\"text\">&nbsp&nbsp<input name=\"btnReplace\" type=\"submit\" value=\"Replace\"></form>\n" +
					"<form action=\"/Project_1b/home-page\" method=\"post\"><input name=\"cookieMessage\" type=\"text\">&nbsp&nbsp<input name=\"btnNetworkWrite\" type=\"submit\" value=\"Network Write\"></form>\n" +
					"<form action=\"/Project_1b/home-page\" method=\"get\"><input name=\"btnRefresh\" type=\"submit\" value=\"Refresh\"><br><br><input name=\"btnLogout\" type=\"submit\" value=\"Logout\"><br><br><input name=\"btnNetworkRead\" type=\"submit\" value=\"Network Read\"></form>" +
					"Cookie: " + sessionID + "<br><br>\n" +
					"Expires: " + expireTimestamp + "\n" +
					"</BODY></HTML>");
		}
		else // return the logout page
		{
			out.println(docType +
					"<HTML>\n" +
					"<HEAD><TITLE>CS 5300 Project 1a</TITLE></HEAD><BODY BGCOLOR=\"#FDF5E6\">\n" +
					"<H1>You have logged out. Thank you for using this website!</H1>\n" +
					"<form action=\"/Project_1b/home-page\" method=\"get\"><input name=\"btnReload\" type=\"submit\" value=\"Reload the Website\"></form>" +
					"</BODY></HTML>");
		}
		
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


