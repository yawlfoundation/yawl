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
import org.yawlfoundation.yawl.editor.core.resourcing.DynParam;

/**
 * @author Michael Adams
 * @date 12/07/13
 */
public class DynParamSet extends EntityCollection<DynParam> {

    public DynParamSet() {
        super(false);
    }

    public boolean add(String name) {
        return add(name, DynParam.Refers.Participant);
    }


    public boolean add(String name, DynParam.Refers refers) {
        DynParam exists = get(name);
        if (exists == null) add(new DynParam(name, refers));
        return exists == null;
    }


    public DynParam get(String name) {
        for (DynParam d : getAll()) {
            if (d.getName().equals(name)) return d;
        }
        return null;
    }


    public void parse(Element e, Namespace nsYawl) {
        if (e != null) {
            for (Element eParam : e.getChildren("param", nsYawl)) {
                String name = eParam.getChildText("name", nsYawl);
                String refers = eParam.getChildText("refers", nsYawl);
                DynParam.Refers pType = refers.equals("role") ?
                        DynParam.Refers.Role : DynParam.Refers.Participant;
                add(name, pType);
            }
        }
    }

}
