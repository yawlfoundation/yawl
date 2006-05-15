package au.edu.qut.yawl.xformsupload;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * Uploads a XML Schema or XML instance file with the given name set as a 
 * header in the request.  The schema and instance have the same name but
 * a different extension.  Both the uploaded schema and instance are placed 
 * in the forms directory in Chiba with the given filename.
 *  @author Guy Redding 26/11/2004
 */
public class YAWLServlet extends HttpServlet{
	
	private boolean debug = false;
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		
		String filename = null;
		
		if (request.getHeader("schema") != null){
			filename = new String(request.getHeader("schema"));
			if (debug){
				System.out.println("YAWLServlet doPost: schema = "+filename);
			}
			processUpload(request, filename);
		}
		else if (request.getHeader("instance") != null){
			filename = new String(request.getHeader("instance"));
			if (debug){
				System.out.println("YAWLServlet doPost: instance = "+filename);
			}
			processUpload(request, filename);
		}
	}

	
    public void processUpload(HttpServletRequest request, String fileName) throws IOException{
        StringBuffer result = new StringBuffer();
        ServletInputStream in = request.getInputStream();
        
        int i = in.read();
        while (i != -1) {
            result.append((char) i);
            i = in.read();
        }
        in.close();
        
        ServletContext RP = getServletConfig().getServletContext();
        // file goes in "forms" directory...
        String filePath = RP.getRealPath(File.separator+"forms"+File.separator+fileName);       
        
        File f = new File(filePath);
        
        BufferedWriter bw = null;
        
        if (debug){
        	System.out.println("New YAWL file = "+f.getAbsolutePath());
        	System.out.println("RP YAWL File = "+filePath);
        }
        
        f.createNewFile();
        
        String fileResult = new String(result); 
        
		try{
			bw = new BufferedWriter(new FileWriter(f, false));
		}
		catch(IOException ioe){
			System.out.println(f.getName()+" IO error: "+ioe.toString());
		}
        
		if (bw != null){
			bw.write(fileResult);
			bw.newLine();
			bw.flush();
		}
		bw = null;
    }
}