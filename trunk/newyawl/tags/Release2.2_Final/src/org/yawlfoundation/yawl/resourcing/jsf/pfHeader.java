/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.appbase.AbstractFragmentBean;
import com.sun.rave.web.ui.component.ImageComponent;

import javax.faces.FacesException;

/**
 * Fragment bean that inserts header on each page
 *
 * @author Michael Adams
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
