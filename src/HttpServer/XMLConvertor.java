package HttpServer;

public class XMLConvertor implements ConvertorInterface{
	
	static{
		ConvertorMapper.registerConvertor("*/*", new XMLConvertor());
	}
	@Override
	public String translate(String str) {
		// TODO Auto-generated method stub
		return null;
	}

}
