package org.yawlfoundation.yawl.editor.swing.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.io.Serializable;

/**
 * This class represents a value that can not be set until the final "read" on XML export.
 *
 * @author Mike Fowler
 *         Date: Nov 2, 2005
 */
public class DynamicValue implements Serializable
{
    private String property;
    private Object target;

    public DynamicValue()
    {
        target = property = null;
    }

    public DynamicValue(String property, Object target)
    {
        this.property = property;
        this.target = target;
    }

    /**
     * Invokes the method on target and returns the result as a string.
     *
     * @return a string representation of method's invocation against target.
     */
    public String toString()
    {
        Object result = null;

        Method[] methods = target.getClass().getMethods();

        //identify the most appropriate "accessor" method
        for (int i = 0; i < methods.length; i++)
        {
            if (methods[i].getParameterTypes().length == 0) //no parameter methods only
            {
                String name = methods[i].getName();
                if (name.toLowerCase().equals("get" + property.toLowerCase()) ||
                    name.toLowerCase().equals("is" + property.toLowerCase()))
                {
                    try
                    {
                        result = methods[i].invoke(target, new Object[]{});
                        break;
                    }
                    catch (IllegalAccessException e)
                    {
                        //todo propogate?
                    }
                    catch (InvocationTargetException e)
                    {
                        //todo propogate?
                    }
                }
            }
        }

        if (result != null)
            return result.toString();
        else
            return "";
    }

    public String getProperty()
    {
        return property;
    }

    public void setProperty(String property)
    {
        this.property = property;
    }

    public Object getTarget()
    {
        return target;
    }

    public void setTarget(Object target)
    {
        this.target = target;
    }
}
