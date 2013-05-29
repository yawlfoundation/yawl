

/*
 * 
 * @author Moe Thandar Wynn
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
 
package org.yawlfoundation.yawl.analyser.reductionrules;

import org.yawlfoundation.yawl.analyser.elements.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Reduction rule for RWF-net: DEAR rule
 */
public class DEARrule extends ResetReductionRule {

   /**
    * Innermost method for a reduction rule.
    * @param net ResetWFNet to perform reduction
    * @param nextElement one element for consideration.
    * @return a reduced ResetWFNet or null if a given net cannot be reduced.
    * Must be implemented by individual reduction rule
    */
   public ResetWFNet reduceElement(ResetWFNet net, RElement nextElement) {
       if (nextElement instanceof RPlace) {
           RPlace s = (RPlace) nextElement;
           Set<RElement> postSet = s.getPostsetElements();
           Set<RElement> preSet = s.getPresetElements();

           //\post{s} = t && s cannot be reset
           //s cannot be input place - pre empty
           if (! preSet.isEmpty() && postSet.size() == 1 &&
                   s.getCancelledBySet().isEmpty()) {

               RTransition t = (RTransition) postSet.iterator().next();
               Set<RElement> preSetOft = new HashSet<RElement>(t.getPresetElements());

               //\pre{t} = s && t does not have reset arcs
               if (preSetOft.size() == 1 && t.getRemoveSet().isEmpty()) {
                   Set<RElement> preSetOfs = new HashSet<RElement>(s.getPresetElements());
                   Set<RElement> postpreSetOfs = ResetWFNet.getPostset(preSetOfs);
                   Set<RElement> postSetOft = new HashSet<RElement>(t.getPostsetElements());
                   postpreSetOfs.retainAll(postSetOft);

                   //no direct connection pre s x post t is empty
                   //all output of t are not reset places
                   if (postpreSetOfs.isEmpty() && !isResetPlaces(postSetOft) &&
                           isNotOutput(postSetOft) && isNotInput(preSetOfs)) {

                       // connections from pre s to post t
                       Set<RElement> preSetOfs2 = new HashSet<RElement>(s.getPresetElements());
                       for (RElement prenext : preSetOfs2) {
                           prenext.addToResetMappings(s.getResetMappings());
                           prenext.addToResetMappings(t.getResetMappings());

                           for (RElement postnext : postSetOft) {
                               prenext.setPostset(new RFlow(prenext,postnext));
                               postnext.addToResetMappings(s.getResetMappings());
                               postnext.addToResetMappings(t.getResetMappings());
                           }
                       }
                   }

                   //remove s and t
                   net.removeNetElement(s);
                   net.removeNetElement(t);
                   return net;
               }
           }
       }
       return null;
   }


    private boolean isResetPlaces(Set<RElement> places) {
        for (RElement place : places) {
            if (!place.getCancelledBySet().isEmpty()) return true;
        }
        return false;
    }


    private boolean isNotOutput(Set<RElement> places) {
        for (RElement place : places) {
            if (place.getPostsetElements().isEmpty()) return false;
        }
        return true;
    }


    private boolean isNotInput(Set<RElement> places) {
        for (RElement place : places) {
            if (place.getPresetElements().isEmpty()) return false;
        }
        return true;
    }
}