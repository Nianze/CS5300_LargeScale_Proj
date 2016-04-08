package sessionManagement;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

public class RPCClient 
{
	
	private ArrayList<InetAddress> requestAddress;
	private final int portProj1bRPC = 5300;
	private String[] locMetaData;  
	private int R = 2;
	private int W = 3;
	private int WQ = 2;
	
	public RPCClient(ArrayList<InetAddress> list)
	{
		requestAddress = list;
	}
	
	public RPCClient(String metaData) throws UnknownHostException{
		locMetaData = metaData.split("_");
	}
	
	public RPCClient(){}

	public SessionValues sessionReadClient(String sessionID, Integer callID)
	{
		try 
		{
			DatagramSocket rpcSocket = new DatagramSocket();
			Integer operationCode = 0;
			String request = callID + "_" + operationCode + "_" + sessionID + "_" + "dummyParam";
			byte[] outBuf = new byte[256];
			for(int i = 0; i < R; i++){
				// get R instances which has stored sessoin_value				
				requestAddress = new ArrayList<InetAddress>();				
				for(int j = 0; j < locMetaData.length; j++){
					requestAddress.add(InetAddress.getByName(Globals.ipAddressMapping.get(locMetaData[j])));
				}
				// get random R instances' ip address 
				Random randomizer = new Random();
				InetAddress addr = requestAddress.get(randomizer.nextInt(requestAddress.size()));
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
			do
			{
				rpcSocket.receive(recvPkt);
				String response = new String(recvPkt.getData());
				System.out.println("SPCClient Received Response: " + response);
				String[] parts = response.split("_");
				replyCallID = Integer.parseInt(parts[0]);
				returnSessionID = parts[1];
				version = Integer.parseInt(parts[2]);
				message = parts[3];
				expiredTS = parts[4];				
			} while(replyCallID != callID);
			
			rpcSocket.close();
			return new SessionValues(version, message, expiredTS);
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
	public SessionValues sessionWriteClient(String sessionID, String newMessage, Integer callID)
	{
		try 
		{
			// randomly choose W instances to write new session
			Random randomizer = new Random();
			locMetaData = new String[W];
			for(int j = 0; j < W; j++){				
				locMetaData[j] = (String) Globals.ipAddressMapping.entrySet().toArray()[(randomizer.nextInt(Globals.ipAddressMapping.size()))];
				requestAddress.add(InetAddress.getByName(Globals.ipAddressMapping.get(locMetaData[j])));
			}
			StringBuilder builder = new StringBuilder();
			// create new location MetaData in session value
			for(String s : locMetaData) {
			    builder.append(s);
			    builder.append("_");
			}
			
			DatagramSocket rpcSocket = new DatagramSocket();
			Integer operationCode = 1;
			String request = callID + "_" + operationCode + "_" + sessionID + "_" + newMessage + "_" + builder.toString();
			byte[] outBuf = new byte[256];

			System.out.println("Before Sending!!");
			for(int i = 0; i < WQ; i++){
				// get random WQ instances' ip address 				
				InetAddress addr = requestAddress.get(randomizer.nextInt(requestAddress.size()));
				DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, addr, portProj1bRPC);
				sendPkt.setData(request.getBytes());
				rpcSocket.send(sendPkt);
			}
			System.out.println("After Sending!!");
			
			byte[] inBuf = new byte[256];
			DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
			Integer replyCallID = -1;
			String message = "";
			String returnSessionID = "";
			int version = -1;
			String expiredTS = "";
			
			int wait4WQ = 0;
			do{
				rpcSocket.receive(recvPkt);
				String response = new String(recvPkt.getData());
				System.out.println("SPCClient Received Response: " + response);
				String[] parts = response.split("_");
				replyCallID = Integer.parseInt(parts[0]);
				returnSessionID = parts[1];
				version = Integer.parseInt(parts[2]);
				message = parts[3];
				expiredTS = parts[4];
				if( replyCallID == callID ) wait4WQ++;
			} while(wait4WQ < WQ);
			
			rpcSocket.close();
			
			SessionValues sv = new SessionValues(version, message, expiredTS);
			sv.locMetaData = builder.toString();
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
