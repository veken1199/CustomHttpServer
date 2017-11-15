package HttpServer;

import java.util.HashMap;
import java.util.Map;

public class Utilities {
	// Method responsible for convertion json like string to map 
		// in a key-value form
		public static Map<String, String> mapConverter(String str){
			Map<String, String> map = new HashMap<String, String>();
			
			// Step 0 - return empty map if the parameter is empty
			if(str.length()==0){
				return map;
			}
			
			if(str.charAt(0)=='{'){
				str = str.substring(1,str.length()-1);
			}
			
			// Step 1 - breaking {x:y , k:l} into array of strings where comma appears
			String[] items = str.split(",");
			
			// Step 2 - Break each item into key and value and store it in the map
			for(String item : items){
				String str_parsed = "";
				
				// Step 0 - remove ""
				for(int x = 0; x < item.length(); x++){
					if(item.charAt(x) == '"'){
						continue;
					}
					else{
						str_parsed = str_parsed + item.charAt(x);
					}
				}
				
				String[] arr = str_parsed.split(":");
				map.put(arr[0], arr[1]);
			}
			
			// Step 3 - Return the map 
			return map;
		}
		
		// Method reponsible for parsing directory into path and filename
		public static String[] parsePath(String str){
			
			String[] pathArray = {};
			String filename = "";
			String path = "";
			
			//Step 1 find '/' if does exists
			if(str.contains("/")){
				pathArray = str.split("/");
				
				if (pathArray.length == 0){
					path = "/";
				}
			}
			
			//Step 2 create a path 
			for(int i = 0; i<pathArray.length; i++){
				
				if(!pathArray[i].contains(".")){
					path = path + pathArray[i] + "/";
				}
				
				else{
					filename = pathArray[i];
				}
			}
			
			return new String[]{path, filename};
		}
		
		
			
}
