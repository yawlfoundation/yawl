/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.net.URI;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;

import com.nexusbpm.editor.persistence.EditorDataProxy;

/**
 * The CreateSpecificationCommand creates a specification under the specified
 * parent. The created specification is stored in the command for later undoing.
 * 
 * @author Nathan Rose
 */
public class CreateSpecificationCommand extends AbstractCommand {

	private DataContext context;
    private EditorDataProxy parent;
    private YSpecification specification;
    private DataProxy<YSpecification> specProxy;
    private String specName;
	
	public CreateSpecificationCommand(DataContext context, EditorDataProxy parent, String specName) {
		this.context = context;
        this.parent = parent;
        this.specName = specName;
	}
	
    /**
     * @see com.nexusbpm.command.AbstractCommand#attach()
     */
    @Override
    protected void attach() {
        context.attachProxy( specProxy, specification );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#detach()
     */
    @Override
    protected void detach() { 
        context.detachProxy( specProxy );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#perform()
     */
    @Override
    protected void perform() {
        specification = createSpecification( parent, specName );
        context.generateProxies( specification );
        specProxy = context.getDataProxy( specification, null );
    }
    
    private YSpecification createSpecification(EditorDataProxy parent, String name) {
        
        YSpecification spec = null;
        try {
            URI u = new URI( parent.getData().toString() );
            spec = new YSpecification((
                    new URI(
                            u.getScheme(),
                            u.getAuthority(),
                            u.getPath() + "/" + name,
                            null,
                            null
                            )).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        spec.setName(name);
        
        return spec;
    }
}
