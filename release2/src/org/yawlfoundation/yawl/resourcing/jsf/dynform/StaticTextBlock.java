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
        if (_font == null) _font = new Font("Helvetica", Font.PLAIN, 12);
        return _font;
    }

    public void setFont(Font font) {
        _font = font;
    }

}
