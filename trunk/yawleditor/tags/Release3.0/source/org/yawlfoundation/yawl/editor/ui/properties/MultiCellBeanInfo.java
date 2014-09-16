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

package org.yawlfoundation.yawl.editor.ui.properties;

import com.l2fprod.common.beans.ExtendedPropertyDescriptor;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.properties.editor.*;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;

import javax.swing.*;
import java.util.Set;

/**
 * Loads the properties for a CellProperties backing Bean
 *
 * @author Michael Adams
 * @date 12/06/14
 */
public class MultiCellBeanInfo extends NetBeanInfo {

    private Category _category;
    private Set<YAWLVertex> _vertexSet;

    private enum Category { Tasks, Conditions, Elements }

    public MultiCellBeanInfo(Object[] cells) {
        this(MultiCellProperties.class, cells);
    }

    public MultiCellBeanInfo(Class subClass, Object[] cells) {
        super(subClass);
        _vertexSet = PropertyUtil.makeVertexSet(cells);
        _category = makeCategory();
        addCommonProperties();
        addSpecificProperties();
    }


    private void addCommonProperties() {
        String catName = _category.name();
        addProperty("Documentation", catName, null, "A description of the element")
                .setPropertyEditorClass(TextPropertyEditor.class);
        addProperty("Font", catName, null, "The Font")
                .setPropertyEditorClass(FontPropertyEditor.class);
        addProperty("Label", catName, null, "Element name");
        addProperty("idLabelSynch", catName, "Synch ID & Label",
                "Use the label text as the internal identifier");
        if (! containsTerminalCondition()) {
            addProperty("CellFillColor", catName, "Fill Colour", "The background colour");
        }
    }


    private void addSpecificProperties() {
        String catName = _category.name();
        if (_category == Category.Tasks) {
            addProperty("join", catName, "Join Type",
                    "The join type of the selected task")
                    .setPropertyEditorClass(JoinDecoratorEditor.class);
            addProperty("joinPosition", catName, "Join Position",
                    "Where the join is located on the selected task")
                    .setPropertyEditorClass(DecoratorPosEditor.class);
            addProperty("split", catName, "Split Type",
                    "The split type of the selected task")
                    .setPropertyEditorClass(SplitDecoratorEditor.class);
            addProperty("splitPosition", catName, "Split Position",
                    "Where the split is located on the selected task")
                    .setPropertyEditorClass(DecoratorPosEditor.class);
        }
        if (allInstanceOf(YAWLAtomicTask.class)) {
            addProperty("CustomForm", catName, "Custom Form",
                    "A selected web page to use instead of the generic page")
                    .setPropertyEditorClass(CustomFormPropertyEditor.class);
//            addProperty("Resourcing", catName, "Resourcing",
//                    "Set resourcing properties for the currently selected task")
//                    .setPropertyEditorClass(ResourcingPropertyEditor.class);

            // decomposition property
            addProperty("Decomposition", "Decomposition", "Name",
                    "The name of the decomposition associated with the selected task")
                    .setPropertyEditorClass(DecompositionNameEditor.class);

            if (! (allInstanceOf(MultipleAtomicTask.class))) {
                addProperty("Timer", catName, null, "Set a timer on this task")
                        .setPropertyEditorClass(TimerPropertyEditor.class);
            }

            ExtendedPropertyDescriptor property = addProperty("Icon", catName, null, "");
            property.setPropertyEditorClass(IconPropertyEditor.class);
            property.setPropertyTableRendererClass(IconPropertyRenderer.class);
        }
        if (allInstanceOf(YAWLCompositeTask.class)) {
            addProperty("Decomposition", "Decomposition", "Sub-net Name",
                    "The name of the sub-net associated with the selected composite task")
                    .setPropertyEditorClass(SubNetNameEditor.class);
        }
        if (allInstanceOf(YAWLMultipleInstanceTask.class)) {
            addProperty("miAttributes", catName, "M-I Attributes",
                    "Set the minimum, maximum, threshold and creation mode for this " +
                            "multiple-instance task")
                    .setPropertyEditorClass(MultiInstancePropertyEditor.class);
        }
    }


    private Category makeCategory() {
        Category category = null;
        for (YAWLVertex vertex : _vertexSet) {
            if (vertex instanceof YAWLTask) {
                if (category == null) category = Category.Tasks;
                else if (category != Category.Tasks) return Category.Elements;
            }
            else if (vertex instanceof Condition) {
                if (category == null) category = Category.Conditions;
                else if (category != Category.Conditions) return Category.Elements;
            }
        }
        return category;
    }


    protected boolean allInstanceOf(Class<?> c) {
        for (YAWLVertex vertex : _vertexSet) {
            if (! c.isInstance(vertex)) return false;
        }
        return true;
    }

    private boolean containsTerminalCondition() {
        for (YAWLVertex vertex : _vertexSet) {
             if (vertex instanceof InputCondition || vertex instanceof OutputCondition) {
                 return true;
             }
        }
        return false;
    }


    /******************************************************************************/

    public static class DecoratorPosEditor extends ComboPropertyEditor {
        public DecoratorPosEditor() { super(CellProperties.DECORATOR_POS); }
    }

    public static class SplitDecoratorEditor extends DecoratorEditor {
        public SplitDecoratorEditor() { super("Split"); }
    }

    public static class JoinDecoratorEditor extends DecoratorEditor {
        public JoinDecoratorEditor() { super("Join"); }
    }

    public static class DecoratorEditor extends ComboPropertyEditor {

        public DecoratorEditor(String type) {
            super(CellProperties.DECORATOR);
            setAvailableIcons(getIcons(type));
        }

        private Icon[] getIcons(String type) {
            Icon[] icons = new Icon[4];
            icons[0] = getIcon("And" + type);
            icons[1] = getIcon("Or" + type);
            icons[2] = getIcon("Xor" + type);
            icons[3] = getIcon("No"+ type);
            return icons;
        }

        private Icon getIcon(String name) {
           return ResourceLoader.getMenuIcon(name);
       }

    }

}
