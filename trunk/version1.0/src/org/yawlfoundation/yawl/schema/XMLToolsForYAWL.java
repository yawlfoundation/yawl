/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.schema;

import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.exceptions.YSchemaBuildingException;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.unmarshal.SchemaForSchemaValidator;
import org.apache.xerces.parsers.DOMParser;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDConstants;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.*;

/**
 /**
 * 
 * @author Lachlan Aldred
 * Date: 13/09/2004
 * Time: 10:33:10
 * 
 */
public class XMLToolsForYAWL {

    private XSD4YAWLBuilder _xsd4YAWLBuilder;

    public XMLToolsForYAWL() {
        _xsd4YAWLBuilder = new XSD4YAWLBuilder();
    }

    /**
     * Helper method to set the primary Schema for use later.
     * @param schemaStr the entire schema as a string
     */
    public void setPrimarySchema(String schemaStr) throws YSchemaBuildingException, YSyntaxException {
        //first of all check that the schema itself is valid
        SchemaForSchemaValidator tmp = SchemaForSchemaValidator.getInstance();
        String results = tmp.validateSchema(schemaStr);
        if (results.length() > 0) {
            throw new YSyntaxException(results);
        }

        //now set the schema
        Document document = null;
        DOMParser parser = new DOMParser();
        InputSource inputSrc = new InputSource(new StringReader(schemaStr));
        try {
            parser.parse(inputSrc);
            document = parser.getDocument();
            _xsd4YAWLBuilder.setSchema(document.getDocumentElement());
        } catch (Exception e) {
            YSchemaBuildingException f = new YSchemaBuildingException(e.getMessage());
            f.setStackTrace(e.getStackTrace());
            throw f;
        }
    }

    public void setPrimarySchema(InputSource schemaSource) {


    }


    /**
     * Returns a Set of the global type names as strings.
     * @return global type names Set.
     */
    public Set getPrimarySchemaTypeNames() {
        if (_xsd4YAWLBuilder != null) {
            return _xsd4YAWLBuilder.getGlobalTypeNames();
        } else {
            throwException();
        }
        return null;
    }


    /**
     * Returns a Set of global element names.
     * @return global element names Set.
     */
    public Set getPrimarySchemaElementNames() {
        if (_xsd4YAWLBuilder != null) {
            return _xsd4YAWLBuilder.getGlobalElementNames();
        } else {
            throwException();
        }
        return null;
    }


    /**
     * Creates a Schema in String format with a root element called data.  This data element
     * then contains sub-elements that are the union of the desired type names and desired
     * element names.  For each desired type one element is created and for each desired
     * element it is used directly.
     * Note that it is a syntax error to desired element names and type names to overlap.
     * @param instructions a 2 dimensional array of strings to strings all in QName format.
     * For example you could create a schema using only built in types like this:
     * <pre>
     * String [][] desiredElementNamesToTypes = new String[2][];
     * desiredElementNamesToTypes[0] = new String[]{"number",
     *                                              "integer",
     *                                              "http://www.w3.org/2001/XMLSchema"};
     * desiredElementNamesToTypes[1] = new String[]{"address",
     *                                              "string",
     *                                              "http://www.w3.org/2001/XMLSchema"};
     * String schemaStr = createYAWLSchema(desiredElementNamesToTypes);
     *</pre>
     * Will result in a schema like this:
     * <pre>
     *    <?xml version="1.0" encoding="UTF-8"?>
     *    <xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema">
     *      <xsd:element name="data">
     *         <xsd:complexType>
     *            <xsd:sequence>
     *               <xsd:element name="number" type="xsd:integer"/>
     *               <xsd:element name="address" type="xsd:string"/>
     *            </xsd:sequence>
     *         </xsd:complexType>
     *      </xsd:element>
     *   </xsd:schema>
     * </pre>
     * Note that the sequence of elements described in the String[][] is preserved in the
     * resulting schema.  Also note that this is the way that you use the method to create
     * elements using Schema's built in types.<p/>
     * Now imagine that you have set a primary Schema like this one:
     * <pre>
     <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified" attributeFormDefault="unqualified">
     <xs:element name="person" type="PersonType"/>
     <xs:complexType name="PersonType">
     <xs:sequence>
     <xs:element name="age" type="xs:nonNegativeInteger"/>
     <xs:element ref="address"/>
     <xs:element name="name2" type="xs:string"/>
     <xs:element name="favouriteFruit" type="FruitType"/>
     </xs:sequence>
     <xs:attribute ref="maritalStatus"/>
     <xs:attribute name="isParent" type="xs:boolean"/>
     </xs:complexType>
     <xs:element name="address">
     <xs:complexType>
     <xs:sequence>
     <xs:element ref="number"/>
     <xs:element name="streetName" type="xs:string"/>
     <xs:element ref="locality" minOccurs="0"/>
     <xs:element name="state" type="StateType"/>
     </xs:sequence>
     </xs:complexType>
     </xs:element>
     <xs:attribute name="maritalStatus" type="MaritalStatusType"/>
     <xs:element name="number" type="xs:nonNegativeInteger"/>
     <xs:element name="streetNumber" type="xs:nonNegativeInteger"/>
     <xs:element name="postCode">
     <xs:simpleType>
     <xs:restriction base="xs:nonNegativeInteger">
     <xs:minInclusive value="1000"/>
     <xs:maxInclusive value="9999"/>
     </xs:restriction>
     </xs:simpleType>
     </xs:element>
     <xs:simpleType name="StateType">
     <xs:restriction base="xs:string">
     <xs:enumeration value="Qld"/>
     <xs:enumeration value="NSW"/>
     <xs:enumeration value="Vic"/>
     <xs:enumeration value="WA"/>
     <xs:enumeration value="ACT"/>
     <xs:enumeration value="NT"/>
     <xs:enumeration value="SA"/>
     </xs:restriction>
     </xs:simpleType>
     <xs:simpleType name="FruitType">
     <xs:restriction base="xs:string">
     <xs:enumeration value="Apples"/>
     <xs:enumeration value="Bananas"/>
     <xs:enumeration value="Grapes"/>
     <xs:enumeration value="Peaches"/>
     <xs:enumeration value="Tomatoes"/>
     <xs:enumeration value="Lychees"/>
     </xs:restriction>
     </xs:simpleType>
     <xs:simpleType name="MaritalStatusType">
     <xs:restriction base="xs:string">
     <xs:enumeration value="Married"/>
     <xs:enumeration value="Single"/>
     <xs:enumeration value="Defacto"/>
     </xs:restriction>
     </xs:simpleType>
     <xs:element name="locality" type="xs:string"/>
     </xs:schema>
     *</pre>
     * A slightly more complicated use of the library could be:
     *<pre>
     * String [][] desiredElementNamesToTypes = new String[6][];
     * desiredElementNamesToTypes[0] = new String[]{"number", null};
     * desiredElementNamesToTypes[1] = new String[]{"address", null};
     * desiredElementNamesToTypes[2] = new String[]{"person", "PersonType"};
     * desiredElementNamesToTypes[3] = new String[]{"person2", "PersonType"};
     * desiredElementNamesToTypes[4] = new String[]{"myState", "StateType"};
     * desiredElementNamesToTypes[5] = new String[]{"yourState", "StateType"};
     * </pre>
     * Note you specify the element name first (mandatory) then the type name (optional).
     * An instruction to specify the element name only means that there is an existing element
     * to be retrieved.
     * The resulting schema could look like this:
     * <pre>
     <?xml version="1.0" encoding="UTF-8"?>
     <xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema">
     <xsd:element name="data">
     <xsd:complexType>
     <xsd:sequence>
     <xsd:element form="qualified" name="number" type="xsd:nonNegativeInteger"/>
     <xsd:element form="qualified" name="address">
     <xsd:complexType>
     <xsd:sequence>
     <xsd:element name="number" type="xsd:nonNegativeInteger"/>
     <xsd:element name="streetName" type="xsd:string"/>
     <xsd:element name="locality" type="xsd:string"/>
     <xsd:element name="state">
     <xsd:simpleType name="StateType">
     <xsd:restriction base="xsd:string">
     <xsd:enumeration value="Qld"/>
     <xsd:enumeration value="NSW"/>
     <xsd:enumeration value="Vic"/>
     <xsd:enumeration value="WA"/>
     <xsd:enumeration value="ACT"/>
     <xsd:enumeration value="NT"/>
     <xsd:enumeration value="SA"/>
     </xsd:restriction>
     </xsd:simpleType>
     </xsd:element>
     </xsd:sequence>
     </xsd:complexType>
     </xsd:element>
     <xsd:element name="person">
     <xsd:complexType name="PersonType">
     <xsd:sequence>
     <xsd:element name="age" type="xsd:nonNegativeInteger"/>
     <xsd:element name="address">
     <xsd:complexType>
     <xsd:sequence>
     <xsd:element name="number" type="xsd:nonNegativeInteger"/>
     <xsd:element name="streetName" type="xsd:string"/>
     <xsd:element name="locality" type="xsd:string"/>
     <xsd:element name="state">
     <xsd:simpleType name="StateType">
     <xsd:restriction base="xsd:string">
     <xsd:enumeration value="Qld"/>
     <xsd:enumeration value="NSW"/>
     <xsd:enumeration value="Vic"/>
     <xsd:enumeration value="WA"/>
     <xsd:enumeration value="ACT"/>
     <xsd:enumeration value="NT"/>
     <xsd:enumeration value="SA"/>
     </xsd:restriction>
     </xsd:simpleType>
     </xsd:element>
     </xsd:sequence>
     </xsd:complexType>
     </xsd:element>
     <xsd:element name="name2" type="xsd:string"/>
     <xsd:element name="favouriteFruit">
     <xsd:simpleType name="FruitType">
     <xsd:restriction base="xsd:string">
     <xsd:enumeration value="Apples"/>
     <xsd:enumeration value="Bananas"/>
     <xsd:enumeration value="Grapes"/>
     <xsd:enumeration value="Peaches"/>
     <xsd:enumeration value="Tomatoes"/>
     <xsd:enumeration value="Lychees"/>
     </xsd:restriction>
     </xsd:simpleType>
     </xsd:element>
     </xsd:sequence>
     <xsd:attribute name="maritalStatus">
     <xsd:simpleType name="MaritalStatusType">
     <xsd:restriction base="xsd:string">
     <xsd:enumeration value="Married"/>
     <xsd:enumeration value="Single"/>
     <xsd:enumeration value="Defacto"/>
     </xsd:restriction>
     </xsd:simpleType>
     </xsd:attribute>
     <xsd:attribute name="isParent" type="xsd:boolean"/>
     </xsd:complexType>
     </xsd:element>
     <xsd:element name="person2">
     <xsd:complexType name="PersonType">
     <xsd:sequence>
     <xsd:element name="age" type="xsd:nonNegativeInteger"/>
     <xsd:element name="address">
     <xsd:complexType>
     <xsd:sequence>
     <xsd:element name="number" type="xsd:nonNegativeInteger"/>
     <xsd:element name="streetName" type="xsd:string"/>
     <xsd:element name="locality" type="xsd:string"/>
     <xsd:element name="state">
     <xsd:simpleType name="StateType">
     <xsd:restriction base="xsd:string">
     <xsd:enumeration value="Qld"/>
     <xsd:enumeration value="NSW"/>
     <xsd:enumeration value="Vic"/>
     <xsd:enumeration value="WA"/>
     <xsd:enumeration value="ACT"/>
     <xsd:enumeration value="NT"/>
     <xsd:enumeration value="SA"/>
     </xsd:restriction>
     </xsd:simpleType>
     </xsd:element>
     </xsd:sequence>
     </xsd:complexType>
     </xsd:element>
     <xsd:element name="name2" type="xsd:string"/>
     <xsd:element name="favouriteFruit">
     <xsd:simpleType name="FruitType">
     <xsd:restriction base="xsd:string">
     <xsd:enumeration value="Apples"/>
     <xsd:enumeration value="Bananas"/>
     <xsd:enumeration value="Grapes"/>
     <xsd:enumeration value="Peaches"/>
     <xsd:enumeration value="Tomatoes"/>
     <xsd:enumeration value="Lychees"/>
     </xsd:restriction>
     </xsd:simpleType>
     </xsd:element>
     </xsd:sequence>
     <xsd:attribute name="maritalStatus">
     <xsd:simpleType name="MaritalStatusType">
     <xsd:restriction base="xsd:string">
     <xsd:enumeration value="Married"/>
     <xsd:enumeration value="Single"/>
     <xsd:enumeration value="Defacto"/>
     </xsd:restriction>
     </xsd:simpleType>
     </xsd:attribute>
     <xsd:attribute name="isParent" type="xsd:boolean"/>
     </xsd:complexType>
     </xsd:element>
     <xsd:element name="myState">
     <xsd:simpleType name="StateType">
     <xsd:restriction base="xsd:string">
     <xsd:enumeration value="Qld"/>
     <xsd:enumeration value="NSW"/>
     <xsd:enumeration value="Vic"/>
     <xsd:enumeration value="WA"/>
     <xsd:enumeration value="ACT"/>
     <xsd:enumeration value="NT"/>
     <xsd:enumeration value="SA"/>
     </xsd:restriction>
     </xsd:simpleType>
     </xsd:element>
     <xsd:element name="yourState">
     <xsd:simpleType name="StateType">
     <xsd:restriction base="xsd:string">
     <xsd:enumeration value="Qld"/>
     <xsd:enumeration value="NSW"/>
     <xsd:enumeration value="Vic"/>
     <xsd:enumeration value="WA"/>
     <xsd:enumeration value="ACT"/>
     <xsd:enumeration value="NT"/>
     <xsd:enumeration value="SA"/>
     </xsd:restriction>
     </xsd:simpleType>
     </xsd:element>
     </xsd:sequence>
     </xsd:complexType>
     </xsd:element>
     </xsd:schema>
     </pre>
     * Note that unlike the input schema there are no global elements and no global types.
     * If possible the returned schema should look like this, but it will at least be valid.
     * @return a schema built accordingly
     */
    public String createYAWLSchema(Instruction[] instructions, String rootElementName) throws YSchemaBuildingException {
        XSDSchema schema = null;
        if (null == _xsd4YAWLBuilder) {
            throwException();
        }
        String[][] desiredNamesToTypes = convertFormat(instructions);
        Set originalComponents = getComponentsNeeded(instructions);
        Set clonedComponents =
                _xsd4YAWLBuilder.rebuildComponentsAsSelfContainedCopies(originalComponents);
        schema = _xsd4YAWLBuilder.createYAWLSchema(
                desiredNamesToTypes,
                clonedComponents,
                rootElementName);

        if (null != schema) {
            return XSDUtil.convertToString(schema);
        }
        return null;
    }


    private String[][] convertFormat(Instruction[] instructions) {
        List formattedInstructions = new ArrayList();
        for (int i = 0; i < instructions.length; i++) {
            Instruction instruction = instructions[i];
            String elementName = instruction.getElementName();
            if (instruction instanceof ElementCreationInstruction) {
                ElementCreationInstruction cInstruction =
                        (ElementCreationInstruction) instruction;
                String typeName = cInstruction.getTypeName();
                boolean isSchema4SchemaType = cInstruction.isSchem4SchemaType();
                if (isSchema4SchemaType) {
                    /**
                     * AJH: Added mandatory flag as 4th array item
                     */
//                    formattedInstructions.add(new String[]{
//                        elementName, typeName, XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001});
                    formattedInstructions.add(new String[]{
                        elementName, typeName, XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001, Boolean.toString(cInstruction.isMandatory())});
                } else {
                    formattedInstructions.add(new String[]{elementName, typeName});
                }
            } else if (instruction instanceof ElementReuseInstruction) {
                formattedInstructions.add(new String[]{elementName, null});
            } else if (instruction instanceof UntypedElementInstruction) {
                formattedInstructions.add(new String[]{"xs:any", null});
            }
        }
        return (String[][]) formattedInstructions.toArray(new String[instructions.length][]);
    }


    /**
     * Return original copies of all the components needed to do the job.
     * @param desiredNamesToTypes
     * @return
     */
    private Set getComponentsNeeded(Instruction[] desiredNamesToTypes) {
        Set originalDefinitions = new HashSet();
        for (int i = 0; i < desiredNamesToTypes.length; i++) {
            Instruction instruction = desiredNamesToTypes[i];
            if (instruction instanceof ElementCreationInstruction) {
                ElementCreationInstruction cInstruction =
                        (ElementCreationInstruction) instruction;
                String typeName = cInstruction.getTypeName();
                originalDefinitions.addAll(
                        _xsd4YAWLBuilder.getGlobalDefinitionsForType(typeName));

            } else if (instruction instanceof ElementReuseInstruction) {
                String elementName = instruction.getElementName();
                originalDefinitions.addAll(
                        _xsd4YAWLBuilder.getGlobalDefinitionsForElement(elementName));
            }
        }
        return originalDefinitions;
    }


    private void throwException() {
        throw new RuntimeException(
                "You need to set the primary schema before you can query it.");
    }


    /**
     * Returns a string representation of the primary schema set into this object.
     * @return schema in string format.
     */
    public String getSchemaString() {
        if (null != _xsd4YAWLBuilder) {
            XSDSchema schema = _xsd4YAWLBuilder.getOriginalSchema();
            if (null != schema) {
                String schemaStr = XSDUtil.convertToString(schema);
                schemaStr = schemaStr.substring(schemaStr.indexOf('\n'));
                return schemaStr;
            }
        }
        return "";
    }

    /**
     * Returns the Schema 4 Schema Namespace (2001).
     * @return
     */
    public static String getSchema4SchemaNameSpace() {
        return XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001;
    }


    /**
     * Returns is schema for schema type
     * @param name
     * @return
     */
    public boolean isSchemaForSchemaType(String name) {
        return _xsd4YAWLBuilder.getSchemaForSchemaTypeDef(name) != null;
    }


    /**
     * Returns is a valid type contained by the Schema library or a valid schema for
     * schema type.
     * @param typename of the type in question
     * @param isSchem4Schema is schema 4 schema type
     * @return
     */
    public boolean isValidType(String typename, boolean isSchem4Schema) {
        if (isSchem4Schema) {
            return isSchemaForSchemaType(typename);
        } else {
            return _xsd4YAWLBuilder.getTypeDefinition(typename) != null;
        }
    }


    public Instruction[] buildInstructions(Collection params) {
        List instructions = new ArrayList();
        Instruction i;

        List paramsList = new ArrayList(params);
        Collections.sort(paramsList);
        for (int j = 0; j < paramsList.size(); j++) {
            YVariable parameter = (YVariable) paramsList.get(j);
            if (parameter.usesElementDeclaration()) {
                i = new ElementReuseInstruction(
                        parameter.getElementName());

                instructions.add(i);
            }
            if (parameter.usesTypeDeclaration()) {
                boolean isBuiltInSchemaType =
                        null != parameter.getDataTypeNameSpace() &&
                        parameter.getDataTypeNameSpace().equals(
                                XMLToolsForYAWL.getSchema4SchemaNameSpace());

                i = new ElementCreationInstruction(
                        parameter.getName(),
                        parameter.getDataTypeName(),
                        isBuiltInSchemaType,
                        parameter.isMandatory());
                instructions.add(i);
            }
            if (parameter.isUntyped()) {
                i = new UntypedElementInstruction();
                instructions.add(i);
            }
        }
        Instruction[] instructionsArr;
        instructionsArr = (Instruction[]) instructions.toArray(
                new Instruction[instructions.size()]);
        return instructionsArr;
    }

}
