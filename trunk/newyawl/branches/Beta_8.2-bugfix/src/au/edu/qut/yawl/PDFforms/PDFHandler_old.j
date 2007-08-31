package au.edu.qut.yawl.PDFforms;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PRAcroForm;

import au.edu.qut.yawl.engine.interfce.interfaceD_WorkItemExecution.*;
import au.edu.qut.yawl.worklist.model.*;

import org.jdom.Element;
import org.apache.commons.fileupload.*;

/**
 * This servlet handles the the manipulation and interaction of PDF related tasks.
 * @author Ignatius Ong
 */
public class PDFHandler extends HttpServlet
{
	private HttpSession sess;
	private HttpServletRequest request;
	private HttpServletResponse response;
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
	throws javax.servlet.ServletException, java.io.IOException {

	    
	String taskname = request.getParameter("taskname");	    
	String specname = request.getParameter("spec");
	
	
	String filename = specname+taskname+".pdf";
	
	//Find the workItemID based on the input parameters
	//Then look up the workitem data string,
	//and then display the form	
	//then  build the filename and stuff based on that

	response.sendRedirect("/PDFforms/complete.jsp?filename="+filename);
	
    }

	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws javax.servlet.ServletException, java.io.IOException
	{
	    //Find the workItemID based on the input parameters
	    //Find the filename...
	    //Then look up the workitem data


	    //Check in the data to the worklist
	    //uploadFile(specname+taskname);
	    
	}

	private boolean fillForm(WorkItemRecord workitem, Iterator itr)
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
				AcroFields form = stamp.getAcroFields();
				String prefix = "";

				while(itr.hasNext())
				{
					Element e = (Element)itr.next();

					//filling fields that are not empty
					if (e.getText() != "")
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


	private void uploadFile(String filename) throws javax.servlet.ServletException, java.io.IOException
	{

 		ServletContext sc = getServletConfig().getServletContext();
		String path = sc.getRealPath("/") + "repository/working/";		
		String workitemStr = "";

		int size = -1;

		System.out.println("-2");

		//checks if the post is a file upload
		if(FileUpload.isMultipartContent(request))
		{
				System.out.println("-1");

			try{
			
				System.out.println("0: " + request);
				//retrieve the PDF file and the work item
				DiskFileUpload fu = new DiskFileUpload();
				System.out.println("1");

				List fileItems = fu.parseRequest(request);

				System.out.println("2");
				//System.out.println(fu.getRepositoryPath());

				FileItem fi = (FileItem)fileItems.get(0);
				System.out.println("3");

                                //System.out.println(fi.getString());

				FileItem fi2 = (FileItem)fileItems.get(1);
				System.out.println("4");

                                //System.out.println(fi2.getString());

				System.out.println("5");
				
				workitemStr = fi2.getString();
				
				System.out.println("This workitem returned: " + workitemStr.substring(0,200));
                                
				System.out.println("6");

				// write the file
				fi.write(new File(path+"filled_"+filename));

			}catch(Exception e)
			{
				e.printStackTrace();
				sess.setAttribute("result","failed");
				response.sendRedirect("./complete2.jsp");
				return;
			}
			processPDF(path+"filled_"+filename, workitemStr);
		}

	}

	private void processPDF(String filename, String workitemStr) throws javax.servlet.ServletException, java.io.IOException
	{

		//unmarshal work item
	    
		WorkItemRecord workitem = Marshaller.unmarshalWorkItem(workitemStr);
		Element element = workitem.getWorkItemData();
		Iterator itr = (element.getChildren()).iterator();

		PdfReader reader = new PdfReader(filename);
		PRAcroForm form = reader.getAcroForm();
		AcroFields fields = reader.getAcroFields();
		ArrayList fieldsList = form.getFields();
		ArrayList outputFields = new ArrayList();

		//retrieve input fields
		for(int i=0; i<fieldsList.size(); i++)
		{
			Object o = fieldsList.get(i);
			if(((PRAcroForm.FieldInformation)o).getName().startsWith("<I>")!=true)
				outputFields.add(((PRAcroForm.FieldInformation)o).getName());
		}

		ArrayList list = new ArrayList();

		//retrieve output fields
		while(itr.hasNext())
		{
			Element e = (Element)itr.next();

			//matching the output fields with the fields of the filled PDF form
			for(int i=0; i<outputFields.size(); i++)
				if (e.getName().equals((String)outputFields.get(i)))
					list.add(e.getName() + ": " + fields.getField(e.getName()));
		}

		sess.setAttribute("result",list);
		response.sendRedirect("./worklist.jsp");
	}

	private boolean fillForm(WorkItemRecord workitem, Iterator itr)
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
		AcroFields form = stamp.getAcroFields();
		String prefix = "";
		
		while(itr.hasNext())
		    {
			Element e = (Element)itr.next();
			
			//filling fields that are not empty
			if (e.getText() != "")
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
    

    public String buildWorkItem(WorklistController _worklistController, String workItemID) {
	WorkItemRecord witem = _worklistController.getRemotelyCachedWorkItem(workItemID);

	System.out.println(workItemID);

	System.out.println(witem);

	StringBuffer xmlBuff = new StringBuffer();
	xmlBuff.append("<workItem>");
	xmlBuff.append("<taskID>" + witem.getTaskID() + "</taskID>");
	xmlBuff.append("<caseID>" + witem.getCaseID() + "</caseID>");
	xmlBuff.append("<uniqueID>" + witem.getUniqueID() + "</uniqueID>");
	xmlBuff.append("<specID>" + witem.getSpecificationID() + "</specID>");
	xmlBuff.append("<status>" + witem.getStatus() + "</status>");
	xmlBuff.append("<data>" + witem.getDataListString() + "</data>");
	xmlBuff.append("<enablementTime>" + witem.getEnablementTime() + "</enablementTime>");
	xmlBuff.append("<firingTime>" + witem.getFiringTime() + "</firingTime>");
	xmlBuff.append("<startTime>" + witem.getStartTime() + "</startTime>");
	xmlBuff.append("<assignedTo>" + witem.getWhoStartedMe() + "</assignedTo>");
	xmlBuff.append("</workItem>");

	return xmlBuff.toString();       

    } 

}