package org.yawlfoundation.yawl.engine.instance;

import org.jdom.Element;

/**
 * Author: Michael Adams
 * Creation Date: 21/12/2009
 */
public interface YInstance {

    public String toXML();

    public void fromXML(String s);

    public void fromXML(Element e);
}
