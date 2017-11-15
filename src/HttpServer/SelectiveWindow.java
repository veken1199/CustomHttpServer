package HttpServer;

import java.util.ArrayList;

public class SelectiveWindow {

	private int windowSize;
	private ArrayList<Packet> packets;
	private ArrayList<Packet> window;
	private int SequenceNumberOffset;

	public SelectiveWindow(int windowSize, int sequenseOffset){
		this.windowSize = windowSize;
		this.SequenceNumberOffset = sequenseOffset;
	}
	
	

}
