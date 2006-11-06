/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.worker;

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * EventQueue.
 * 
 * @author Toby
 */
public class EventQueue extends LinkedList implements Runnable{
	
	private static final Log LOG = LogFactory.getLog(EventQueue.class);
	
    /** Whether the EventQueue should stop or not. */
	private boolean stop = false;
	
    /** The currently executing Thread. */
	private Thread currentThread;
	
    /**
     * Whether it's time to stop or not.
     * @return whether it's time to stop or not.
     */
	public synchronized boolean isStop() {
		return stop;
	}
    
    /**
     * Sets whether it's time to stop or not.
     * @param stop whether it's time to stop or not.
     */
	public synchronized void setStop(boolean stop) {
		this.stop = stop;
	}
    
    /**
     * Thread-safe accessor for the number of <tt>Worker</tt>s in the queue.
     * @return the number of <tt>Worker</tt>s in the queue.
     */
	public synchronized int size(){
		return super.size();
	}
	
    /**
     * Adds the given <tt>Worker</tt> to the <i>back</i> of the queue.
     * @param t the <tt>Worker</tt> to add.
     */
	public synchronized void add(Worker t){
		boolean signal = size() == 0;
		this.addLast(t);
		LOG.debug("Job added.");
		if (signal) notifyAll();
	}
    
    /**
     * Adds the given <tt>Worker</tt> to the <i>front</i> of the queue.
     * @param t the <tt>Worker</tt> to add.
     */
	public synchronized void push(Worker t){
		boolean signal = size() == 0;
		addFirst(t);
		LOG.debug("Job pushed.");
		if (signal) notifyAll();
	}
	
    /**
     * Gets the <tt>Worker</tt> that is next in the queue.
     * @return the next <tt>Worker</tt> in the queue. 
     */
	public synchronized Worker pop(){
		return (Worker)removeFirst();
	}
	
    /**
     * The <tt>EventQueue</tt>'s event loop.
     */
	public void run(){
		LOG.debug("Starting event queue thread.");
		while(!isStop()){
			while (size() == 0) {
				try {
					synchronized(this) {
						this.wait();
					}
				}
				catch (InterruptedException e) {
					LOG.error(e.getMessage(), e);
				}
			}
			final Worker t = pop();
			currentThread = new Thread( t.getName() ){
				public void run(){
					LOG.debug("Starting next thread.");
					try{
						t.execute();
					}catch(ThreadDeath e){
						LOG.info("Thread stopped.");
					}catch(Error e){
						LOG.error(e.getMessage(), e);
					}catch(Throwable e){
						LOG.error(e.getMessage(), e);
					}
				}
			};
			currentThread.start();
			try{
				currentThread.join();
				LOG.debug("Thread joined.");
				currentThread = null;
			}catch(InterruptedException e){
				LOG.error(e.getMessage(), e);
			}
		}
	}
    
    /**
     * Empties the EventQueue.
     */
	public synchronized void empty(){
		while(size() != 0)
			pop();
		setStop(true);
	}
    
    /**
     * Stops the currently executing thread.
     */
	public void killCurrentJob(){
		if (currentThread != null){
			currentThread.stop();
			currentThread = null;
		}
	}
	
    /**
     * Returns whether a job is currently running or not.
     * @return whether a job is currently running or not.
     */
	public boolean hasCurrentJob(){
		return currentThread != null;
	}
}
