package com.nexusbpm.editor.component;

/**
 * A validation message indicating an error or warning in the user's design
 * of a flow (such as having the true/false edge names of a conditional
 * component not match the names of the control flow edges).
 *
 * @author Dean Mao
 * @created May 27, 2004
 */
public class ValidationMessage {

	/**
	 * Constant denoting a validation message that is an error.
	 */
	public static final boolean ERROR = true;
	/**
	 * Constant denoting a validation message that is a warning.
	 */
	public static final boolean WARNING = false;

	private String message;

	private Component referringComponent;

//	private ComponentEdge referringEdge;

	private String attributeName;

	private boolean isError;

	/**
	 * Creates a validation message for the given attribute of the given
	 * component.
	 *
	 * @param message            a description of the validation warning/error.
	 * @param referringComponent the component giving the warning.
	 * @param isError            whether the message is a warning or an error.
	 * @param attributeName      the attribute that the warning refers to.
	 */
	public ValidationMessage( String message, Component referringComponent, boolean isError, String attributeName ) {
		setReferringComponent( referringComponent );
		setMessage( message );
		setError( isError );
		setAttributeName( attributeName );
	}

	/**
	 * Creates a validation message for the given component.
	 *
	 * @param message            a description of the validation warning/error.
	 * @param referringComponent the component that the validation message
	 *                           refers to.
	 * @param isError            whether the message is a warning or an error.
	 */
	public ValidationMessage( String message, Component referringComponent, boolean isError ) {
		this( message, referringComponent, isError, null );
	}

	/**
	 * Creates a validation message for the given edge.
	 *
	 * @param message       a description of the validation warning/error.
	 * @param referringEdge the edge that the validation message refers to.
	 * @param isError       whether the message is a warning or an error.
	 */
//	public ValidationMessage( String message, ComponentEdge referringEdge, boolean isError ) {
//		setReferringEdge( referringEdge );
//		setMessage( message );
//		setError( isError );
//	}

	/**
	 * @return the description of the validation warning/error.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return the component that the validation message refers to.
	 */
	public Component getReferringComponent() {
		return referringComponent;
	}

	/**
	 * Sets the description of the validation warning/error.
	 *
	 * @param string a description of the validation warning/error.
	 */
	public void setMessage( String string ) {
		message = string;
	}

	/**
	 * Sets the component that the validation message refers to.
	 *
	 * @param component the component that the validation message refers to.
	 */
	public void setReferringComponent( Component component ) {
		referringComponent = component;
	}

	/**
	 * @return the edge that the validation message refers to.
	 */
//	public ComponentEdge getReferringEdge() {
//		return referringEdge;
//	}
//
	/**
	 * Sets the edge that the validation message refers to.
	 *
	 * @param edge the edge that the validation message refers to.
	 */
//	public void setReferringEdge( ComponentEdge edge ) {
//		referringEdge = edge;
//	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return message;
	}

	/**
	 * @return whether the validation message is a warning or error.
	 * @see #WARNING
	 * @see #ERROR
	 */
	public boolean isError() {
		return isError;
	}

	/**
	 * Sets whether the validation message is a warning or error.
	 *
	 * @param b whether the validation message is a warning or error.
	 * @see #WARNING
	 * @see #ERROR
	 */
	public void setError( boolean b ) {
		isError = b;
	}

	/**
	 * @return the name of the attribute that the validation message refers to.
	 */
	public String getAttributeName() {
		return attributeName;
	}

	/**
	 * Sets the name of the attribute that the validation message refers to.
	 *
	 * @param string the name of the attribute that the validation message
	 *               refers to.
	 */
	public void setAttributeName( String string ) {
		attributeName = string;
	}

}
