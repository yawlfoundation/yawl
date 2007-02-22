/*
 * Created on 16/02/2006
 * YAWLEditor v1.4 
 *
 * @author Moe Thandar Wyn
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package au.edu.qut.yawl.editor.analyser;

import au.edu.qut.yawl.elements.*;
import au.edu.qut.yawl.unmarshal.YMarshal;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.exceptions.*;

import java.io.IOException;
import java.util.*;
import org.jdom.JDOMException;


import au.edu.qut.yawl.editor.reductionrules.*;

public class YAWLResetAnalyser {

  public static final String RESET_NET_ANALYSIS_PREFERENCE = "resetNetAnalysisCheck";

  public static final String SOUNDNESS_ANALYSIS_PREFERENCE = "resetSoundnessCheck";

  public static final String WEAKSOUNDNESS_ANALYSIS_PREFERENCE = "resetWeakSoundnessCheck";

  public static final String CANCELLATION_ANALYSIS_PREFERENCE = "resetCancellationCheck";

  public static final String ORJOIN_ANALYSIS_PREFERENCE = "resetOrjoinCheck";

  public static final String SHOW_OBSERVATIONS_PREFERENCE = "resetShowObservationsCheck";

  public static final String USE_YAWLREDUCTIONRULES_PREFERENCE = "yawlReductionRules";

  public static final String USE_RESETREDUCTIONRULES_PREFERENCE = "resetReductionRules";

  /**
   * This method is used for the analysis.
   * @fileURL - xml file format of the YAWL model
   * @options - four options (weak soundness w, soundness s, cancellation set c, orjoins o) 
   * @useYAWLReductionRules - reduce the net using YAWL reduction rules
   * @useResetReductionRules - reduce the net using Reset reduction rules
   * returns a xml formatted string with warnings and observations.
   */
  public String analyse(String fileURL, String options,
      boolean useYAWLReductionRules, boolean useResetReductionRules)
      throws IOException, YSchemaBuildingException, YSyntaxException,
      JDOMException {

    YSpecification specs = null;
    try {
      specs = (YSpecification) YMarshal.unmarshalSpecifications(fileURL).get(0);
    } catch (YPersistenceException ype) {
      ype.printStackTrace();
      return null;
    }

    StringBuffer msgBuffer = new StringBuffer(200);

    //   boolean useYAWLReductionRules = true;
    //   boolean useResetReductionRules = false;

    YDecomposition decomposition;
    YNet decomRootNet, reducedYNet;
    ResetWFNet decomResetNet, reducedNet;

    YSpecification newSpecification = specs;

    Set decompositions = new HashSet(specs.getDecompositions());
    if (decompositions.size() > 0) {
      for (Iterator iterator = decompositions.iterator(); iterator.hasNext();) {
        decomposition = (YDecomposition) iterator.next();
        if (decomposition instanceof YNet) {

          decomRootNet = (YNet) decomposition;
          //reduction rules
          if (useYAWLReductionRules) {
            reducedYNet = reduceNet(decomRootNet);
            if (reducedYNet != null) {
              decomRootNet = reducedYNet;
              //  newSpecification.setDecomposition((YDecomposition) reducedYNet);     
            }
          }

          decomResetNet = new ResetWFNet(decomRootNet);
          YAWLReachabilityUtils utils = new YAWLReachabilityUtils(decomRootNet);

          if (useResetReductionRules) {
            reducedNet = reduceNet(decomResetNet);
            if (reducedNet != null) {
              decomResetNet = reducedNet;
            }
          }
          if (options.indexOf("w") >= 0) {
            msgBuffer.append(decomResetNet.checkWeakSoundness());
          }

          //checking rechability set for bounded nets
          if (options.indexOf("s") >= 0) {
            try {
              if (decomResetNet.containsORjoins()) {
                msgBuffer.append(utils.checkSoundness());
              } else {
                msgBuffer.append(decomResetNet.checkSoundness());
              }

            } catch (Exception e) {
               msgBuffer.append(formatXMLMessage(e.toString(), false));
            }

          }

          if (options.indexOf("c") >= 0) {
            try {
              if (decomResetNet.containsORjoins()) {

                msgBuffer.append(utils.checkCancellationSets());
              } else {
                msgBuffer.append(decomResetNet.checkCancellationSets());
              }

            } catch (ClassCastException ex) {

              System.out.println(ex.getStackTrace().toString()
                  + ex.getMessage());
              
            } catch (Exception e) {
              msgBuffer.append(formatXMLMessage(e.toString(), false));

            }

          }

          //do unnecessary orjoin checks
          if (options.indexOf("o") >= 0) {
            try {
              if (decomResetNet.containsORjoins()) {

                msgBuffer.append(utils.checkUnnecessaryORJoins());
              } else {
                msgBuffer.append(formatXMLMessage(
                    "There are no OR-joins in the net " + decomRootNet.getId()
                        + ".", true));
              }

            }

            catch (Exception e) {
              msgBuffer.append(formatXMLMessage(e.toString(), false));
            }

          }

        } //endif
      } //end for
    }//end if

    // }//end while

    return formatXMLMessageForEditor(msgBuffer.toString());

  }

  private YNet reduceNet(YNet originalNet) {
   // System.out.println(" Original net:"+ originalNet.getNetElements().size());
    YAWLReductionRule rule;

    YNet reducedNet_t, reducedNet;
    reducedNet = originalNet;
    int loop = 0;
    String rules = "FSPY";
    String rulesmsg = "";

    do {
      loop++;
      rule = new FSPYrule();
      reducedNet_t = rule.reduce(reducedNet);
      if (reducedNet_t == null) {
        rules = "FSTY";
        rule = new FSTYrule();
        reducedNet_t = rule.reduce(reducedNet);

        if (reducedNet_t == null) {
          rules = "FPPY";
          rule = new FPPYrule();
          reducedNet_t = rule.reduce(reducedNet);

          if (reducedNet_t == null) {
            rules = "FPTY";
            rule = new FPTYrule();
            reducedNet_t = rule.reduce(reducedNet);

            if (reducedNet_t == null) {
              rules = "FAPY";
              rule = new FAPYrule();
              reducedNet_t = rule.reduce(reducedNet);

              if (reducedNet_t == null) {
                rules = "FATY";
                rule = new FATYrule();
                reducedNet_t = rule.reduce(reducedNet);

                if (reducedNet_t == null) {
                  rules = "ELTY";
                  rule = new ELTYrule();
                  reducedNet_t = rule.reduce(reducedNet);

                  if (reducedNet_t == null) {
                    rules = "FXOR";
                    rule = new FXORrule();
                    reducedNet_t = rule.reduce(reducedNet);

                    if (reducedNet_t == null) {
                      rules = "FAND";
                      rule = new FANDrule();
                      reducedNet_t = rule.reduce(reducedNet);

                      if (reducedNet_t == null) {
                        rules = "FOR";
                        rule = new FIErule();
                        reducedNet_t = rule.reduce(reducedNet);

                        if (reducedNet_t == null) {
                          rules = "FIE";
                          rule = new FORrule();
                          reducedNet_t = rule.reduce(reducedNet);
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }// 10 endif
      if (reducedNet_t == null) { //if (reducedNet != originalNet)
        //{  
        loop--;
           //  System.out.println("YAWL Reduced net "+ loop + "rules "+ rulesmsg+ " size:"+ reducedNet.getNetElements().size());
        return reducedNet;
        //}
        //else 
        //return null; 
      } else {
        rulesmsg += rules;
        reducedNet = reducedNet_t;

      }
    } while (reducedNet != null);//end while
    return null;

  }

  private ResetWFNet reduceNet(ResetWFNet originalNet) {
    // System.out.println(" Original net:"+ originalNet.getNetElements().size());
    ResetReductionRule rule;

    ResetWFNet reducedNet_t, reducedNet;
    // reducedNet = originalNet;
    //a copy of original net
    reducedNet = new ResetWFNet(originalNet);
    String rules = "FSPR";
    String rulesmsg = "";
    int loop = 0;
    do {
      loop++;
      rule = new FSPRrule();
      reducedNet_t = rule.reduce(reducedNet);

      if (reducedNet_t == null) {
        rules = "FSTR";
        rule = new FSTRrule();
        reducedNet_t = rule.reduce(reducedNet);

        if (reducedNet_t == null) {
          rules = "FPPR";
          rule = new FPPRrule();
          reducedNet_t = rule.reduce(reducedNet);

          if (reducedNet_t == null) {
            rules = "FPTR";
            rule = new FPTRrule();
            reducedNet_t = rule.reduce(reducedNet);

            if (reducedNet_t == null) {
              rules = "DEAR";
              rule = new DEARrule();
              reducedNet_t = rule.reduce(reducedNet);

              if (reducedNet_t == null) {
                rules = "ELTR";
                rule = new ELTRrule();
                reducedNet_t = rule.reduce(reducedNet);

                if (reducedNet_t == null) {
                  rules = "FESR";
                  rule = new FESRrule();
                  reducedNet_t = rule.reduce(reducedNet);
                }

              }
            }
          }
        }

      }//5 endif
      if (reducedNet_t == null) {
        if (reducedNet != originalNet) {
          loop--;
          //  System.out.println("Reset Reduced net "+ loop + "rules "+ rulesmsg+ " size:"+ reducedNet.getNetElements().size());
          return reducedNet;
        } else {
          return null;
        }
      } else {
        rulesmsg += rules;
        reducedNet = reducedNet_t;
      }
    } while (reducedNet != null);//end while
    return null;

  }

  public String formatXMLMessageForEditor(String msg) {
    StringBuffer msgBuffer = new StringBuffer(200);
    msgBuffer.append("<wofyawl><net><behavior>");
    msgBuffer.append(msg);
    msgBuffer.append("</behavior></net></wofyawl>");

    return msgBuffer.toString();

  }

  /**
   * used for formatting xml messages.
   * Message could be a warning or observation. 
   */
  private String formatXMLMessage(String msg, boolean isObservation) {
    StringBuffer msgBuffer = new StringBuffer(200);
    if (isObservation) {
      msgBuffer.append("<observation>");
      msgBuffer.append(msg);
      msgBuffer.append("</observation>");

    } else {
      msgBuffer.append("<warning>");
      msgBuffer.append(msg);
      msgBuffer.append("</warning>");
    }

    return msgBuffer.toString();
  }
 

 
}