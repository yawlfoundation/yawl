/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.resourcing.util;

/**
 * A static "psuedo post-it note" class to store bits 'n' pieces used throughout the
 * service
 *
 *  @author Michael Adams
 *  v0.1, 12/09/2007
 */

public class Docket {

    private static String _fileRoot = "";
    private static String _classesDir = "WEB-INF/classes/";
    private static String _headPackageDir = _classesDir + "org/yawlfoundation/yawl/resourcing/";

    public static void setServiceRootDir(String path) {
       path = path.replace('\\', '/' );             // switch slashes
       if (! path.endsWith("/")) path += "/";       // make sure it has ending slash
       _fileRoot = path ;
    }

    public static String getServiceRootDir() { return _fileRoot ; }

    public static String getPackageFileDir(String pkgName) {
        return _fileRoot + _headPackageDir + pkgName ;
    }

    public static String getPropertiesDir() { return _fileRoot + _classesDir; }

}
