package org.yawlfoundation.yawl.balancer;

import org.hawkular.datamining.forecast.AutomaticForecaster;
import org.hawkular.datamining.forecast.DataPoint;
import org.hawkular.datamining.forecast.ImmutableMetricContext;
import org.hawkular.datamining.forecast.MetricContext;

import java.util.Date;
import java.util.List;

/**
 * @author Michael Adams
 * @date 14/8/17
 */
public class Forecaster {

    private AutomaticForecaster _forecaster;


    public Forecaster(int maxValues, long interval) {
        MetricContext context = ImmutableMetricContext.getDefault();
        context.setCollectionInterval(interval);

        _forecaster = new AutomaticForecaster(context, initConfig(maxValues));
        _forecaster.config().setWindowsSize(maxValues);
    }


    public void add(double value) {
        _forecaster.learn(new DataPoint(value, new Date().getTime()));
    }


    public DataPoint forecast(int lookAhead) {
        if (lookAhead > 1) {
            List<DataPoint> dataPoints = _forecaster.forecast(lookAhead);
            return dataPoints.get(dataPoints.size() -1);
        }
        return _forecaster.forecast();
    }

    public DataPoint forecast() { return forecast(1); }


    private org.hawkular.datamining.forecast.Forecaster.Config initConfig(int windowSize) {
       return org.hawkular.datamining.forecast.Forecaster.Config.builder()
                .withWindowSize(windowSize).build();
    }
    
}

