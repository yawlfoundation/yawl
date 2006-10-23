package au.edu.qut.yawl.util;

import java.util.Collection;
import java.util.HashSet;

/**
 * not yet used but purpose is to provide framework for walking over sets of things...
 * 
 * @author matthew sandoz
 *
 */
public abstract class IterativeWalker {

	public void visit(Collection startToVisit, Collection startVisited) {
		Collection visited = new HashSet(startVisited);
		Collection toVisit = new HashSet(startToVisit);
		while(!toVisit.isEmpty()) {
			doSomething(toVisit);
			toVisit.removeAll(visited);
			visited.addAll(toVisit);
			toVisit = getNextSetFrom(toVisit);
		}
	}
	public abstract void doSomething(Collection toVisit);
	public abstract Collection getNextSetFrom(Collection toGetNextSetFrom);
}
