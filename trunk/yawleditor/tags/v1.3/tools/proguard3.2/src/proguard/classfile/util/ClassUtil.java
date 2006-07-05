/* $Id: ClassUtil.java,v 1.21 2004/08/15 12:39:30 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2004 Eric Lafortune (eric@graphics.cornell.edu)
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
package proguard.classfile.util;

import proguard.classfile.*;

import java.io.*;
import java.util.*;


/**
 * Utility methods for converting between internal and external representations
 * of names and descriptions.
 *
 * @author Eric Lafortune
 */
public class ClassUtil
{
    private static final String EMPTY_STRING = "";

    private static final InternalTypeEnumeration internalTypeEnumeration = new InternalTypeEnumeration();
    private static final ExternalTypeEnumeration externalTypeEnumeration = new ExternalTypeEnumeration();


    /**
     * Checks whether the given class file magic number is correct.
     * @param magicNumber the magic number.
     * @throws IOException when the magic number is incorrect.
     */
    public static void checkMagicNumber(int magicNumber) throws IOException
    {
        if (magicNumber != ClassConstants.MAGIC)
        {
            throw new IOException("Invalid magic number ["+Integer.toHexString(magicNumber)+"] in class file");
        }
    }


    /**
     * Checks whether the given class file version numbers are supported.
     * @param majorVersionNumber the major version number.
     * @param minorVersionNumber the minor version number.
     * @throws IOException when the version is not supported.
     */
    public static void checkVersionNumbers(int majorVersionNumber, int minorVersionNumber) throws IOException
    {
        if (majorVersionNumber < ClassConstants.MAJOR_VERSION_MIN ||
            (majorVersionNumber == ClassConstants.MAJOR_VERSION_MIN &&
             minorVersionNumber <  ClassConstants.MINOR_VERSION_MIN) ||
            (majorVersionNumber == ClassConstants.MAJOR_VERSION_MAX &&
             minorVersionNumber >  ClassConstants.MINOR_VERSION_MAX) ||
            majorVersionNumber > ClassConstants.MAJOR_VERSION_MAX)
        {
            throw new IOException("Unsupported version number ["+majorVersionNumber+"."+minorVersionNumber+"] for class file format");
        }
    }


    /**
     * Converts an external class name into an internal class name.
     * @param externalClassName the external class name,
     *                          e.g. "<code>java.lang.Object</code>"
     * @return the internal class name,
     *                          e.g. "<code>java/lang/Object</code>".
     */
    public static String internalClassName(String externalClassName)
    {
        return externalClassName.replace(ClassConstants.EXTERNAL_PACKAGE_SEPARATOR,
                                         ClassConstants.INTERNAL_PACKAGE_SEPARATOR);
    }


    /**
     * Converts an internal class description into an external class description.
     * @param accessFlags       the access flags of the class.
     * @param internalClassName the internal class name,
     *                          e.g. "<code>java/lang/Object</code>".
     * @return the external class description,
     *                          e.g. "<code>public java.lang.Object</code>".
     */
    public static String externalFullClassDescription(int    accessFlags,
                                                      String internalClassName)
    {
        return externalClassAccessFlags(accessFlags) +
               externalClassName(internalClassName);
    }


    /**
     * Converts an internal class name into an external class name.
     * @param internalClassName the internal class name,
     *                          e.g. "<code>java/lang/Object</code>".
     * @return the external class name,
     *                          e.g. "<code>java.lang.Object</code>".
     */
    public static String externalClassName(String internalClassName)
    {
        return //internalClassName.startsWith(ClassConstants.INTERNAL_PACKAGE_JAVA_LANG) &&
               //internalClassName.indexOf(ClassConstants.INTERNAL_PACKAGE_SEPARATOR, ClassConstants.INTERNAL_PACKAGE_JAVA_LANG.length() + 1) < 0 ?
               //internalClassName.substring(ClassConstants.INTERNAL_PACKAGE_JAVA_LANG.length()) :
               internalClassName.replace(ClassConstants.INTERNAL_PACKAGE_SEPARATOR,
                                         ClassConstants.EXTERNAL_PACKAGE_SEPARATOR);
    }


    /**
     * Converts an internal class name into an external short class name, without
     * package specification.
     * @param externalClassName the external class name,
     *                          e.g. "<code>java.lang.Object</code>"
     * @return the external short class name,
     *                          e.g. "<code>Object</code>".
     */
    public static String externalShortClassName(String externalClassName)
    {
        int index = externalClassName.lastIndexOf(ClassConstants.EXTERNAL_PACKAGE_SEPARATOR);
        return externalClassName.substring(index+1);
    }


    /**
     * Returns whether the given internal type is an array type.
     * @param internalType the internal type,
     *                     e.g. "<code>[[Ljava/lang/Object;</code>".
     * @return <code>true</code> if the given type is an array type,
     *         <code>false</code> otherwise.
     */
    public static boolean isInternalArrayType(String internalType)
    {
        return internalType.length() > 1 &&
               internalType.charAt(0) == ClassConstants.INTERNAL_TYPE_ARRAY;
    }


    /**
     * Returns the number of dimensions of the given internal type.
     * @param internalType the internal type,
     *                     e.g. "<code>[[Ljava/lang/Object;</code>".
     * @return the number of dimensions, e.g. 2.
     */
    public static int internalArrayTypeDimensionCount(String internalType)
    {
        int dimensions = 0;
        while (internalType.charAt(dimensions) == ClassConstants.INTERNAL_TYPE_ARRAY)
        {
            dimensions++;
        }

        return dimensions;
    }


    /**
     * Returns whether the given internal type is a plain primitive type
     * (not void).
     * @param internalType the internal type,
     *                     e.g. "<code>I</code>".
     * @return <code>true</code> if the given type is a class type,
     *         <code>false</code> otherwise.
     */
    public static boolean isInternalPrimitiveType(char internalType)
    {
        return internalType == ClassConstants.INTERNAL_TYPE_BOOLEAN ||
               internalType == ClassConstants.INTERNAL_TYPE_BYTE    ||
               internalType == ClassConstants.INTERNAL_TYPE_CHAR    ||
               internalType == ClassConstants.INTERNAL_TYPE_SHORT   ||
               internalType == ClassConstants.INTERNAL_TYPE_INT     ||
               internalType == ClassConstants.INTERNAL_TYPE_FLOAT   ||
               internalType == ClassConstants.INTERNAL_TYPE_LONG    ||
               internalType == ClassConstants.INTERNAL_TYPE_DOUBLE;
    }


    /**
     * Returns whether the given internal type is a plain class type
     * (not an array type).
     * @param internalType the internal type,
     *                     e.g. "<code>Ljava/lang/Object;</code>".
     * @return <code>true</code> if the given type is a class type,
     *         <code>false</code> otherwise.
     */
    public static boolean isInternalClassType(String internalType)
    {
        int length = internalType.length();
        return length > 1 &&
               internalType.charAt(0)        == ClassConstants.INTERNAL_TYPE_CLASS_START &&
               internalType.charAt(length-1) == ClassConstants.INTERNAL_TYPE_CLASS_END;
    }


    /**
     * Returns the internal element type of a given internal array type.
     * @param internalArrayType the internal array type,
     *                          e.g. "<code>[[Ljava/lang/Object;</code>" or
     *                               "<code>[I</code>".
     * @return the internal type of the array elements,
     *                          e.g. "<code>Ljava/lang/Object;</code>" or
     *                               "<code>I</code>".
     */
    public static String internalTypeFromArrayType(String internalArrayType)
    {
        int index = internalArrayType.lastIndexOf(ClassConstants.INTERNAL_TYPE_ARRAY);
        return internalArrayType.substring(index+1);
    }


    /**
     * Returns the internal class name of a given internal class type.
     * @param internalClassType the internal class type,
     *                          e.g. "<code>Ljava/lang/Object;</code>".
     * @return the internal class name,
     *                          e.g. "<code>java/lang/Object</code>".
     */
    public static String internalClassNameFromClassType(String internalClassType)
    {
        return internalClassType.substring(1, internalClassType.length()-1);
    }


    /**
     * Returns the internal class name of any given internal type.
     * The returned class name for primitive array types is
     * "<code>java/lang/Object</code>".
     * @param internalClassType the internal class type,
     *                          e.g. "<code>Ljava/lang/Object;</code>" or
     *                               "<code>[[I</code>".
     * @return the internal class name,
     *                          e.g. "<code>java/lang/Object</code>".
     */
    public static String internalClassNameFromType(String internalClassType)
    {
        // Is it an array type?
        if (isInternalArrayType(internalClassType))
        {
            internalClassType = internalTypeFromArrayType(internalClassType);

            // Is the array of a non-primitive type?
            if (isInternalClassType(internalClassType))
            {
                internalClassType = internalClassNameFromClassType(internalClassType);
            }
            else
            {
                internalClassType = ClassConstants.INTERNAL_NAME_JAVA_LANG_OBJECT;
            }
        }

        return internalClassType;
    }


    /**
     * Returns the internal type of the given internal method descriptor.
     * @param internalMethodDescriptor the internal method descriptor,
     *                                 e.g. "<code>(II)Z</code>".
     * @return the internal return type,
     *                                 e.g. "<code>Z</code>".
     */
    public static String internalMethodReturnType(String internalMethodDescriptor)
    {
        int index = internalMethodDescriptor.indexOf(ClassConstants.INTERNAL_METHOD_ARGUMENTS_CLOSE);
        return internalMethodDescriptor.substring(index + 1);
    }


    /**
     * Returns the number of parameters of the given internal method descriptor.
     * @param internalMethodDescriptor the internal method descriptor,
     *                                 e.g. "<code>(ID)Z</code>".
     * @return the number of parameters,
     *                                 e.g. 2.
     */
    public static int internalMethodParameterCount(String internalMethodDescriptor)
    {
        internalTypeEnumeration.setDescriptor(internalMethodDescriptor);

        int counter = 0;
        while (internalTypeEnumeration.hasMoreTypes())
        {
            internalTypeEnumeration.nextType();

            counter++;
        }

        return counter;
    }


    /**
     * Returns the size taken up on the stack by the parameters of the given
     * internal method descriptor. This accounts for long and double parameters
     * taking up two spaces.
     * @param internalMethodDescriptor the internal method descriptor,
     *                                 e.g. "<code>(ID)Z</code>".
     * @return the size taken up on the stack,
     *                                 e.g. 3.
     */
    public static int internalMethodParameterSize(String internalMethodDescriptor)
    {
        internalTypeEnumeration.setDescriptor(internalMethodDescriptor);

        int size = 0;
        while (internalTypeEnumeration.hasMoreTypes())
        {
            String internalType = internalTypeEnumeration.nextType();

            size += internalTypeSize(internalType);
        }

        return size;
    }


    /**
     * Returns the size taken up on the stack by the given internal type.
     * The size is 1, except for long and double types, for which it is 2,
     * and for the void type, for which 0 is returned
     * @param internalType the internal type,
     *                     e.g. "<code>I</code>".
     * @return the size taken up on the stack,
     *                     e.g. 1.
     */
    public static int internalTypeSize(String internalType)
    {
        if (internalType.length() == 1)
        {
            char internalPrimitiveType = internalType.charAt(0);
            if      (internalPrimitiveType == ClassConstants.INTERNAL_TYPE_LONG ||
                     internalPrimitiveType == ClassConstants.INTERNAL_TYPE_DOUBLE)
            {
                return 2;
            }
            else if (internalPrimitiveType == ClassConstants.INTERNAL_TYPE_VOID)
            {
                return 0;
            }
        }

        return 1;
    }


    /**
     * Converts an external type into an internal type.
     * @param externalType the external type,
     *                     e.g. "<code>java.lang.Object[][]</code>" or
     *                          "<code>int[]</code>".
     * @return the internal type,
     *                     e.g. "<code>[[Ljava/lang/Object;</code>" or
     *                          "<code>[I</code>".
     */
    public static String internalType(String externalType)
    {
        // Strip the array part, if any.
        int dimensionCount = externalArrayTypeDimensionCount(externalType);
        if (dimensionCount > 0)
        {
            externalType = externalType.substring(0, externalType.length() - dimensionCount * ClassConstants.EXTERNAL_TYPE_ARRAY.length());
        }

        // Analyze the actual type part.
        char internalTypeChar =
            externalType.equals(ClassConstants.EXTERNAL_TYPE_VOID   ) ?
                                ClassConstants.INTERNAL_TYPE_VOID     :
            externalType.equals(ClassConstants.EXTERNAL_TYPE_BOOLEAN) ?
                                ClassConstants.INTERNAL_TYPE_BOOLEAN  :
            externalType.equals(ClassConstants.EXTERNAL_TYPE_BYTE   ) ?
                                ClassConstants.INTERNAL_TYPE_BYTE     :
            externalType.equals(ClassConstants.EXTERNAL_TYPE_CHAR   ) ?
                                ClassConstants.INTERNAL_TYPE_CHAR     :
            externalType.equals(ClassConstants.EXTERNAL_TYPE_SHORT  ) ?
                                ClassConstants.INTERNAL_TYPE_SHORT    :
            externalType.equals(ClassConstants.EXTERNAL_TYPE_INT    ) ?
                                ClassConstants.INTERNAL_TYPE_INT      :
            externalType.equals(ClassConstants.EXTERNAL_TYPE_FLOAT  ) ?
                                ClassConstants.INTERNAL_TYPE_FLOAT    :
            externalType.equals(ClassConstants.EXTERNAL_TYPE_LONG   ) ?
                                ClassConstants.INTERNAL_TYPE_LONG     :
            externalType.equals(ClassConstants.EXTERNAL_TYPE_DOUBLE ) ?
                                ClassConstants.INTERNAL_TYPE_DOUBLE   :
            externalType.equals("%"                                 ) ?
                                '%'                                   :
                                (char)0;

        String internalType =
            internalTypeChar != 0 ? ("" + internalTypeChar) :
                                    (ClassConstants.INTERNAL_TYPE_CLASS_START +
                                     internalClassName(externalType) +
                                     ClassConstants.INTERNAL_TYPE_CLASS_END);

        // Prepend the array part, if any.
        for (int count = 0; count < dimensionCount; count++)
        {
            internalType = ClassConstants.INTERNAL_TYPE_ARRAY + internalType;
        }

        return internalType;
    }


    /**
     * Returns the number of dimensions of the given external type.
     * @param externalType the external type,
     *                     e.g. "<code>[[Ljava/lang/Object;</code>".
     * @return the number of dimensions, e.g. 2.
     */
    public static int externalArrayTypeDimensionCount(String externalType)
    {
        int dimensions = 0;
        int length = ClassConstants.EXTERNAL_TYPE_ARRAY.length();
        int offset = externalType.length() - length;
        while (externalType.regionMatches(offset,
                                          ClassConstants.EXTERNAL_TYPE_ARRAY,
                                          0,
                                          length))
        {
            dimensions++;
            offset -= length;
        }

        return dimensions;
    }


    /**
     * Converts an internal type into an external type.
     * @param internalType the internal type,
     *                     e.g. "<code>[[Ljava/lang/Object;</code>" or
     *                          "<code>[I</code>".
     * @return the external type,
     *                     e.g. "<code>java.lang.Object[][]</code>" or
     *                          "<code>int[]</code>".
     */
    public static String externalType(String internalType)
    {
        // Strip the array part, if any.
        int dimensionCount = internalArrayTypeDimensionCount(internalType);
        if (dimensionCount > 0)
        {
            internalType = internalType.substring(dimensionCount);
        }

        // Analyze the actual type part.
        char internalTypeChar = internalType.charAt(0);

        String externalType =
            internalTypeChar == ClassConstants.INTERNAL_TYPE_VOID        ?
                                ClassConstants.EXTERNAL_TYPE_VOID        :
            internalTypeChar == ClassConstants.INTERNAL_TYPE_BOOLEAN     ?
                                ClassConstants.EXTERNAL_TYPE_BOOLEAN     :
            internalTypeChar == ClassConstants.INTERNAL_TYPE_BYTE        ?
                                ClassConstants.EXTERNAL_TYPE_BYTE        :
            internalTypeChar == ClassConstants.INTERNAL_TYPE_CHAR        ?
                                ClassConstants.EXTERNAL_TYPE_CHAR        :
            internalTypeChar == ClassConstants.INTERNAL_TYPE_SHORT       ?
                                ClassConstants.EXTERNAL_TYPE_SHORT       :
            internalTypeChar == ClassConstants.INTERNAL_TYPE_INT         ?
                                ClassConstants.EXTERNAL_TYPE_INT         :
            internalTypeChar == ClassConstants.INTERNAL_TYPE_FLOAT       ?
                                ClassConstants.EXTERNAL_TYPE_FLOAT       :
            internalTypeChar == ClassConstants.INTERNAL_TYPE_LONG        ?
                                ClassConstants.EXTERNAL_TYPE_LONG        :
            internalTypeChar == ClassConstants.INTERNAL_TYPE_DOUBLE      ?
                                ClassConstants.EXTERNAL_TYPE_DOUBLE      :
            internalTypeChar == ClassConstants.INTERNAL_TYPE_CLASS_START ?
                                externalClassName(internalType.substring(1, internalType.indexOf(ClassConstants.INTERNAL_TYPE_CLASS_END))) :
                                null;

        if (externalType == null)
        {
            throw new IllegalArgumentException("Unknown type ["+internalType+"]");
        }

        // Append the array part, if any.
        for (int count = 0; count < dimensionCount; count++)
        {
            externalType = externalType + ClassConstants.EXTERNAL_TYPE_ARRAY;
        }

        return externalType;
    }


    /**
     * Returns whether the given internal descriptor String represents a method
     * descriptor.
     * @param internalDescriptor the internal descriptor String,
     *                           e.g. "<code>(II)Z</code>".
     * @return <code>true</code> if the given String is a method descriptor,
     *         <code>false</code> otherwise.
     */
    public static boolean isInternalMethodDescriptor(String internalDescriptor)
    {
        return internalDescriptor.charAt(0) == ClassConstants.INTERNAL_METHOD_ARGUMENTS_OPEN;
    }


    /**
     * Returns whether the given member String represents an external method
     * name with arguments.
     * @param externalMemberNameAndArguments the external member String,
     *                                       e.g. "<code>myField</code>" or
     *                                       e.g. "<code>myMethod(int,int)</code>".
     * @return <code>true</code> if the given String refers to a method,
     *         <code>false</code> otherwise.
     */
    public static boolean isExternalMethodNameAndArguments(String externalMemberNameAndArguments)
    {
        return externalMemberNameAndArguments.indexOf(ClassConstants.EXTERNAL_METHOD_ARGUMENTS_OPEN) > 0;
    }


    /**
     * Returns the name part of the given external method name and arguments.
     * @param externalMethodNameAndArguments the external method name and arguments,
     *                                       e.g. "<code>myMethod(int,int)</code>".
     * @return the name part of the String, e.g. "<code>myMethod</code>".
     */
    public static String externalMethodName(String externalMethodNameAndArguments)
    {
        externalTypeEnumeration.setDescriptor(externalMethodNameAndArguments);
        return externalTypeEnumeration.methodName();
    }


    /**
     * Converts the given external method return type and name and arguments to
     * an internal method descriptor.
     * @param externalReturnType             the external method return type,
     *                                       e.g. "<code>boolean</code>".
     * @param externalMethodNameAndArguments the external method name and arguments,
     *                                       e.g. "<code>myMethod(int,int)</code>".
     * @return the internal method descriptor,
     *                                       e.g. "<code>(II)Z</code>".
     */
    public static String internalMethodDescriptor(String externalReturnType,
                                                  String externalMethodNameAndArguments)
    {
        StringBuffer internalMethodDescriptor = new StringBuffer();
        internalMethodDescriptor.append(ClassConstants.INTERNAL_METHOD_ARGUMENTS_OPEN);

        externalTypeEnumeration.setDescriptor(externalMethodNameAndArguments);
        while (externalTypeEnumeration.hasMoreTypes())
        {
            internalMethodDescriptor.append(internalType(externalTypeEnumeration.nextType()));
        }

        internalMethodDescriptor.append(ClassConstants.INTERNAL_METHOD_ARGUMENTS_CLOSE);
        internalMethodDescriptor.append(internalType(externalReturnType));

        return internalMethodDescriptor.toString();
    }


    /**
     * Converts the given external method return type and List of arguments to
     * an internal method descriptor.
     * @param externalReturnType the external method return type,
     *                                       e.g. "<code>boolean</code>".
     * @param externalArguments the external method arguments,
     *                                       e.g. <code>{ "int", "int" }</code>.
     * @return the internal method descriptor,
     *                                       e.g. "<code>(II)Z</code>".
     */
    public static String internalMethodDescriptor(String externalReturnType,
                                                  List   externalArguments)
    {
        StringBuffer internalMethodDescriptor = new StringBuffer();
        internalMethodDescriptor.append(ClassConstants.INTERNAL_METHOD_ARGUMENTS_OPEN);

        for (int index = 0; index < externalArguments.size(); index++)
        {
            internalMethodDescriptor.append(internalType((String)externalArguments.get(index)));
        }

        internalMethodDescriptor.append(ClassConstants.INTERNAL_METHOD_ARGUMENTS_CLOSE);
        internalMethodDescriptor.append(internalType(externalReturnType));

        return internalMethodDescriptor.toString();
    }


    /**
     * Converts an internal field description into an external full field description.
     * @param accessFlags             the access flags of the field.
     * @param fieldName               the field name,
     *                                e.g. "<code>myField</code>".
     * @param internalFieldDescriptor the internal field descriptor,
     *                                e.g. "<code>Z</code>".
     * @return the external full field description,
     *                                e.g. "<code>public boolean myField</code>".
     */
    public static String externalFullFieldDescription(int    accessFlags,
                                                      String fieldName,
                                                      String internalFieldDescriptor)
    {
        return externalFieldAccessFlags(accessFlags) +
               externalType(internalFieldDescriptor) +
               " " +
               fieldName;
    }


    /**
     * Converts an internal method description into an external full method description.
     * @param internalClassName        the internal name of the class of the method,
     *                                 e.g. "<code>mypackage/MyClass</code>".
     * @param accessFlags              the access flags of the method.
     * @param internalMethodName       the internal method name,
     *                                 e.g. "<code>myMethod</code>" or
     *                                      "<code>&lt;init&gt;</code>".
     * @param internalMethodDescriptor the internal method descriptor,
     *                                 e.g. "<code>(II)Z</code>".
     * @return the external full method description,
     *                                 e.g. "<code>public boolean myMethod(int,int)</code>" or
     *                                      "<code>public MyClass(int,int)</code>".
     */
    public static String externalFullMethodDescription(String internalClassName,
                                                       int    accessFlags,
                                                       String internalMethodName,
                                                       String internalMethodDescriptor)
    {
        return externalMethodAccessFlags(accessFlags) +
               externalMethodReturnTypeAndName(internalClassName,
                                               internalMethodName,
                                               internalMethodDescriptor) +
               ClassConstants.EXTERNAL_METHOD_ARGUMENTS_OPEN +
               externalMethodArguments(internalMethodDescriptor) +
               ClassConstants.EXTERNAL_METHOD_ARGUMENTS_CLOSE;
    }


    /**
     * Converts internal class access flags into an external access description.
     * @param accessFlags the class access flags.
     * @return the external class access description,
     *         e.g. "<code>public final </code>".
     */
    public static String externalClassAccessFlags(int accessFlags)
    {
        return externalClassAccessFlags(accessFlags, "");
    }


    /**
     * Converts internal class access flags into an external access description.
     * @param accessFlags the class access flags.
     * @param prefix      a prefix that is added to each access modifier.
     * @return the external class access description,
     *         e.g. "<code>public final </code>".
     */
    public static String externalClassAccessFlags(int accessFlags, String prefix)
    {
        if (accessFlags == 0)
        {
            return EMPTY_STRING;
        }

        StringBuffer string = new StringBuffer(50);

        if ((accessFlags & ClassConstants.INTERNAL_ACC_PUBLIC) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_PUBLIC).append(" ");
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_FINAL) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_FINAL).append(" ");
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_INTERFACE) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_INTERFACE).append(" ");
        }
        else
        {
            if ((accessFlags & ClassConstants.INTERNAL_ACC_ABSTRACT) != 0)
            {
                string.append(prefix).append(ClassConstants.EXTERNAL_ACC_ABSTRACT).append(" ");
            }
        }

        return string.toString();
    }


    /**
     * Converts internal field access flags into an external access description.
     * @param accessFlags the field access flags.
     * @return the external field access description,
     *         e.g. "<code>public volatile </code>".
     */
    public static String externalFieldAccessFlags(int accessFlags)
    {
        return externalFieldAccessFlags(accessFlags, "");
    }


    /**
     * Converts internal field access flags into an external access description.
     * @param accessFlags the field access flags.
     * @param prefix      a prefix that is added to each access modifier.
     * @return the external field access description,
     *         e.g. "<code>public volatile </code>".
     */
    public static String externalFieldAccessFlags(int accessFlags, String prefix)
    {
        if (accessFlags == 0)
        {
            return EMPTY_STRING;
        }

        StringBuffer string = new StringBuffer(50);

        if ((accessFlags & ClassConstants.INTERNAL_ACC_PUBLIC) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_PUBLIC).append(" ");
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_PRIVATE) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_PRIVATE).append(" ");
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_PROTECTED) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_PROTECTED).append(" ");
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_STATIC) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_STATIC).append(" ");
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_FINAL) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_FINAL).append(" ");
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_VOLATILE) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_VOLATILE).append(" ");
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_TRANSIENT) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_TRANSIENT).append(" ");
        }

        return string.toString();
    }


    /**
     * Converts internal method access flags into an external access description.
     * @param accessFlags the method access flags.
     * @return the external method access description,
     *                    e.g. "<code>public synchronized </code>".
     */
    public static String externalMethodAccessFlags(int accessFlags)
    {
        return externalMethodAccessFlags(accessFlags, "");
    }


    /**
     * Converts internal method access flags into an external access description.
     * @param accessFlags the method access flags.
     * @param prefix      a prefix that is added to each access modifier.
     * @return the external method access description,
     *                    e.g. "public synchronized ".
     */
    public static String externalMethodAccessFlags(int accessFlags, String prefix)
    {
        if (accessFlags == 0)
        {
            return EMPTY_STRING;
        }

        StringBuffer string = new StringBuffer(50);

        if ((accessFlags & ClassConstants.INTERNAL_ACC_PUBLIC) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_PUBLIC).append(" ");
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_PRIVATE) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_PRIVATE).append(" ");
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_PROTECTED) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_PROTECTED).append(" ");
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_STATIC) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_STATIC).append(" ");
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_FINAL) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_FINAL).append(" ");
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_SYNCHRONIZED) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_SYNCHRONIZED).append(" ");
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_NATIVE) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_NATIVE).append(" ");
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_ABSTRACT) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_ABSTRACT).append(" ");
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_STRICT) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_STRICT).append(" ");
        }

        return string.toString();
    }


    /**
     * Converts an internal method descriptor into an external method return type.
     * @param internalMethodDescriptor the internal method descriptor,
     *                                 e.g. "<code>(II)Z</code>".
     * @return the external method return type,
     *                                 e.g. "<code>boolean</code>".
     */
    public static String externalMethodReturnType(String internalMethodDescriptor)
    {
        return externalType(internalMethodReturnType(internalMethodDescriptor));
    }


    /**
     * Converts an internal class name, method name, and method descriptor to
     * an external method return type and name.
     * @param internalClassName        the internal name of the class of the method,
     *                                 e.g. "<code>mypackage/MyClass</code>".
     * @param internalMethodName       the internal method name,
     *                                 e.g. "<code>myMethod</code>" or
     *                                      "<code>&lt;init&gt;</code>".
     * @param internalMethodDescriptor the internal method descriptor,
     *                                 e.g. "<code>(II)Z</code>".
     * @return the external method return type and name,
     *                                 e.g. "<code>boolean myMethod</code>" or
     *                                      "<code>MyClass</code>".
     */
    private static String externalMethodReturnTypeAndName(String internalClassName,
                                                          String internalMethodName,
                                                          String internalMethodDescriptor)
    {
        return internalMethodName.equals(ClassConstants.INTERNAL_METHOD_NAME_INIT) ?
                    (ClassUtil.externalShortClassName(
                     ClassUtil.externalClassName(internalClassName))) :
                    (ClassUtil.externalMethodReturnType(internalMethodDescriptor) +
                     " " +
                     internalMethodName);
    }


    /**
     * Converts an internal method descriptor into an external method argument
     * description.
     * @param internalMethodDescriptor the internal method descriptor,
     *                                 e.g. "<code>(II)Z</code>".
     * @return the external method argument description,
     *                                 e.g. "<code>int,int</code>".
     */
    public static String externalMethodArguments(String internalMethodDescriptor)
    {
        StringBuffer externalMethodNameAndArguments = new StringBuffer();

        internalTypeEnumeration.setDescriptor(internalMethodDescriptor);
        while (internalTypeEnumeration.hasMoreTypes())
        {
            externalMethodNameAndArguments.append(externalType(internalTypeEnumeration.nextType()));
            if (internalTypeEnumeration.hasMoreTypes())
            {
                externalMethodNameAndArguments.append(ClassConstants.EXTERNAL_METHOD_ARGUMENTS_SEPARATOR);
            }
        }

        return externalMethodNameAndArguments.toString();
    }


    /**
     * Returns the internal package name of the given internal class name.
     * @param internalClassName the internal class name,
     *                          e.g. "<code>java/lang/Object</code>".
     * @return the internal package name,
     *                          e.g. "<code>java/lang</code>".
     */
    public static String internalPackageName(String internalClassName)
    {
        int lastSeparatorIndex = internalClassName.lastIndexOf(ClassConstants.INTERNAL_PACKAGE_SEPARATOR);
        if (lastSeparatorIndex < 0)
        {
            lastSeparatorIndex = 0;
        }

        return internalClassName.substring(0, lastSeparatorIndex);
    }


    /**
     * Returns the external package name of the given external class name.
     * @param externalClassName the external class name,
     *                          e.g. "<code>java.lang.Object</code>".
     * @return the external package name,
     *                          e.g. "<code>java.lang</code>".
     */
    public static String externalPackageName(String externalClassName)
    {
        int lastSeparatorIndex = externalClassName.lastIndexOf(ClassConstants.EXTERNAL_PACKAGE_SEPARATOR);
        if (lastSeparatorIndex < 0)
        {
            lastSeparatorIndex = 0;
        }

        return externalClassName.substring(0, lastSeparatorIndex);
    }
}
