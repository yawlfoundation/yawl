package org.yawlfoundation.yawl.editor.core.resourcing.validation;

/**
 * @author Michael Adams
 * @date 25/06/12
 */
public class InvalidNonHumanResourceReference extends InvalidReference {

    public InvalidNonHumanResourceReference(String rid) {
        super(rid, "Non-Human Resource");
    }

}
