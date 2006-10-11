package au.edu.qut.yawl.engine.interfce;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Set;

import au.edu.qut.yawl.elements.YAWLServiceGateway;
import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YSpecification;

import junit.framework.TestCase;

public class TestInterfaceA_EnvironmentBasedClient extends TestCase {

	private InterfaceA_EnvironmentBasedClient iaClient;
	private InterfaceB_EnvironmentBasedClient ibClient;
	private String aConnectionHandle;
	private String bConnectionHandle;
	
	protected void setUp() throws Exception {
		super.setUp();
	    iaClient = new InterfaceA_EnvironmentBasedClient("http://localhost:8080/yawl/ia");
	    ibClient = new InterfaceB_EnvironmentBasedClient("http://localhost:8080/yawl/ib");
	    aConnectionHandle = getAConnectionHandle();
	    bConnectionHandle = getBConnectionHandle();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
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
			fail(e.getMessage());
		}
	}

	public void testGetRegisteredYAWLServices() {
		String sessionHandle = aConnectionHandle;
		Set result = iaClient.getRegisteredYAWLServices(sessionHandle);
		for (Object thing: result) {
			System.out.println(thing);
		}
	}

	public void testSetYAWLService() {
		String sessionHandle = aConnectionHandle;
		YAWLServiceGateway gateway = new YAWLServiceGateway("april", new YSpecification("bobo"));
		YAWLServiceReference ref = new YAWLServiceReference("http://www.xyz.com/april", gateway);
		try {
			String result = iaClient.setYAWLService(ref, sessionHandle);
			System.out.println(result);
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	public void testRemoveYAWLService() {
		fail("Not yet implemented");
	}

	public void testUploadUnloadSpecification() {
		try {
			String sessionHandle = aConnectionHandle;
			String text = readFileAsString("exampleSpecs/xml/Maketrip1.xml");
			String result = iaClient.uploadSpecification(text, "Maketrip1.xml", sessionHandle);
			String result2 = iaClient.unloadSpecification("Maketrip1.xml", sessionHandle);
			System.out.println(result);
			System.out.println(result2);
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	public void test() {
//		try {
//			String result = ibClient.launchCase("Maketrip1.xml", "", bConnectionHandle);
//			System.out.println(result);
//		} catch (IOException e) {
//			fail(e.getMessage());
//		}
	}
}
