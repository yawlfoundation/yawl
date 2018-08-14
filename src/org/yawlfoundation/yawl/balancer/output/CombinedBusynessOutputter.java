package org.yawlfoundation.yawl.balancer.output;

import org.yawlfoundation.yawl.util.StringUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 13/6/18
 */
public class CombinedBusynessOutputter extends AbstractLoadOutputter {

    private String[] buffer = new String[4];
    private static final Object MUTEX = new Object();
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public CombinedBusynessOutputter() {
        super("combined", "busyness", ".log");
        clear();
    }

    @Override
    void writeValues(Map<String, String> values) throws IOException {
        insert(values);
        if (filled()) {
            synchronized (MUTEX) {
                String fDate = SDF.format(new Date());
                _out.write(fDate);
                for (String s : buffer) {
                    if (s == null) continue;
                    _out.write(',');
                    _out.write(s);
                }
                _out.write('\n');
                _out.flush();
                clear();
            }
        }
    }

    public void add(Map<String, String> values) {
        if (_out != null) {
            try {
                writeValues(values);
            }
            catch (IOException ioe) {
                // forget it
            }
        }
    }

    private void insert(Map<String, String> values) {
        int index = StringUtil.strToInt(values.get("index"), -1);
        if (index >= 0 && index <= 3) {
            buffer[index] = values.get("busyness");
        }
    }

    private boolean filled() {
       for (String s : buffer) {
           if (s == null) return false;
       }
       return true;
    }

    private void clear() {
        for (int i=0; i<buffer.length; i++) {
            buffer[i] = null;
        }
    }

    @Override
    String getHeader() {
        return "time,e1,e2,e3,e4";
    }

    @Override
    void finalise() throws IOException {

    }
}
