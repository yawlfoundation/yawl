package org.yawlfoundation.yawl.engine.time.workdays;

import org.yawlfoundation.yawl.util.StringUtil;

import javax.xml.datatype.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Michael Adams
 * @date 2/6/17
 */
public class WorkDayAdjuster {

    private HolidayLoader _loader;


    /**
     * Adjust a Duration value so that it will apply only to working days,
     * taking the current moment as a starting date
     * @param duration the value to be adjusted
     * @return the adjusted Duration value
     */
    public Duration adjust(Duration duration) {
        return adjust(createCalendar(), duration);
    }


    /**
     * Adjust a Duration value so that it will apply only to working days
     * @param startDate the moment to start the adjustment from
     * @param duration the value to be adjusted
     * @return the adjusted Duration value
     */
    public Duration adjust(Calendar startDate, Duration duration) {
        Calendar endDate = addDuration(startDate, duration);
        endDate = adjust(startDate, endDate);
        return spanAsDuration(startDate, endDate);
    }


    /**
     * Adjust a Calendar value so that it will apply only to working days,
     * taking the current moment as a starting date
     * @param calendar the value to be adjusted
     * @return the adjusted Calendar value
     */
    public Calendar adjust(Calendar calendar) {
        return adjust(createCalendar(), calendar);
    }


    /**
     * Adjust a Calendar value so that it will apply only to working days
     * @param startDate the moment to start the adjustment from
     * @param endDate the value to be adjusted
     * @return the adjusted Calendar value
     */
    public Calendar adjust(Calendar startDate, Calendar endDate) {
        Calendar stepDate = (GregorianCalendar) startDate.clone();
        calcAdjustedEndDate(stepDate, endDate);
        return endDate;
    }


    /**
     * Adjust a Date value so that it will apply only to working days,
     * taking the current moment as a starting date
     * @param date the value to be adjusted
     * @return the adjusted Date value
     */
    public Date adjust(Date date) {
        return adjust(new Date(), date);
    }


    /**
     * Adjust a Date value so that it will apply only to working days
     * @param start the moment to start the adjustment from
     * @param end the value to be adjusted
     * @return the adjusted Date value
     */
    public Date adjust(Date start, Date end) {
        return adjust(createCalendar(start), createCalendar(end)).getTime();
    }

    

    private Calendar createCalendar() {
        return createCalendar(new Date());
    }


    private Calendar createCalendar(Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return calendar;
    }


    private Calendar addDuration(Calendar start, Duration duration) {
        Calendar summedDate = new GregorianCalendar();
        summedDate.setTimeInMillis(start.getTimeInMillis() + duration.getTimeInMillis(start));
        return summedDate;
    }


    private void calcAdjustedEndDate(Calendar step, Calendar end) {
        while (step.before(end)) {
            step.add(Calendar.DATE, 1);
            if (isWeekend(step) || isHoliday(step)) {
                end.add(Calendar.DATE, 1);
            }
        }
    }


    private Duration spanAsDuration(Calendar start, Calendar end) {
        long diff = end.getTimeInMillis() - start.getTimeInMillis();
        return StringUtil.msecsToDuration(diff);
    }


    private boolean isWeekend(Calendar date) {
        int day = date.get(Calendar.DAY_OF_WEEK);
        return day == Calendar.SATURDAY || day == Calendar.SUNDAY;
    }


    private boolean isHoliday(Calendar date) {
        return getHolidayLoader().isHoliday(date);
    }


    private HolidayLoader getHolidayLoader() {
        if (_loader == null) {
            _loader = new HolidayLoader(true);
        }
        return _loader;
    }


    public static void main(String[] a) {
        Duration d = StringUtil.strToDuration("P1M3DT5H30M");
        Duration e = new WorkDayAdjuster().adjust(d);
        System.out.println("DURATION: " + e.toString());
    }
}
