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

package org.yawlfoundation.yawl.util;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 19/08/13
 */
public class SaxonErrorListener implements ErrorListener {

    List<TransformerException> warnings;
    List<TransformerException> errors;
    List<TransformerException> fatals;

    public SaxonErrorListener() {
        reset();
    }

    public final void reset() {
        warnings = new ArrayList<TransformerException>();
        errors = new ArrayList<TransformerException>();
        fatals = new ArrayList<TransformerException>();
    }


    public void warning(TransformerException e) throws TransformerException {
        warnings.add(e);
    }

    public void error(TransformerException e) throws TransformerException {
        errors.add(e);
    }

    public void fatalError(TransformerException e) throws TransformerException {
        fatals.add(e);
    }


    public List<String> getWarningMessages() {
        return getMessages(warnings);
    }

    public List<String> getErrorMessages() {
        return getMessages(errors);
    }

    public List<String> getFatalMessages() {
        return getMessages(fatals);
    }

    public List<String> getAllMessages() {
        List<String> messages = new ArrayList<String>();
        messages.addAll(getWarningMessages());
        messages.addAll(getErrorMessages());
        messages.addAll(getFatalMessages());
        return messages;
    }


    private List<String> getMessages(List<TransformerException> list) {
        List<String> messages = new ArrayList<String>();
        for (TransformerException e : list) {
             messages.add(e.getMessageAndLocation());
        }
        return messages;
    }
}
