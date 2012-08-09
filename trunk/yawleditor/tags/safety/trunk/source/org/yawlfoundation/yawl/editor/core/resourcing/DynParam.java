package org.yawlfoundation.yawl.editor.core.resourcing;

/**
 * A class that encapsulates one dynamic parameter - i.e. a data variable that at
 * runtime will contain a value corresponding to a participant or role that the
 * task is to be offered to.
 */
public class DynParam {

    public enum Refers { Participant, Role }  // Dynamic Parameter types

    private String _name ;              // the name of the data variable
    private Refers _refers ;               // participant or role

    /** the constructor
     *
     * @param name - the name of a data variable of this task that will contain
     *               a runtime value specifying a particular participant or role.
     * @param refers - either Participant or Role
     */
    public DynParam(String name, Refers refers) {
        _name = name ;
        _refers = refers ;
    }

/*******************************************************************************/

    // GETTERS & SETTERS //

    public String getName() { return _name ; }

    public Refers getRefers() { return _refers ; }

    public void setName(String name) { _name = name; }

    public void setRefers(Refers refers) { _refers = refers; }

    public String getRefersString() {
        return _refers == Refers.Participant ? "participant" : "role" ;
    }

/*******************************************************************************/

    /** this is for the spec file */
    public String toXML() {
        StringBuilder xml = new StringBuilder("<param>");
        xml.append("<name>").append(_name).append("</name>");
        xml.append("<refers>").append(getRefersString()).append("</refers>");
        xml.append("</param>");
        return xml.toString();
    }

}  // end of private class DynParam
