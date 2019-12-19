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

import java.net.InetAddress;

public class Receiver {
	
	private JavaCPN conn1 = null;
	
	public Receiver () {
		conn1 = new JavaCPN();
	}
	
	public void initiate () {
		byte ipAddr[] = {127, 0, 0, 1};
		try {
			InetAddress addr3 = InetAddress.getByAddress(ipAddr);
			String sHostName = addr3.getHostName();
			String host = sHostName;
			int port = 9005;
			conn1.connect(host, port);
			String message = EncodeDecode.decodeString(conn1.receive());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void send(String message) {
		try {
			conn1.send(EncodeDecode.encodeString(message));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String receive() {
		try {
			return EncodeDecode.decodeString(conn1.receive());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
