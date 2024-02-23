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

package org.yawlfoundation.yawl.procletService.util;

public class ThreadNotify extends Thread{
	protected boolean threadSuspended = true;
	protected boolean timeOut = false;
	
	public synchronized void press() {
        threadSuspended = !threadSuspended;
        if (!threadSuspended) {
        	System.out.println("press!");
            notifyAll();
        }
    }
	
	public synchronized void notification(boolean b) {
	     threadSuspended = !threadSuspended;
	     this.timeOut = true;
	     if (!threadSuspended)
	    	 System.out.println("notify! " + this);
	          notifyAll();
	}
	
    public synchronized void run() {
    	//synchronized(this) {
    		System.out.println();
    		try {
              while (threadSuspended) {
                  	System.out.println("before " + this);
                    wait();
                    System.out.println("after " + this);
             }
           }
           catch (InterruptedException e){
           }
    	//}
    }
	
	protected boolean isTimeOut() {
		return this.timeOut;
	}
}
