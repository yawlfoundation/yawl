/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.core.resourcing.entity;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.editor.core.resourcing.ResourceDataSet;
import org.yawlfoundation.yawl.editor.core.resourcing.validation.InvalidFilterReference;
import org.yawlfoundation.yawl.resourcing.filters.AbstractFilter;
import org.yawlfoundation.yawl.resourcing.filters.GenericFilter;
import org.yawlfoundation.yawl.resourcing.interactions.ResourceParseException;

import java.util.List;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 12/07/13
 */
public class FilterSet extends EntityCollection<AbstractFilter> {

    public FilterSet() {
        super(false);
    }


    public boolean add(String name) {
        return add(name, null);
    }


    public boolean add(String name, Map<String, String> params) {
        AbstractFilter filter = ResourceDataSet.getFilter(name);
        if (filter != null) {

            // if its one of ours, we want the simple class name only
            String filterName = filter.getCanonicalName();
            if (filterName.startsWith(YAWL_PACKAGE_ROOT)) {
                name = filterName.substring(filterName.lastIndexOf('.') + 1);
            }

            // create new generic filter - only used to generate xml on save
            add(new GenericFilter(name, params));
        }
        else addInvalidReference(new InvalidFilterReference(name));
        return filter != null;
    }


    public AbstractFilter get(String name) {
        for (AbstractFilter f : getAll()) {
            if (f.getCanonicalName().endsWith(name)) return f;
        }
        return null;
    }


    public void parse(Element e, Namespace nsYawl) throws ResourceParseException {
        Element eFilters = e.getChild("filters", nsYawl);
        if (eFilters != null) {
            List<Element> filters = eFilters.getChildren("filter", nsYawl);
            if (filters == null)
                throw new ResourceParseException(
                        "No filter elements found in filters element");

            for (Element eFilter : filters) {
                String filterClassName = eFilter.getChildText("name", nsYawl);
                if (filterClassName != null) {
                        add(filterClassName, parseParams(eFilter, nsYawl));
                }
                else throw new ResourceParseException("Missing filter element: name");
            }
        }
    }

}
