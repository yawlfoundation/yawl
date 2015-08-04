/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements.e2wfoj;


/**
 *
 * Representation of Flow relation.
 */
public class RFlow {
    private RElement _priorElement;
    private RElement _nextElement;


    public RFlow(RElement prior, RElement next) {
        _priorElement = prior;
        _nextElement = next;
    }

    public RElement getPriorElement() {
        return _priorElement;
    }


    public RElement getNextElement() {
        return _nextElement;
    }

}

