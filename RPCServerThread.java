package sessionManagement;

import java.io.FileReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class RPCServerThread extends Thread
{
	private final int portProj1bRPC = 5300;
	public SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");
	
    @SuppressWarnings("unused")
	@Override
    public void run() 
    {
    	try 
    	{
    		JSONParser parser = new JSONParser();
    		Object obj = parser.parse(new FileReader("/home/ec2-user/ipAddrInfo.txt"));
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray itemList = (JSONArray) jsonObject.get("Items");
            int length = itemList.size() ;
            for(int i=0; i<length; i++)
            {
            	JSONObject json = (JSONObject)itemList.get(i);
            	JSONArray attribute = (JSONArray)json.get("Attributes");
            	JSONObject jsonAttribute = (JSONObject)attribute.get(0);
            	String key = (String)json.get("Name");
            	String value = (String)jsonAttribute.get("Value");
            	Globals.ipAddressMapping.put(key, value);
            }
                        
			DatagramSocket rpcSocket = new DatagramSocket(portProj1bRPC);
			while(true)
			{
				byte[] inBuf = new byte[256];
				DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
				rpcSocket.receive(recvPkt);
				InetAddress returnAddr = recvPkt.getAddress();
				int returnPort = recvPkt.getPort();
				
				// setup new expiredTS
    			Date expired = new Date(System.currentTimeMillis()+5*60*1000);
    			String expireTimestamp = sdf.format(expired);
				
				//here inBuf contains the callID and operationCode
				String request = new String(recvPkt.getData());
				System.out.println("SPCServerThread Received Request: " + request);
				String[] parts = request.split("_");
				Integer callID = Integer.parseInt(parts[0]);
				Integer operationCode = Integer.parseInt(parts[1]);
				String sessionID = parts[2];
				Integer currentVersion = Integer.parseInt(parts[3]);
				String reply = "";
				SessionValues sv = Globals.hashtable.get(sessionID);
				switch(operationCode)
				{
					case 0: //sessionRead operation
					{
						if(sv != null)
						{
		        			// currently a naive way to create new session ID: using UUID
		        			// TODO: should change to the form < SvrID, reboot_num, sess_num >
							
							sv.sessionExpiredTS = expireTimestamp;
							sv.sessionVersion += 1;
							//sessionID = UUID.randomUUID().toString();
							Globals.hashtable.put(sessionID, sv);
							reply = callID + "_" + sessionID + "_" + sv.sessionVersion + "_" + sv.sessionMessage + "_" + sv.sessionExpiredTS + "_" + "dummyParam";
						}
						break;
					}
					case 1: //sessionWrite operation
					{
						String newMessage = parts[4];
						String metaData = parts[5];
						if(sv!= null)
						{
		        			// currently a naive way to create new session ID: using UUID
		        			// TODO: should change to the form < SvrID, reboot_num, sess_num >
							
							sv.sessionMessage = newMessage;
							sv.sessionVersion = (currentVersion+1);
							sv.sessionExpiredTS = expireTimestamp;
							sv.locMetaData = metaData;
							//sessionID = UUID.randomUUID().toString();
							Globals.hashtable.put(sessionID, sv);
						}
						else
						{
		        			// currently a naive way to create new session ID: using UUID
		        			// TODO: should change to the form < SvrID, reboot_num, sess_num >
							
							sv = new SessionValues(currentVersion+1, newMessage, expireTimestamp);
							sv.locMetaData = metaData;
							//sessionID = UUID.randomUUID().toString();
							Globals.hashtable.put(sessionID, sv);
						}
						reply = callID + "_" + sessionID + "_" + sv.sessionVersion + "_" + sv.sessionMessage + "_" + sv.sessionExpiredTS + "_" + sv.locMetaData + "_" + "dummyParam";
						break;
					}
					default:
						break;
				}
				
				byte[] outBuf = new byte[256];
				DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, returnAddr, returnPort);
				sendPkt.setData(reply.getBytes());
				rpcSocket.send(sendPkt);
			}
		} 
    	catch (SocketException e) 
    	{
			e.printStackTrace();
		}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    }

    public void doShutdown() 
    {

    }
}
