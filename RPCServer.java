package sessionManagement;

import java.io.FileReader;
import java.io.PrintWriter;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class RPCServer implements ServletContextListener 
{
   private RPCServerThread myThread = null;
   private final int checkInterval = 5; 
    public void contextInitialized(ServletContextEvent sce)
    {
        if ((myThread == null) || (!myThread.isAlive())) 
        {
            myThread = new RPCServerThread();
            myThread.start();
        }
        
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new checkExpire(), 0, this.checkInterval*1000);
    }
    
    class checkExpire extends TimerTask
    {
    	public SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");
    	
    	public void run()
    	{
    		try
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
    		catch (Exception e)
    		{
    			e.printStackTrace();
    		}
    	}
    }

    public void contextDestroyed(ServletContextEvent sce)
    {
        try 
        {
            myThread.doShutdown();
            myThread.interrupt();
        }
        catch (Exception ex)
        {
        	ex.printStackTrace();
        }
    }
}
