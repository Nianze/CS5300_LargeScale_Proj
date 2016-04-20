package sessionManagement;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.Map.Entry;

public class RPCClient 
{
	
	private ArrayList<InetAddress> requestAddress;
	private final int portProj1bRPC = 5300;
	private String[] locMetaData;  
	
	public RPCClient(ArrayList<InetAddress> list)
	{
		requestAddress = list;
	}
	
	public RPCClient(String metaData) throws UnknownHostException{
		locMetaData = metaData.split("_");
		requestAddress = new ArrayList<InetAddress>();
	}
	
	// for new users, randomly assign servers to store data
	public RPCClient()
	{
		requestAddress = new ArrayList<InetAddress>();
	}

	public SessionValues sessionReadClient(String sessionIDWithVersion, Integer callID)
	{
		try 
		{
			DatagramSocket rpcSocket = new DatagramSocket();
			Integer operationCode = 0;
			String request = callID + "_" + operationCode + "_" + sessionIDWithVersion + "_" + "dummyParam";
			byte[] outBuf = new byte[256];
			
			// select the first R address in metadata and store the IPs in requestAddress
			for(int i = 0; i < Globals.R; i++)
				requestAddress.add(InetAddress.getByName(Globals.ipAddressMapping.get(locMetaData[i])));
			
			for(int i = 0; i < requestAddress.size(); i++)
			{
				InetAddress addr = requestAddress.get(i);
				DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, addr, portProj1bRPC);
				sendPkt.setData(request.getBytes());
				rpcSocket.send(sendPkt);
			}
			
			byte[] inBuf = new byte[256];
			DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
			Integer replyCallID = -1;
			String message = "";
			String returnSessionID = "";
			int version = -1;
			String expiredTS = "";
			String returnServerID = "";
			do
			{
				rpcSocket.receive(recvPkt);
				
				// check which serverID responded to the request
				String response = new String(recvPkt.getData());
				String returnAddress = recvPkt.getAddress().getHostAddress();
		        for (Entry<String, String> entry : Globals.ipAddressMapping.entrySet())
		        {
		            if (entry.getValue().equals(returnAddress))
		            {
		                returnServerID = entry.getKey();
		            }
		        }
				System.out.println("SPCClient Received Response: " + response);
				String[] parts = response.split("_");
				replyCallID = Integer.parseInt(parts[0]);
				returnSessionID = parts[1];
				version = Integer.parseInt(parts[2]);
				message = parts[3];
				expiredTS = parts[4];			
			} while(replyCallID != callID);
			
			rpcSocket.close();
			
			SessionValues sv = new SessionValues(version, message, expiredTS);
			sv.returnSessionID = returnSessionID;
			sv.readServerID = returnServerID;
			return sv;
		}
		catch (SocketException e) 
		{
			e.printStackTrace();
			return null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		
	}
	
	// TODO: need to fix this to match the SSM protocol
	// Right now, send to all servers and wait for one reply (just like a read)
	public SessionValues sessionWriteClient(String sessionIDWithVersion, String newMessage, Integer callID)
	{
		try 
		{
			// randomly choose W ip address instances to write new session (add those ip into requestAddress)
			ArrayList<String> copy = new ArrayList<String>(Globals.ipAddressMapping.keySet());
			Collections.shuffle(copy);
			locMetaData = new String[Globals.W];
			for(int i = 0; i < Globals.W; i++){
				locMetaData[i] = copy.get(i); 
				requestAddress.add(InetAddress.getByName(Globals.ipAddressMapping.get(locMetaData[i])));
			}
			
			StringBuilder builder = new StringBuilder();
			for(String s : locMetaData) 
			{
			    builder.append(s);
			    builder.append("_");
			}
			
			DatagramSocket rpcSocket = new DatagramSocket();
			Integer operationCode = 1;
			String request = callID + "_" + operationCode + "_" + sessionIDWithVersion + "_" + newMessage + "_" + builder.toString() + "dummyParam";
			byte[] outBuf = new byte[256];

			// send to all addresses in requestAddress
			for(int i = 0; i < requestAddress.size(); i++)
			{
				// get random WQ instances' ip address 				
				InetAddress addr = requestAddress.get(i);
				DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, addr, portProj1bRPC);
				sendPkt.setData(request.getBytes());
				rpcSocket.send(sendPkt);
			}
			
			byte[] inBuf = new byte[256];
			DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
			Integer replyCallID = -1;
			String message = "";
			String returnSessionID = "";
			String returnServerID = "";
			int version = -1;
			String expiredTS = "";
			
			int wait4WQ = 0;
			do{
				rpcSocket.receive(recvPkt);
				
				// check which serverID responded to the request
				String response = new String(recvPkt.getData());
				String returnAddress = recvPkt.getAddress().getHostAddress();
		        for (Entry<String, String> entry : Globals.ipAddressMapping.entrySet())
		        {
		            if (entry.getValue().equals(returnAddress))
		            {
		                returnServerID += entry.getKey();
		                returnServerID += "_";
		            }
		        }
				
				System.out.println("SPCClient Received Response: " + response);
				String[] parts = response.split("_");
				replyCallID = Integer.parseInt(parts[0]);
				returnSessionID = parts[1];
				version = Integer.parseInt(parts[2]);
				message = parts[3];
				expiredTS = parts[4];
				if( replyCallID == callID ) wait4WQ++;
			} while(wait4WQ < Globals.WQ);
			
			rpcSocket.close();
			SessionValues sv = new SessionValues(version, message, expiredTS);
			sv.locMetaData = returnServerID;
			sv.returnSessionID = returnSessionID;
			sv.writeServerID = returnServerID.substring(0, returnServerID.length()-1);
			return sv;
		}
		catch (SocketException e) 
		{
			e.printStackTrace();
			return null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
}
