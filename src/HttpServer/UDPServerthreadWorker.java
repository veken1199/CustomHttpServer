package HttpServer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister.Pack;


public class UDPServerthreadWorker implements Runnable {

	private static final int WINDOWSIZE = 5;

	private static SocketAddress ROUTERADDRESS = new InetSocketAddress("localhost", 3000);

	private enum CState {
		NONE, SYN, SYN_ACK, SYN_SIZE, DATA, ESTABLISHED
	};

	public Thread currentThread;

	private int clientPort;
	private InetSocketAddress clientFullAddress;
	private WindowManager windowManager;

	Queue<Packet> queue = new ConcurrentLinkedQueue<Packet>();
	public ArrayList<Packet> processedPackets;
	private ArrayList<Packet> unprocessedPackets;
	private ArrayList<Packet> newUnprocessedPackets;

	private CState connectionState;
	private DatagramChannel UDPChannel;

	private ConvertorMapper cm;
	private RequestsManager rm;
	private StreamManager streamManager;
	private BufferedReader reader;

	public boolean updated;

	// Constructor
	public UDPServerthreadWorker(DatagramChannel UDPChannel, Packet pack) {
		this.connectionState = CState.NONE;
		this.clientPort = pack.getPeerPort();
		this.clientFullAddress = new InetSocketAddress("localhost", this.clientPort);
		this.UDPChannel = UDPChannel;
		this.streamManager = new StreamManager(new Logger());

		this.unprocessedPackets = new ArrayList<Packet>();
		this.processedPackets = new ArrayList<Packet>();
		this.newUnprocessedPackets = new ArrayList<Packet>();

		this.cm = new ConvertorMapper();
		this.rm = new RequestsManager();
		this.windowManager = new WindowManager(pack, UDPChannel);
	}

	public synchronized void receivePacket(Packet packet) {
		this.unprocessedPackets.add(packet);
		this.queue.add(packet);
	}

	@SuppressWarnings("deprecation")
	public void run() {

		// step one check if we already established connection with the client
		if (!(this.connectionState == CState.ESTABLISHED)) {
			try {
				// Threeway handshake
				this.threeWayHandShake();
			}

			catch (InterruptedException e) {
				System.out.println("ERROR887: could not establish three way shake");
			}
		}

		else if (this.connectionState == CState.ESTABLISHED || this.connectionState == CState.SYN_SIZE) {
			boolean updated = true;
			System.out.println("WHAT NOW?");
			// parse the request and see what the client is actually looking
			// for!

			while (updated) {
				this.notifyUpdate(false);
				// unprocessedPackets.removeAll(processedPackets);
				// unprocessedPackets.addAll(this.newUnprocessedPackets);
				// this.cleanNewUnprocessedPackets(null, false,
				// newUnprocessedPackets);

				// unprocessedPackets.removeAll(processedPackets);
				// unprocessedPackets.addAll(this.newUnprocessedPackets);
				// this.cleanNewUnprocessedPackets(null, false,
				// this.newUnprocessedPackets);

				Iterator<Packet> packIterator = queue.iterator();
				// Iterator<Packet> packIterator =
				// unprocessedPackets.iterator();

				// getting the request
				// if the sender sends a GET request
				while (packIterator.hasNext()) {
					// packIterator = unprocessedPackets.iterator();
					Packet pack = packIterator.next();

					// check if the sendering is doing POST
					if (pack.getType() == 5) {
						windowManager.setWINDOWSIZE(5);
						windowManager.setCurrentSequenceNumber(1);
						windowManager.initializeReceiverWindow(1, Integer.parseInt(new String(pack.getPayload())));

						windowManager.initialShiftWindow();

						// setting up the sliding window system
						windowManager.setPacketsNumber(Integer.parseInt(new String(pack.getPayload())));

						this.sendPacket(this.packetBuilder(9, "".getBytes(), 1L));
						System.out.print("Window created, ready to receive packets");

						this.connectionState = CState.SYN_ACK;
						this.processedPackets.add(pack);
						packIterator.remove();
					}

					// check if im receiving data packets
					if (pack.getType() == 7) {

						System.out.println("Size of the receiver window: " + windowManager.getPacketsNumber());

						// start getting the packets
						// check if we can slide
						if (!windowManager.isWindowWaitingForPackets()) {
							windowManager.shiftWindow();
						}

						System.out.println("processing packet sn: " + pack.getSequenceNumber());
						// step 1 check if the current packet sequence
						// number is
						// within the window
						if (windowManager.isPacketAccepted(pack)) {

							// remove the packet sequence number
							windowManager.removePacketFromReceiverWindow(pack);

							// add the packet to receivedPackets
							windowManager.insertPacketReceivedPackets(pack);

							// send ack
							this.sendPacket(this.packetBuilder(10, "".getBytes(), pack.getSequenceNumber()));

							System.out.println(new String(pack.getPayload()));
							System.out.println(pack.getSequenceNumber());
							
							this.processedPackets.add(pack);
							packIterator.remove();
							
						} else {
							System.out.print("Packet " + pack.getSequenceNumber() + " REJECTED");
							this.processedPackets.add(pack);
							packIterator.remove();
						}
					

					System.out.println("Transction is done!");
					System.out.println("constructing the reponse for the packets");
					System.out.println("------------------------------------------");
					System.out.println(windowManager.decodePackets());
					System.out.println("------------------------------------------");
					
					Reader inputString = new StringReader(windowManager.decodePackets());
					String[] request_info = streamManager.readRequest(new BufferedReader(inputString));
					String[] response = rm.requestHandler(request_info[0], request_info[1], request_info[2], request_info[4], this.streamManager);
					
					
					//send the response
					this.sendPacket(this.packetBuilder(5, this.rm.prepareHeader(request_info[3], response[1], request_info[5]) + response[0]));
				
					}
				// parse the get request into a packet
					

				// If the sender sents his request packet
				if (pack.getType() == 8) {
					this.reader = new BufferedReader(
							new InputStreamReader(new ByteArrayInputStream(pack.getPayload())));
					String[] request_info = this.streamManager.readRequest(reader);

					// handle the request;
					String[] response = rm.requestHandler(request_info[0], request_info[1], request_info[2],
							request_info[4], this.streamManager);
					String responseHeader = this.rm.prepareHeader(request_info[3], response[1], request_info[5]);

					response[0] = cm.convert(response[0], request_info[3]);
					response[0] = responseHeader + response[0];

					int responseSize = response[0].getBytes().length;
					int numberOfPackets = (responseSize / 1013) + 1;
					byte[] responseBytes = response[0].getBytes();

					this.windowManager.setPacketsNumber(numberOfPackets);
					this.windowManager.windowPacketBuilder(numberOfPackets, responseBytes);

					// number send the number of the packet the sender will
					// be expecting
					// setting the state to SYN-SIZE
					windowManager.setCurrentSequenceNumber(1);
					windowManager.setWINDOWSIZE(5);
					this.sendPacket(this.packetBuilder(5, numberOfPackets + ""));

					this.windowManager.initializeSenderWindow(1, numberOfPackets);
					windowManager.initialShiftWindow();

					this.connectionState = CState.SYN_SIZE;

					this.processedPackets.add(pack);
					packIterator.remove();
					continue;
				}

				// waiting for sender to tell us that his window is ready by
				if (pack.getType() == 9) {
					this.processedPackets.add(pack);
					packIterator.remove();

					windowManager.sendWindow();
					continue;
				}

				// waiting for the acks
				if (pack.getType() == 10) {
					System.out.println("Ack received with seq num : " + pack.getSequenceNumber());
					if (windowManager.isValidAct(pack)) {

						System.out.println("removing the the acked packet, " + pack.getSequenceNumber());
						this.windowManager.receiveAck(pack);

						packIterator.remove();
						this.processedPackets.add(pack);

						// check if we can shift the window now
						if (this.windowManager.isWindowReceived()) {
							this.windowManager.shiftWindow();
							this.windowManager.sendWindow();
						}
					} else {
						packIterator.remove();
						this.processedPackets.add(pack);
						System.out.println("Received a delayed ACK " + pack.getSequenceNumber());
					}

					// check if we transmitted all the files
					if (this.windowManager.isSenderTransmissionDone()) {
						System.out.println("The request is complete!");
						this.notifyUpdate(false);
						break;
					}
				}
			}
		}
	}

	}

	public synchronized void cleanNewUnprocessedPackets(Packet pack, boolean state, ArrayList<Packet> packList) {
		if (state) {
			System.out.println("adding to the queue " + pack.getType() + " sn " + pack.getSequenceNumber());
			this.newUnprocessedPackets.add(pack);
			this.unprocessedPackets.add(pack);
		} else {
			for (Packet packs : packList) {
				System.out.println("removing from the queue " + packs.getType() + " sn " + packs.getSequenceNumber());

			}
			this.newUnprocessedPackets.removeAll(packList);
		}

	}

	public void threeWayHandShake() throws InterruptedException {
		// first step remove the procesed packets
		unprocessedPackets.removeAll(processedPackets);

		if (this.connectionState == CState.NONE || this.connectionState == CState.SYN_ACK) {
			// check what packet type we received : we need syn type

			Iterator<Packet> packIterator = unprocessedPackets.iterator();
			while (packIterator.hasNext()) {
				packIterator = unprocessedPackets.iterator();
				Packet pack = packIterator.next();
				if (pack.getType() == 0) {
					// remove the packet
					this.processedPackets.add(pack);
					packIterator.remove();

					// send SYN-ACK
					System.out.println("---------------------------");
					System.out.println("HandShaking step 1");
					Packet packet = this.packetBuilder(1, "SYN-ACK");
					sendPacket(packet);

					this.connectionState = CState.SYN_ACK;
					continue;
				}

				else if (pack.getType() == 1) {
					// remove this packet
					System.out.println("HandShaking step 2");
					processedPackets.add(pack);
					packIterator.remove();

					this.connectionState = CState.ESTABLISHED;
					System.out.println("Connection established with " + pack.getPeerPort());
					System.out.println("---------------------------\n");
					continue;
				}
			}
		}
	}

	public void sendPacket(Packet packet) {
		try {
			this.UDPChannel.send(packet.toBuffer(), ROUTERADDRESS);
		}

		catch (IOException e) {
			System.out.println("ERROR444:The server could not sent the packet");
		}
	}

	public Packet packetBuilder(int type, String payload) {
		return new Packet.Builder().setType(type).setSequenceNumber(1).setPortNumber(this.clientFullAddress.getPort())
				.setPeerAddress(this.clientFullAddress.getAddress()).setPayload(payload.getBytes()).create();
	}

	public Packet packetBuilder(int type, byte[] payload, long sequenceNumber) {
		return new Packet.Builder().setType(type).setSequenceNumber(sequenceNumber)
				.setPortNumber(this.clientFullAddress.getPort()).setPeerAddress(this.clientFullAddress.getAddress())
				.setPayload(payload).create();
	}


	public synchronized void removePacket(Packet packet) {
		this.unprocessedPackets.remove(packet);
	}

	public void setThread(Thread thread) {
		this.currentThread = thread;
	}

	public synchronized void notifyUpdate(boolean flag) {
		this.updated = flag;
	}

	public void processResponse() {
		// forEach(Packet pack : this.receivedPackets)
	}

}
