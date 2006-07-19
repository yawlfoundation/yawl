/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import operation.WorkflowOperation;
import au.edu.qut.yawl.elements.YCondition;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;

import com.nexusbpm.editor.persistence.EditorDataProxy;

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
    private EditorDataProxy parent;
    private YCondition condition;
    private DataProxy<YCondition> conditionProxy;
    private String conditionType;
    private String label;
    
    public static final String TYPE_CONDITION = "";
    public static final String TYPE_INPUT_CONDITION = "";
    public static final String TYPE_OUTPUT_CONDITION = "";
	
    /**
     * NOTE: the parent proxy needs to be connected to the context.
     * @param parent
     * @param conditionType the type of condition to create.
     * @param label the label given to the condition if the type is not input or output.
     */
	public CreateConditionCommand(EditorDataProxy parent, String conditionType, String label) {
		this.context = parent.getContext();
        this.parent = parent;
        this.conditionType = conditionType;
        this.label = label;
	}
	
    /**
     * @see com.nexusbpm.command.AbstractCommand#attach()
     */
    @Override
    protected void attach() throws Exception {
        WorkflowOperation.attachConditionToNet( parent, condition );
        context.attachProxy( conditionProxy, condition );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#detach()
     */
    @Override
    protected void detach() throws Exception {
        WorkflowOperation.detachConditionFromNet( condition );
        context.detachProxy( conditionProxy );
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
            // TODO do we need to URI encode the label when setting it as the ID?
            condition = WorkflowOperation.createCondition( label.replaceAll( " ", "_" ), label );
        }
        conditionProxy = context.getDataProxy( condition, null );
    }
}
