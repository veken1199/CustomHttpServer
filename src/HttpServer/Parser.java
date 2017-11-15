package HttpServer;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import static  java.util.Arrays.asList;


public class Parser {
	
	private OptionParser parser;
	public OptionSet opts;
	private Boolean verbose;
	private FileSecurityLayer security_layer;
	private RequestsManager requests_manager;
	
	// Constructor
	public Parser(String[] args){
		
		this.parser = new OptionParser();
		this.requests_manager = new RequestsManager();
		
		// Populating the commands list
		this.createCommands();
		
		// Passing java arguements into the parser
		this.opts = parser.parse(args);
		
		
		// Check if the verbose is requested
		this.verbose = opts.has("v");	
	}
	
	
	// Method responsible for creating the commands 
	private void createCommands(){

		parser.acceptsAll(asList("help", "h"), "Server help")
        .withOptionalArg();
		
		parser.acceptsAll(asList("p", "port"), "Server Port")
        .withOptionalArg()
        .ofType( Integer.class )
		.defaultsTo(8007);
        
		parser.acceptsAll(asList("d", "dir", "directory"), "Server Directory")
        .withOptionalArg()
		.defaultsTo(System.getProperty("user.dir"));
		
		
		parser.acceptsAll(asList("v","verbose"), "Gereral usage")
        .withOptionalArg()
        .ofType( Boolean.class )
        .defaultsTo(true);
	}
	
	
	// This Method responsible for handling and processing the commands
	public void handleCommands(){
		
		// Handling Help commands
		if (opts.has("help")){
			help();
		}
		
		else{
			
			System.out.println("Checking server directory....");
			
			this.security_layer = FileSecurityLayer.getFileSecurityLayerObject(this.opts.valueOf("d").toString());
			
			if(this.security_layer.isPathAccessable("")){
				System.out.println("The server is staring on port: "+ this.opts.valueOf("p").toString());
				System.out.println("The server has access to: " + this.opts.valueOf("d").toString());
				
				Logger.ALLOWLOGGING = (boolean)this.opts.valueOf("v");
				
				if((int)this.opts.valueOf("p") == 8007){
					this.requests_manager = new UDPRequestsManager();
				}
				
				this.requests_manager.startServer((int)this.opts.valueOf("p"));
			}
			
			else {
				System.out.println("\nThe server directory is not availabe, make sure you have inserted a valid directory");
			}
		}
	}
	
	// Displaying help info
	public void help(){
		System.out.println("httpf is a simple http server, serves requests to localhost\n");
		System.out.println("Usage: httpf 'command' [arguments]. The commands are:");
		System.out.println("-p \t localhost port");
		System.out.println("-d \t server workdirectory");
		System.out.println("-v \t <boolean> verbose");
		System.out.println("Use 'httpf -help ");
	}	
}

	
