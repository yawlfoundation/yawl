/*
 * Created on 17/05/2005
 * YAWLEditor v1.1-2
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
 */

package org.yawlfoundation.yawl.editor.thirdparty.wofyawl;

import java.util.List;

import org.yawlfoundation.yawl.editor.specification.SpecificationModel;

public interface WofYAWLProxyInterface {

  public static String WOFYAWL_ANALYSIS_PREFERENCE = "wofYawlAnalysisCheck";
  public static String STRUCTURAL_ANALYSIS_PREFERENCE = "wofYawlStructuralAnalysisCheck";
  public static String BEHAVIOURAL_ANALYSIS_PREFERENCE = "wofYawlBehaviouralAnalysisCheck";
  public static String EXTENDED_COVERABILITY_PREFERENCE = "wofYawlExtendedCoverabilityCheck";
  
  public List getAnalysisResults(SpecificationModel editorSpec);
}
