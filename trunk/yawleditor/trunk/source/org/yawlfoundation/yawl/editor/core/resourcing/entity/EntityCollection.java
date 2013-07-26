package org.yawlfoundation.yawl.editor.core.resourcing.entity;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.editor.core.resourcing.YResourceHandler;
import org.yawlfoundation.yawl.editor.core.resourcing.validation.InvalidReference;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.resourcing.interactions.ResourceParseException;

import java.util.*;

/**
 * @author Michael Adams
 * @date 12/07/13
 */
public abstract class EntityCollection<E> {

    private AbstractCollection<E> _entities;
    private Set<InvalidReference> _invalidReferences;
    private boolean _allowDuplicates;

    protected static final String YAWL_PACKAGE_ROOT = "org.yawlfoundation.yawl.";

    public static final boolean ALLOW_DUPLICATES = true;


    public EntityCollection() { this(false); }      // defaults to unique members

    public EntityCollection(boolean allowDuplicates) {
        _allowDuplicates = allowDuplicates;
        init(null);
    }


    public void set(AbstractCollection<E> collection) {
        init(collection);
    }


    public boolean add(E e) {
        if (e != null) {
            _entities.add(e);
        }
        return e != null;
    }


    public void addCSV(String idList) {
        String[] ids = idList.split(",") ;
        for (String id : ids) add(id.trim());
    }


    public AbstractCollection<E> getAll() { return _entities; }


    public boolean contains(E e) { return getAll().contains(e); }


    public boolean remove(E e) { return getAll().remove(e); }


    public void clear() { _entities.clear(); }


    public boolean remove(String id) {
        E e = get(id);
        return e != null && remove(e);
    }

    public boolean isEmpty() { return _entities == null || _entities.isEmpty(); }

    public int size() { return _entities.size(); }


    public Set<InvalidReference> getInvalidReferences() {
        return _invalidReferences != null ? _invalidReferences :
                Collections.<InvalidReference>emptySet();
    }


    protected void addInvalidReference(InvalidReference reference) {
        if (_invalidReferences == null) {
            _invalidReferences = new HashSet<InvalidReference>();
        }
        _invalidReferences.add(reference);
    }


    protected YResourceHandler getResourceHandler() {
        return SpecificationModel.getHandler().getResourceHandler();
    }

    protected Map<String, String> parseParams(Element e, Namespace nsYawl) {
        Map<String, String> result = new HashMap<String, String>() ;
        Element eParams = e.getChild("params", nsYawl);
        if (eParams != null) {
            for (Element eParam : eParams.getChildren("param", nsYawl)) {
                result.put(eParam.getChildText("key", nsYawl),
                           eParam.getChildText("value", nsYawl));
             }
        }
        return result ;
    }


    private void init(AbstractCollection<E> collection) {
        _entities = _allowDuplicates ? new ArrayList<E>() : new HashSet<E>();
        if (collection != null) _entities.addAll(collection);
    }


    public abstract boolean add(String id);

    public abstract E get(String id);

    public abstract void parse(Element e, Namespace nsYawl) throws ResourceParseException;

}
