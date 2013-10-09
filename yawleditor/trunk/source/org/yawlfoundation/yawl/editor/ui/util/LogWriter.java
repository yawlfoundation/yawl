/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.util;

import org.apache.log4j.*;

import java.io.IOException;

/**
 * Author: Michael Adams
 * Creation Date: 3/03/2009
 */
public class LogWriter {

    private static Logger _log = Logger.getRootLogger() ;

    private LogWriter() {
        configure("");
    }

    public static void init(String homeDir) { configure(homeDir) ;}
    
    
    private static void configure(String homeDir) {
        _log.setLevel(Level.WARN);

        PatternLayout layout = new PatternLayout("%d{ISO8601} [%-5p] :- %m%n");

        // appender for system.out
        _log.addAppender(new ConsoleAppender(layout));

        // appender for file
        try {
            _log.addAppender(new FileAppender(layout, homeDir + "YAWLEditor.log"));
        }
        catch (IOException ioe) {
            _log.error("Could not instantiate log file", ioe) ;
        }
    }

    public static void info(String msg) { _log.info(msg); }

    public static void info(String msg, Throwable e) { _log.info(msg, e); }


    public static void warn(String msg) { _log.warn(msg); }
    
    public static void warn(String msg, Throwable e) { _log.warn(msg, e); }


    public static void debug(String msg) { _log.debug(msg); }

    public static void debug(String msg, Throwable e) { _log.debug(msg, e); }


    public static void error(String msg) { _log.error(msg); }

    public static void error(String msg, Throwable e) { _log.error(msg, e); }    
}
