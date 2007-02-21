/*
 * Created on 17/05/2005
 * YAWLEditor v1.1-2
 *
 * @author Lindsay Bradford / Moe Wynn
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

package au.edu.qut.yawl.editor.thirdparty.resetanalysis;

import java.util.List;

public interface ResetAnalysisProxyInterface {

  public static String SOUNDNESS_ANALYSIS_PREFERENCE = "resetSoundnessCheck";
  public static String WEAKSOUNDNESS_ANALYSIS_PREFERENCE = "resetWeakSoundnessCheck";
  public static String CANCELLATION_ANALYSIS_PREFERENCE = "resetCancellationCheck";
  public static String ORJOIN_ANALYSIS_PREFERENCE = "resetOrjoinCheck";
  
  public List getAnalysisResults();
}
