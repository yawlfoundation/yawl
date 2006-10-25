package au.edu.qut.yawl.engine.interfce;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

import junit.framework.TestCase;
import au.edu.qut.yawl.elements.YAWLServiceGateway;
import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YSpecification;

/**
 * this test will self-skip if theres no local yawl server running 
 * 
 * @author SandozM
 *
 */
public class TestInterfaceA_EnvironmentBasedClient extends TestCase {

	private static final String SUCCESS = "<success/>";
	private InterfaceA_EnvironmentBasedClient iaClient;
	private InterfaceB_EnvironmentBasedClient ibClient;
	private String aConnectionHandle;
	private String bConnectionHandle;
	private String text;
	
	protected void setUp() throws Exception {
		super.setUp();
		text = readFileAsString("exampleSpecs/xml/MakeRecordings(Beta4).xml");
	    iaClient = new InterfaceA_EnvironmentBasedClient("http://localhost:8080/yawl/ia");
	    ibClient = new InterfaceB_EnvironmentBasedClient("http://localhost:8080/yawl/ib");
	    aConnectionHandle = getAConnectionHandle();
	    bConnectionHandle = getBConnectionHandle();
		String result = iaClient.unloadSpecification("MakeRecordings", aConnectionHandle);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		String result = iaClient.unloadSpecification("MakeRecordings", aConnectionHandle);
		iaClient = null;
		ibClient = null;
	}

	private String getAConnectionHandle() throws IOException{
		return iaClient.connect("admin", "YAWL");
	}

	private String getBConnectionHandle() throws IOException{
		return ibClient.connect("admin", "YAWL");
	}

    private static String readFileAsString(String filePath)
    throws java.io.IOException{
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(
                new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }	

    public void testConnect() {
		try {
			getAConnectionHandle();
		} catch (IOException e) {
			System.out.println("Couldnt connect...");
		}
	}

	public void testSetYAWLServiceLifecycle() {
		YAWLServiceGateway gateway = new YAWLServiceGateway("april", new YSpecification("bobo"));
		String refName = "http://www.xyz.com/" + System.currentTimeMillis();
		YAWLServiceReference ref = new YAWLServiceReference(refName, gateway);
		try {
			String result1 = iaClient.setYAWLService(ref, aConnectionHandle);
			Set result = iaClient.getRegisteredYAWLServices(aConnectionHandle);
			boolean found = false;
			for (Object thing: result) {
				if ((((YAWLServiceReference) thing).getURI().equals(refName))) {
					found = true;
					break;
				}
			}
			if (!found) {fail("Should have been able to find service reference " + refName);}
			String result2 = iaClient.removeYAWLService(refName, aConnectionHandle);
			assertEquals(SUCCESS, result1);
			assertEquals(SUCCESS, result2);
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	public void testUploadSpecification() {
		try {
			//next line is cleanup - dont check its state...
			String result = iaClient.uploadSpecification(text, "MakeRecordings", aConnectionHandle);
			assertEquals(SUCCESS, result);
		} catch (Exception e) {
			System.out.println("Couldnt connect...");
		}
	}

	public void testLaunchCase() {
		try {
			String sessionHandle = bConnectionHandle;
			String result = iaClient.uploadSpecification(text, "MakeRecordings", sessionHandle);
			String results = ibClient.launchCase("MakeRecordings", "", bConnectionHandle);
			System.out.println("Launch test:" + results);
			String fin = ibClient.cancelCase(results, bConnectionHandle);
			System.out.println(result);
			System.out.println(results);
			System.out.println(fin);
			assertEquals(SUCCESS, result);
			assertEquals(SUCCESS, fin);
		} catch (IOException e) {
			System.out.println("Couldnt connect...");
		}
	}
}
