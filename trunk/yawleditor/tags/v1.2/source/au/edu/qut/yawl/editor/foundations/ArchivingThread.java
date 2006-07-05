/*
 * Created on 12/04/2004
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2003 Queensland University of Technology
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

package au.edu.qut.yawl.editor.foundations;

import au.edu.qut.yawl.editor.specification.*;
import au.edu.qut.yawl.editor.thirdparty.engine.YAWLEngineProxy;
import au.edu.qut.yawl.editor.thirdparty.wofyawl.WofYAWLProxy;

public class ArchivingThread extends Thread {
	
	private static final int NOTHING = 0;
  private static final int SAVE    = 1;
	private static final int SAVEAS  = 2;
	private static final int OPEN    = 3;
	private static final int CLOSE   = 4;

	private static final int EXPORT   = 5;
	private static final int VALIDATE = 6;
  private static final int ANALYSE  = 7;
	
	private static final int SLEEP_PERIOD = 100;
	private int request = NOTHING;

	private static final ArchivingThread INSTANCE = new ArchivingThread();

	public static ArchivingThread getInstance() {
		return INSTANCE;
	}  

	private ArchivingThread() {}

  public synchronized void save() {
    request = SAVE;
  }
  
  public synchronized void saveAs() {
  	request = SAVEAS;
  }
  
  public synchronized void open() {
  	request = OPEN;
  }
  
  public synchronized void close() {
    request = CLOSE;
  }
  
  public synchronized void export() {
		request = EXPORT;
  }
  
  public synchronized void validate() {
		request = VALIDATE;
  }

  public synchronized void analyse() {
    request = ANALYSE;
  }
  
	public void run() {
		while(true) {
			try {
				sleep(SLEEP_PERIOD);
			} catch (Exception e) {};
			processRequestState();
		}
	}
	
	private synchronized void processRequestState() {
		switch(request) {
      case NOTHING: {
      	return;			
      }
      case CLOSE: {
        SpecificationFileModel.getInstance().busy();
        SpecificationArchiveHandler.getInstance().close();
        SpecificationFileModel.getInstance().notBusy();
        break;
      }
      case SAVE: {
        SpecificationFileModel.getInstance().busy();
        SpecificationArchiveHandler.getInstance().save();
        SpecificationFileModel.getInstance().notBusy();
        break;
      }
			case SAVEAS: {
        SpecificationFileModel.getInstance().busy();
        SpecificationArchiveHandler.getInstance().saveAs();
        SpecificationFileModel.getInstance().notBusy();
        break;
			}
      case OPEN: {
        SpecificationFileModel.getInstance().busy();
        SpecificationArchiveHandler.getInstance().open();
        SpecificationFileModel.getInstance().notBusy();
        break;
      }
			case EXPORT: {
        SpecificationFileModel.getInstance().busy();
        YAWLEngineProxy.getInstance().export();
        SpecificationFileModel.getInstance().notBusy();
        break;
			}
			case VALIDATE: {
        SpecificationFileModel.getInstance().busy();
        YAWLEngineProxy.getInstance().validate();
        SpecificationFileModel.getInstance().notBusy();
        break;
			}
      case ANALYSE: {
        SpecificationFileModel.getInstance().busy();
        WofYAWLProxy.getInstance().analyse();
        SpecificationFileModel.getInstance().notBusy();
      }
		}
		request = NOTHING;
  }
}