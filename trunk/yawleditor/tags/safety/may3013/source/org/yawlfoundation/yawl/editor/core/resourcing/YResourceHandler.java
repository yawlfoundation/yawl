package org.yawlfoundation.yawl.editor.core.resourcing;

import org.yawlfoundation.yawl.elements.YSpecification;

/**
 * @author Michael Adams
 * @date 31/08/12
 */
public class YResourceHandler {

    private YSpecification _specification;


    public YResourceHandler() { }

    public YResourceHandler(YSpecification specification) {
        _specification = specification;
    }


    public YSpecification getSpecification() {
        return _specification;
    }

    public void setSpecification(YSpecification specification) {
        _specification = specification;
    }
}
