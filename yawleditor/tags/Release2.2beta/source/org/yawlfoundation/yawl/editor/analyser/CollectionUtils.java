package org.yawlfoundation.yawl.editor.analyser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A bunch of collection conversion methods added simply to get
 * Moe's reset net analysis to compile against Engine 1.0-RC1.
 * Yes, this is a great, dirtt, steaming hack... There has to be a
 * very good reason indeed for this class shipping with the final release.
 * 
 * @author Lindsay Bradford
 */

public class CollectionUtils {

  
  /**
   * Interim method to get this code to compile against the 1.0-RC1 engine
   * @param key
   * @param list
   * @return
   */
  public static Set getSetFromList(List list) {
    return new HashSet(list);
  }
  
  /**
   * Interim method to get this code to compile against the 1.0-RC1 engine.
   * Creats a map with only one key, and the list supplied as the collection of
   * values for that key.
   * @param key
   * @param list
   * @return
   */
  public static Map getMapFromList(Object key, List list) {
    HashMap map = new HashMap();
    for (Object value : list) {
      map.put(key, value);
    }
    return map;
  }
}
