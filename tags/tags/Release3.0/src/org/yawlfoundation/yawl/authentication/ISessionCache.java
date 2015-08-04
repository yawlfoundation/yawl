package org.yawlfoundation.yawl.authentication;

/**
 * @author Michael Adams
 * @date 13/10/13
 */
public interface ISessionCache {

    public String connect(String name, String password, long timeOutSeconds);

    public boolean checkConnection(String handle);

    public YAbstractSession getSession(String handle);

    public void expire(String handle);

    public void disconnect(String handle);

    public void shutdown();

}
