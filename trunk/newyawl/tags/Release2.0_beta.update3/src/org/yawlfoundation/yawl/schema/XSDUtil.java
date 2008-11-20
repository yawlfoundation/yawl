/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.schema;

import org.yawlfoundation.yawl.exceptions.YSchemaBuildingException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.xsd.*;
import org.eclipse.xsd.impl.XSDDiagnosticImpl;
import org.eclipse.xsd.util.XSDResourceImpl;
import org.eclipse.xsd.util.XSDSchemaBuildingTools;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;

/**
 /**
 * 
 * @author Lachlan Aldred
 * Date: 27/08/2004
 * Time: 13:27:30
 * 
 */
public class XSDUtil {

    /**
     * Creates a blank XSdSchema
     * @return an XSDSchema blank.
     */
    public static XSDSchema createBlankSchema() {
        XSDSchema schemaNew = XSDSchemaBuildingTools.
                getBlankSchema(null, null, null, null, null);
        return schemaNew;
    }


    /**
     * Helper method to serialize an XSD Schema.
     * @param schema the schema to serialize.
     * @param output the desired stream to send the output.
     */
    public static void serializeXSDSchema(XSDSchema schema, OutputStream output) {
//        try {
        //this seems to be how to save an XML Schema
        ResourceSet resourceSet = new ResourceSetImpl();
        Resource resource = new XSDResourceImpl(URI.createURI("file://D:/testing.xml"));
        resource.getContents().add(schema);
        resourceSet.getResources().add(resource);
        schema.validate();
        printDiagnostics(schema);
//            schema.eResource().save(output, null);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }


    /**
     * Worker method to create an element declaration.
     * @param schema the schem to attach ot to.
     * @param localName the name of the new element.
     * @param type the type of the new element (must already exist).
     * @return the element declaration.
     * @throws YSchemaBuildingException
     */
    public static XSDElementDeclaration addElementDeclaration
            (XSDSchema schema, String localName, String type) throws YSchemaBuildingException {
        if ((null == schema) || (null == localName) || (null == type)) {
            throw new IllegalArgumentException("addComplexTypeDefinition called with null schema/type/name");
        }
        // Get the factory and create the type
        XSDFactory xsdFactory = XSDSchemaBuildingTools.getXSDFactory();

        // Create the element and set name, etc
        XSDElementDeclaration element = xsdFactory.createXSDElementDeclaration();
        element.setName(localName);

        // Add the complexType to the schema; it's typically a good
        // practice to do this sooner rather than later
        schema.getContents().add(element);

        XSDTypeDefinition typeDef = schema.resolveTypeDefinition(type);
        if (null == typeDef) {
            throw new YSchemaBuildingException("Problem: could not find type in schema called: " +
                    type);
        }
        element.setTypeDefinition(typeDef);
        return element;
    }


    /**
     * Creates a complex type def to the schema.
     * @param schema
     * @param localName
     * @return
     */
    public static XSDComplexTypeDefinition createComplexTypeDefinition
            (XSDSchema schema, String localName) {
        XSDFactory xsdFactory = getXSDFactory();

        // Create the type and set name, etc
        XSDComplexTypeDefinition complexType = xsdFactory.createXSDComplexTypeDefinition();
        complexType.setName(localName);
        complexType.setDerivationMethod(XSDDerivationMethod.EXTENSION_LITERAL);

        // Add the complexType to the schema; it's typically a good
        // practice to do this sooner rather than later
        schema.getContents().add(complexType);

//         Create simple anonymous type to extend
        XSDSimpleTypeDefinition anonSimpleType = xsdFactory.createXSDSimpleTypeDefinition();
//        complexType.setBaseTypeDefinition
//                (schema.resolveSimpleTypeDefinition(type));
//        XSDPrototypicalSchema.
        // Be sure to set the contents as well (obviously if this
        // were a reference it would be different)
//        complexType.setContent(anonSimpleType);
        return complexType;
    }

    /**
     * Gets an XSDFactory
     * @return
     */
    public static XSDFactory getXSDFactory() {
        return XSDSchemaBuildingTools.getXSDFactory();
    }


    /**
     * Prints diagnostic messages.
     * @param schema
     */
    private static void printDiagnostics(XSDSchema schema) {
        EList diagnositcs = schema.getAllDiagnostics();
        System.out.println("diagnositcs.size() = " + diagnositcs.size());
        for (int i = 0; i < diagnositcs.size(); i++) {
            XSDDiagnosticImpl diag = (XSDDiagnosticImpl) diagnositcs.get(i);
            System.out.println(diag.getMessage());

            if (diag.getNode() != null) {
                System.out.println("\tnode name : " + diag.getNode().getLocalName());
                NamedNodeMap map = diag.getNode().getAttributes();
                for (int j = 0; j < map.getLength(); j++) {
                    System.out.println("\t\t" + map.item(j));
                }
            }
        }
    }


    public static void printComponent(Writer output,
                                      XSDConcreteComponent xsdConcreteComponent) {
        // Print a component's element using Xerces.
        //

        // Get the component's element and create one if there isn't one already.
        //
        Element element = xsdConcreteComponent.getElement();
        if (element == null) {
            xsdConcreteComponent.updateElement();
            element = xsdConcreteComponent.getElement();
        }

        if (element != null) {
            try {
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();

                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(OutputKeys.METHOD, "xml");

                transformer.transform(new DOMSource(element), new StreamResult(output));
            } catch (TransformerException exception) {
                System.out.println(exception.getLocalizedMessage());
                exception.printStackTrace();
            }
        }
    }


    public static String convertToString(XSDConcreteComponent component) {
        StringWriter writer = new StringWriter();
        XSDUtil.printComponent(writer, component);
        return writer.getBuffer().toString();
    }
}
