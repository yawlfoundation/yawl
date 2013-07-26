package org.yawlfoundation.yawl.editor.core.resourcing.entity;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.editor.core.resourcing.DynParam;

/**
 * @author Michael Adams
 * @date 12/07/13
 */
public class DynParamSet extends EntityCollection<DynParam> {

    public DynParamSet() { this(false); }

    public DynParamSet(boolean allowDuplicates) {
        super(allowDuplicates);
    }

    public boolean add(String name) {
        return add(name, DynParam.Refers.Participant);
    }


    public boolean add(String name, DynParam.Refers refers) {
        DynParam exists = get(name);
        if (exists != null) add(new DynParam(name, refers));
        return exists != null;
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
