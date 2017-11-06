package Tests;

import static org.junit.Assert.*;
import java.nio.file.Path;
import javax.naming.spi.DirectoryManager;
import HttpServer.FileSecurityLayer;
import org.junit.Test;


public class FIleSecurityLayerTest {

	@Test
	public final void fileExistsTest(){
		FileSecurityLayer securityLayer = FileSecurityLayer.getFileSecurityLayerObject("C:/Users/BABO99/Desktop/src/src/resources/");
		assertEquals(securityLayer.isPathAccessable("foo.text"), true);
		assertEquals(securityLayer.isPathAccessable(""), true);
	}

}
