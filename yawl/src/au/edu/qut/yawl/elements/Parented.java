package au.edu.qut.yawl.elements;

public interface Parented<Type> {
	public Type getParent();
	public void setParent(Type t);
}
