package au.edu.qut.yawl.PDFforms;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.File;

// import the iText packages
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;


/**
 * This servlet generates PDF files and writes the file onto a repository.
 * @author Ignatius Ong
 */
public class PDFGenerator extends HttpServlet
{
	private HttpSession sess;
	private HttpServletRequest req;

	public PDFGenerator() {
		super();
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws javax.servlet.ServletException, java.io.IOException
	{
		sess = req.getSession();
		this.req = req;

		DocumentException ex = null;

		ByteArrayOutputStream baosPDF = null;

		try
		{
			//create PDF file
			baosPDF = generatePDFDocumentBytes(req, this.getServletContext());

			//setting the paths for writing the PDF
			String fileName = (String) sess.getAttribute("fileName");
			ServletContext sc = getServletConfig().getServletContext();
			String path = sc.getRealPath("/") + "repository/";
			FileOutputStream out = new FileOutputStream(new File(path+fileName));
			baosPDF.writeTo(out);
			out.flush();
			out.close();

			//redirect to conclusion page
			RequestDispatcher disp = getServletContext().getRequestDispatcher("/finish_1.jsp");
			disp.forward(req, resp);

		}
		catch (DocumentException dex)
		{
			resp.setContentType("text/html");
			PrintWriter writer = resp.getWriter();
			writer.println(
					this.getClass().getName()
					+ " caught an exception: "
					+ dex.getClass().getName()
					+ "<br>");
			writer.println("<pre>");
			dex.printStackTrace(writer);
			writer.println("</pre>");
		}
		finally
		{
			if (baosPDF != null)
			{
				baosPDF.reset();
			}
		}

	}//end doGet

		//generates PDF file
		protected ByteArrayOutputStream generatePDFDocumentBytes(
			final HttpServletRequest req,
			final ServletContext ctx)
			throws DocumentException

		{

			//retrieving mandatory form fields
			String author = (String) sess.getAttribute("author");
			String subject = (String) sess.getAttribute("subject");
			String header = (String) sess.getAttribute("header");
			String footer = (String) sess.getAttribute("footer");
			String logo = (String) sess.getAttribute("logo");
			String instructions = (String) sess.getAttribute("instructions");
			String conclusion = (String) sess.getAttribute("conclusion");
			String keywords = (String) sess.getAttribute("keywords");
			String title = (String) sess.getAttribute("title");
			String strFields = (String) sess.getAttribute("fields");
			String signature = (String) sess.getAttribute("signature");
			int fields = Integer.parseInt(strFields);

			Document document = new Document();

			ByteArrayOutputStream baosPDF = new ByteArrayOutputStream();
			PdfWriter docWriter = null;

			try
			{
				docWriter = PdfWriter.getInstance(document, baosPDF);

				document.addAuthor(author);
				//doc.addCreationDate();
				//document.addProducer();
				document.addCreator("YAWL PDF Generator");
				document.addTitle(title);

				if(keywords!=null)
					document.addKeywords(keywords);


				if(header!=null)
				{
					HeaderFooter hfHeader = new HeaderFooter(new Phrase(header), false);
					document.setHeader(hfHeader);
				}

				if(footer!=null)
				{
					HeaderFooter hfFooter = new HeaderFooter(new Phrase(footer), new Phrase("."));
					hfFooter.setAlignment(Element.ALIGN_CENTER);
					document.setFooter(hfFooter);
				}

 				document.addSubject(subject);

				document.open();


				//creating an image instance if it exists
				if(logo!=null)
				{
					Image logoImage = Image.getInstance(logo);
					//setting image size and alignment
					logoImage.scaleToFit(279,55);
					logoImage.setAlignment(0);

					//add image
					document.add(logoImage);
				}

				document.add(new Paragraph(instructions));
				document.add(new Paragraph(" "));


				document = CreatePDF.create(document, docWriter, req, fields);
				document.add(new Paragraph(" "));

				if(conclusion!=null)
					document.add(new Paragraph(conclusion));

				//writing signature field
				if(signature.equals("Yes"))
				{
					PdfAcroForm acroForm = docWriter.getAcroForm();
					acroForm.addSignature("signatureField", 73, 75, 149, 135);

				}

			}
			catch (DocumentException dex)
			{
				baosPDF.reset();
				throw dex;
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				if (document != null)
				{
					document.close();
				}
				if (docWriter != null)
				{
					docWriter.close();
				}
			}

			if (baosPDF.size() < 1)
			{
				throw new DocumentException(
					"document has "
					+ baosPDF.size()
					+ " bytes");
			}
			return baosPDF;
		}


}