package org.yawlfoundation.yawl.editor.resourcing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CodeletDataMap implements Serializable, Cloneable {

  private static final long serialVersionUID = 1L;


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