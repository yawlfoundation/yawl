package org.yawlfoundation.yawl.balancer;

/**
 * @author Michael Adams
 * @date 8/8/17
 */
public class ExponentialMovingAverage {

    private double _alpha;
    private double _average;

    public ExponentialMovingAverage(double alpha) {
        _alpha = alpha;
        _average = -1;
    }


    public double getAverage() { return _average; }


    // Sn = αY + (1-α)Sn-1
    //    = Sn + α(Y - Sn)
    public void add(double value) {
        _average = _average == -1 ? value : _average + _alpha * (value - _average);
    }
    
}
