/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.persistence.dao;

import java.io.File;

import au.edu.qut.yawl.elements.Parented;

/**
 * @author Nathan Rose
 */
public class DatasourceFolder implements Parented<DatasourceFolder> {
    private DatasourceFolder parent;
    private String name;
    private File file;
    
    
    public DatasourceFolder( String name, DatasourceFolder parent ) {
        this.name = name;
        this.parent = parent;
        this.file = null;
    }
    
	public DatasourceFolder( File file, DatasourceFolder parent ) {
        assert file.getParentFile().equals( parent.getFile() ) : "adding child that isn't a child";
        this.parent = parent;
        String temp = file.toURI().toString();
        while( temp.endsWith( "/" ) ) {
            temp = temp.substring( 0, temp.length() - 1 );
        }
        if( temp.indexOf( "/" ) >= 0 ) {
            temp = temp.substring( temp.lastIndexOf( "/" ) + 1 );
        }
        this.name = temp;
        this.file = file;
    }
    
    public DatasourceFolder getParent() {
        return parent;
    }
    
    public void setParent( DatasourceFolder parent ) {
        this.parent = parent;
    }
	
	public String getName() {
		return name;
	}
    
	public void setName( String name ) {
		this.name = name;
	}
    
    public File getFile() {
        return file;
    }
    
    public void setFile( File file ) {
        this.file = file;
    }
    
    public String getPath() {
        String path;
        if( getParent() != null ) {
            path = getParent().getPath();
            if( ! path.endsWith( "/" ) ) path += "/";
            path += getName();
        }
        else {
            path = getName();
        }
        return path;
    }
    
    public DatasourceRoot getRoot() {
        return getParent().getRoot();
    }
    
    public boolean isSchemaVirtual() {
        return getRoot().isSchemaVirtual();
    }
    
    public boolean isSchemaFile() {
        return getRoot().isSchemaFile();
    }
    
    public boolean isSchemaHibernate() {
        return getRoot().isSchemaHibernate();
    }
    
    public boolean isFile() {
        if( getFile() != null ) {
            return getFile().isFile();
        }
        else {
            // if it doesn't represent a real file on the filesystem, it's a virtual folder,
            // so it's always a folder and never a file
            return false;
        }
    }
    
    public boolean isFolder() {
        if( getFile() != null ) {
            return getFile().isDirectory();
        }
        else {
            // if it doesn't represent a real file on the filesystem, it's a virtual folder,
            // so it's always a folder and never a file
            return true;
        }
    }
    
	public String toString() {
        return getPath();
    }
}
