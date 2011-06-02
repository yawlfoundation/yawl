/*
 * Created on 05/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.yawlfoundation.yawl.editor.specification;

import java.util.LinkedList;

/**
 * A singleton model of specification file state. This model keeps track of 
 * the state of specification files, and publishes state changes to subscribers
 * implementing the the {@link SpecificationFileModelListener} interface. Together, this singleton and 
 * subscribing objects implementing the <code>SpecificationFileModelListener</code> interface are example of the 
 * publish/subscribe design pattern.
 * 
 * @see SpecificationFileModelListener
 * @author Lindsay Bradford
 */

public class SpecificationFileModel {
  static private int fileCount = 0;
  
  private String fileName = "";
  private String engineFileName = "";
  
  /**
   * State indicating that no specification file is currently open
   */
  public static final int IDLE             = 0;

  /**
   * State indicating that a specification file is currently open and being edited
   */
  public static final int EDITING          = 1;

  /**
   * State indicating that some specification file operation is currently in progress
   */
  public static final int BUSY             = 2;
  
  static private int state = IDLE;
  static private int oldState = IDLE;
  
  static private LinkedList subscribers = new LinkedList();
  
  private static final SpecificationFileModel INSTANCE = new SpecificationFileModel();
  
  private SpecificationFileModel() {}
  
  /**
   * Returns the one and only allowable instance of <code>SpecificationFileModel</code>
   * allowed to exist.
   */

  public static SpecificationFileModel getInstance() {
    return INSTANCE; 
  }
  
  /**
   * Adds a new subscriber to list of subscribers wanting to be informed of 
   * <code>SpecificationFileModel</code> state change.  Note that the act of subscription 
   * will have the side-effect of the <code>SpecificationFileModel</code> publishing its current 
   * state to the new subscriber only.
   * 
   * @param subscriber The object needing notification of state change..
   * @see SpecificationFileModelListener
   */
  
  public void subscribe(final SpecificationFileModelListener subscriber) {
    subscribers.add(subscriber);
    subscriber.specificationFileModelStateChanged(state);
  }

  private  void publishState(final int inputState) {
    if (state == BUSY) {
      oldState = inputState;
      return;      
    }
    
    state = inputState;
    publishState();    
  }
  
  private void publishState() {
    for(int i = 0; i < subscribers.size();  i++) {
      SpecificationFileModelListener listener = (SpecificationFileModelListener) subscribers.get(i);
      listener.specificationFileModelStateChanged(state);
    }
  }

  /**
   * A method allowing other objects to inform the model of the opening of a new specification file.
   * This may trigger the publishing of an {@link #EDITING} state to all subscribing 
   * {@link SpecificationFileModelListener} objects.
   * 
   * @see SpecificationFileModelListener
   */
  
  public void incrementFileCount() {
    final int oldFileCount = fileCount;
    fileCount++;
    if (oldFileCount == 0) {
      publishState(EDITING);    
    }
  }

  /**
   * A method allowing other objects to inform the model of the closing of an open specification file.
   * This may trigger the publishing of an {@link #IDLE} state to all subscribing 
   * {@link SpecificationFileModelListener} objects.
   * 
   * @see SpecificationFileModelListener
   */
  
  public void decrementFileCount() {
    final int oldFileCount = fileCount;
    fileCount--;
    if (oldFileCount == 1)  {
        publishState(IDLE);    
    }
  }

  /**
   * A method allowing other objects to retrieve the number of open specification files. This has
   * no state change side-effects. Note that at the moment, this method should return only 0 or 1.
   * @return The number of specification files currently open
   */
  
  public int getFileCount() {
    return fileCount;
  }
  
  /**
   * A method allowing other objects to alert the <CODE>SpecificationFileModel</CODE> that some
   * specification file operation is in progress. This causes a publication of the {@link #BUSY} state
   * to all subscribed {@link SpecificationFileModelListener} objects. Note that the calling object, 
   * by invoking this method, is accepting responsibility for invoking the {@link #notBusy()} method 
   * once the file operation is complete.
   * 
   * @see SpecificationFileModelListener
   * @see #notBusy()
   */
  public void busy() {
    oldState = state;
    publishState(BUSY);  
  }

  /**
   * A method allowing other objects to alert the <CODE>SpecificationFileModel</CODE> that some
   * specification file operation has completed. This causes a publication of the stete what was 
   * in place before the last call to the {@link #busy()} method was made to all currently subscribed
   * {@link SpecificationFileModelListener} objects.
   * 
   * @see SpecificationFileModelListener
   * @see #busy()
   */
  public void notBusy() {
    state = oldState;
    publishState(); 
  }

  /**
   * A method allowing other objects to get the file name that will be used for saving and loading 
   * specification files.
   * @return The name (as a full path) of the specification file.
   */
  public String getFileName() {
    return this.fileName;
  }

  /**
   * A method allowing other objects to set the file name that will be used for saving and loading 
   * specification files.
   * @param fileName The name (as a full path) of the specification file.
   */
  public void setFileName(String fileName) {
    this.fileName = fileName; 
    if (fileName != null) {
      publishState();
    }
  }

  /**
   * A method allowing other objects to get the file name that will be used for engine export of 
   * the current specification.
   * @return The name (as a full path) of the engine XML specification file.
   */
  public String getEngineFileName() {
    return this.engineFileName;
  }

  /**
   * A method allowing other objects to set the file name that will be used for exporting engine 
   * XML specification files.
   * @param fileName The name (as a full path) of the engine XML specification file.
   */
  public void setEngineFileName(String fileName) {
    this.engineFileName = fileName; 
  }
}
