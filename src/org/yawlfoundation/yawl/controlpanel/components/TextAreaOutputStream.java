package org.yawlfoundation.yawl.controlpanel.components;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Michael Adams
 * @date 20/10/2015
 */
public class TextAreaOutputStream extends OutputStream {

    private final JTextArea _textArea;


    public TextAreaOutputStream(JTextArea textArea) {
        super();
        _textArea = textArea;
    }


    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException {
        final String text = new String(buffer, offset, length);
        SwingUtilities.invokeLater(new Runnable () {
            @Override
            public void run() {
                _textArea.append(text);
            }
        });
    }


    @Override
    public void write(int b) throws IOException {
        write (new byte[] {(byte)b}, 0, 1);
    }

}




