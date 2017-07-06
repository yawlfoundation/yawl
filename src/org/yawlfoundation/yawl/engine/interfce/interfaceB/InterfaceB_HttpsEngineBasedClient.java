package org.yawlfoundation.yawl.engine.interfce.interfaceB;

/**
 * @author Michael Adams
 * @date 6/7/17
 */
public class InterfaceB_HttpsEngineBasedClient extends InterfaceB_EngineBasedClient {

    /**
      * Indicates which protocol this shim services.
      * @return the scheme
      */
     public String getScheme() { return "https"; }

}
