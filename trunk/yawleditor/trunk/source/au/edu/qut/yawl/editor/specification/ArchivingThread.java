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

package au.edu.qut.yawl.editor.specification;

import au.edu.qut.yawl.editor.swing.JStatusBar;
import au.edu.qut.yawl.editor.thirdparty.engine.YAWLEngineProxy;

public class ArchivingThread extends Thread {

  private static final int NOTHING = 0;
  private static final int SAVE = 1;
  private static final int SAVEAS = 2;
  private static final int OPEN = 3;
  private static final int OPEN_FILE = 4;
  
  private static final int CLOSE = 5;
  private static final int ENGINE_FILE_EXPORT = 6;
  private static final int ENGINE_FILE_IMPORT = 7;

  private static final int VALIDATE = 8;
  private static final int ANALYSE = 9;
  
  private static final int EXIT = 10;

  private static final int SLEEP_PERIOD = 100;

  private int request = NOTHING;
  private String openFileName;

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
    request = OPEN_FILE;
    openFileName = fileName;
  }

  public synchronized void close() {
    request = CLOSE;
  }

  public synchronized void engineFileExport() {
    request = ENGINE_FILE_EXPORT;
  }

  public synchronized void engineFileImport() {
    request = ENGINE_FILE_IMPORT;
  }

  public synchronized void validate() {
    request = VALIDATE;
  }

  public synchronized void analyse() {
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
    SpecificationFileModel.getInstance().busy();
    switch (request) {
      case CLOSE: {
        SpecificationArchiveHandler.getInstance().close();
        break;
      }
      case SAVE: {
        SpecificationArchiveHandler.getInstance().save();
        break;
      }
      case SAVEAS: {
        SpecificationArchiveHandler.getInstance().saveAs();
        break;
      }
      case OPEN: {
        SpecificationArchiveHandler.getInstance().open();
        break;
      }
      case OPEN_FILE: {
        SpecificationArchiveHandler.getInstance().open(openFileName);
        break;
      }
      case ENGINE_FILE_EXPORT: {
        YAWLEngineProxy.getInstance().engineFormatFileExport();
        break;
      }
      case ENGINE_FILE_IMPORT: {
        YAWLEngineProxy.getInstance().engineFormatFileImport();
        break;
      }
      case VALIDATE: {
        YAWLEngineProxy.getInstance().validate();
        break;
      }
      case ANALYSE: {
        YAWLEngineProxy.getInstance().analyse();
        break;
      }
      case EXIT: {
        SpecificationArchiveHandler.getInstance().exit();
        break;
      }
    }
    request = NOTHING;
    SpecificationFileModel.getInstance().notBusy();
  }
}