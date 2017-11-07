package HttpServer;

import java.util.ArrayDeque;
import java.util.Queue;

public class Logger {
	public Queue<String> msgLogs = new ArrayDeque<String>();
	public static boolean ALLOWLOGGING = true;
	
	public Logger(){
	}
	
	public void addMessage(String str){
		this.msgLogs.add(str);
		this.displayMessages();
	}
	
	public void displayMessages(){
		while(!this.msgLogs.isEmpty() && ALLOWLOGGING){
			System.out.println(this.msgLogs.remove());
		}
	}
	

}
