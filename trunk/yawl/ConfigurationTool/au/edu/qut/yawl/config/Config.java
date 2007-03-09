/*
 * HelloWorldSwing.java is a 1.4 example that
 * requires no other files. 
 */
package au.edu.qut.yawl.config;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.*;        

import java.util.prefs.Preferences;

public class Config implements ActionListener {
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
	
	public static String toDir = "";
	public static String fromDir = "";
	
    public JCheckBox persistencebox = new JCheckBox();
    JFrame frame = new JFrame("YAWL Configuration Tool");
    JLabel error = new JLabel("");

    Preferences prefs = Preferences.userNodeForPackage(Config.class);

    
    private void createAndShowGUI() {

	Boolean persistent = prefs.getBoolean("persistent", false);

	System.out.println(persistent);

	if (persistent) {
		persistencebox.setSelected(true);
	} else{
		persistencebox.setSelected(false);
	}

        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        error.setForeground(new Color(255,0,0));
        
        //Create and set up the window.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLayout(new GridLayout(3,1));
        
        //Add the ubiquitous "Hello World" label.
        JLabel label = new JLabel("Enable Persistence");

        JPanel enableper = new JPanel();
        enableper.add(label);
        enableper.setPreferredSize(new Dimension(300,50));


        enableper.add(persistencebox);
        frame.getContentPane().add(enableper,"1");
        
        JButton savebox = new JButton("Save and Exit");
        JButton cancelbox = new JButton("Cancel");

        savebox.addActionListener(this);
        cancelbox.addActionListener(this);
        
        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout());

        JPanel errors = new JPanel();
        errors.setLayout(new FlowLayout());
        errors.add(error);
        
        frame.getContentPane().add(errors,"2");       
        buttons.add(savebox);
        buttons.add(cancelbox);
        frame.getContentPane().add(buttons,"3");



        frame.setLocation(100,100);
        frame.setPreferredSize(new Dimension(300,300));
        
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public void actionPerformed( ActionEvent e ) {
    	if (e.getActionCommand().equalsIgnoreCase("Cancel")) {
    		System.exit(0);
    	}
    	if (e.getActionCommand().equalsIgnoreCase("Save and Exit")) {

    		try {
    			CopyFile("hibernate.properties", "hibernate.properties", persistencebox.isSelected());
    			CopyFile("InterfaceAServlet-servlet.xml", "InterfaceAServlet-servlet.xml", persistencebox.isSelected());    			
    			CopyFile("InterfaceBServlet-servlet.xml", "InterfaceBServlet-servlet.xml", persistencebox.isSelected());    			
        		System.exit(0);
    		} catch (Exception ex) {
    			ex.printStackTrace();
    	        error.setText("Failed to configure YAWL");
    		}
    	}

    	
    }
    
    public void CopyFile(String filename, String outfile, boolean enabled) throws Exception {



    	// Declare variables
    	InputStream sIn = null;
    	OutputStream sOut = null;

    	try
    	{
    		// Declare variables
    		int nLen = 0;
    		int total = 0;
    		sIn = new FileInputStream(new File(toDir+"\\"+filename));
    		sOut = new FileOutputStream(new File(fromDir+"\\"+outfile));

    		// Transfer bytes from in to out
    		byte[] bBuffer = new byte[1024];
    		StringBuffer content = new StringBuffer();
    		while ((nLen = sIn.read(bBuffer))> 0)
    		{
    			content.append(new String(bBuffer).substring(0, nLen));
    			total += nLen;
    			bBuffer = new byte[1024];
    		}

    		String newContent = null;
    		if (enabled) {
			prefs.putBoolean("persistent", true);
    			newContent = new String(content).replaceAll( 
    					"HSQLDialect",
    					"PostgreSQLDialect" );
    			newContent = newContent.replaceAll( 
    					"hibernate.connection.driver_class org.hsqldb.jdbcDriver",
    					"hibernate.connection.driver_class org.postgresql.Driver" );
    			newContent = newContent.replaceAll( 
    					"url jdbc:hsqldb:mem:yawl",
    					"url jdbc:postgresql:yawl" );
    			newContent = newContent.replaceAll( 
    					"username sa",
    					"username postgres" );
    			newContent = newContent.replaceAll( 
    					"password  ",
    					"password admin" );
    			 
    			newContent = newContent.replaceAll( 
    					"<property name=\"dataSource\" ref=\"memoryDataSource\" />",
    					"<property name=\"dataSource\" ref=\"postgresDataSource\" />" );

    			
    		} else {
			prefs.putBoolean("persistent", false);
    			newContent = new String(content).replaceAll( 
    					"PostgreSQLDialect",
    					"HSQLDialect" );
    			newContent = newContent.replaceAll( 
    					"hibernate.connection.driver_class org.postgresql.Driver",
    					"hibernate.connection.driver_class org.hsqldb.jdbcDriver" );
    			newContent = newContent.replaceAll( 
    					"url jdbc:postgresql:yawl",
    					"url jdbc:hsqldb:mem:yawl" );
    			newContent = newContent.replaceAll( 
    					"username postgres",
    					"username sa" );
    			newContent = newContent.replaceAll( 
    					"password admin",
    					"password  " );
    			 
    			newContent = newContent.replaceAll( 
    					"<property name=\"dataSource\" ref=\"postgresDataSource\" />",
    					"<property name=\"dataSource\" ref=\"memoryDataSource\" />" );  		}

    		
    		if (newContent!=null) {
    			byte[] outbuffer = newContent.getBytes();

    			sOut.write(outbuffer, 0, outbuffer.length);
    		} else {
    			System.out.println("a");
    			throw new Exception("something happnened");
    		}

    		
    		// Flush
    		sOut.flush();
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    		throw new Exception();
    		
    	} catch (IOException eError) {
    		eError.printStackTrace();
    		throw new Exception();
    	} 


    	try
    	{
    		if (sIn != null)
    			sIn.close();
    		if (sOut != null)
    			sOut.close();
    	} catch (IOException eError) {
    	} 


    }

    
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
    	
    	if (args.length != 2) {
    		System.out.println("Two arguments please!");
    		System.exit(1);
    	}
    	Config.toDir = args[0];
    	Config.fromDir = args[1];
    	
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Config().createAndShowGUI();
            }
        });
    }
}