/*
 * TypeExtensionSorter.java
 *
 * Created on 24 March 2004, 09:02
 */

package org.chiba.tools.schemabuilder;

import org.apache.log4j.Category;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSTypeDefinition;

import java.util.Comparator;

/**
 * @author sof
 */
public class TypeExtensionSorter implements Comparator {
    private static Category LOGGER = SchemaFormBuilder.LOGGER;

    private static TypeExtensionSorter instance;

    static {
        instance = new TypeExtensionSorter();
    }

    public static TypeExtensionSorter getInstance() {
        return instance;
    }

    /**
     * Creates a new instance of TypeExtensionSorter
     */
    private TypeExtensionSorter() {
    }

    public int compare(Object obj1, Object obj2) {
        if (obj1 == null && obj2 != null)
            return -1;
        else if (obj1 != null && obj2 == null)
            return 1;
        else if (obj1 == null && obj2 == null)
            return 0;
        else if (obj1 == obj2)
            return 0;
        else {
            try {
                XSTypeDefinition type1 = (XSTypeDefinition) obj1;
                XSTypeDefinition type2 = (XSTypeDefinition) obj2;

                if (type1.derivedFromType(type2, XSConstants.DERIVATION_EXTENSION))
                    return 1;
                else if (type2.derivedFromType(type1, XSConstants.DERIVATION_EXTENSION))
                    return -1;
                else
                    return 0;
            } catch (java.lang.ClassCastException ex) {
                String s = "ClassCastException in TypeExtensionSorter: one of the types is not a type !";
                s = s + "\n obj1 class = " + obj1.getClass().getName() + ", toString=" + obj1.toString();
                s = s + "\n obj2 class = " + obj2.getClass().getName() + ", toString=" + obj2.toString();
                LOGGER.error(s, ex);
                return 0;
            }
        }
    }
}
