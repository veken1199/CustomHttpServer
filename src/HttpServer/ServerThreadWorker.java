package HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.simple.JSONObject;

public class ServerThreadWorker implements Runnable {
	
	private Socket socket;
	private PrintWriter out;
	private BufferedReader reader;
	private StreamManager streamManager;
	private RequestsManager rm;
	private ConvertorMapper cm;
	private Logger logger;
	
	public static int request_num = 1;
	
	public ServerThreadWorker(Socket socket) throws Exception{
		this.socket = socket;
		this.logger = new Logger();
		this.streamManager = new StreamManager(this.logger);
	}
	
	public void run(){
		
		try {
			this.cm = new ConvertorMapper();
			this.rm = new RequestsManager();
			
			this.out = new PrintWriter(new OutputStreamWriter (this.socket.getOutputStream()));
			this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			
			String[] request_info = this.streamManager.readRequest(this.reader);
			
			//handle the request;
			String[] response = rm.requestHandler(request_info[0], request_info[1], request_info[2], request_info[4], this.streamManager);
			response[0] = cm.convert(response[0], request_info[3]);
			
			this.out.print(this.rm.prepareHeader(request_info[3], response[1], request_info[5]));
			
			if(request_info[5].contains("none")){
				this.out.println(response[0]);
			}
			else{
				this.streamManager.returnAttachment(System.getProperty("user.dir") + response[0] ,out);
			}
				
			this.out.flush();
			this.out.close();
			
			this.reader.close();
			this.socket.close();
			
			this.logger.displayMessages();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
