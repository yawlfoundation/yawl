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

import org.hawkular.datamining.forecast.AutomaticForecaster;
import org.hawkular.datamining.forecast.DataPoint;
import org.hawkular.datamining.forecast.ImmutableMetricContext;
import org.hawkular.datamining.forecast.MetricContext;
import org.yawlfoundation.yawl.balancer.config.Config;

import java.util.Date;
import java.util.List;

/**
 * @author Michael Adams
 * @date 14/8/17
 */
public class HawkularForecaster implements BusynessRule {

    private AutomaticForecaster _forecaster;


    public HawkularForecaster(int maxValues, long interval) {
        MetricContext context = new ImmutableMetricContext(null,
                "YAWL Load Balancer", interval);

        _forecaster = new AutomaticForecaster(context, initConfig(maxValues));
        _forecaster.config().setWindowsSize(maxValues);
    }


    public void add(double value) {
        _forecaster.learn(new DataPoint(value, new Date().getTime()));
    }

    public double get() {
        return forecast().getValue();
    }


    public DataPoint forecast() {
        int lookAhead = Config.getForecastLookahead();
        if (lookAhead > 1) {
            List<DataPoint> dataPoints = _forecaster.forecast(lookAhead);
            return dataPoints.get(dataPoints.size() -1);
        }
        return _forecaster.forecast();
    }



    private org.hawkular.datamining.forecast.Forecaster.Config initConfig(int windowSize) {
       return org.hawkular.datamining.forecast.Forecaster.Config.builder()
                .withWindowSize(windowSize).build();
    }
    
}

