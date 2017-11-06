package HttpServer;

import java.nio.file.Path;

public class Httpf {
	static
	{
		try
		{
			Class.forName("HttpServer.JSONConvertor");
			Class.forName("HttpServer.PlainConvertor");
		}
		catch (ClassNotFoundException any)
		{
			any.printStackTrace();
		}
	}
	public static void main(String[] args) {
		
		// TODO Auto-generated method stub
		Parser parser = new Parser(args);
		parser.handleCommands();
		
		//RequestsManager req = new RequestsManager();
		
		System.getProperty("user.dir");
		
		System.out.print(System.getProperty("user.dir"));
		//req.startServer(8000, "asd");
	}

}
