package org.yawlfoundation.yawl.editor.core.resourcing.validation;

/**
 * @author Michael Adams
 * @date 25/06/12
 */
public class InvalidConstraintReference extends InvalidReference {

    public InvalidConstraintReference(String constraintName) {
        super(constraintName, "Constraint");
    }

}
