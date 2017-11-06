package HttpServer;

import java.util.HashMap;

public class ConvertorMapper {
	
	private ConvertorInterface convertor;
	private static HashMap<String, ConvertorInterface> convertors = new HashMap<String, ConvertorInterface>();
	
	public static void registerConvertor(String type, ConvertorInterface conv){
		convertors.put(type,conv);
	}
	
	//mini strategy method
	public String convert(String data, String type){
		if(!convertors.containsKey(type)){
			return convertors.get("*/*").translate(data);
		}
		return convertors.get(type).translate(data);
	}
}

