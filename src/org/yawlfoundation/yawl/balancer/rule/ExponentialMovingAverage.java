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
