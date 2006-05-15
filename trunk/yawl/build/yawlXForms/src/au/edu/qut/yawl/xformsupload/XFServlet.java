package au.edu.qut.yawl.xformsupload;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.chiba.tools.schemabuilder.Schema2XForms;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * Posts received by this servlet result in a XForm being generated dynamically 
 * by Chiba, using the Schema2XForms tool.  The schema and instance files must
 * already exist in the Chiba forms directory.  If a form with the same name exists,
 * it is deleted.
 * @author Guy Redding 11/01/2005
 */
public class XFServlet extends HttpServlet{

	private boolean debug = false;
	private SortedSet s = Collections.synchronizedSortedSet(new TreeSet());

	
	/* (non-Javadoc) expected header parameters are: root, schema, instance, form, workItemID, sessionHandle
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		
		File f = null;
		int start = 0;
		
		String root = request.getHeader("root");
        if (debug) System.out.println("XF root: "+root);
		
		String schema = request.getHeader("schema");
		if (debug) System.out.println("XF schema name: "+schema);
        
        String instance = request.getHeader("instance");
        if (debug) System.out.println("XF instance name: "+instance);
        
        String form = request.getHeader("form");
        if (debug) System.out.println("XF form name: "+form);
        
        String inputParams = request.getHeader("inputParams");
        // inputParams will appear separated by commas in this format: 
        // nameofartist,nameofrecord,nameofsong,
        
        if (inputParams != null){
	        if (inputParams.compareTo("") != 0){
		        int end = inputParams.length();
		        if (debug) System.out.println("XF input params: "+inputParams);
		        
		        // finish parsing when start = end
		        while (start < end){
		        	int newstart = inputParams.indexOf(',', start);
		        	if (debug) System.out.println("start: "+start+", newstart: "+newstart);
		        	s.add(inputParams.substring(start, newstart));
		        	if (debug) System.out.println("string: "+inputParams.substring(start, newstart));
		        	start = newstart+1;
		        }
	        }
        }
        
		if (debug) System.out.println("XFServlet: making builder");
		Schema2XForms builder = new Schema2XForms(); 
		
        ServletContext RP = getServletConfig().getServletContext();
        String filePath = RP.getRealPath(File.separator+"forms");   
		
		// if schema != null etc. set directory to forms, not Tomcat/bin
		if (debug) System.out.println("XFServlet: setInputURI to "+schema);
		try{
			builder.setInputURI(filePath+File.separator+schema);
		}
		catch(IOException e){
			System.out.println("XFServlet IOException: "+e.toString());
		}
		
		if (debug) System.out.println("XFServlet: setInstanceFile to "+instance);
		try{
			builder.setInstanceFile(filePath+File.separator+instance);
		}
		catch(IOException e){
			System.out.println("IOException: "+e.toString());
		}
		
		builder.setInstanceHref("http://www.w3.org/2001/XMLSchema-instance");

		ServletContext context = getServletContext();
		
		// calls yawlFormServlet with a GET
		//builder.setAction("http://localhost:8080/worklist/yawlFormServlet?workItemID="+workItemID+"&sessionHandle="+sessionHandle);
		//builder.setAction("http://localhost:8080/worklist/yawlFormServlet");
		builder.setAction(context.getInitParameter("YAWL"));
		
		//builder.setBase("http://localhost:8080/YAWLXForms/");
		builder.setBase(context.getInitParameter("FormBase"));
		
		f = new File(filePath+File.separator+form);
		
		// goes in chiba forms directory
		builder.setOutputFile(f);
		
		builder.setRootTagName(root);
		
		// this transform is required for maintaining the look & feel of YAWL
		builder.setStylesheet("html4yawl.xsl"); 
		
		builder.setSubmitMethod("post");
		
		builder.setWrapperType("XHTML");
		
		if (debug) System.out.println("XFServlet: completed making builder");
		builder.execute();

		// TODO cleanup of temporary schema and instance files currently in forms directory
		
		// now parse the newly created form in DOM.  If a readonly input parameter matches
		// a nodeset attribute, add a " xforms:readonly="true()" " attribute to the form and serialise
		// the form to file output, overwriting the previously existing form.  if it exists, remove the 
		// " xforms:required " attribute for nodes that are readonly, because they might be empty, 
		// which means that the form will never submit if a node is required, is empty and is readonly.
		
		// an input param may be a root node for a complex type, meaning that all of its leaf nodes are input as well.
		if (inputParams != null){
			domCreation(f);
		}
		
		s.clear();
	}
	
	
	/**
	 * @param f
	 */
	private void domCreation(File f){
		
		Document document = null;
		
		try {			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			// parse schema
			document = builder.parse(f);
			
			DOMImplementation impl = builder.getDOMImplementation();
			
			// start recursive parse of the schema DOM
			nodeDetails(document, document);
		}
		catch (FactoryConfigurationError e) {
			e.printStackTrace();
		} 
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		catch (SAXException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
	    BufferedWriter bw = null;
	    
	    // delete any existing xform with the same filename
	    // cauz we can't trust the webapp to overwrite the xform at all
	    if (f.exists()){
	    	f.delete();
	    }
	    
		// create temporary instance XML writer
		try{
			if (debug){
				System.out.println("formFilePath: "+f.toString());
			}
			bw = new BufferedWriter(new FileWriter(f, false));
		}
		catch(IOException e){
			System.out.println("--IO file error: "+e.toString());
		}

		// write new XML instance
		try {
			OutputFormat format = new OutputFormat(document);
			XMLSerializer output = new XMLSerializer(bw, format);
			output.serialize(document);
			if (debug) System.out.println("XForm EDITED/OVERWRITTEN: "+f.toString());
		}
		catch (IOException e) {
		  System.out.println(e);
		}
	}
	
	
	/**
	 * @param document
	 * @param node
	 */
	private void nodeDetails (Document document, Node node) {
		String Content = "";
		int type = node.getNodeType();
		
		// check if element
		if (type == Node.ELEMENT_NODE) {
			
			if (debug) System.out.println("Node Name: "+node.getNodeName());
			
			// check if the node has any attributes, important for filtering out
			// anonymous complex types, which we don't want to add
			if ((node.hasAttributes() == true) && (node.getNodeName().compareTo("xforms:bind") == 0)){

				// if it does, store it in a NamedNodeMap object
				NamedNodeMap AttributesList = node.getAttributes();
				
				for (int j = 0; j < AttributesList.getLength(); j++) {
					if (debug){
						System.out.println("Attribute Name: "+AttributesList.item(j).getNodeName());
						System.out.println("Attribute Value: "+AttributesList.item(j).getNodeValue());
						System.out.println("Attribute Type: "+AttributesList.item(j).getNodeType());
					}
					// cycle thru attributes, report match if the element is "xforms:bind"
					// and "xforms:nodeset" equals the name of an input parameter.
					if ( (AttributesList.item(j).getNodeName().compareTo("xforms:nodeset") == 0) &&
							(s.contains(AttributesList.item(j).getNodeValue()) == true) ){

						Element element = (Element) node;
						Document factory = node.getOwnerDocument();
						
						// if it exists, edit the "xforms:required" attribute to be false
						for (int i = 0; i < AttributesList.getLength(); i++) {
							if (AttributesList.item(i).getNodeName().compareTo("xforms:required") == 0){
								// edit the required attribute from this node to be false
								//AttributesList.item(j).setNodeValue("false()");
								if (debug) System.out.println("XFORMS:REQUIRED FOUND");
								Attr sa = factory.createAttribute("xforms:required");
								sa.setValue("false()");
								element.setAttributeNode(sa);
							}
						}

						Attr specifiedAttribute = factory.createAttribute("xforms:readonly");
						specifiedAttribute.setValue("true()");
						element.setAttributeNode(specifiedAttribute);
					}
				}
			}
		}
		else if (type == Node.TEXT_NODE) {
			// check if text node
			Content = node.getNodeValue();
			if (debug){
				if (!Content.trim().equals("")){
					System.out.println ("Character data: " + Content);
				}
				else{
				}
			}
		}
		else if (type == Node.COMMENT_NODE) {
			// check if comment node
			Content = node.getNodeValue();
			if (debug){	              
				if (!Content.trim().equals("")){
					System.out.println ("Comment: " + Content);
				}
			}
		}
	
		// check if current node has any children
		NodeList children = node.getChildNodes();
		
		if (children != null) {
			// if it does, iterate through the collection
			for (int i=0; i< children.getLength(); i++) {
				// recursively call function to proceed to next level
				nodeDetails(document, children.item(i));
			}
		}
	}
}