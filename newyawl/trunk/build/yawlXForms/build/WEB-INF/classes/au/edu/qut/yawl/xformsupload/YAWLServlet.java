package au.edu.qut.yawl.xformsupload;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
 
import org.apache.log4j.Logger;
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
 * 
 *  @author Guy Redding 26/11/2004
 */
public class YAWLServlet extends HttpServlet{
	
	private static Logger logger = Logger.getLogger(YAWLServlet.class);
	private SortedSet s = Collections.synchronizedSortedSet(new TreeSet());
	private boolean submissionElementsDone = false;
	private boolean submitElementsDone = false;
	private boolean launchCase = false;
	 
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
 	
		submissionElementsDone = false;
		submitElementsDone = false;
		launchCase = false;
		
		String sessionHandle = request.getParameter("sessionHandle");
		String userID = request.getParameter("userID");
		String root = request.getParameter("root");
        String specID = request.getParameter("specID");
        String workItemID = request.getParameter("workItemID");
        String task = request.getParameter("task");
        
        try{
        	task = URLEncoder.encode(task, "UTF-8");
        }
        catch(UnsupportedEncodingException e){
        	e.printStackTrace();
        }
            
        if (workItemID == null){
        	launchCase = true;
        }
        
		if (request.getParameter("schema") != null){
			processUpload(request.getParameter("schema"), "schema", task);
		}
		
		if (request.getParameter("instance") != null){
			processUpload(request.getParameter("instance"), "instance", task);
		}
		
        // the inputParams will be separated by commas in the format below
        // eg: nameofartist,nameofrecord,nameofsong,
        String inputParams = request.getParameter("inputparams");
        int start = 0;
        
        if (inputParams != null){
	        if (inputParams.compareTo("") != 0){
		        int end = inputParams.length();
		        //logger.debug("input params: "+inputParams);
		        
		        // finish parsing when start = end
		        while (start < end){
		        	int newstart = inputParams.indexOf(',', start);
		        	s.add(inputParams.substring(start, newstart));
		        	start = newstart+1;
		        }
	        }
        }
        
        // begin Schema2XForms process
        buildForm(task+".xsd", task+".xml", task, root, 
        		workItemID, sessionHandle, userID, specID);
	}
	
	
    /**
     * Uploads an XML schema or instance file found on the request to the web container. 
     * 
     * @param data
     * @param fileType
     * @param fileName
     * @throws IOException
     */
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
       
        BufferedWriter bw = null;
        
		try{
			bw = new BufferedWriter(new FileWriter(f, false)); // append = false
		}
		catch(IOException ioe){
			logger.debug(f.getName()+" IO error: "+ioe.toString());
		}
        
		if (bw != null){
			bw.write(data);
			bw.newLine();
			bw.flush();
		}
		bw = null;
    }
    
    
	/**
	 * Builds an xform based on the data passed to this servlet.
	 * 
	 * @param schema
	 * @param instance
	 * @param form
	 * @param root
	 * @param workItemID
	 * @param sessionHandle
	 * @param userID
	 * @param specID
	 */
	private void buildForm(String schema, String instance, String form, String root,
			String workItemID, String sessionHandle, String userID, String specID){
		
		Schema2XForms builder = new Schema2XForms(); 
		
	    ServletContext RP = getServletConfig().getServletContext();
	    String filePath = RP.getRealPath(File.separator+"forms");   
		
		try{
			builder.setInputURI(filePath+File.separator+schema);
		}
		catch(IOException e){
			logger.debug("YAWLServlet IOException: "+e.toString());
		}
		
		File f = new File(filePath+File.separator+form+".xhtml");
		
		try{
			builder.setInstanceFile(filePath+File.separator+instance);
		}
		catch(IOException e){
			logger.debug("IOException: "+e.toString());
		}
		
		builder.setInstanceHref("http://www.w3.org/2001/XMLSchema-instance");
		
		// EG: YAWL = "http://localhost:8080/worklist/yawlFormServlet";
		builder.setAction(RP.getInitParameter("YAWL")+"?userID="+userID+
				"&sessionHandle="+sessionHandle+"&specID="+specID+
				"&workItemID="+workItemID+"&submit=submit");
		
		// EG: FormBase = "http://localhost:8080/YAWLXForms/"
		builder.setBase(RP.getInitParameter("FormBase"));
		builder.setOutputFile(f); // goes in chiba forms directory
		builder.setRootTagName(root);
		builder.setStylesheet("html4yawl.xsl"); 
		builder.setSubmitMethod("post");
		builder.setWrapperType("XHTML");
		builder.execute();
		
		//deleteTempFile(filePath+File.separator+schema);
		//deleteTempFile(filePath+File.separator+instance);
		
		fixFormParams(f);
		
		s.clear();
		f = null;
	}
    
	
	/**
	 * Attempts to deletes the file passed to this method.
	 * 
	 * @param filename
	 * @return true/false, depending on the success of the deletion operation.
	 */
	private boolean deleteTempFile(String filename){
		
		File temp = new File(filename);		
		return temp.delete();
	}
	
	
	/**
	 *  now parse the newly created form in DOM.  If a readonly input parameter matches
	 *  a nodeset attribute, add a " xforms:readonly="true()" " attribute to the form and serialise
	 *  the form to file output, overwriting the previously existing form.  if it exists, remove the
	 *  " xforms:required " attribute for nodes that are readonly, because they might be empty,
	 *  which means that the form will never submit if a node is required, is empty and is readonly.
	 *  
	 * @param f xForms .xhtml file with input params to fix.
	 */
	private void fixFormParams(File f){
		
		Document document = null;
		
		try {			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(f);
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
	    // cauz we can't trust the web container to "overwrite" the xform.
	    if (f.exists()){
	    	f.delete();
	    }
	    
		try{
			bw = new BufferedWriter(new FileWriter(f, false));
		}
		catch (IOException e){
			logger.debug("IO file error: "+e.toString());
		}

		try {
			OutputFormat format = new OutputFormat(document);
			XMLSerializer output = new XMLSerializer(bw, format);
			output.serialize(document);
		}
		catch (IOException e) {
			logger.debug(e);
		}
	}
	
	
	/**
	 * Recursive method for traversal of a DOM. Adds necessary information to the xform
	 * for input only params.
	 * 
	 * @param document
	 * @param node
	 */
	private void nodeDetails (Document document, Node node) {
		
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			
			// if input params exist (s.size()>0), check if the node has any attributes. 
			// filter out anonymous complex types, which we don't want to add
			if (s.size() > 0){
				if ((node.hasAttributes() == true) && (node.getNodeName().compareTo("xforms:bind") == 0)){
					
					NamedNodeMap AttributesList = node.getAttributes();
					
					for (int j = 0; j < AttributesList.getLength(); j++) {
	
						// cycle thru attributes, report match if the element is "xforms:bind"
						// and "xforms:nodeset" equals the name of an input parameter.
						if ( (AttributesList.item(j).getNodeName().compareTo("nodeset") == 0) &&
								(s.contains(AttributesList.item(j).getNodeValue()) == true) ){
	
							Element element = (Element) node;
							Document factory = node.getOwnerDocument();
							
							// if it exists, edit the "xforms:required" attribute to be false
							for (int i = 0; i < AttributesList.getLength(); i++) {
								if (AttributesList.item(i).getNodeName().compareTo("required") == 0){
									// edit the required attribute from this node to be false
									Attr newAttrib = factory.createAttribute("required");
									newAttrib.setValue("false()");
									element.setAttributeNode(newAttrib);
								}
							}
							Attr specifiedAttribute = factory.createAttribute("readonly");
							specifiedAttribute.setValue("true()");
							element.setAttributeNode(specifiedAttribute);
						}
					}
				}
			}
			
			
			if (node.getNodeName().compareTo("submission") == 0 && submissionElementsDone == false){
				// create cancel button submission node
				//eg: <xforms:submission id="submission_1" xforms:action="http://localhost:8080/worklist/yawlFormServlet?userID=admin&amp;sessionHandle=3456218449289224029&amp;specID=null&amp;workItemID=100000.1:Call_for_papers_5&amp;JSESSIONID=D6B01B27183706B536BE90204788DC71&amp;submit=cancel" xforms:method="post"/>
				
				// if launching a case, don't add suspend or save elements to a form
				if (launchCase == false){
					addSubmissionElement(node, "suspend", 1);
					addSubmissionElement(node, "save", 2);
				}
				
				addSubmissionElement(node, "cancel", 3);
				submissionElementsDone = true;
			}
			if (node.getNodeName().compareTo("submit") == 0 && submitElementsDone == false){
				// create submit child
				//eg: <xforms:submit xforms:id="submit_1" xforms:submission="submission_1">
				//<xforms:label xforms:id="label_4">Cancel</xforms:label>
				//</xforms:submit>
				
				if (launchCase == false){
					addSubmitElement(node, "Suspend", 1);
					addSubmitElement(node, "Save", 2);
				}
				
				addSubmitElement(node, "Cancel", 3);
				submitElementsDone = true;
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
	
	// TODO for every xforms:insert and xforms:delete, replace the "at" attribute with at="last()" 
	// or something similar that works
	
	
	/**
	 * Adds a submission element to a xform.
	 * @param node
	 * @param submissionType
	 * @param number
	 */
	private void addSubmissionElement(Node node, String submissionType, int number){
		
		Document factory = node.getOwnerDocument();
		Node parent = node.getParentNode();
		Element newElement = factory.createElement("xforms:submission");
		NamedNodeMap AttributesList = node.getAttributes();
		String action = new String();
		
		for (int j = 0; j < AttributesList.getLength(); j++) {
			if (AttributesList.item(j).getNodeName().compareTo("action") == 0){
				action = AttributesList.item(j).getNodeValue();
				action = action.substring(0, action.indexOf("=submit")).concat("="+submissionType);
			}
		}

		Attr newAttrib1 = factory.createAttribute("id");
		newAttrib1.setValue("submission_"+number);
		
		Attr newAttrib2 = factory.createAttribute("action");
		newAttrib2.setValue(action);

		Attr newAttrib3 = factory.createAttribute("validate");
		newAttrib3.setValue("false");
		
		Attr newAttrib4 = factory.createAttribute("method");
		newAttrib4.setValue("post");
		
		newElement.setAttributeNode(newAttrib1);
		newElement.setAttributeNode(newAttrib2);
		newElement.setAttributeNode(newAttrib3);
		newElement.setAttributeNode(newAttrib4);
		
		parent.appendChild(newElement);
	}
	
	
	/**
	 * Adds a xform submit element. The submit element is bound to a 
	 * particular submission element indicated by a submission ID.
	 * @param node
	 * @param submitType
	 * @param number
	 */
	private void addSubmitElement(Node node, String submitType, int number){
		
		Document factory = node.getOwnerDocument();
		Node parent = node.getParentNode();
		Element newElement = factory.createElement("xforms:submit");
		
		Attr newAttrib1 = factory.createAttribute("id");
		newAttrib1.setValue("submit_"+number);
		
		Attr newAttrib2 = factory.createAttribute("submission");
		newAttrib2.setValue("submission_"+number);
		
		newElement.setAttributeNode(newAttrib1);
		newElement.setAttributeNode(newAttrib2);
		
		parent.appendChild(newElement);
		
		Element child = factory.createElement("xforms:label");
		
		Attr newAttrib3 = factory.createAttribute("id");
		newAttrib3.setValue(submitType+"_label");
		child.setAttributeNode(newAttrib3);
		child.setTextContent(submitType);
		
		newElement.appendChild(child);
	}
}