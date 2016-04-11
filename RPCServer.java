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
    
    class checkExpire extends TimerTask{
    	public SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");
    	public void run(){
    		Set<String> ids = Globals.hashtable.keySet();
    		// delete expired session from the hashtable
    		for(String id : ids) if(isExpired(id)) Globals.hashtable.remove(id);
    		// update server IP table
    		try{
        		JSONParser parser = new JSONParser();
        		Object obj = parser.parse(new FileReader("/home/ec2-user/ipAddrInfo.txt"));
                JSONObject jsonObject = (JSONObject) obj;
                JSONArray itemList = (JSONArray) jsonObject.get("Items");
                int length = itemList.size() ;
                for(int i=0; i<length; i++)
                {
                	JSONObject json = (JSONObject) itemList.get(i);
                	JSONArray attribute = (JSONArray) json.get("Attributes");
                	JSONObject jsonAttribute = (JSONObject) attribute.get(0);
                	String key = (String) json.get("Name");
                	String value = (String) jsonAttribute.get("Value");                	
                	Globals.ipAddressMapping.put(key, value);
                }
    		}catch (Exception e){e.printStackTrace();}
    	}

    	private boolean isExpired(String ID){
    		try{
    			Date expire = sdf.parse(Globals.hashtable.get(ID).sessionExpiredTS);
        		if(System.currentTimeMillis() > expire.getTime()) {
        			Globals.hashtable.remove(ID);
        			return true;
        		}    		
    		}catch(Exception e){
    			System.out.println(e.getMessage());
    		}    		
    		return false;
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
