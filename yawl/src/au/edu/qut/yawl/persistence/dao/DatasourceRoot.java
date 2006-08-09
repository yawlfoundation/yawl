/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.persistence.dao;

import java.io.File;

/**
 * @author Matthew Sandoz
 * @author Nathan Rose
 */
public class DatasourceRoot extends DatasourceFolder {
	public DatasourceRoot( String name ) {
        super( name, null );
    }
    
    public DatasourceRoot( File file ) {
        super( file.toURI().toString(), null );
        setFile( file );
    }
    
    @Override
    public String getPath() {
        return getName();
    }
    
    @Override
    public DatasourceRoot getRoot() {
        return this;
    }
    
    public boolean isSchemaVirtual() {
        return getName().startsWith( "virtual:" );
    }
    
    public boolean isSchemaFile() {
        return getName().startsWith( "file:" );
    }
    
    public boolean isSchemaHibernate() {
        return getName().startsWith( "hibernate:" );
    }
}
