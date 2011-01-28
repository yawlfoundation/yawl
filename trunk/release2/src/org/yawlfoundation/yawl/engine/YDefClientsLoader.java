/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.engine;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.authentication.YClient;
import org.yawlfoundation.yawl.authentication.YExternalClient;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.util.PasswordEncryptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;


/**
 * Registers the default service and external client accounts when they have not been
 * previously persisted (ie. on first startup) or when persistence is disabled.
 *
 * @author Michael Adams
 * @date 27/01/2011
 */
public class YDefClientsLoader {

    private Logger _log;
    private final String _propsFile = "defaultClients.properties";

    public YDefClientsLoader() {
        _log = Logger.getLogger(this.getClass());
    }

    public Set<YClient> load() {
        _log.info("Loading default client and service account details - Starts");
        Set<YClient> loadedSet = new HashSet<YClient>();
        InputStream in = getClass().getResourceAsStream(_propsFile);
        if (in != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            try {
                String line = reader.readLine();
                while (line != null) {
                    YClient client = processLine(line);
                    if (client != null) {
                        loadedSet.add(client);
                    }
                    line = reader.readLine();
                }
            }
            catch (IOException ioe) {
                _log.warn("Error reading file '" + _propsFile + "'.");
            }
        }
        else _log.warn("Error opening file '" + _propsFile + "'.");

        _log.info("Loading default client and service account details - Ends");
        return loadedSet;
    }


    private YClient processLine(String rawLine) {
        rawLine = rawLine.trim();

        // ignore comments and empty lines
        if ((rawLine.length() == 0) || rawLine.startsWith("#")) {
            return null;
        }
        
        int headerEnd = rawLine.indexOf(':');
        if (headerEnd > -1) {
            String[] parts = rawLine.substring(headerEnd + 1).split(",");
            if (rawLine.startsWith("extClient:") && (parts.length == 3)) {
                return new YExternalClient(parts[0].trim(),
                        PasswordEncryptor.encrypt(parts[1].trim(), null),
                        parts[2].trim());

            }
            else if (rawLine.startsWith("service:") && (parts.length == 5)) {
                YAWLServiceReference service = new YAWLServiceReference(
                        parts[3].trim(), null, parts[0].trim(),
                        PasswordEncryptor.encrypt(parts[1].trim(), null),
                        parts[2].trim());
                service.setAssignable(parts[4].trim().equalsIgnoreCase("true"));
                return service;
            }
        }
        _log.warn("Could not load default external client - malformed entry: " + rawLine);
        return null;
    }
    
}
