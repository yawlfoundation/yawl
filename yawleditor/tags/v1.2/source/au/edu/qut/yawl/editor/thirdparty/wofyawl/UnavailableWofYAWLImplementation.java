/*
 * Created on 17/05/2005
 * YAWLEditor v1.1-2
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2005 Queensland University of Technology
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
 */

package au.edu.qut.yawl.editor.thirdparty.wofyawl;

import java.util.LinkedList;
import java.util.List;

public class UnavailableWofYAWLImplementation implements WofYAWLProxyInterface {

  /* (non-Javadoc)
   * @see au.edu.qut.yawl.editor.thirdparty.wofyawl.WofYAWLProxyInterface#getAnalysisResults()
   */
  public List getAnalysisResults() {
    List resultsList = new LinkedList();
    resultsList.add("Cannot analyse the specificaiton. You do not have the wofYAWL analysis tool installed. ");
    return resultsList;
  }
}
