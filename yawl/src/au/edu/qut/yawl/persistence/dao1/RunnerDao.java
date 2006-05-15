/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.persistence.dao1;

import java.util.Set;

import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.YNetRunner;

/**
 * Interface to replace the static HashMaps that existed in YNetRunner
 * 
 * @author Dean Mao
 * @created Oct 27, 2005
 */
public interface RunnerDao {
	// Engine specific interfaces
	public YNetRunner loadNetRunner(String caseID);
	public YNetRunner loadNetRunner(YIdentifier caseID);
	public void storeNetRunner(YNetRunner netRunner, String specID);
	public void removeNetRunner(YIdentifier caseIDForNet);
	public Set getNetRunnerCaseIDs();
	public Set getRunningCaseIDs();
	public Set getRunningSpecIDs();
	public String getRunningSpecID(YIdentifier runningCaseID);
}
