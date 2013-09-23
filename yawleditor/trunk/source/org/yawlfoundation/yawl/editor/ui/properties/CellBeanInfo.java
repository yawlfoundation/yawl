package org.yawlfoundation.yawl.editor.ui.properties;

import com.l2fprod.common.beans.ExtendedPropertyDescriptor;
import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.Property;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.properties.editor.*;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Loads the properties for a CellProperties backing Bean
 *
 * @author Michael Adams
 * @date 4/07/12
 */
public class CellBeanInfo extends NetBeanInfo {

    private java.util.List<Property> _basicProperties;
    private String _category;


    public CellBeanInfo(YAWLVertex vertex) {
        this(CellProperties.class, vertex);
    }

    public CellBeanInfo(Class subClass, YAWLVertex vertex) {
        super(subClass);
        _basicProperties = new ArrayList<Property>();
        setCategory(vertex);
        addCommonProperties();
        addSpecificProperties(vertex);
    }


    public java.util.List<Property> getBasicProperties() { return _basicProperties; }


    private void addCommonProperties() {
        addProperty("id", _category, null, "Internal Identifier");
        addProperty("Documentation", _category, null, "A description of the element")
                .setPropertyEditorClass(TextPropertyEditor.class);
        addProperty("Font", _category, null, "The Font");
        addProperty("Label", _category, null, "Element name");
        addLocation();
        addProperty("idLabelSynch", _category, "Synch ID & Label",
                "Use the label text as the internal identifier");
    }


    private void addLocation() {
        DefaultProperty property = createProperty("Location", _category, Point.class);
        property.setShortDescription("The element's net co-ordinates");
        DefaultProperty propertyX = createProperty("X", null, double.class);
        propertyX.setShortDescription("The element's X co-ordinate");
        DefaultProperty propertyY = createProperty("Y", null, double.class);
        propertyY.setShortDescription("The element's Y co-ordinate");
        property.addSubProperty(propertyX);
        property.addSubProperty(propertyY);
        _basicProperties.add(property);
    }


    private void addSpecificProperties(YAWLVertex vertex) {
        if (! ((vertex instanceof InputCondition) || (vertex instanceof OutputCondition))) {
            addProperty("CellFillColor", _category, "Fill Colour", "The background colour");
        }
        if (vertex instanceof YAWLTask) {
            addProperty("join", _category, "Join Type",
                    "The join type of the selected task")
                    .setPropertyEditorClass(JoinDecoratorEditor.class);
            addProperty("joinPosition", _category, "Join Position",
                    "Where the join is located on the selected task")
                    .setPropertyEditorClass(DecoratorPosEditor.class);
            addProperty("split", _category, "Split Type",
                    "The split type of the selected task")
                    .setPropertyEditorClass(SplitDecoratorEditor.class);
            addProperty("splitPosition", _category, "Split Position",
                    "Where the split is located on the selected task")
                    .setPropertyEditorClass(DecoratorPosEditor.class);
            addProperty("SplitConditions", _category, "Split Predicates",
                    "Specify expressions for each outgoing flow to prioritise runtime" +
                            " control-flow")
                    .setPropertyEditorClass(SplitConditionPropertyEditor.class);
            addProperty("ViewCancelSet", _category, "View Cancel Set",
                    "Display this task's cancellation set");
        }
        if (vertex instanceof YAWLAtomicTask) {
            addProperty("CustomForm", _category, "Custom Form",
                    "A selected web page to use instead of the generic page")
                    .setPropertyEditorClass(CustomFormPropertyEditor.class);
            addProperty("Resourcing", _category, "Resourcing",
                    "Set resourcing properties for the currently selected task")
                    .setPropertyEditorClass(ResourcingPropertyEditor.class);

            // decomposition property
            addProperty("Decomposition", "Decomposition", "Name",
                    "The name of the decomposition associated with the selected task")
                    .setPropertyEditorClass(DecompositionNameEditor.class);

            if (! (vertex instanceof MultipleAtomicTask)) {
                addProperty("Timer", _category, null, "Set a timer on this task")
                        .setPropertyEditorClass(TimerPropertyEditor.class);
            }

            ExtendedPropertyDescriptor property = addProperty("Icon", _category, null, "");
            property.setPropertyEditorClass(IconPropertyEditor.class);
            property.setPropertyTableRendererClass(IconPropertyRenderer.class);
        }
        if (vertex instanceof YAWLCompositeTask) {
            addProperty("Decomposition", "Decomposition", "Sub-net Name",
                    "The name of the sub-net associated with the selected composite task")
                    .setPropertyEditorClass(SubNetNameEditor.class);

        }
        if (vertex instanceof YAWLMultipleInstanceTask) {
            addProperty("miAttributes", _category, "M-I Attributes",
                    "Set the minimum, maximum, threshold and creation mode for this " +
                            "multiple-instance task")
                    .setPropertyEditorClass(MultiInstancePropertyEditor.class);
        }
    }


    private void setCategory(YAWLVertex vertex) {
        _category = (vertex instanceof Condition) ? "Condition" : "Task";
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
           return ResourceLoader.getImageAsIcon(
               "/org/yawlfoundation/yawl/editor/ui/resources/menuicons/Palette"
               + name + "16.gif");
       }

    }


}
