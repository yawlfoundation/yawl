/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.analyser;

import org.yawlfoundation.yawl.analyser.util.YAWLResetAnalyser;
import org.yawlfoundation.yawl.analyser.wofyawl.WofYAWLInvoker;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.unmarshal.YMarshal;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 15/05/12
 */
public class YAnalyser {

    private YAWLResetAnalyser _resetAnalyser;
    private Set<YAnalyserEventListener> _listeners;
    private boolean _cancelled;
    private String _specXML;

    public String analyse(String specXML, YAnalyserOptions options) throws YSyntaxException {
        _specXML = specXML;
        return analyse(YMarshal.unmarshalSpecifications(specXML).get(0), options);
    }


    public String analyse(YSpecification specification, YAnalyserOptions options) {
        StringBuilder results = new StringBuilder("<analysis_results>");
        if (options.isResetAnalysis()) {
            _resetAnalyser = new YAWLResetAnalyser();
            results.append(_resetAnalyser.analyse(specification, options, _listeners));
        }
        if (options.isWofAnalysis() && ! _cancelled) {
            if (_specXML == null) _specXML = specification.toXML();
            results.append(new WofYAWLInvoker().getAnalysisResults(_specXML, options));
        }
        results.append("</analysis_results>");
        return results.toString();
    }


    public void cancelAnalysis() {
        _cancelled = true;
        if (_resetAnalyser != null) _resetAnalyser.cancel();
    }


    public boolean addEventListener(YAnalyserEventListener listener) {
        if (_listeners == null) _listeners = new HashSet<YAnalyserEventListener>();
        return _listeners.add(listener);
    }


    public boolean removeEventListener(YAnalyserEventListener listener) {
        return (_listeners != null) && _listeners.remove(listener);
    }


    public Set<YAnalyserEventListener> getEventListeners() { return _listeners; }

}
