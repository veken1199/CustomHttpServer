package HttpServer;

import java.io.File;
import java.nio.file.*;

public class FileSecurityLayer {

	private String mainDirectory;
	private static FileSecurityLayer securityLayer;
	
	
	public static FileSecurityLayer getFileSecurityLayerObject(String directoryPath){
		if(securityLayer == null){
			securityLayer = new FileSecurityLayer(directoryPath);
		}
		
		return securityLayer;
	}
	
	private FileSecurityLayer(String directoryPath){
		this.mainDirectory = directoryPath;
	}
	
	
	//Method responsible to validate if we can access a directory
	public Boolean isPathAccessable(String directory){
		
		File directory1 = new File(this.mainDirectory + directory);
		
		if(directory1.exists()){
			return true;
		}
			
		else{
			System.out.println("illegal access to " + directory1);
			return false;
		}
	}

	public String getServerDirectory() {
		return this.mainDirectory;
	}
	
	

}
