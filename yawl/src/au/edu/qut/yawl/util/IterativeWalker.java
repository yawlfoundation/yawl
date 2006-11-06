/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

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
