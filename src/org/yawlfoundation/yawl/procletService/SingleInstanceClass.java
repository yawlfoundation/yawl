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

package org.yawlfoundation.yawl.procletService;

import org.yawlfoundation.yawl.procletService.blockType.BlockPICreate;
import org.yawlfoundation.yawl.procletService.state.Performative;
import org.yawlfoundation.yawl.procletService.state.Performatives;
import org.yawlfoundation.yawl.procletService.util.ThreadNotify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class SingleInstanceClass {
	
	private static SingleInstanceClass singleInstance = null;
	
	private List<ThreadNotify> registeredClasses = new ArrayList<ThreadNotify>();
	private HashMap<ThreadNotify,InternalRunner> mapping = new HashMap<ThreadNotify,InternalRunner>();
	private HashMap<ThreadNotify,Boolean> mappingDone = new HashMap<ThreadNotify,Boolean>();
	private Object mutex = new Object();
	private Object mutex2 = new Object();
	private Object mutex3 = new Object();
	private List<String> blockedCases = new ArrayList<String>();
	
	private SingleInstanceClass() {
		super();
	}
	
	public void blockCase(String caseid) {
		synchronized(mutex3) {
			if (!blockedCases.contains(caseid)) {
				blockedCases.add(caseid);
			}
		}
	}
	
	public boolean isCaseBlocked(String caseid) {
		synchronized(mutex3) {
			return blockedCases.contains(caseid);
		}
	}
	
	public void unblockCase(String caseid) {
		synchronized(mutex3) {
			blockedCases.remove(caseid);
		}
	}
	
	public InternalRunner registerAndWait(ThreadNotify thread, long w) {
		InternalRunner ir = null;
		synchronized (mutex) {
			if (!registeredClasses.contains(thread)) {
				registeredClasses.add(thread);
				// add a sleep thread
				ir = new InternalRunner(thread,w);
				//mapping.put(thread, ir);
			}
			ir.start();
		}
		return ir;
	}
	
	public InternalRunner registerAndWaitDuringNotify(ThreadNotify thread, long w) {
		synchronized (mutex2) {
			// remove old thread from mapping done and registeredclasses
			InternalRunner ir = null;
			// assume registeredClasses is empty
//			while (true) {
//				if (this.registeredClasses.isEmpty()) {
//					break;
//				}
//			}
			if (!registeredClasses.contains(thread)) {
				registeredClasses.add(thread);
				// add a sleep thread
				ir = new InternalRunner(thread,w);
				mapping.put(thread, ir);
				ir.start();
			}
			return ir;
		}
	}
	
	public void notifyPerformativeListeners (List<Performative> perfs) {
		// fill mappingDone
		synchronized(mutex) {
			// first add performatives
			Performatives perfsInst = Performatives.getInstance();
			for (Performative perf : perfs) {
				perfsInst.addPerformative(perf);
			}
			// first process the creation of new classes
			BlockPICreate bpc = BlockPICreate.getInstance();
			bpc.checkForCreationProclets();
			// notify the listeners
			for (ThreadNotify tn : this.registeredClasses) {
				this.mappingDone.put(tn, false);
			}
			for (ThreadNotify tn : registeredClasses) {
				tn.notification(false);
			}
			this.mapping.clear();
			this.registeredClasses.clear();
			// 	wait for all
			while (true) {
				try {
					Thread.currentThread().sleep(500);
					// 	everybody done
					boolean done = true;
					Iterator<ThreadNotify> it = this.mappingDone.keySet().iterator();
					while (it.hasNext()) {
						ThreadNotify notif = it.next();
						boolean d = this.mappingDone.get(notif);
						if (!d) {
							done = false;
							break;
						}
					}
					if (done) {
						break;
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void done(ThreadNotify notify) {
		if (this.mappingDone.containsKey(notify)) {
			this.mappingDone.put(notify, true);
		}
	}
	
	public void unregister(ThreadNotify thread) {
		synchronized(mutex2) {
			registeredClasses.remove(thread);
			mapping.remove(thread);
		}
	}
	
	public static SingleInstanceClass getInstance() {
		if (singleInstance == null) {
			singleInstance = new SingleInstanceClass();
			
		}
		return singleInstance;
	}
	
	public class InternalRunner extends Thread {
		private long started = 0;
		private long interval = 0;
		private ThreadNotify tn = null;
		//private SingleInstanceClass sic = null;
		public InternalRunner (ThreadNotify tn, long interval) {
			this.tn = tn;
			this.interval = interval;
			//this.sic = sic;
		}
		
		public void setThreadNotify(ThreadNotify tn) {
			this.tn = tn;
		}
		
		public void run () {
			try {
				started = System.currentTimeMillis();
				System.out.println("sleep:" + interval);
				sleep(interval);
				System.out.println("done sleeping");
				// done sleeping
				synchronized(mutex) {
					System.out.println("inside mutex!");
					System.out.println(tn);
					//System.out.println(sic.mapping.containsValue(this));
					if (tn != null && tn.isAlive()) {
						System.out.println("timer done!");
						tn.notification(true);
						tn = null;
					}
					else {
						System.out.println("thread died without doing anything!");
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public long leftOver () {
			return System.currentTimeMillis() - this.started;
		}
	}
	
}

