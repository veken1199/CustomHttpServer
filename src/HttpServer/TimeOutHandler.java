package HttpServer;

import java.lang.Thread.UncaughtExceptionHandler;

import HttpServer.UDPServerthreadWorker.CState;

public class TimeOutHandler implements UncaughtExceptionHandler{
	
	public UDPServerthreadWorker s;
	private String msg;
	private String type;
	
	public void uncaughtException(Thread th, Throwable ex) {
		switch(type){
		case "handshake":
			if((s.connectionState == CState.NONE || s.connectionState == CState.SYN_ACK)){
				s.connectionState =  UDPServerthreadWorker.CState.BAD;
				System.out.println(msg);
			}
			
			break;
		
		case "sendpackets":
			if(s.connectionState != CState.DONE){
				System.out.println(msg);
				s.senderRetransmitWindow();
				break;
			}
			
		case "resendRes":
			
			s.retransmitResponse();
			break;
		
		case "none":
			break;
			
		case "resendGetResponse":
			if(s.connectionState != CState.DONE){
				s.retransmitRequest();
			}
			
		case "resendGetRequest":
			s.retransmitRequest();
			break;
		}
	}
	
	
	public TimeOutHandler(UDPServerthreadWorker server, String errorMsg, String type) {
		this.s = server;
		this.msg = errorMsg;
		this.type = type;
	}
			
	public void setUDP(UDPServerthreadWorker server){
	}
		
}
