/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing.jsf.dynform;

import com.sun.rave.web.ui.component.StaticText;

import java.awt.*;

/**
 * Author: Michael Adams
 * Creation Date: 16/03/2010
 */
public class StaticTextBlock extends StaticText {

    private Font _font;

    public StaticTextBlock() {
        super();
    }

    public Font getFont() {
        if (_font == null) _font = DynFormFont._defFormFont;
        return _font;
    }

    public void setFont(Font font) {
        _font = font;
    }

}
