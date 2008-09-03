package org.yawlfoundation.yawl.editor.specification;

import java.util.Collection;
import java.util.LinkedList;

public class ProblemList extends LinkedList<String> {
  
  public static enum STATUS {
    NO_ENTRIES,
    HAS_ENTRIES
  };
  
  private STATUS status = STATUS.NO_ENTRIES;

  private LinkedList<ProblemListSubscriber> subscribers 
      = new LinkedList<ProblemListSubscriber>();
  
  public void subscribe(ProblemListSubscriber newSubscriber) {
    getSubscribers().add(newSubscriber);
  }
  
  private LinkedList<ProblemListSubscriber> getSubscribers() {
    return this.subscribers;
  }
  
  private void publish(STATUS status) {
    setStatus(status);
    for(ProblemListSubscriber subscriber: getSubscribers()) {
      subscriber.problemListUpdated(status);
    }
  }
  
  private void setStatus(STATUS status) {
    this.status = status;
  }
  
  private STATUS getStatus() {
    return this.status;
  }

  private void publishStatusChangeIfNecessary() {
    if (getStatus() == STATUS.NO_ENTRIES && size() > 0) {
      publish(STATUS.HAS_ENTRIES);
    }
    if (getStatus() == STATUS.HAS_ENTRIES && size() == 0) {
      publish(STATUS.NO_ENTRIES);
    }
  }
  
  public void add(int index, String element) {
    super.add(index, element);
    publishStatusChangeIfNecessary();
  }
  
  public boolean add(String element) {
    boolean result = super.add(element);
    publishStatusChangeIfNecessary();
    return result;    
  }
  
  public boolean addAll(Collection c) {
    boolean result = super.addAll(c);
    publishStatusChangeIfNecessary();
    return result;    
  }
  
  public boolean addAll(int index, Collection c) {
    boolean result = super.addAll(index, c);
    publishStatusChangeIfNecessary();
    return result;    
  }
  
  public boolean remove(String element) {
    boolean result = super.remove(element);
    publishStatusChangeIfNecessary();
    return result;    
  }
  
  public boolean removeAll(Collection c) {
    boolean result = super.removeAll(c);
    publishStatusChangeIfNecessary();
    return result;    
  }
  
  public boolean retainAll(Collection c) {
    boolean result = super.retainAll(c);
    publishStatusChangeIfNecessary();
    return result;    
  }
  
  public void clear() {
    super.clear();
    publishStatusChangeIfNecessary();
  }
}
