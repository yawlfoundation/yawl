/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.monitor.sort;

import org.yawlfoundation.yawl.engine.instance.ParameterInstance;

import java.util.Comparator;

/**
 * Author: Michael Adams
 * Creation Date: 10/12/2009
 * Based on code sourced from http://stackoverflow.com/
 */
public enum ParamInstanceComparator implements Comparator<ParameterInstance> {
    Name {
        public int compare(ParameterInstance o1, ParameterInstance o2) {
            return compareStrings(o1.getName(), o2.getName());
        }},
    DataType {
        public int compare(ParameterInstance o1, ParameterInstance o2) {
            return compareStrings(o1.getDataType(), o2.getDataType());
        }},
    DataSchema {
        public int compare(ParameterInstance o1, ParameterInstance o2) {
            return compareStrings(o1.getDataSchema(), o2.getDataSchema());
        }},
    Usage {
        public int compare(ParameterInstance o1, ParameterInstance o2) {
            return compareStrings(o1.getUsage().name(), o2.getUsage().name());
        }},
    InputPredicate {
        public int compare(ParameterInstance o1, ParameterInstance o2) {
            return compareStrings(o1.getInputPredicate(), o2.getInputPredicate());
        }},
    OutputPredicate {
        public int compare(ParameterInstance o1, ParameterInstance o2) {
            return compareStrings(o1.getOutputPredicate(), o2.getOutputPredicate());
        }},
    OriginalValue {
        public int compare(ParameterInstance o1, ParameterInstance o2) {
            return compareStrings(o1.getOriginalValue(), o2.getOriginalValue());
        }},
    DefaultValue {
        public int compare(ParameterInstance o1, ParameterInstance o2) {
            return compareStrings(o1.getDefaultValue(), o2.getDefaultValue());
        }},
    Value {
        public int compare(ParameterInstance o1, ParameterInstance o2) {
            return compareStrings(o1.getValue(), o2.getValue());
        }};



   private static int compareStrings(String a, String b) {
       if (a == null) return -1;
       if (b == null) return 1;
       return a.compareTo(b);
   }


    public static Comparator<ParameterInstance> descending(final Comparator<ParameterInstance> other) {
        return new Comparator<ParameterInstance>() {
            public int compare(ParameterInstance o1, ParameterInstance o2) {
                return -1 * other.compare(o1, o2);
            }
        };
    }

    public static Comparator<ParameterInstance> getComparator(final ParamInstanceComparator... multipleOptions) {
        return new Comparator<ParameterInstance>() {
            public int compare(ParameterInstance o1, ParameterInstance o2) {
                for (ParamInstanceComparator option : multipleOptions) {
                    int result = option.compare(o1, o2);
                    if (result != 0) {
                        return result;
                    }
                }
                return 0;
            }
        };
    }

    
    public static Comparator<ParameterInstance> getComparator(ParamOrder paramOrder) {
        Comparator<ParameterInstance> comparator = null;
        switch (paramOrder.getColumn()) {
            case Name : comparator = getComparator(Name); break;
            case DataType : comparator = getComparator(DataType, Name); break;
            case DataSchema : comparator = getComparator(DataSchema, Name); break;
            case Usage : comparator = getComparator(Usage, Name); break;
            case InputPredicate : comparator = getComparator(InputPredicate, Name); break;
            case OutputPredicate : comparator = getComparator(OutputPredicate, Name); break;
            case OriginalValue : comparator = getComparator(OriginalValue, Name); break;
            case DefaultValue : comparator = getComparator(DefaultValue, Name); break;
            case Value : comparator = getComparator(Value, Name);
        }
        if ((comparator != null) && ! paramOrder.isAscending()) {
            comparator = descending(comparator);
        }
        return comparator;
    }

}