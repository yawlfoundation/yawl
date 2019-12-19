/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.procletService.connect;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketException;

/** JavaCPNInterface is the interface which defines the methods implemented
 * by JavaCPN.
 * @author Guy Gallasch
 * @version 0.6
 */

public interface JavaCPNInterface
{
    // A method to actively establish a connection to an external process
    public void connect(String hostName, int port) throws IOException;

    // A method to passively establish a connection with an external process
    public void accept(int port) throws IOException;

    // A method to send data to an external process
    public void send(ByteArrayInputStream sendBytes) throws SocketException;

    // A method to receive data from an external process
    public ByteArrayOutputStream receive() throws SocketException;

    // A method to close a connection to an external process
    public void disconnect() throws IOException;
}

