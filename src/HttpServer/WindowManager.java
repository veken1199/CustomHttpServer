package HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WindowManager {
	private int WINDOWSIZE = 0;
	private int currentSequenceNumber = 1;
	private static SocketAddress ROUTERADDRESS = new InetSocketAddress("localhost", 3000);
	private InetSocketAddress clientFullAddress;
	
	private Map<Integer, Integer> senderWindow;
	private Map<Integer, Integer> receiverWindow;
	private Map<Integer, Packet> receivedPacket;
	private ArrayList<Integer> slidingWindow;
	private Map<Integer, Packet> WindowPackets;

	private int packetsNumber;
	private DatagramChannel UDPChannel;
	
	public WindowManager(){
		this.receiverWindow = new HashMap<Integer, Integer>();
		this.receivedPacket = new HashMap<Integer, Packet>();
		this.senderWindow = new HashMap<Integer, Integer>();
		this.slidingWindow = new ArrayList<Integer>();
	}
	
	public WindowManager(Packet pack, DatagramChannel ch){
		this.clientFullAddress = new InetSocketAddress("localhost", pack.getPeerPort());
		this.receiverWindow = new HashMap<Integer, Integer>();
		this.receivedPacket = new HashMap<Integer, Packet>();
		this.senderWindow = new HashMap<Integer, Integer>();	
		this.slidingWindow = new ArrayList<Integer>();
		this.UDPChannel = ch;
		
	}
	
	
	public int getCurrentSequenceNumber() {
		return currentSequenceNumber;
	}


	public void setCurrentSequenceNumber(int currentSequenceNumber) {
		this.currentSequenceNumber = currentSequenceNumber;
	}
	
	
	/**
	 * @return the packetsNumber
	 */
	public int getPacketsNumber() {
		return packetsNumber;
	}
	
	
	/**
	 * @category This method will return the sequenceNumber and then increment it
	 * @return int
	 */
	public int getPacketNumberAndIncreament(){
		return this.currentSequenceNumber++;
	}

	
	/**
	 * @param packetsNumber the packetsNumber to set
	 */
	public void setPacketsNumber(int packetsNumber) {
		this.packetsNumber = packetsNumber;
	}
	
	
	public int getWINDOWSIZE() {
		return WINDOWSIZE;
	}

	
	public void setWINDOWSIZE(int wINDOWSIZE) {
		WINDOWSIZE = wINDOWSIZE;
	}
	
	
	public void initialShiftWindow(){
		System.out.println("Sliding the window : ");
		System.out.print("currently waiting for ");
		int newSequenceNumber = this.currentSequenceNumber;
		
		for(int i = 0; i < this.WINDOWSIZE; i++){
			System.out.print(newSequenceNumber +" ");
			this.slidingWindow.add(i, newSequenceNumber++);
		}
	}
	
	
	public void shiftWindow(){
		System.out.println("Sliding the window : ");
		System.out.print("currently waiting for ");
		this.currentSequenceNumber =  this.currentSequenceNumber + this.WINDOWSIZE;
		int newSequenceNumber = this.currentSequenceNumber;
		
		for(int i = 0; i < this.WINDOWSIZE; i++){
			System.out.print(newSequenceNumber +" ");
			this.slidingWindow.add(i, newSequenceNumber++);
		}
	}
	
	/**
	 * this function checks if all the sequence numbers within the window are acked
	 * @return
	 */
	public boolean isWindowWaitingForPackets(){
		for(int i = 0; i<this.WINDOWSIZE; i++){
			if(!this.receivedPacket.containsKey((this.slidingWindow.get(i)))){
				return true;
			}
		}
		return false;
	}

	
	public void initializeReceiverWindow(int firtPackSeqNumber, int size){
		for(int i = 0; i < size; i++){
			this.receiverWindow.put(firtPackSeqNumber++, 0); //0 means has not received
		}
	}
	
	
	public int countPacketsReceived(){
		return this.receivedPacket.size();
	}
	
	/**
	 * This method is responsible for inserting packets into the received packets collection
	 * @param pack
	 */
	public void insertPacketReceivedPackets(Packet pack){
		this.receivedPacket.put((int)pack.getSequenceNumber(), pack);

	}
	
	
	/**
	 * This method is responsible for informing the client whether the received packet 
	 * is a good one or a bad one
	 * @param pack
	 * @return
	 */
	public boolean isPacketAccepted(Packet pack){
		return ((pack.getSequenceNumber() >= this.slidingWindow.get(0) &&  pack.getSequenceNumber() <= this.slidingWindow.get(this.WINDOWSIZE-1)));
	}
	
	
	/**
	 * This method is responsible for checking if all expected packets have received
	 * @return boolean
	 */
	public boolean isTransmisionComplete(){
		return (this.receivedPacket.size() == this.packetsNumber);
	}
	
	
	public void removePacketFromReceiverWindow(Packet pack){
		this.receiverWindow.remove(pack.getSequenceNumber());
	}
	
	
	/**
	 *  this method is responsible for constructing the string from the packets receiced
	 * @return String
	 */
	public String decodePackets(){
		String decodedString = new String("");

		for(int i = 1; i <= this.packetsNumber; i++){
			decodedString = decodedString + new String(this.receivedPacket.get(i).getPayload());
		}

		return decodedString;
	}

	
	/**
	 * this method is responsible for creating the packets to be inserted in the window
	 * the packets to be transfered to the sender from the window
	 * @param numberOfPackets
	 * @param responseBytes
	 * @return
	 */
	public void windowPacketBuilder(int numberOfPackets, byte[] responseBytes){
		
		Map<Integer,Packet> windowPackets = new HashMap<Integer,Packet>();
		long packetSequenceNumber = 0;
		
		//generating the packet
		for(int i = 0; i < numberOfPackets; i++){
			byte[] packetData;
			// Check if it the last packet
			if(i == numberOfPackets-1){
				packetData = new byte[responseBytes.length - i*1013];
			}
			
			else{
				packetData = new byte[1013];
			}
			
			for(int j = 0; j < packetData.length; j++){
				packetData[j] = responseBytes[j + (i*1013)];
			}
				
			Packet packet = this.packetBuilder(7, packetData, ++packetSequenceNumber);
			System.out.println("Added the sequence number : " + packet.getSequenceNumber());
			windowPackets.put((int)packetSequenceNumber, packet);
		}
		
		this.WindowPackets =  windowPackets;
	}
	
	
	public Packet packetBuilder(int type, byte[] payload, long sequenceNumber){
		
		return new Packet.Builder()
				.setType(type)
                .setSequenceNumber(sequenceNumber)
                .setPortNumber(this.clientFullAddress.getPort())
                .setPeerAddress(this.clientFullAddress.getAddress())
                .setPayload(payload)
                .create();
	}
	
	
	/**
	 *  this method is responsible for initializing the entire window in order to track the acks
	 * @param initialSequenceNumber
	 * @param size
	 */
	public void initializeSenderWindow(int initialSequenceNumber ,int size){
		for(int i = 0; i < size; i++ ){
			this.senderWindow.put(i + initialSequenceNumber, 0);
		}
	}
	
	/**
	 * This method helps you to check whether the received ack is something the sender
	 * is waiting for
	 * @param pack
	 * @return
	 */
	public boolean isValidAct(Packet pack){
		return (this.senderWindow.containsKey((int)pack.getSequenceNumber()));
	}
	
	
	public void receiveAck(Packet pack){
		this.senderWindow.put((int)pack.getSequenceNumber(), 1);
	}
	/*
	 * This method is responsible for sending an entire window
	 */
	public void sendWindow(){
		
		boolean windowSent = false;
		while(!windowSent){
			System.out.println("the Sender created its window, the server starts to send the packets");
			for(int i = 0; i < this.WINDOWSIZE; i++){
				try{
					this.sendPacket(this.WindowPackets.get((int)this.currentSequenceNumber + i));
				}catch(Exception e){
					windowSent = true;
					System.out.println("No more packets to send");
					break;
				}
			}
		windowSent = true;
		}
	}	
	
	
	public void sendPacket(Packet packet)
	{
        try 
        {
			this.UDPChannel.send(packet.toBuffer(), ROUTERADDRESS);
		} 
        
        catch (IOException e) 
        {
			System.out.println("ERROR444:The server could not sent the packet");
		}
	}
	

	/**
	 * this method is resposible for testing whether the window has been act by the sender
	 * this method is used by the sender when sending window and making sure that the entire 
	 * window is received before shifting/sliding
	 * @return
	 */
	public boolean isWindowReceived(){
		for(int i = 0; i<this.WINDOWSIZE;i++){
			try{
				if(this.senderWindow.get((int)this.currentSequenceNumber + i) == 0){
					return false;
				}
			}catch(NullPointerException e){
				continue;
			}
			
		}return true;
	}
	
	
	public boolean isSenderTransmissionDone(){
		if(this.senderWindow.containsValue(0)){
			return false;
		}
		return true;
	}

	public void senderRetransmitWindow() {
		for(int i = 0; i < this.WINDOWSIZE; i++){
			if(this.senderWindow.get((int)this.currentSequenceNumber + i) == 0){
				this.sendPacket(this.WindowPackets.get((int)this.currentSequenceNumber + i));
			}
		}
	}
}
