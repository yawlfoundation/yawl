package com.nexusbpm.editor.logger;

import java.awt.Color;

/**
 * The model for a cell; holds a foreground color and object to be displayed.
 * @author hoy
 */
public class CellModel
{
	private Object obj;
	private Color color;

	/**
	 * Creates a new cell model with the given foreground color and object to
	 * display.
	 * @param color the color of the text to display
	 * @param obj the object to display.
	 */
	public CellModel(Color color, Object obj)
	{
		this.obj = obj;
		this.color = color;
	}

	/**
	 * @return the object to display.
	 */
	public Object getObj()
	{
		return obj;
	}

	/**
	 * Sets the object to display.
	 * @param object the object to display.
	 */
	public void setObj(Object object)
	{
		obj = object;
	}

	/**
	 * @return the foreground color for the cell.
	 */
	public Color getColor()
	{
		return color;
	}

	/**
	 * Sets the foreground color for the cell.
	 * @param color the foreground color for the cell.
	 */
	public void setColor(Color color)
	{
		this.color = color;
	}

	/**
	 * Implement so Ctrl+C copies something intelligible from the log table.
	 * @return a string version of the object for this cell.
	 */
	public String toString()
	{
		return this.obj.toString();
	}
}