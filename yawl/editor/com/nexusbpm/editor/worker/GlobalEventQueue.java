package com.nexusbpm.editor.worker;


/**
 * The global event queue schedules all threaded objects to make calls to the
 * server in an orderly process. We don't want to use the Swing event queue
 * since this will make the GUI freeze.  We have a separate event queue to
 * handle potentially long running events.  I don't remember writing this
 * class, but I think it is work heavily derived from Toby's EventQueue class.
 * 
 * @author catch23
 */
public class GlobalEventQueue {
  /** The singleton instance of the GlobalEventQueue */
  protected static GlobalEventQueue singleton = null;
  /** The EventQueue for the GlobalEventQueue */
  protected EventQueue eventQueue;
  
  /**
   * Protected constructor for the singleton instance.
   *
   */
  protected GlobalEventQueue() {
    eventQueue = new EventQueue();
    new Thread( eventQueue, "Capsela Global Event Queue" ).start();
  }
  
  /**
   * Gets the singleton instance of the GlobalEventQueue.
   * @return the singleton instance.
   */
  protected static GlobalEventQueue getInstance() {
    if (singleton == null) {
      singleton = new GlobalEventQueue();
    }
    return singleton;
  }
  
  /**
   * Adds the given <tt>Worker</tt> to the <i>back</t> of the queue.
   * @param r the <tt>Worker</tt> to add.
   */
  public static void add(Worker r) {
    getInstance().addToQueue(r);
  }
  
  /**
   * Adds the given <tt>Worker</tt> to the <i>front</t> of the queue.
   * @param r the <tt>Worker</tt> to add.
   */
  public static void push(Worker r) {
    getInstance().pushToQueue(r);
  }
  
  /**
   * Adds the given <tt>Worker</tt> to the <i>back</t> of the queue.
   * @param r the <tt>Worker</tt> to add.
   */
  protected void addToQueue(Worker r) {
    eventQueue.add(r);
  }
  
  /**
   * Adds the given <tt>Worker</tt> to the <i>front</t> of the queue.
   * @param r the <tt>Worker</tt> to add.
   */
  protected void pushToQueue(Worker r) {
    eventQueue.push(r);
  }
  
  /**
   * Gets the <tt>EventQueue</tt>.
   * @return the EventQueue
   */
  protected EventQueue getEventQueue() {
  	return eventQueue;
  }
  
  /**
   * Gets the <tt>EventQueue</tt>.
   * @return the EventQueue
   */
  public static EventQueue eventQueue() {
  	return getInstance().getEventQueue();
  }
}
