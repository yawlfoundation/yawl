/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.engine.interfce;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import au.edu.qut.yawl.elements.YAWLServiceGateway;
import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.dao.restrictions.NegatedRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction.Comparison;

/**
 * this test will self-skip if theres no local yawl server running 
 * 
 * @author SandozM
 *
 */
public class TestInterfaceA_EnvironmentBasedClient extends TestCase {

	private static final String MAKE_RECORDINGS = "JythonSpecJaxb4.xml";
	private static final String SUCCESS = "<success/>";
	private InterfaceA_EnvironmentBasedClient iaClient;
	private InterfaceB_EnvironmentBasedClient ibClient;
	private String aConnectionHandle;
	private String bConnectionHandle;
	private String text;
	
	protected void setUp() throws Exception {
		super.setUp();
		text = readFileAsString("exampleSpecs/xml/JythonSpecJaxb4.xml");
	    iaClient = new InterfaceA_EnvironmentBasedClient("http://localhost:8080/yawl/ia");
	    ibClient = new InterfaceB_EnvironmentBasedClient("http://localhost:8080/yawl/ib");
	    try {
	    aConnectionHandle = getAConnectionHandle();
	    bConnectionHandle = getBConnectionHandle();
		String result = iaClient.unloadSpecification(MAKE_RECORDINGS, aConnectionHandle);
	    } catch(ConnectException e) {} //leave them null if not able to connect
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		try {
			String result = iaClient.unloadSpecification(MAKE_RECORDINGS, aConnectionHandle);
		} catch (ConnectException e) {
		}
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

    public void testConnect() throws IOException {
		try {
			getAConnectionHandle();
		} catch (ConnectException e) {
			System.out.println("Couldnt connect...");
		}
	}

	public void testSetYAWLServiceLifecycle() throws IOException {
		YAWLServiceGateway gateway = new YAWLServiceGateway("april", new YSpecification("bobo"));
		String refName = "http://www.xyz.com/" + System.currentTimeMillis();
		YAWLServiceReference ref = new YAWLServiceReference(refName, gateway);
		try {
			String result1 = iaClient.setYAWLService(ref, aConnectionHandle);
			Set result = iaClient.getRegisteredYAWLServices(aConnectionHandle);
			boolean found = false;
			for (Object thing: result) {
				System.out.println(((YAWLServiceReference) thing).getURI().toString());
				if ((((YAWLServiceReference) thing).getURI().equals(refName))) {
					found = true;
					break;
				}
			}
			if (!found) {fail("Should have been able to find service reference " + refName);}
			String result2 = iaClient.removeYAWLService(refName, aConnectionHandle);
			assertEquals(SUCCESS, result1);
			assertEquals(SUCCESS, result2);
		} catch (ConnectException e) {
			System.out.println("couldn't connect");
		}
	}

	public void testUploadSpecification() throws IOException {
		try {
			//next line is cleanup - dont check its state...
			String result = iaClient.uploadSpecification(text, MAKE_RECORDINGS, aConnectionHandle);
			assertEquals(SUCCESS, result);
		} catch (ConnectException e) {
			System.out.println("Couldnt connect...");
		}
	}
	
	public void testRetrieveByRestriction1() throws Exception {
		try {
			String result = iaClient.uploadSpecification(text, MAKE_RECORDINGS, aConnectionHandle);
			assertEquals(SUCCESS, result);
			List<YSpecification> specs = ibClient.getSpecificationsByRestriction(
					new PropertyRestriction("ID", Comparison.LIKE, "Jython%"), bConnectionHandle );
			Map<String,YSpecification> specMap = new HashMap<String, YSpecification>();
			String ids = "Specifiaction URIs:\n";
			for( YSpecification spec : specs ) {
				ids += spec.getID() + "\n";
				specMap.put( spec.getID(), spec );
			}
			
			assertTrue( "" + specs.size(), specs.size() >= 1 );
			assertTrue( ids, specMap.containsKey( MAKE_RECORDINGS ) );
		}
		catch( ConnectException e ) {
			System.out.println("couldn't connect");
		}
	}
	
	public void testRetrieveByRestriction2() throws Exception {
		try {
			String result = iaClient.uploadSpecification(text, MAKE_RECORDINGS, aConnectionHandle);
			assertEquals(SUCCESS, result);
			List<YSpecification> specs = ibClient.getSpecificationsByRestriction(
				new NegatedRestriction( new PropertyRestriction( "ID", Comparison.LIKE, "Jython%" ) ),
				bConnectionHandle );
			Map<String,YSpecification> specMap = new HashMap<String, YSpecification>();
			String ids = "Specification URIs:\n";
			for( YSpecification spec : specs ) {
				ids += spec.getID() + "\n";
				specMap.put( spec.getID(), spec );
			}
			
			assertFalse( ids, specMap.containsKey( MAKE_RECORDINGS ) );
		}
		catch( ConnectException e ) {
			System.out.println("couldn't connect");
		}
	}

	public void testLaunchCase() throws IOException {
		try {
			String sessionHandle = bConnectionHandle;
			String result = iaClient.uploadSpecification(text, MAKE_RECORDINGS, sessionHandle);
			String results = ibClient.launchCase(MAKE_RECORDINGS, "", bConnectionHandle);
			System.out.println("Launch test:" + results);
//			try {
//				Thread.sleep(15000);
//			} catch (InterruptedException e) {}
//			String fin = ibClient.cancelCase(results, bConnectionHandle);
//			System.out.println(result);
//			System.out.println(results);
//			System.out.println(fin);
			assertEquals(SUCCESS, result);
//			assertEquals(SUCCESS, fin);
		} catch (ConnectException e) {
			System.out.println("Couldnt connect...");
		}
	}
}
