/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package au.edu.qut.yawl.util;

import java.util.Vector;

/**
 * Extracted from source obtained from http://www.koders.com/
 */

public class Sorter {

    public static Object[] sort(Order order, Object[] objects) {
        return mergeSort(order, objects, 0, objects.length - 1);
    }


    private static Object[] mergeSort(Order order, Object[] objects, int startIndex, int endIndex) {
        if (startIndex > endIndex)
            return null;
        else if (startIndex == endIndex)
            return new Object[]{objects[startIndex]};
        else {
            int midPoint = startIndex + (endIndex - startIndex) / 2;
            return merge(
                    order,
                    mergeSort(order, objects, startIndex, midPoint),
                    mergeSort(order, objects, midPoint + 1, endIndex)
            );
        }
    }

    private static Object[] merge(Order order, Object[] listA, Object[] listB) {
        Vector v = new Vector();
        for (int i = 0, j = 0; (i < listA.length) || (j < listB.length);) {
            if ((i < listA.length) && ((j >= listB.length) || (order.lessThan(listA[i], listB[j]))))
                v.addElement(listA[i++]);
            else
                v.addElement(listB[j++]);
        }
        Object[] ret = new Object[v.size()];
        v.copyInto(ret);
        return ret;
    }

}

