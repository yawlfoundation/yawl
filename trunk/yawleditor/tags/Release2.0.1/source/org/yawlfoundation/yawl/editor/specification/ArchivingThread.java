/*
 * Created on 12/04/2004
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

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.thirdparty.engine.YAWLEngineProxy;

import java.awt.*;

public class ArchivingThread extends Thread {

  private static final int NOTHING = 0;
  private static final int SAVE = 1;
  private static final int SAVEAS = 2;
  private static final int OPEN = 3;
  private static final int OPEN_FILE = 4;
  
  private static final int CLOSE = 5;
  private static final int ENGINE_FILE_IMPORT_FILE = 6;
  private static final int ENGINE_FILE_IMPORT = 7;
  private static final int VALIDATE = 8;
  private static final int ANALYSE = 9;
  
  private static final int EXIT = 10;

  private static final int SLEEP_PERIOD = 100;

  private int request = NOTHING;
  private String openFileName;
  private SpecificationModel specification;

  private static final ArchivingThread INSTANCE = new ArchivingThread();

  public static ArchivingThread getInstance() {
    return INSTANCE;
  }

  private ArchivingThread() {
  }

  public synchronized void save() {
    request = SAVE;
  }

  public synchronized void saveAs() {
    request = SAVEAS;
  }

  public synchronized void open() {
    request = OPEN;
  }

  public synchronized void open(String fileName) {
    openFileName = fileName;
    if (fileName.endsWith(".yawl") || fileName.endsWith(".xml"))
        request = OPEN_FILE;
     else
        request = ENGINE_FILE_IMPORT_FILE;
  }

  public synchronized void close() {
    request = CLOSE;
  }

  public synchronized void engineFileExport(SpecificationModel specification) {
    this.specification = specification;
    request = SAVE;
  }

  public synchronized void engineFileImport() {
    request = ENGINE_FILE_IMPORT;
  }

  public synchronized void validate(SpecificationModel specification) {
    this.specification = specification;
    request = VALIDATE;
  }

  public synchronized void analyse(SpecificationModel specification) {
    this.specification = specification;
    request = ANALYSE;
  }

  public synchronized void exit() {
    request = EXIT;
  }
  
  public void run() {
    while (true) {
      try {
        sleep(SLEEP_PERIOD);
      } catch (Exception e) {}
      processRequestState();
    }
  }

  private synchronized void processRequestState() {
    if (request == NOTHING) {
      return;
    }
    
    Cursor oldCursor = YAWLEditor.getInstance().getCursor();
    YAWLEditor.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    
    SpecificationFileModel.getInstance().busy();
    switch (request) {
      case CLOSE: {
        SpecificationArchiveHandler.getInstance().processCloseRequest();
        break;
      }
      case SAVE: {
        SpecificationArchiveHandler.getInstance().processSaveRequest();
        break;
      }
      case SAVEAS: {
        SpecificationArchiveHandler.getInstance().processSaveAsRequest();
        break;
      }
      case OPEN: {      
        YAWLEngineProxy.getInstance().engineFormatFileImport();
        break;
      }
      case OPEN_FILE: {
        YAWLEngineProxy.getInstance().engineFormatFileImport(openFileName);
        break;
      }
      case EXIT: {
        SpecificationArchiveHandler.getInstance().processExitRequest();
        break;
      }
      case ENGINE_FILE_IMPORT_FILE: {
        SpecificationArchiveHandler.getInstance().processOpenRequest(openFileName);
        break;
      }
      case ENGINE_FILE_IMPORT: {
        SpecificationArchiveHandler.getInstance().processOpenRequest();
        break;
      }
      case VALIDATE: {
        YAWLEngineProxy.getInstance().validate(specification);
        break;
      }
      case ANALYSE: {
        YAWLEngineProxy.getInstance().analyse(specification);
        break;
      }
    }
    request = NOTHING;
    SpecificationFileModel.getInstance().notBusy();
    YAWLEditor.getInstance().setCursor(oldCursor);
  }
}