/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements.data;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;

import au.edu.qut.yawl.elements.YAWLServiceGateway;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.unmarshal.YMarshal;
import au.edu.qut.yawl.util.YMessagePrinter;


/**
 * @author Nathan Rose
 */
public class TestYParameter extends TestCase {
	private YParameter _param1;
	private YParameter _param2;
	private YSpecification _goodSpec;
	
	public TestYParameter(String name) {
		super(name);
	}
	
	public void setUp() throws YSchemaBuildingException, YSyntaxException, JDOMException, IOException, YPersistenceException {
        // create a couple simple params
        _param1 = new YParameter();
        _param2 = new YParameter();

        // read in a specification that has params
        File file1  = new File(getClass().getResource("ParametersGoodSpec.xml").getFile());
        _goodSpec = (YSpecification) YMarshal.unmarshalSpecifications(file1.getAbsolutePath()).get(0);
    }
	
	public void testYParameterConstructors() {
		try {
			new YParameter(null, YParameter._ENABLEMENT_PARAM_TYPE);
			new YParameter(null, YParameter._INPUT_PARAM_TYPE);
			new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
		}
		catch(IllegalArgumentException e) {
			e.printStackTrace();
			fail(e.toString());
		}
		try {
			new YParameter(null,
					Math.max(Math.max(YParameter._ENABLEMENT_PARAM_TYPE, YParameter._INPUT_PARAM_TYPE),
							YParameter._OUTPUT_PARAM_TYPE) + 1);
			fail("YParameter was created with an invalid type, which shouldn't happen");
		}
		catch(IllegalArgumentException e) {
			// proper exception was thrown
		}
		try {
			new YParameter(null,
					Math.min(Math.min(YParameter._ENABLEMENT_PARAM_TYPE, YParameter._INPUT_PARAM_TYPE),
							YParameter._OUTPUT_PARAM_TYPE) - 1);
			fail("YParameter was created with an invalid type, which shouldn't happen");
		}
		catch(IllegalArgumentException e) {
			// proper exception was thrown
		}
		
		try {
			new YParameter(new YDecomposition(), "inputParam");
			new YParameter(new YDecomposition(), "outputParam");
			new YParameter(new YDecomposition(), "enablementParam");
		}
		catch(IllegalArgumentException e) {
			e.printStackTrace();
			fail(e.toString());
		}
		try {
			new YParameter(new YDecomposition(), "bad input string");
			fail("YParameter was created with an invalid type, which shouldn't be allowed");
		}
		catch(IllegalArgumentException e) {
			// proper exception was thrown
		}
	}
	
	public void testCutThroughParameter() {
		_param1 = new YParameter(new YDecomposition(), YParameter._INPUT_PARAM_TYPE);
		_param2 = new YParameter(new YDecomposition(), YParameter._OUTPUT_PARAM_TYPE);
		
		try {
			_param1.setIsCutThroughParam(true);
			fail("Should not be allowed to set cutThroughParam property for an input parameter");
		}
		catch(IllegalArgumentException e) {
			// proper exception was thrown
		}
		
		_param2.setIsCutThroughParam(true);
		
		String xml = _param2.toString();
		
		SAXBuilder builder = new SAXBuilder();
		try {
			Document d = builder.build(new StringReader(xml));
			assertNotNull(d);
			assertNotNull(d.getContent());
			Element param = (Element) d.getContent().get(0);
			assertNotNull(param);
			assertNotNull(param.getContent());
			Element child = (Element) param.getContent().get(0);
			assertNotNull(child);
			assertTrue(child.getName(), child.getName().equals("bypassesStatespaceForDecomposition"));
		}
		catch( JDOMException e ) {
			e.printStackTrace();
			fail(e.toString());
		}
		catch( IOException e ) {
			e.printStackTrace();
			fail(e.toString());
		}
	}
	
	public void testToXML() {
		_param1 = new YParameter(new YDecomposition(), YParameter._INPUT_PARAM_TYPE);
		_param2 = new YParameter(new YDecomposition(), YParameter._INPUT_PARAM_TYPE);
		
		_param1.setAttributes(new HashMap<String,String>());
		_param1.addAttribute("fakeAttr1", "aValue");
		_param1.addAttribute("fakeAttr2", "anotherValue");
		_param2.addAttribute("blankAttr", "");
		
		String xml1 = _param1.toXML();
		String xml2 = _param2.toXML();
		
		SAXBuilder builder = new SAXBuilder();
		try {
			Document d = builder.build(new StringReader(xml1));
			List<Attribute> attributes = ((Element)d.getContent().get(0)).getAttributes();
			assertNotNull(attributes);
			assertTrue("attributes.size():" + attributes.size(), attributes.size() == 2);
			Map<String, Attribute> attributeMap = new HashMap<String, Attribute>();
			attributeMap.put(attributes.get(0).getName(), attributes.get(0));
			attributeMap.put(attributes.get(1).getName(), attributes.get(1));
			assertNotNull(attributeMap.get("fakeAttr1"));
			assertTrue(attributeMap.get("fakeAttr1").getValue(), attributeMap.get("fakeAttr1").getValue().equals("aValue"));
			assertNotNull(attributeMap.get("fakeAttr2"));
			assertTrue(attributeMap.get("fakeAttr2").getValue(), attributeMap.get("fakeAttr2").getValue().equals("anotherValue"));
		}
		catch( JDOMException e ) {
			e.printStackTrace();
			fail(e.toString());
		}
		catch( IOException e ) {
			e.printStackTrace();
			fail(e.toString());
		}
		
		builder = new SAXBuilder();
		try {
			Document d = builder.build(new StringReader(xml2));
			List<Attribute> attributes = ((Element)d.getContent().get(0)).getAttributes();
			assertNotNull(attributes);
			assertTrue("attributes.size():" + attributes.size(), attributes.size() == 1);
			assertTrue(attributes.get(0).getName().equals("blankAttr"));
			assertTrue(attributes.get(0).getValue().equals(""));
		}
		catch( JDOMException e ) {
			e.printStackTrace();
			fail(e.toString());
		}
		catch( IOException e ) {
			e.printStackTrace();
			fail(e.toString());
		}
	}
	
	public void testSummaryXML() {
		_param1 = new YParameter(new YDecomposition(), "inputParam");
		_param1.setName("Param1");
		_param2 = new YParameter(new YDecomposition(), "outputParam");
		String xml1 = _param1.toSummaryXML();
		String xml2 = _param2.toSummaryXML();
		
		SAXBuilder builder = new SAXBuilder();
		try {
			Document d = builder.build(new StringReader(xml1));
			Element root = (Element) d.getContent().get(0);
			assertNotNull(root);
			assertTrue(root.getName(), root.getName().equals("inputParam"));
			Map<String, Element> children = new HashMap<String, Element>();
			for(Element child : (List<Element>) root.getContent())
				children.put(child.getName(), child);
			assertTrue(children.size() == 2);
			assertTrue(children.containsKey("name"));
			assertTrue(children.containsKey("type"));
			Element child = children.get("name");
			Text t = (Text) child.getContent().get(0);
			assertTrue(t.getText(), t.getText().equals("Param1"));
			child = children.get("type");
			t = (Text) child.getContent().get(0);
			assertTrue(t.getText(), t.getText().equals("null"));
		}
		catch( JDOMException e ) {
			e.printStackTrace();
			fail(e.toString());
		}
		catch( IOException e ) {
			e.printStackTrace();
			fail(e.toString());
		}
		
		builder = new SAXBuilder();
		try {
			Document d = builder.build(new StringReader(xml2));
			Element root = (Element) d.getContent().get(0);
			assertNotNull(root);
			assertTrue(root.getName(), root.getName().equals("outputParam"));
			Map<String, Element> children = new HashMap<String, Element>();
			for(Element child : (List<Element>) root.getContent())
				children.put(child.getName(), child);
			assertTrue(children.size() == 0);
			assertFalse(children.containsKey("name"));
			assertFalse(children.containsKey("type"));
			Element child = children.get("name");
		}
		catch( JDOMException e ) {
			e.printStackTrace();
			fail(e.toString());
		}
		catch( IOException e ) {
			e.printStackTrace();
			fail(e.toString());
		}
	}
	
	public void testVerify() {
		try {
			_param1.verify();
			fail("Should have thrown a null pointer exception");
		}
		catch(NullPointerException e) {
			// proper exception was thrown
		}
		
		List<YParameter> inParams = _goodSpec.getDecomposition("Root").getInputParameters();
		assertNotNull(inParams);
		assertTrue(inParams.size() == 2);
		
		for(YParameter param : inParams) {
			List messages = param.verify();
			assertNotNull("" + param.getID(), messages);
			assertTrue(param.getName() + "\n" +
					YMessagePrinter.getMessageString(messages), messages.size() == 0);
		}
		
		YParameter yparam = inParams.get(0);
		yparam.setManadatory(true);
		List msgs = yparam.verify();
		assertNotNull(msgs);
		assertTrue("" + msgs.size(), msgs.size() > 0);
		
		
		List<YParameter> outParams = _goodSpec.getDecomposition("Root").getOutputParameters();
		assertNotNull(outParams);
		assertTrue(outParams.size() == 2);
		
		for(YParameter param : outParams) {
			List messages = param.verify();
			assertNotNull("" + param.getID(), messages);
			assertTrue(param.getName() + "\n" +
					YMessagePrinter.getMessageString(messages), messages.size() == 0);
		}
		
		Collection<YVariable> enParams =
			((YAWLServiceGateway)_goodSpec.getDecomposition("SignOff")).getEnablementParametersMap().values();
		
		for(YVariable var : enParams) {
			YParameter param = (YParameter) var;
			List messages = param.verify();
			assertNotNull("" + param.getID(), messages);
			assertTrue(param.getName() + "\n" +
					YMessagePrinter.getMessageString(messages), messages.size() == 0);
		}
	}
	
//	public void testCompareTo() {
//		_param1.setOrdering(1);
//		_param2.setOrdering(2);
//		assertTrue(_param1.compareTo(_param2) < 0);
//		assertTrue(_param2.compareTo(_param1) > 0);
//		assertTrue(_param1.compareTo(_param1) == 0);
//		_param2.setOrdering(1);
//		assertTrue(_param1.compareTo(_param2) == 0);
//	}
	
//	private static String collectionToString(Collection c) {
//		String str = "[";
//		int count = 0;
//		Iterator iter = c.iterator();
//		Object o;
//		while(iter.hasNext()) {
//			if(count > 0)
//				str += ", ";
//			count += 1;
//			o = iter.next();
//			str += (o instanceof Collection) ? collectionToString((Collection)o) : o.toString();
//		}
//		str += "]";
//		return str;
//	}
}
