package org.yawlfoundation.yawl.editor.core.resourcing.entity;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.editor.core.resourcing.GenericNonHumanCategory;
import org.yawlfoundation.yawl.editor.core.resourcing.ResourceDataSet;
import org.yawlfoundation.yawl.editor.core.resourcing.validation.InvalidNonHumanCategoryReference;
import org.yawlfoundation.yawl.resourcing.interactions.ResourceParseException;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanCategory;

/**
 * @author Michael Adams
 * @date 12/07/13
 */
public class NonHumanCategorySet extends EntityCollection<GenericNonHumanCategory> {

    public NonHumanCategorySet() { this(false); }

    public NonHumanCategorySet(boolean allowDuplicates) {
        super(allowDuplicates);
    }


    public boolean add(String id) {
        return add(id, null);
    }


    public boolean add(NonHumanCategory category, String subCategory) {
        return add(category.getID(), subCategory);
    }


    public boolean add(String id, String subCategory) {
        NonHumanCategory category = ResourceDataSet.getNonHumanResourceCategory(id);
        if (category != null) {

            // create new generic category - only used to generate xml on save
            add(new GenericNonHumanCategory(id, category.getName(), subCategory));
        }
        else addInvalidReference(new InvalidNonHumanCategoryReference(id));
        return category != null;
    }


    public GenericNonHumanCategory get(String id) {
        for (GenericNonHumanCategory c : getAll()) {
            if (c.getID().equals(id)) return c;
        }
        return null;
    }


    public void parse(Element e, Namespace nsYawl) throws ResourceParseException {
        if (e == null) return;

        for (Element eCat : e.getChildren("nonHumanCategory", nsYawl)) {
            add(eCat.getText(), eCat.getAttributeValue("subcategory"));
        }
    }


}
