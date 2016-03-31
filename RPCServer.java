package sessionManagement;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class RPCServer implements ServletContextListener 
{
   private RPCServerThread myThread = null;

    public void contextInitialized(ServletContextEvent sce)
    {
        if ((myThread == null) || (!myThread.isAlive())) 
        {
            myThread = new RPCServerThread();
            myThread.start();
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