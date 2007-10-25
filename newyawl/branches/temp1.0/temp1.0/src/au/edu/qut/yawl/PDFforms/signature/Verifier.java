package au.edu.qut.yawl.PDFforms.signature;

import org.apache.commons.fileupload.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Calendar;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * This servlet verifies the signature on a uploaded PDF with the ID stored in the ID store.
 * @author Ignatius Ong
 */
public class Verifier extends HttpServlet
{
	private HttpSession sess;
	private HttpServletRequest request;
	private HttpServletResponse response;


	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws javax.servlet.ServletException, java.io.IOException
	{
		sess = request.getSession();
		this.request = request;
		this.response = response;

		ServletContext sc = getServletConfig().getServletContext();
		String path = sc.getRealPath("/") + "secure/genDir/";
		String name = "";
		String fileName = "";

		if(FileUpload.isMultipartContent(request))
		{
			try{

				DiskFileUpload fu = new DiskFileUpload();
				List fileItems = fu.parseRequest(request);
				FileItem fi = (FileItem)fileItems.get(0);
				FileItem fi2 = (FileItem)fileItems.get(1);

				//name of the signer
				name = fi.getString();

				fileName = name.substring(0,name.indexOf(" ")) + name.substring(name.indexOf(" ")+1);

				//upload file
				fi2.write(new File(path+"tmpFile.pdf"));

				verify(path+"tmpFile.pdf",sc.getRealPath("/")+"secure/certificates/"+fileName+".cer");

			}catch(Exception e)
			{
				sess.setAttribute("status","Upload has failed. " + e);
				response.sendRedirect("./status.jsp");
				return;
			}
		}
	}

	private void verify(String pdf, String certFileStr) throws javax.servlet.ServletException, java.io.IOException
	{
		sess.setAttribute("status", "true");
		FileInputStream certFile = new FileInputStream(certFileStr);

		String sigName = "";
		String cover = "";
		String revision = "";

		String subject = "";
		String modified = "";
		String verified = "";

		try
		{
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
				System.out.println("Adding a certificate to keystore: " + certFileStr);
				kall.setCertificateEntry(cert.getSerialNumber().toString(Character.MAX_RADIX), cert);
			}

			//setting the reader for the PDF file
			PdfReader reader = new PdfReader(pdf);
			AcroFields af = reader.getAcroFields();
			ArrayList names = af.getSignatureNames();

			if(names.size()==0)
			{
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
			   System.out.println("verifying finally");
			   Object fails[] = PdfPKCS7.verifyCertificates(pkc, kall, null, cal);
			   System.out.println("verification complete");

			   if (fails == null) 
			       verified = "Signature verified against the KeyStore";
			   else 
			       verified = "Signature failed: " + fails[1] +  " " + fails[0];
			   
			}
			//return data of the signature on the PDF
			sess.setAttribute("sigName",sigName);
			sess.setAttribute("cover",cover);
			sess.setAttribute("revision",revision);
			sess.setAttribute("subject",subject);
			sess.setAttribute("modified", modified);
			sess.setAttribute("verified", verified);
			response.sendRedirect("./status.jsp");
			return;
			
		}catch(Exception e)
		    {
			sess.setAttribute("status","Verification has failed. " + e);
			response.sendRedirect("./status.jsp");
			return;
		    }
		finally
		    {
			File tmp = new File(pdf);
			certFile.close();
			tmp.delete();
		    }
	}
}