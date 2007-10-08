/* $Id: KeepAttributeElement.java,v 1.3.2.2 2007/01/18 21:31:51 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2007 Eric Lafortune (eric@graphics.cornell.edu)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package proguard.ant;

import org.apache.tools.ant.types.*;

import java.util.*;

/**
 * This DataType represents a named attribute in Ant.
 *
 * @author Eric Lafortune
 */
public class KeepAttributeElement extends DataType
{
    private String name;


    /**
     * Adds the contents of this element to the given list of attributes.
     * @param keepAttributes the list of attributes to be extended.
     */
    public void appendTo(List keepAttributes)
    {
        // Get the referenced element, or else this one.
        KeepAttributeElement keepAttributeElement = isReference() ?
            (KeepAttributeElement)getCheckedRef(this.getClass(),
                                                this.getClass().getName()) :
            this;

        String name = keepAttributeElement.name;

        if (name == null)
        {
            // Clear the list to keep all attributes.
            keepAttributes.clear();
        }
        else
        {
            // Add the attibute name to the list.
            keepAttributes.add(name);
        }
    }


    // Ant task attributes.

    public void setName(String name)
    {
        this.name = name;
    }
}
