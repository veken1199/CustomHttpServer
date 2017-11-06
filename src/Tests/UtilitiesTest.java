package Tests;

import static org.junit.Assert.*;
import HttpServer.Utilities;
import org.junit.Test;

public class UtilitiesTest {
	
	@Test 
	public final void parsePathTest(){
		Utilities u = new Utilities();
		assertEquals(u.parsePath("/path1/path2/f1.test")[1], "f1.test");
		assertEquals(u.parsePath("/path1/path2/f1.test")[0], "/path1/path2/");
		
		assertEquals(u.parsePath("/path1/path2/")[0], "/path1/path2/");
		assertEquals(u.parsePath("/path1/path2/")[1], "");
		
		assertEquals(u.parsePath("/path1/path2/f1")[0], "/path1/path2/f1/");
		
		System.out.print(u.parsePath("/")[0]);
		assertEquals(u.parsePath("/")[0], "/");
	}

}
