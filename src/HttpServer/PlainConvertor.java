package HttpServer;

public class PlainConvertor implements ConvertorInterface {

	static{
		ConvertorMapper.registerConvertor("*/*", new PlainConvertor());
		ConvertorMapper.registerConvertor("", new PlainConvertor());
	}
	
	@Override
	public String translate(String str) {
		return str;
	}

}
