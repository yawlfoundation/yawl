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


// colset Port = record portID : PortID * dir:Direction * card:Cardinality * 
// mult:Multiplicity;

public class ProcletPort {
	
	public enum Direction {
	    IN, OUT;
	}
	
	// colset Signature = with ZERO | ONE | PLUS | STAR | QUEST;
	public enum Signature {
	    ZERO, ONE, PLUS, STAR, QUEST;
	}
	
	private String portID = "";
	private Direction direction = Direction.IN;
	private Signature cardinality = Signature.ONE;
	private Signature multiplicity = Signature.ONE;
	 
	
	public ProcletPort(String portID, Direction direction, Signature cardinality, Signature multiplicity) {
		this.portID = portID;
		this.direction = direction;
		this.cardinality = cardinality;
		this.multiplicity = multiplicity;
	}
	
	public String getPortID () {
		return this.portID;
	}
	
	public void setPortID (String portID) {
		this.portID = portID;
	}
	
	public Direction getDirection () {
		return this.direction;
	}
	
	public void setDirection (Direction dir) {
		this.direction = dir;
	}
	
	public Signature getCardinality () {
		return this.cardinality;
	}
	
	public void setCardinality (Signature card) {
		this.cardinality = card;
	}
	
	public Signature getMultiplicity () {
		return this.multiplicity;
	}
	
	public void setMultiplicity (Signature mult) {
		this.multiplicity = mult;
	}
	
	public static Direction getDirectionFromString(String direction) {
		if (direction.equals("IN")) {
			return Direction.IN;
		}
		if (direction.equals("OUT")) {
			return Direction.OUT;
		}
		return null;
	}
	
	public static String getShortSignature (Signature sign) {
		if (sign.equals(Signature.ZERO)) {
			return "0";
		}
		if (sign.equals(Signature.ONE)) {
			return "1";
		}
		if (sign.equals(Signature.PLUS)) {
			return "+";
		}
		if (sign.equals(Signature.STAR)) {
			return "*";
		}
		if (sign.equals(Signature.QUEST)) {
			return "?";
		}
		return "";
	}
	
	public static Signature getSignatureFromString(String sign) {
		// ZERO, ONE, PLUS, STAR, QUEST;
		if (sign.equals("ZERO")) {
			return Signature.ZERO;
		}
		if (sign.equals("ONE")) {
			return Signature.ONE;
		}
		if (sign.equals("PLUS")) {
			return Signature.PLUS;
		}
		if (sign.equals("STAR")) {
			return Signature.STAR;
		}
		if (sign.equals("QUEST")) {
			return Signature.QUEST;
		}
		return null;
	}
	
	public String toString(){
		return this.portID + "," + this.getCardinality() + "," + this.getMultiplicity() + "," + this.getDirection();
	}
}
