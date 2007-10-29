/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing.rsInterface;

/**
 * A static "psuedo post-it note" class to store bits 'n' pieces used throughout the
 * service
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.1, 12/09/2007
 */

public class Docket {

    private static String _fileRoot = "";
    private static String _headPackageDir = "/WEB-INF/classes/au/edu/qut/yawl/resourcing/";

    public static void setServiceRootDir(String path) {
       path = path.replace('\\', '/' );             // switch slashes
       if (! path.endsWith("/")) path += "/";       // make sure it has ending slash
       _fileRoot = path ;
    }

    public static String getServiceRootDir() { return _fileRoot ; }

    public static String getPackageFileDir(String pkgName) {
        return _fileRoot + _headPackageDir + pkgName ;
    }

}
