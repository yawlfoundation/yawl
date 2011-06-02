package org.yawlfoundation.yawl.schema;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.xerces.parsers.DOMParser;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xsd.*;
import org.eclipse.xsd.impl.XSDModelGroupImpl;
import org.eclipse.xsd.impl.XSDParticleImpl;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.yawlfoundation.yawl.exceptions.YSchemaBuildingException;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;


/**
 * 
 * @author Lachlan Aldred
 * Date: 6/08/2004
 * Time: 16:48:26
 * 
 */
public class TestXSD4YAWLBuilder extends TestCase {
    private XSD4YAWLBuilder _xsd4YAWLBuilder;


    public void setUp() throws ParserConfigurationException, SAXException, IOException {
        Document document = null;
        DOMParser parser = new DOMParser();
        try {
            URL url = getClass().getResource("SelfContainedPerson.xsd");
            String fn = url.getFile();
            parser.parse(fn);
            document = parser.getDocument();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            _xsd4YAWLBuilder = new XSD4YAWLBuilder();
            _xsd4YAWLBuilder.setSchema(document.getDocumentElement());
        } catch (YSyntaxException e) {
            e.printStackTrace();
        }
    }

    public void testXSDLibCreation() {
        assertNotNull(_xsd4YAWLBuilder);
    }


    public void testCopyNode() {
        XSDTypeDefinition type = _xsd4YAWLBuilder.getTypeDefinition("PersonType");
        assertNotNull(type);
        assertTrue(type.getName().equals("PersonType"));
    }


    public void testCanGetStateType() {
//        <xs:complexType name="PersonType">
//            <xs:sequence>
//                <xs:element name="age" personType="xs:nonNegativeInteger"/>
//                <xs:element ref="address"/>
//                <xs:element name="name2" personType="xs:string"/>
//            </xs:sequence>
//        </xs:complexType>
        XSDTypeDefinition personType = _xsd4YAWLBuilder.getTypeDefinition("PersonType");
        XSDComplexTypeDefinition complexPersonType = (XSDComplexTypeDefinition) personType;
        EList attributes = complexPersonType.getAttributeContents();
        for (int i = 0; i < attributes.size(); i++) {
            XSDAttributeUse attribute = (XSDAttributeUse) attributes.get(i);

            XSDAttributeDeclaration attDecl = attribute.getAttributeDeclaration();
            String attName = attDecl.getName();
            assertTrue(attName.equals("maritalStatus") || attName.equals("isParent"));
            XSDSimpleTypeDefinition attType = attDecl.getTypeDefinition();
            assertTrue("Attname(): " + attType.getName(),
                    attType.getName().equals("MaritalStatusType") ||
                    attType.getName().equals("boolean"));
        }
        XSDComplexTypeContent content = complexPersonType.getContent();
        XSDParticleImpl particle1 = (XSDParticleImpl) content;
        EList contents = particle1.eContents();
        XSDParticleContent particleContent = particle1.getContent();
        assertNotNull(particleContent);
        for (int i = 0; i < contents.size(); i++) {
            XSDModelGroupImpl modelGroup = (XSDModelGroupImpl) contents.get(i);
            EList elements = modelGroup.getContents();
            XSDCompositor compositor = modelGroup.getCompositor();
            assertNotNull(compositor);
            for (int j = 0; j < elements.size(); j++) {
                XSDParticle particle = (XSDParticle) elements.get(j);
                EList contents2 = particle.eContents();
                for (int k = 0; k < contents2.size(); k++) {
                    Object o = contents2.get(k);
                    assertNotNull(o);
                }
                XSDParticleContent pContent = particle.getContent();
                if (pContent instanceof XSDElementDeclaration) {
                    XSDElementDeclaration elementDecl = (XSDElementDeclaration) pContent;
                    XSDTypeDefinition tDef = elementDecl.getTypeDefinition();
                    String elementStr = XSDUtil.convertToString(elementDecl);

                    assertTrue("Element: " + elementStr, tDef != null
                            || elementDecl.isElementDeclarationReference());

                    XSDElementDeclaration refElem = elementDecl.getResolvedElementDeclaration();
                    if (refElem.getName().equals("address")) {
                        XSDComplexTypeDefinition addressType = (XSDComplexTypeDefinition) refElem.getTypeDefinition();
                        assertNotNull(addressType);
                    }
                }
            }
        }
    }


    public void testGetClonedGlobalDefinitions_String() {
        Set globalDefs = _xsd4YAWLBuilder.getGlobalDefinitionsForType("PersonType");
        for (Iterator iterator = globalDefs.iterator(); iterator.hasNext();) {
            Object o = iterator.next();
            assertNotNull(o);
        }
    }

    public void testRetreivalOfSimpleType() {
        Set list = _xsd4YAWLBuilder.getGlobalDefinitionsForType("StateType");
        assertTrue("Expected listsize 1, got size " + list.size(), list.size() == 1);
        for (Iterator iterator = list.iterator(); iterator.hasNext();) {
            XSDTypeDefinition simpleTypeDef = (XSDTypeDefinition) iterator.next();
            assertEquals("StateType", simpleTypeDef.getName());
        }
    }

    public void testRetreivalOfGlobalElement() {
        Set set = _xsd4YAWLBuilder.getGlobalDefinitionsForElement("number");
        set = _xsd4YAWLBuilder.rebuildComponentsAsSelfContainedCopies(set);
        assertTrue(set.size() == 1);
        for (Iterator iterator = set.iterator(); iterator.hasNext();) {
            XSDElementDeclaration simpleElement = (XSDElementDeclaration) iterator.next();
            assertEquals("number", simpleElement.getName());
        }
    }


    public void testSimpleRetreivalOfGlobalElements() {
        Set set = _xsd4YAWLBuilder.getGlobalDefinitionsForElement("address");
        set = _xsd4YAWLBuilder.rebuildComponentsAsSelfContainedCopies(set);
        int[] results = new int[4];
        for (Iterator iterator = set.iterator(); iterator.hasNext();) {
            XSDConcreteComponent o = (XSDConcreteComponent) iterator.next();
            if (o instanceof XSDElementDeclaration) {
                XSDElementDeclaration element = (XSDElementDeclaration) o;
                String name = element.getName();
                if ("address".equals(name)) {
                    results[0]++;
                }
                if ("number".equals(name)) {
                    results[1]++;
                }
                if ("locality".equals(name)) {
                    results[2]++;
                }
            } else {
                XSDTypeDefinition def = (XSDTypeDefinition) o;
                String name = def.getName();
                if ("StateType".equals(name)) {
                    results[3]++;
                }
            }
        }
        assertTrue("expecting 1 addresses not " + results[0], results[0] == 1);
        assertTrue("expecting 1 number not " + results[1], results[1] == 1);
        assertTrue("expecting 1 locality not " + results[2], results[2] == 1);
        assertTrue("expecting 1 StateType not " + results[3], results[3] == 1);
        assertTrue(set.size() == 4);
    }


    public void testIfWeCanSerialiseASimpleRetrieve() {
        Set set = _xsd4YAWLBuilder.getGlobalDefinitionsForElement("address");
        set = _xsd4YAWLBuilder.rebuildComponentsAsSelfContainedCopies(set);
        try {
            String [][] desiredElementNamesToTypes = new String[1][];
            desiredElementNamesToTypes[0] = new String[]{"address", null};
            XSDSchema schema = _xsd4YAWLBuilder.createYAWLSchema(
                    desiredElementNamesToTypes,
                    set,
                    "data");

            String schemaStr = XSDUtil.convertToString(schema);
            assertNotNull(schemaStr);
        } catch (YSchemaBuildingException e) {
            fail("the generated schema should not throw exceptions");
        }
    }


    public void testIfWeCanCatchABadRequest() {
        Set set = _xsd4YAWLBuilder.getGlobalDefinitionsForElement("address");
        set = _xsd4YAWLBuilder.rebuildComponentsAsSelfContainedCopies(set);
        Exception f = null;
        try {
            String [][] desiredElementNamesToTypes = new String[2][];
            desiredElementNamesToTypes[0] = new String[]{"address", null};
            desiredElementNamesToTypes[1] = new String[]{"fruit", null};

            XSDSchema schema = _xsd4YAWLBuilder.createYAWLSchema(
                    desiredElementNamesToTypes,
                    set,
                    "data");
            assertNotNull(schema);
        } catch (YSchemaBuildingException e) {
            f = e;
        }
        if (f == null) {
            fail("the generated schema should throw exceptions");
        }
    }



    public void testIfWeCanSerialiseASlightlyMoreComplicatedRetrieve() {
        Set set = _xsd4YAWLBuilder.getGlobalDefinitionsForElement("person");
        set = _xsd4YAWLBuilder.rebuildComponentsAsSelfContainedCopies(set);
        try {
            String [][] desiredElementNamesToTypes = new String[4][];
            desiredElementNamesToTypes[0] = new String[]{"number", null};
            desiredElementNamesToTypes[1] = new String[]{"address", null};
            desiredElementNamesToTypes[2] = new String[]{"person", "PersonType"};
            desiredElementNamesToTypes[3] = new String[]{"myState", "StateType"};

            XSDSchema schema = _xsd4YAWLBuilder.createYAWLSchema(
                    desiredElementNamesToTypes,
                    set,
                    "data");
            String schemaStr = XSDUtil.convertToString(schema);
            if(schemaStr.indexOf("myState") == -1){
                System.out.println("schemaStr = " + schemaStr);
                fail("Was looking for 'myState' element inside schema but not found.");
            }
            int numberIdx = schemaStr.indexOf("name=\"number\"");
            int addressIdx = schemaStr.indexOf("name=\"address\"");
            assertTrue( "The 'number' element should appear before the" +
                    " 'address' element inside the schema." +
                    "\n\tSchemaSTRING:\n" +
                    schemaStr,
                    numberIdx < addressIdx);
        } catch (YSchemaBuildingException e) {
            e.printStackTrace();
            fail("The generated schema should not throw exceptions." + e.getMessage());
        }
    }


//    public void testIfWeCanBuildAnAnyDeclaration() {
//        Set set = new HashSet();
//        try {
//            String [][] desiredElementNamesToTypes = new String[1][];
//            desiredElementNamesToTypes[0] =
//                    new String[]{"xs:any", null, "http://www.w3.org/2001/XMLSchema"};
//
//            XSDSchema schema = _xsd4YAWLBuilder.createYAWLSchema(
//                    desiredElementNamesToTypes,
//                    set,
//                    "data");
//            String schemaStr = XSDUtil.convertToString(schema);
//            assertTrue(schemaStr.indexOf("<xsd:any processContents=\"lax\"/>") != -1);
//        } catch (YSchemaBuildingException e) {
//            e.printStackTrace();
//            fail("The generated schema should not throw exceptions." + e.getMessage());
//        }
//    }




//    public void testIfWeCanSerialiseATypesProperly() {
//        Set globalDefinitions = _xsd4YAWLBuilder.getGlobalDefinitionsForType("PersonType");
//        try {
//            String [][] desiredElementNamesToTypes = new String[6][];
//            desiredElementNamesToTypes[0] = new String[]{"number", null};
//            desiredElementNamesToTypes[1] = new String[]{"address", null};
//            desiredElementNamesToTypes[2] = new String[]{"person", "PersonType"};
//            desiredElementNamesToTypes[3] = new String[]{"person2", "PersonType"};
//            desiredElementNamesToTypes[4] = new String[]{"myState", "StateType"};
//            desiredElementNamesToTypes[5] = new String[]{"yourState", "StateType"};
//
//            XSDSchema schema = _xsd4YAWLBuilder.createYAWLSchema(
//                    desiredElementNamesToTypes,
//                    globalDefinitions,
//                    "data");
//            String schemaStr = XSDUtil.convertToString(schema);
//            if(schemaStr.indexOf("<xsd:enumeration value=\"Qld\"/>") == -1) {
//                System.out.println("schemaStr = " + schemaStr);
//                fail("Was looking for content inside stateType but not found.");
//            }
//        } catch (YSchemaBuildingException e) {
//            fail("The generated schema should not throw exceptions.");
//        }
//    }





//    public void testSimpleTypeCreation(){
//        try {
//            String [][] desiredElementNamesToTypes = new String[2][];
//            desiredElementNamesToTypes[0] = new String[]{"number",
//                                                         "integer",
//                                                         XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001};
//            desiredElementNamesToTypes[1] = new String[]{"address",
//                                                         "string",
//                                                         XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001};
//            XSDSchema schema =
//                    _xsd4YAWLBuilder.createYAWLSchema(
//                            desiredElementNamesToTypes,
//                            null,
//                            "data");
//            String schemaStr = XSDUtil.convertToString(schema);
//            assertTrue(schemaStr.indexOf("<xsd:element name=\"number\" type=\"xsd:integer\"/>") != -1);
//            assertTrue(schemaStr.indexOf("<xsd:element name=\"address\" type=\"xsd:string\"/>") != -1);
//        } catch (YSchemaBuildingException e) {
//            e.printStackTrace();
//            fail("The generated schema should not throw exceptions." + e.getMessage());
//        }
//    }



    public void testIfWeCanFailToBuildAnInvalidRequest() {
        Set set = _xsd4YAWLBuilder.getGlobalDefinitionsForElement("address");
        set = _xsd4YAWLBuilder.rebuildComponentsAsSelfContainedCopies(set);
        Exception f = null;
        try {
            String [][] desiredElementNamesToTypes = new String[3][];
            desiredElementNamesToTypes[0] = new String[]{"number", null};
            desiredElementNamesToTypes[1] = new String[]{"address", null};
            desiredElementNamesToTypes[2] = new String[]{"person", "PersonType"};

            XSDSchema schema = _xsd4YAWLBuilder.createYAWLSchema(
                    desiredElementNamesToTypes,
                    set,
                    "data");
            assertNotNull(schema);
        } catch (YSchemaBuildingException e) {
            f = e;
        }
        if (f == null) {
            fail("the generated schema should throw exceptions because person " +
                    "type is not referenced");
        }
    }



    public void testGlobalAttributeDetermination(){
        XSDSchema schema = _xsd4YAWLBuilder.getOriginalSchema();
        EList listAtts = schema.getAttributeDeclarations();
        for (int i = 0; i < listAtts.size(); i++) {
            XSDAttributeDeclaration attribute = (XSDAttributeDeclaration) listAtts.get(i);
            assertTrue(_xsd4YAWLBuilder.isGlobalAttributeDeclaration(attribute));
        }
    }



    public static void main(String args[]) {
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestXSD4YAWLBuilder.class);
        return suite;
    }
}
