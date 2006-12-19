/* 
 * This file is made available under the terms of the LGPL licence. 
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html. 
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of 
 * individuals and organisations who are commited to improving workflow technology. 
 * 
 */
package au.edu.qut.yawl.elements.state;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/** 
 * 
 */
@Entity
public class IdentifierSequence {
    private String sequence;
    private Long value;
    
    public IdentifierSequence() {
    }
    
    public IdentifierSequence( String sequence ) {
        this.sequence = sequence;
    }
    
    @Column(name = "sequence")
    @Id
    public String getSequence() {
        return sequence;
    }
    
    public void setSequence( String sequence ) {
        this.sequence = sequence;
    }
    
    @Column(name = "value")
    public Long getValue() {
        return value;
    }
    
    public void setValue( Long value ) {
        this.value = value;
    }
}
