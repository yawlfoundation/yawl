/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.editor.ui.properties.dialog.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CodeletDataMap {

  protected Map<String, String> _codeletMap;

  public CodeletDataMap() {}

  public CodeletDataMap(Map<String, String> codeletMap) {
    _codeletMap = codeletMap;
  }


  public void setCodeletDataMap(Map<String, String> codeletMap) {
    _codeletMap = codeletMap;
  }

  public Map<String, String> getCodeletDataMap() {
    return _codeletMap;
  }

  public List<CodeletData> getCodeletDataAsList() {
      List<CodeletData> result = new ArrayList<CodeletData>();
      TreeMap<String, String> sortedMap = new TreeMap<String, String>(_codeletMap);
      for (String key : sortedMap.keySet()) {
          result.add(new CodeletData(key, prepareTextForWrapping(_codeletMap.get(key))));
      }
      return result;
  }

  private String prepareTextForWrapping(String text) {
      StringBuilder result = new StringBuilder("<html>");

      if ((text != null) && (text.length() > 0))
          result.append(text);
      else
          result.append("No description provided");

      result.append("</html>");
      return result.toString();
  }

}