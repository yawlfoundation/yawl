package org.yawlfoundation.yawl.elements;

import org.jdom2.Element;
import org.yawlfoundation.yawl.engine.YWorkItemStatus;
import org.yawlfoundation.yawl.engine.time.YTimer;
import org.yawlfoundation.yawl.engine.time.YWorkItemTimer;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import javax.xml.datatype.Duration;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.yawlfoundation.yawl.engine.YWorkItemStatus.statusEnabled;
import static org.yawlfoundation.yawl.engine.YWorkItemStatus.statusExecuting;

/**
 * @author Michael Adams
 * @date 30/07/12
 */
public class YTimerParameters {

    // the ways in which timer parameters may be expressed
    public enum TimerType { Duration, Expiry, Interval, LateBound, Nil }

    private String _variableName;                         // late bound net variable
    private Date _expiryTime;                             // date param
    private Duration _duration;                           // duration param
    private long _ticks;                                  // interval params
    private YTimer.TimeUnit _timeUnit;                    // ditto
    private YWorkItemTimer.Trigger _trigger;
    private TimerType _timerType;


    public YTimerParameters() { _timerType = TimerType.Nil; }

    public YTimerParameters(String netParamName) { set(netParamName); }

    public YTimerParameters(YWorkItemTimer.Trigger trigger, Date expiryTime) {
        set(trigger, expiryTime);
    }

    public YTimerParameters(YWorkItemTimer.Trigger trigger, Duration duration) {
        set(trigger, duration);
    }

    public YTimerParameters(YWorkItemTimer.Trigger trigger, long ticks,
                                       YTimer.TimeUnit timeUnit) {
        set(trigger, ticks, timeUnit);
    }


    /**
     * sets
     * @param netParamName
     */
    public void set(String netParamName) {
        _variableName = netParamName;
        _timerType = TimerType.LateBound;
    }


    public void set(YWorkItemTimer.Trigger trigger, Date expiryTime) {
        _trigger = trigger;
        _expiryTime = expiryTime;
        _timerType = TimerType.Expiry;
    }


    public void set(YWorkItemTimer.Trigger trigger, Duration duration) {
        _trigger = trigger;
        _duration = duration;
        _timerType = TimerType.Duration;
    }


    public void set(YWorkItemTimer.Trigger trigger, long ticks,
                                   YTimer.TimeUnit timeUnit) {
        _trigger = trigger;
        _ticks = ticks;
        _timeUnit = timeUnit != null ? timeUnit : YTimer.TimeUnit.MSEC;
        _timerType = TimerType.Interval;
    }


    public boolean statusMatchesTrigger(YWorkItemStatus status) {
        if (_timerType == TimerType.Nil) return false;
        switch (_trigger) {
            case OnEnabled: return status.equals(statusEnabled);
            case OnExecuting: return status.equals(statusExecuting);
        }
        return false;
    }


    public String getVariableName() { return _variableName; }

    public void setVariableName(String varName) {
        _variableName = varName;
        _timerType = TimerType.LateBound;
    }


    public Date getDate() { return _expiryTime; }

    public void setDate(Date date) {
        this._expiryTime = date;
        _timerType = TimerType.Expiry;
    }


    public Duration getDuration() { return _duration; }

    public void setDuration(Duration duration) {
        _duration = duration;
        _timerType = TimerType.Duration;
    }


    public long getTicks() { return _ticks; }

    public void setTicks(long ticks) {
        _ticks = ticks;
        _timerType = TimerType.Interval;
    }


    public YTimer.TimeUnit getTimeUnit() { return _timeUnit; }

    public void setTimeUnit(YTimer.TimeUnit timeUnit) {
        _timeUnit = timeUnit;
        _timerType = TimerType.Interval;
    }


    public YWorkItemTimer.Trigger getTrigger() { return _trigger; }

    public void setTrigger(YWorkItemTimer.Trigger trigger) { this._trigger = trigger; }


    public TimerType getTimerType() { return _timerType; }


    public void parseYTimerType(Element eTimerTypeValue) throws IllegalArgumentException {
        XNode node = new XNodeParser(true).parse(eTimerTypeValue);
        if (node == null) throw new IllegalArgumentException("Invalid YTimerType XML");

        String triggerStr = node.getChildText("trigger");
        if (triggerStr == null) throw new IllegalArgumentException("Missing 'trigger' parameter");

        // throws IllegalArgumentException if triggerStr is not a valid Trigger
        YWorkItemTimer.Trigger trigger = YWorkItemTimer.Trigger.valueOf(triggerStr);

        String expiry = node.getChildText("expiry");
        if (expiry == null) throw new IllegalArgumentException("Missing 'expiry' parameter");

        if (expiry.startsWith("P")) {         // duration types start with P
            Duration duration = StringUtil.strToDuration(expiry);
            if (duration == null) throw new IllegalArgumentException("Malformed duration value");
            set(trigger,  duration);
        }
        else {
            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            try {
                Date date = sdf.parse(expiry);                       // test for dateTime
                set(trigger, date);
            }
            catch (ParseException pe) {
                // do nothing here - trickle down
            }

            long time = StringUtil.strToLong(expiry, -1);           // test for long
            if (time < 0) throw new IllegalArgumentException("Malformed expiry value");
            set(trigger, new Date(time));
        }
    }


    public String toXML() {
        if (_timerType == TimerType.Nil) return "";

        XNode node = new XNode("timer");
        switch (_timerType) {
            case Duration: {
                node.addChild("trigger", _trigger.name());
                node.addChild("duration", _duration.toString());
                break;
            }
            case Expiry: {
                node.addChild("trigger", _trigger.name());
                node.addChild("expiry", _expiryTime.getTime());
                break;
            }
            case Interval: {
                node.addChild("trigger", _trigger.name());
                XNode params = node.addChild("durationparams");
                params.addChild("ticks", _ticks);
                params.addChild("interval", _timeUnit.name());
                break;
            }
            case LateBound: {
                node.addChild("netparam", _variableName);
                break;
            }
        }
        return node.toString();
    }


    public String toString() {
        if (_timerType == TimerType.Nil) return "Nil";
        String s = _trigger == YWorkItemTimer.Trigger.OnExecuting ? "Start: " : "Offer: ";
        switch (_timerType) {
            case Duration: s += _duration.toString(); break;
            case Expiry: s += new SimpleDateFormat().format(_expiryTime); break;
            case Interval: s += _ticks + " " + _timeUnit.name(); break;
            case LateBound: s = "Variable: " + _variableName; break;
        }
        return s;
    }

}
