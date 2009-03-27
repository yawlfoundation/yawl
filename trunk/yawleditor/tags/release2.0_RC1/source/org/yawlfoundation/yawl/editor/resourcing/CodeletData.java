package org.yawlfoundation.yawl.editor.resourcing;

import java.io.Serializable;

/**
 * Author: Michael Adams
 * Creation Date: 23/06/2008
 */
public class CodeletData implements Serializable, Cloneable {

    private String name ;
    private String description ;

    public CodeletData(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public CodeletData() { }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
