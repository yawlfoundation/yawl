/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.dao;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.elements.SpecVersion;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.persistence.dao.SpringDAO.AbstractPersistenceWrapper;

public class SpecificationPersistenceWrapper extends AbstractPersistenceWrapper<YSpecification> {
    private static final Log LOG = LogFactory.getLog( SpecificationPersistenceWrapper.class );
    
    @Override
    public void preSave(YSpecification spec, SpringDAO dao) throws YPersistenceException {
        try {
            spec.setID(new URI(spec.getID()).toASCIIString());
            if(spec.getVersion() == null) {
                setVersion(spec, dao);
            }
        }
        catch(URISyntaxException e) {
            LOG.error("Error setting version for specification!", e);
        }
    }
    
    private void setVersion(YSpecification spec, SpringDAO dao) throws YPersistenceException {
        String uriString = spec.getID();
        
        SpecVersion specVersion = (SpecVersion) dao.retrieve(SpecVersion.class, uriString);
        
        int nextVersion = 1;
        
        if(specVersion == null) {
            specVersion = new SpecVersion(uriString, Integer.valueOf(nextVersion));
        }
        else {
            nextVersion = specVersion.getHighestVersion().intValue() + 1;
        }
        
        spec.setVersion(Integer.valueOf(nextVersion));
        specVersion.setHighestVersion(Integer.valueOf(nextVersion));
        
        dao.save(specVersion);
    }
}
