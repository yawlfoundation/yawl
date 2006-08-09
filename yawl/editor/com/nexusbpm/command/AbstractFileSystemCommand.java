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
import au.edu.qut.yawl.persistence.dao.DatasourceRoot;

import com.nexusbpm.editor.tree.SharedNode;

/**
 * This class provides functionality useful for commands that interact with the filesystem.
 * 
 * @author Nathan Rose
 */
public abstract class AbstractFileSystemCommand extends AbstractCommand {
    private static final Log LOG = LogFactory.getLog( AbstractFileSystemCommand.class );
    
    protected List<String> getChildNames( SharedNode folder ) throws URISyntaxException {
        List<String> ids = new LinkedList<String>();
        String parentPath = getPath( folder.getProxy().getData(), true );
        if( parentPath.startsWith( "file:" ) ) {
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
                String id = getPath( child.getProxy().getData(), false );
                LOG.debug( "used ID:" + id );
                ids.add( getLastPartOf( id ) );
            }
        }
        return ids;
    }
    
    protected String getPath( Object object, boolean requireFolder ) {
        String path = "";
        LOG.trace( "getting path of object: " + object );
        if( object == null ) {
            throw new IllegalArgumentException( "Cannot get the path for null!" );
        }
        else if( object instanceof File ) {
            LOG.trace( "object is a real folder" );
            File file = (File) object;
            path = file.toURI().toString();
        }
        else if( object instanceof String || object instanceof DatasourceRoot ) {
            LOG.trace( "object is a virtual folder or datasource root" );
            path = object.toString();
        }
        else if( requireFolder ) {
            throw new IllegalArgumentException( "Expected to find a folder, but found a " +
                    object.getClass().getName() + "!" );
        }
        else if( object instanceof YSpecification ) {
            path = ((YSpecification) object).getID();
        }
        else if( object instanceof String ) {
            path = (String) object;
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
