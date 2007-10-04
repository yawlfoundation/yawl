package au.edu.qut.yawl.PDFforms;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;
import java.util.Calendar;

import org.apache.commons.fileupload.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;


import au.edu.qut.yawl.engine.interfce.interfaceD_WorkItemExecution.*;
import au.edu.qut.yawl.worklist.model.*;

import org.jdom.Element;

/**
 * This servlet handles the the manipulation and interaction of PDF related tasks.
 * @author Ignatius Ong
 */
public class PDFHandler extends HttpServlet
{
    
    //private InterfaceD_Client idclient = new InterfaceD_Client("http://131.181.70.9:8080/PDFforms/interfaceD");
    
    private HttpSession sess;
	private HttpServletRequest request;
	private HttpServletResponse response;


	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws javax.servlet.ServletException, java.io.IOException
	{

	    System.out.println("Post request received by PDF");

		sess = request.getSession();
		this.request = request;
		this.response = response;

		String workitemStr = null;
		workitemStr = request.getParameter("workitem");
		String decompositionID = request.getParameter("decompositionID");
		String username = request.getParameter("username");
		sess.setAttribute("workitemStr",workitemStr);

		System.out.println("WorkItem for PDF: " + workitemStr);

		//either display a form or upload PDF file
		if (workitemStr!=null)
		{
			if(workitemStr.equals("")!=true)
			    displayForm(workitemStr,decompositionID,username);
			else
				response.sendRedirect("../Test.jsp"); //if workitem string is empty, redirects to test page
		}
		else
			uploadFile();
	}

	private void uploadFile() throws javax.servlet.ServletException, java.io.IOException
	{

	    	   
 		ServletContext sc = getServletConfig().getServletContext();
		String path = sc.getRealPath("/") + "repository/working/";
		String filename = (String)request.getParameter("filename");
		System.out.println("filename: " + filename);
		String workitemStr = "";

		int size = -1;
		boolean result = false;
		
		//checks if the post is a file upload
		if(FileUpload.isMultipartContent(request))
		    {
			try{
			    String securepath = sc.getRealPath("/") + "secure/genDir/";

			    //retrieve the PDF file and the work item
			    DiskFileUpload fu = new DiskFileUpload();
			    List fileItems = fu.parseRequest(request);
			    
			    FileItem fi = (FileItem)fileItems.get(0);
			    
			    FileItem fi2 = (FileItem)fileItems.get(1);

			    filename = fi2.getString();
			    System.out.println("filename: " + filename);
			    
			    //name of the signer....get from somewhere...e.g. worklist...who is logged in

			    
			    // write the file
			    fi.write(new File(path+"filled_"+filename));

			    String pdf = path+"filled_"+filename;

			    result = verify(pdf);
			    
			    }catch(Exception e) {
			    e.printStackTrace();
			    sess.setAttribute("result","failed");
			    response.sendRedirect("../complete2.jsp");
			    return;
			}
			if (result) {
			    processPDF(path+"filled_"+filename);
			} else {
			    response.sendRedirect("./status.jsp");
			}
		    }	
		
	}

	private void processPDF(String filename) throws javax.servlet.ServletException, java.io.IOException {
	    PdfReader reader = new PdfReader(filename);
	    System.out.println("This workitem contains:" + reader.getInfo());
	    
	    String workitemStr = (String) reader.getInfo().get("workitem");
	    String decompositionID = (String) reader.getInfo().get("decompositionID");
	    System.out.println("workitemstr: " + workitemStr);

		//unmarshal work item
		WorkItemRecord workitem = Marshaller.unmarshalWorkItem(workitemStr);

		PRAcroForm form = reader.getAcroForm();
		AcroFields fields = reader.getAcroFields();
		ArrayList fieldsList = form.getFields();
		ArrayList outputFields = new ArrayList();

		//retrieve input fields
		for(int i=0; i<fieldsList.size(); i++) {
		    Object o = fieldsList.get(i);
		    outputFields.add(((PRAcroForm.FieldInformation)o).getName());
		}
		
		ArrayList list = new ArrayList();
		
		String datastring = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<"+decompositionID+">\n";
		String dataend = "</"+decompositionID+">";
		//retrieve output fields
		
		//matching the output fields with the fields of the filled PDF form
		for(int i=0; i<outputFields.size(); i++) {
		    String dataname = (String) outputFields.get(i);
		    datastring = datastring 
			+ "<"+dataname+">"+fields.getField(dataname)+ "</"+dataname+">\n";
		}
		
		datastring = datastring + dataend;
		System.out.println("returning data: " + datastring);
		/*
		  The list contains a set of output fields which are to be checked back into the worklist
		  create data based on it
		*/
		


		//workitem.getOutputData();
		try {
		    HashMap queryMap = new HashMap();
		    StringBuffer xmlBuff = new StringBuffer();
		    xmlBuff.append("<workItem>");
		    xmlBuff.append("<taskID>" + workitem.getTaskID() + "</taskID>");
		    xmlBuff.append("<caseID>" + workitem.getCaseID() + "</caseID>");
		    xmlBuff.append("<uniqueID>" + workitem.getUniqueID() + "</uniqueID>");
		    xmlBuff.append("<specID>" + workitem.getSpecificationID() + "</specID>");
		    xmlBuff.append("<status>" + workitem.getStatus() + "</status>");
		    xmlBuff.append("<data>" + workitem.getDataListString() + "</data>");
		    xmlBuff.append("<enablementTime>" + workitem.getEnablementTime() + "</enablementTime>");
		    xmlBuff.append("<firingTime>" + workitem.getFiringTime() + "</firingTime>");
		    xmlBuff.append("<startTime>" + workitem.getStartTime() + "</startTime>");
		    xmlBuff.append("<assignedTo>" + workitem.getWhoStartedMe() + "</assignedTo>");
		    xmlBuff.append("</workItem>");
		    
		    queryMap.put("workitem", xmlBuff.toString());
		    

		    System.out.println("inputdata: " + workitem.getDataListString());
		    
		    Element inputData = null;
		    Element outputData = null;
		    SAXBuilder _builder = new SAXBuilder();

		    try {
			Document inputDataDoc = _builder.build(new StringReader(workitem.getDataListString()));
			inputData = inputDataDoc.getRootElement();
			
			Document outputDataDoc = _builder.build(new StringReader(datastring));
			outputData = outputDataDoc.getRootElement();
		    } catch (JDOMException e) {
			e.printStackTrace();
		    }
		    



		    request.setAttribute("inputData", inputData);
		    request.setAttribute("outputData", outputData);

		    request.setAttribute("workItemID", workitem.getID());
		    request.setAttribute("submit", "Submit Work Item");
		    
		    RequestDispatcher rd = request.getRequestDispatcher("/workItemProcessor");
		    rd.forward(request, response);		    

		    //InterfaceD_Client.executePost("http://131.181.70.9:8080/PDFforms/interfaceD" + "", queryMap);
		    
		//idclient.sendWorkItem(workitem);

		} catch (Exception e) {
		    e.printStackTrace();
		}
		   

		//response.sendRedirect("../worklist.jsp");
	}


    private void displayForm(String workitemStr, String decompositionID, String username) throws javax.servlet.ServletException, java.io.IOException
	{
		//unmarshal work item
		WorkItemRecord workitem = Marshaller.unmarshalWorkItem(workitemStr);
		Element element = workitem.getWorkItemData();
		String filename = workitem.getSpecificationID() + workitem.getTaskID() + workitem.getUniqueID() + ".pdf";

		System.out.println("Displaying form for WorkItem:" + workitemStr);

		//get form fields
		Iterator itr = (element.getChildren()).iterator();

		//fill form with fields
		if (fillForm(workitem,itr,decompositionID,username))
			sess.setAttribute("filename",filename);
		else
			sess.setAttribute("filename","error");

		//response.sendRedirect("../complete.jsp");
	}

    private boolean fillForm(WorkItemRecord workitem, Iterator itr, String decompositionID, String username)
	{
	    try {
		ServletContext sc = getServletConfig().getServletContext();
		String path = sc.getRealPath("/") + "repository/";
		
		// create a reader for a certain document
		PdfReader reader = new PdfReader(path + workitem.getSpecificationID() + workitem.getTaskID() + ".pdf");
		PRAcroForm f = reader.getAcroForm();
		ArrayList list = f.getFields();
		ArrayList inputFields = new ArrayList();
		ArrayList outputFields = new ArrayList();
		
		for(int i=0; i<list.size(); i++)
		    {
			Object o = list.get(i);
			if(((PRAcroForm.FieldInformation)o).getName().startsWith("<I>"))
			    inputFields.add((((PRAcroForm.FieldInformation)o).getName()).substring(3));
			else
			    outputFields.add(((PRAcroForm.FieldInformation)o).getName());
		    }
		
		// filling in the form
		PdfStamper stamp = new PdfStamper(reader, new FileOutputStream(path+"working/"+ workitem.getSpecificationID() + workitem.getTaskID() + workitem.getUniqueID() + ".pdf"));

		HashMap hash = new HashMap();
		
		StringBuffer xmlBuff = new StringBuffer();
		xmlBuff.append("<workItem>");
		xmlBuff.append("<taskID>" + workitem.getTaskID() + "</taskID>");
		xmlBuff.append("<caseID>" + workitem.getCaseID() + "</caseID>");
		xmlBuff.append("<uniqueID>" + workitem.getUniqueID() + "</uniqueID>");
		xmlBuff.append("<specID>" + workitem.getSpecificationID() + "</specID>");
		xmlBuff.append("<status>" + workitem.getStatus() + "</status>");
		xmlBuff.append("<data>" + workitem.getDataListString() + "</data>");
		xmlBuff.append("<enablementTime>" + workitem.getEnablementTime() + "</enablementTime>");
		xmlBuff.append("<firingTime>" + workitem.getFiringTime() + "</firingTime>");
		xmlBuff.append("<startTime>" + workitem.getStartTime() + "</startTime>");
		xmlBuff.append("<assignedTo>" + workitem.getWhoStartedMe() + "</assignedTo>");
		xmlBuff.append("</workItem>");
		
		hash.put("workitem",xmlBuff.toString());
		hash.put("decompositionID",decompositionID);
		hash.put("username",username);
		stamp.setMoreInfo(hash);
		
		AcroFields form = stamp.getAcroFields();
		String prefix = "";
		
		    //setXmpMetadata(workitem.toXML().getBytes());

		//stamp.getWriter().open();
		while(itr.hasNext())
		    {
			Element e = (Element)itr.next();
			
			//filling fields that are not empty
			if (!e.getText().equals(""))
			    {
				for(int i=0; i<inputFields.size(); i++)
				    {
					if (e.getName().equals((String)inputFields.get(i)))
					    prefix = "<I>";
				    }
				
				form.setField(prefix + e.getName(), e.getText());
				prefix = "";
			    }
			
		    }
		//stamp1.setFormFlattening(true);
		stamp.close();
	    }
	    catch (Exception de) {
		de.printStackTrace();
		return false;
	    }
	    return true;
	}
    

	private boolean verify(String pdf) throws javax.servlet.ServletException, java.io.IOException
	{

	    FileInputStream certFile = null;
	    String sigName = "";
	    String cover = "";
	    String revision = "";
	    
	    String subject = "";
	    String modified = "";
	    String verified = "";
	    
		try
		    {
			
			PdfReader reader = new PdfReader(pdf);
			String fileName = (String) reader.getInfo().get("username");			    

			System.out.println("Checked out by: " + fileName);

			ServletContext sc = getServletConfig().getServletContext();
			String certFileStr = sc.getRealPath("/")+"secure/certificates/"+fileName+".cer";
			
			
			certFile = new FileInputStream(certFileStr);
			
			
			//setting security provider as BouncyCastle
			java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

			//retrieving public certificate
			CertificateFactory cf = CertificateFactory.getInstance("X509");
			Collection col = cf.generateCertificates(certFile);
			KeyStore kall = KeyStore.getInstance(KeyStore.getDefaultType());
			kall.load(null, null);

			//retrieves every certificate in the file, but for this case, there is only one
			for (Iterator it = col.iterator(); it.hasNext();) {
				X509Certificate cert = (X509Certificate)it.next();
				kall.setCertificateEntry(cert.getSerialNumber().toString(Character.MAX_RADIX), cert);
			}

			System.out.println("verifying the identity");

			//setting the reader for the PDF file
			AcroFields af = reader.getAcroFields();
			ArrayList names = af.getSignatureNames();

			if(names.size()==0)
			{
			    System.out.println("verifying the identity- no signatures ");
				sess.setAttribute("status","There are no signatures in this PDF.");
			}

			//reading signatures in the form - there can be multiple signatures on the form
			for (int k = 0; k < names.size(); ++k) {
			   String name = (String)names.get(k);
			   sigName = name;
			   cover = af.signatureCoversWholeDocument(name) + "";
			   revision = af.getRevision(name) + " of " + af.getTotalRevisions();

			   //verify signature
			   PdfPKCS7 pk = af.verifySignature(name,"BC");
			   Calendar cal = pk.getSignDate();
			   Certificate pkc[] = pk.getCertificates();
			   subject = PdfPKCS7.getSubjectFields(pk.getSigningCertificate()).toString();
			   //modified = !pk.verify() + "";
			   modified = (af.getTotalRevisions()>1) + "";

			   Object fails[] = PdfPKCS7.verifyCertificates(pkc, kall, null, cal);

			   if (fails == null) {
			       System.out.println("verification is ok");
				   verified = "Signature verified against the KeyStore";
				   return true;
			   }
			   else {
			       verified = "Signature failed: " + fails[1];
			       System.out.println(verified);

			       sess.setAttribute("status","Verification has failed.");
			       return false;
			   }
			   
			}
			
		}catch(Exception e)
		    {
			sess.setAttribute("status","Verification has failed. ");
			e.printStackTrace();
			return false;
		    }
		finally
		    {
			certFile.close();
		    }
		return false;
	}
    
}

