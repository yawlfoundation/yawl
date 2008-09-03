/* $Id: ExtensionMatcher.java,v 1.4.2.2 2007/01/18 21:31:53 eric Exp $
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
package proguard.util;

/**
 * This StringMatcher tests whether strings end in a given extension.
 *
 * @author Eric Lafortune
 */
public class ExtensionMatcher implements StringMatcher
{
    private String extension;


    /**
     * Creates a new StringMatcher.
     * @param extension the extension against which strings will be matched.
     */
    public ExtensionMatcher(String extension)
    {
        this.extension = extension;
    }


    // Implementations for StringMatcher.

    public boolean matches(String string)
    {
        return string.endsWith(extension);
    }
}
