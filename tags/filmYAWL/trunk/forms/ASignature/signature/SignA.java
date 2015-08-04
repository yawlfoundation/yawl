package signature;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JPanel;

import netscape.javascript.JSObject;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

public class SignA extends JApplet{

	private static final long serialVersionUID = 1L;
	private final List<String> errorFields = new ArrayList<String>();
	private final List<String> validFields = new ArrayList<String>();
	private final String JS_METHOD_DISPLAYING_ERRORS = "showFieldsInError";
	private final String JS_METHOD_DISPLAYING_VALID_FIELDS = "showValidFields";
	
	private JPanel jContentPane = null;
	private JPanel canvasPanel = null;
	private JPanel buttonsPanel = null;
	private JButton clearButton = null;
	private JButton saveButton = null;
	private Canvas cv;
	private String load_url = null;  //  @jve:decl-index=0:
	private String save_url = null;  //  @jve:decl-index=0:
	static String ver = "SignA v 1.0"; //  @jve:decl-index=0:
	private JSObject win = null;
	private JSObject artist = null;
	private JSObject artist_pu = null;
	private JSObject artist_muwdcall_scheduled = null;
	private JSObject artist_muwdcall_actual = null;
	private JSObject artist_meal = null;
	private JSObject artist_wrap = null;
	private JSObject artist_travel = null;
    private JSObject artist_signature;

    /**
	 * This is the default constructor
	 */
	public SignA() {}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	public void init() {
		this.setContentPane(getJContentPane());
		clearErrors();
		retrieveFields();
		load_url = getParameter("load_url");
	    if(load_url != null)
	        //loadImage();
	    	System.out.println("File loaded8");

	    
	    if (getParameter("save_url") != null) {
	    	save_url = getParameter("save_url");
	    }
	    
        System.out.println("save url is: " + save_url);
	}

	private void clearErrors() {
		errorFields.clear();
	}
	
	private void addErrorField(JSObject field) {
		errorFields.add(field.getMember("id").toString());
	}
	
	private void addValidField(JSObject field) {
		validFields.add(field.getMember("id").toString());
	}

	private boolean checkFields() {
		boolean check=true;
		if (artist.getMember("value").toString().compareTo("")==0){
			check=false;
			addErrorField(artist);
		}
		if (artist_pu.getMember("value").toString().compareTo("")==0){
			check=false;
			addErrorField(artist_pu);
	    }
		try{
			XMLGregorianCalendarImpl.parse(artist_muwdcall_scheduled.getMember("value").toString());
		}
		catch (Exception e){
			check=false;
			addErrorField(artist_muwdcall_scheduled);
		}
		try{
			XMLGregorianCalendarImpl.parse(artist_muwdcall_actual.getMember("value").toString());
		}
		catch (Exception e){
			check=false;
			addErrorField(artist_muwdcall_actual);
		}
		try{
			XMLGregorianCalendarImpl.parse(artist_meal.getMember("value").toString());
		}
		catch (Exception e){
			check=false;
			addErrorField(artist_meal);
		}
		try{
			XMLGregorianCalendarImpl.parse(artist_wrap.getMember("value").toString());
		}
		catch (Exception e){
			check=false;
			addErrorField(artist_wrap);
		}
		try{
			XMLGregorianCalendarImpl.parse(artist_travel.getMember("value").toString());
		}
		catch (Exception e){
			check=false;
			addErrorField(artist_travel);
		}
		return check;
	}
	
	/**
	 * This method initializes canvasPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCanvasPanel() {
		if (canvasPanel == null) {
			canvasPanel = new JPanel();
			canvasPanel.setLayout(new BoxLayout(getCanvasPanel(), BoxLayout.Y_AXIS));
			canvasPanel.setPreferredSize(new Dimension(getSize().width, getSize().height-30));
			canvasPanel.setBorder(BorderFactory.createLineBorder(Color.lightGray, 1));
			cv = new Canvas();
			cv.setPreferredSize(new Dimension(350, getSize().height));
			canvasPanel.add(cv, null);
		}
		return canvasPanel;
	}

	/**
	 * This method initializes buttonsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getButtonsPanel() {
		if (buttonsPanel == null) {
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.fill = GridBagConstraints.NONE;
			gridBagConstraints1.insets = new Insets(3, 0, 7, 0);
			gridBagConstraints1.gridy = 1;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.insets = new Insets(7, 0, 3, 0);
			gridBagConstraints.fill = GridBagConstraints.NONE;
			gridBagConstraints.gridy = 0;
			buttonsPanel = new JPanel();
			buttonsPanel.setLayout(new GridBagLayout());
			buttonsPanel.setPreferredSize(new Dimension(30, getSize().height));
			buttonsPanel.setBackground(Color.white);
			buttonsPanel.add(getClearButton(), gridBagConstraints);
			buttonsPanel.add(getSaveButton(), gridBagConstraints1);
		}
		return buttonsPanel;
	}

	/**
	 * This method initializes clearButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getClearButton() {
		if (clearButton == null) {
			clearButton = new JButton(new ImageIcon(getClass().getResource("/icons/add_16.gif")));
			clearButton.setPreferredSize(new Dimension(20, 20));
			clearButton.setFocusable(false);
			//clearButton.setEnabled(false);
			clearButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					cv.onErase();
				}
			});
		}
		return clearButton;
	}

	/**
	 * This method initializes saveButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getSaveButton() {
		if (saveButton == null) {
			saveButton = new JButton(new ImageIcon(getClass().getResource("/icons/ok_16.gif")));
			saveButton.setPreferredSize(new Dimension(20, 20));
			saveButton.setFocusable(false);
			//saveButton.setEnabled(false);
			saveButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {					
					if (save_url!=null){
						System.out.println("Save url is not null. checking fields.");
						if (checkFields()){
							saveImage(cv.image);
							win.call(JS_METHOD_DISPLAYING_VALID_FIELDS, new Object[] {validFields.toArray(new String[] {})});
						}
						else{
							System.out.println("Incorrect format of fields. : " + errorFields);
							win.call(JS_METHOD_DISPLAYING_ERRORS, new Object[] {errorFields.toArray(new String[] {})});
							resetStyleOnFieldsNotInError();
							clearErrors();
						}											
					}
			  }

				private void resetStyleOnFieldsNotInError() {
					List<String> anyValidFields = new ArrayList<String>(validFields);
					anyValidFields.removeAll(errorFields);
					if (!anyValidFields.isEmpty()) {
						win.call(JS_METHOD_DISPLAYING_VALID_FIELDS, new Object[] {anyValidFields.toArray(new String[] {})});
					}
				}
			});
		}
		return saveButton;
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BoxLayout(getJContentPane(), BoxLayout.X_AXIS));
			jContentPane.setPreferredSize(new Dimension(350, 60));
			jContentPane.setSize(new Dimension(350, 100));
			jContentPane.add(getCanvasPanel(), null);
			jContentPane.add(getButtonsPanel(), null);
		}
		return jContentPane;
	}
	
   
    private void loadImage()
    {
        try
        {
            Image load_image = ImageIO.read(new File(load_url));
   
            if (load_image!=null){
            	cv.setBkImage(load_image, "", 0);
            	lockCanvas();
            	lockArtist();
            }   	      	
        }
        catch(MalformedURLException mue)
        {
            System.out.println("url " + mue.getMessage());
        }
        catch(IOException ioe)
        {
            System.out.println("read: " + ioe.getMessage());
        }
    }

    
	private void saveImage(Image image)
    {
        String fileName = null;
		int w = image.getWidth(null);
        int h = image.getHeight(null);
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bi.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        
//        try
//        {	//creates a file with the current timestamp
            fileName = save_url + "ADReport_"+ artist.getMember("value") + "_" + new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date()) + ".bmp";
        	//ImageIO.write(bi, "bmp", new File(fileName));
            System.out.println("File saved: " + fileName);
            artist_signature.setMember("value", fileName);            
            //pass saved file name back to the JSP page.
            lockCanvas();
            lockArtist();
//        }
//        catch(IOException ioe)
//        {
//            System.out.println("write: " + ioe.getMessage());
//        }
    }
//	locks the canvas
    private void lockCanvas() {
    	cv.iAction=0;//no action means no drawing capabilities
    	clearButton.setEnabled(false);//disables buttons
    	saveButton.setEnabled(false);		
	}
    
//	unlocks the canvas
//    private void unlockCanvas() {
//    	cv.iAction=24;
//    	clearButton.setEnabled(true);
//    	saveButton.setEnabled(true);		
//	}
    
//  locks the HTML fiels given as parameters
    private void lockArtist() {
        artist.setMember("readOnly", "readOnly");
        artist_pu.setMember("readOnly", "readOnly");
        artist_muwdcall_scheduled.setMember("readOnly", "readOnly");
        artist_muwdcall_actual.setMember("readOnly", "readOnly");
        artist_meal.setMember("readOnly", "readOnly");
        artist_wrap.setMember("readOnly", "readOnly");
        artist_travel.setMember("readOnly", "readOnly");
	}

    private void retrieveFields(){
        win = JSObject.getWindow(this);
        JSObject doc = (JSObject)win.getMember("document");
        JSObject forms = (JSObject)doc.getMember("forms");
        JSObject myform = (JSObject)forms.getMember("form1");
        JSObject elements = (JSObject)myform.getMember("elements");

        String propertyRoot = getParameter("propertyRoot");
        String index = getParameter("index");
        
        artist = (JSObject)elements.getMember(propertyRoot + "_" + index);
        artist_pu = (JSObject)elements.getMember(propertyRoot + "_pu_" + index);
        artist_muwdcall_scheduled = (JSObject)elements.getMember(propertyRoot + "_muwdcall_scheduled_" +index);
        artist_muwdcall_actual = (JSObject)elements.getMember(propertyRoot + "_muwdcall_actual_" + index);
        artist_meal = (JSObject)elements.getMember(propertyRoot + "_meal_" + index);
        artist_wrap = (JSObject)elements.getMember(propertyRoot + "_wrap_" + index);
        artist_travel = (JSObject)elements.getMember(propertyRoot + "_travel_" + index);        
        artist_signature = (JSObject)elements.getMember(propertyRoot + "_signature_" + index);
        
        addValidFields();
    }
    
	private void addValidFields() {
		addValidField(artist);
		addValidField(artist_pu);
		addValidField(artist_muwdcall_scheduled);
		addValidField(artist_muwdcall_actual);
		addValidField(artist_meal);
		addValidField(artist_wrap);
		addValidField(artist_travel);
	}

	public String getAppletInfo()
    {
        return String.valueOf(String.valueOf((new StringBuffer("Name: ")).append(ver).append("\r\n").append("Author: Marcello La Rosa\r\n").append("mail : m.larosa@qut.edu.au\r\n")));
    }
}
