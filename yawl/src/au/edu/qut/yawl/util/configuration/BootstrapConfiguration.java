/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.util.configuration;

import org.springframework.context.ApplicationContext;

public class BootstrapConfiguration {

	protected static BootstrapConfiguration instance = new BootstrapConfiguration(false);
	
    protected BootstrapConfiguration()  
    {  
        if (getClass().equals(BootstrapConfiguration.class))  
        {  
            throw new RuntimeException("Violation of intended use, use getInstance() instead");  
        }  
    }  

    private BootstrapConfiguration(boolean b) {}  
    
	protected ApplicationContext applicationContext;
	
	public synchronized static BootstrapConfiguration getInstance() {
		if (instance == null) {
			instance = new BootstrapConfiguration();
		}
		return instance;
	}

	public static final synchronized void setInstance(
			BootstrapConfiguration instance) {
		if (instance != null
				&& !instance.getClass().equals(BootstrapConfiguration.class)) {
			BootstrapConfiguration.instance = instance;
		} else {
			throw new RuntimeException("Violation of intended use.");  
		}
	}
	
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}
	
}
