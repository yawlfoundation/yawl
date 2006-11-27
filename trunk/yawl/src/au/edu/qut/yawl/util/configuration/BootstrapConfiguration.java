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
