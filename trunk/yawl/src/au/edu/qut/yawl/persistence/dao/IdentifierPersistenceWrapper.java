/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.dao;

import java.util.List;

import au.edu.qut.yawl.elements.state.IdentifierSequence;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.persistence.dao.SpringDAO.AbstractPersistenceWrapper;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.Restriction;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction.Comparison;

public class IdentifierPersistenceWrapper extends AbstractPersistenceWrapper<YIdentifier> {
    @Override
    public void preSave(YIdentifier item, SpringDAO dao) throws YPersistenceException {
        if( item.getId() == null ) {
            Restriction restriction = new PropertyRestriction("sequence", Comparison.EQUAL, "sequence");
            List sequences = dao.retrieveByRestriction(IdentifierSequence.class, restriction);
            
            int value = 1;
            IdentifierSequence sequence = new IdentifierSequence("sequence");
            
            if(sequences.size() > 0) {
                sequence = (IdentifierSequence) sequences.get(0);
                value = sequence.getValue().intValue() + 1;
            }
            
            item.setId(String.valueOf(value));
            sequence.setValue(Long.valueOf(value));
            
            dao.save(sequence);
        }
    }
}
