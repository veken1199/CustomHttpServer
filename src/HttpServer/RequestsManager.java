package HttpServer;

import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Hashtable;
import java.util.Map;

class Item{
	public double price;
	
}
public class RequestsManager {	
	
	private ServerSocket server;
	private Socket socket;
	
	//Method resposible for creating the server socket and starting it on the machine
	public void startServer(int port, String Directory){
		
		try {
			this.prepareRequest(port);

			while(true){
				this.socket = server.accept();
				ServerThreadWorker worker = new ServerThreadWorker(this.socket);
				Thread threadWorker = new Thread(worker);
				threadWorker.start();
				}
			
		} catch (IllegalArgumentException e){
			System.out.print("ERROR RM1 - The port number is outside the acceptable range.");
		} catch (IOException e){
			System.out.println("ERROR RM2 - The port is all ready in use, try different port");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	//Method to terminate a server socket
	public void stopServer(){
		try {
			this.server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	// Method responsible for opening sockets and preparing input/output streams
	private void prepareRequest(int port) throws Exception{
		this.server = new ServerSocket(port);
		System.out.println("\nThe Server is ready to accept http requests........");
	}
	
	//This method is responsible for handling the request
	public String[] requestHandler(String request_info, String request_method, String request_path, String body, StreamManager streamManager){
		if(request_method.equalsIgnoreCase("get")){
			return this.handleGET(request_info, request_method, request_path, streamManager);
		}
		
		else if(request_method.equalsIgnoreCase("post")){
			return this.handlePOST(request_info, request_method, request_path, body, streamManager);
		}
		
		else{
			return new String[]  {"Invalid Request Method, The server currently accepts GET and POST only!", "500"};
		}
	}
	
	
	//Method to handle POST request
	private String[] handlePOST(String request_info, String request_method, String request_path, String body, StreamManager streamManager) {
		//Step 1 : get the fileSecurityLayer object
		FileSecurityLayer fsl = FileSecurityLayer.getFileSecurityLayerObject("");
		
		//Step 2 : parse the path
		String[] parsed_path = Utilities.parsePath(request_path);
		
		//Step 3 : Check if the requested path is legal 
		if(fsl.isPathAccessable(parsed_path[0])){
			//Step 3a: read the content of the parsed_path
			try {
				//Step 3b: convert the body to map<string>
				Map<String, String> body_map = Utilities.mapConverter(body);
				
				if (!this.isValidContent(body_map)){
					return new String[] {"make sure the body contains 'content'<string> and 'overwrite' <boolean>", "500"};
				}
		
				return new String[]{ (streamManager.writeFile(fsl.getServerDirectory()+request_path, body_map) + " "), "200" };
				
			} catch (Exception e) {
				System.out.println(e);
				return new String[] {"Error occured will writing to the file", "500"};
			}
		}
		else{
			return new String[] {"Requested file not found", "500"};
		}
	}

	private boolean isValidContent(Map<String, String> body_map) {
		if(!body_map.containsKey("content")){
			return false;
		}
		
		if(!body_map.containsKey("overwrite")){
			if(! (body_map.get("overwrite").contains("true")) || !(body_map.get("overwrite").contains("false")) ){
				return false;
			}
		}
		return true;
	}

	//Method to handle GET request
	//Returns array of strings such that str[0] is the response, and str[1] is the response code
	private String[] handleGET(String request_info, String request_method, String request_path, StreamManager streamManager) {
		//Step 1 : get the fileSecurityLayer object
		FileSecurityLayer fsl = FileSecurityLayer.getFileSecurityLayerObject("");
				
		//Step 2 : parse the path
		String[] parsed_path = Utilities.parsePath(request_path);
				
		//Step 3 : Check if the requested path is legal 
		if(fsl.isPathAccessable(parsed_path[0] + parsed_path[1])){
			
			//Step 3a: read the content of the parsed_path
			return new String[] {streamManager.readPathContent(parsed_path[0] + parsed_path[1]), "200"};
		}
		else{
			return new String[] {"Requested file not found", "400"};
		}
	}


	public String prepareHeader(String accept, String response_code, String disposition){
		
		Hashtable<String,String> t = new Hashtable<String,String>();
		t.put("*/*", "text");
		t.put("application/json", "application/json");
		t.put("attachment", "application/octet- stream");
		t.put("", "text");
		String str = "";
		
		
		//str = str + "Content-type: " + "application/octet- stream"+ "\n";
		//str = str + "Content-Disposition: filename=pyyyy.text" + "\n";
		str = "HTTP/1.1 " + response_code + "\n";
		str = str + "Content-type: " + t.get(accept) + "\n";
		
		if(!disposition.contains("none")){
		
			str = str + "Content-Disposition: attachment; " + disposition + "\n";
		}
		str = str + "Connection: closed \n\r\n";
	
		return str;
	}
}
