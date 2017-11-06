package HttpServer;

import org.json.simple.JSONObject;

public class JSONConvertor implements ConvertorInterface {
	
	static{
		ConvertorMapper.registerConvertor("application/json", new JSONConvertor());
	}
	
	@Override
	public String translate(String str) {
		JSONObject json = new JSONObject();
		json.put("Content", str);
		return json.toString();
	}

}
