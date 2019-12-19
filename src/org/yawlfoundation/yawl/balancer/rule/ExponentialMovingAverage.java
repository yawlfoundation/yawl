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

package org.yawlfoundation.yawl.balancer.rule;

/**
 * @author Michael Adams
 * @date 8/8/17
 */
public class ExponentialMovingAverage implements BusynessRule {

    private double _alpha;
    private double _average;

    public ExponentialMovingAverage(double alpha) {
        _alpha = alpha;
        _average = -1;
    }


    public double get() { return _average; }


    // Sn = αY + (1-α)Sn-1
    //    = Sn + α(Y - Sn)
    public void add(double value) {
        _average = _average == -1 ? value : _average + _alpha * (value - _average);
    }
    
}
