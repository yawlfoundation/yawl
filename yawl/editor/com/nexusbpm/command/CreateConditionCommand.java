/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import com.nexusbpm.operation.WorkflowOperation;

import au.edu.qut.yawl.elements.YCondition;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;
import au.edu.qut.yawl.persistence.managed.DataProxyStateChangeListener;

/**
 * The CreateConditionCommand creates a condition in the specified net.
 * The type of condition created is specified through one of the constants
 * defined in this class. The created condition is stored in the command
 * for later undoing.
 * 
 * @author Nathan Rose
 */
public class CreateConditionCommand extends AbstractCommand {
	private DataContext context;
    private DataProxy<YNet> netProxy;
    private YCondition condition;
    private DataProxy<YCondition> conditionProxy;
    private String conditionType;
    private String label;
    private DataProxyStateChangeListener listener;
    
    public static final String TYPE_CONDITION = "cond";
    public static final String TYPE_INPUT_CONDITION = "input";
    public static final String TYPE_OUTPUT_CONDITION = "output";
	
    /**
     * NOTE: the parent proxy needs to be connected to the context.
     * @param parent
     * @param conditionType the type of condition to create.
     * @param label the label given to the condition if the type is not input or output.
     */
	public CreateConditionCommand( DataProxy netProxy, String conditionType, String label,
            DataProxyStateChangeListener listener ) {
        this.netProxy = netProxy;
        this.context = netProxy.getContext();
        this.conditionType = conditionType;
        this.label = label;
        this.listener = listener;
	}
	
    /**
     * @see com.nexusbpm.command.AbstractCommand#attach()
     */
    @Override
    protected void attach() throws Exception {
        WorkflowOperation.attachNetElementToNet( condition, netProxy.getData() );
        context.attachProxy( conditionProxy, condition, netProxy );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#detach()
     */
    @Override
    protected void detach() throws Exception {
        WorkflowOperation.detachNetElementFromNet( condition );
        context.detachProxy( conditionProxy, condition, netProxy );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#perform()
     */
    @Override
    protected void perform() throws Exception {
        if( TYPE_INPUT_CONDITION.equals( conditionType ) ) {
            condition = WorkflowOperation.createInputCondition();
        }
        else if( TYPE_OUTPUT_CONDITION.equals( conditionType ) ) {
            condition = WorkflowOperation.createOutputCondition();
        }
        else {
            condition = WorkflowOperation.createCondition( netProxy.getData(), label );
        }
        conditionProxy = context.createProxy( condition, listener );
    }
}
