/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.appbase.AbstractFragmentBean;
import com.sun.rave.web.ui.component.ImageComponent;

import javax.faces.FacesException;

/**
 * Fragment bean that inserts header on each page
 *
 * @author: Michael Adams
 * Date: 10/12/2007
 */

public class pfHeader extends AbstractFragmentBean {
    private int __placeholder;

    private void _init() throws Exception { }


    public pfHeader() { }


    public void init() {
        super.init();

        // *Note* - this logic should NOT be modified
        try {
            _init();
        } catch (Exception e) {
            log("pfHeader Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
    }

    public void destroy() { }


    private ImageComponent image1 = new ImageComponent();

    public ImageComponent getImage1() { return image1; }

    public void setImage1(ImageComponent ic) { image1 = ic; }

}
