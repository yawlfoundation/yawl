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

public class ThreadTest extends ThreadNotify{
	
	private boolean threadSuspended = true;
	
	public ThreadTest() {
		
	}
	
    public synchronized void press() {
        threadSuspended = !threadSuspended;
        if (!threadSuspended)
            notify();
    }

	
    public void run() {
    	synchronized(this) {
    		try {
              while (threadSuspended) {
                  	System.out.println("before");
                    this.wait();
                    System.out.println("after");
             }
           }
           catch (InterruptedException e){
           }
    	}
    }

	
	public static void main(String [] args) {
		ThreadTest t = new ThreadTest();
		t.start();
		System.out.println();
		t.press();
		System.out.println();
	}
}
