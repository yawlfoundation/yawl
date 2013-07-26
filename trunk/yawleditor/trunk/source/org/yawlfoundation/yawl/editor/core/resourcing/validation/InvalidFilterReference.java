package org.yawlfoundation.yawl.editor.core.resourcing.validation;

/**
 * @author Michael Adams
 * @date 25/06/12
 */
public class InvalidFilterReference extends InvalidReference {

    public InvalidFilterReference(String filterName) {
        super(filterName, "Filter");
    }

}
