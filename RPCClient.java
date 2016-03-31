package sessionManagement;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class RPCClient 
{
	
	private ArrayList<InetAddress> requestAddress;
	private final int portProj1bRPC = 5300;
	
	public RPCClient(ArrayList<InetAddress> list)
	{
		requestAddress = list;
	}

	public SessionValues sessionReadClient(String sessionID, Integer callID)
	{
		try 
		{
			DatagramSocket rpcSocket = new DatagramSocket();
			Integer operationCode = 0;
			String request = callID + "_" + operationCode + "_" + sessionID + "_" + "dummyParam";
			byte[] outBuf = new byte[256];
			for(InetAddress addr: requestAddress)
			{
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
			DatagramSocket rpcSocket = new DatagramSocket();
			Integer operationCode = 1;
			String request = callID + "_" + operationCode + "_" + sessionID + "_" + newMessage + "_" + "dummyParam";
			byte[] outBuf = new byte[256];
			for(InetAddress addr: requestAddress)
			{
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
	
}
