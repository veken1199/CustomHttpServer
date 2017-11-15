package HttpServer;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.util.Hashtable;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;

import java.io.IOException;

public class UDPRequestsManager extends RequestsManager {

	public DatagramChannel UDPChannel;
	public ByteBuffer buf;
	private String connectionState;
	//we assume all the clients are on the local host
	private Hashtable<Integer, UDPServerthreadWorker> clientsMap = new Hashtable<Integer, UDPServerthreadWorker>();
	
	public void startServer(int port){
		prepareServer(port);
		
		while(true){
			try {
				SocketAddress router =  UDPChannel.receive(buf);
				buf.flip();
				Packet packet = Packet.fromBuffer(buf);
				buf.flip();
				
				String payload = new String(packet.getPayload(), UTF_8);
				System.out.println("Print the content of a packet: " + payload + " from address " + packet.getPeerPort());
				
				UDPServerthreadWorker udpWorker = clientsMap.containsKey(packet.getPeerPort())?
						clientsMap.get(packet.getPeerPort()):
							clientsMap.put(packet.getPeerPort(), new UDPServerthreadWorker(this.UDPChannel, packet));
				
				
				udpWorker = clientsMap.get(packet.getPeerPort());
				udpWorker.receivePacket(packet);
				Thread udpThread = new Thread(udpWorker);
				udpThread.start();
		
				
			} catch (IOException e) {
				System.out.println("ERROR323: Error while receiving packets");
				e.printStackTrace();
			}
		}
	}
	
	private void prepareServer(int port){
		try {
			this.UDPChannel = DatagramChannel.open();
			this.UDPChannel.socket().bind(new InetSocketAddress(port));
			buf = ByteBuffer.allocate(Packet.MAX_LEN)
	                    	.order(ByteOrder.BIG_ENDIAN);
			
		} catch (IOException e) {
			System.out.print("ERROR500: Error occured while creating UDP channel");
			e.printStackTrace();
		}
		
	}
	
}
