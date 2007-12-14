package org.yawlfoundation.yawl.editor.thirdparty.engine;

import java.util.LinkedList;
import java.util.List;

import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.util.YVerificationMessage;

/**
 * A class whose sole responsibility is to provide engine validation results of the current specification.
 * This class should have no user-interface effects.
 * @author Lindsay Bradford
 *
 */

public class EngineSpecificationValidator {
  
  public static String NO_PROBLEMS_MESSAGE = "No design-time engine validation problems were found in this specification.";
  
  public static List getValidationResults() {
    return getValidationResults(SpecificationModel.getInstance());
  }

  public static List getValidationResults(SpecificationModel specification) {
    return getValidationResults(
        EngineSpecificationExporter.getEngineSpecAsEngineObjects(
            specification
        )
    );
  }
  
  public static List getValidationResults(YSpecification specification) {
    return createProblemListFrom(specification.verify());
  }
  
  private static List createProblemListFrom(List verificationList) {
    LinkedList problemList = new LinkedList();
    
    for(int i = 0; i < verificationList.size(); i++) {
      YVerificationMessage message = (YVerificationMessage) verificationList.get(i);
      String messageString = message.getMessage();
      
      if (messageString.indexOf("composite task may not decompose to other than a net") != -1) {
        continue;
      }
      if (messageString.indexOf("is not registered with engine.") != -1) {
        // We have no running engine when validating, so this is not valid.
        continue;
      }
      
      messageString = messageString.replaceAll("postset size","outgoing flow number");
      messageString = messageString.replaceAll("preset size","incomming flow number");
      messageString = messageString.replaceAll("Check the empty tasks linking from i to o.",
                                               "Should all atomic tasks in the net have no decomposition?");
      messageString = messageString.replaceAll("from i to o","between the input and output conditions");
      messageString = messageString.replaceAll("InputCondition","Input Condition");
      messageString = messageString.replaceAll("OutputCondition","Output Condition");

      messageString = messageString.replaceAll("ExternalCondition","Condition");
      messageString = messageString.replaceAll("AtomicTask","Atomic Task");
      messageString = messageString.replaceAll("CompositeTask","Composite Task");
      messageString = messageString.replaceAll("The net \\(Net:","The net (");
      messageString = messageString.replaceAll("composite task must contain a net","must unfold to some net");
      
      problemList.add(messageString);
    }
    if (problemList.size() == 0) {
      problemList.add(NO_PROBLEMS_MESSAGE);
    }
    return problemList;
  }
}
