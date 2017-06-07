package org.yawlfoundation.yawl.engine.time.workdays;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yawlfoundation.yawl.util.HibernateEngine;
import org.yawlfoundation.yawl.util.SoapClient;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import javax.xml.soap.SOAPException;
import java.io.IOException;
import java.util.*;

/**
 * @author Michael Adams
 * @date 1/6/17
 */
public class HolidayLoader {

    private static final String ENDPOINT = "http://kayaposoft.com/enrico/ws/v1.0/index.php";
    private static final String NAMESPACE = "http://www.kayaposoft.com/enrico/ws/v1.0/";
    private static final String OPERATION = "getPublicHolidaysForYear";
    private static final List<String> KEYS = Arrays.asList("year", "country", "region");

    private Logger _log;
    private Map<Integer, List<Holiday>> _yearHolidayMap;


    public HolidayLoader(boolean restore) {
        if (restore) _yearHolidayMap = restore();
        _log = LogManager.getLogger(this.getClass());
    }


    public void startupCheck(String codes) {
        if (codes != null) {
            String country = null;
            String region =  null;

            if (!codes.isEmpty()) {
                String[] parts = codes.split(";");
                country = parts[0];
                if (parts.length > 1) region = parts[1];
            }
            
            updateLocation(country, region);
        }
    }


    public boolean isHoliday(Calendar date) {
        for (Holiday holiday : getHolidays(date.get(Calendar.YEAR))) {
            if (holiday.isAfter(date)) {                    // can ignore later hols
                return false;
            }
            if (holiday.matches(date)) {
                return true;
            }
        }
        return false;
    }


    public List<Holiday> getHolidays(int year) {
        List<Holiday> holidays = _yearHolidayMap.get(year);
        try {
            if (holidays == null) {
                holidays = load(String.valueOf(year));
                _yearHolidayMap.put(year, holidays);
            }
        }
        catch (Exception e) {
            _log.error(e.getMessage());
            holidays = Collections.emptyList();
        }
        return holidays;
    }

    
    public List<Holiday> load(String year) throws SOAPException, IOException {
        HibernateEngine persister = getPersister();
        HolidayRegion holidayRegion = getLocation(persister);
        if (holidayRegion == null) {
            return Collections.emptyList();
        }
        String country = holidayRegion.getCountry();
        String region = holidayRegion.getRegion();
        List<String> argValues = Arrays.asList(year, country, region);
        List<Holiday> holidays = parseResponse(getResponse(argValues));
        persist(persister, holidays);
        return holidays;
    }

    
    private String getResponse(List<String> argValues) throws SOAPException, IOException {
        SoapClient client = new SoapClient(ENDPOINT);
        return client.send(NAMESPACE, OPERATION, KEYS, argValues);
    }


    private List<Holiday> parseResponse(String response) throws IOException {
        XNode root = new XNodeParser().parse(response);
        while (! (root == null || root.getName().equals("return"))) {
            root = root.getChild(0);
        }
        if (root == null) {
            throw new IOException("No response");
        }
        String error = root.getChildText("error");
        if (error != null) {
            throw new IOException(error);
        }
        List<Holiday> holidays = new ArrayList<Holiday>();
        for (XNode holidayNode : root.getChildren()) {
            holidays.add(new Holiday(holidayNode));
        }
        Collections.sort(holidays);
        return holidays;
    }


    private void persist(HibernateEngine persister, List<Holiday> holidays) {
        for (Holiday holiday : holidays) {
            persister.exec(holiday, HibernateEngine.DB_INSERT, false);
        }
        persister.commit();
    }


    private Map<Integer, List<Holiday>> restore() {
        HibernateEngine persister = getPersister();
        Map<Integer, List<Holiday>> holidayMap = new HashMap<Integer, List<Holiday>>();
        for (Object o : persister.getObjectsForClass("Holiday")) {
            Holiday holiday = (Holiday) o;
            int year = holiday.getYear();
            if (year > -1) {
                List<Holiday> holidays = holidayMap.get(year);
                if (holidays == null) {
                    holidays = new ArrayList<Holiday>();
                    holidayMap.put(year, holidays);
                }
                holidays.add(holiday);
            }
        }
        for (List<Holiday> holidays : holidayMap.values()) {
           Collections.sort(holidays);
        }
        return holidayMap;
    }


    private void updateLocation(String country, String region) {
        HibernateEngine persister = getPersister();
        HolidayRegion holidayRegion = getLocation(persister);
        if (holidayRegion == null) {                           // no stored country
            if (country != null) {                             // new country value
                holidayRegion = new HolidayRegion(country, region);
                persister.exec(holidayRegion, HibernateEngine.DB_INSERT);
            }
        }
        else {                                                 // stored country
            if (! holidayRegion.matches(country, region)) {
                if (country == null) {                         // remove stored
                    persister.exec(holidayRegion, HibernateEngine.DB_DELETE);
                }
                else {                                         // change stored
                    holidayRegion.setCountry(country);
                    holidayRegion.setRegion(region);
                    persister.exec(holidayRegion, HibernateEngine.DB_UPDATE);
                }

                // location changed, remove previously stored holiday entries
                persister.execUpdate("DELETE FROM Holiday");
            }
        }
    }


    private HolidayRegion getLocation(HibernateEngine persister) {
        List rawList = persister.getObjectsForClass("HolidayRegion");
        if (rawList == null || rawList.isEmpty()) {
            return null;
        }
        return (HolidayRegion) rawList.get(0);
    }


    private HibernateEngine getPersister() {
        Set<Class> classSet = new HashSet<Class>();
        classSet.add(Holiday.class);
        classSet.add(HolidayRegion.class);
        return new HibernateEngine(true, classSet);
    }

}
