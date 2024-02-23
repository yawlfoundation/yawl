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

package org.yawlfoundation.yawl.procletService.models.procletModel;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import org.yawlfoundation.yawl.procletService.persistence.DBConnection;
import org.yawlfoundation.yawl.procletService.persistence.StoredPortConnection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PortConnections extends DirectedSparseGraph {
	
	private static PortConnections pconns = null;

	private List<PortConnection> pcs = new ArrayList<PortConnection>();
	private List<String> channels = new ArrayList<String>();
	
	private PortConnections () {
		
	}
	
	public static PortConnections getInstance() {
		if (pconns == null) {
			pconns = new PortConnections();
			pconns.buildPConnsFromDB();
		}
		return pconns;
	}
	
	public void addChannel(String channel) {
		if (!this.channels.contains(channel)) {
			this.channels.add(channel);
		}
	}
	
	public void removeChannel (String channel) {
		this.channels.remove(channel);
	}
	
	public List<String> getChannels () {
		return this.channels;
//		List<String> returnList = new ArrayList<String>();
//		for (PortConnection pconn : getPortConnections()) {
//			String channel = pconn.getChannel();
//			if (!returnList.contains(channel)) {
//				returnList.add(channel);
//			}
//		}
//		return returnList;
	}
	
	public void addPortConnection (PortConnection conn) {
		if (!this.pcs.contains(conn)) {
			this.pcs.add(conn);
			this.addVertex(conn.getIPort());
			this.addVertex(conn.getOPort());
			// bpe,block,port,EdgeType.DIRECTED
			//this.addEdge(conn, conn.getOPort(), conn.getIPort(), EdgeType.DIRECTED);
			this.addEdge(conn, conn.getIPort(), conn.getOPort(), EdgeType.DIRECTED);
		}
	}
	
	public void deletePortConnection (PortConnection pc) {
		this.pcs.remove(pc);
		// delete also from graph
		this.removeVertex(pc.getIPort());
		this.removeVertex(pc.getOPort());
		this.removeEdge(pc);
	}
	
	public List<PortConnection> getPortConnections () {
		return this.pcs;
	}
	
	public PortConnection getPortConnectionIPort (PortConnection iPort) {
		Iterator<PortConnection> it = pcs.iterator();
		while (it.hasNext()) {
			PortConnection next = it.next();
			if (next.getIPort().equals(iPort)) {
				return next;
			}
		}
		return null;
	}
	
	public PortConnection getPortConnectionIPort (String iPortID) {
		Iterator<PortConnection> it = pcs.iterator();
		while (it.hasNext()) {
			PortConnection next = it.next();
			if (next.getIPort().getPortID().equals(iPortID)) {
				return next;
			}
		}
		return null;
	}
	
	public PortConnection getPortConnectionOPort (PortConnection oPort) {
		Iterator<PortConnection> it = pcs.iterator();
		while (it.hasNext()) {
			PortConnection next = it.next();
			if (next.getOPort().equals(oPort)) {
				return next;
			}
		}
		return null;
	}
	
	public PortConnection getPortConnectionOPort (String oPortID) {
		Iterator<PortConnection> it = pcs.iterator();
		while (it.hasNext()) {
			PortConnection next = it.next();
			if (next.getOPort().getPortID().equals(oPortID)) {
				return next;
			}
		}
		return null;
	}
	
	public ProcletPort getIPort(String portID) {
		Iterator<PortConnection> it = pcs.iterator();
		while (it.hasNext()) {
			PortConnection port = it.next();
			if (port.getIPort().getPortID().equals(portID)) {
				return port.getIPort();
			}
		}
		return null;
	}
	
	public ProcletPort getOPort(String portID) {
		Iterator<PortConnection> it = pcs.iterator();
		while (it.hasNext()) {
			PortConnection port = it.next();
			if (port.getOPort().getPortID().equals(portID)) {
				return port.getOPort();
			}
		}
		return null;
	}

    public boolean buildPConnsFromDB() {
        pcs.clear();

        // all ports
        List<ProcletPort> ports = ProcletModels.getInstance().getPorts();
        // 	get blocks
        List items = DBConnection.getObjectsForClass("StoredPortConnection");
        for (Object o : items) {
            StoredPortConnection spc = (StoredPortConnection) o;
            String iportid = spc.getInput();
            String channel = spc.getChannel();
            String oportid = spc.getOutput();
            ProcletPort iPort = null;
            ProcletPort oPort = null;
            for (ProcletPort port : ports) {
                if (port.getPortID().equals(iportid)) {
                    iPort = port;
                }
                if (port.getPortID().equals(oportid)) {
                    oPort = port;
                }
            }
            PortConnection pconn = new PortConnection(iPort,oPort,channel);
            addPortConnection(pconn);
        }

        // determine channels
        for (PortConnection pconn : pcs) {
            if (!channels.contains(pconn.getChannel())) {
                channels.add(pconn.getChannel());
            }
        }

        return true;
    }
	
	public void persistPConns() {
		this.deletePConnsFromDB();
        for (PortConnection pConn : getPortConnections()) {
            DBConnection.insert(pConn.newStoredPortConnection());
        }
    }

	public void deletePConnsFromDB() {
		DBConnection.deleteAll("StoredPortConnection");
	}
	
	public static void main(String [] args) { 
		PortConnections pconns = PortConnections.getInstance();
		pconns.buildPConnsFromDB();
		pconns.persistPConns();
		System.out.println("done");
	}
}
