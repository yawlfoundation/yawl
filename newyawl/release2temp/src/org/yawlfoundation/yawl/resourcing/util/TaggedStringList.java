package org.yawlfoundation.yawl.resourcing.util;

import org.yawlfoundation.yawl.util.StringUtil;

import java.util.ArrayList;

/**
 * Provides the storage of multiple String values for a single (tag) attribute
 *
 * @author Michael Adams
 * Date: 05/03/2008
 */

public class TaggedStringList extends ArrayList<String> {

    private String _tag ;

    // Constructors //
    public TaggedStringList(String tag) {
        super() ;
        _tag = tag ;

    }

    public TaggedStringList(String tag, String value) {
        this(tag);
        add(value);
    }

    public String getTag() { return _tag ; }

    public String toXML() {
        StringBuilder result = new StringBuilder() ;
        for (String value : this)
            result.append(StringUtil.wrap(value, _tag));
        return result.toString() ;
    }
}
