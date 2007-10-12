/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.forms;

import org.apache.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.*;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.io.StringWriter;


/**
 * @author Guy Redding 7/05/2004
 *
 */
public class InstanceBuilder {

    private String root;
    private String builtinstance;
	private static Logger _logger = Logger.getLogger(InstanceBuilder.class);
    
    /**
     * Builds a populated XML instance given a schema and task instance data from YAWL.
     * @param schema String containing the schema.
     * @param _root Instance root name.
     * @param instance String containing the instance data.
     */
    public InstanceBuilder(String schema, String _root, String instance) {
    	
        root = _root; // _root must be vaild xml
        
        Document document = null;
        Document instanceS = null;
        Document instanceI = null;
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            byte[] xmlByteArray = schema.getBytes();
    		ByteArrayInputStream xmlStream = new ByteArrayInputStream( xmlByteArray );
    		InputSource xmlReader = new InputSource(xmlStream);
    		
            document = builder.parse(xmlReader);
               
            DOMImplementation impl = builder.getDOMImplementation();
            instanceS = impl.createDocument(null, root, null);
            Element rootElement = instanceS.getDocumentElement();
            
            // start recursive parse of the schema DOM
            nodeDetails(document, instanceS, rootElement);
            
        } catch (FactoryConfigurationError e) {
            _logger.debug("Factory Configuration Error: " + e.toString());
        } catch (ParserConfigurationException e) {
            _logger.debug("Parser Configuration Error: " + e.toString());
        } catch (SAXException e) {
            _logger.debug("SAX Exception: " + e.toString());
        } catch (IOException ioe) {
            _logger.debug("IO Exception: " + ioe.toString());
        }
        
        if (instance != null){
	        // overwrite any matching elements in the empty instance with
	        // elements contained in this input instance
	        try{
	        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        	DocumentBuilder builder = factory.newDocumentBuilder();
	            
	            byte[] xmlByteArray = instance.getBytes();
	    		ByteArrayInputStream xmlStream = new ByteArrayInputStream(xmlByteArray);
	    		InputSource xmlReader = new InputSource(xmlStream);
	    		
	            instanceI = builder.parse(xmlReader);
	            
	        } catch (FactoryConfigurationError e) {
	            _logger.debug("Factory Configuration Error: " + e.toString());
	        } catch (ParserConfigurationException e) {
	            _logger.debug("Parser Configuration Error: " + e.toString());
	        } catch (SAXException e) {
	        	_logger.debug("SAX Error: " + e.toString());
			} catch (IOException e) {
				_logger.debug("IO Exception: " + e.toString());
			}
	        
	        // Create NodeIterators for both the "empty" and "input" instance documents
	        // NOTE: must have DOM level 2 to allow DocumentTraversal.
	        DocumentTraversal traversableS = (DocumentTraversal) instanceS;
	        NodeIterator iteratorS = traversableS.createNodeIterator(instanceS, NodeFilter.SHOW_ALL, null, true);
	        
	        // Iterate over the input instance data
	        Node nodeS;
	        Node nodeI;
	        boolean found = false;
	        
	        while ((nodeS = iteratorS.nextNode()) != null) {
	            
	            if (nodeS.getNodeName().compareTo(root) != 0) { // ignore root element
	            	
	                DocumentTraversal traversableI = (DocumentTraversal) instanceI;
	                NodeIterator iteratorI = traversableI.createNodeIterator(instanceI, NodeFilter.SHOW_ELEMENT, null, true);
	                
	                // reset flag
	                if (found == true) {
	                    found = false;
	                }
	                
	                while (((nodeI = iteratorI.nextNode()) != null) && (found == false)) {
	                	
	                    // if the nodeName for this instance node matches an elements attribute in the schema,
	                    // overwrite the text value of that node with the text from this node
	                	// then delete the node from the original instance so that it can't be copied again.
	                    if (nodeI.getNodeName().compareTo(nodeS.getNodeName()) == 0) {
	                    	
	                        try {
	                            Node newNode = instanceS.importNode(nodeI, true);
	                            instanceS.getDocumentElement().replaceChild(newNode, nodeS);
	                            
	                            Node parent = nodeI.getParentNode();
	                            parent.removeChild(nodeI);
	                            
	                            found = true;
	
	                        } catch (DOMException e) {
	                            _logger.debug("DOM Error: "+e.toString());
	                        }
	                    }
	                }
	            }
	        }
        }
        
        StringWriter sw = new StringWriter();
        
        // write new XML instance
        try {
            OutputFormat format = new OutputFormat(instanceS);
            XMLSerializer output = new XMLSerializer(sw, format);
            output.serialize(instanceS);
        } catch (IOException e) {
            _logger.debug("IOException: "+e.toString());
        }
        
        builtinstance = sw.toString();

        //logger.debug("InstanceBuilder XML: "+builtinstance);
    }


    /**
     * A recursive function to traverse and mutate the schema document tree,
     * to create an XML instance that contains YAWL data.
     * @param node The currently referenced Node in the original schema Document.
     * @param instanceDoc The original schema Document.
     * @param instanceElement The instance Document.
     */
    private void nodeDetails(Node node, Document instanceDoc, Element instanceElement) {
    	
        int type = node.getNodeType();
        boolean isQualifiedForm = false;
        boolean isType = false;

        // check if element
        if (type == Node.ELEMENT_NODE) {

            // if the element is a simpleType or complexType do not include it
            // because this is the empty-instance generation which does not
            // require simple or complex type node names in it.
            if (node.getNodeName().compareTo("xsd:simpleType") == 0 || node.getNodeName().compareTo("xsd:complexType") == 0) {
                isType = true;
            }

            // check if the node has any attributes, important for filtering out
            // anonymous complex types, which we don't want to add
            if (node.hasAttributes() == true && isType == false) {

                // if it does, store it in a NamedNodeMap object
                NamedNodeMap AttributesList = node.getAttributes();

                for (int j = 0; j < AttributesList.getLength(); j++) {
                    if (AttributesList.item(j).getNodeName().compareTo("form") == 0
                            && AttributesList.item(j).getNodeValue().compareTo("qualified") == 0) {
                        isQualifiedForm = true;
                    }
                }

                if (isQualifiedForm != true) {

                    // iterate through the NamedNodeMap and get the attribute names and values
                    for (int j = 0; j < AttributesList.getLength(); j++) {

                        // if the name attribute for this element is found and not the root element
                        if ((AttributesList.item(j).getNodeName().compareTo("name") == 0
                                || AttributesList.item(j).getNodeName().compareTo("ref") == 0)
                                && AttributesList.item(j).getNodeValue().compareTo(root) != 0) {

                            // create new element
                            Element e = instanceDoc.createElement(AttributesList.item(j).getNodeValue());
                            instanceElement.appendChild(e);
                            instanceElement = e;
                        }
                    }
                }
            }
        }

        // check if current node has any children
        NodeList children = node.getChildNodes();

        if (children != null && isQualifiedForm == false) {
            // if it does, iterate through the collection
            for (int i = 0; i < children.getLength(); i++) {
                // recursively call function to proceed to next level
                nodeDetails(children.item(i), instanceDoc, instanceElement);
            }
        }
    }
    
    
    /**
     * Gets the XML instance built by this class.
     * @return XML instance as a String.
     */
    public String getInstance(){
    	return builtinstance;
    }
}