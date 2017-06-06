package org.yawlfoundation.yawl.engine.time.workdays;

import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author Michael Adams
 * @date 26/5/17
 */
public class Holiday {

    private Calendar _date;
    private String _name;

    protected Holiday() { }                                         // for persistence

    public Holiday(int day, int month, int year, String name) {
        _date = new GregorianCalendar(year, month, day);
        _name = name;
    }

    public Holiday(XNode node) {
        fromXNode(node);
    }


    public boolean matches(Calendar other) {
        return _date != null && other != null &&
            _date.get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
            _date.get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR);
    }


    public int getYear() {
        return _date != null ? _date.get(Calendar.YEAR) : -1;
    }


    public String getName() { return _name; }

    public void setName(String name) { _name = name; }


    public long getTime() { return _date != null ? _date.getTimeInMillis() : -1; }

    public void setTime(long time) {
        if (time > -1) {
            _date = new GregorianCalendar();
            _date.setTimeInMillis(time);
        }
    }


    public String toString() {
        String date = _date != null ? _date.toString() : "";
        return _name + ": " + date;
    }


    private void fromXNode(XNode node) {
        XNode dateNode = node.getChild("date");
        if (dateNode != null) {
            int day = getIntValue(dateNode, "day");
            int month = getIntValue(dateNode, "month");
            int year = getIntValue(dateNode, "year");
            if (day > 0 && month > 0 && year > 0) {
                _date = new GregorianCalendar(year, month, day);
            }
        }
        _name = node.getChildText("localName", true);     // escape
    }
    

    private int getIntValue(XNode dateNode, String name) {
        return StringUtil.strToInt(dateNode.getChildText(name), 0);
    }

}
