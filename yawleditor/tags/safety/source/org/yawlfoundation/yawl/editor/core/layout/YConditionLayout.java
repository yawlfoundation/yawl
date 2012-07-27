package org.yawlfoundation.yawl.editor.core.layout;

import org.yawlfoundation.yawl.elements.YCondition;

import java.text.NumberFormat;

/**
 *  Stores the layout information for a particular condition
 *
 * @author Michael Adams
 * @date 19/06/12
 */
public class YConditionLayout extends YNetElementNode {

    private YCondition _condition;


    /**
     * Creates a new YConditionLayout object
     * @param condition the YCondition this layout describes
     * @param formatter a number format for the specific locale
     */
    public YConditionLayout(YCondition condition, NumberFormat formatter) {
        _condition = condition;
        setID(condition.getID());
        setNumberFormatter(formatter);
    }


    public YCondition getCondition() { return _condition; }

    public void setCondition(YCondition condition) { _condition = condition; }

}
