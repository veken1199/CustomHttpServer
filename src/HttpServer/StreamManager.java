package HttpServer;

import java.awt.List;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.text.AbstractDocument.Content;

import org.omg.CORBA.portable.OutputStream;


public class StreamManager {
	
	private BufferedReader reader;
	private String header_top = "---------------Response Header--------------- \n";
	private String header_buttom = "---------------End of Response Header--------------- \n";
	private String response_top = "---------------Response Body--------------- \n";
	private String response_buttom = "---------------End of Response Body---------------\n";
	public String header_val = "";
	private static Hashtable<Integer,String> synchronizedFiles = new Hashtable<Integer,String>();
	private Logger logger;
	
	public StreamManager(Logger logger){
		this.logger = logger;
	}
	
	// Method responsible for reading the data from a file and pass 
	// the content to the POST method, or any other entity requested it.
	public String readFile(String filename)throws Exception{
		String data = "";
		String partial_data = "";
		
		while(synchronizedFiles.containsKey(filename.hashCode())){
			logger.addMessage("File ; " + filename +  " is not available");
			//System.out.println("File ; " + filename +  " is not available");
			Thread.sleep(1000);
		}
		
		this.lockFile(filename);
		
		this.logger.addMessage("Started reading " + filename);
		//System.out.println("Started reading " + filename);
		this.reader = new BufferedReader(new FileReader(filename));
		
		
		while((partial_data = reader.readLine()) != null){
			data = data + partial_data + "\n";
		}
		
		this.unlockFile(filename);
		Thread.yield();
		
		logger.addMessage("Finished reading " + filename);
		//System.out.println("Finished reading " + filename);
		reader.close();
		return data;
	}
	

	
	// Method responsiblt for reading and returning the response from the server.
	public String readResponse(BufferedReader reader, boolean verbose)throws Exception{
		String complete_response = "";
		String input_line = "";
		String header_response = "";
		boolean loop = true;	
		
		while (loop) {
			if (reader.ready()) {
				while ((input_line = reader.readLine()) != null) {
			        // finding the header in the reponse
			        // if the part of the stream is empty and the 'header' variable is empty,
			        // the part of the stream before the the response is the header
			        if(input_line.equals("") && header_response.equals("")){
			        	this.header_val = complete_response;
			        	if(verbose){
				        	header_response = this.header_top + complete_response + this.header_buttom;
				        	complete_response = "";
			        	}
			        	else{
			        		header_response = " ";
			        		complete_response = "";
			        	}
			        }
			        else{
			        	complete_response = complete_response + input_line + "\n";
			        	}
			        }
			       loop = false;
				}
			}
		
		reader.close();
		return header_response + this.response_top + complete_response + this.response_buttom;
	}
	
	
	// Method responsible for writing stirng to specifiec directory 
	// with the content passed to it as a string.
	public String writeFile(String filename, Map<String, String> body_map)throws Exception{
		//System.out.println("Started writing " + filename);
		logger.addMessage("Started writing " + filename);
		
		while(synchronizedFiles.containsKey(filename.hashCode())){
			logger.addMessage("File ; " + filename +  " is not available");
			//System.out.println("File ; " + filename +  " is not available");
			Thread.sleep(1000);
		}
		
		this.lockFile(filename);
		
		BufferedWriter out = new BufferedWriter(new FileWriter(filename, !Boolean.valueOf((body_map.get("overwrite")))));
		
		out.write(body_map.get("content") + "\n");
		out.close();
		
		this.unlockFile(filename);
		
		logger.addMessage("done writing to " + filename);
		//System.out.println("done writing to " + filename);
		return "The content has been written to the file";
	}
	
	
	// Method responsible for reading and parsing client requests
	// it returns request info [0], method[1], and path[2]
	public String[] readRequest(BufferedReader reader){
		boolean loop = true;
		String input_line = "";
		String complete_response ="";
		String request_method = "";
		String path = "";
		String accept = "";
		String body = "";
		String disposition_type = "none";
		
		int content_length = 0;
		
		try{
			while (loop) {
				{
					while (!(input_line = reader.readLine()).isEmpty()) {
						
						//get request method, path, and the rest of the header
						if(input_line.contains("HTTP") && request_method.equals("")){
							String[] info = input_line.split(" ");
							request_method = info[0];
							path = info[1];
						}
						
						if(input_line.contains("Accept")){
							accept = input_line.substring(input_line.indexOf(":")+2);
						}
						
						
						if(input_line.contains("content-length")){
							content_length = Integer.parseInt(input_line.substring(input_line.indexOf(":")+2));
							
						}
						
						if(input_line.contains("content-disposition")){
							disposition_type = input_line.substring(input_line.indexOf(";")+2);
						}
						
						complete_response = complete_response + input_line + "\n";
				      }
					
					loop = false;
					
					//check if the there is a body
					char[]  bufferChars = new char[content_length];
					
					//System.out.println("Reading "+ content_length + " bytes.");
					reader.read(bufferChars, 0, content_length);
					body = new String(bufferChars, 0, bufferChars.length); //to delete {} at the start and the end of the request
					logger.addMessage(body);
					//System.out.println(body); 
				}
			}
			
			logger.addMessage("----------Serving Request " + ServerThreadWorker.request_num++ + "-------------");
			//System.out.println("----------Serving Request " + ServerThreadWorker.request_num++ + "-------------");
		}	
		
		catch(Exception e){
			
		}
		finally{
			return new String[]{complete_response, request_method, path, accept, body, disposition_type};
		}
		
	}
	
	
	//Method to read the content of a passed path
	public synchronized String readPathContent(String path){
		
		//Step 1 : concatinate the path with server directory path
		FileSecurityLayer fsl = FileSecurityLayer.getFileSecurityLayerObject("");
		String complete_path = fsl.getServerDirectory() + path;
		
		//Step 2 : check if the path is leading to a file or a directory
		File fl = new File(complete_path);
		if(fl.isFile()){
			try {
				return this.readFile(complete_path);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//Step 3: if it is a directory, return list of files in that directory
		File[] file_list = fl.listFiles();
		String file_list_str = "";
		
		for(int i = 0; i < file_list.length; i++){
			if(file_list[i].isFile()){
				file_list_str = file_list_str + file_list[i].getName() + ", ";
			}
		}
		return file_list_str;
	}	
	
	
	//this method responsible for locking files
	private synchronized void lockFile(String filename){
		this.synchronizedFiles.put(filename.hashCode(), "any");
	}
	
	
	//this method responsible for unlocking files 
	private void unlockFile(String filename){
		this.synchronizedFiles.remove(filename.hashCode());
	}
	
	public void returnAttachment(String content, PrintWriter out){
		try {
			File myFile = File.createTempFile("tmep", ".txt");
			Map<String, String> map = new HashMap<String, String>();
			map.put("content", content);
			
			this.writeFile(myFile.getAbsolutePath(), map);
			
			char [] mybytearray  = new char [(int)myFile.length()];
			FileInputStream fileInput = null;
		
			fileInput = new FileInputStream(myFile);
		
			BufferedInputStream bufferOutput = new BufferedInputStream(fileInput);
			out.println(content);
		}
		
		catch (Exception e) {
			System.out.print("Error is raised in returnAttachment Method in the streamManagr");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  
	}
 	
}
