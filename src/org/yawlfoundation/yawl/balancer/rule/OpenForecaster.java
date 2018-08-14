package org.yawlfoundation.yawl.balancer.rule;

import net.sourceforge.openforecast.DataPoint;
import net.sourceforge.openforecast.DataSet;
import net.sourceforge.openforecast.ForecastingModel;
import net.sourceforge.openforecast.Observation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yawlfoundation.yawl.balancer.config.Config;

import java.util.Iterator;

/**
 * @author Michael Adams
 * @date 9/10/17
 */
public class OpenForecaster implements BusynessRule {

    private final DataSet _series = new DataSet();
    private final Logger _log = LogManager.getLogger(this.getClass());
    
    private static final int MIN_MEANINGFUL_QUEUE_SIZE = 10;


    @Override
    public void add(double value) {
        checkSize();
        DataPoint dp = new Observation(value);
        dp.setIndependentValue("timestamp", System.currentTimeMillis());
        _series.add(dp);
    }

    @Override
    public double get() {
        if (_series.size() < MIN_MEANINGFUL_QUEUE_SIZE) {
            return getLastValue(_series);
        }
        ForecastingModel forecaster = net.sourceforge.openforecast.Forecaster.getBestForecast(_series);
        System.out.println("Selected forecasting model: " + forecaster.getForecastType());
        DataSet transport = getForecastTransport();
        forecaster.forecast(transport);
        return getLastValue(transport);
    }


    private DataSet getForecastTransport() {
        DataSet transport = new DataSet();
        long now = System.currentTimeMillis();
        for (int i=0; i< Config.getForecastLookahead(); i++) {
            DataPoint dp = new Observation(0.0);
            dp.setIndependentValue("timestamp", now);
            transport.add(dp);
            now += Config.getPollInterval();
        }
        return transport;
    }


    private double getLastValue(DataSet forecasted) {
        Iterator<DataPoint> itr = forecasted.iterator();
        while (itr.hasNext()) {
            DataPoint dp = itr.next();
            if (! itr.hasNext()) {
                return dp.getDependentValue();
            }
        }
        return 0;
    }


    private void checkSize() {
        if (_series.size() == Config.getForecastQueueSize()) {
            DataPoint first = _series.iterator().next();
            _series.remove(first);
        }
    }
}
