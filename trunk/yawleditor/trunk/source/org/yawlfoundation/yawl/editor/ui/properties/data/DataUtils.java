/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.properties.data;

import org.yawlfoundation.yawl.editor.core.data.YDataHandlerException;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.util.StringUtil;

/**
 * @author Michael Adams
 * @date 2/06/2014
 */
public class DataUtils {

    // removes curly braces and xml tags from start and end of binding
    public static String unwrapBinding(String binding) {
        String unwrapped = removeSurroundingBraces(binding);
        unwrapped = StringUtil.unwrap(unwrapped);
        return removeSurroundingBraces(unwrapped);
    }


    public static String wrapBinding(String tagName, String binding) {
        if (StringUtil.isNullOrEmpty(binding)) return null;
        boolean needsBrackets = needsBrackets(binding);
        StringBuilder s = new StringBuilder();
        s.append('<').append(tagName).append(">");
        if (needsBrackets) s.append("{");
        s.append(binding);
        if (needsBrackets) s.append("}");
        s.append("</").append(tagName).append('>');
        return s.toString();
    }


    public static String createBinding(String decompositionID, String varName,
                                       String dataType) {
        StringBuilder s = new StringBuilder("/");
        s.append(decompositionID)
         .append("/")
         .append(varName)
         .append("/")
         .append(getXQuerySuffix(dataType));
        return s.toString();
    }


    public static String createBinding(VariableRow row) {
        return createBinding(row.getDecompositionID(), row.getName(), row.getDataType());
    }


    private static String getXQuerySuffix(String dataType) {
        try {
            return SpecificationModel.getHandler().getDataHandler().getXQuerySuffix(
                             dataType);
        }
        catch (YDataHandlerException ydhe) {
            return "";
        }
    }


    private static boolean needsBrackets(String binding) {
        String s = binding.trim();
        return ! (s.startsWith("<") || s.startsWith("{"));
    }


    private static String removeSurroundingBraces(String binding) {
        if (binding == null) return null;
        String s = binding.trim();
        if (s.startsWith("{") && s.endsWith("}")) {
            String unBraced = s.substring(1, s.length() - 1);
            if (! (unBraced.contains("{") || unBraced.contains("}"))) {
                return unBraced;
            }
        }
        return s;
    }

}
