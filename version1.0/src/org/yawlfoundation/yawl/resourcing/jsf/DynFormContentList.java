package org.yawlfoundation.yawl.resourcing.jsf;

import javax.faces.component.UIComponent;
import java.util.ArrayList;

/**
 * Author: Michael Adams
 * Creation Date: 25/02/2008
 */
public class DynFormContentList extends ArrayList<UIComponent> {

    private boolean external;

    private int height;

    public DynFormContentList() { super(); }

    public void setExternal(boolean bool) { external = bool; }

    public boolean isExternal() { return external; }

    public int getHeight() { return height; }

    public void setHeight(int height) { this.height = height; }
    
}
