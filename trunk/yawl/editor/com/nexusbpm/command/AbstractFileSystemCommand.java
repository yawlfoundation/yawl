/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.dao.DatasourceFolder;
import au.edu.qut.yawl.persistence.managed.DataProxy;

import com.nexusbpm.editor.persistence.EditorDataProxy;
import com.nexusbpm.editor.tree.SharedNode;

/**
 * This class provides functionality useful for commands that interact with the filesystem.
 * 
 * @author Nathan Rose
 */
public abstract class AbstractFileSystemCommand extends AbstractCommand {
    private static final Log LOG = LogFactory.getLog( AbstractFileSystemCommand.class );
    
    protected List<String> getChildNames( DataProxy folder) throws URISyntaxException {
        return getChildNames( ((EditorDataProxy) folder).getTreeNode() );
    }
    
    protected List<String> getChildNames( SharedNode folder ) throws URISyntaxException {
        List<String> ids = new LinkedList<String>();
        DatasourceFolder parent = (DatasourceFolder) folder.getProxy().getData();
        String parentPath = parent.getPath();
        if( parent.isSchemaFile() ) {
            LOG.trace( "" + parentPath );
            File[] children = new File( new URI( parentPath ) ).listFiles();
            for( int index = 0; index < children.length; index++ ) {
                String uri = children[ index ].toURI().toString();
                LOG.debug( "used URI:" + uri );
                ids.add( getLastPartOf( uri ) );
            }
        }
        else {
            for( int index = 0; index < folder.getChildCount(); index++ ) {
                SharedNode child = (SharedNode) folder.getChildAt( index );
                String id = getPath( child.getProxy().getData() );
                LOG.debug( "used ID:" + id );
                ids.add( getLastPartOf( id ) );
            }
        }
        return ids;
    }
    
    protected String getPath( Object object ) {
        String path = "";
        LOG.trace( "getting path of object: " + object );
        if( object == null ) {
            throw new IllegalArgumentException( "Cannot get the path for null!" );
        }
        else if( object instanceof DatasourceFolder ) {
            LOG.trace( "object is a folder" );
            DatasourceFolder folder = (DatasourceFolder) object;
            path = folder.getPath();
        }
        else if( object instanceof YSpecification ) {
            path = ((YSpecification) object).getID();
        }
        else {
            throw new IllegalArgumentException( "Objects of type " +
                    object.getClass().getName() + " do not have a path!" );
        }
        return path;
    }
    
    protected String getLastPartOf( String uri ) {
        if( uri.endsWith( "/" ) ) {
            uri = uri.substring( 0, uri.length() - 1 );
        }
        if( uri.indexOf( "/" ) >= 0 ) {
            return uri.substring( uri.lastIndexOf( "/" ) + 1 );
        }
        else {
            return uri;
        }
    }
}
