package dk.aau.cs.giraf.launcher.test;

import android.content.Context;
import android.test.AndroidTestCase;
import dk.aau.cs.giraf.launcher.Tools;
//import android.test.mock.MockContext;

public class ToolsTest extends AndroidTestCase {
	
	@Override
	public void setUp() {
		
	}
	
	public void testisLandscape() {
		Context context = getContext();
		assertEquals(true, Tools.isLandscape(context));
	}
	
	public void testintToDP() {
		Context context = getContext();
		int input;
		int expectedOutput;
		int actualOutput;
		
		input = 100;
		expectedOutput = 100;
		actualOutput = Tools.intToDP(context, input);
		assertEquals(expectedOutput, actualOutput);
		
		input = 30;
		expectedOutput = 30;
		actualOutput = Tools.intToDP(context, input);
		assertEquals(expectedOutput, actualOutput);
	}

}
