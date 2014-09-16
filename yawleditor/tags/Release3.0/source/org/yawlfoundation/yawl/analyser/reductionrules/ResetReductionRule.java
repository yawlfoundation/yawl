
/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

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

import org.yawlfoundation.yawl.analyser.elements.RElement;
import org.yawlfoundation.yawl.analyser.elements.ResetWFNet;

public abstract class ResetReductionRule {

   /**
    * Main method for calling reduction rule, apply a reduction rule recursively 
    * to a given net until it cannot be reduced further.
    * @param net ResetWFNet to perform reduction
    * returns a reduced ResetWFNet or null if a given net cannot be reduced.
    */
	public ResetWFNet reduce(ResetWFNet net) {
	    ResetWFNet reducedNet = reduceNet(net);
        while (reducedNet != null) {
            ResetWFNet temp = reduceNet(reducedNet);
            if (temp == null) break;
            reducedNet = temp;
        }
        return reducedNet;
	}
	
   /**
    * Inner method for a reduction rule.
    * Go through all elements in a ResetWFNet.
    * @param net ResetWFNet to perform reduction
    * returns a reduced ResetWFNet or null if a given net cannot be reduced.
    */
    private ResetWFNet reduceNet(ResetWFNet net) {
        ResetWFNet reducedNet = null;
        for (RElement element : net.getNetElements().values()) {
            reducedNet = reduceElement(net, element);
            if (reducedNet != null) {
                return reducedNet;
            }
        }
        return reducedNet;
    }

 
   /**
    * Innermost method for a reduction rule.
    * @param net ResetWFNet to perform reduction
    * @param element one element for consideration.
    * returns a reduced ResetWFNet or null if a given net cannot be reduced.
    * Must be implemented by individual reduction rule
    */
    protected abstract ResetWFNet reduceElement(ResetWFNet net, RElement element);

}