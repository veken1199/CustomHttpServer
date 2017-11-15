package HttpServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;


public class UDPServerthreadWorker implements Runnable  {
	
	private static SocketAddress ROUTERADDRESS = new InetSocketAddress("localhost", 3000);
	private long startSequenseNumber;
	private int clientPort;
	private InetAddress clientAddress;
	private InetSocketAddress clientFullAddress;
	private ArrayList<Packet> unprocessedPackets; 
	private enum CState {NONE, SYN, SYN_ACK, ESTABLISHED};
	private CState connectionState;
	private DatagramChannel UDPChannel; 
	
	public UDPServerthreadWorker(DatagramChannel UDPChannel, Packet pack){
		this.clientPort = pack.getPeerPort();
		this.clientAddress = pack.getPeerAddress();
		this.clientFullAddress = new InetSocketAddress("localhost", this.clientPort);
		this.UDPChannel = UDPChannel;
		this.startSequenseNumber = pack.getSequenceNumber();
		this.connectionState = CState.NONE;
		this.unprocessedPackets = new ArrayList<Packet>();
	}
	
	public void receivePacket(Packet packet){
		this.unprocessedPackets.add(packet);
	}
	
	public void run(){
		//step one check if we already established connection with the client
		if(!(this.connectionState == CState.ESTABLISHED)){
			threeWayHandShake();
		}
	}
	
	public void threeWayHandShake(){
		//first step
		System.out.println("handshaking");
		if(this.connectionState == CState.NONE){
			//check what packet type we received : we need syn tpye
			for(Packet pack : unprocessedPackets){
				System.out.print(pack.getPayload().toString());
				if(pack.getType() == 0){
					//send SYN-ACK
					System.out.println("HandShaking step 1");
					Packet packet = new Packet.Builder()
							.setType(1)
			                .setSequenceNumber(this.startSequenseNumber + 1)
			                .setPortNumber(this.clientFullAddress.getPort())
			                .setPeerAddress(this.clientFullAddress.getAddress())
			                .setPayload("HI from Server".getBytes())
			                .create();
					
					sendPacket(packet);
					this.connectionState = CState.SYN_ACK;
				}
			}
		}
	}
	
	public void sendPacket(Packet packet){
        try {
        	System.out.println("HandShaking step 1 sent");
			this.UDPChannel.send(packet.toBuffer(), ROUTERADDRESS);
		} catch (IOException e) {
			System.out.println("ERROR444:The server could not sent the packet");
		}
	}
	

}


