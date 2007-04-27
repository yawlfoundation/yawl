package au.edu.qut.yawl.PDFforms;

import java.io.FileOutputStream;
import java.io.IOException;
import javax.servlet.http.*;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

/**
 * Creates PDF by returning a PDF Document object to the calling class. This class creates
 * PDF forms based on the parameters in the HTTPServletRequest object.
 *
 * @author Ignatius Ong 15/10/2005
 */
public class CreatePDF implements PdfPCellEvent {

	private PdfWriter writer;

	/** the current fieldname */
	private String fieldname = "NoName";
	private boolean readOnly = false;


	public CreatePDF(PdfWriter writer, String fieldname, boolean readOnly) {
		this.writer = writer;
		this.fieldname = fieldname;
		this.readOnly = readOnly;
	}

	/**
	 * @see com.lowagie.text.pdf.PdfPCellEvent#cellLayout(com.lowagie.text.pdf.PdfPCell,
	 *      com.lowagie.text.Rectangle, com.lowagie.text.pdf.PdfContentByte[])
	 */
	public void cellLayout(PdfPCell cell, Rectangle position,
			PdfContentByte[] canvases) {
		TextField tf = new TextField(writer, position, fieldname);

		//sets the form field as read only
		if(readOnly)
			tf.setOptions(BaseField.READ_ONLY);

		//font size st to 12 by default
		tf.setFontSize(12);
		try {
			PdfFormField field = tf.getTextField();
			writer.addAnnotation(field);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

   /**
     * Creates a PDF form section by generating form fields
     * @param document 		document object of the PDF file
     * @param writer		PDF writer
     * @param req			request object from the calling servlet
     * @param fields		number of fields in this form
     */
	public static Document create(Document document, PdfWriter writer, HttpServletRequest req, int fields) {

		try {

				//creating a table with 2 columns
				PdfPTable table = new PdfPTable(2);
				PdfPCell cell;
				table.getDefaultCell().setPadding(5f);
				boolean readOnly = false;
				String prefix = "";

				for(int i=0; i<fields; i++)
				{
					//retrieve each form field name
					if(req.getParameter("fr"+i)!=null)
					{
						//if field is read-only, add a prefix <I> to name
						readOnly = true;
						prefix = "<I>";
					}
					else
					{
						readOnly = false;
						prefix = "";
					}

					//create a cell for each field
					table.addCell(req.getParameter("f"+i));
					cell = new PdfPCell();
					cell.setCellEvent(new CreatePDF(writer, prefix+req.getParameter("f"+i), readOnly));
					table.addCell(cell);
				}

				//adds the table - form fields onto the PDF document
				document.add(table);

		}
		catch(DocumentException de) {
			System.err.println(de.getMessage());
		}

		return document;

	}
}