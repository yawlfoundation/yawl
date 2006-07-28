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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * Uploads a XML Schema or XML instance file with the given name set from 
 * parameters in the request.  The schema and instance have the same name but
 * a different extension.  Both the uploaded schema and instance are placed 
 * in the forms directory in Chiba with the given filename. Builds an XForm
 * using an automated builder and fixes any input parameters on the form. 
 *  @author Guy Redding 26/11/2004
 */
public class YAWLServlet extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private boolean debug = false; // TODO log4j 
	private SortedSet s = Collections.synchronizedSortedSet(new TreeSet());
	private String sessionHandle;
	private String userID;
	private String specID;
	private String workItemID;
	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		
		if (debug) System.out.println("--- YAWLXForms Servlet ---");
		
		sessionHandle = request.getParameter("sessionHandle");
        if (debug) System.out.println("sessionHandle: "+sessionHandle);
        
		userID = request.getParameter("userID");
        if (debug) System.out.println("userID: "+userID);
		
		String root = request.getParameter("root");
        if (debug) System.out.println("root: "+root);
		
        specID = request.getParameter("specID");
        if (debug) System.out.println("specID: "+specID);
        
        workItemID = request.getParameter("workItemID");
        if (debug) System.out.println("workItemID: "+workItemID);
        
        String task = request.getParameter("task");
        if (debug) System.out.println("XF form name: "+task);

        String wir = request.getParameter("workitem");
        if (debug) System.out.println("WIR XML: "+wir);
        
		if (request.getParameter("schema") != null){
			if (debug) System.out.println("schema = "+request.getParameter("schema"));
			processUpload(request.getParameter("schema"), "schema", task);
		}
		
		if (request.getParameter("instance") != null){
			if (debug) System.out.println("instance = "+request.getParameter("instance"));
			processUpload(request.getParameter("instance"), "instance", task);
		}
		
        // inputParams will appear separated by commas in the format below
        // eg: nameofartist,nameofrecord,nameofsong,
        String inputParams = request.getParameter("inputparams");
        int start = 0;
        
        if (inputParams != null){
	        if (inputParams.compareTo("") != 0){
		        int end = inputParams.length();
		        if (debug) System.out.println("input params: "+inputParams);
		        
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
        // begin Schema2XForms process
        buildForm(task+".xsd", task+".xml", task, root);
	}
	
	
    private void processUpload(String data, String fileType, String fileName) 
    	throws IOException {
       
        ServletContext RP = getServletConfig().getServletContext();
    	
        if (fileType.compareTo("schema") == 0){
        	fileName = fileName.concat(".xsd");
        }
        else if (fileType.compareTo("instance") == 0){
        	fileName = fileName.concat(".xml");
    	}
    	
        // file goes in "forms" directory...
        String filePath = RP.getRealPath(File.separator+"forms"+File.separator+fileName);       
        
        File f = new File(filePath);
        f.createNewFile();
        
        if (debug)
        	System.out.println("New file = "+f.getAbsolutePath());
       
        BufferedWriter bw = null;
        
		try{
			bw = new BufferedWriter(new FileWriter(f, false)); // append = false
		}
		catch(IOException ioe){
			System.out.println(f.getName()+" IO error: "+ioe.toString());
		}
        
		if (bw != null){
			bw.write(data);
			bw.newLine();
			bw.flush();
		}
		bw = null;
    }
    
    
	private void buildForm(String schema, String instance, String form, String root){
		
		Schema2XForms builder = new Schema2XForms(); 
		
	    ServletContext RP = getServletConfig().getServletContext();
	    String filePath = RP.getRealPath(File.separator+"forms");   
		
		// if schema != null etc. set directory to forms, not Tomcat/bin
		if (debug) System.out.println("YAWLServlet: setInputURI to "+schema);
		try{
			builder.setInputURI(filePath+File.separator+schema);
		}
		catch(IOException e){
			System.out.println("YAWLServlet IOException: "+e.toString());
		}
		
		File f = new File(filePath+File.separator+form+".xhtml");
		
		if (debug) System.out.println("YAWLServlet: setInstanceFile to "+instance);
		try{
			builder.setInstanceFile(filePath+File.separator+instance);
		}
		catch(IOException e){
			System.out.println("IOException: "+e.toString());
		}
		
		builder.setInstanceHref("http://www.w3.org/2001/XMLSchema-instance");
		
		// EG: YAWL = "http://localhost:8080/worklist/yawlFormServlet";
		builder.setAction(RP.getInitParameter("YAWL")+"?userID="+userID+
				"&sessionHandle="+sessionHandle+"&specID="+specID+
				"&workItemID="+workItemID);
		
		// EG: FormBase = "http://localhost:8080/YAWLXForms/"
		builder.setBase(RP.getInitParameter("FormBase"));
		builder.setOutputFile(f); // goes in chiba forms directory
		builder.setRootTagName(root);
		builder.setStylesheet("html4yawl.xsl"); 
		builder.setSubmitMethod("post");
		builder.setWrapperType("XHTML");
		builder.execute();
	
		// TODO cleanup of temporary schema and instance files in forms directory (if existing)
		
		// an input param may be a root node for a complex type, meaning that all of its leaf nodes are input as well.
		if (s.size() > 0){
			fixInputParams(f);
		}
		
		s.clear();
	}
    
	
	/**
	 *  now parse the newly created form in DOM.  If a readonly input parameter matches
	 *  a nodeset attribute, add a " xforms:readonly="true()" " attribute to the form and serialise
	 *  the form to file output, overwriting the previously existing form.  if it exists, remove the
	 *  " xforms:required " attribute for nodes that are readonly, because they might be empty,
	 *  which means that the form will never submit if a node is required, is empty and is readonly.
	 * @param f XForms XHTML file with input params to fix.
	 */
	private void fixInputParams(File f){
		
		Document document = null;
		
		try {			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(f);
			
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
	    
	    // attempt to delete any existing xform with the same name
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