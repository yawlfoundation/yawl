package org.yawlfoundation.yawl.scheduling;

import junit.framework.TestCase;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceCalendarGatewayClient;
import org.yawlfoundation.yawl.scheduling.resource.ResourceServiceInterface;
import org.yawlfoundation.yawl.scheduling.util.Utils;

import java.util.Date;


public class TestCalendarManager extends TestCase implements Constants {
	private static Logger logger = LogManager.getLogger(TestCalendarManager.class);

	ResourceCalendarGatewayClient client;
	String handle;

	@Before
	public void setUp() {
        ResourceServiceInterface rsi = ResourceServiceInterface.getInstance();
//		try {
//			client = rsi
//			handle = config.getSessionHandle_Cal();
//		} catch (IOException e) {
//			logger.error("", e);
//		}
	}

	@After
	public void tearDown() {

	}

	@Test
	public void testJUnit() throws Exception {
		//assert (false) : "JUnit4 geht";
		assertTrue(false);
	}

	/**
	 * Thomas Becker lokal: PA-d9ea95e8-49d0-4bea-87e5-393738b11016
	 * Thomas Becker patty: PA-e371924c-bcd2-4384-bf25-a4a6d91318f2
	 */
	private String getResourceId_PA() {
		return "PA-e371924c-bcd2-4384-bf25-a4a6d91318f2";
	}

	private String getXML_Resource_PA() {
		return "<Resource><Id>"+getResourceId_PA()+"</Id><Role/>"+
			"<Capability/><"+XML_CATEGORY+"/><"+XML_SUBCATEGORY+"/></Resource>";
	}

	private String getXML_Resource_Role() {
		return "<Resource><Id></Id><Role>Nurse</Role>" +
			"<Capability/><"+XML_CATEGORY+"/><"+XML_SUBCATEGORY+"/></Resource>";
	}

	/**
	 * test lokal:
	 * test patty: NH-557c9718-ee5c-415c-b1b8-9f120c39bf6f
	 */
	private String getResourceId_NA() {
		return "NH-557c9718-ee5c-415c-b1b8-9f120c39bf6f";
	}

	private String getXML_Resource_NH() {
		return "<Resource><Id>"+getResourceId_NA()+"</Id><Role/>"+
			"<Capability/><"+XML_CATEGORY+"/><"+XML_SUBCATEGORY+"/></Resource>";
	}

	private String getXML_Resource_NH(String nhr) {
		return "<Resource><Id>"+nhr+"</Id><Role/>"+
			"<Capability/><"+XML_CATEGORY+"/><"+XML_SUBCATEGORY+"/></Resource>";
	}

	private String getXML_Resource_Category() {
		return "<Resource><Id></Id><Role></Role><Capability>" +
			"</Capability><"+XML_CATEGORY+">device</"+XML_CATEGORY+"><"+XML_SUBCATEGORY+"/></Resource>";
	}

	private String getXML_RUP() {
		return "<"+XML_RUP+"><CaseId>201</CaseId><"+XML_ACTIVITY+">" +
			"<"+XML_ACTIVITYNAME+">SurgicalProcedure</"+XML_ACTIVITYNAME+">"+
			"<"+XML_REQUESTTYPE+">POU</"+XML_REQUESTTYPE+">"+
			"<From>2010-05-11T16:30:00.000</From><To>2010-05-11T17:30:00.000</To>" +

	    "<Reservation><StatusToBe>Reserved</StatusToBe><Status />"+getXML_Resource_PA()+
	    "<Workload>90</Workload></Reservation>" +

	    "<Reservation><StatusToBe>Requested</StatusToBe><Status />"+getXML_Resource_Role()+
	    "<Workload>90</Workload></Reservation>" +

	    "<Reservation><StatusToBe>Requested</StatusToBe><Status />"+getXML_Resource_NH()+
	    "<Workload>90</Workload></Reservation>" +

	    "<Reservation><StatusToBe>Reserved</StatusToBe><Status />"+getXML_Resource_Category()+
	    "<Workload>90</Workload></Reservation>" +

//	    "<"+XML_UTILISATIONREL+"><ThisUtilisationType>EOU</ThisUtilisationType>" +
//	    "<OtherUtilisationType>SOU</OtherUtilisationType>" +
//	    "<OtherActivityName>Extubation</OtherActivityName><Min>0</Min><Max>999999</Max>" +
//	    "</"+XML_UTILISATIONREL+">" +

	    "</"+XML_ACTIVITY+"></"+XML_RUP+">";
	}

	@Test
  public void testSaveReservations() {
		try {
			String rupStr = client.saveReservations(getXML_RUP(), false, handle);
			logger.debug("rupStr:\r\n" + Utils.element2String(Utils.string2Element(rupStr), true));
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	@Test
  public void testGetAvailability() {
		try {
			Date from = Utils.string2Date("21.03.2011", Utils.DATE_PATTERN);
			Date to = Utils.string2Date("22.03.2011", Utils.DATE_PATTERN);
			String rupStr = client.getAvailability(getXML_Resource_NH("NH-e1ad4ca9-4f1b-4d0e-8c2b-24d649545964"), from, to, handle);
			logger.debug("rupStr:\r\n" + Utils.element2String(Utils.string2Element(rupStr), true));
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	@Test
  public void testGetResourceAvailability() {
		try {
			Date from = Utils.string2Date("10.05.2010", Utils.DATE_PATTERN);
			Date to = Utils.string2Date("12.05.2010", Utils.DATE_PATTERN);
			String rupStr = client.getResourceAvailability(getResourceId_PA(), from, to, handle);
			logger.debug("rupStr:\r\n" + Utils.element2String(Utils.string2Element(rupStr), true));
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	@Test
  public void testGetReservations() {
		try {
			Date from = Utils.string2Date("10.05.2010", Utils.DATE_PATTERN);
			Date to = Utils.string2Date("12.05.2010", Utils.DATE_PATTERN);
			String rupStr = client.getReservations(getXML_Resource_PA(), from, to, handle);
			logger.debug("rupStr:\r\n" + Utils.element2String(Utils.string2Element(rupStr), true));
		} catch (Exception e) {
			logger.error("", e);
		}
	}

}
